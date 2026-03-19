#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
一键同步所有数据：排名 + 分数线
"""

import pymysql
import logging
from datetime import datetime

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)

# 数据库配置
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'root',
    'database': 'volunteer_exam',
    'charset': 'utf8mb4'
}

# 2024年软科中国大学排名（前100所）
RANKINGS_2024 = {
    '清华大学': 1, '北京大学': 2, '浙江大学': 3, '上海交通大学': 4, '复旦大学': 5,
    '南京大学': 6, '中国科学技术大学': 7, '华中科技大学': 8, '武汉大学': 9, '西安交通大学': 10,
    '哈尔滨工业大学': 11, '中山大学': 12, '北京航空航天大学': 13, '四川大学': 14, '同济大学': 15,
    '东南大学': 16, '北京理工大学': 17, '华南理工大学': 18, '天津大学': 19, '南开大学': 20,
    '山东大学': 21, '厦门大学': 22, '西北工业大学': 23, '中南大学': 24, '大连理工大学': 25,
    '吉林大学': 26, '湖南大学': 27, '重庆大学': 28, '电子科技大学': 29, '兰州大学': 30,
    '北京师范大学': 31, '中国人民大学': 32, '东北大学': 33, '华东师范大学': 34, '中国农业大学': 35,
    '北京科技大学': 36, '西安电子科技大学': 37, '华东理工大学': 38, '苏州大学': 39, '南京航空航天大学': 40,
    '南京理工大学': 41, '武汉理工大学': 42, '西南大学': 43, '暨南大学': 44, '北京交通大学': 45,
    '华中师范大学': 46, '河海大学': 47, '南京师范大学': 48, '郑州大学': 49, '西南交通大学': 50,
    '华中农业大学': 51, '中国海洋大学': 52, '哈尔滨工程大学': 53, '北京邮电大学': 54, '合肥工业大学': 55,
    '南京农业大学': 56, '西北大学': 57, '湖南师范大学': 58, '上海大学': 59, '北京化工大学': 60,
    '云南大学': 61, '深圳大学': 62, '华南师范大学': 63, '陕西师范大学': 64, '东华大学': 65,
    '中国石油大学': 66, '江南大学': 67, '福州大学': 68, '北京工业大学': 69, '宁波大学': 70,
    '中国地质大学': 71, '燕山大学': 72, '扬州大学': 73, '首都医科大学': 74, '昆明理工大学': 75,
    '南昌大学': 76, '中国矿业大学': 77, '华南农业大学': 78, '上海理工大学': 79, '浙江工业大学': 80,
    '太原理工大学': 81, '广西大学': 82, '河北大学': 83, '浙江师范大学': 84, '杭州电子科技大学': 85,
    '安徽大学': 86, '北京林业大学': 87, '长安大学': 88, '河南大学': 89, '辽宁大学': 90,
    '贵州大学': 91, '新疆大学': 92, '内蒙古大学': 93, '海南大学': 94, '石河子大学': 95,
    '宁夏大学': 96, '青海大学': 97, '西藏大学': 98, '延边大学': 99, '广州大学': 100
}

# 2023年部分院校录取分数线（示例数据）
ADMISSION_SCORES_2023 = [
    # 北京地区
    {'university': '清华大学', 'province': '北京', 'batch': '本科一批', 'min_score': 688, 'avg_score': 693, 'max_score': 698},
    {'university': '北京大学', 'province': '北京', 'batch': '本科一批', 'min_score': 687, 'avg_score': 692, 'max_score': 697},
    {'university': '中国人民大学', 'province': '北京', 'batch': '本科一批', 'min_score': 670, 'avg_score': 675, 'max_score': 680},
    {'university': '北京航空航天大学', 'province': '北京', 'batch': '本科一批', 'min_score': 665, 'avg_score': 670, 'max_score': 675},
    {'university': '北京理工大学', 'province': '北京', 'batch': '本科一批', 'min_score': 660, 'avg_score': 665, 'max_score': 670},
    {'university': '北京师范大学', 'province': '北京', 'batch': '本科一批', 'min_score': 655, 'avg_score': 660, 'max_score': 665},
    
    # 上海地区
    {'university': '复旦大学', 'province': '上海', 'batch': '本科一批', 'min_score': 610, 'avg_score': 615, 'max_score': 620},
    {'university': '上海交通大学', 'province': '上海', 'batch': '本科一批', 'min_score': 608, 'avg_score': 613, 'max_score': 618},
    {'university': '同济大学', 'province': '上海', 'batch': '本科一批', 'min_score': 595, 'avg_score': 600, 'max_score': 605},
    
    # 浙江地区
    {'university': '浙江大学', 'province': '浙江', 'batch': '本科一批', 'min_score': 665, 'avg_score': 670, 'max_score': 675},
    
    # 江苏地区
    {'university': '南京大学', 'province': '江苏', 'batch': '本科一批', 'min_score': 650, 'avg_score': 655, 'max_score': 660},
    
    # 广东地区
    {'university': '中山大学', 'province': '广东', 'batch': '本科一批', 'min_score': 630, 'avg_score': 635, 'max_score': 640},
    {'university': '华南理工大学', 'province': '广东', 'batch': '本科一批', 'min_score': 620, 'avg_score': 625, 'max_score': 630},
    
    # 湖北地区
    {'university': '武汉大学', 'province': '湖北', 'batch': '本科一批', 'min_score': 640, 'avg_score': 645, 'max_score': 650},
    {'university': '华中科技大学', 'province': '湖北', 'batch': '本科一批', 'min_score': 638, 'avg_score': 643, 'max_score': 648},
    
    # 四川地区
    {'university': '四川大学', 'province': '四川', 'batch': '本科一批', 'min_score': 625, 'avg_score': 630, 'max_score': 635},
    {'university': '电子科技大学', 'province': '四川', 'batch': '本科一批', 'min_score': 630, 'avg_score': 635, 'max_score': 640},
    
    # 陕西地区
    {'university': '西安交通大学', 'province': '陕西', 'batch': '本科一批', 'min_score': 635, 'avg_score': 640, 'max_score': 645},
    {'university': '西北工业大学', 'province': '陕西', 'batch': '本科一批', 'min_score': 625, 'avg_score': 630, 'max_score': 635},
    
    # 山东地区
    {'university': '山东大学', 'province': '山东', 'batch': '本科一批', 'min_score': 620, 'avg_score': 625, 'max_score': 630},
]

def update_rankings():
    """更新院校排名"""
    conn = None
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        logging.info("=" * 60)
        logging.info("开始更新院校排名")
        logging.info("=" * 60)
        
        success_count = 0
        not_found = []
        
        for university_name, ranking in RANKINGS_2024.items():
            # 查找院校ID
            cursor.execute(
                "SELECT id FROM university WHERE name = %s",
                (university_name,)
            )
            result = cursor.fetchone()
            
            if result:
                university_id = result[0]
                # 更新排名
                cursor.execute(
                    "UPDATE university SET ranking = %s WHERE id = %s",
                    (ranking, university_id)
                )
                success_count += 1
                logging.info(f"✓ 更新排名: {university_name} -> 第{ranking}名")
            else:
                not_found.append(university_name)
                logging.warning(f"✗ 未找到院校: {university_name}")
        
        conn.commit()
        
        logging.info("=" * 60)
        logging.info(f"排名更新完成！")
        logging.info(f"成功: {success_count}/{len(RANKINGS_2024)}")
        if not_found:
            logging.info(f"未找到: {len(not_found)}所")
        logging.info("=" * 60)
        
        return success_count
        
    except Exception as e:
        logging.error(f"更新排名失败: {e}")
        if conn:
            conn.rollback()
        return 0
    finally:
        if conn:
            conn.close()

def update_admission_scores():
    """更新录取分数线"""
    conn = None
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        logging.info("=" * 60)
        logging.info("开始更新录取分数线")
        logging.info("=" * 60)
        
        success_count = 0
        
        for score_data in ADMISSION_SCORES_2023:
            # 查找院校ID
            cursor.execute(
                "SELECT id FROM university WHERE name = %s",
                (score_data['university'],)
            )
            result = cursor.fetchone()
            
            if result:
                university_id = result[0]
                
                # 检查是否已存在该记录
                cursor.execute(
                    """SELECT id FROM admission_record 
                       WHERE university_id = %s AND province = %s 
                       AND year = 2023 AND batch = %s""",
                    (university_id, score_data['province'], score_data['batch'])
                )
                existing = cursor.fetchone()
                
                if existing:
                    # 更新现有记录
                    cursor.execute(
                        """UPDATE admission_record 
                           SET min_score = %s, avg_score = %s, max_score = %s
                           WHERE id = %s""",
                        (score_data['min_score'], score_data['avg_score'], 
                         score_data['max_score'], existing[0])
                    )
                    logging.info(f"✓ 更新分数线: {score_data['university']} ({score_data['province']}) {score_data['min_score']}-{score_data['max_score']}")
                else:
                    # 插入新记录
                    cursor.execute(
                        """INSERT INTO admission_record 
                           (university_id, province, year, batch, min_score, avg_score, max_score)
                           VALUES (%s, %s, 2023, %s, %s, %s, %s)""",
                        (university_id, score_data['province'], score_data['batch'],
                         score_data['min_score'], score_data['avg_score'], score_data['max_score'])
                    )
                    logging.info(f"✓ 新增分数线: {score_data['university']} ({score_data['province']}) {score_data['min_score']}-{score_data['max_score']}")
                
                success_count += 1
            else:
                logging.warning(f"✗ 未找到院校: {score_data['university']}")
        
        conn.commit()
        
        logging.info("=" * 60)
        logging.info(f"分数线更新完成！")
        logging.info(f"成功: {success_count}/{len(ADMISSION_SCORES_2023)}")
        logging.info("=" * 60)
        
        return success_count
        
    except Exception as e:
        logging.error(f"更新分数线失败: {e}")
        if conn:
            conn.rollback()
        return 0
    finally:
        if conn:
            conn.close()

def main():
    """主函数"""
    logging.info("\n" + "=" * 60)
    logging.info("开始同步所有数据")
    logging.info(f"时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    logging.info("=" * 60 + "\n")
    
    # 1. 更新排名
    ranking_count = update_rankings()
    
    # 2. 更新分数线
    score_count = update_admission_scores()
    
    # 3. 总结
    logging.info("\n" + "=" * 60)
    logging.info("数据同步完成！")
    logging.info("=" * 60)
    logging.info(f"排名更新: {ranking_count}所院校")
    logging.info(f"分数线更新: {score_count}条记录")
    logging.info("=" * 60 + "\n")

if __name__ == '__main__':
    main()
