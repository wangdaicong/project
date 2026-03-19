"""
数据验证工具
用于检查数据库中的数据质量和完整性
"""
import pymysql
import logging
from datetime import datetime

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


def get_connection():
    """获取数据库连接"""
    return pymysql.connect(
        host='localhost',
        port=3306,
        user='root',
        password='root',
        database='volunteer_exam',
        charset='utf8mb4'
    )


def verify_rankings():
    """验证排名数据"""
    logger.info("\n" + "=" * 60)
    logger.info("【排名数据验证】")
    logger.info("=" * 60)
    
    conn = get_connection()
    cursor = conn.cursor()
    
    # 1. 统计有排名的院校数量
    cursor.execute("SELECT COUNT(*) FROM university WHERE ranking IS NOT NULL")
    ranked_count = cursor.fetchone()[0]
    
    cursor.execute("SELECT COUNT(*) FROM university")
    total_count = cursor.fetchone()[0]
    
    coverage = (ranked_count / total_count * 100) if total_count > 0 else 0
    
    logger.info(f"院校总数: {total_count}")
    logger.info(f"有排名院校数: {ranked_count}")
    logger.info(f"覆盖率: {coverage:.1f}%")
    
    # 2. 检查排名前10
    cursor.execute("""
        SELECT name, ranking, level 
        FROM university 
        WHERE ranking <= 10 
        ORDER BY ranking
    """)
    top10 = cursor.fetchall()
    
    if top10:
        logger.info("\n排名前10的院校:")
        for name, ranking, level in top10:
            logger.info(f"  {ranking:2d}. {name:20s} ({level})")
    
    # 3. 检查985/211院校排名覆盖率
    cursor.execute("""
        SELECT COUNT(*) FROM university 
        WHERE (level LIKE '%985%' OR level LIKE '%211%') 
        AND ranking IS NOT NULL
    """)
    ranked_985_211 = cursor.fetchone()[0]
    
    cursor.execute("""
        SELECT COUNT(*) FROM university 
        WHERE level LIKE '%985%' OR level LIKE '%211%'
    """)
    total_985_211 = cursor.fetchone()[0]
    
    coverage_985_211 = (ranked_985_211 / total_985_211 * 100) if total_985_211 > 0 else 0
    
    logger.info(f"\n985/211院校排名覆盖:")
    logger.info(f"  总数: {total_985_211}")
    logger.info(f"  有排名: {ranked_985_211}")
    logger.info(f"  覆盖率: {coverage_985_211:.1f}%")
    
    # 4. 检查缺少排名的985/211院校
    cursor.execute("""
        SELECT name, level FROM university 
        WHERE (level LIKE '%985%' OR level LIKE '%211%') 
        AND ranking IS NULL
        LIMIT 10
    """)
    missing = cursor.fetchall()
    
    if missing:
        logger.info("\n缺少排名的985/211院校（前10所）:")
        for name, level in missing:
            logger.info(f"  - {name} ({level})")
    
    cursor.close()
    conn.close()


def verify_scores(year=2023):
    """验证分数线数据"""
    logger.info("\n" + "=" * 60)
    logger.info(f"【{year}年分数线数据验证】")
    logger.info("=" * 60)
    
    conn = get_connection()
    cursor = conn.cursor()
    
    # 1. 统计总记录数
    cursor.execute("SELECT COUNT(*) FROM admission_record WHERE year = %s", (year,))
    total_records = cursor.fetchone()[0]
    
    cursor.execute("SELECT COUNT(DISTINCT university_id) FROM admission_record WHERE year = %s", (year,))
    university_count = cursor.fetchone()[0]
    
    cursor.execute("SELECT COUNT(DISTINCT province) FROM admission_record WHERE year = %s", (year,))
    province_count = cursor.fetchone()[0]
    
    logger.info(f"分数线记录总数: {total_records}")
    logger.info(f"覆盖院校数: {university_count}")
    logger.info(f"覆盖省份数: {province_count}")
    
    # 2. 按省份统计
    cursor.execute("""
        SELECT province, COUNT(*) as count 
        FROM admission_record 
        WHERE year = %s 
        GROUP BY province 
        ORDER BY count DESC
    """, (year,))
    provinces = cursor.fetchall()
    
    if provinces:
        logger.info(f"\n各省份数据统计:")
        for province, count in provinces:
            logger.info(f"  {province:10s}: {count:4d}条")
    
    # 3. 按批次统计
    cursor.execute("""
        SELECT batch, COUNT(*) as count 
        FROM admission_record 
        WHERE year = %s 
        GROUP BY batch 
        ORDER BY count DESC
    """, (year,))
    batches = cursor.fetchall()
    
    if batches:
        logger.info(f"\n各批次数据统计:")
        for batch, count in batches:
            logger.info(f"  {batch:15s}: {count:4d}条")
    
    # 4. 检查异常数据
    cursor.execute("""
        SELECT u.name, a.province, a.min_score, a.max_score
        FROM admission_record a
        JOIN university u ON a.university_id = u.id
        WHERE a.year = %s 
        AND (a.min_score > a.max_score OR a.min_score < 0 OR a.max_score > 750)
    """, (year,))
    abnormal = cursor.fetchall()
    
    if abnormal:
        logger.warning(f"\n发现 {len(abnormal)} 条异常数据:")
        for name, province, min_score, max_score in abnormal[:10]:
            logger.warning(f"  {name} ({province}): {min_score}-{max_score}分")
    else:
        logger.info("\n✓ 未发现异常数据")
    
    # 5. 检查985院校覆盖率
    cursor.execute("""
        SELECT u.name, COUNT(a.id) as record_count
        FROM university u
        LEFT JOIN admission_record a ON u.id = a.university_id AND a.year = %s
        WHERE u.level LIKE '%%985%%'
        GROUP BY u.id
        HAVING record_count = 0
    """, (year,))
    missing_985 = cursor.fetchall()
    
    if missing_985:
        logger.warning(f"\n缺少{year}年分数线的985院校（共{len(missing_985)}所）:")
        for name, _ in missing_985[:10]:
            logger.warning(f"  - {name}")
    else:
        logger.info(f"\n✓ 所有985院校都有{year}年分数线数据")
    
    cursor.close()
    conn.close()


