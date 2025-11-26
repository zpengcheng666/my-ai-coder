package org.example.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.example.ai.entity.UserSetting;

/**
 * 用户设置 Mapper 接口
 */
public interface UserSettingMapper extends BaseMapper<UserSetting> {
    
    /**
     * 根据用户ID查找用户设置
     * @param userId 用户ID
     * @return 用户设置
     */
    UserSetting findByUserId(@Param("userId") String userId);
    
    /**
     * 根据用户ID更新用户设置
     * @param userSetting 用户设置
     * @return 影响行数
     */
    int updateByUserId(UserSetting userSetting);
}