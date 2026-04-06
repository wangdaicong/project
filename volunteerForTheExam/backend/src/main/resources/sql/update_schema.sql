-- 更新表结构以支持新的数据字段

USE volunteer_exam;

-- 1. 更新university表，添加985/211/双一流标识
-- 检查并添加字段（如果已存在会报错但不影响后续执行）
ALTER TABLE university ADD COLUMN is_985 TINYINT(1) DEFAULT 0 COMMENT '是否985高校';
ALTER TABLE university ADD COLUMN is_211 TINYINT(1) DEFAULT 0 COMMENT '是否211高校';
ALTER TABLE university ADD COLUMN is_double_first_class TINYINT(1) DEFAULT 0 COMMENT '是否双一流高校';
ALTER TABLE university ADD COLUMN created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE university ADD COLUMN updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 2. 创建独立的major表（不依赖university_id）
CREATE TABLE IF NOT EXISTS major_info (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  name VARCHAR(100) NOT NULL COMMENT '专业名称',
  code VARCHAR(20) COMMENT '专业代码',
  category VARCHAR(50) COMMENT '专业类别（工学/理学/医学等）',
  degree VARCHAR(20) COMMENT '学位类型',
  years INT DEFAULT 4 COMMENT '学制（年）',
  introduction TEXT COMMENT '专业介绍',
  main_courses TEXT COMMENT '主要课程',
  employment_direction TEXT COMMENT '就业方向',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_code (code),
  INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业信息表';

-- 3. 创建score_line表（历年分数线）
CREATE TABLE IF NOT EXISTS score_line (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  university_id BIGINT NOT NULL COMMENT '高校ID',
  province VARCHAR(50) NOT NULL COMMENT '省份',
  year INT NOT NULL COMMENT '年份',
  category VARCHAR(20) NOT NULL COMMENT '科类（理科/文科）',
  batch VARCHAR(50) COMMENT '批次',
  min_score INT COMMENT '最低分',
  avg_score INT COMMENT '平均分',
  max_score INT COMMENT '最高分',
  min_rank INT COMMENT '最低位次',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_score (university_id, province, year, category),
  INDEX idx_university_year (university_id, year),
  INDEX idx_province_year (province, year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历年分数线表';

-- 4. 创建employment_data表（就业数据）
CREATE TABLE IF NOT EXISTS employment_data (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  major_id BIGINT NOT NULL COMMENT '专业ID',
  year INT NOT NULL COMMENT '年份',
  employment_rate DECIMAL(5,2) COMMENT '就业率（%）',
  average_salary INT COMMENT '平均薪资（元/月）',
  top_industries TEXT COMMENT '主要就业行业',
  top_positions TEXT COMMENT '主要就业岗位',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_major_year (major_id, year),
  INDEX idx_year (year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就业数据表';

-- 5. 创建university_major关联表
CREATE TABLE IF NOT EXISTS university_major (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  university_id BIGINT NOT NULL COMMENT '高校ID',
  major_id BIGINT NOT NULL COMMENT '专业ID',
  enrollment_plan INT COMMENT '招生计划',
  tuition_fee INT COMMENT '学费（元/年）',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_university_major (university_id, major_id),
  INDEX idx_university (university_id),
  INDEX idx_major (major_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='高校专业关联表';

-- 显示表结构更新结果
SELECT 'Schema update completed!' as status;
