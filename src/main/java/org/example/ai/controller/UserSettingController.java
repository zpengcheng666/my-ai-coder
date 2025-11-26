package org.example.ai.controller;

import org.example.ai.entity.UserSetting;
import org.example.ai.service.UserSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户设置控制器
 */
@RestController
@RequestMapping("/api/settings")
public class UserSettingController {
    
    private final UserSettingService userSettingService;
    
    public UserSettingController(UserSettingService userSettingService) {
        this.userSettingService = userSettingService;
    }
    
    /**
     * 获取用户设置
     * @param userId 用户ID
     * @return 用户设置
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserSetting> getUserSettings(@PathVariable String userId) {
        UserSetting settings = userSettingService.getByUserId(userId);
        return ResponseEntity.ok(settings);
    }
    
    /**
     * 保存用户设置
     * @param userId 用户ID
     * @param userSetting 用户设置
     * @return 是否保存成功
     */
    @PostMapping("/{userId}")
    public ResponseEntity<Boolean> saveUserSettings(@PathVariable String userId, @RequestBody UserSetting userSetting) {
        userSetting.setUserId(userId);
        boolean result = userSettingService.saveOrUpdateByUserId(userSetting);
        return ResponseEntity.ok(result);
    }
}