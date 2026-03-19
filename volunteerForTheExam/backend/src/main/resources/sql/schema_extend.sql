-- ========================================
-- 数据库扩展脚本 - 支持历年分数线、排名等
-- ========================================

-- 1. 历年录取分数线表（支持专业级别）
DROP TABLE IF EXISTS `admission_score_history`;
CREATE TABLE `admission_score_history` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `university_id` BIGINT(20) NOT NULL COMMENT '院校ID',
  `major_id` BIGINT(20) DEFAULT NULL COMMENT '专业ID（为空表示院校整体分数线）',
  `province` VARCHAR(50) NOT NULL COMMENT '省份',
  `year` INT(11) NOT NULL COMMENT '年份',
  `batch` VARCHAR(50) DEFAULT NULL COMMENT '批次（本科一批、本科二批等）',
  `subject_type` VARCHAR(20) DEFAULT NULL COMMENT '科目类型（文科、理科、综合）',
  `min_score` INT(11) DEFAULT NULL COMMENT '最低分',
  `avg_score` INT(11) DEFAULT NULL COMMENT '平均分',
  `max_score` INT(11) DEFAULT NULL COMMENT '最高分',
  `min_rank` INT(11) DEFAULT NULL COMMENT '最低位次',
  `enrollment_number` INT(11) DEFAULT NULL COMMENT '招生人数',
  `applicant_number` INT(11) DEFAULT NULL COMMENT '报考人数',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_university_year` (`university_id`, `year`),
  KEY `idx_major_year` (`major_id`, `year`),
  KEY `idx_province_year` (`province`, `year`),
  KEY `idx_year` (`year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历年录取分数线表';

