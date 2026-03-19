"""
阳光高考平台全自动爬虫
从 https://gaokao.chsi.com.cn/ 获取所有数据
包括：院校信息、专业信息、历年分数线
"""
import requests
from bs4 import BeautifulSoup
import pymysql
import logging
import time
import random
import json
import re
from urllib.parse import urljoin

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('gaokao_crawler.log', encoding='utf-8'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)


class GaoKaoChsiCrawler:
    """阳光高考平台爬虫"""
    
    def __init__(self):
        self.base_url = "https://gaokao.chsi.com.cn"
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Language': 'zh-CN,zh;q=0.9',
            'Referer': 'https://gaokao.chsi.com.cn/',
        }
        self.session = requests.Session()
        self.session.headers.update(self.headers)
        
        # 数据库连接配置
        self.db_config = {
            'host': 'localhost',
            'port': 3306,
            'user': 'root',
            'password': 'root',
            'database': 'volunteer_exam',
            'charset': 'utf8mb4'
        }
    
    def get_db_connection(self):
        """获取数据库连接"""
        return pymysql.connect(**self.db_config)
    
    def crawl_universities(self):
        """
        爬取所有院校信息
        阳光高考平台院校库：https://gaokao.chsi.com.cn/sch/
        """
        logger.info("=" * 60)
        logger.info("开始爬取院校信息")
        logger.info("=" * 60)
        
        universities = []
        
        try:
            # 阳光高考院校库API（可能需要根据实际调整）
            # 方式1：通过搜索接口获取
            search_url = f"{self.base_url}/sch/search"
            
            # 方式2：通过院校列表页获取
            list_url = f"{self.base_url}/sch/schoolList.do"
            
            # 尝试获取院校列表
            for page in range(1, 100):  # 假设最多100页
                logger.info(f"正在爬取第 {page} 页...")
                
                params = {
                    'page': page,
                    'size': 50
                }
                
                time.sleep(random.uniform(2, 4))
                
                try:
                    response = self.session.get(list_url, params=params, timeout=30)
                    
                    if response.status_code != 200:
                        logger.warning(f"第{page}页请求失败")
                        break
                    
                    # 解析JSON或HTML
                    if 'application/json' in response.headers.get('Content-Type', ''):
                        data = response.json()
                        if 'data' in data and data['data']:
                            for item in data['data']:
                                universities.append({
                                    'name': item.get('name'),
                                    'province': item.get('province'),
                                    'city': item.get('city'),
                                    'level': self._parse_level(item.get('tags', [])),
                                    'type': item.get('type'),
                                    'website': item.get('website'),
                                    'code': item.get('code'),
                                })
                        else:
                            break
                    else:
                        # 解析HTML
                        soup = BeautifulSoup(response.text, 'html.parser')
                        school_items = soup.find_all('div', class_='school-item')
                        
                        if not school_items:
                            break
                        
                        for item in school_items:
                            name = item.find('h3', class_='school-name')
                            if name:
                                universities.append({
                                    'name': name.get_text(strip=True),
                                    'province': self._extract_province(item),
                                    'city': self._extract_city(item),
                                    'level': self._extract_level(item),
                                    'type': self._extract_type(item),
                                })
                
                except Exception as e:
                    logger.error(f"第{page}页爬取失败: {e}")
                    break
            
            logger.info(f"成功爬取 {len(universities)} 所院校")
            return universities
            
        except Exception as e:
            logger.error(f"爬取院校失败: {e}")
            return []
    
    def crawl_university_detail(self, university_name):
        """
        爬取单个院校详细信息
        包括：简介、专业、历年分数线
        """
        logger.info(f"爬取院校详情: {university_name}")
        
        try:
            # 搜索院校
            search_url = f"{self.base_url}/sch/search.do"
            params = {'searchType': 'school', 'keyword': university_name}
            
            time.sleep(random.uniform(1, 3))
            response = self.session.get(search_url, params=params, timeout=30)
            
            if response.status_code == 200:
                soup = BeautifulSoup(response.text, 'html.parser')
                
                # 提取院校详情页链接
                detail_link = soup.find('a', class_='school-link')
                if detail_link:
                    detail_url = urljoin(self.base_url, detail_link['href'])
                    
                    # 访问详情页
                    time.sleep(random.uniform(1, 3))
                    detail_response = self.session.get(detail_url, timeout=30)
                    detail_soup = BeautifulSoup(detail_response.text, 'html.parser')
                    
                    # 提取详细信息
                    detail = {
                        'introduction': self._extract_introduction(detail_soup),
                        'ranking': self._extract_ranking(detail_soup),
                        'website': self._extract_website(detail_soup),
                    }
                    
                    return detail
            
            return None
            
        except Exception as e:
            logger.error(f"爬取院校详情失败: {e}")
            return None
    
    def crawl_admission_scores(self, year=2023):
        """
        爬取历年录取分数线
        阳光高考分数线查询：https://gaokao.chsi.com.cn/zsgs/
        """
        logger.info("=" * 60)
        logger.info(f"开始爬取 {year} 年录取分数线")
        logger.info("=" * 60)
        
        scores = []
        
        try:
            # 分数线查询接口
            score_url = f"{self.base_url}/zsgs/queryScore.do"
            
            # 获取所有省份
            provinces = ['北京', '上海', '广东', '浙江', '江苏', '山东', '河南', 
                        '湖北', '湖南', '四川', '重庆', '天津', '河北', '山西',
                        '辽宁', '吉林', '黑龙江', '安徽', '福建', '江西', '广西',
                        '海南', '贵州', '云南', '陕西', '甘肃', '青海', '宁夏', '新疆']
            
            for province in provinces:
                logger.info(f"正在爬取 {province} 分数线...")
                
                params = {
                    'year': year,
                    'province': province,
                    'batch': '本科一批'
                }
                
                time.sleep(random.uniform(2, 4))
                
                try:
                    response = self.session.get(score_url, params=params, timeout=30)
                    
                    if response.status_code == 200:
                        # 解析数据
                        if 'application/json' in response.headers.get('Content-Type', ''):
                            data = response.json()
                            if 'data' in data:
                                for item in data['data']:
                                    scores.append({
                                        'university_name': item.get('schoolName'),
                                        'year': year,
                                        'province': province,
                                        'batch': item.get('batch', '本科一批'),
                                        'min_score': item.get('minScore'),
                                        'avg_score': item.get('avgScore'),
                                        'max_score': item.get('maxScore'),
                                        'enrollment_number': item.get('enrollment'),
                                    })
                        else:
                            # 解析HTML表格
                            soup = BeautifulSoup(response.text, 'html.parser')
                            table = soup.find('table', class_='score-table')
                            if table:
                                rows = table.find_all('tr')[1:]  # 跳过表头
                                for row in rows:
                                    cols = row.find_all('td')
                                    if len(cols) >= 4:
                                        scores.append({
                                            'university_name': cols[0].get_text(strip=True),
                                            'year': year,
                                            'province': province,
                                            'batch': cols[1].get_text(strip=True),
                                            'min_score': self._extract_number(cols[2].get_text()),
                                            'max_score': self._extract_number(cols[3].get_text()),
                                        })
                
                except Exception as e:
                    logger.error(f"{province} 爬取失败: {e}")
                    continue
            
            logger.info(f"成功爬取 {len(scores)} 条分数线")
            return scores
            
        except Exception as e:
            logger.error(f"爬取分数线失败: {e}")
            return []
    
    def save_universities(self, universities):
        """保存院校信息到数据库"""
        if not universities:
            return
        
        logger.info(f"开始保存 {len(universities)} 所院校...")
        
        conn = self.get_db_connection()
        cursor = conn.cursor()
        
        success_count = 0
        
        for univ in universities:
            try:
                # 检查是否已存在
                cursor.execute("SELECT id FROM university WHERE name = %s", (univ['name'],))
                if cursor.fetchone():
                    # 更新
                    cursor.execute("""
                        UPDATE university 
                        SET province = %s, city = %s, level = %s, type = %s
                        WHERE name = %s
                    """, (univ.get('province'), univ.get('city'), 
                          univ.get('level'), univ.get('type'), univ['name']))
                else:
                    # 插入
                    cursor.execute("""
                        INSERT INTO university (name, province, city, level, type)
                        VALUES (%s, %s, %s, %s, %s)
                    """, (univ['name'], univ.get('province'), univ.get('city'),
                          univ.get('level'), univ.get('type')))
                
                success_count += 1
                
            except Exception as e:
                logger.error(f"保存院校失败 {univ['name']}: {e}")
        
        conn.commit()
        cursor.close()
        conn.close()
        
        logger.info(f"✓ 成功保存 {success_count} 所院校")
    
    def save_scores(self, scores):
        """保存分数线到数据库"""
        if not scores:
            return
        
        logger.info(f"开始保存 {len(scores)} 条分数线...")
        
        conn = self.get_db_connection()
        cursor = conn.cursor()
        
        success_count = 0
        
        for score in scores:
            try:
                # 查询院校ID
                cursor.execute("SELECT id FROM university WHERE name = %s", 
                             (score['university_name'],))
                result = cursor.fetchone()
                
                if not result:
                    continue
                
                university_id = result[0]
                
                # 检查是否已存在
                cursor.execute("""
                    SELECT id FROM admission_record 
                    WHERE university_id = %s AND year = %s AND province = %s
                """, (university_id, score['year'], score['province']))
                
                if cursor.fetchone():
                    # 更新
                    cursor.execute("""
                        UPDATE admission_record 
                        SET min_score = %s, avg_score = %s, max_score = %s, 
                            enrollment_number = %s
                        WHERE university_id = %s AND year = %s AND province = %s
                    """, (score.get('min_score'), score.get('avg_score'), 
                          score.get('max_score'), score.get('enrollment_number'),
                          university_id, score['year'], score['province']))
                else:
                    # 插入
                    cursor.execute("""
                        INSERT INTO admission_record 
                        (university_id, year, province, batch, min_score, 
                         avg_score, max_score, enrollment_number)
                        VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                    """, (university_id, score['year'], score['province'],
                          score.get('batch'), score.get('min_score'),
                          score.get('avg_score'), score.get('max_score'),
                          score.get('enrollment_number')))
                
                success_count += 1
                
            except Exception as e:
                logger.error(f"保存分数线失败: {e}")
        
        conn.commit()
        cursor.close()
        conn.close()
        
        logger.info(f"✓ 成功保存 {success_count} 条分数线")
    
    # 辅助方法
    def _parse_level(self, tags):
        """解析院校层次"""
        level_tags = []
        if '985' in str(tags):
            level_tags.append('985')
        if '211' in str(tags):
            level_tags.append('211')
        if '双一流' in str(tags):
            level_tags.append('双一流')
        return '/'.join(level_tags) if level_tags else '普通本科'
    
    def _extract_number(self, text):
        """从文本提取数字"""
        numbers = re.findall(r'\d+', str(text))
        return int(numbers[0]) if numbers else None
    
    def _extract_province(self, item):
        """提取省份"""
        province_elem = item.find(class_='province')
        return province_elem.get_text(strip=True) if province_elem else None
    
    def _extract_city(self, item):
        """提取城市"""
        city_elem = item.find(class_='city')
        return city_elem.get_text(strip=True) if city_elem else None
    
    def _extract_level(self, item):
        """提取层次"""
        tags = item.find_all(class_='tag')
        levels = []
        for tag in tags:
            text = tag.get_text(strip=True)
            if text in ['985', '211', '双一流']:
                levels.append(text)
        return '/'.join(levels) if levels else '普通本科'
    
    def _extract_type(self, item):
        """提取类型"""
        type_elem = item.find(class_='type')
        return type_elem.get_text(strip=True) if type_elem else None
    
    def _extract_introduction(self, soup):
        """提取简介"""
        intro = soup.find(class_='introduction')
        return intro.get_text(strip=True) if intro else None
    
    def _extract_ranking(self, soup):
        """提取排名"""
        ranking = soup.find(class_='ranking')
        if ranking:
            return self._extract_number(ranking.get_text())
        return None
    
    def _extract_website(self, soup):
        """提取官网"""
        website = soup.find('a', class_='website')
        return website['href'] if website else None
    
    def run_full_update(self):
        """执行完整更新"""
        logger.info("=" * 60)
        logger.info("阳光高考平台全自动更新启动")
        logger.info("=" * 60)
        
        # 1. 爬取院校信息
        universities = self.crawl_universities()
        if universities:
            self.save_universities(universities)
        
        # 2. 爬取分数线
        for year in [2023, 2022, 2021]:
            scores = self.crawl_admission_scores(year)
            if scores:
                self.save_scores(scores)
        
        logger.info("=" * 60)
        logger.info("全自动更新完成！")
        logger.info("=" * 60)


def main():
    """主函数"""
    crawler = GaoKaoChsiCrawler()
    crawler.run_full_update()


if __name__ == '__main__':
    main()
