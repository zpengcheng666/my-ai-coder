package org.example.ai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * 文档处理主流程,涵盖增量检测、流式读取、并行处理、质量过滤等.
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
@Slf4j
public class DocumentProcessingService {

    private final DocumentProcessingStateTracker stateTracker;
    private final RagUtils ragUtils;

    @Value("${rag.large-file.threshold-bytes:5242880}") // 5MB
    private long largeFileThresholdBytes;

    @Value("${rag.large-file.stream-chunk-size:262144}") // 256KB
    private int streamChunkSize;

    @Value("${rag.processing.include-hidden:false}")
    private boolean includeHiddenFiles;

    @Value("${rag.parallelism:4}")
    private int maxParallelism;

    @Value("${rag.content.min-length:0}") // 修改默认值为0，不过滤短文档
    private int minContentLength;

    @Value("${rag.content.max-noise-ratio:1.0}") // 修改默认值为1.0，不过滤高噪音文档
    private double maxNoiseRatio;

    @Value("${rag.processing.track-state:false}") // 默认不跟踪状态
    private boolean trackState;

    @Value("${rag.processing.state-file-name:.rag-processing-state.json}")
    private String stateFileName;

    /**
     * 全量/增量加载文档.
     *
     * @param ingestor    ingestor
     * @param forceReload 是否强制重建
     */
    public void ingestAllDocuments(EmbeddingStoreIngestor ingestor, boolean forceReload) {
        Path docsPath = resolveDocumentsPath();
        if (docsPath == null) {
            return;
        }
        try (Stream<Path> pathStream = Files.walk(docsPath)) {
            List<Path> candidates = pathStream
                    .filter(Files::isRegularFile)
                    .filter(this::filterHiddenFile)
                    .filter(this::filterStateFile)
                    .filter(this::filterNonDocumentFiles) // 添加非文档文件过滤
                    .collect(Collectors.toList());

            if (candidates.isEmpty()) {
                log.info("未在路径 {} 中发现可处理的文档", docsPath);
                return;
            }

            // 当禁用状态跟踪或强制重载时，处理所有文件
            List<Path> toProcess = (forceReload || !trackState) ? candidates : filterChangedFiles(candidates);
            if (toProcess.isEmpty()) {
                log.info("没有需要更新的文档,跳过摄取");
                return;
            }

            log.info("开始处理 {} 个文档 (forceReload={})", toProcess.size(), forceReload);
            processInParallel(toProcess, ingestor);
        } catch (IOException e) {
            log.error("遍历文档目录失败", e);
        }
    }

