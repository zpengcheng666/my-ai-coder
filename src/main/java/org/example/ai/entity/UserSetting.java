package org.example.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户设置实体类 - 用于存储用户的个性化设置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_setting")
public class UserSetting {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;
    
    /**
     * 用户名称
     */
    @TableField("user_name")
    private String userName;
    
    /**
     * 流式输出模式
     */
    @TableField("stream_mode")
    private Boolean streamMode;
    
    /**
     * 自动滚动到底部
     */
    @TableField("auto_scroll")
    private Boolean autoScroll;
    
    /**
     * 显示消息时间戳
     */
    @TableField("show_timestamp")
    private Boolean showTimestamp;
    
    /**
     * API基础地址
     */
    @TableField("api_base_url")
    private String apiBaseUrl;
    
    /**
     * 超时时间（秒）
     */
    @TableField("timeout")
    private Integer timeout;
    
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
}