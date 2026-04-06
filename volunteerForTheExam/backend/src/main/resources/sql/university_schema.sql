-- 院校信息表
CREATE TABLE IF NOT EXISTS university (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '院校ID',
    name VARCHAR(100) NOT NULL COMMENT '院校名称',
    short_name VARCHAR(50) COMMENT '院校简称',
    logo_url VARCHAR(255) COMMENT '院校LOGO',
    
    -- 基本信息
    province VARCHAR(20) COMMENT '所在省份',
    city VARCHAR(50) COMMENT '所在城市',
    address VARCHAR(255) COMMENT '详细地址',
    
    -- 办学信息
    type VARCHAR(20) COMMENT '院校类型：综合、理工、师范、医药等',
    nature VARCHAR(20) COMMENT '办学性质：公办、民办',
    level VARCHAR(200) COMMENT '办学层次：985、211、双一流等',
    department VARCHAR(50) COMMENT '主管部门：教育部、省教育厅等',
    
    -- 排名信息
    ranking INT COMMENT '综合排名',
    
    -- 联系方式
    phone VARCHAR(100) COMMENT '官方电话',
    website VARCHAR(255) COMMENT '官方网站',
    email VARCHAR(100) COMMENT '官方邮箱',
    
    -- 学科信息
    master_points INT COMMENT '硕士点数量',
    doctor_points INT COMMENT '博士点数量',
    academicians INT COMMENT '院士数量',
    outstanding_alumni INT COMMENT '杰出校友数量',
    
    -- 统计数据
    enrollment_rate DECIMAL(5,2) COMMENT '升学率',
    postgraduate_rate DECIMAL(5,2) COMMENT '保研率',
    
    -- 历史信息
    founded_year INT COMMENT '建校年份',
    introduction TEXT COMMENT '院校简介',
    
    -- 其他
    total_students INT COMMENT '在校生人数',
    campus_area DECIMAL(10,2) COMMENT '校园面积（亩）',
    
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_province (province),
    INDEX idx_type (type),
    INDEX idx_level (level(50))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院校信息表';

-- 院系设置表
CREATE TABLE IF NOT EXISTS university_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    university_id BIGINT NOT NULL COMMENT '院校ID',
    name VARCHAR(100) NOT NULL COMMENT '院系名称',
    major_count INT COMMENT '专业数量',
    introduction TEXT COMMENT '院系介绍',
    
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_university_id (university_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院系设置表';

-- 专业信息表
CREATE TABLE IF NOT EXISTS major (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '专业ID',
    code VARCHAR(20) COMMENT '专业代码（国标码）',
    name VARCHAR(100) NOT NULL COMMENT '专业名称',
    
    -- 分类信息
    category VARCHAR(50) COMMENT '学科门类：工学、理学、文学等',
    sub_category VARCHAR(50) COMMENT '专业类别：计算机类、电子信息类等',
    
    -- 基本信息
    degree_level VARCHAR(20) COMMENT '学历层次：本科、专科、职业本科',
    study_years VARCHAR(20) COMMENT '学业年限：4年、3年等',
    degree_type VARCHAR(50) COMMENT '授予学位：工学学士、理学学士等',
    
    -- 比例信息
    male_ratio DECIMAL(5,2) COMMENT '男生比例',
    female_ratio DECIMAL(5,2) COMMENT '女生比例',
    
    -- 详细信息
    introduction TEXT COMMENT '专业介绍',
    courses TEXT COMMENT '专业课程（JSON格式）',
    postgraduate_directions TEXT COMMENT '考研方向（JSON格式）',
    
    -- 就业信息
    employment_rate DECIMAL(5,2) COMMENT '就业率',
    employment_prospect TEXT COMMENT '就业前景',
    salary_range VARCHAR(50) COMMENT '薪资范围',
    
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_category (category),
    INDEX idx_sub_category (sub_category),
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业信息表';

-- 院校专业关联表
CREATE TABLE IF NOT EXISTS university_major (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    university_id BIGINT NOT NULL COMMENT '院校ID',
    major_id BIGINT NOT NULL COMMENT '专业ID',
    department_id BIGINT COMMENT '所属院系ID',
    
    -- 招生信息
    is_featured TINYINT(1) DEFAULT 0 COMMENT '是否特色专业',
    feature_level VARCHAR(20) COMMENT '特色级别：国家级、省级',
    enrollment_plan INT COMMENT '招生计划',
    
    -- 录取信息
    min_score INT COMMENT '最低录取分数',
    avg_score INT COMMENT '平均录取分数',
    max_score INT COMMENT '最高录取分数',
    year INT COMMENT '年份',
    province VARCHAR(20) COMMENT '省份',
    
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_university_id (university_id),
    INDEX idx_major_id (major_id),
    INDEX idx_year (year),
    UNIQUE KEY uk_university_major_year (university_id, major_id, year, province)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院校专业关联表';

-- 招生简章表
CREATE TABLE IF NOT EXISTS enrollment_brochure (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    university_id BIGINT NOT NULL COMMENT '院校ID',
    title VARCHAR(255) NOT NULL COMMENT '简章标题',
    year INT COMMENT '年份',
    type VARCHAR(50) COMMENT '类型：本科、强基计划、自强计划等',
    content TEXT COMMENT '简章内容',
    file_url VARCHAR(255) COMMENT '文件链接',
    publish_date DATE COMMENT '发布日期',
    
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_university_id (university_id),
    INDEX idx_year (year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招生简章表';

-- 就业报告表
CREATE TABLE IF NOT EXISTS employment_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    university_id BIGINT NOT NULL COMMENT '院校ID',
    title VARCHAR(255) NOT NULL COMMENT '报告标题',
    year INT COMMENT '年份',
    type VARCHAR(20) COMMENT '类型：本科生、研究生',
    content TEXT COMMENT '报告内容',
    file_url VARCHAR(255) COMMENT '文件链接',
    
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_university_id (university_id),
    INDEX idx_year (year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就业报告表';
