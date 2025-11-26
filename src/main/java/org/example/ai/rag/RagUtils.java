package org.example.ai.rag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RAG工具类，提供公共的RAG相关工具方法
 */
@Component
public class RagUtils {
    
    @Value("${rag.os.windows.documents.path:}")
    private String windowsDocumentsPath;
    
    @Value("${rag.os.linux.documents.path:}")
    private String linuxDocumentsPath;
    
    /**
     * 根据操作系统获取实际的文档路径
     * Windows系统使用 rag.os.windows.documents.path 配置的路径
     * Linux系统使用 rag.os.linux.documents.path 配置的路径
     * @return 文档路径
     */
    public String getActualDocumentsPath() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            return windowsDocumentsPath;
        } else {
            return linuxDocumentsPath;
        }
    }
}