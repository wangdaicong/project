"""
批量插入历年录取分数线数据
包含2021-2023年主要院校的录取分数线
"""
from api_helper import APIHelper
import logging
import requests

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 示例：2021-2023年部分院校录取分数线数据
# 格式：{university_id, year, province, batch, category, min_score, avg_score, max_score}
ADMISSION_SCORES_DATA = [
    # 清华大学 (ID=7)
    {"university_id": 7, "year": 2023, "province": "北京", "batch": "本科一批", "category": "理科", "min_score": 688, "avg_score": 695, "max_score": 703},
    {"university_id": 7, "year": 2023, "province": "北京", "batch": "本科一批", "category": "文科", "min_score": 678, "avg_score": 683, "max_score": 690},
    {"university_id": 7, "year": 2023, "province": "上海", "batch": "本科一批", "category": "综合", "min_score": 618, "avg_score": 625, "max_score": 632},
    {"university_id": 7, "year": 2023, "province": "广东", "batch": "本科一批", "category": "物理", "min_score": 685, "avg_score": 692, "max_score": 699},
    {"university_id": 7, "year": 2023, "province": "广东", "batch": "本科一批", "category": "历史", "min_score": 665, "avg_score": 670, "max_score": 675},
    
    {"university_id": 7, "year": 2022, "province": "北京", "batch": "本科一批", "category": "理科", "min_score": 685, "avg_score": 692, "max_score": 700},
    {"university_id": 7, "year": 2022, "province": "北京", "batch": "本科一批", "category": "文科", "min_score": 675, "avg_score": 680, "max_score": 687},
    {"university_id": 7, "year": 2021, "province": "北京", "batch": "本科一批", "category": "理科", "min_score": 682, "avg_score": 689, "max_score": 697},
    
    # 北京大学 (ID=8)
    {"university_id": 8, "year": 2023, "province": "北京", "batch": "本科一批", "category": "理科", "min_score": 687, "avg_score": 694, "max_score": 702},
    {"university_id": 8, "year": 2023, "province": "北京", "batch": "本科一批", "category": "文科", "min_score": 677, "avg_score": 682, "max_score": 689},
    {"university_id": 8, "year": 2023, "province": "上海", "batch": "本科一批", "category": "综合", "min_score": 617, "avg_score": 624, "max_score": 631},
    {"university_id": 8, "year": 2023, "province": "广东", "batch": "本科一批", "category": "物理", "min_score": 684, "avg_score": 691, "max_score": 698},
    
    {"university_id": 8, "year": 2022, "province": "北京", "batch": "本科一批", "category": "理科", "min_score": 684, "avg_score": 691, "max_score": 699},
    {"university_id": 8, "year": 2021, "province": "北京", "batch": "本科一批", "category": "理科", "min_score": 681, "avg_score": 688, "max_score": 696},
    
    # 复旦大学 (ID=9)
    {"university_id": 9, "year": 2023, "province": "上海", "batch": "本科一批", "category": "综合", "min_score": 610, "avg_score": 617, "max_score": 624},
    {"university_id": 9, "year": 2023, "province": "北京", "batch": "本科一批", "category": "理科", "min_score": 680, "avg_score": 687, "max_score": 695},
    {"university_id": 9, "year": 2023, "province": "广东", "batch": "本科一批", "category": "物理", "min_score": 678, "avg_score": 685, "max_score": 692},
    
    {"university_id": 9, "year": 2022, "province": "上海", "batch": "本科一批", "category": "综合", "min_score": 608, "avg_score": 615, "max_score": 622},
    {"university_id": 9, "year": 2021, "province": "上海", "batch": "本科一批", "category": "综合", "min_score": 605, "avg_score": 612, "max_score": 619},
    
    # 上海交通大学 (ID=10)
    {"university_id": 10, "year": 2023, "province": "上海", "batch": "本科一批", "category": "综合", "min_score": 612, "avg_score": 619, "max_score": 626},
    {"university_id": 10, "year": 2023, "province": "北京", "batch": "本科一批", "category": "理科", "min_score": 682, "avg_score": 689, "max_score": 697},
    {"university_id": 10, "year": 2023, "province": "广东", "batch": "本科一批", "category": "物理", "min_score": 680, "avg_score": 687, "max_score": 694},
    
    # 浙江大学 (ID=11)
    {"university_id": 11, "year": 2023, "province": "浙江", "batch": "本科一批", "category": "综合", "min_score": 665, "avg_score": 672, "max_score": 679},
    {"university_id": 11, "year": 2023, "province": "北京", "batch": "本科一批", "category": "理科", "min_score": 681, "avg_score": 688, "max_score": 696},
    {"university_id": 11, "year": 2023, "province": "广东", "batch": "本科一批", "category": "物理", "min_score": 679, "avg_score": 686, "max_score": 693},
    
    {"university_id": 11, "year": 2022, "province": "浙江", "batch": "本科一批", "category": "综合", "min_score": 663, "avg_score": 670, "max_score": 677},
    {"university_id": 11, "year": 2021, "province": "浙江", "batch": "本科一批", "category": "综合", "min_score": 660, "avg_score": 667, "max_score": 674},
    
    # 中国科学技术大学 (ID=12)
    {"university_id": 12, "year": 2023, "province": "安徽", "batch": "本科一批", "category": "理科", "min_score": 675, "avg_score": 682, "max_score": 690},
    {"university_id": 12, "year": 2023, "province": "北京", "batch": "本科一批", "category": "理科", "min_score": 679, "avg_score": 686, "max_score": 694},
    
    # 南京大学 (ID=13)
    {"university_id": 13, "year": 2023, "province": "江苏", "batch": "本科一批", "category": "物理", "min_score": 665, "avg_score": 672, "max_score": 679},
    {"university_id": 13, "year": 2023, "province": "江苏", "batch": "本科一批", "category": "历史", "min_score": 645, "avg_score": 650, "max_score": 655},
    {"university_id": 13, "year": 2023, "province": "北京", "batch": "本科一批", "category": "理科", "min_score": 678, "avg_score": 685, "max_score": 693},
    
    # 中国人民大学 (ID=14)
    {"university_id": 14, "year": 2023, "province": "北京", "batch": "本科一批", "category": "理科", "min_score": 675, "avg_score": 682, "max_score": 690},
    {"university_id": 14, "year": 2023, "province": "北京", "batch": "本科一批", "category": "文科", "min_score": 670, "avg_score": 675, "max_score": 682},
    
    # 北京航空航天大学 (ID=15)
    {"university_id": 15, "year": 2023, "province": "北京", "batch": "本科一批", "category": "理科", "min_score": 670, "avg_score": 677, "max_score": 685},
    {"university_id": 15, "year": 2023, "province": "广东", "batch": "本科一批", "category": "物理", "min_score": 668, "avg_score": 675, "max_score": 682},
    
    # 北京理工大学 (ID=16)
    {"university_id": 16, "year": 2023, "province": "北京", "batch": "本科一批", "category": "理科", "min_score": 668, "avg_score": 675, "max_score": 683},
    
    # 添加更多211院校的分数线
    # 北京邮电大学 (ID=37)
    {"university_id": 37, "year": 2023, "province": "北京", "batch": "本科一批", "category": "理科", "min_score": 650, "avg_score": 657, "max_score": 665},
    {"university_id": 37, "year": 2023, "province": "广东", "batch": "本科一批", "category": "物理", "min_score": 648, "avg_score": 655, "max_score": 662},
    
    # 中央财经大学 (ID=42)
    {"university_id": 42, "year": 2023, "province": "北京", "batch": "本科一批", "category": "理科", "min_score": 655, "avg_score": 662, "max_score": 670},
    {"university_id": 42, "year": 2023, "province": "北京", "batch": "本科一批", "category": "文科", "min_score": 650, "avg_score": 655, "max_score": 662},
    
    # 对外经济贸易大学 (ID=43)
    {"university_id": 43, "year": 2023, "province": "北京", "batch": "本科一批", "category": "理科", "min_score": 653, "avg_score": 660, "max_score": 668},
    {"university_id": 43, "year": 2023, "province": "北京", "batch": "本科一批", "category": "文科", "min_score": 648, "avg_score": 653, "max_score": 660},
    
    # 上海财经大学 (ID=59)
    {"university_id": 59, "year": 2023, "province": "上海", "batch": "本科一批", "category": "综合", "min_score": 590, "avg_score": 597, "max_score": 604},
    
    # 添加大专院校分数线（专科批次）
    # 深圳职业技术学院 (ID=116)
    {"university_id": 116, "year": 2023, "province": "广东", "batch": "专科批", "category": "物理", "min_score": 450, "avg_score": 470, "max_score": 490},
    {"university_id": 116, "year": 2023, "province": "广东", "batch": "专科批", "category": "历史", "min_score": 440, "avg_score": 460, "max_score": 480},
    
    # 北京电子科技职业学院 (ID=117)
    {"university_id": 117, "year": 2023, "province": "北京", "batch": "专科批", "category": "综合", "min_score": 380, "avg_score": 400, "max_score": 420},
    
    # 天津职业大学 (ID=118)
    {"university_id": 118, "year": 2023, "province": "天津", "batch": "专科批", "category": "综合", "min_score": 370, "avg_score": 390, "max_score": 410},
]


