-- 历年分数线表
CREATE TABLE IF NOT EXISTS score_line (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    university_id BIGINT NOT NULL COMMENT '院校ID',
    university_name VARCHAR(100) NOT NULL COMMENT '院校名称',
    province VARCHAR(50) NOT NULL COMMENT '省份',
    year INT NOT NULL COMMENT '年份',
    batch VARCHAR(50) COMMENT '批次（本科一批、本科二批等）',
    category VARCHAR(20) COMMENT '科类（理科、文科）',
    min_score INT COMMENT '最低分',
    avg_score INT COMMENT '平均分',
    max_score INT COMMENT '最高分',
    min_rank INT COMMENT '最低位次',
    enrollment_count INT COMMENT '招生人数',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_university_id (university_id),
    INDEX idx_province (province),
    INDEX idx_year (year),
    INDEX idx_university_year (university_id, year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历年分数线表';

-- 插入示例数据（清华大学）
INSERT INTO score_line (university_id, university_name, province, year, batch, category, min_score, avg_score, max_score, min_rank, enrollment_count) VALUES
(1, '清华大学', '北京', 2023, '本科一批', '理科', 685, 692, 705, 150, 120),
(1, '清华大学', '北京', 2022, '本科一批', '理科', 680, 688, 700, 180, 115),
(1, '清华大学', '北京', 2021, '本科一批', '理科', 678, 686, 698, 200, 110),
(1, '清华大学', '北京', 2020, '本科一批', '理科', 675, 683, 695, 220, 105),
(1, '清华大学', '北京', 2019, '本科一批', '理科', 672, 680, 692, 250, 100),
(1, '清华大学', '北京', 2023, '本科一批', '文科', 665, 672, 685, 50, 30),
(1, '清华大学', '北京', 2022, '本科一批', '文科', 660, 668, 680, 60, 28),
(1, '清华大学', '北京', 2021, '本科一批', '文科', 658, 665, 678, 70, 25),
(1, '清华大学', '北京', 2020, '本科一批', '文科', 655, 662, 675, 80, 22),
(1, '清华大学', '北京', 2019, '本科一批', '文科', 652, 659, 672, 90, 20);

-- 插入示例数据（北京大学）
INSERT INTO score_line (university_id, university_name, province, year, batch, category, min_score, avg_score, max_score, min_rank, enrollment_count) VALUES
(2, '北京大学', '北京', 2023, '本科一批', '理科', 683, 690, 703, 160, 115),
(2, '北京大学', '北京', 2022, '本科一批', '理科', 678, 686, 698, 190, 110),
(2, '北京大学', '北京', 2021, '本科一批', '理科', 676, 684, 696, 210, 105),
(2, '北京大学', '北京', 2020, '本科一批', '理科', 673, 681, 693, 230, 100),
(2, '北京大学', '北京', 2019, '本科一批', '理科', 670, 678, 690, 260, 95),
(2, '北京大学', '北京', 2023, '本科一批', '文科', 663, 670, 683, 55, 28),
(2, '北京大学', '北京', 2022, '本科一批', '文科', 658, 666, 678, 65, 26),
(2, '北京大学', '北京', 2021, '本科一批', '文科', 656, 663, 676, 75, 23),
(2, '北京大学', '北京', 2020, '本科一批', '文科', 653, 660, 673, 85, 20),
(2, '北京大学', '北京', 2019, '本科一批', '文科', 650, 657, 670, 95, 18);

-- 插入示例数据（浙江大学）
INSERT INTO score_line (university_id, university_name, province, year, batch, category, min_score, avg_score, max_score, min_rank, enrollment_count) VALUES
(3, '浙江大学', '浙江', 2023, '本科一批', '理科', 665, 672, 685, 800, 200),
(3, '浙江大学', '浙江', 2022, '本科一批', '理科', 660, 668, 680, 850, 195),
(3, '浙江大学', '浙江', 2021, '本科一批', '理科', 658, 665, 678, 900, 190),
(3, '浙江大学', '浙江', 2020, '本科一批', '理科', 655, 662, 675, 950, 185),
(3, '浙江大学', '浙江', 2019, '本科一批', '理科', 652, 659, 672, 1000, 180),
(3, '浙江大学', '浙江', 2023, '本科一批', '文科', 645, 652, 665, 300, 50),
(3, '浙江大学', '浙江', 2022, '本科一批', '文科', 640, 648, 660, 320, 48),
(3, '浙江大学', '浙江', 2021, '本科一批', '文科', 638, 645, 658, 340, 45),
(3, '浙江大学', '浙江', 2020, '本科一批', '文科', 635, 642, 655, 360, 42),
(3, '浙江大学', '浙江', 2019, '本科一批', '文科', 632, 639, 652, 380, 40);
