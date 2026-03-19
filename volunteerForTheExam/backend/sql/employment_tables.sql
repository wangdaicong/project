-- 就业相关数据表设计
-- 基于张雪峰《从就业看专业》理念

-- 1. 专业就业数据表
CREATE TABLE IF NOT EXISTS major_employment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    major_id BIGINT NOT NULL COMMENT '专业ID',
    year INT NOT NULL COMMENT '数据年份',
    employment_rate DECIMAL(5,2) COMMENT '就业率(%)',
    avg_salary INT COMMENT '平均薪资(元/月)',
    median_salary INT COMMENT '中位数薪资(元/月)',
    match_rate DECIMAL(5,2) COMMENT '专业对口率(%)',
    upgrade_rate DECIMAL(5,2) COMMENT '升学率(%)',
    industry_distribution JSON COMMENT '行业分布 {"互联网":30, "金融":20, ...}',
    typical_jobs JSON COMMENT '典型岗位 ["软件工程师", "算法工程师", ...]',
    education_requirement VARCHAR(50) COMMENT '学历要求(本科/硕士/博士)',
    data_source VARCHAR(100) COMMENT '数据来源',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_major_year (major_id, year),
    INDEX idx_year (year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业就业数据表';

-- 2. 行业数据表
CREATE TABLE IF NOT EXISTS industry (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '行业名称',
    category VARCHAR(50) COMMENT '行业分类(互联网/金融/医疗等)',
    avg_salary INT COMMENT '行业平均薪资(元/月)',
    growth_rate DECIMAL(5,2) COMMENT '增长率(%)',
    job_count INT COMMENT '岗位数量',
    description TEXT COMMENT '行业描述',
    trend VARCHAR(20) COMMENT '发展趋势(上升/稳定/下降)',
    hot_cities JSON COMMENT '热门城市 ["北京", "上海", ...]',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行业数据表';

-- 3. 职业数据表
CREATE TABLE IF NOT EXISTS career (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '职业名称',
    industry_id BIGINT COMMENT '所属行业ID',
    avg_salary INT COMMENT '平均薪资(元/月)',
    salary_range VARCHAR(50) COMMENT '薪资区间(如"8k-15k")',
    education_requirement VARCHAR(50) COMMENT '学历要求',
    skill_requirements JSON COMMENT '技能要求 ["Java", "Python", ...]',
    career_path TEXT COMMENT '职业发展路径',
    description TEXT COMMENT '职业描述',
    job_count INT COMMENT '岗位数量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_industry (industry_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职业数据表';

-- 4. 专业-职业关联表
CREATE TABLE IF NOT EXISTS major_career_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    major_id BIGINT NOT NULL COMMENT '专业ID',
    career_id BIGINT NOT NULL COMMENT '职业ID',
    match_degree INT COMMENT '匹配度(1-100)',
    employment_percentage DECIMAL(5,2) COMMENT '该专业从事该职业的比例(%)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_major (major_id),
    INDEX idx_career (career_id),
    UNIQUE KEY uk_major_career (major_id, career_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业-职业关联表';

-- 5. 升学路径表
CREATE TABLE IF NOT EXISTS enrollment_path (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '路径名称(强基计划/综合评价等)',
    type VARCHAR(50) COMMENT '类型',
    description TEXT COMMENT '详细描述',
    requirements TEXT COMMENT '报名要求',
    universities JSON COMMENT '参与院校列表',
    majors JSON COMMENT '招生专业列表',
    timeline JSON COMMENT '时间节点 [{"month":3, "event":"报名"}]',
    advantages TEXT COMMENT '优势',
    disadvantages TEXT COMMENT '劣势',
    suitable_students TEXT COMMENT '适合学生类型',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='升学路径表';

-- 6. 专科院校扩展信息表
CREATE TABLE IF NOT EXISTS vocational_college_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    university_id BIGINT NOT NULL COMMENT '院校ID',
    is_double_high BOOLEAN DEFAULT FALSE COMMENT '是否双高计划院校',
    is_demonstration BOOLEAN DEFAULT FALSE COMMENT '是否示范性高职',
    level VARCHAR(20) COMMENT '评级(S/A/B/C)',
    featured_majors JSON COMMENT '特色专业列表',
    enterprise_cooperation JSON COMMENT '校企合作企业列表',
    order_training JSON COMMENT '订单培养专业',
    upgrade_rate DECIMAL(5,2) COMMENT '专升本率(%)',
    employment_rate DECIMAL(5,2) COMMENT '就业率(%)',
    internship_opportunities TEXT COMMENT '实习就业机会说明',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_university (university_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专科院校扩展信息表';

-- 7. 城市就业数据表
CREATE TABLE IF NOT EXISTS city_employment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    city VARCHAR(50) NOT NULL COMMENT '城市名称',
    province VARCHAR(50) COMMENT '省份',
    tier VARCHAR(20) COMMENT '城市等级(一线/新一线/二线等)',
    avg_salary INT COMMENT '平均薪资(元/月)',
    job_count INT COMMENT '岗位数量',
    hot_industries JSON COMMENT '热门行业',
    living_cost INT COMMENT '生活成本(元/月)',
    development_potential VARCHAR(20) COMMENT '发展潜力(高/中/低)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tier (tier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='城市就业数据表';

-- 8. 专业评分表
CREATE TABLE IF NOT EXISTS major_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    major_id BIGINT NOT NULL COMMENT '专业ID',
    employment_score INT COMMENT '就业得分(0-100)',
    salary_score INT COMMENT '薪资得分(0-100)',
    development_score INT COMMENT '发展前景得分(0-100)',
    stability_score INT COMMENT '稳定性得分(0-100)',
    total_score INT COMMENT '综合得分(0-100)',
    recommendation_level VARCHAR(20) COMMENT '推荐等级(强烈推荐/推荐/谨慎/不推荐)',
    year INT COMMENT '评分年份',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_major_year (major_id, year),
    INDEX idx_total_score (total_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业评分表';

-- 9. 职业测评结果表
CREATE TABLE IF NOT EXISTS career_assessment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '用户ID',
    assessment_type VARCHAR(50) COMMENT '测评类型(霍兰德/MBTI/多元智能)',
    result JSON COMMENT '测评结果',
    recommended_careers JSON COMMENT '推荐职业列表',
    recommended_majors JSON COMMENT '推荐专业列表',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职业测评结果表';

-- 10. 志愿推荐记录表
CREATE TABLE IF NOT EXISTS recommendation_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '用户ID',
    score INT COMMENT '高考分数',
    province VARCHAR(50) COMMENT '省份',
    subject_type VARCHAR(20) COMMENT '科类(理科/文科/物理类等)',
    career_preference JSON COMMENT '职业偏好',
    location_preference JSON COMMENT '地域偏好',
    recommendations JSON COMMENT '推荐结果(冲稳保)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='志愿推荐记录表';

-- 插入示例数据

-- 行业数据示例
INSERT INTO industry (name, category, avg_salary, growth_rate, description, trend, hot_cities) VALUES
('互联网IT', '技术', 15000, 12.5, '包括软件开发、人工智能、大数据等领域', '上升', '["北京", "上海", "深圳", "杭州"]'),
('金融', '金融', 12000, 5.2, '包括银行、证券、保险、投资等', '稳定', '["北京", "上海", "深圳", "广州"]'),
('医疗健康', '医疗', 10000, 8.3, '包括临床医疗、医药研发、医疗器械等', '上升', '["北京", "上海", "广州", "成都"]'),
('教育培训', '教育', 8000, 6.5, '包括K12教育、高等教育、职业培训等', '稳定', '["北京", "上海", "广州", "成都"]'),
('制造业', '制造', 7000, 3.2, '包括汽车、电子、机械等传统制造业', '稳定', '["上海", "深圳", "苏州", "东莞"]');

-- 升学路径数据示例
INSERT INTO enrollment_path (name, type, description, requirements, advantages, disadvantages, suitable_students) VALUES
('强基计划', '特殊招生', '选拔培养有志于服务国家重大战略需求且综合素质优秀或基础学科拔尖的学生', '高考成绩优异，对基础学科有浓厚兴趣', '本硕博衔接培养，优质教育资源，国家重点支持', '专业选择受限，主要为基础学科，不能转专业', '成绩优异，对数学、物理、化学、生物、历史、哲学等基础学科有浓厚兴趣的学生'),
('综合评价', '特殊招生', '高考成绩+校测成绩+学业水平测试成绩综合评价录取', '高考成绩良好，综合素质强', '多一次录取机会，看重综合素质', '需要参加校测，准备时间紧张', '综合素质强，有特长或竞赛经历的学生'),
('专升本', '升学途径', '专科毕业后通过考试升入本科院校', '专科在读或毕业生', '获得本科学历，提升就业竞争力', '需要额外2年时间，竞争激烈', '有上进心，希望提升学历的专科生');

-- 城市就业数据示例
INSERT INTO city_employment (city, province, tier, avg_salary, living_cost, hot_industries, development_potential) VALUES
('北京', '北京', '一线', 12000, 6000, '["互联网", "金融", "教育"]', '高'),
('上海', '上海', '一线', 11500, 5500, '["金融", "互联网", "制造"]', '高'),
('深圳', '广东', '一线', 11000, 5000, '["互联网", "金融", "电子"]', '高'),
('杭州', '浙江', '新一线', 10000, 4000, '["互联网", "电商", "金融"]', '高'),
('成都', '四川', '新一线', 8000, 3000, '["互联网", "游戏", "电子"]', '中');
