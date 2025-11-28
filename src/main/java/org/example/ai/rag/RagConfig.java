package org.example.ai.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Rag 配置
 */
@Configuration
public class RagConfig {

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    @Resource
    private EmbeddingStoreIngestor embeddingStoreIngestor;

    @Resource
    private DocumentProcessingService documentProcessingService;

    @Bean
    public ContentRetriever contentRetriever() {
        documentProcessingService.ingestAllDocuments(embeddingStoreIngestor, false);

        //4.自定义内容加载器
        //最大返回10条
        //最小相似度0.7

        return EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(10)
                .minScore(0.7)
                .build();
    }
}