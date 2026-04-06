-- 清空university表数据并添加唯一约束
-- 执行时间: 2026-03-22

-- 1. 清空university表所有数据
TRUNCATE TABLE university;

-- 2. 添加唯一约束，防止重复数据
-- 使用学校名称作为唯一约束（一个学校名称应该是唯一的）
ALTER TABLE university ADD UNIQUE KEY `uk_name` (`name`);

-- 3. 如果需要使用学校标识码作为唯一约束（如果有的话）
-- ALTER TABLE university ADD UNIQUE KEY `uk_school_code` (`school_code`);

-- 验证表结构
SHOW CREATE TABLE university;

-- 验证数据已清空
SELECT COUNT(*) as total_count FROM university;
