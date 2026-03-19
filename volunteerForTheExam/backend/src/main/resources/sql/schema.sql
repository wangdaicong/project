CREATE DATABASE IF NOT EXISTS volunteer_exam DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE volunteer_exam;

CREATE TABLE IF NOT EXISTS `university` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '学校名称',
  `province` VARCHAR(50) NOT NULL COMMENT '所在省份',
  `city` VARCHAR(50) COMMENT '所在城市',
  `level` VARCHAR(20) COMMENT '办学层次（985/211/双一流/普通本科）',
  `type` VARCHAR(20) COMMENT '学校类型（综合/理工/师范/医药等）',
  `tags` VARCHAR(200) COMMENT '标签',
  `introduction` TEXT COMMENT '学校简介',
  `address` VARCHAR(200) COMMENT '学校地址',
  `website` VARCHAR(200) COMMENT '官网',
  `phone` VARCHAR(50) COMMENT '联系电话',
  `min_score` INT COMMENT '最低录取分数',
  `max_score` INT COMMENT '最高录取分数',
  `logo_url` VARCHAR(200) COMMENT 'Logo图片',
  `ranking` INT COMMENT '排名',
  `features` TEXT COMMENT '办学特色',
  PRIMARY KEY (`id`),
  INDEX `idx_province` (`province`),
  INDEX `idx_level` (`level`),
  INDEX `idx_score` (`min_score`, `max_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='高校信息表';

CREATE TABLE IF NOT EXISTS `major` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `university_id` BIGINT NOT NULL COMMENT '所属高校ID',
  `name` VARCHAR(100) NOT NULL COMMENT '专业名称',
  `category` VARCHAR(50) COMMENT '专业类别',
  `degree` VARCHAR(20) COMMENT '学位类型（学士/硕士/博士）',
  `introduction` TEXT COMMENT '专业介绍',
  `courses` TEXT COMMENT '主要课程',
  `employment_direction` TEXT COMMENT '就业方向',
  `employment_rate` DECIMAL(5,2) COMMENT '就业率',
  `duration` INT COMMENT '学制（年）',
  `min_score` INT COMMENT '最低录取分数',
  `enrollment_number` INT COMMENT '招生人数',
  PRIMARY KEY (`id`),
  INDEX `idx_university` (`university_id`),
  INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业信息表';

CREATE TABLE IF NOT EXISTS `career` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `industry` VARCHAR(50) NOT NULL COMMENT '行业',
  `position` VARCHAR(100) NOT NULL COMMENT '职位名称',
  `description` TEXT COMMENT '职位描述',
  `trend` TEXT COMMENT '发展趋势',
  `salary` VARCHAR(50) COMMENT '薪资范围',
  `requirements` TEXT COMMENT '任职要求',
  `related_majors` VARCHAR(200) COMMENT '相关专业',
  `development_path` TEXT COMMENT '发展路径',
  `demand_index` INT COMMENT '需求指数',
  PRIMARY KEY (`id`),
  INDEX `idx_industry` (`industry`),
  INDEX `idx_demand` (`demand_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职业信息表';

CREATE TABLE IF NOT EXISTS `admission_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `university_id` BIGINT NOT NULL COMMENT '高校ID',
  `major_id` BIGINT COMMENT '专业ID',
  `province` VARCHAR(50) NOT NULL COMMENT '省份',
  `year` INT NOT NULL COMMENT '年份',
  `min_score` INT COMMENT '最低分',
  `avg_score` INT COMMENT '平均分',
  `max_score` INT COMMENT '最高分',
  `enrollment_number` INT COMMENT '录取人数',
  `batch` VARCHAR(20) COMMENT '批次',
  PRIMARY KEY (`id`),
  INDEX `idx_university_year` (`university_id`, `year`),
  INDEX `idx_province_year` (`province`, `year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='录取记录表';
