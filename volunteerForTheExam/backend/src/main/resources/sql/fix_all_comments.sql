-- ========================================
-- 修复所有数据库表和字段注释
-- 基于实际表结构
-- ========================================

USE volunteer_exam;

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ========================================
-- 1. university 表
-- ========================================
ALTER TABLE university COMMENT = '高校信息表';

ALTER TABLE university 
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    MODIFY COLUMN name VARCHAR(100) NOT NULL COMMENT '学校名称',
    MODIFY COLUMN province VARCHAR(50) DEFAULT NULL COMMENT '省份',
    MODIFY COLUMN city VARCHAR(50) DEFAULT NULL COMMENT '城市',
    MODIFY COLUMN level VARCHAR(20) DEFAULT NULL COMMENT '办学层次',
    MODIFY COLUMN type VARCHAR(20) DEFAULT NULL COMMENT '学校类型',
    MODIFY COLUMN tags VARCHAR(200) DEFAULT NULL COMMENT '标签',
    MODIFY COLUMN introduction TEXT COMMENT '学校简介',
    MODIFY COLUMN address VARCHAR(200) DEFAULT NULL COMMENT '详细地址',
    MODIFY COLUMN website VARCHAR(200) DEFAULT NULL COMMENT '官方网站',
    MODIFY COLUMN phone VARCHAR(50) DEFAULT NULL COMMENT '联系电话',
    MODIFY COLUMN min_score INT DEFAULT NULL COMMENT '最低录取分数',
    MODIFY COLUMN max_score INT DEFAULT NULL COMMENT '最高录取分数',
    MODIFY COLUMN logo_url VARCHAR(200) DEFAULT NULL COMMENT 'Logo图片URL',
    MODIFY COLUMN ranking INT DEFAULT NULL COMMENT '排名',
    MODIFY COLUMN features TEXT COMMENT '特色专业',
    MODIFY COLUMN is_985 TINYINT(1) DEFAULT 0 COMMENT '是否985高校',
    MODIFY COLUMN is_211 TINYINT(1) DEFAULT 0 COMMENT '是否211高校',
    MODIFY COLUMN is_double_first_class TINYINT(1) DEFAULT 0 COMMENT '是否双一流高校',
    MODIFY COLUMN created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- ========================================
-- 2. major_info 表
-- ========================================
ALTER TABLE major_info COMMENT = '专业信息表';

ALTER TABLE major_info
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    MODIFY COLUMN name VARCHAR(100) NOT NULL COMMENT '专业名称',
    MODIFY COLUMN code VARCHAR(20) DEFAULT NULL COMMENT '专业代码',
    MODIFY COLUMN category VARCHAR(50) DEFAULT NULL COMMENT '专业类别',
    MODIFY COLUMN degree VARCHAR(20) DEFAULT NULL COMMENT '学位类型',
    MODIFY COLUMN years INT DEFAULT 4 COMMENT '学制（年）',
    MODIFY COLUMN introduction TEXT COMMENT '专业介绍',
    MODIFY COLUMN main_courses TEXT COMMENT '主要课程',
    MODIFY COLUMN employment_direction TEXT COMMENT '就业方向',
    MODIFY COLUMN created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- ========================================
-- 3. score_line 表
-- ========================================
ALTER TABLE score_line COMMENT = '历年分数线表';

ALTER TABLE score_line
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    MODIFY COLUMN university_id BIGINT NOT NULL COMMENT '高校ID',
    MODIFY COLUMN university_name VARCHAR(100) NOT NULL COMMENT '高校名称',
    MODIFY COLUMN province VARCHAR(50) NOT NULL COMMENT '省份',
    MODIFY COLUMN year INT NOT NULL COMMENT '年份',
    MODIFY COLUMN batch VARCHAR(50) DEFAULT NULL COMMENT '批次',
    MODIFY COLUMN category VARCHAR(20) DEFAULT NULL COMMENT '科类',
    MODIFY COLUMN min_score INT DEFAULT NULL COMMENT '最低分',
    MODIFY COLUMN avg_score INT DEFAULT NULL COMMENT '平均分',
    MODIFY COLUMN max_score INT DEFAULT NULL COMMENT '最高分',
    MODIFY COLUMN min_rank INT DEFAULT NULL COMMENT '最低位次',
    MODIFY COLUMN enrollment_count INT DEFAULT NULL COMMENT '招生人数',
    MODIFY COLUMN created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- ========================================
-- 4. employment_data 表
-- ========================================
ALTER TABLE employment_data COMMENT = '就业数据表';

ALTER TABLE employment_data
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    MODIFY COLUMN major_id BIGINT NOT NULL COMMENT '专业ID',
    MODIFY COLUMN year INT NOT NULL COMMENT '年份',
    MODIFY COLUMN employment_rate DECIMAL(5,2) DEFAULT NULL COMMENT '就业率',
    MODIFY COLUMN average_salary INT DEFAULT NULL COMMENT '平均薪资',
    MODIFY COLUMN top_industries TEXT COMMENT '主要就业行业',
    MODIFY COLUMN top_positions TEXT COMMENT '主要就业岗位',
    MODIFY COLUMN created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- ========================================
