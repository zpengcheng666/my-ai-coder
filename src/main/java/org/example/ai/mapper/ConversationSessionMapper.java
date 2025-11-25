package org.example.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.example.ai.entity.ConversationSession;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话Session Mapper
 */
public interface ConversationSessionMapper extends BaseMapper<ConversationSession> {
    
    /**
     * 根据会话ID查询
     */
    ConversationSession selectByConversationId(String conversationId);
    
    /**
     * 根据用户ID查询会话列表（分页）
     */
    IPage<ConversationSession> selectByUserIdAndStatusOrderByLastActiveTimeDesc(
            Page<ConversationSession> page,
            @Param("userId") String userId,
            @Param("status") ConversationSession.SessionStatus status);
    
    /**
     * 根据用户ID查询所有活跃会话
     */
    List<ConversationSession> selectByUserIdAndStatusOrderByLastActiveTimeDesc(
            @Param("userId") String userId,
            @Param("status") ConversationSession.SessionStatus status);
    
    /**
     * 更新会话的最后活跃时间和消息数
     */
    void updateSessionActivity(
            @Param("conversationId") String conversationId,
            @Param("lastActiveTime") LocalDateTime lastActiveTime,
            @Param("tokenUsed") Integer tokenUsed);
    
    /**
     * 查询需要归档的会话（超过指定时间未活跃）
     */
    List<ConversationSession> selectSessionsToArchive(@Param("archiveTime") LocalDateTime archiveTime);

    /**
     * 软删除会话（将状态置为DELETED）
     */
    int softDeleteByConversationIdAndUserId(
            @Param("conversationId") String conversationId,
            @Param("userId") String userId);
}