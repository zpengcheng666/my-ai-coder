package org.example.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话历史实体 - 用于MySQL持久化存储
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("conversation_history")
public class ConversationHistory {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 消息ID
     */
    @TableField("message_id")
    private String messageId;
    
    /**
     * 会话ID
     */
    @TableField("conversation_id")
    private String conversationId;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;
    
    /**
     * 消息类型
     */
    @TableField("message_type")
    private MessageType messageType;
    
    /**
     * 消息内容
     */
    @TableField("content")
    private String content;
    
    /**
     * 令牌消耗
     */
    @TableField("token_used")
    private Integer tokenUsed;
    
    /**
     * 响应时间（毫秒）
     */
    @TableField("response_time_ms")
    private Long responseTimeMs;
    
    /**
     * 模型名称
     */
    @TableField("model_name")
    private String modelName;
    
    /**
     * 是否是流式响应
     */
    @TableField("is_streaming")
    private Boolean isStreaming;
    
    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;
    
    /**
     * 用户代理
     */
    @TableField("user_agent")
    private String userAgent;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    public enum MessageType {
        USER, ASSISTANT
    }
}