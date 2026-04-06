-- 数据库索引优化SQL脚本
-- 用于提升常用查询的性能

-- ========================================
-- 1. 院校表索引优化
-- ========================================

-- 省份查询索引（已存在，检查）
-- CREATE INDEX idx_university_province ON university(province);

-- 层次查询索引（模糊查询优化）
CREATE INDEX idx_university_level ON university(level(50));

-- 类型查询索引
CREATE INDEX idx_university_type ON university(type(50));

-- 分数范围查询复合索引
CREATE INDEX idx_university_score_range ON university(min_score, max_score);

-- 排名查询索引
CREATE INDEX idx_university_ranking ON university(ranking);

-- 省份+层次复合索引（常用组合查询）
CREATE INDEX idx_university_province_level ON university(province, level(50));

-- ========================================
-- 2. 专业表索引优化
-- ========================================

-- 院校ID索引（已存在，检查）
-- CREATE INDEX idx_major_university_id ON major(university_id);

-- 专业类别索引
CREATE INDEX idx_major_category ON major(category);

-- 就业率索引（排序优化）
CREATE INDEX idx_major_employment_rate ON major(employment_rate DESC);

-- 院校ID+就业率复合索引
CREATE INDEX idx_major_university_employment ON major(university_id, employment_rate DESC);

-- 专业名称索引（搜索优化）
CREATE INDEX idx_major_name ON major(name(100));

-- ========================================
-- 3. 分数线表索引优化
-- ========================================

-- 院校ID+省份+科类复合索引（趋势查询）
CREATE INDEX idx_scoreline_query ON score_line(university_id, province, category, year DESC);

-- 省份+年份+科类复合索引（排名查询）
CREATE INDEX idx_scoreline_ranking ON score_line(province, year, category, min_score DESC);

-- 年份索引（年份列表查询）
CREATE INDEX idx_scoreline_year ON score_line(year DESC);

-- ========================================
-- 4. 用户收藏表索引优化
-- ========================================

-- 用户ID索引（已存在，检查）
-- CREATE INDEX idx_favorite_user_id ON user_favorite(user_id);

-- 院校ID索引
CREATE INDEX idx_favorite_university_id ON user_favorite(university_id);

-- 用户ID+院校ID唯一索引（防重复收藏）
CREATE UNIQUE INDEX idx_favorite_user_university ON user_favorite(user_id, university_id);

-- 创建时间索引（按时间排序）
CREATE INDEX idx_favorite_created_time ON user_favorite(created_time DESC);

-- ========================================
-- 5. 浏览历史表索引优化
-- ========================================

-- 用户ID+创建时间复合索引
CREATE INDEX idx_history_user_time ON user_history(user_id, created_time DESC);

-- 院校ID索引
CREATE INDEX idx_history_university_id ON user_history(university_id);

-- ========================================
-- 6. 测评记录表索引优化
-- ========================================

-- 用户ID+创建时间复合索引
CREATE INDEX idx_assessment_user_time ON assessment_record(user_id, created_time DESC);

-- ========================================
-- 7. 志愿填报表索引优化
-- ========================================

-- 用户ID+状态复合索引
CREATE INDEX idx_volunteer_user_status ON volunteer_application(user_id, status);

-- 创建时间索引
CREATE INDEX idx_volunteer_created_time ON volunteer_application(created_time DESC);

-- 志愿详情表：志愿填报ID索引（已存在）
-- CREATE INDEX idx_volunteer_detail_app_id ON volunteer_detail(application_id);

-- 志愿详情表：院校ID索引
CREATE INDEX idx_volunteer_detail_university ON volunteer_detail(university_id);

-- ========================================
-- 8. 就业信息表索引优化
-- ========================================

-- 专业就业表：专业ID索引
CREATE INDEX idx_major_employment_major_id ON major_employment(major_id);

-- 职业信息表：行业ID索引
CREATE INDEX idx_career_industry_id ON career(industry_id);

-- 城市就业表：城市索引
CREATE INDEX idx_city_employment_city ON city_employment(city);

-- ========================================
-- 查看索引创建结果
-- ========================================

-- 查看university表的所有索引
SHOW INDEX FROM university;

-- 查看major表的所有索引
SHOW INDEX FROM major;

-- 查看score_line表的所有索引
SHOW INDEX FROM score_line;
