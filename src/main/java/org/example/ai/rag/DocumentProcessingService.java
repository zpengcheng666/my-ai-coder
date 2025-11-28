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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private void processInParallel(List<Path> paths, EmbeddingStoreIngestor ingestor) {
        int threads = Math.min(Math.max(1, maxParallelism), paths.size());
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<CompletableFuture<Void>> futures = paths.stream()
                    .map(path -> CompletableFuture.runAsync(
                            () -> processDocument(path, ingestor, stateTracker, false), executor))
                    .toList();
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processDocument(Path path,
                                 EmbeddingStoreIngestor ingestor,
                                 DocumentProcessingStateTracker tracker,
                                 boolean force) {
        // 当禁用状态跟踪时，总是处理文档
        if (!force && trackState && tracker != null && !tracker.shouldProcess(createSnapshot(path))) {
            tracker.markSkipped(createSnapshot(path));
            return;
        }

        if (trackState && tracker != null) {
            tracker.markProcessing(createSnapshot(path));
        }

        AtomicLong ingestedSegments = new AtomicLong();
        try {
            if (Files.size(path) >= largeFileThresholdBytes) {
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

}

