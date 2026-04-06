-- 扩展university表，添加院校详细信息字段
-- 包括：官网、校址、电话、微信、微博、百家号、视频号、Logo等

-- 添加官网地址
ALTER TABLE university ADD COLUMN website VARCHAR(255) DEFAULT NULL COMMENT '官方网站';

-- 添加详细地址
ALTER TABLE university ADD COLUMN address VARCHAR(255) DEFAULT NULL COMMENT '详细地址';

-- 添加联系电话
ALTER TABLE university ADD COLUMN phone VARCHAR(50) DEFAULT NULL COMMENT '联系电话';

-- 添加Logo URL
ALTER TABLE university ADD COLUMN logo_url VARCHAR(500) DEFAULT NULL COMMENT '院校Logo地址';

-- 添加社交媒体账号
ALTER TABLE university ADD COLUMN wechat_name VARCHAR(100) DEFAULT NULL COMMENT '微信公众号名称';
ALTER TABLE university ADD COLUMN wechat_id VARCHAR(100) DEFAULT NULL COMMENT '微信公众号ID';

ALTER TABLE university ADD COLUMN weibo_name VARCHAR(100) DEFAULT NULL COMMENT '微博账号名称';
ALTER TABLE university ADD COLUMN weibo_id VARCHAR(100) DEFAULT NULL COMMENT '微博账号ID';

ALTER TABLE university ADD COLUMN baijia_name VARCHAR(100) DEFAULT NULL COMMENT '百家号名称';
ALTER TABLE university ADD COLUMN baijia_id VARCHAR(100) DEFAULT NULL COMMENT '百家号ID';

ALTER TABLE university ADD COLUMN video_name VARCHAR(100) DEFAULT NULL COMMENT '视频号名称';
ALTER TABLE university ADD COLUMN video_id VARCHAR(100) DEFAULT NULL COMMENT '视频号ID';

-- 添加院校简介
ALTER TABLE university ADD COLUMN introduction TEXT DEFAULT NULL COMMENT '院校简介';

-- 添加索引
CREATE INDEX idx_website ON university(website);
CREATE INDEX idx_wechat_id ON university(wechat_id);
CREATE INDEX idx_weibo_id ON university(weibo_id);

-- 查看表结构
DESC university;
