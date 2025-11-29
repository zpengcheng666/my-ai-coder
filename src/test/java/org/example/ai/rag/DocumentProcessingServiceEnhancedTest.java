package org.example.ai.rag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentProcessingServiceEnhancedTest {

    @TempDir
    Path tempDir;

    @Test
    void testFilterNonDocumentFiles() throws IOException {
        // 创建DocumentProcessingService实例
        DocumentProcessingStateTracker mockTracker = mock(DocumentProcessingStateTracker.class);
        RagUtils mockRagUtils = mock(RagUtils.class);
        DocumentProcessingService service = new DocumentProcessingService(mockTracker, mockRagUtils);

        // 测试文档文件应该通过过滤器
        Path txtFile = tempDir.resolve("document.txt");
        Files.createFile(txtFile);
        
        Path mdFile = tempDir.resolve("readme.md");
        Files.createFile(mdFile);
        
        Path pdfFile = tempDir.resolve("paper.pdf");
        Files.createFile(pdfFile);

        // 使用反射调用filterNonDocumentFiles方法
        try {
            java.lang.reflect.Method filterMethod = DocumentProcessingService.class.getDeclaredMethod("filterNonDocumentFiles", Path.class);
            filterMethod.setAccessible(true);
            
            assertTrue((Boolean) filterMethod.invoke(service, txtFile), "TXT file should pass the filter");
            assertTrue((Boolean) filterMethod.invoke(service, mdFile), "MD file should pass the filter");
            assertTrue((Boolean) filterMethod.invoke(service, pdfFile), "PDF file should pass the filter");
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                fail("Failed to invoke filterNonDocumentFiles method: " + ((InvocationTargetException) e).getTargetException().getMessage());
            } else {
                fail("Failed to invoke filterNonDocumentFiles method: " + e.getMessage());
            }
        }
    }

    @Test
    void testFilterOutNonDocumentFiles() throws IOException {
        // 创建DocumentProcessingService实例
        DocumentProcessingStateTracker mockTracker = mock(DocumentProcessingStateTracker.class);
        RagUtils mockRagUtils = mock(RagUtils.class);
        DocumentProcessingService service = new DocumentProcessingService(mockTracker, mockRagUtils);

        // 测试非文档文件应该被过滤掉
        Path videoFile = tempDir.resolve("movie.mp4");
        Files.createFile(videoFile);
        
        Path imageFile = tempDir.resolve("photo.jpg");
        Files.createFile(imageFile);
        
        Path archiveFile = tempDir.resolve("archive.zip");
        Files.createFile(archiveFile);
        
        Path exeFile = tempDir.resolve("program.exe");
        Files.createFile(exeFile);

        // 使用反射调用filterNonDocumentFiles方法
        try {
            java.lang.reflect.Method filterMethod = DocumentProcessingService.class.getDeclaredMethod("filterNonDocumentFiles", Path.class);
            filterMethod.setAccessible(true);
            
            assertFalse((Boolean) filterMethod.invoke(service, videoFile), "MP4 file should be filtered out");
            assertFalse((Boolean) filterMethod.invoke(service, imageFile), "JPG file should be filtered out");
            assertFalse((Boolean) filterMethod.invoke(service, archiveFile), "ZIP file should be filtered out");
            assertFalse((Boolean) filterMethod.invoke(service, exeFile), "EXE file should be filtered out");
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                fail("Failed to invoke filterNonDocumentFiles method: " + ((InvocationTargetException) e).getTargetException().getMessage());
            } else {
                fail("Failed to invoke filterNonDocumentFiles method: " + e.getMessage());
            }
        }
    }
}