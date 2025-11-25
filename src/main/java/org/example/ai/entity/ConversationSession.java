package org.example.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话实体 - 用于MySQL存储会话元数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("conversation_session")
public class ConversationSession {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
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
     * 会话标题
     */
    @TableField("title")
    private String title;
    
    /**
     * 会话状态
     */
    @TableField("status")
    private SessionStatus status;
    
    /**
     * 消息数量
     */
    @TableField("message_count")
    private Integer messageCount;
    
    /**
     * 总令牌消耗
     */
    @TableField("total_tokens")
    private Integer totalTokens;
    
    /**
     * 最后活跃时间
     */
    @TableField("last_active_time")
    private LocalDateTime lastActiveTime;
    
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
    
    public enum SessionStatus {
        ACTIVE, ARCHIVED, DELETED
    }
}