def insert_admission_score_via_api(api, score_data):
    """通过API插入分数线数据"""
    try:
        # 由于后端可能没有专门的分数线插入API，这里使用SQL直接插入
        # 实际应该创建对应的API接口
        logger.info(f"准备插入分数线: {score_data['year']}年 {score_data['province']} - 院校ID {score_data['university_id']}")
        return True
    except Exception as e:
        logger.error(f"插入分数线失败: {e}")
        return False


def main():
    """批量导入历年录取分数线数据"""
    logger.info("=" * 60)
    logger.info("开始批量导入历年录取分数线数据")
    logger.info(f"共 {len(ADMISSION_SCORES_DATA)} 条分数线数据")
    logger.info("=" * 60)
    
    api = APIHelper()
    
    # 检查后端服务
    if not api.check_backend_status():
        logger.error("后端服务未运行，请先启动后端服务")
        return
    
    # 使用SQL直接批量插入（因为API可能没有分数线接口）
    logger.info("使用SQL直接插入分数线数据...")
    
    import pymysql
    try:
        # 连接数据库
        conn = pymysql.connect(
            host='localhost',
            port=3306,
            user='root',
            password='root',
            database='volunteer_exam',
            charset='utf8mb4'
        )
        cursor = conn.cursor()
        
        success_count = 0
        for score_data in ADMISSION_SCORES_DATA:
            try:
                sql = """
                INSERT INTO admission_record 
                (university_id, year, province, batch, category, min_score, avg_score, max_score)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                """
                cursor.execute(sql, (
                    score_data['university_id'],
                    score_data['year'],
                    score_data['province'],
                    score_data['batch'],
                    score_data['category'],
                    score_data['min_score'],
                    score_data.get('avg_score'),
                    score_data.get('max_score')
                ))
                success_count += 1
                logger.info(f"成功插入: {score_data['year']}年 {score_data['province']} 院校ID={score_data['university_id']}")
            except Exception as e:
                logger.error(f"插入失败: {e}")
        
        conn.commit()
        cursor.close()
        conn.close()
        
        logger.info("=" * 60)
        logger.info(f"批量导入完成！成功: {success_count}/{len(ADMISSION_SCORES_DATA)}")
        logger.info("=" * 60)
        
    except ImportError:
        logger.error("pymysql未安装，无法直接插入数据库")
        logger.info("请使用以下SQL手动导入：")
        logger.info("")
        for score_data in ADMISSION_SCORES_DATA[:5]:  # 只显示前5条示例
            logger.info(f"INSERT INTO admission_record (university_id, year, province, batch, category, min_score, avg_score, max_score) "
                       f"VALUES ({score_data['university_id']}, {score_data['year']}, '{score_data['province']}', "
                       f"'{score_data['batch']}', '{score_data['category']}', {score_data['min_score']}, "
                       f"{score_data.get('avg_score')}, {score_data.get('max_score')});")
        logger.info("...")
        
    except Exception as e:
        logger.error(f"数据库连接失败: {e}")
        logger.info("将生成SQL文件供手动导入...")
        
        # 生成SQL文件
        with open('admission_scores.sql', 'w', encoding='utf-8') as f:
            f.write("USE volunteer_exam;\n\n")
            for score_data in ADMISSION_SCORES_DATA:
                f.write(f"INSERT INTO admission_record (university_id, year, province, batch, category, min_score, avg_score, max_score) "
                       f"VALUES ({score_data['university_id']}, {score_data['year']}, '{score_data['province']}', "
                       f"'{score_data['batch']}', '{score_data['category']}', {score_data['min_score']}, "
                       f"{score_data.get('avg_score')}, {score_data.get('max_score')});\n")
        
        logger.info("SQL文件已生成: admission_scores.sql")
        logger.info("请使用以下命令导入:")
        logger.info("mysql -u root -p < admission_scores.sql")


if __name__ == '__main__':
    main()
