-- 简化版院校库和专业库数据表设计
-- 只保留核心的院校、专业和院校-专业关联表

-- 1. 院校信息表（已存在，但需要确保字段完整）
-- university表应该包含以下字段，如果不存在需要添加

-- 2. 专业信息表（已存在，但需要确保字段完整）
-- major表应该包含以下字段，如果不存在需要添加

-- 3. 院校-专业关联表（核心表）
CREATE TABLE IF NOT EXISTS `university_major` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `university_id` BIGINT NOT NULL COMMENT '院校ID',
  `major_id` BIGINT NOT NULL COMMENT '专业ID',
  `major_code` VARCHAR(20) COMMENT '专业代码',
  `major_name` VARCHAR(100) NOT NULL COMMENT '专业名称',
  `category` VARCHAR(50) COMMENT '学科门类',
  `sub_category` VARCHAR(50) COMMENT '专业类别',
  `degree_level` VARCHAR(20) COMMENT '学历层次：本科/专科/职业本科',
  `degree_type` VARCHAR(50) COMMENT '学位类型：工学学士/理学学士等',
  `duration` VARCHAR(20) COMMENT '学制：4年/3年等',
  `tuition` VARCHAR(50) COMMENT '学费',
  `enrollment_year` INT COMMENT '开设年份',
  `province` VARCHAR(50) COMMENT '招生省份',
  `enrollment_plan` INT COMMENT '招生计划人数',
  `min_score` INT COMMENT '最低录取分数',
  `avg_score` INT COMMENT '平均录取分数',
  `max_score` INT COMMENT '最高录取分数',
  `rank_min` INT COMMENT '最低位次',
  `rank_avg` INT COMMENT '平均位次',
  `subject_requirement` VARCHAR(200) COMMENT '选科要求',
  `is_featured` TINYINT(1) DEFAULT 0 COMMENT '是否特色专业',
  `feature_level` VARCHAR(50) COMMENT '特色级别：国家级/省级',
  `description` TEXT COMMENT '专业描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_university_id` (`university_id`),
  KEY `idx_major_id` (`major_id`),
  KEY `idx_major_name` (`major_name`),
  KEY `idx_category` (`category`),
  KEY `idx_degree_level` (`degree_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院校-专业关联表';

-- 确保university表有必要字段
ALTER TABLE `university` 
ADD COLUMN IF NOT EXISTS `logo_url` VARCHAR(500) COMMENT '院校LOGO地址',
ADD COLUMN IF NOT EXISTS `introduction` TEXT COMMENT '院校简介',
ADD COLUMN IF NOT EXISTS `founded_year` INT COMMENT '建校年份',
ADD COLUMN IF NOT EXISTS `nature` VARCHAR(50) COMMENT '办学性质：公办/民办',
ADD COLUMN IF NOT EXISTS `phone` VARCHAR(50) COMMENT '联系电话',
ADD COLUMN IF NOT EXISTS `website` VARCHAR(200) COMMENT '官方网站',
ADD COLUMN IF NOT EXISTS `address` VARCHAR(200) COMMENT '院校地址',
ADD COLUMN IF NOT EXISTS `master_points` INT DEFAULT 0 COMMENT '硕士点数量',
ADD COLUMN IF NOT EXISTS `doctor_points` INT DEFAULT 0 COMMENT '博士点数量',
ADD COLUMN IF NOT EXISTS `ranking` INT COMMENT '综合排名',
ADD COLUMN IF NOT EXISTS `is_985` TINYINT(1) DEFAULT 0 COMMENT '是否985',
ADD COLUMN IF NOT EXISTS `is_211` TINYINT(1) DEFAULT 0 COMMENT '是否211',
ADD COLUMN IF NOT EXISTS `is_double_first_class` TINYINT(1) DEFAULT 0 COMMENT '是否双一流';

-- 确保major表有必要字段
ALTER TABLE `major` 
ADD COLUMN IF NOT EXISTS `major_code` VARCHAR(20) COMMENT '专业代码',
ADD COLUMN IF NOT EXISTS `category` VARCHAR(50) COMMENT '学科门类',
ADD COLUMN IF NOT EXISTS `sub_category` VARCHAR(50) COMMENT '专业类别',
ADD COLUMN IF NOT EXISTS `degree_level` VARCHAR(20) COMMENT '学历层次',
ADD COLUMN IF NOT EXISTS `degree_type` VARCHAR(50) COMMENT '学位类型',
ADD COLUMN IF NOT EXISTS `duration` VARCHAR(20) COMMENT '学制',
ADD COLUMN IF NOT EXISTS `description` TEXT COMMENT '专业介绍',
ADD COLUMN IF NOT EXISTS `employment_rate` DECIMAL(5,2) COMMENT '就业率',
ADD COLUMN IF NOT EXISTS `salary_avg` INT COMMENT '平均薪资';
