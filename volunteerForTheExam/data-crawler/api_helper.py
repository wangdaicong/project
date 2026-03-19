"""
通过后端API导入数据
无需直接连接数据库，避免cryptography依赖问题
"""
import requests
import logging

logger = logging.getLogger(__name__)


class APIHelper:
    """后端API调用助手"""
    
    def __init__(self, base_url='http://localhost:8080/api'):
        self.base_url = base_url
        self.session = requests.Session()
    
    def insert_university(self, data):
        """
        通过API插入或更新院校数据
        
        Args:
            data: 院校数据字典
                {
                    'name': '清华大学',
                    'province': '北京',
                    'city': '北京',
                    'level': '985',
                    'type': '综合',
                    'description': '...',
                    'website': 'https://...'
                }
        
        Returns:
            院校ID或None
        """
        try:
            # 调用后端API
            response = self.session.post(
                f'{self.base_url}/university',
                json=data,
                timeout=10
            )
            
            if response.status_code == 200:
                result = response.json()
                if result.get('code') == 200:
                    university_id = result.get('data', {}).get('id')
                    logger.info(f"成功插入院校: {data['name']}, ID: {university_id}")
                    return university_id
                else:
                    logger.warning(f"插入院校失败: {result.get('message')}")
                    return None
            else:
                logger.error(f"API请求失败: {response.status_code}")
                return None
                
        except Exception as e:
            logger.error(f"插入院校失败: {e}")
            return None
    
    def insert_major(self, data):
        """
        通过API插入或更新专业数据
        
        Args:
            data: 专业数据字典
                {
                    'university_id': 1,
                    'name': '计算机科学与技术',
                    'category': '工学',
                    'degree': '本科',
                    'duration': '4年',
                    'description': '...'
                }
        
        Returns:
            专业ID或None
        """
        try:
            response = self.session.post(
                f'{self.base_url}/major',
                json=data,
                timeout=10
            )
            
            if response.status_code == 200:
                result = response.json()
                if result.get('code') == 200:
                    major_id = result.get('data', {}).get('id')
                    logger.info(f"成功插入专业: {data['name']}, ID: {major_id}")
                    return major_id
                else:
                    logger.warning(f"插入专业失败: {result.get('message')}")
                    return None
            else:
                logger.error(f"API请求失败: {response.status_code}")
                return None
                
        except Exception as e:
            logger.error(f"插入专业失败: {e}")
            return None
    
    def batch_insert_universities(self, universities):
        """
        批量插入院校数据
        
        Args:
            universities: 院校数据列表
        
        Returns:
            成功插入的数量
        """
        success_count = 0
        
        for university in universities:
            if self.insert_university(university):
                success_count += 1
        
        logger.info(f"批量插入完成，成功: {success_count}/{len(universities)}")
        return success_count
    
    def batch_insert_majors(self, majors):
        """
        批量插入专业数据
        
        Args:
            majors: 专业数据列表
        
        Returns:
            成功插入的数量
        """
        success_count = 0
        
        for major in majors:
            if self.insert_major(major):
                success_count += 1
        
        logger.info(f"批量插入完成，成功: {success_count}/{len(majors)}")
        return success_count
    
    def check_backend_status(self):
        """
        检查后端服务是否运行
        
        Returns:
            True/False
        """
        try:
            response = self.session.get(
                f'{self.base_url}/university/list',
                params={'pageNum': 1, 'pageSize': 1},
                timeout=5
            )
            
            if response.status_code == 200:
                logger.info("后端服务正常运行")
                return True
            else:
                logger.warning(f"后端服务异常: {response.status_code}")
                return False
                
        except Exception as e:
            logger.error(f"无法连接后端服务: {e}")
            logger.error("请确保后端服务已启动: mvn spring-boot:run")
            return False
    
    def log_sync(self, sync_type, status, message, record_count=0):
        """
        记录同步日志（可选，如果后端提供日志API）
        
        Args:
            sync_type: 同步类型（university/major）
            status: 状态（success/failed）
            message: 消息
            record_count: 记录数量
        """
        logger.info(f"同步日志 - 类型: {sync_type}, 状态: {status}, 消息: {message}, 数量: {record_count}")
