"""
自动爬取各省录取分数线
支持多省份并发爬取
"""
import requests
from bs4 import BeautifulSoup
import pymysql
import logging
import time
import random
import re
from concurrent.futures import ThreadPoolExecutor, as_completed

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class ScoreCrawler:
    """分数线爬虫"""
    
    def __init__(self):
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Language': 'zh-CN,zh;q=0.9',
        }
        self.session = requests.Session()
        self.session.headers.update(self.headers)
        
        # 各省考试院URL配置
        self.province_urls = {
            '北京': 'https://www.bjeea.cn/',
            '上海': 'https://www.shmeea.edu.cn/',
            '广东': 'https://eea.gd.gov.cn/',
            '浙江': 'https://www.zjzs.net/',
            '江苏': 'https://www.jseea.cn/',
        }
    
    def crawl_province_scores(self, province, year=2023):
        """
        爬取指定省份的分数线
        
        Args:
            province: 省份名称
            year: 年份
        
        Returns:
            list: 分数线数据列表
        """
        logger.info(f"开始爬取{province} {year}年分数线...")
        
        if province not in self.province_urls:
            logger.warning(f"暂不支持{province}省份")
            return []
        
        try:
            # 添加延时
            time.sleep(random.uniform(2, 5))
            
            base_url = self.province_urls[province]
            
            # 这里需要根据各省网站实际结构调整
            # 示例：尝试查找分数线数据页面
            response = self.session.get(base_url, timeout=30)
            response.raise_for_status()
            
            soup = BeautifulSoup(response.text, 'html.parser')
            
            # 查找分数线相关链接
            score_links = []
            for link in soup.find_all('a', href=True):
                text = link.get_text()
                if any(keyword in text for keyword in ['录取分数', '分数线', '投档线']):
                    score_links.append(link['href'])
            
            scores = []
            
            # 解析分数线数据（示例，需根据实际调整）
            for link in score_links[:3]:  # 只处理前3个链接
                try:
                    if not link.startswith('http'):
                        link = base_url + link
                    
                    time.sleep(random.uniform(1, 3))
                    detail_response = self.session.get(link, timeout=30)
                    detail_soup = BeautifulSoup(detail_response.text, 'html.parser')
                    
                    # 查找表格
                    tables = detail_soup.find_all('table')
                    for table in tables:
                        rows = table.find_all('tr')[1:]  # 跳过表头
                        for row in rows:
                            cols = row.find_all('td')
                            if len(cols) >= 3:
                                try:
                                    university_name = cols[0].get_text(strip=True)
                                    min_score = self._extract_number(cols[1].get_text())
                                    max_score = self._extract_number(cols[2].get_text()) if len(cols) > 2 else None
                                    
                                    if university_name and min_score:
                                        scores.append({
                                            'university_name': university_name,
                                            'province': province,
                                            'year': year,
                                            'batch': '本科一批',
                                            'min_score': min_score,
                                            'max_score': max_score,
                                        })
                                except:
                                    continue
                except Exception as e:
                    logger.warning(f"解析链接失败: {e}")
                    continue
            
            logger.info(f"{province}爬取到 {len(scores)} 条分数线")
            return scores
            
        except Exception as e:
            logger.error(f"爬取{province}失败: {e}")
            return []
    
    def _extract_number(self, text):
        """从文本中提取数字"""
        numbers = re.findall(r'\d+', text)
        return int(numbers[0]) if numbers else None
    
    def crawl_all_provinces(self, year=2023, max_workers=3):
        """
        并发爬取所有省份
        
        Args:
            year: 年份
            max_workers: 最大并发数
        
        Returns:
            list: 所有分数线数据
        """
        all_scores = []
        
        with ThreadPoolExecutor(max_workers=max_workers) as executor:
            futures = {
                executor.submit(self.crawl_province_scores, province, year): province
                for province in self.province_urls.keys()
            }
            
            for future in as_completed(futures):
                province = futures[future]
                try:
                    scores = future.result()
                    all_scores.extend(scores)
                    logger.info(f"✓ {province}完成")
                except Exception as e:
                    logger.error(f"✗ {province}失败: {e}")
        
        return all_scores
    
    def save_to_database(self, scores):
        """保存分数线到数据库"""
        if not scores:
            logger.warning("没有数据需要保存")
            return
        
        logger.info(f"开始保存 {len(scores)} 条分数线到数据库...")
        
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
            skip_count = 0
            
            for item in scores:
                # 查询院校ID
                cursor.execute(
                    "SELECT id FROM university WHERE name = %s",
                    (item['university_name'],)
                )
                result = cursor.fetchone()
                
                if not result:
                    skip_count += 1
                    continue
                
                university_id = result[0]
                
                # 检查是否已存在
                cursor.execute(
                    """SELECT id FROM admission_record 
                       WHERE university_id = %s AND year = %s AND province = %s""",
                    (university_id, item['year'], item['province'])
                )
                
                if cursor.fetchone():
                    # 更新
                    cursor.execute(
                        """UPDATE admission_record 
                           SET min_score = %s, max_score = %s
                           WHERE university_id = %s AND year = %s AND province = %s""",
                        (item['min_score'], item['max_score'], 
                         university_id, item['year'], item['province'])
                    )
                else:
                    # 插入
                    cursor.execute(
                        """INSERT INTO admission_record 
                           (university_id, year, province, batch, min_score, max_score)
                           VALUES (%s, %s, %s, %s, %s, %s)""",
                        (university_id, item['year'], item['province'], 
                         item['batch'], item['min_score'], item['max_score'])
                    )
                
                success_count += 1
                logger.info(f"✓ {item['university_name']} {item['province']}")
            
            conn.commit()
            cursor.close()
            conn.close()
            
            logger.info("=" * 60)
            logger.info(f"保存完成！成功: {success_count}, 跳过: {skip_count}")
            logger.info("=" * 60)
            
        except Exception as e:
            logger.error(f"保存失败: {e}")


def main():
    """主函数"""
    logger.info("=" * 60)
    logger.info("分数线自动爬虫启动")
    logger.info("=" * 60)
    
    crawler = ScoreCrawler()
    
    # 爬取2023年分数线
    scores = crawler.crawl_all_provinces(year=2023, max_workers=2)
    
    if scores:
        # 保存到数据库
        crawler.save_to_database(scores)
    else:
        logger.warning("未能获取分数线数据")
        logger.info("提示：各省网站结构可能需要手动适配")


if __name__ == '__main__':
    main()
