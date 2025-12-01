package org.example.ai.mcp;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Collections;

/**
 * 配置MCP,智普AI的免费mcp大模型
 */
@Configuration
@Slf4j
public class McpConfig {

    @Value("${bigmodel.api-key}")
    private String apiKey;

    @Value("${bigmodel.sse-url}")
    private String sseUrl;

    @Value("${bigmodel.mcp-key}")
    private String mcpKey;

    @Bean
    public McpToolProvider mcpToolProvider() {
        // 和 MCP 服务通讯 - 注意：这是一个可选的工具提供者
        // 如果连接失败，不会影响主要的 AI 功能
        try {
            // MCP 服务通讯
            McpTransport transport = new HttpMcpTransport.Builder()
                    .sseUrl(sseUrl + apiKey)
                    .logRequests(true) // 开启日志，查看更多信息
                    .logResponses(true)
                    .timeout(Duration.ofSeconds(60))
                    .build();

            // 创建 MCP 客户端
            McpClient mcpClient = new DefaultMcpClient.Builder()
                    .key(mcpKey)
                    .transport(transport)
                    .initializationTimeout(Duration.ofSeconds(60))
                    .build();

            // 从 MCP 客户端获取工具
            McpToolProvider toolProvider = McpToolProvider.builder()
                    .mcpClients(Collections.singletonList(mcpClient))
                    .build();
            return toolProvider;

        } catch (Exception e) {
            // 如果 MCP 连接失败，记录警告但不抛出异常
            // 这样可以让应用程序继续运行，只是没有额外的工具功能
            log.warn("无法连接到BigModel-MCP服务，跳过相关功能(不影响使用)。错误信息: " + e.getMessage(), e);

            // 返回一个空的工具提供者，使用空集合而不是null
            return McpToolProvider.builder()
                    .mcpClients(Collections.emptyList())
                    .build();
        }
    }
}