def verify_all_years():
    """验证所有年份的数据"""
    logger.info("\n" + "=" * 60)
    logger.info("【历年数据统计】")
    logger.info("=" * 60)
    
    conn = get_connection()
    cursor = conn.cursor()
    
    cursor.execute("""
        SELECT year, COUNT(*) as count, COUNT(DISTINCT university_id) as universities
        FROM admission_record 
        GROUP BY year 
        ORDER BY year DESC
    """)
    years = cursor.fetchall()
    
    if years:
        logger.info("\n年份 | 记录数 | 院校数")
        logger.info("-" * 30)
        for year, count, universities in years:
            logger.info(f"{year} | {count:6d} | {universities:4d}")
    else:
        logger.warning("未找到任何分数线数据")
    
    cursor.close()
    conn.close()


def generate_summary_report():
    """生成汇总报告"""
    logger.info("\n" + "=" * 60)
    logger.info("【数据质量汇总报告】")
    logger.info(f"生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    logger.info("=" * 60)
    
    conn = get_connection()
    cursor = conn.cursor()
    
    # 基础统计
    cursor.execute("SELECT COUNT(*) FROM university")
    total_universities = cursor.fetchone()[0]
    
    cursor.execute("SELECT COUNT(*) FROM university WHERE ranking IS NOT NULL")
    ranked_universities = cursor.fetchone()[0]
    
    cursor.execute("SELECT COUNT(*) FROM admission_record")
    total_scores = cursor.fetchone()[0]
    
    cursor.execute("SELECT COUNT(DISTINCT university_id) FROM admission_record")
    scored_universities = cursor.fetchone()[0]
    
    cursor.execute("SELECT COUNT(DISTINCT province) FROM admission_record")
    provinces = cursor.fetchone()[0]
    
    cursor.execute("SELECT MAX(year) FROM admission_record")
    latest_year = cursor.fetchone()[0]
    
    logger.info(f"\n院校数据:")
    logger.info(f"  总院校数: {total_universities}")
    logger.info(f"  有排名: {ranked_universities} ({ranked_universities/total_universities*100:.1f}%)")
    logger.info(f"  有分数线: {scored_universities} ({scored_universities/total_universities*100:.1f}%)")
    
    logger.info(f"\n分数线数据:")
    logger.info(f"  总记录数: {total_scores}")
    logger.info(f"  覆盖省份: {provinces}")
    logger.info(f"  最新年份: {latest_year}")
    
    # 数据完整性评分
    ranking_score = (ranked_universities / total_universities * 100) if total_universities > 0 else 0
    score_score = (scored_universities / total_universities * 100) if total_universities > 0 else 0
    overall_score = (ranking_score + score_score) / 2
    
    logger.info(f"\n数据完整性评分:")
    logger.info(f"  排名完整度: {ranking_score:.1f}分")
    logger.info(f"  分数线完整度: {score_score:.1f}分")
    logger.info(f"  综合评分: {overall_score:.1f}分")
    
    if overall_score >= 90:
        logger.info(f"  评级: ⭐⭐⭐⭐⭐ 优秀")
    elif overall_score >= 70:
        logger.info(f"  评级: ⭐⭐⭐⭐ 良好")
    elif overall_score >= 50:
        logger.info(f"  评级: ⭐⭐⭐ 及格")
    else:
        logger.info(f"  评级: ⭐⭐ 需改进")
    
    cursor.close()
    conn.close()


if __name__ == '__main__':
    try:
        # 生成完整验证报告
        generate_summary_report()
        verify_rankings()
        verify_scores(2023)
        verify_all_years()
        
        logger.info("\n" + "=" * 60)
        logger.info("验证完成！")
        logger.info("=" * 60)
        
    except Exception as e:
        logger.error(f"验证失败: {e}")
        import traceback
        traceback.print_exc()
