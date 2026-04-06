-- 张雪峰式功能数据库扩展方案
-- 核心思想：将书中的方法论转化为可查询、可计算的数据结构

-- 1. 扩展university表 - 添加"张雪峰式"标签字段
ALTER TABLE university ADD COLUMN IF NOT EXISTS `historical_affiliation` VARCHAR(100) COMMENT '历史隶属关系（如：原电力部直属）';
ALTER TABLE university ADD COLUMN IF NOT EXISTS `industry_recognition` VARCHAR(200) COMMENT '行业认可度标签（如：电力系统认可、铁路系统认可）';
ALTER TABLE university ADD COLUMN IF NOT EXISTS `has_doctoral_program` TINYINT(1) DEFAULT 0 COMMENT '是否有博士点';
ALTER TABLE university ADD COLUMN IF NOT EXISTS `has_master_program` TINYINT(1) DEFAULT 0 COMMENT '是否有硕士点';
ALTER TABLE university ADD COLUMN IF NOT EXISTS `zhangxuefeng_rating` VARCHAR(50) COMMENT '张雪峰评级（如：性价比之王、考公神校）';
ALTER TABLE university ADD COLUMN IF NOT EXISTS `employment_advantage` TEXT COMMENT '就业优势（对应书中的就业分析）';
ALTER TABLE university ADD COLUMN IF NOT EXISTS `postgraduate_difficulty` VARCHAR(20) COMMENT '考研难度（保研率、考研率）';

-- 2. 扩展major表 - 添加专业避坑和就业路径
ALTER TABLE major ADD COLUMN IF NOT EXISTS `zhangxuefeng_tags` VARCHAR(200) COMMENT '张雪峰式标签（如：考公王者、高薪但累、名字好听被误解）';
ALTER TABLE major ADD COLUMN IF NOT EXISTS `civil_service_advantage` VARCHAR(100) COMMENT '考公优势（如：岗位多、竞争小）';
ALTER TABLE major ADD COLUMN IF NOT EXISTS `postgraduate_necessity` VARCHAR(50) COMMENT '考研必要性（必须考研、建议考研、不需要考研）';
ALTER TABLE major ADD COLUMN IF NOT EXISTS `employment_reality` TEXT COMMENT '就业现实（对应书中的真实就业情况）';
ALTER TABLE major ADD COLUMN IF NOT EXISTS `common_misconceptions` TEXT COMMENT '常见误解（如：名字好听但实际不好就业）';
ALTER TABLE major ADD COLUMN IF NOT EXISTS `salary_level` VARCHAR(50) COMMENT '薪资水平（高薪、中等、偏低）';
ALTER TABLE major ADD COLUMN IF NOT EXISTS `work_intensity` VARCHAR(50) COMMENT '工作强度（轻松、一般、高强度）';
ALTER TABLE major ADD COLUMN IF NOT EXISTS `gender_preference` VARCHAR(50) COMMENT '性别倾向（适合男生、适合女生、无限制）';

