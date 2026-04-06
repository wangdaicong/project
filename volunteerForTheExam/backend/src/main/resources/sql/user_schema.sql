-- 用户相关表结构

USE volunteer_exam;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  openid VARCHAR(100) UNIQUE COMMENT '微信openid',
  nickname VARCHAR(100) COMMENT '昵称',
  avatar_url VARCHAR(500) COMMENT '头像URL',
  phone VARCHAR(20) COMMENT '手机号',
  province VARCHAR(50) COMMENT '所在省份',
  score INT COMMENT '高考分数',
  category VARCHAR(20) COMMENT '科类（理科/文科）',
  rank_number INT COMMENT '省排名',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  INDEX idx_openid (openid),
  INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 用户收藏表
CREATE TABLE IF NOT EXISTS user_favorite (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  university_id BIGINT NOT NULL COMMENT '院校ID',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_university (user_id, university_id),
  INDEX idx_user (user_id),
  INDEX idx_university (university_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- 3. 浏览历史表
CREATE TABLE IF NOT EXISTS browse_history (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  university_id BIGINT COMMENT '院校ID',
  major_id BIGINT COMMENT '专业ID',
  browse_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  PRIMARY KEY (id),
  INDEX idx_user_time (user_id, browse_time),
  INDEX idx_university (university_id),
  INDEX idx_major (major_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览历史表';

-- 4. 测评记录表
CREATE TABLE IF NOT EXISTS assessment_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  assessment_type VARCHAR(50) NOT NULL COMMENT '测评类型',
  result TEXT COMMENT '测评结果（JSON格式）',
  score INT COMMENT '得分',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  INDEX idx_user_type (user_id, assessment_type),
  INDEX idx_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测评记录表';

-- 5. 志愿填报记录表
CREATE TABLE IF NOT EXISTS volunteer_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  year INT NOT NULL COMMENT '年份',
  volunteers TEXT COMMENT '志愿列表（JSON格式）',
  status VARCHAR(20) DEFAULT 'draft' COMMENT '状态（draft草稿/submitted已提交）',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  INDEX idx_user_year (user_id, year),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='志愿填报记录表';

-- 显示创建结果
SELECT 'User tables created successfully!' as status;
