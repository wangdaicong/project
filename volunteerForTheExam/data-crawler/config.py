"""
配置文件
"""
import os
from dotenv import load_dotenv

load_dotenv()

# 数据库配置
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'port': int(os.getenv('DB_PORT', 3306)),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD', ''),
    'database': os.getenv('DB_NAME', 'volunteer_exam'),
    'charset': 'utf8mb4'
}

# 爬虫配置
CRAWLER_CONFIG = {
    'user_agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    'timeout': 10,
    'retry_times': 3,
    'delay': 2,  # 请求间隔（秒）
}

# 阳光高考平台URL
GAOKAO_URLS = {
    'base': 'https://gaokao.chsi.com.cn',
    'school_list': 'https://gaokao.chsi.com.cn/sch/search.do',
    'school_detail': 'https://gaokao.chsi.com.cn/sch/schoolInfo--schId-{}.dhtml',
    'major_list': 'https://gaokao.chsi.com.cn/zyk/zybk/specialityesList.action',
}

# 日志配置
LOG_CONFIG = {
    'level': 'INFO',
    'format': '%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    'file': 'logs/crawler.log'
}
