package org.example.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.spring.AiService;
import org.example.ai.guardrail.SafeInputGuardrail;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 是声明式服务，框架自动处理很多细节,提供更多高级功能，如流式响应、结构化输出等
 */
//改为手动构建，更灵活
//@AiService
@InputGuardrails({SafeInputGuardrail.class})
public interface AiCodeHelperService {

    /**
     * 聊天,可以根据系统提示词和用户提示词进行回答
     * @param memoryId 存储记忆id
     * @param userMessage 用户输入提示词
     * @return 聊天结果
     */
    @SystemMessage(fromResource = "system-prompt.txt")
    String chat(@MemoryId String  memoryId, @UserMessage String userMessage);

    @SystemMessage(fromResource = "system-prompt.txt")
    Flux<String> chatStream(@MemoryId String memoryId, @UserMessage String userMessage);

    @SystemMessage(fromResource = "system-prompt.txt")
    Report chatForReport(@MemoryId String  memoryId, @UserMessage String userMessage);

    record Report(String message,List<String> stringList) {
    }

    @SystemMessage(fromResource = "system-prompt.txt")
    Result<String> chatWthRag(String userMessage);
}
