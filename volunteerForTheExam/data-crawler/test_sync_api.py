"""
测试数据同步功能 - API版本
通过后端API导入数据，无需数据库连接
"""
import logging
from university_crawler_api import UniversityCrawlerAPI
from api_helper import APIHelper

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


def test_backend_connection():
    """测试后端连接"""
    logger.info("=" * 60)
    logger.info("测试后端服务连接")
    logger.info("=" * 60)
    
    api = APIHelper()
    
    if api.check_backend_status():
        logger.info("✅ 后端服务正常运行")
        return True
    else:
        logger.error("❌ 后端服务未运行")
        logger.error("")
        logger.error("请先启动后端服务:")
        logger.error("  cd E:\\AiProject\\project\\volunteerForTheExam\\backend")
        logger.error("  mvn spring-boot:run")
        logger.error("")
        return False


def test_university_sync_api():
    """测试院校数据同步 - API版本"""
    logger.info("=" * 60)
    logger.info("开始测试院校数据同步（API版本）")
    logger.info("=" * 60)
    
    try:
        crawler = UniversityCrawlerAPI()
        # 只爬取前2页进行测试
        universities = crawler.crawl_university_list(max_pages=2)
        
        logger.info("=" * 60)
        logger.info(f"✅ 院校数据同步测试完成")
        logger.info(f"共爬取 {len(universities)} 所院校")
        logger.info(f"数据已通过API保存到数据库")
        logger.info("=" * 60)
        
        return len(universities) > 0
        
    except Exception as e:
        logger.error(f"❌ 院校数据同步测试失败: {e}")
        return False


def test_manual_insert():
    """测试手动插入数据"""
    logger.info("=" * 60)
    logger.info("测试手动插入数据")
    logger.info("=" * 60)
    
    api = APIHelper()
    
    # 测试插入一所院校
    test_university = {
        'name': '测试大学',
        'province': '北京',
        'city': '北京',
        'level': '本科',
        'type': '综合',
        'description': '这是一所测试大学',
        'website': 'https://test.edu.cn'
    }
    
    university_id = api.insert_university(test_university)
    
    if university_id:
        logger.info(f"✅ 成功插入测试院校，ID: {university_id}")
        return True
    else:
        logger.error("❌ 插入测试院校失败")
        return False


def main():
    """主测试函数"""
    logger.info("\n" + "=" * 60)
    logger.info("数据同步功能测试（API版本）")
    logger.info("=" * 60 + "\n")
    
    # 1. 测试后端连接
    backend_ok = test_backend_connection()
    
    if not backend_ok:
        print("\n" + "=" * 60)
        print("测试结果总结")
        print("=" * 60)
        print("❌ 后端服务未运行，无法继续测试")
        print("\n请先启动后端服务，然后重新运行此测试")
        print("=" * 60)
        return
    
    print("\n")
    
    # 2. 测试手动插入
    manual_ok = test_manual_insert()
    
    print("\n")
    
    # 3. 测试爬虫同步
    sync_ok = test_university_sync_api()
    
    # 总结
    print("\n" + "=" * 60)
    print("测试结果总结")
    print("=" * 60)
    print(f"后端服务连接: {'✅ 成功' if backend_ok else '❌ 失败'}")
    print(f"手动插入测试: {'✅ 成功' if manual_ok else '❌ 失败'}")
    print(f"爬虫同步测试: {'✅ 成功' if sync_ok else '❌ 失败'}")
    print("=" * 60)
    
    if backend_ok and (manual_ok or sync_ok):
        print("\n✅ 测试通过！数据可以通过API成功导入")
        print("\n📝 下一步:")
        print("1. 查询数据库验证数据")
        print("2. 调整爬虫的CSS选择器以匹配实际网站")
        print("3. 扩大爬取范围")
    else:
        print("\n⚠️ 部分测试失败")
        print("\n📝 可能的原因:")
        print("1. 后端服务未启动")
        print("2. 网站结构变化，需要调整CSS选择器")
        print("3. 网络问题")


if __name__ == '__main__':
    main()
