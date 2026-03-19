-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 1. 专业就业数据表
CREATE TABLE IF NOT EXISTS major_employment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    major_id BIGINT NOT NULL,
    year INT NOT NULL,
    employment_rate DECIMAL(5,2),
    avg_salary INT,
    median_salary INT,
    match_rate DECIMAL(5,2),
    upgrade_rate DECIMAL(5,2),
    industry_distribution JSON,
    typical_jobs JSON,
    education_requirement VARCHAR(50),
    data_source VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_major_year (major_id, year),
    INDEX idx_year (year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 行业数据表
CREATE TABLE IF NOT EXISTS industry (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    avg_salary INT,
    growth_rate DECIMAL(5,2),
    job_count INT,
    description TEXT,
    trend VARCHAR(20),
    hot_cities JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 职业数据表
CREATE TABLE IF NOT EXISTS career (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    industry_id BIGINT,
    avg_salary INT,
    salary_range VARCHAR(50),
    education_requirement VARCHAR(50),
    skill_requirements JSON,
    career_path TEXT,
    description TEXT,
    job_count INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_industry (industry_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 专业-职业关联表
CREATE TABLE IF NOT EXISTS major_career_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    major_id BIGINT NOT NULL,
    career_id BIGINT NOT NULL,
    match_degree INT,
    employment_percentage DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_major (major_id),
    INDEX idx_career (career_id),
    UNIQUE KEY uk_major_career (major_id, career_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 升学路径表
CREATE TABLE IF NOT EXISTS enrollment_path (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    description TEXT,
    requirements TEXT,
    universities JSON,
    majors JSON,
    timeline JSON,
    advantages TEXT,
    disadvantages TEXT,
    suitable_students TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 专科院校扩展信息表
CREATE TABLE IF NOT EXISTS vocational_college_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    university_id BIGINT NOT NULL,
    is_double_high BOOLEAN DEFAULT FALSE,
    is_demonstration BOOLEAN DEFAULT FALSE,
    level VARCHAR(20),
    featured_majors JSON,
    enterprise_cooperation JSON,
    order_training JSON,
    upgrade_rate DECIMAL(5,2),
    employment_rate DECIMAL(5,2),
    internship_opportunities TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_university (university_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 城市就业数据表
CREATE TABLE IF NOT EXISTS city_employment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    city VARCHAR(50) NOT NULL,
    province VARCHAR(50),
    tier VARCHAR(20),
    avg_salary INT,
    job_count INT,
    hot_industries JSON,
    living_cost INT,
    development_potential VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tier (tier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 专业评分表
CREATE TABLE IF NOT EXISTS major_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    major_id BIGINT NOT NULL,
    employment_score INT,
    salary_score INT,
    development_score INT,
    stability_score INT,
    total_score INT,
    recommendation_level VARCHAR(20),
    year INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_major_year (major_id, year),
    INDEX idx_total_score (total_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 职业测评结果表
CREATE TABLE IF NOT EXISTS career_assessment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    assessment_type VARCHAR(50),
    result JSON,
    recommended_careers JSON,
    recommended_majors JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. 志愿推荐记录表
CREATE TABLE IF NOT EXISTS recommendation_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    score INT,
    province VARCHAR(50),
    subject_type VARCHAR(20),
    career_preference JSON,
    location_preference JSON,
    recommendations JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
