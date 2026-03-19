"""
从CSV批量导入历年分数线
支持批量导入多年份、多省份的录取分数线数据
"""
import csv
import pymysql
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


def import_scores_from_csv(csv_file):
    """从CSV文件导入分数线"""
    logger.info("=" * 60)
    logger.info(f"开始从CSV导入分数线: {csv_file}")
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
        skip_count = 0
        error_count = 0
        
        # 读取CSV文件
        with open(csv_file, 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            
            for row in reader:
                university_name = row['院校名称']
                year = int(row['年份'])
                province = row['省份']
                batch = row['批次']
                category = row.get('科类', '')
                min_score = int(row['最低分']) if row['最低分'] else None
                avg_score = int(row['平均分']) if row.get('平均分') and row['平均分'] else None
                max_score = int(row['最高分']) if row.get('最高分') and row['最高分'] else None
                enrollment = int(row['录取人数']) if row.get('录取人数') and row['录取人数'] else None
                
                # 查询院校ID
                cursor.execute(
                    "SELECT id FROM university WHERE name = %s",
                    (university_name,)
                )
                result = cursor.fetchone()
                
                if not result:
                    logger.warning(f"✗ 未找到院校: {university_name}")
                    skip_count += 1
                    continue
                
                university_id = result[0]
                
                # 检查是否已存在
                cursor.execute(
                    """SELECT id FROM admission_record 
                       WHERE university_id = %s AND year = %s AND province = %s AND batch = %s""",
                    (university_id, year, province, batch)
                )
                
                if cursor.fetchone():
                    # 更新
                    cursor.execute(
                        """UPDATE admission_record 
                           SET min_score = %s, avg_score = %s, max_score = %s, enrollment_number = %s
                           WHERE university_id = %s AND year = %s AND province = %s AND batch = %s""",
                        (min_score, avg_score, max_score, enrollment, university_id, year, province, batch)
                    )
                    logger.info(f"↻ 更新 {university_name} {year}年 {province} {batch} 分数线")
                else:
                    # 插入
                    cursor.execute(
                        """INSERT INTO admission_record 
                           (university_id, year, province, batch, min_score, avg_score, max_score, enrollment_number)
                           VALUES (%s, %s, %s, %s, %s, %s, %s, %s)""",
                        (university_id, year, province, batch, min_score, avg_score, max_score, enrollment)
                    )
                    logger.info(f"✓ 导入 {university_name} {year}年 {province} {batch} 分数线: {min_score}-{max_score}分")
                
                success_count += 1
        
        conn.commit()
        cursor.close()
        conn.close()
        
        logger.info("=" * 60)
        logger.info(f"导入完成！")
        logger.info(f"成功: {success_count}")
        logger.info(f"跳过: {skip_count}")
        logger.info(f"错误: {error_count}")
        logger.info("=" * 60)
        
    except Exception as e:
        logger.error(f"导入失败: {e}")
        import traceback
        traceback.print_exc()


if __name__ == '__main__':
    # 使用示例
    import_scores_from_csv('历年分数线导入模板.csv')
