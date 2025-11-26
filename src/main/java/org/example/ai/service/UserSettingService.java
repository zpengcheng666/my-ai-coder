package org.example.ai.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ai.entity.UserSetting;
import org.example.ai.mapper.UserSettingMapper;
import org.springframework.stereotype.Service;

/**
 * 用户设置服务类
 */
@Service
public class UserSettingService extends ServiceImpl<UserSettingMapper, UserSetting> {
    
    private final UserSettingMapper userSettingMapper;
    
    public UserSettingService(UserSettingMapper userSettingMapper) {
        this.userSettingMapper = userSettingMapper;
    }
    
    /**
     * 根据用户ID获取用户设置
     * @param userId 用户ID
     * @return 用户设置
     */
    public UserSetting getByUserId(String userId) {
        return userSettingMapper.findByUserId(userId);
    }
    
    /**
     * 保存或更新用户设置
     * @param userSetting 用户设置
     * @return 是否保存成功
     */
    public boolean saveOrUpdateByUserId(UserSetting userSetting) {
        UserSetting existingSetting = userSettingMapper.findByUserId(userSetting.getUserId());
        if (existingSetting != null) {
            userSetting.setId(existingSetting.getId());
            return this.updateById(userSetting);
        } else {
            return this.save(userSetting);
        }
    }
}