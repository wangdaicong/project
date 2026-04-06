-- ========================================
-- 扩展 university 表以支持完整的Excel数据
-- ========================================

USE volunteer_exam;

-- 添加Excel中的所有字段（如果已存在会报错，可忽略）
ALTER TABLE university 
    ADD COLUMN school_code VARCHAR(50) DEFAULT NULL COMMENT '学校标识码' AFTER name,
    ADD COLUMN supervisor VARCHAR(100) DEFAULT NULL COMMENT '主管部门' AFTER school_code,
    ADD COLUMN location VARCHAR(200) DEFAULT NULL COMMENT '所在地（完整）' AFTER city,
    ADD COLUMN remarks VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER level,
    ADD COLUMN school_nature VARCHAR(20) DEFAULT NULL COMMENT '办学性质（公办/民办）' AFTER type,
    ADD COLUMN rating DECIMAL(3,1) DEFAULT NULL COMMENT '评分' AFTER ranking,
    ADD COLUMN logo_path VARCHAR(500) DEFAULT NULL COMMENT 'Logo本地路径' AFTER logo_url;

-- 创建索引以优化搜索（如果已存在会报错，可忽略）
CREATE INDEX idx_school_code ON university(school_code);
CREATE INDEX idx_supervisor ON university(supervisor);
CREATE INDEX idx_school_nature ON university(school_nature);
CREATE INDEX idx_985_211 ON university(is_985, is_211, is_double_first_class);

-- 显示修改后的表结构
SHOW FULL COLUMNS FROM university;

SELECT '表结构扩展完成！' as message;
