package org.example.ai.rag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档处理状态,用于持久化处理结果,支持增量更新.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentProcessingState {

    private String filePath;

    private long lastModified;

    private long fileSize;

    private ProcessingStatus status;

    private String lastError;

    private long updatedAt;

    private long processedSegments;

    public enum ProcessingStatus {
        PENDING,
        PROCESSING,
        SUCCESS,
        FAILED,
        SKIPPED
    }
}

