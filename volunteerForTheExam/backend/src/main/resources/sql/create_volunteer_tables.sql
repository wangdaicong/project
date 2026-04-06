-- 志愿填报模拟系统数据库表

-- 志愿填报记录表
CREATE TABLE IF NOT EXISTS volunteer_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '用户ID',
    student_name VARCHAR(50) NOT NULL COMMENT '考生姓名',
    province VARCHAR(50) NOT NULL COMMENT '省份',
    score INT NOT NULL COMMENT '高考分数',
    category VARCHAR(20) NOT NULL COMMENT '科类（理科/文科）',
    rank_position INT COMMENT '位次',
    batch VARCHAR(50) DEFAULT '本科一批' COMMENT '批次',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft草稿/submitted已提交',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿填报记录表';

-- 志愿详情表（每个志愿填报可以包含多个志愿）
CREATE TABLE IF NOT EXISTS volunteer_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    application_id BIGINT NOT NULL COMMENT '志愿填报记录ID',
    volunteer_order INT NOT NULL COMMENT '志愿顺序（1-N）',
    university_id BIGINT NOT NULL COMMENT '院校ID',
    university_name VARCHAR(100) NOT NULL COMMENT '院校名称',
    major_id BIGINT COMMENT '专业ID',
    major_name VARCHAR(100) COMMENT '专业名称',
    admission_probability VARCHAR(20) COMMENT '录取概率：high高/medium中/low低',
    risk_level VARCHAR(20) COMMENT '风险等级：safe保底/stable稳妥/rush冲刺',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_application_id (application_id),
    INDEX idx_university_id (university_id),
    FOREIGN KEY (application_id) REFERENCES volunteer_application(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿详情表';

-- 志愿分析结果表
CREATE TABLE IF NOT EXISTS volunteer_analysis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    application_id BIGINT NOT NULL COMMENT '志愿填报记录ID',
    total_volunteers INT COMMENT '志愿总数',
    rush_count INT COMMENT '冲刺志愿数',
    stable_count INT COMMENT '稳妥志愿数',
    safe_count INT COMMENT '保底志愿数',
    risk_score DECIMAL(5,2) COMMENT '风险评分（0-100）',
    suggestion TEXT COMMENT '填报建议',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_application_id (application_id),
    FOREIGN KEY (application_id) REFERENCES volunteer_application(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿分析结果表';
