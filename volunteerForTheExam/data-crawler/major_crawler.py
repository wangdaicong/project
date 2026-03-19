"""
专业数据爬虫
爬取阳光高考平台的专业信息
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


class MajorCrawler:
    """专业爬虫"""
    
    def __init__(self):
        self.headers = {
            'User-Agent': CRAWLER_CONFIG['user_agent'],
            'Accept': 'application/json, text/javascript, */*; q=0.01',
            'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8',
        }
        self.db = DBHelper()
        self.session = requests.Session()
    
    def crawl_major_categories(self):
        """
        爬取专业类别和专业列表
        
        注意：这是示例代码，需要根据实际API调整
        """
        logger.info("开始爬取专业数据")
        majors = []
        
        try:
            self.db.connect()
            
            # 专业门类（根据教育部分类）
            categories = [
                '哲学', '经济学', '法学', '教育学', '文学', '历史学',
                '理学', '工学', '农学', '医学', '管理学', '艺术学'
            ]
            
            for category in categories:
                try:
                    # 构造请求（需根据实际API调整）
                    params = {
                        'specialityType': category,
                    }
                    
                    response = self.session.get(
                        GAOKAO_URLS['major_list'],
                        headers=self.headers,
                        params=params,
                        timeout=CRAWLER_CONFIG['timeout']
                    )
                    
                    if response.status_code != 200:
                        logger.warning(f"{category}类专业请求失败")
                        continue
                    
                    # 解析数据（可能是JSON或HTML）
                    data = response.json() if 'application/json' in response.headers.get('Content-Type', '') else None
                    
                    if data:
                        # 处理JSON数据
                        category_majors = self.parse_major_json(data, category)
                    else:
                        # 处理HTML数据
                        soup = BeautifulSoup(response.text, 'html.parser')
                        category_majors = self.parse_major_html(soup, category)
                    
                    majors.extend(category_majors)
                    logger.info(f"{category}类专业爬取完成，共{len(category_majors)}个")
                    
                    time.sleep(CRAWLER_CONFIG['delay'])
                    
                except Exception as e:
                    logger.error(f"爬取{category}类专业失败: {e}")
                    continue
            
            self.db.log_sync('major', 'success', f'成功爬取{len(majors)}个专业', len(majors))
            logger.info(f"专业数据爬取完成，共{len(majors)}个")
            
        except Exception as e:
            logger.error(f"爬取失败: {e}")
            self.db.log_sync('major', 'failed', str(e), 0)
        finally:
            self.db.close()
        
        return majors
    
    def parse_major_json(self, data, category):
        """解析JSON格式的专业数据"""
        majors = []
        
        try:
            # 示例：根据实际JSON结构调整
            items = data.get('items', [])
            
            for item in items:
                major_data = {
                    'name': item.get('name'),
                    'category': category,
                    'code': item.get('code'),
                    'degree': item.get('degree', '本科'),
                    'duration': item.get('duration', '4年'),
                    'description': item.get('description'),
                }
                majors.append(major_data)
                
        except Exception as e:
            logger.error(f"解析专业JSON失败: {e}")
        
        return majors
    
    def parse_major_html(self, soup, category):
        """解析HTML格式的专业数据"""
        majors = []
        
        try:
            # 示例：根据实际HTML结构调整
            items = soup.select('.major-item')
            
            for item in items:
                name_elem = item.select_one('.major-name')
                if not name_elem:
                    continue
                
                major_data = {
                    'name': name_elem.text.strip(),
                    'category': category,
                    'degree': '本科',
                    'duration': '4年',
                    'description': None,
                }
                majors.append(major_data)
                
        except Exception as e:
            logger.error(f"解析专业HTML失败: {e}")
        
        return majors


def main():
    """主函数"""
    crawler = MajorCrawler()
    
    logger.info("=" * 50)
    logger.info("开始爬取专业数据")
    logger.info("=" * 50)
    
    majors = crawler.crawl_major_categories()
    
    logger.info("=" * 50)
    logger.info(f"爬取完成！共获取 {len(majors)} 个专业")
    logger.info("=" * 50)


if __name__ == '__main__':
    main()
