package org.example.ai.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 文档处理状态跟踪器,用于持久化每个文件的处理情况,支持增量式加载.
 */
@Component
@Slf4j
public class DocumentProcessingStateTracker {

    private final RagUtils ragUtils;
    private final ObjectMapper objectMapper;
    private final String stateFileName;
    private final Map<String, DocumentProcessingState> states = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 依赖注入：通过@Lazy注解延迟加载RagUtils工具类
     * 配置读取：从配置文件读取状态文件名，默认为".rag-processing-state.json"
     * 初始化组件：创建JSON对象映射器并注册所有模块
     * 加载状态：调用loadFromDisk()方法从磁盘加载处理状态
     * 主要用于文档处理状态的持久化管理。
     *
     * @param ragUtils RagUtils工具类
     * @param stateFileName 状态文件名
     */
    public DocumentProcessingStateTracker(@Lazy RagUtils ragUtils, @Value("${rag.processing.state-file-name:.rag-processing-state.json}") String stateFileName) {
        this.ragUtils = ragUtils;
        this.stateFileName = stateFileName;
        this.objectMapper = JsonMapper.builder().findAndAddModules().build();
        loadFromDisk();
    }

    /**
     * 判断文件是否需要重新处理。首先根据文件路径获取其处理状态，若无记录则返回 true 表示需处理；
     * 若有记录，则比较文件的最后修改时间和大小，任一不同则返回 true，表示文件已变更需重新处理。
     * @param snapshot 文件快照信息
     * @return 是否需要处理
     */
    public boolean shouldProcess(DocumentProcessingSnapshot snapshot) {
        DocumentProcessingState state = states.get(snapshot.absolutePath());
        if (state == null) {
            return true;
        }
        return state.getLastModified() != snapshot.lastModifiedTime() || state.getFileSize() != snapshot.fileSize();
    }

    /**
     * 标记指定文件的处理状态为“正在处理”。
     * 它调用 updateState 方法，传入文件快照、状态 PROCESSING、无错误信息和已处理分片数为0，更新并持久化该文件的处理状态
     * @param snapshot 文件快照信息
     */
    public void markProcessing(DocumentProcessingSnapshot snapshot) {
        updateState(snapshot, DocumentProcessingState.ProcessingStatus.PROCESSING, null, 0);
    }

    /**
     * 标记成功/**
     * 标记处理成功
     * @param snapshot 文件信息
     * @param processedSegments 处理的分片数
     */
    public void markSuccess(DocumentProcessingSnapshot snapshot, long processedSegments) {
        updateState(snapshot, DocumentProcessingState.ProcessingStatus.SUCCESS, null, processedSegments);
    }

    /**
     * 跳过处理
     * @param snapshot 文件信息
     */
    public void markSkipped(DocumentProcessingSnapshot snapshot) {
        updateState(snapshot, DocumentProcessingState.ProcessingStatus.SKIPPED, null, 0);
    }

    /**
     * 处理失败
     * @param snapshot 文件信息
     * @param errorMessage 错误信息
     */
    public void markFailure(DocumentProcessingSnapshot snapshot, String errorMessage) {
        updateState(snapshot, DocumentProcessingState.ProcessingStatus.FAILED, errorMessage, 0);
    }

    public Map<String, DocumentProcessingState> snapshotStates() {
        return Collections.unmodifiableMap(states);
    }

    /**
     * 更新文档处理状态并持久化。通过DocumentProcessingSnapshot获取文件信息，
     * 结合传入的处理状态、错误信息和已处理分片数，构建一个新的DocumentProcessingState对象，
     * 并将其存入内存映射中，最后调用persist()方法将状态写入磁盘。
     *
     * @param snapshot 文件信息
     * @param status 状态
     * @param errorMessage 错误信息
     * @param processedSegments 处理的分片数
     */
    private void updateState(DocumentProcessingSnapshot snapshot, DocumentProcessingState.ProcessingStatus status,
                             String errorMessage, long processedSegments) {
        DocumentProcessingState state = DocumentProcessingState.builder()
                .filePath(snapshot.absolutePath())
                .fileSize(snapshot.fileSize())
                .lastModified(snapshot.lastModifiedTime())
                .status(status)
                .lastError(errorMessage)
                .processedSegments(processedSegments)
                .updatedAt(System.currentTimeMillis())
                .build();
        states.put(snapshot.absolutePath(), state);
        persist();
    }

    /**
     * 从磁盘加载文档处理状态文件。
     * 首先通过 resolveStateFile() 获取状态文件路径，若文件不存在则直接返回。
     * 若存在，则获取写锁后读取文件内容并反序列化为 DocumentProcessingState 列表，
     * 将每个状态存入内存映射中，最后记录加载条数或异常信息。
     */
    private void loadFromDisk() {
        Path stateFile = resolveStateFile();
        if (stateFile == null || !Files.exists(stateFile)) {
            log.info("未找到RAG状态文件,将从空状态开始加载文档: {}", stateFile);
            return;
        }
        // 获取写锁, 防止其他线程写入
        lock.writeLock().lock();
        try {
            byte[] data = Files.readAllBytes(stateFile);
            List<DocumentProcessingState> list = objectMapper.readValue(data, new TypeReference<>() {});
            list.forEach(state -> states.put(state.getFilePath(), state));
            log.info("成功加载 {} 条文档处理状态", list.size());
        } catch (IOException e) {
            log.warn("读取RAG状态文件失败,忽略并重新开始: {}", e.getMessage());
        } finally {
            lock.writeLock().unlock();
            log.info("已释放RAG状态文件锁");
        }
    }

    /**
     * 将内存中的文档处理状态写入磁盘。
     * 首先通过 resolveStateFile() 获取状态文件路径，若为空则返回。
     * 获取写锁，并创建父目录，最后将状态列表写入文件。
     */
    private void persist() {
        Path stateFile = resolveStateFile();
        if (stateFile == null) {
            return;
        }
        // 获取写锁, 防止其他线程写入
        lock.writeLock().lock();
        try {
            Files.createDirectories(stateFile.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(stateFile.toFile(), states.values());
        } catch (IOException e) {
            log.warn("写入RAG状态文件失败: {}", e.getMessage());
        } finally {
            lock.writeLock().unlock();
            log.info("已释放RAG状态文件锁");
        }
    }

    /**
     * 获取状态文件路径。
     * 首先获取文档路径，若为空则返回 null。
     * 创建状态文件路径，并返回。
     * @return 状态文件路径
     */
    private Path resolveStateFile() {
        String documentsPath = ragUtils.getActualDocumentsPath();
        if (documentsPath == null || documentsPath.isBlank()) {
            log.warn("文档路径未配置,无法记录状态");
            return null;
        }
        return Paths.get(documentsPath, stateFileName);
    }
}

