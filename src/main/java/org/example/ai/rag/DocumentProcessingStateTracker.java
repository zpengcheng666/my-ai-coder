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

    public DocumentProcessingStateTracker(@Lazy RagUtils ragUtils,
                                          @Value("${rag.processing.state-file-name:.rag-processing-state.json}") String stateFileName) {
        this.ragUtils = ragUtils;
        this.stateFileName = stateFileName;
        this.objectMapper = JsonMapper.builder().findAndAddModules().build();
        loadFromDisk();
    }

    public boolean shouldProcess(DocumentProcessingSnapshot snapshot) {
        DocumentProcessingState state = states.get(snapshot.absolutePath());
        if (state == null) {
            return true;
        }
        return state.getLastModified() != snapshot.lastModifiedTime()
                || state.getFileSize() != snapshot.fileSize();
    }

    public void markProcessing(DocumentProcessingSnapshot snapshot) {
        updateState(snapshot, DocumentProcessingState.ProcessingStatus.PROCESSING, null, 0);
    }

    public void markSuccess(DocumentProcessingSnapshot snapshot, long processedSegments) {
        updateState(snapshot, DocumentProcessingState.ProcessingStatus.SUCCESS, null, processedSegments);
    }

    public void markSkipped(DocumentProcessingSnapshot snapshot) {
        updateState(snapshot, DocumentProcessingState.ProcessingStatus.SKIPPED, null, 0);
    }

    public void markFailure(DocumentProcessingSnapshot snapshot, String errorMessage) {
        updateState(snapshot, DocumentProcessingState.ProcessingStatus.FAILED, errorMessage, 0);
    }

    public Map<String, DocumentProcessingState> snapshotStates() {
        return Collections.unmodifiableMap(states);
    }

    private void updateState(DocumentProcessingSnapshot snapshot,
                             DocumentProcessingState.ProcessingStatus status,
                             String errorMessage,
                             long processedSegments) {
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

    private void loadFromDisk() {
        Path stateFile = resolveStateFile();
        if (stateFile == null || !Files.exists(stateFile)) {
            log.info("未找到RAG状态文件,将从空状态开始加载文档: {}", stateFile);
            return;
        }
        lock.writeLock().lock();
        try {
            byte[] data = Files.readAllBytes(stateFile);
            List<DocumentProcessingState> list = objectMapper.readValue(
                    data,
                    new TypeReference<List<DocumentProcessingState>>() {
                    });
            list.forEach(state -> states.put(state.getFilePath(), state));
            log.info("成功加载 {} 条文档处理状态", list.size());
        } catch (IOException e) {
            log.warn("读取RAG状态文件失败,忽略并重新开始: {}", e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void persist() {
        Path stateFile = resolveStateFile();
        if (stateFile == null) {
            return;
        }
        lock.writeLock().lock();
        try {
            Files.createDirectories(stateFile.getParent());
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(stateFile.toFile(), states.values());
        } catch (IOException e) {
            log.warn("写入RAG状态文件失败: {}", e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    private Path resolveStateFile() {
        String documentsPath = ragUtils.getActualDocumentsPath();
        if (documentsPath == null || documentsPath.isBlank()) {
            log.warn("文档路径未配置,无法记录状态");
            return null;
        }
        return Paths.get(documentsPath, stateFileName);
    }
}

