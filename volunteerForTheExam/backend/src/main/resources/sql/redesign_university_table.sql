-- 重新设计university表结构
-- 基于"全国普通高等学校名单.xls"的实际列名
-- 执行时间: 2026-03-22

-- 1. 备份现有数据（如果需要）
-- CREATE TABLE university_backup AS SELECT * FROM university;

-- 2. 删除所有旧字段（保留5个基础字段）
ALTER TABLE university DROP COLUMN name;
ALTER TABLE university DROP COLUMN school_code;
ALTER TABLE university DROP COLUMN supervisor;
ALTER TABLE university DROP COLUMN province;
ALTER TABLE university DROP COLUMN city;
ALTER TABLE university DROP COLUMN location;
ALTER TABLE university DROP COLUMN level;
ALTER TABLE university DROP COLUMN remarks;
ALTER TABLE university DROP COLUMN type;
ALTER TABLE university DROP COLUMN school_nature;
ALTER TABLE university DROP COLUMN tags;
ALTER TABLE university DROP COLUMN introduction;
ALTER TABLE university DROP COLUMN address;
ALTER TABLE university DROP COLUMN website;
ALTER TABLE university DROP COLUMN phone;
ALTER TABLE university DROP COLUMN min_score;
ALTER TABLE university DROP COLUMN max_score;
ALTER TABLE university DROP COLUMN logo_url;
ALTER TABLE university DROP COLUMN logo_path;
ALTER TABLE university DROP COLUMN ranking;
ALTER TABLE university DROP COLUMN rating;
ALTER TABLE university DROP COLUMN features;

-- 3. 根据Excel列名添加新字段
-- Excel列结构：序号 | 学校名称 | 学校标识码 | 主管部门 | 所在地 | 办学层次 | 备注

-- 学校名称（必填）- Excel列：学校名称
ALTER TABLE university ADD COLUMN school_name VARCHAR(100) NOT NULL COMMENT 'Excel列：学校名称' AFTER id;

-- 学校标识码 - Excel列：学校标识码
ALTER TABLE university ADD COLUMN school_id_code VARCHAR(50) DEFAULT NULL COMMENT 'Excel列：学校标识码（10位数字）' AFTER school_name;

-- 主管部门 - Excel列：主管部门
ALTER TABLE university ADD COLUMN supervisor VARCHAR(100) DEFAULT NULL COMMENT 'Excel列：主管部门（如教育部、省教育厅等）' AFTER school_id_code;

-- 所在地 - Excel列：所在地
ALTER TABLE university ADD COLUMN location VARCHAR(100) DEFAULT NULL COMMENT 'Excel列：所在地（省份+城市）' AFTER supervisor;

-- 办学层次 - Excel列：办学层次
ALTER TABLE university ADD COLUMN school_level VARCHAR(20) DEFAULT NULL COMMENT 'Excel列：办学层次（本科/专科）' AFTER location;

-- 备注 - Excel列：备注
ALTER TABLE university ADD COLUMN remarks VARCHAR(500) DEFAULT NULL COMMENT 'Excel列：备注' AFTER school_level;

-- 4. 添加索引
-- 学校名称唯一索引
ALTER TABLE university ADD UNIQUE KEY uk_school_name (school_name);

-- 学校标识码索引
ALTER TABLE university ADD KEY idx_school_id_code (school_id_code);

-- 主管部门索引
ALTER TABLE university ADD KEY idx_supervisor (supervisor);

-- 所在地索引
ALTER TABLE university ADD KEY idx_location (location);

-- 办学层次索引
ALTER TABLE university ADD KEY idx_school_level (school_level);

-- 985/211/双一流组合索引
ALTER TABLE university ADD KEY idx_985_211_double (is_985, is_211, is_double_first_class);

-- 5. 验证表结构
SHOW CREATE TABLE university;

-- 6. 查看字段列表
DESC university;
