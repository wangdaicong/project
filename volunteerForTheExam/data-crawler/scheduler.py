"""
定时任务调度器
定期执行数据爬取和同步
"""
import schedule
import time
import logging
from datetime import datetime
from university_crawler import UniversityCrawler
from major_crawler import MajorCrawler

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('logs/scheduler.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)


def sync_universities():
    """同步院校数据"""
    logger.info("=" * 60)
    logger.info(f"开始同步院校数据 - {datetime.now()}")
    logger.info("=" * 60)
    
    try:
        crawler = UniversityCrawler()
        universities = crawler.crawl_university_list(max_pages=50)
        logger.info(f"院校数据同步完成，共{len(universities)}所")
    except Exception as e:
        logger.error(f"院校数据同步失败: {e}")


def sync_majors():
    """同步专业数据"""
    logger.info("=" * 60)
    logger.info(f"开始同步专业数据 - {datetime.now()}")
    logger.info("=" * 60)
    
    try:
        crawler = MajorCrawler()
        majors = crawler.crawl_major_categories()
        logger.info(f"专业数据同步完成，共{len(majors)}个")
    except Exception as e:
        logger.error(f"专业数据同步失败: {e}")


def main():
    """主函数 - 设置定时任务"""
    logger.info("数据同步调度器启动")
    
    # 设置定时任务 - 每3个月（90天）执行一次
    schedule.every(90).days.at("02:00").do(sync_universities)
    schedule.every(90).days.at("03:00").do(sync_majors)
    
    logger.info("定时任务已设置:")
    logger.info("- 院校数据: 每3个月（90天） 02:00")
    logger.info("- 专业数据: 每3个月（90天） 03:00")
    
    # 首次启动时立即执行一次（可选）
    # sync_universities()
    # sync_majors()
    
    # 保持运行
    while True:
        schedule.run_pending()
        time.sleep(60)  # 每分钟检查一次


if __name__ == '__main__':
    main()