-- 3. 新建表：考研决策数据表
CREATE TABLE IF NOT EXISTS `postgraduate_guide` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `university_id` BIGINT NOT NULL COMMENT '院校ID',
  `major_category` VARCHAR(50) COMMENT '专业类别',
  `region_type` VARCHAR(20) COMMENT '地区类型（A区、B区）',
  `is_dry_region` TINYINT(1) DEFAULT 0 COMMENT '是否旱区（压分严重）',
  `score_suppression_level` VARCHAR(20) COMMENT '压分程度（严重、一般、宽松）',
  `retest_discrimination` TINYINT(1) DEFAULT 0 COMMENT '是否存在复试歧视',
  `acceptance_rate` DECIMAL(5,2) COMMENT '录取率',
  `recommended_for_985` TINYINT(1) DEFAULT 0 COMMENT '是否推荐985考生',
  `recommended_for_211` TINYINT(1) DEFAULT 0 COMMENT '是否推荐211考生',
  `recommended_for_double_non` TINYINT(1) DEFAULT 0 COMMENT '是否推荐双非考生',
  `zhangxuefeng_advice` TEXT COMMENT '张雪峰建议',
  PRIMARY KEY (`id`),
  INDEX `idx_university` (`university_id`),
  INDEX `idx_region` (`region_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考研择校指南表';

-- 4. 新建表：铁饭碗路径规划表
CREATE TABLE IF NOT EXISTS `stable_career_path` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `target_organization` VARCHAR(100) NOT NULL COMMENT '目标单位（如：国家电网、中国烟草）',
  `organization_type` VARCHAR(50) COMMENT '单位类型（国企、央企、事业单位）',
  `required_majors` TEXT COMMENT '需要的专业（JSON数组）',
  `required_education` VARCHAR(50) COMMENT '学历要求（专科、本科、硕士）',
  `preferred_universities` TEXT COMMENT '优先录取院校（JSON数组，含原因）',
  `recruitment_scale` VARCHAR(50) COMMENT '招聘规模（大量、适中、少量）',
  `entry_difficulty` VARCHAR(20) COMMENT '进入难度（容易、中等、困难）',
  `salary_range` VARCHAR(50) COMMENT '薪资范围',
  `career_stability` VARCHAR(20) COMMENT '职业稳定性（极高、高、中等）',
  `zhangxuefeng_recommendation` TEXT COMMENT '张雪峰推荐理由',
  PRIMARY KEY (`id`),
  INDEX `idx_organization` (`target_organization`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='铁饭碗路径规划表';

-- 5. 新建表：专科升本路径表
CREATE TABLE IF NOT EXISTS `vocational_upgrade_path` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `province` VARCHAR(50) NOT NULL COMMENT '省份',
  `vocational_college_id` BIGINT COMMENT '专科院校ID',
  `target_university_id` BIGINT COMMENT '对口本科院校ID',
  `major_category` VARCHAR(50) COMMENT '专业类别',
  `upgrade_rate` DECIMAL(5,2) COMMENT '升本率',
  `exam_difficulty` VARCHAR(20) COMMENT '考试难度（简单、中等、困难）',
  `policy_notes` TEXT COMMENT '政策说明',
  `zhangxuefeng_tips` TEXT COMMENT '张雪峰建议',
  PRIMARY KEY (`id`),
  INDEX `idx_province` (`province`),
  INDEX `idx_vocational` (`vocational_college_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专科升本路径表';

-- 6. 新建表：AI知识库表（用于智能问答）
CREATE TABLE IF NOT EXISTS `zhangxuefeng_knowledge` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category` VARCHAR(50) NOT NULL COMMENT '分类（志愿填报、考研、专科、就业）',
  `question` TEXT NOT NULL COMMENT '问题',
  `answer` TEXT NOT NULL COMMENT '答案（基于书籍内容提炼）',
  `keywords` VARCHAR(200) COMMENT '关键词（用于检索）',
  `book_source` VARCHAR(100) COMMENT '来源书籍',
  `relevance_score` INT DEFAULT 0 COMMENT '相关度评分',
  `usage_count` INT DEFAULT 0 COMMENT '使用次数',
  PRIMARY KEY (`id`),
  INDEX `idx_category` (`category`),
  FULLTEXT INDEX `idx_keywords` (`keywords`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='张雪峰知识库表';

-- 7. 新建表：决策树配置表（用于问答流程）
CREATE TABLE IF NOT EXISTS `decision_tree` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tree_type` VARCHAR(50) NOT NULL COMMENT '决策树类型（考研择校、专业选择、就业规划）',
  `node_id` VARCHAR(50) NOT NULL COMMENT '节点ID',
  `parent_node_id` VARCHAR(50) COMMENT '父节点ID',
  `question` TEXT NOT NULL COMMENT '问题',
  `options` TEXT COMMENT '选项（JSON数组）',
  `result_action` TEXT COMMENT '结果动作（如：推荐院校列表）',
  `zhangxuefeng_logic` TEXT COMMENT '背后的张雪峰逻辑',
  PRIMARY KEY (`id`),
  INDEX `idx_tree_type` (`tree_type`),
  INDEX `idx_node` (`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='决策树配置表';

-- 8. 示例数据插入（铁饭碗路径）
INSERT INTO `stable_career_path` 
(`target_organization`, `organization_type`, `required_majors`, `required_education`, 
 `preferred_universities`, `recruitment_scale`, `entry_difficulty`, `salary_range`, 
 `career_stability`, `zhangxuefeng_recommendation`)
VALUES
('国家电网', '国企', 
 '["电气工程及其自动化","电力系统及其自动化","高电压与绝缘技术"]', 
 '本科及以上',
 '[{"name":"华北电力大学","reason":"原电力部直属，电网认可度最高"},{"name":"东北电力大学","reason":"性价比高，电网认可"},{"name":"三峡大学","reason":"原电力部直属，二本分数进电网"}]',
 '大量', '中等', '8-15万/年', '极高',
 '电力系统是最稳定的铁饭碗之一，优先选择原电力部直属院校，即使是专科也有机会进入县级电网。关键是专业对口，电气类专业是王牌。'),

('中国烟草', '国企',
 '["机械设计制造及其自动化","电气工程及其自动化","食品科学与工程","工商管理"]',
 '本科及以上',
 '[{"name":"郑州轻工业大学","reason":"烟草专业全国第一"},{"name":"河南农业大学","reason":"烟草学科强"},{"name":"云南农业大学","reason":"烟草产区优势"}]',
 '适中', '困难', '15-30万/年', '极高',
 '烟草系统待遇极好但竞争激烈，专业对口很重要。郑州轻工业大学的烟草专业是王牌，进烟草的成功率最高。'),

('中国铁路', '国企',
 '["交通运输","铁道工程","车辆工程","通信工程"]',
 '专科及以上',
 '[{"name":"西南交通大学","reason":"铁路黄埔军校"},{"name":"北京交通大学","reason":"铁路系统认可度高"},{"name":"石家庄铁道大学","reason":"原铁道部直属，性价比高"}]',
 '大量', '容易', '6-12万/年', '高',
 '铁路系统招聘量大，专科也有很多机会。关键是选对学校，原铁道部直属院校认可度最高，即使现在改名了也保留铁路基因。');

-- 9. 示例数据插入（专业标签）
-- 注意：这里只是示例，实际需要批量更新所有专业
UPDATE major SET 
  zhangxuefeng_tags = '考公王者,岗位多,竞争小',
  civil_service_advantage = '岗位数量多，专业限制少',
  postgraduate_necessity = '不需要考研',
  employment_reality = '就业面广，但起薪不高，适合求稳定',
  salary_level = '中等',
  work_intensity = '一般',
  gender_preference = '无限制'
WHERE name LIKE '%会计%';

UPDATE major SET 
  zhangxuefeng_tags = '高薪但累,需要考研,就业压力大',
  civil_service_advantage = '岗位少',
  postgraduate_necessity = '必须考研',
  employment_reality = '本科就业难，需要读研甚至读博，但读完后待遇好',
  common_misconceptions = '很多人以为学了就能当医生，实际上临床医学需要至少5年本科+3年规培',
  salary_level = '高薪',
  work_intensity = '高强度',
  gender_preference = '无限制'
WHERE name LIKE '%临床医学%';

UPDATE major SET 
  zhangxuefeng_tags = '名字好听被误解,实际是工科,就业一般',
  civil_service_advantage = '岗位少',
  postgraduate_necessity = '建议考研',
  employment_reality = '听起来高大上，实际是偏工科的专业，就业不如想象中好',
  common_misconceptions = '很多人以为是管理岗位，实际上是技术岗位，需要学大量工科知识',
  salary_level = '中等',
  work_intensity = '一般',
  gender_preference = '适合男生'
WHERE name LIKE '%信息管理与信息系统%';

-- 10. 示例数据插入（知识库Q&A）
INSERT INTO `zhangxuefeng_knowledge` 
(`category`, `question`, `answer`, `keywords`, `book_source`)
VALUES
('志愿填报', '理科女生，分数一本线左右，不想学计算机，推荐什么专业？',
 '推荐会计学、医学技术类（如医学影像技术、医学检验技术）、师范类专业。理由：1. 会计学考公岗位多，就业稳定；2. 医学技术类不用当医生，工作环境好，适合女生；3. 师范类稳定，有寒暑假。避免：生物、化学等需要读研的纯理科专业。',
 '理科女生,一本线,专业推荐,不学计算机', '《选择比努力更重要》'),

('考研', '双非二本，想考985，应该怎么选学校？',
 '建议：1. 避开热门城市的热门专业（如北京上海的金融、计算机）；2. 选择B区的985（如兰州大学、西北农林科技大学），竞争相对小；3. 选择冷门专业，先进985再说；4. 关注调剂友好的学校。核心逻辑：求稳>冲名校，能上岸才是王道。',
 '双非,考研,985,择校策略', '《方向比努力更重要》'),

('专科', '专科生想进国企，有哪些路径？',
 '最佳路径：1. 电力类专科→县级电网（选原电力部直属专科，如郑州电力高等专科学校）；2. 铁路类专科→铁路局（选原铁道部直属专科）；3. 专升本→考公务员（先升本提高学历）。关键：专业对口+学校背景>学历层次。',
 '专科,国企,就业路径,铁饭碗', '《手把手教你报专科》'),

('就业', '计算机专业毕业，不想进大厂996，有什么选择？',
 '推荐路径：1. 考公务员（网信办、公安系统技术岗）；2. 进国企（三大运营商、国家电网信息部门）；3. 进银行科技部门；4. 考事业单位（高校网络中心）。这些单位稳定，不加班，待遇也不错。',
 '计算机,不想996,稳定工作,考公', '《稳就业》');
