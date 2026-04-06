-- 张雪峰式功能快速部署SQL
-- 只扩展现有表，最小化改动

USE volunteer_exam;

-- 1. 扩展university表（7个字段）
-- 使用存储过程检查字段是否存在
SET @dbname = DATABASE();
SET @tablename = 'university';

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'historical_affiliation') > 0,
  'SELECT 1',
  'ALTER TABLE university ADD COLUMN historical_affiliation VARCHAR(100) COMMENT ''历史隶属关系'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'industry_recognition') > 0,
  'SELECT 1',
  'ALTER TABLE university ADD COLUMN industry_recognition VARCHAR(200) COMMENT ''行业认可度标签'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'has_doctoral_program') > 0,
  'SELECT 1',
  'ALTER TABLE university ADD COLUMN has_doctoral_program TINYINT(1) DEFAULT 0 COMMENT ''是否有博士点'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'has_master_program') > 0,
  'SELECT 1',
  'ALTER TABLE university ADD COLUMN has_master_program TINYINT(1) DEFAULT 0 COMMENT ''是否有硕士点'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'zhangxuefeng_rating') > 0,
  'SELECT 1',
  'ALTER TABLE university ADD COLUMN zhangxuefeng_rating VARCHAR(50) COMMENT ''张雪峰评级'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'employment_advantage') > 0,
  'SELECT 1',
  'ALTER TABLE university ADD COLUMN employment_advantage TEXT COMMENT ''就业优势'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'postgraduate_difficulty') > 0,
  'SELECT 1',
  'ALTER TABLE university ADD COLUMN postgraduate_difficulty VARCHAR(20) COMMENT ''考研难度'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 2. 扩展major表（8个字段）
SET @tablename = 'major';

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'zhangxuefeng_tags') > 0,
  'SELECT 1',
  'ALTER TABLE major ADD COLUMN zhangxuefeng_tags VARCHAR(200) COMMENT ''张雪峰式标签'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'civil_service_advantage') > 0,
  'SELECT 1',
  'ALTER TABLE major ADD COLUMN civil_service_advantage VARCHAR(100) COMMENT ''考公优势'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'postgraduate_necessity') > 0,
  'SELECT 1',
  'ALTER TABLE major ADD COLUMN postgraduate_necessity VARCHAR(50) COMMENT ''考研必要性'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'employment_reality') > 0,
  'SELECT 1',
  'ALTER TABLE major ADD COLUMN employment_reality TEXT COMMENT ''就业现实'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'common_misconceptions') > 0,
  'SELECT 1',
  'ALTER TABLE major ADD COLUMN common_misconceptions TEXT COMMENT ''常见误解'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'salary_level') > 0,
  'SELECT 1',
  'ALTER TABLE major ADD COLUMN salary_level VARCHAR(50) COMMENT ''薪资水平'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'work_intensity') > 0,
  'SELECT 1',
  'ALTER TABLE major ADD COLUMN work_intensity VARCHAR(50) COMMENT ''工作强度'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'gender_preference') > 0,
  'SELECT 1',
  'ALTER TABLE major ADD COLUMN gender_preference VARCHAR(50) COMMENT ''性别倾向'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 3. 新建知识库表（用于AI问答缓存）
CREATE TABLE IF NOT EXISTS zhangxuefeng_knowledge (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  question TEXT NOT NULL COMMENT '问题',
  answer TEXT NOT NULL COMMENT '答案',
  keywords VARCHAR(200) COMMENT '关键词',
  usage_count INT DEFAULT 0 COMMENT '使用次数',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_keywords (keywords(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='张雪峰知识库';

-- 4. 插入示例数据（院校）
UPDATE university SET 
  historical_affiliation = '原电力部直属',
  industry_recognition = '电力系统认可度极高',
  has_doctoral_program = 1,
  has_master_program = 1,
  zhangxuefeng_rating = '电力系统王牌',
  employment_advantage = '进国家电网成功率90%以上，电力行业认可度最高，电气类专业毕业生首选单位'
WHERE school_name LIKE '%华北电力%';

UPDATE university SET 
  historical_affiliation = '原铁道部直属',
  industry_recognition = '铁路系统认可度高',
  has_doctoral_program = 1,
  has_master_program = 1,
  zhangxuefeng_rating = '铁路系统首选',
  employment_advantage = '进铁路局、中铁、中铁建优势明显，交通运输类专业就业率高'
WHERE school_name LIKE '%北京交通%';

-- 5. 插入示例数据（专业）
UPDATE major SET 
  zhangxuefeng_tags = '高薪但累,大厂首选,不需要考研',
  civil_service_advantage = '岗位中等，竞争激烈',
  postgraduate_necessity = '不需要考研',
  employment_reality = '本科就业很好，应届15-30万，但工作强度大，996常态。大厂、互联网公司首选专业。',
  common_misconceptions = '很多人以为学计算机就是修电脑，实际是做软件开发，和修电脑没关系',
  salary_level = '高薪',
  work_intensity = '高强度',
  gender_preference = '适合男生'
WHERE major_name LIKE '%计算机%';

UPDATE major SET 
  zhangxuefeng_tags = '考公王者,岗位多,竞争小',
  civil_service_advantage = '岗位数量多，专业限制少，考公务员优势明显',
  postgraduate_necessity = '不需要考研',
  employment_reality = '就业面广，但起薪不高，适合求稳定。会计事务所、企业财务部门、银行都需要。',
  salary_level = '中等',
  work_intensity = '一般',
  gender_preference = '适合女生'
WHERE major_name LIKE '%会计%';

-- 6. 插入知识库示例数据
INSERT INTO zhangxuefeng_knowledge (question, answer, keywords) VALUES
('理科女生，分数一本线左右，不想学计算机，推荐什么专业？',
 '推荐会计学、医学技术类（如医学影像技术、医学检验技术）、师范类专业。理由：1. 会计学考公岗位多，就业稳定；2. 医学技术类不用当医生，工作环境好，适合女生；3. 师范类稳定，有寒暑假。避免：生物、化学等需要读研的纯理科专业。',
 '理科女生,一本线,专业推荐'),

('双非二本想考985，应该怎么选学校？',
 '建议：1. 避开热门城市的热门专业（如北京上海的金融、计算机）；2. 选择B区的985（如兰州大学、西北农林科技大学），竞争相对小；3. 选择冷门专业，先进985再说；4. 关注调剂友好的学校。核心逻辑：求稳>冲名校，能上岸才是王道。',
 '双非,考研,985,择校'),

('计算机专业毕业，不想进大厂996，有什么选择？',
 '推荐路径：1. 考公务员（网信办、公安系统技术岗）；2. 进国企（三大运营商、国家电网信息部门）；3. 进银行科技部门；4. 考事业单位（高校网络中心）。这些单位稳定，不加班，待遇也不错。',
 '计算机,不想996,稳定工作');

SELECT '数据库扩展完成！' AS status;
