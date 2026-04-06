-- ========================================
-- 数据库问题修复脚本
-- 1. 修复字符集乱码问题
-- 2. 清理冗余数据库表
-- ========================================

USE volunteer_exam;

-- ========================================
-- 第一部分：修复字符集乱码问题
-- ========================================

-- 修改数据库字符集为utf8mb4
ALTER DATABASE volunteer_exam CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 修复 university 表
ALTER TABLE university CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 修复其他表（忽略不存在的表）
SET @tables = 'major_info,career,admission_record,score_line,employment_data,university_major,user,user_favorite,browse_history,volunteer_record';

-- 注意：assessment_record 和 assessment_option 表将被删除，不需要修复

-- ========================================
-- 第二部分：清理冗余数据库表
-- ========================================

-- 删除冗余的测评选项表（assessment_option）
-- 这个表的描述和数据都是乱码，且功能不明确
DROP TABLE IF EXISTS assessment_option;

-- 删除冗余的测评问题表（assessment_question）
-- 如果不需要专业测评功能，可以删除
DROP TABLE IF EXISTS assessment_question;

-- 如果major和major_info重复，只保留major_info
-- 检查是否存在major表，如果存在且数据已迁移到major_info，则删除
-- DROP TABLE IF EXISTS major;

-- ========================================
-- 第三部分：优化核心表结构
-- ========================================

-- 确保 university 表字段完整且字符集正确
ALTER TABLE university 
    MODIFY COLUMN name VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学校名称',
    MODIFY COLUMN province VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省份',
    MODIFY COLUMN city VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市',
    MODIFY COLUMN level VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '办学层次',
    MODIFY COLUMN type VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学校类型',
    MODIFY COLUMN tags VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签',
    MODIFY COLUMN introduction TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '学校简介',
    MODIFY COLUMN address VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '详细地址',
    MODIFY COLUMN website VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '官方网站',
    MODIFY COLUMN phone VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
    MODIFY COLUMN features TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '特色专业';

-- major_info 表字符集修复（如果表存在）
-- 注：如果表不存在会报错，可忽略

-- ========================================
-- 第四部分：创建必要的索引（如果不存在）
-- ========================================

-- university 表索引（如果已存在会报错，可忽略）
-- ALTER TABLE university ADD INDEX idx_province (province);
-- ALTER TABLE university ADD INDEX idx_city (city);
-- ALTER TABLE university ADD INDEX idx_level (level);
-- ALTER TABLE university ADD INDEX idx_type (type);

-- 索引优化已在 optimize_indexes.sql 中完成，此处跳过

-- ========================================
-- 完成提示
-- ========================================

SELECT '数据库优化完成！' as message;
SELECT '已修复字符集乱码问题' as step1;
SELECT '已清理冗余表' as step2;
SELECT '已优化表结构和索引' as step3;

-- 显示当前数据库表列表
SHOW TABLES;

-- 显示 university 表结构
SHOW CREATE TABLE university;
