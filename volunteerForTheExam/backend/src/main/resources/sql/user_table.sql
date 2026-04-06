-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `openid` VARCHAR(100) NOT NULL COMMENT '微信openid',
  `unionid` VARCHAR(100) DEFAULT NULL COMMENT '微信unionid',
  `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
  `score` INT DEFAULT NULL COMMENT '高考分数',
  `year` INT DEFAULT NULL COMMENT '高考年份',
  `subject_type` VARCHAR(20) DEFAULT NULL COMMENT '科目类型：文科/理科/新高考',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_unionid` (`unionid`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户收藏表
CREATE TABLE IF NOT EXISTS `user_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type` VARCHAR(20) NOT NULL COMMENT '收藏类型：university-院校，major-专业',
  `target_id` BIGINT NOT NULL COMMENT '目标ID（院校ID或专业ID）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_type_target` (`user_id`, `type`, `target_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- 用户浏览历史表
CREATE TABLE IF NOT EXISTS `user_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type` VARCHAR(20) NOT NULL COMMENT '浏览类型：university-院校，major-专业',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `view_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_view_time` (`view_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户浏览历史表';
