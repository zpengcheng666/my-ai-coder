package org.example.ai.rag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文档处理快照
 * @param absolutePath
 * @param fileSize
 * @param lastModifiedTime
 */
public record DocumentProcessingSnapshot(String absolutePath, long fileSize, long lastModifiedTime) {

    /**
     * 从给定的文件路径创建一个DocumentProcessingSnapshot对象
     * 通过Files.size()获取文件大小，通过Files.getLastModifiedTime()获取文件最后修改时间
     * 将路径、文件大小和修改时间作为参数构造新的快照对象
     * 主要用于创建文档处理的快照信息
     *
     * @param path 文件路径
     * @return DocumentProcessingSnapshot
     */
    public static DocumentProcessingSnapshot from(Path path) throws IOException {
        return new DocumentProcessingSnapshot(
                path.toAbsolutePath().toString(),
                Files.size(path),
                Files.getLastModifiedTime(path).toMillis()
        );
    }
}