    /**
     * 单文件摄取,用于新增文档.
     */
    public void ingestSingleDocument(String filePath,
                                     EmbeddingStoreIngestor ingestor,
                                     boolean forceReload) {
        if (filePath == null || filePath.isBlank()) {
            log.warn("文件路径为空,忽略添加");
            return;
        }
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            log.warn("文件不存在: {}", filePath);
            return;
        }
        processDocument(path, ingestor, stateTracker, forceReload);
    }

    /**
     * 并行处理文档,并行处理多个文件
     * @param paths 文件路径列表
     * @param ingestor 向量存储对象
     */
    private void processInParallel(List<Path> paths, EmbeddingStoreIngestor ingestor) {
        // 使用自定义线程池配置提升大量文件处理性能
        int threads = Math.min(Math.max(1, maxParallelism), paths.size());
        // 创建具有合适配置的线程池
        ExecutorService executor = Executors.newFixedThreadPool(threads, r -> {
            Thread t = new Thread(r);
            t.setDaemon(false);  // 非守护线程
            t.setPriority(Thread.NORM_PRIORITY);  // 标准优先级
            return t;
        });
        try {
            List<CompletableFuture<Void>> futures = paths.stream()
                    .map(path -> CompletableFuture.runAsync(
                            () -> processDocument(path, ingestor, stateTracker, false), executor))
                    .toList();
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
            try {
                // 增加等待时间以确保所有任务完成
                if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                    log.warn("文档处理线程池关闭超时，强制关闭");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.warn("文档处理线程池关闭被中断");
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processDocument(Path path, EmbeddingStoreIngestor ingestor, DocumentProcessingStateTracker tracker, boolean force) {
        // 当禁用状态跟踪时，总是处理文档
        if (!force && trackState && tracker != null && !tracker.shouldProcess(createSnapshot(path))) {
            tracker.markSkipped(createSnapshot(path));
            return;
        }

        if (trackState && tracker != null) {
            tracker.markProcessing(createSnapshot(path));
        }

        // 检查文件是否为空，如果为空则跳过
        try {
            if (Files.size(path) == 0) {
                log.debug("文件为空,跳过处理: {}", path);
                if (trackState && tracker != null) {
                    tracker.markSkipped(createSnapshot(path));
                }
                return;
            }
        } catch (IOException e) {
            log.warn("无法获取文件大小,跳过处理: {}", path, e);
            if (trackState && tracker != null) {
                tracker.markFailure(createSnapshot(path), "无法获取文件大小: " + e.getMessage());
            }
            return;
        }

        AtomicLong ingestedSegments = new AtomicLong();
        try {
            // 检查文件是否受密码保护
            if (isPasswordProtected(path)) {
                log.debug("文件受密码保护,跳过处理: {}", path);
                if (trackState && tracker != null) {
                    tracker.markSkipped(createSnapshot(path));
                }
                return;
            }

            // 对于PDF等二进制文件，不应使用大文件流式处理逻辑
            String fileName = path.getFileName().toString();
            String extension = getFileExtension(fileName).toLowerCase();
            boolean isBinaryFile = "pdf".equals(extension) || 
                                  "doc".equals(extension) || 
                                  "docx".equals(extension) || 
                                  "xls".equals(extension) || 
                                  "xlsx".equals(extension);

            // 只对文本文件使用大文件流式处理
            if (!isBinaryFile && Files.size(path) >= largeFileThresholdBytes) {
                processLargeFile(path, ingestor, createSnapshot(path), ingestedSegments);
            } else {
                processRegularFile(path, ingestor, ingestedSegments);
            }
            if (trackState && tracker != null) {
                tracker.markSuccess(createSnapshot(path), ingestedSegments.get());
            }
        } catch (Exception e) {
            log.error("处理文档失败: {}", path, e);
            if (trackState && tracker != null) {
                tracker.markFailure(createSnapshot(path), e.getMessage());
            }
        }
    }

    private void processRegularFile(Path path,
                                   EmbeddingStoreIngestor ingestor,
                                   AtomicLong ingestedSegments) throws IOException {
        Document document = FileSystemDocumentLoader.loadDocument(path, getDocumentParserForFile(path));
        if (isHighQuality(document.text())) {
            ingestor.ingest(document);
            ingestedSegments.incrementAndGet();
        } else {
            log.debug("文档质量较低,跳过: {}", path);
        }
    }

    private void processLargeFile(Path path,
                                  EmbeddingStoreIngestor ingestor,
                                  DocumentProcessingSnapshot snapshot,
                                  AtomicLong ingestedSegments) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            char[] buffer = new char[streamChunkSize];
            StringBuilder chunkBuilder = new StringBuilder(streamChunkSize);
            int read;
            int chunkIndex = 0;
            while ((read = reader.read(buffer)) != -1) {
                chunkBuilder.append(buffer, 0, read);
                if (chunkBuilder.length() >= streamChunkSize) {
                    chunkIndex = ingestChunk(path, ingestor, chunkBuilder, chunkIndex, ingestedSegments);
                }
            }
            if (chunkBuilder.length() > 0) {
                ingestChunk(path, ingestor, chunkBuilder, chunkIndex, ingestedSegments);
            }
            log.debug("大文件采用流式处理完成: {} (size={} bytes)", path, snapshot.fileSize());
        }
    }

    private int ingestChunk(Path path,
                            EmbeddingStoreIngestor ingestor,
                            StringBuilder chunkBuilder,
                            int chunkIndex,
                            AtomicLong ingestedSegments) {
        String chunkText = chunkBuilder.toString();
        chunkBuilder.setLength(0);
        if (!isHighQuality(chunkText)) {
            return chunkIndex + 1;
        }

        Metadata metadata = new Metadata();
        metadata.put(Document.FILE_NAME, path.getFileName().toString());
        metadata.put("chunk_index", chunkIndex);
        metadata.put("chunk_length", chunkText.length());

        Document chunkDocument = Document.from(chunkText, metadata);
        ingestor.ingest(chunkDocument);
        ingestedSegments.incrementAndGet();
        return chunkIndex + 1;
    }

    private boolean isHighQuality(String text) {
        if (text == null) {
            return false;
        }
        String trimmed = text.strip();
        if (trimmed.length() < minContentLength) {
            return false;
        }
        long noise = trimmed.chars()
                .filter(ch -> !Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch) && ch < 128)
                .count();
        double noiseRatio = (double) noise / trimmed.length();
        return noiseRatio <= maxNoiseRatio;
    }

    private DocumentProcessingSnapshot createSnapshot(Path path) {
        try {
            return DocumentProcessingSnapshot.from(path);
        } catch (IOException e) {
            log.error("获取文件元信息失败: {}", path, e);
            return null;
        }
    }

    private List<Path> filterChangedFiles(List<Path> candidates) {
        return candidates.stream()
                .map(this::createSnapshot)
                .filter(Objects::nonNull)
                .filter(stateTracker::shouldProcess)
                .map(snapshot -> Paths.get(snapshot.absolutePath()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean filterHiddenFile(Path path) {
        if (includeHiddenFiles) {
            return true;
        }
        try {
            return !Files.isHidden(path);
        } catch (IOException e) {
            return true;
        }
    }

    private boolean filterStateFile(Path path) {
        return !path.getFileName().toString().equals(stateFileName);
    }

    /**
     * 过滤掉非文档文件（视频、图片、压缩包等）
     */
    private boolean filterNonDocumentFiles(Path path) {
        String fileName = path.getFileName().toString();
        String extension = getFileExtension(fileName).toLowerCase();
        
        // 定义非文档文件扩展名集合
        Set<String> nonDocumentExtensions = Set.of(
            // 视频格式
            "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v", "3gp", "3g2", "mpg", "mpeg", "m2v", "svi", "vob", "rm", "rmvb",
            // 图片格式
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "tiff", "tif", "svg", "ico", "raw", "arw", "cr2", "nrw", "k25", "dib", "heif", "heic", "ind", "indd", "indt", "jp2", "j2k", "jpf", "jpx", "jpm", "mj2", "svgz", "ai", "eps",
            // 压缩包格式
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "tgz", "tbz2", "txz", "iso", "dmg", "jar", "war", "ear",
            // 可执行文件
            "exe", "msi", "bat", "cmd", "sh", "bin", "app", "deb", "rpm",
            // 数据库文件
            "db", "sqlite", "mdb", "accdb", "dbf",
            // 日志文件
            "log"
        );
        
        return !nonDocumentExtensions.contains(extension);
    }

    private Path resolveDocumentsPath() {
        String actualDocumentsPath = ragUtils.getActualDocumentsPath();
        if (actualDocumentsPath == null || actualDocumentsPath.isBlank()) {
            log.warn("未配置RAG文档路径");
            return null;
        }
        Path docsPath = Paths.get(actualDocumentsPath);
        if (!Files.exists(docsPath)) {
            log.warn("文档路径不存在: {}", docsPath);
            return null;
        }
        return docsPath;
    }

    /**
     * 根据文件扩展名获取相应的文档解析器
     */
    private DocumentParser getDocumentParserForFile(Path path) {
        String fileName = path.getFileName().toString();
        String extension = getFileExtension(fileName).toLowerCase();
        
        switch (extension) {
            case "pdf":
                return new ApachePdfBoxDocumentParser();
            case "doc":
            case "docx":
            case "xls":
            case "xlsx":
                return new ApachePoiDocumentParser();
            case "txt":
            case "md":
            default:
                return new TextDocumentParser();
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    /**
     * 检查文件是否受密码保护
     * @param path 文件路径
     * @return 如果文件受密码保护返回true，否则返回false
     */
    private boolean isPasswordProtected(Path path) {
        String fileName = path.getFileName().toString();
        String extension = getFileExtension(fileName).toLowerCase();
        
        try {
            switch (extension) {
                case "pdf":
                    // 检查PDF文件是否加密
                    try {
                        PDDocument document = PDDocument.load(path.toFile());
                        boolean encrypted = document.isEncrypted();
                        document.close();
                        return encrypted;
                    } catch (IOException e) {
                        // 如果加载失败，可能是因为密码保护
                        log.debug("PDF文件可能受密码保护: {}", path, e);
                        return true;
                    }
                    
                case "doc":
                case "docx":
                case "xls":
                case "xlsx":
                    // 检查Office文档是否加密
                    try (FileInputStream fis = new FileInputStream(path.toFile())) {
                        POIFSFileSystem poifs = new POIFSFileSystem(fis);
                        // 尝试获取加密信息
                        boolean encrypted = poifs.getRoot().hasEntry(EncryptionInfo.ENCRYPTION_INFO_ENTRY);
                        return encrypted;
                    } catch (Exception e) {
                        // 如果读取失败，可能是因为密码保护
                        log.debug("Office文档可能受密码保护: {}", path, e);
                        return true;
                    }
                    
                case "zip":
                case "rar":
                case "7z":
                    // 对于压缩文件，简单检查是否能列出条目
                    try {
                        switch (extension) {
                            case "zip":
                                try (ZipFile zipFile = new ZipFile(path.toFile())) {
                                    // 尝试访问条目但不读取内容
                                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                                    if (entries.hasMoreElements()) {
                                        ZipEntry entry = entries.nextElement();
                                        // 尝试获取输入流来检查是否受密码保护
                                        try (InputStream is = zipFile.getInputStream(entry)) {
                                            // 如果能获取到流，则不是密码保护的（至少这个条目不是）
                                            return false;
                                        } catch (IOException e) {
                                            // 如果获取流失败，可能是密码保护
                                            log.debug("ZIP条目可能受密码保护: {}", entry.getName());
                                            return true;
                                        }
                                    }
                                    return false;
                                }
                                
                            // 注意：RAR和7Z需要额外的库支持，这里仅作占位符
                            case "rar":
                            case "7z":
                                // 对于RAR和7Z文件，暂时无法简单检查密码保护
                                // 可以考虑使用第三方库如junrar或sevenzipjbinding
                                log.debug("无法检查{}文件的密码保护状态", extension);
                                return false;
                                
                            default:
                                return false;
                        }
                    } catch (IOException e) {
                        // 如果无法打开压缩文件，可能是因为密码保护
                        log.debug("压缩文件可能受密码保护: {}", path, e);
                        return true;
                    }
                    
                default:
                    // 对于其他类型的文件，默认不检查密码保护
                    return false;
            }
        } catch (Exception e) {
            log.warn("检查文件密码保护时发生错误: {}", path, e);
            // 发生异常时，保守地认为文件可能受密码保护
            return true;
        }
    }

}

