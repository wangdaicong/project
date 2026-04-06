-- 专业测评问题表
CREATE TABLE IF NOT EXISTS assessment_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    category VARCHAR(50) NOT NULL COMMENT '问题类别（兴趣、能力、性格、价值观）',
    question TEXT NOT NULL COMMENT '问题内容',
    question_type VARCHAR(20) DEFAULT 'single' COMMENT '问题类型（single单选/multiple多选）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业测评问题表';

-- 专业测评选项表
CREATE TABLE IF NOT EXISTS assessment_option (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    question_id BIGINT NOT NULL COMMENT '问题ID',
    option_text TEXT NOT NULL COMMENT '选项内容',
    score INT DEFAULT 0 COMMENT '选项分数',
    major_tags VARCHAR(200) COMMENT '关联专业标签（逗号分隔）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业测评选项表';

-- 专业测评记录表
CREATE TABLE IF NOT EXISTS assessment_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '用户ID',
    answers TEXT COMMENT '答案JSON',
    result_scores TEXT COMMENT '各类别得分JSON',
    recommended_majors TEXT COMMENT '推荐专业JSON',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业测评记录表';

-- 插入测评问题（兴趣类）
INSERT INTO assessment_question (category, question, question_type, sort_order) VALUES
('兴趣', '你对以下哪类活动最感兴趣？', 'single', 1),
('兴趣', '你更喜欢哪种学习方式？', 'single', 2),
('兴趣', '你对哪个领域最感兴趣？', 'single', 3),
('兴趣', '你更倾向于从事什么类型的工作？', 'single', 4),
('兴趣', '你喜欢以下哪种活动？', 'single', 5);

-- 插入测评问题（能力类）
INSERT INTO assessment_question (category, question, question_type, sort_order) VALUES
('能力', '你认为自己在哪方面能力最强？', 'single', 6),
('能力', '你更擅长哪种思维方式？', 'single', 7),
('能力', '你在团队中通常扮演什么角色？', 'single', 8),
('能力', '你更擅长处理哪类问题？', 'single', 9),
('能力', '你的优势是什么？', 'single', 10);

-- 插入测评问题（性格类）
INSERT INTO assessment_question (category, question, question_type, sort_order) VALUES
('性格', '你更倾向于哪种工作环境？', 'single', 11),
('性格', '面对挑战时，你通常会？', 'single', 12),
('性格', '你更喜欢哪种工作节奏？', 'single', 13),
('性格', '你的性格特点是？', 'single', 14),
('性格', '你更适合哪种工作方式？', 'single', 15);

-- 插入测评问题（价值观类）
INSERT INTO assessment_question (category, question, question_type, sort_order) VALUES
('价值观', '你最看重工作的哪个方面？', 'single', 16),
('价值观', '你希望通过工作实现什么？', 'single', 17),
('价值观', '你认为什么最重要？', 'single', 18),
('价值观', '你的职业目标是？', 'single', 19),
('价值观', '你更看重哪种回报？', 'single', 20);

-- 插入选项（兴趣类 - 问题1）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(1, '科学实验、技术研发', 10, '计算机,软件工程,电子信息,机械工程', 1),
(1, '艺术创作、设计表达', 10, '艺术设计,动画,广告学,建筑学', 2),
(1, '社会服务、帮助他人', 10, '医学,护理,社会工作,教育学', 3),
(1, '商业管理、市场营销', 10, '工商管理,市场营销,金融学,经济学', 4),
(1, '文字写作、语言表达', 10, '新闻学,汉语言文学,外语,法学', 5);

-- 插入选项（兴趣类 - 问题2）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(2, '动手实践、实验操作', 10, '机械工程,材料科学,化学工程,生物工程', 1),
(2, '理论研究、深入思考', 10, '数学,物理,哲学,理论物理', 2),
(2, '团队协作、交流讨论', 10, '管理学,人力资源,社会学,心理学', 3),
(2, '独立探索、自主学习', 10, '计算机,软件工程,数据科学,人工智能', 4),
(2, '案例分析、实际应用', 10, '法学,会计学,临床医学,工程管理', 5);

-- 插入选项（兴趣类 - 问题3）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(3, '科技与工程', 10, '计算机,电子信息,自动化,通信工程', 1),
(3, '医学与健康', 10, '临床医学,口腔医学,药学,护理学', 2),
(3, '经济与金融', 10, '金融学,经济学,会计学,财务管理', 3),
(3, '文化与艺术', 10, '艺术设计,音乐,戏剧,美术', 4),
(3, '教育与社会', 10, '教育学,社会学,心理学,法学', 5);

-- 插入选项（兴趣类 - 问题4）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(4, '技术研发、产品开发', 10, '软件工程,电子信息,机械设计,材料工程', 1),
(4, '创意设计、艺术创作', 10, '视觉传达,工业设计,动画,广告学', 2),
(4, '医疗服务、健康管理', 10, '临床医学,护理学,公共卫生,康复治疗', 3),
(4, '商业运营、市场拓展', 10, '市场营销,工商管理,电子商务,国际贸易', 4),
(4, '教育培训、咨询服务', 10, '教育学,心理咨询,人力资源,社会工作', 5);

-- 插入选项（兴趣类 - 问题5）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(5, '编程开发、数据分析', 10, '计算机,数据科学,人工智能,软件工程', 1),
(5, '绘画设计、视频制作', 10, '艺术设计,动画,数字媒体,影视制作', 2),
(5, '阅读写作、语言学习', 10, '汉语言文学,新闻学,外语,翻译', 3),
(5, '运动健身、户外探险', 10, '体育教育,运动康复,旅游管理,地理科学', 4),
(5, '社交活动、志愿服务', 10, '社会工作,公共事业管理,行政管理,政治学', 5);

-- 插入选项（能力类 - 问题6）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(6, '逻辑思维、数学计算', 10, '数学,计算机,统计学,金融工程', 1),
(6, '语言表达、文字写作', 10, '新闻学,汉语言文学,法学,外语', 2),
(6, '空间想象、艺术审美', 10, '建筑学,艺术设计,工业设计,城市规划', 3),
(6, '人际交往、组织协调', 10, '工商管理,人力资源,市场营销,行政管理', 4),
(6, '动手操作、实践能力', 10, '机械工程,材料科学,化学工程,生物工程', 5);

-- 插入选项（能力类 - 问题7）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(7, '抽象思维、理论推导', 10, '数学,物理,哲学,理论物理', 1),
(7, '形象思维、创意联想', 10, '艺术设计,广告学,动画,影视制作', 2),
(7, '逻辑思维、系统分析', 10, '计算机,软件工程,信息管理,系统工程', 3),
(7, '批判思维、辩证分析', 10, '法学,哲学,政治学,社会学', 4),
(7, '实用思维、问题解决', 10, '工程管理,工商管理,会计学,临床医学', 5);

-- 插入选项（能力类 - 问题8）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(8, '领导者、决策者', 10, '工商管理,行政管理,公共事业管理,政治学', 1),
(8, '执行者、实施者', 10, '工程管理,项目管理,会计学,审计学', 2),
(8, '创新者、设计者', 10, '产品设计,工业设计,软件工程,建筑学', 3),
(8, '协调者、沟通者', 10, '人力资源,市场营销,公共关系,社会工作', 4),
(8, '分析者、研究者', 10, '数据科学,统计学,经济学,心理学', 5);

-- 插入选项（能力类 - 问题9）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(9, '技术问题、工程难题', 10, '计算机,电子信息,机械工程,自动化', 1),
(9, '人际问题、沟通障碍', 10, '心理学,社会工作,人力资源,教育学', 2),
(9, '商业问题、市场挑战', 10, '市场营销,工商管理,金融学,电子商务', 3),
(9, '创意问题、设计需求', 10, '艺术设计,广告学,建筑学,工业设计', 4),
(9, '理论问题、学术研究', 10, '数学,物理,哲学,基础医学', 5);

-- 插入选项（能力类 - 问题10）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(10, '学习能力强、适应性好', 10, '计算机,软件工程,数据科学,人工智能', 1),
(10, '沟通能力强、亲和力好', 10, '市场营销,人力资源,教育学,社会工作', 2),
(10, '创新能力强、想象力丰富', 10, '艺术设计,产品设计,广告学,建筑学', 3),
(10, '执行能力强、责任心强', 10, '会计学,审计学,工程管理,临床医学', 4),
(10, '分析能力强、逻辑性好', 10, '数学,统计学,经济学,金融工程', 5);

-- 插入选项（性格类 - 问题11）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(11, '安静独立、专注研究', 10, '计算机,数学,物理,生物科学', 1),
(11, '活跃开放、团队协作', 10, '市场营销,人力资源,工商管理,公共关系', 2),
(11, '创意自由、灵活多变', 10, '艺术设计,广告学,动画,影视制作', 3),
(11, '规范有序、流程清晰', 10, '会计学,审计学,法学,行政管理', 4),
(11, '挑战竞争、目标导向', 10, '金融学,投资学,市场营销,企业管理', 5);

-- 插入选项（性格类 - 问题12）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(12, '冷静分析、理性应对', 10, '计算机,数学,经济学,工程管理', 1),
(12, '积极乐观、勇于尝试', 10, '市场营销,创业管理,电子商务,新闻学', 2),
(12, '谨慎稳妥、步步为营', 10, '会计学,审计学,法学,医学', 3),
(12, '创新突破、另辟蹊径', 10, '产品设计,软件工程,艺术设计,建筑学', 4),
(12, '寻求帮助、团队协作', 10, '人力资源,社会工作,教育学,心理学', 5);

-- 插入选项（性格类 - 问题13）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(13, '快节奏、高强度', 10, '金融学,市场营销,新闻学,急诊医学', 1),
(13, '稳定规律、按部就班', 10, '会计学,教育学,行政管理,图书馆学', 2),
(13, '灵活自由、弹性工作', 10, '艺术设计,自由撰稿,软件开发,咨询服务', 3),
(13, '项目制、阶段性目标', 10, '工程管理,建筑学,软件工程,科研', 4),
(13, '长期稳定、持续深耕', 10, '医学,教育学,基础科学,档案学', 5);

-- 插入选项（性格类 - 问题14）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(14, '内向沉稳、善于思考', 10, '数学,物理,计算机,哲学', 1),
(14, '外向活泼、善于交际', 10, '市场营销,公共关系,旅游管理,播音主持', 2),
(14, '细心谨慎、注重细节', 10, '会计学,审计学,医学检验,质量管理', 3),
(14, '大胆创新、敢于冒险', 10, '创业管理,投资学,艺术设计,新媒体', 4),
(14, '温和友善、乐于助人', 10, '护理学,社会工作,教育学,心理咨询', 5);

-- 插入选项（性格类 - 问题15）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(15, '独立工作、自主决策', 10, '软件工程,艺术设计,科研,自由职业', 1),
(15, '团队合作、协同作战', 10, '工程管理,人力资源,市场营销,项目管理', 2),
(15, '领导管理、统筹规划', 10, '工商管理,行政管理,公共事业管理,企业管理', 3),
(15, '辅助支持、服务他人', 10, '护理学,社会工作,行政助理,客户服务', 4),
(15, '研究分析、提供建议', 10, '咨询,数据分析,经济学,战略规划', 5);

-- 插入选项（价值观类 - 问题16）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(16, '高收入、物质回报', 10, '金融学,投资学,计算机,医学', 1),
(16, '社会地位、职业声望', 10, '法学,医学,建筑学,外交学', 2),
(16, '工作稳定、保障性强', 10, '教育学,公务员,会计学,行政管理', 3),
(16, '个人成长、能力提升', 10, '咨询,培训,软件工程,管理学', 4),
(16, '社会价值、帮助他人', 10, '医学,社会工作,教育学,公益事业', 5);

-- 插入选项（价值观类 - 问题17）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(17, '财富自由、经济独立', 10, '金融学,投资学,创业管理,电子商务', 1),
(17, '专业成就、行业认可', 10, '医学,法学,建筑学,科研', 2),
(17, '工作生活平衡', 10, '教育学,行政管理,图书馆学,档案学', 3),
(17, '创新创造、实现想法', 10, '产品设计,软件工程,艺术设计,创业', 4),
(17, '服务社会、造福他人', 10, '医学,教育学,社会工作,公共卫生', 5);

-- 插入选项（价值观类 - 问题18）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(18, '个人发展和成功', 10, '工商管理,金融学,法学,计算机', 1),
(18, '家庭幸福和稳定', 10, '教育学,护理学,会计学,行政管理', 2),
(18, '社会责任和贡献', 10, '医学,社会工作,环境科学,公共管理', 3),
(18, '自我实现和价值', 10, '艺术设计,科研,哲学,文学创作', 4),
(18, '人际关系和影响力', 10, '市场营销,公共关系,政治学,外交学', 5);

-- 插入选项（价值观类 - 问题19）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(19, '成为行业专家', 10, '医学,法学,建筑学,工程学', 1),
(19, '创办自己的企业', 10, '创业管理,工商管理,市场营销,电子商务', 2),
(19, '从事科研教学', 10, '基础科学,教育学,科研,高校教师', 3),
(19, '进入政府机关', 10, '行政管理,公共管理,法学,政治学', 4),
(19, '追求艺术梦想', 10, '艺术设计,音乐,戏剧,文学创作', 5);

-- 插入选项（价值观类 - 问题20）
INSERT INTO assessment_option (question_id, option_text, score, major_tags, sort_order) VALUES
(20, '经济收益、物质奖励', 10, '金融学,投资学,计算机,医学', 1),
(20, '精神满足、成就感', 10, '教育学,科研,艺术,社会工作', 2),
(20, '社会认可、他人尊重', 10, '法学,医学,建筑学,管理学', 3),
(20, '自由时间、工作弹性', 10, '自由职业,艺术设计,咨询,远程工作', 4),
(20, '学习机会、成长空间', 10, '咨询,培训,科技,互联网', 5);
