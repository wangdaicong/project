-- 清除重复数据脚本

USE volunteer_exam;

-- 1. 查找重复的院校（按名称）
SELECT name, COUNT(*) as count 
FROM university 
GROUP BY name 
HAVING count > 1;

-- 2. 删除重复的院校（保留ID最小的）
DELETE u1 FROM university u1
INNER JOIN university u2 
WHERE u1.id > u2.id AND u1.name = u2.name;

-- 3. 查找重复的专业（同一院校的同名专业）
SELECT university_id, name, COUNT(*) as count 
FROM major 
GROUP BY university_id, name 
HAVING count > 1;

-- 4. 删除重复的专业（保留ID最小的）
DELETE m1 FROM major m1
INNER JOIN major m2 
WHERE m1.id > m2.id 
  AND m1.university_id = m2.university_id 
  AND m1.name = m2.name;

-- 5. 验证清理结果
SELECT '院校总数' as item, COUNT(*) as count FROM university
UNION ALL
SELECT '专业总数', COUNT(*) FROM major;

-- 6. 查看是否还有重复
SELECT '重复院校' as item, COUNT(*) as count FROM (
    SELECT name FROM university GROUP BY name HAVING COUNT(*) > 1
) as dup_universities
UNION ALL
SELECT '重复专业', COUNT(*) FROM (
    SELECT university_id, name FROM major GROUP BY university_id, name HAVING COUNT(*) > 1
) as dup_majors;
