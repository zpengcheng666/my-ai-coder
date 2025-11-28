package org.example.ai.rag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 快照信息,用于对比文件是否发生变化.
 */
public record DocumentProcessingSnapshot(String absolutePath, long fileSize, long lastModifiedTime) {

    public static DocumentProcessingSnapshot from(Path path) throws IOException {
        return new DocumentProcessingSnapshot(
                path.toAbsolutePath().toString(),
                Files.size(path),
                Files.getLastModifiedTime(path).toMillis()
        );
    }
}

