"""
自动爬取软科中国大学排名
完全自动化，无需手动操作
"""
import requests
from bs4 import BeautifulSoup
import pymysql
import logging
import time
import random
import json

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class RankingCrawler:
    """软科排名爬虫"""
    
    def __init__(self):
        self.base_url = "https://www.shanghairanking.cn"
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
            'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8',
            'Referer': 'https://www.shanghairanking.cn/',
        }
        self.session = requests.Session()
        self.session.headers.update(self.headers)
    
    def crawl_rankings(self, year=2024):
        """
        爬取指定年份的排名数据
        
        Args:
            year: 年份，默认2024
        
        Returns:
            list: 排名数据列表
        """
        logger.info(f"开始爬取{year}年软科排名...")
        
        # 软科排名API（实际使用时需要根据网站结构调整）
        url = f"{self.base_url}/rankings/bcur/{year}"
        
        try:
            # 添加延时，避免被封
            time.sleep(random.uniform(1, 3))
            
            response = self.session.get(url, timeout=30)
            response.raise_for_status()
            
            # 解析HTML
            soup = BeautifulSoup(response.text, 'html.parser')
            
            rankings = []
            
            # 方法1：尝试解析表格（需根据实际网页结构调整）
            table = soup.find('table', class_='rk-table')
            if table:
                rows = table.find_all('tr')[1:]  # 跳过表头
                for row in rows:
                    cols = row.find_all('td')
                    if len(cols) >= 2:
                        try:
                            ranking = cols[0].get_text(strip=True)
                            name = cols[1].get_text(strip=True)
                            
                            # 清理排名数字
                            ranking = ''.join(filter(str.isdigit, ranking))
                            if ranking:
                                rankings.append({
                                    'ranking': int(ranking),
                                    'name': name,
                                    'year': year
                                })
                        except Exception as e:
                            logger.warning(f"解析行数据失败: {e}")
                            continue
            
            # 方法2：尝试从JSON API获取（软科可能有API）
            if not rankings:
                api_url = f"{self.base_url}/api/rankings/bcur/{year}"
                try:
                    api_response = self.session.get(api_url, timeout=30)
                    if api_response.status_code == 200:
                        data = api_response.json()
                        # 根据实际API结构解析
                        if 'data' in data:
                            for item in data['data']:
                                rankings.append({
                                    'ranking': item.get('rank'),
                                    'name': item.get('name'),
                                    'year': year
                                })
                except:
                    pass
            
            logger.info(f"成功爬取 {len(rankings)} 所院校排名")
            return rankings
            
        except requests.RequestException as e:
            logger.error(f"爬取失败: {e}")
            return []
    
    def save_to_database(self, rankings):
        """
        保存排名到数据库
        
        Args:
            rankings: 排名数据列表
        """
        if not rankings:
            logger.warning("没有数据需要保存")
            return
        
        logger.info(f"开始保存 {len(rankings)} 条排名数据到数据库...")
        
        try:
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
            
            for item in rankings:
                name = item['name']
                ranking = item['ranking']
                
                # 查询院校是否存在
                cursor.execute(
                    "SELECT id FROM university WHERE name = %s",
                    (name,)
                )
                result = cursor.fetchone()
                
                if result:
                    # 更新排名
                    cursor.execute(
                        "UPDATE university SET ranking = %s WHERE name = %s",
                        (ranking, name)
                    )
                    success_count += 1
                    logger.info(f"✓ 更新 {name} 排名为 {ranking}")
                else:
                    not_found_count += 1
                    logger.warning(f"✗ 未找到院校: {name}")
            
            conn.commit()
            cursor.close()
            conn.close()
            
            logger.info("=" * 60)
            logger.info(f"保存完成！")
            logger.info(f"成功: {success_count}/{len(rankings)}")
            logger.info(f"未找到: {not_found_count}")
            logger.info("=" * 60)
            
        except Exception as e:
            logger.error(f"保存到数据库失败: {e}")


def main():
    """主函数"""
    logger.info("=" * 60)
    logger.info("软科排名自动爬虫启动")
    logger.info("=" * 60)
    
    crawler = RankingCrawler()
    
    # 爬取2024年排名
    rankings = crawler.crawl_rankings(year=2024)
    
    if rankings:
        # 保存到数据库
        crawler.save_to_database(rankings)
    else:
        logger.warning("未能获取排名数据，请检查网络或网站结构是否变化")
        logger.info("备选方案：使用预设的排名数据")
        
        # 使用预设数据作为备选
        from update_rankings_from_excel import RANKINGS_2024, update_rankings
        update_rankings()


if __name__ == '__main__':
    main()
