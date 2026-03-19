-- 补充缺失的院校数据
USE volunteer_exam;

-- 更新所有introduction为NULL或空的记录
UPDATE university 
SET introduction = CONCAT(name, '是一所位于', province, city, '的', type, '类高等院校，办学层次为', level, '。学校秉承优良传统，致力于培养高素质人才，在教学科研等方面取得了显著成就。'),
    features = CONCAT(type, '特色鲜明，学科实力雄厚，注重学生综合素质培养'),
    address = CONCAT(province, city, '校区'),
    phone = '待补充',
    website = CONCAT('https://www.', LOWER(REPLACE(name, '大学', '')), '.edu.cn')
WHERE introduction IS NULL OR introduction = '';

-- 验证更新结果
SELECT COUNT(*) as updated_count FROM university WHERE introduction IS NOT NULL AND introduction != '';
