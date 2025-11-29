package org.example.ai.rag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentProcessingServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void testFilterStateFile() throws IOException {
        // 创建测试文件
        Path regularFile = tempDir.resolve("test.txt");
        Files.createFile(regularFile);
        
        Path stateFile = tempDir.resolve(".rag-processing-state.json");
        Files.createFile(stateFile);
        
        // 创建DocumentProcessingService实例（使用部分mock）
        DocumentProcessingStateTracker mockTracker = mock(DocumentProcessingStateTracker.class);
        RagUtils mockRagUtils = mock(RagUtils.class);
        DocumentProcessingService service = new DocumentProcessingService(mockTracker, mockRagUtils);
        
        // 使用反射设置stateFileName字段
        try {
            java.lang.reflect.Field stateFileNameField = DocumentProcessingService.class.getDeclaredField("stateFileName");
            stateFileNameField.setAccessible(true);
            stateFileNameField.set(service, ".rag-processing-state.json");
        } catch (Exception e) {
            fail("Failed to set stateFileName field: " + e.getMessage());
        }
        
        // 测试filterStateFile方法
        try {
            java.lang.reflect.Method filterStateFileMethod = DocumentProcessingService.class.getDeclaredMethod("filterStateFile", Path.class);
            filterStateFileMethod.setAccessible(true);
            
            boolean regularFileResult = (boolean) filterStateFileMethod.invoke(service, regularFile);
            boolean stateFileResult = (boolean) filterStateFileMethod.invoke(service, stateFile);
            
            assertTrue(regularFileResult, "Regular file should pass the filter");
            assertFalse(stateFileResult, "State file should be filtered out");
        } catch (Exception e) {
            fail("Failed to invoke filterStateFile method: " + e.getMessage());
        }
    }
}