package org.example.ai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;

import java.util.List;

/**
 * 智能分割策略:
 * 1. 对超大文档减小chunk,保证召回准确性;
 * 2. 对短文档扩大chunk,减少碎片噪声.
 */
public class SmartDocumentSplitter implements DocumentSplitter {

    private final int defaultSegmentSize;
    private final int smallDocumentSegmentSize;
    private final int largeDocumentSegmentSize;
    private final int smallDocumentThreshold;
    private final int largeDocumentThreshold;
    private final int overlap;

    public SmartDocumentSplitter(int defaultSegmentSize,
                                 int smallDocumentSegmentSize,
                                 int largeDocumentSegmentSize,
                                 int smallDocumentThreshold,
                                 int largeDocumentThreshold,
                                 int overlap) {
        this.defaultSegmentSize = defaultSegmentSize;
        this.smallDocumentSegmentSize = smallDocumentSegmentSize;
        this.largeDocumentSegmentSize = largeDocumentSegmentSize;
        this.smallDocumentThreshold = smallDocumentThreshold;
        this.largeDocumentThreshold = largeDocumentThreshold;
        this.overlap = overlap;
    }

    /**
     * 根据文档长度智能分割文档
     * 该方法根据文档的实际长度动态调整分段大小，使用DocumentByParagraphSplitter进行实际的分割操作。
     * 对于超大文档使用较小的分段以保证召回准确性，对于短文档使用较大的分段以减少碎片噪声。
     * 
     * @param document 待分割的文档对象
     * @return 分割后的文本段列表
     */
    @Override
    public List<TextSegment> split(Document document) {
        int length = document.text() == null ? 0 : document.text().length();
        int segmentSize = determineSegmentSize(length);
        DocumentByParagraphSplitter delegate = new DocumentByParagraphSplitter(segmentSize, overlap);
        return delegate.split(document);
    }

    /**
     * 根据文档长度确定分段大小
     *
     * @param documentLength 文档长度
     * @return 分段大小
     */
    private int determineSegmentSize(int documentLength) {
        if (documentLength >= largeDocumentThreshold) {
            return Math.max(largeDocumentSegmentSize, overlap + 1);
        }
        if (documentLength <= smallDocumentThreshold) {
            return Math.max(smallDocumentSegmentSize, overlap + 1);
        }
        return Math.max(defaultSegmentSize, overlap + 1);
    }
}

