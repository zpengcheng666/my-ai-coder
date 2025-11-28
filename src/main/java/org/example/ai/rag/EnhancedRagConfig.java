package org.example.ai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.segment.TextSegmentTransformer;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;

import java.io.InputStream;

/**
 * 增强的RAG配置
 * 支持Redis持久化向量存储和动态文档加载
 */
@Configuration
@Slf4j
public class EnhancedRagConfig {

    @Resource
    private EmbeddingModel embeddingModel;

    @Lazy
    @Resource
    private DocumentProcessingService documentProcessingService;

    @Value("${rag.vector.store.type:redis}")
    private String vectorStoreType;

    @Value("${rag.segment.max-size:800}")
    private int maxSegmentSize;

    @Value("${rag.segment.max-overlap:200}")
    private int maxOverlap;

    @Value("${rag.segment.smart.small-threshold:1500}")
    private int smallDocumentThreshold;

    @Value("${rag.segment.smart.large-threshold:8000}")
    private int largeDocumentThreshold;

    @Value("${rag.segment.smart.small-size:600}")
    private int smallDocumentSegmentSize;

    @Value("${rag.segment.smart.large-size:400}")
    private int largeDocumentSegmentSize;

    @Value("${rag.retrieval.max-results:10}")
    private int maxResults;

    @Value("${rag.retrieval.min-score:0.7}")
    private double minScore;

    /**
     * 向量存储配置,默认使用内存向量存储,可以替换为Redis
     * 如果使用Redis向量存储实现，需要Redis Stack或RediSearch模块，需要用docker来启动RediSearch模块
     */
    @Bean
    @Primary
    public EmbeddingStore<TextSegment> enhancedEmbeddingStore() {
        if ("redis".equalsIgnoreCase(vectorStoreType)) {
            try {
                log.info("尝试使用Redis向量存储");
                RedisEmbeddingStore redisEmbeddingStore = RedisEmbeddingStore.builder()
                        .host("localhost")
                        .port(6379)
                        .dimension(1536)
                        .build();
                return redisEmbeddingStore;
            } catch (Exception e) {
                log.error("连接Redis向量存储失败: {}", e.getMessage());
                log.info("回退到内存向量存储");
            }
        }
        
        // 默认使用内存向量存储
        log.info("使用内存向量存储");
        return new InMemoryEmbeddingStore<>();
    }

    /**
     * 文档解析器 - 使用默认的文本解析器
     */
    @Bean
    public DocumentParser documentParser() {
        return new TextDocumentParser();
    }

    /**
     * 文档分割器
     */
    @Bean
    public DocumentSplitter documentSplitter() {
        return new SmartDocumentSplitter(
                maxSegmentSize,
                smallDocumentSegmentSize,
                largeDocumentSegmentSize,
                smallDocumentThreshold,
                largeDocumentThreshold,
                maxOverlap
        );
    }

    /**
     * 文档转换器 - 增加文档元信息
     */
    @Bean
    public TextSegmentTransformer textSegmentTransformer() {
        return textSegment -> {
            String fileName = textSegment.metadata().getString("file_name");
            String fileType = getFileExtension(fileName);
            String enhancedContent = String.format("文档：%s\n类型：%s\n内容：%s", 
                    fileName, fileType, textSegment.text());
            return TextSegment.from(enhancedContent, textSegment.metadata());
        };
    }

    /**
     * 文档摄取器
     */
    @Bean
    public EmbeddingStoreIngestor embeddingStoreIngestor(
            EmbeddingStore<TextSegment> embeddingStore,
            DocumentSplitter documentSplitter,
            TextSegmentTransformer textSegmentTransformer) {
        
        return EmbeddingStoreIngestor.builder()
                .documentSplitter(documentSplitter)
                .textSegmentTransformer(textSegmentTransformer)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    /**
     * 内容检索器
     */
    @Bean
    @Lazy
    public ContentRetriever enhancedContentRetriever(EmbeddingStore<TextSegment> embeddingStore) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
    }

    /**
     * 初始化时加载文档
     * 移除@PostConstruct，改为手动初始化以避免循环依赖
     */
    public void initializeDocuments(EmbeddingStoreIngestor embeddingStoreIngestor) {
        loadDocumentsAsync(embeddingStoreIngestor);
    }

    /**
     * 异步加载文档到向量存储
     */
    @Async
    public void loadDocumentsAsync(EmbeddingStoreIngestor embeddingStoreIngestor) {
        documentProcessingService.ingestAllDocuments(embeddingStoreIngestor, false);
    }

    /**
     * 动态添加文档到RAG知识库
     */
    public void addDocument(String filePath, EmbeddingStoreIngestor embeddingStoreIngestor) {
        documentProcessingService.ingestSingleDocument(filePath, embeddingStoreIngestor, true);
    }

    /**
     * 重新加载所有文档
     */
    public void reloadAllDocuments(EmbeddingStoreIngestor embeddingStoreIngestor) {
        log.info("重新加载所有RAG文档");
        documentProcessingService.ingestAllDocuments(embeddingStoreIngestor, true);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "unknown";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}