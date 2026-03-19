"""
从Excel批量更新院校排名
支持批量更新数据库中院校的排名信息
"""
import pymysql
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 2024年软科中国大学排名（Top 100）
RANKINGS_2024 = {
    '清华大学': 1,
    '北京大学': 2,
    '浙江大学': 3,
    '上海交通大学': 4,
    '复旦大学': 5,
    '南京大学': 6,
    '中国科学技术大学': 7,
    '华中科技大学': 8,
    '武汉大学': 9,
    '西安交通大学': 10,
    '哈尔滨工业大学': 11,
    '中国人民大学': 12,
    '北京师范大学': 13,
    '同济大学': 14,
    '北京航空航天大学': 15,
    '四川大学': 16,
    '东南大学': 17,
    '中山大学': 18,
    '南开大学': 19,
    '天津大学': 20,
    '山东大学': 21,
    '厦门大学': 22,
    '北京理工大学': 23,
    '华南理工大学': 24,
    '吉林大学': 25,
    '华东师范大学': 26,
    '中南大学': 27,
    '大连理工大学': 28,
    '电子科技大学': 29,
    '西北工业大学': 30,
    '重庆大学': 31,
    '湖南大学': 32,
    '兰州大学': 33,
    '东北大学': 34,
    '中国农业大学': 35,
    '北京科技大学': 36,
    '北京交通大学': 37,
    '华东理工大学': 38,
    '北京邮电大学': 39,
    '南京航空航天大学': 40,
    '南京理工大学': 41,
    '西南大学': 42,
    '苏州大学': 43,
    '武汉理工大学': 44,
    '西安电子科技大学': 45,
    '暨南大学': 46,
    '北京化工大学': 47,
    '南京农业大学': 48,
    '华中师范大学': 49,
    '郑州大学': 50,
    '西南交通大学': 51,
    '华中农业大学': 52,
    '中国海洋大学': 53,
    '河海大学': 54,
    '南京师范大学': 55,
    '上海大学': 56,
    '江南大学': 57,
    '中国地质大学(武汉)': 58,
    '北京工业大学': 59,
    '陕西师范大学': 60,
    '合肥工业大学': 61,
    '湖南师范大学': 62,
    '东华大学': 63,
    '西北大学': 64,
    '中国石油大学(北京)': 65,
    '福州大学': 66,
    '北京林业大学': 67,
    '中国矿业大学': 68,
    '云南大学': 69,
    '上海财经大学': 70,
    '对外经济贸易大学': 71,
    '中央财经大学': 72,
    '华南师范大学': 73,
    '南昌大学': 74,
    '中国政法大学': 75,
    '深圳大学': 76,
    '安徽大学': 77,
    '首都医科大学': 78,
    '中央民族大学': 79,
    '新疆大学': 80,
}


def update_rankings():
    """批量更新院校排名"""
    logger.info("=" * 60)
    logger.info("开始批量更新院校排名")
    logger.info(f"共 {len(RANKINGS_2024)} 所院校")
    logger.info("=" * 60)
    
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
        not_found_count = 0
        
        for university_name, ranking in RANKINGS_2024.items():
            # 查询院校是否存在
            cursor.execute(
                "SELECT id, name FROM university WHERE name = %s",
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
                logger.info(f"✓ 更新 {university_name} 排名为 {ranking}")
            else:
                not_found_count += 1
                logger.warning(f"✗ 未找到院校: {university_name}")
        
        conn.commit()
        cursor.close()
        conn.close()
        
        logger.info("=" * 60)
        logger.info(f"更新完成！")
        logger.info(f"成功: {success_count}/{len(RANKINGS_2024)}")
        logger.info(f"未找到: {not_found_count}")
        logger.info("=" * 60)
        
    except Exception as e:
        logger.error(f"数据库操作失败: {e}")


if __name__ == '__main__':
    update_rankings()
