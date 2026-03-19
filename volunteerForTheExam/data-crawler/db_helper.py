"""
数据库操作助手
"""
import pymysql
import logging
from config import DB_CONFIG

logger = logging.getLogger(__name__)


class DBHelper:
    """数据库操作类"""
    
    def __init__(self):
        self.conn = None
        self.cursor = None
    
    def connect(self):
        """连接数据库"""
        try:
            self.conn = pymysql.connect(**DB_CONFIG)
            self.cursor = self.conn.cursor(pymysql.cursors.DictCursor)
            logger.info("数据库连接成功")
        except Exception as e:
            logger.error(f"数据库连接失败: {e}")
            raise
    
    def close(self):
        """关闭连接"""
        if self.cursor:
            self.cursor.close()
        if self.conn:
            self.conn.close()
        logger.info("数据库连接已关闭")
    
    def insert_university(self, data):
        """插入或更新院校数据"""
        try:
            # 检查是否已存在
            check_sql = "SELECT id FROM university WHERE name = %s"
            self.cursor.execute(check_sql, (data['name'],))
            existing = self.cursor.fetchone()
            
            if existing:
                # 更新
                update_sql = """
                    UPDATE university SET
                        province = %s,
                        city = %s,
                        level = %s,
                        type = %s,
                        description = %s,
                        website = %s,
                        updated_at = NOW()
                    WHERE id = %s
                """
                self.cursor.execute(update_sql, (
                    data.get('province'),
                    data.get('city'),
                    data.get('level'),
                    data.get('type'),
                    data.get('description'),
                    data.get('website'),
                    existing['id']
                ))
                logger.info(f"更新院校: {data['name']}")
                return existing['id']
            else:
                # 插入
                insert_sql = """
                    INSERT INTO university (name, province, city, level, type, description, website, created_at, updated_at)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
                """
                self.cursor.execute(insert_sql, (
                    data['name'],
                    data.get('province'),
                    data.get('city'),
                    data.get('level'),
                    data.get('type'),
                    data.get('description'),
                    data.get('website')
                ))
                self.conn.commit()
                logger.info(f"插入院校: {data['name']}")
                return self.cursor.lastrowid
        except Exception as e:
            self.conn.rollback()
            logger.error(f"插入院校失败: {e}")
            raise
    
    def insert_major(self, data):
        """插入或更新专业数据"""
        try:
            check_sql = "SELECT id FROM major WHERE university_id = %s AND name = %s"
            self.cursor.execute(check_sql, (data['university_id'], data['name']))
            existing = self.cursor.fetchone()
            
            if existing:
                update_sql = """
                    UPDATE major SET
                        category = %s,
                        degree = %s,
                        duration = %s,
                        description = %s,
                        updated_at = NOW()
                    WHERE id = %s
                """
                self.cursor.execute(update_sql, (
                    data.get('category'),
                    data.get('degree'),
                    data.get('duration'),
                    data.get('description'),
                    existing['id']
                ))
                logger.info(f"更新专业: {data['name']}")
                return existing['id']
            else:
                insert_sql = """
                    INSERT INTO major (university_id, name, category, degree, duration, description, created_at, updated_at)
                    VALUES (%s, %s, %s, %s, %s, %s, NOW(), NOW())
                """
                self.cursor.execute(insert_sql, (
                    data['university_id'],
                    data['name'],
                    data.get('category'),
                    data.get('degree'),
                    data.get('duration'),
                    data.get('description')
                ))
                self.conn.commit()
                logger.info(f"插入专业: {data['name']}")
                return self.cursor.lastrowid
        except Exception as e:
            self.conn.rollback()
            logger.error(f"插入专业失败: {e}")
            raise
    
    def insert_ranking(self, data):
        """插入院校排名数据"""
        try:
            check_sql = "SELECT id FROM university_ranking WHERE university_id = %s AND year = %s AND ranking_type = %s"
            self.cursor.execute(check_sql, (data['university_id'], data['year'], data['ranking_type']))
            existing = self.cursor.fetchone()
            
            if existing:
                update_sql = """
                    UPDATE university_ranking SET
                        ranking = %s,
                        score = %s,
                        updated_at = NOW()
                    WHERE id = %s
                """
                self.cursor.execute(update_sql, (
                    data['ranking'],
                    data.get('score'),
                    existing['id']
                ))
            else:
                insert_sql = """
                    INSERT INTO university_ranking (university_id, year, ranking_type, ranking, score, created_at, updated_at)
                    VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
                """
                self.cursor.execute(insert_sql, (
                    data['university_id'],
                    data['year'],
                    data['ranking_type'],
                    data['ranking'],
                    data.get('score')
                ))
            self.conn.commit()
            logger.info(f"插入排名数据: {data['ranking_type']} - {data['year']}")
        except Exception as e:
            self.conn.rollback()
            logger.error(f"插入排名失败: {e}")
            raise
    
    def log_sync(self, sync_type, status, message, record_count=0):
        """记录同步日志"""
        try:
            sql = """
                INSERT INTO sync_log (sync_type, status, message, record_count, created_at)
                VALUES (%s, %s, %s, %s, NOW())
            """
            self.cursor.execute(sql, (sync_type, status, message, record_count))
            self.conn.commit()
        except Exception as e:
            logger.error(f"记录同步日志失败: {e}")
