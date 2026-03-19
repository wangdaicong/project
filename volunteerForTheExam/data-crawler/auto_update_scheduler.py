"""
自动更新调度器
定时执行数据更新任务
"""
import schedule
import time
import logging
from datetime import datetime
import subprocess
import os

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('auto_update.log', encoding='utf-8'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)


class AutoUpdateScheduler:
    """自动更新调度器"""
    
    def __init__(self):
        self.script_dir = os.path.dirname(os.path.abspath(__file__))
    
    def run_script(self, script_name):
        """运行Python脚本"""
        logger.info(f"开始执行: {script_name}")
        try:
            script_path = os.path.join(self.script_dir, script_name)
            result = subprocess.run(
                ['python', script_path],
                capture_output=True,
                text=True,
                encoding='utf-8',
                timeout=3600  # 1小时超时
            )
            
            if result.returncode == 0:
                logger.info(f"✓ {script_name} 执行成功")
                logger.debug(result.stdout)
            else:
                logger.error(f"✗ {script_name} 执行失败")
                logger.error(result.stderr)
                
        except subprocess.TimeoutExpired:
            logger.error(f"✗ {script_name} 执行超时")
        except Exception as e:
            logger.error(f"✗ {script_name} 执行异常: {e}")
    
    def update_rankings(self):
        """更新院校排名"""
        logger.info("=" * 60)
        logger.info("开始更新院校排名")
        logger.info("=" * 60)
        
        # 先尝试爬虫
        self.run_script('auto_crawl_rankings.py')
        
        # 验证数据
        self.run_script('verify_data.py')
    
    def update_scores(self):
        """更新分数线"""
        logger.info("=" * 60)
        logger.info("开始更新分数线")
        logger.info("=" * 60)
        
        # 爬取分数线
        self.run_script('auto_crawl_scores.py')
        
        # 验证数据
        self.run_script('verify_data.py')
    
    def daily_check(self):
        """每日数据检查"""
        logger.info("执行每日数据检查")
        self.run_script('verify_data.py')
    
    def setup_schedule(self):
        """设置定时任务"""
        logger.info("设置定时任务...")
        
        # 每年4月15日更新排名（软科通常4月发布）
        schedule.every().day.at("02:00").do(self._check_and_update_rankings)
        
        # 每年7月20日开始更新分数线（高考后）
        schedule.every().day.at("03:00").do(self._check_and_update_scores)
        
        # 每天凌晨1点数据检查
        schedule.every().day.at("01:00").do(self.daily_check)
        
        # 每周一凌晨4点完整验证
        schedule.every().monday.at("04:00").do(self.run_script, 'verify_data.py')
        
        logger.info("定时任务设置完成")
        logger.info("- 每年4月15日 02:00 更新排名")
        logger.info("- 每年7月20日 03:00 更新分数线")
        logger.info("- 每天 01:00 数据检查")
        logger.info("- 每周一 04:00 完整验证")
    
    def _check_and_update_rankings(self):
        """检查日期并更新排名"""
        now = datetime.now()
        # 只在4月15日-30日执行
        if now.month == 4 and 15 <= now.day <= 30:
            self.update_rankings()
    
    def _check_and_update_scores(self):
        """检查日期并更新分数线"""
        now = datetime.now()
        # 只在7月20日-8月31日执行
        if (now.month == 7 and now.day >= 20) or now.month == 8:
            self.update_scores()
    
    def run_forever(self):
        """持续运行调度器"""
        logger.info("=" * 60)
        logger.info("自动更新调度器启动")
        logger.info("=" * 60)
        
        self.setup_schedule()
        
        logger.info("调度器正在运行，按Ctrl+C停止...")
        
        try:
            while True:
                schedule.run_pending()
                time.sleep(60)  # 每分钟检查一次
        except KeyboardInterrupt:
            logger.info("调度器已停止")


def main():
    """主函数"""
    scheduler = AutoUpdateScheduler()
    scheduler.run_forever()


if __name__ == '__main__':
    main()
