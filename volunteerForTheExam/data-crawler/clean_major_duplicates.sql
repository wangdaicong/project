-- 清除major表重复数据，保留每个专业名称的第一条记录

-- 1. 创建临时表保存唯一记录
CREATE TEMPORARY TABLE major_unique AS
SELECT MIN(id) as id, name, category, degree_type, duration
FROM major
GROUP BY name;

-- 2. 删除所有记录
DELETE FROM major;

-- 3. 从临时表恢复唯一记录
INSERT INTO major (id, name, category, degree_type, duration)
SELECT id, name, category, degree_type, duration
FROM major_unique;

-- 4. 查看结果
SELECT COUNT(*) as total_after_cleanup FROM major;
SELECT '清除完成！' as message;