-- 2. 院校排名表
DROP TABLE IF EXISTS `university_ranking`;
CREATE TABLE `university_ranking` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `university_id` BIGINT(20) NOT NULL COMMENT '院校ID',
  `ranking_type` VARCHAR(50) NOT NULL COMMENT '排名类型（QS、软科、校友会、US News等）',
  `year` INT(11) NOT NULL COMMENT '年份',
  `ranking` INT(11) DEFAULT NULL COMMENT '排名',
  `score` DECIMAL(10,2) DEFAULT NULL COMMENT '评分',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类排名（综合、理工、师范等）',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_university_year` (`university_id`, `year`),
  KEY `idx_ranking_type` (`ranking_type`, `year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院校排名表';

-- 3. 专业排名表
DROP TABLE IF EXISTS `major_ranking`;
CREATE TABLE `major_ranking` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `university_id` BIGINT(20) NOT NULL COMMENT '院校ID',
  `major_id` BIGINT(20) NOT NULL COMMENT '专业ID',
  `ranking_type` VARCHAR(50) NOT NULL COMMENT '排名类型（教育部学科评估、软科等）',
  `year` INT(11) NOT NULL COMMENT '年份',
  `ranking` INT(11) DEFAULT NULL COMMENT '排名',
  `level` VARCHAR(20) DEFAULT NULL COMMENT '等级（A+、A、A-、B+等）',
  `percentile` VARCHAR(20) DEFAULT NULL COMMENT '百分位（前1%、前5%等）',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_major_year` (`major_id`, `year`),
  KEY `idx_university_major` (`university_id`, `major_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业排名表';

-- 4. 数据同步日志表
DROP TABLE IF EXISTS `sync_log`;
CREATE TABLE `sync_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sync_type` VARCHAR(50) NOT NULL COMMENT '同步类型（university、major、score、ranking等）',
  `sync_method` VARCHAR(50) DEFAULT NULL COMMENT '同步方式（api、excel、crawler）',
  `sync_status` VARCHAR(20) NOT NULL COMMENT '同步状态（success、failed、partial）',
  `total_count` INT(11) DEFAULT 0 COMMENT '总记录数',
  `success_count` INT(11) DEFAULT 0 COMMENT '成功数',
  `fail_count` INT(11) DEFAULT 0 COMMENT '失败数',
  `error_message` TEXT COMMENT '错误信息',
  `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件路径（Excel导入时）',
  `sync_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '同步时间',
  PRIMARY KEY (`id`),
  KEY `idx_sync_type` (`sync_type`),
  KEY `idx_sync_time` (`sync_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据同步日志表';

-- 5. 就业数据表（扩展）
DROP TABLE IF EXISTS `employment_data`;
CREATE TABLE `employment_data` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `university_id` BIGINT(20) DEFAULT NULL COMMENT '院校ID',
  `major_id` BIGINT(20) DEFAULT NULL COMMENT '专业ID',
  `year` INT(11) NOT NULL COMMENT '年份',
  `employment_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '就业率（%）',
  `avg_salary` INT(11) DEFAULT NULL COMMENT '平均薪资（元/月）',
  `top_industries` VARCHAR(500) DEFAULT NULL COMMENT '主要就业行业（JSON格式）',
  `top_companies` VARCHAR(500) DEFAULT NULL COMMENT '主要就业企业（JSON格式）',
  `graduate_school_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '升学率（%）',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_university_year` (`university_id`, `year`),
  KEY `idx_major_year` (`major_id`, `year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就业数据表';

-- 6. 用户收藏表
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` VARCHAR(100) NOT NULL COMMENT '用户ID（微信openid）',
  `favorite_type` VARCHAR(20) NOT NULL COMMENT '收藏类型（university、major、career）',
  `favorite_id` BIGINT(20) NOT NULL COMMENT '收藏对象ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_favorite` (`user_id`, `favorite_type`, `favorite_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- 7. 用户搜索历史表
DROP TABLE IF EXISTS `search_history`;
CREATE TABLE `search_history` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` VARCHAR(100) NOT NULL COMMENT '用户ID（微信openid）',
  `keyword` VARCHAR(200) NOT NULL COMMENT '搜索关键词',
  `search_type` VARCHAR(20) DEFAULT NULL COMMENT '搜索类型（university、major、career）',
  `search_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户搜索历史表';

-- ========================================
-- 插入示例数据
-- ========================================

-- 插入历年分数线示例数据（2022-2024年）
INSERT INTO `admission_score_history` 
(`university_id`, `major_id`, `province`, `year`, `batch`, `subject_type`, `min_score`, `avg_score`, `max_score`, `min_rank`, `enrollment_number`) 
VALUES
-- 清华大学
(1, NULL, '北京', 2024, '本科一批', '综合', 685, 695, 710, 100, 200),
(1, NULL, '广东', 2024, '本科一批', '物理', 680, 693, 708, 150, 180),
(1, NULL, '北京', 2023, '本科一批', '综合', 683, 693, 708, 110, 200),
(1, NULL, '广东', 2023, '本科一批', '物理', 678, 690, 705, 160, 180),
-- 北京大学
(2, NULL, '北京', 2024, '本科一批', '综合', 683, 693, 708, 120, 180),
(2, NULL, '广东', 2024, '本科一批', '物理', 678, 690, 705, 170, 160),
(2, NULL, '北京', 2023, '本科一批', '综合', 681, 691, 706, 130, 180),
(2, NULL, '广东', 2023, '本科一批', '物理', 676, 688, 703, 180, 160);

-- 插入院校排名示例数据
INSERT INTO `university_ranking` 
(`university_id`, `ranking_type`, `year`, `ranking`, `score`, `category`) 
VALUES
(1, '软科', 2024, 1, 100.0, '综合'),
(1, 'QS', 2024, 17, 98.5, '综合'),
(2, '软科', 2024, 2, 99.8, '综合'),
(2, 'QS', 2024, 12, 99.2, '综合'),
(3, '软科', 2024, 3, 98.5, '综合'),
(4, '软科', 2024, 4, 97.8, '综合'),
(5, '软科', 2024, 5, 96.9, '综合');

-- 插入专业排名示例数据
INSERT INTO `major_ranking` 
(`university_id`, `major_id`, `ranking_type`, `year`, `ranking`, `level`, `percentile`) 
VALUES
(1, 1, '教育部学科评估', 2023, 1, 'A+', '前1%'),
(2, 1, '教育部学科评估', 2023, 2, 'A+', '前1%'),
(1, 2, '教育部学科评估', 2023, 3, 'A', '前5%'),
(2, 2, '教育部学科评估', 2023, 1, 'A+', '前1%');

-- 插入就业数据示例
INSERT INTO `employment_data` 
(`university_id`, `major_id`, `year`, `employment_rate`, `avg_salary`, `top_industries`, `graduate_school_rate`) 
VALUES
(1, 1, 2023, 98.5, 25000, '["互联网","金融","科技"]', 45.2),
(2, 1, 2023, 98.2, 24000, '["互联网","咨询","金融"]', 42.8),
(1, 2, 2023, 97.8, 18000, '["教育","政府","企业"]', 38.5);
