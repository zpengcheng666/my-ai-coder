package org.example.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.example.ai.entity.ConversationHistory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话历史Mapper
 */
public interface ConversationHistoryMapper extends BaseMapper<ConversationHistory> {
    
    /**
     * 根据会话ID查询消息历史
     */
    List<ConversationHistory> selectByConversationIdOrderByCreateTimeAsc(String conversationId);
    
    /**
     * 根据用户ID查询消息历史（分页）
     */
    IPage<ConversationHistory> selectByUserIdOrderByCreateTimeDesc(Page<ConversationHistory> page, String userId);
    
    /**
     * 根据用户ID和时间范围查询消息历史
     */
    List<ConversationHistory> selectByUserIdAndCreateTimeBetween(
            @Param("userId") String userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计用户总消息数
     */
    long countByUserId(String userId);
    
    /**
     * 统计用户总token消耗
     */
    Long sumTokenUsedByUserId(@Param("userId") String userId);
    
    /**
     * 删除指定时间之前的历史记录
     */
    void deleteByCreateTimeBefore(LocalDateTime time);
    
    /**
     * 根据会话ID删除历史记录
     */
    void deleteByConversationId(@Param("conversationId") String conversationId);
}