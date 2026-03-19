"""
院校数据爬虫
爬取阳光高考平台的院校信息
"""
import requests
from bs4 import BeautifulSoup
import time
import logging
from config import CRAWLER_CONFIG, GAOKAO_URLS
from db_helper import DBHelper

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class UniversityCrawler:
    """院校爬虫"""
    
    def __init__(self):
        self.headers = {
            'User-Agent': CRAWLER_CONFIG['user_agent'],
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8',
        }
        self.db = DBHelper()
        self.session = requests.Session()
    
    def crawl_university_list(self, max_pages=50):
        """
        爬取院校列表
        
        注意：这是示例代码，实际需要根据网站的真实HTML结构调整
        阳光高考平台可能需要处理JavaScript渲染，可能需要使用Selenium
        """
        logger.info("开始爬取院校列表")
        universities = []
        
        try:
            self.db.connect()
            
            for page in range(1, max_pages + 1):
                try:
                    # 构造请求参数（需根据实际网站调整）
                    params = {
                        'searchType': '1',
                        'start': (page - 1) * 20,
                    }
                    
                    response = self.session.get(
                        GAOKAO_URLS['school_list'],
                        headers=self.headers,
                        params=params,
                        timeout=CRAWLER_CONFIG['timeout']
                    )
                    response.encoding = 'utf-8'
                    
                    if response.status_code != 200:
                        logger.warning(f"第{page}页请求失败: {response.status_code}")
                        continue
                    
                    # 解析HTML（需根据实际结构调整选择器）
                    soup = BeautifulSoup(response.text, 'html.parser')
                    
                    # 示例：查找院校列表项
                    # 实际选择器需要根据网页结构调整
                    school_items = soup.select('.yxk-table tbody tr')
                    
                    if not school_items:
                        logger.info(f"第{page}页没有数据，停止爬取")
                        break
                    
                    for item in school_items:
                        try:
                            university_data = self.parse_university_item(item)
                            if university_data:
                                # 保存到数据库
                                university_id = self.db.insert_university(university_data)
                                universities.append(university_data)
                                logger.info(f"成功爬取: {university_data['name']}")
                        except Exception as e:
                            logger.error(f"解析院校数据失败: {e}")
                            continue
                    
                    logger.info(f"第{page}页爬取完成，共{len(school_items)}所院校")
                    
                    # 礼貌爬取
                    time.sleep(CRAWLER_CONFIG['delay'])
                    
                except Exception as e:
                    logger.error(f"爬取第{page}页失败: {e}")
                    continue
            
            # 记录同步日志
            self.db.log_sync('university', 'success', f'成功爬取{len(universities)}所院校', len(universities))
            logger.info(f"院校列表爬取完成，共{len(universities)}所")
            
        except Exception as e:
            logger.error(f"爬取失败: {e}")
            self.db.log_sync('university', 'failed', str(e), 0)
        finally:
            self.db.close()
        
        return universities
    
    def parse_university_item(self, item):
        """
        解析院校列表项
        
        注意：这是示例代码，需要根据实际HTML结构调整
        """
        try:
            # 示例解析逻辑（需根据实际HTML调整）
            name_elem = item.select_one('td:nth-child(1) a')
            province_elem = item.select_one('td:nth-child(2)')
            level_elem = item.select_one('td:nth-child(3)')
            type_elem = item.select_one('td:nth-child(4)')
            
            if not name_elem:
                return None
            
            data = {
                'name': name_elem.text.strip(),
                'province': province_elem.text.strip() if province_elem else None,
                'level': level_elem.text.strip() if level_elem else None,
                'type': type_elem.text.strip() if type_elem else None,
                'website': None,
                'description': None,
            }
            
            # 获取详情页链接
            detail_url = name_elem.get('href')
            if detail_url and not detail_url.startswith('http'):
                detail_url = GAOKAO_URLS['base'] + detail_url
            
            # 爬取详情页（可选）
            if detail_url:
                detail_data = self.crawl_university_detail(detail_url)
                if detail_data:
                    data.update(detail_data)
            
            return data
            
        except Exception as e:
            logger.error(f"解析院校项失败: {e}")
            return None
    
    def crawl_university_detail(self, url):
        """
        爬取院校详情页
        
        注意：这是示例代码，需要根据实际HTML结构调整
        """
        try:
            time.sleep(CRAWLER_CONFIG['delay'])
            
            response = self.session.get(
                url,
                headers=self.headers,
                timeout=CRAWLER_CONFIG['timeout']
            )
            response.encoding = 'utf-8'
            
            if response.status_code != 200:
                return None
            
            soup = BeautifulSoup(response.text, 'html.parser')
            
            # 解析详情（需根据实际HTML调整）
            detail_data = {}
            
            # 示例：提取城市
            city_elem = soup.select_one('.school-info .city')
            if city_elem:
                detail_data['city'] = city_elem.text.strip()
            
            # 示例：提取简介
            desc_elem = soup.select_one('.school-intro')
            if desc_elem:
                detail_data['description'] = desc_elem.text.strip()[:500]
            
            # 示例：提取官网
            website_elem = soup.select_one('.school-website a')
            if website_elem:
                detail_data['website'] = website_elem.get('href')
            
            return detail_data
            
        except Exception as e:
            logger.error(f"爬取详情页失败: {e}")
            return None


def main():
    """主函数"""
    crawler = UniversityCrawler()
    
    logger.info("=" * 50)
    logger.info("开始爬取阳光高考平台院校数据")
    logger.info("=" * 50)
    
    # 爬取院校列表（最多50页）
    universities = crawler.crawl_university_list(max_pages=50)
    
    logger.info("=" * 50)
    logger.info(f"爬取完成！共获取 {len(universities)} 所院校")
    logger.info("=" * 50)


if __name__ == '__main__':
    main()