-- 5. user 表
-- ========================================
ALTER TABLE user COMMENT = '用户信息表';

ALTER TABLE user
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    MODIFY COLUMN openid VARCHAR(100) NOT NULL COMMENT '微信OpenID',
    MODIFY COLUMN unionid VARCHAR(100) DEFAULT NULL COMMENT '微信UnionID',
    MODIFY COLUMN nickname VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    MODIFY COLUMN avatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    MODIFY COLUMN gender TINYINT DEFAULT 0 COMMENT '性别（0未知1男2女）',
    MODIFY COLUMN phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    MODIFY COLUMN province VARCHAR(50) DEFAULT NULL COMMENT '省份',
    MODIFY COLUMN city VARCHAR(50) DEFAULT NULL COMMENT '城市',
    MODIFY COLUMN score INT DEFAULT NULL COMMENT '高考分数',
    MODIFY COLUMN year INT DEFAULT NULL COMMENT '高考年份',
    MODIFY COLUMN subject_type VARCHAR(20) DEFAULT NULL COMMENT '科目类型',
    MODIFY COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    MODIFY COLUMN last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    MODIFY COLUMN status TINYINT DEFAULT 1 COMMENT '状态（0禁用1正常）';

-- ========================================
-- 6. user_favorite 表
-- ========================================
ALTER TABLE user_favorite COMMENT = '用户收藏表';

ALTER TABLE user_favorite
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    MODIFY COLUMN user_id BIGINT NOT NULL COMMENT '用户ID',
    MODIFY COLUMN type VARCHAR(20) NOT NULL COMMENT '收藏类型（university/major）',
    MODIFY COLUMN target_id BIGINT NOT NULL COMMENT '目标ID（高校ID或专业ID）',
    MODIFY COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

-- ========================================
-- 7. admission_record 表
-- ========================================
ALTER TABLE admission_record COMMENT = '录取记录表';

ALTER TABLE admission_record
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    MODIFY COLUMN university_id BIGINT NOT NULL COMMENT '高校ID',
    MODIFY COLUMN major_id BIGINT DEFAULT NULL COMMENT '专业ID',
    MODIFY COLUMN province VARCHAR(50) NOT NULL COMMENT '省份',
    MODIFY COLUMN year INT NOT NULL COMMENT '年份',
    MODIFY COLUMN min_score INT DEFAULT NULL COMMENT '最低分',
    MODIFY COLUMN avg_score INT DEFAULT NULL COMMENT '平均分',
    MODIFY COLUMN max_score INT DEFAULT NULL COMMENT '最高分',
    MODIFY COLUMN enrollment_number INT DEFAULT NULL COMMENT '招生人数',
    MODIFY COLUMN batch VARCHAR(20) DEFAULT NULL COMMENT '批次';

-- ========================================
-- 8. assessment_record 表
-- ========================================
ALTER TABLE assessment_record COMMENT = '测评记录表';

ALTER TABLE assessment_record
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    MODIFY COLUMN user_id BIGINT DEFAULT NULL COMMENT '用户ID',
    MODIFY COLUMN answers TEXT COMMENT '答案JSON',
    MODIFY COLUMN result_scores TEXT COMMENT '结果分数JSON',
    MODIFY COLUMN recommended_majors TEXT COMMENT '推荐专业JSON',
    MODIFY COLUMN created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

-- ========================================
-- 9. university_major 表
-- ========================================
ALTER TABLE university_major COMMENT = '高校专业关联表';

ALTER TABLE university_major
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    MODIFY COLUMN university_id BIGINT NOT NULL COMMENT '高校ID',
    MODIFY COLUMN major_id BIGINT NOT NULL COMMENT '专业ID',
    MODIFY COLUMN enrollment_plan INT DEFAULT NULL COMMENT '招生计划',
    MODIFY COLUMN tuition_fee INT DEFAULT NULL COMMENT '学费（元/年）',
    MODIFY COLUMN created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- ========================================
-- 完成提示
-- ========================================
SELECT '========================================' as '';
SELECT '表注释修复完成！' as message;
SELECT '所有核心表和字段的注释已更新为中文' as result;
SELECT '========================================' as '';

-- 验证修复结果
SELECT 
    TABLE_NAME as '表名',
    TABLE_COMMENT as '表注释'
FROM 
    information_schema.TABLES 
WHERE 
    TABLE_SCHEMA = 'volunteer_exam' 
    AND TABLE_NAME IN ('university', 'major_info', 'score_line', 'employment_data', 
                       'user', 'user_favorite', 'admission_record', 'assessment_record', 
                       'university_major')
ORDER BY TABLE_NAME;
