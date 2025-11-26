CREATE TABLE `user_setting` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `user_name` varchar(100) DEFAULT '用户' COMMENT '用户名称',
  `stream_mode` tinyint(1) DEFAULT '1' COMMENT '流式输出模式: 0-关闭, 1-开启',
  `auto_scroll` tinyint(1) DEFAULT '1' COMMENT '自动滚动到底部: 0-关闭, 1-开启',
  `show_timestamp` tinyint(1) DEFAULT '1' COMMENT '显示消息时间戳: 0-关闭, 1-开启',
  `api_base_url` varchar(255) DEFAULT 'http://localhost:8081/api' COMMENT 'API基础地址',
  `timeout` int DEFAULT '60' COMMENT '超时时间（秒）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户设置表';