"""
测试数据同步功能
手动执行一次数据同步，验证爬虫是否正常工作
"""
import logging
from university_crawler import UniversityCrawler
from major_crawler import MajorCrawler

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


def test_university_sync():
    """测试院校数据同步"""
    logger.info("=" * 60)
    logger.info("开始测试院校数据同步")
    logger.info("=" * 60)
    
    try:
        crawler = UniversityCrawler()
        # 只爬取前5页进行测试
        universities = crawler.crawl_university_list(max_pages=5)
        
        logger.info("=" * 60)
        logger.info(f"✅ 院校数据同步测试完成")
        logger.info(f"共爬取 {len(universities)} 所院校")
        logger.info("=" * 60)
        
        return len(universities) > 0
        
    except Exception as e:
        logger.error(f"❌ 院校数据同步测试失败: {e}")
        return False


def test_major_sync():
    """测试专业数据同步"""
    logger.info("=" * 60)
    logger.info("开始测试专业数据同步")
    logger.info("=" * 60)
    
    try:
        crawler = MajorCrawler()
        majors = crawler.crawl_major_categories()
        
        logger.info("=" * 60)
        logger.info(f"✅ 专业数据同步测试完成")
        logger.info(f"共爬取 {len(majors)} 个专业")
        logger.info("=" * 60)
        
        return len(majors) > 0
        
    except Exception as e:
        logger.error(f"❌ 专业数据同步测试失败: {e}")
        return False


def main():
    """主测试函数"""
    logger.info("\n" + "=" * 60)
    logger.info("数据同步功能测试")
    logger.info("=" * 60 + "\n")
    
    # 测试院校同步
    university_success = test_university_sync()
    
    print("\n")
    
    # 测试专业同步
    major_success = test_major_sync()
    
    # 总结
    print("\n" + "=" * 60)
    print("测试结果总结")
    print("=" * 60)
    print(f"院校数据同步: {'✅ 成功' if university_success else '❌ 失败'}")
    print(f"专业数据同步: {'✅ 成功' if major_success else '❌ 失败'}")
    print("=" * 60)
    
    if university_success or major_success:
        print("\n✅ 至少有一个功能正常工作")
        print("\n📝 下一步:")
        print("1. 检查数据库中的数据是否正确导入")
        print("2. 如果爬取失败，需要调整CSS选择器")
        print("3. 查看详细日志: logs/crawler.log")
    else:
        print("\n❌ 所有功能都失败了")
        print("\n📝 可能的原因:")
        print("1. 数据库连接失败 - 检查.env配置")
        print("2. 网站结构变化 - 需要更新CSS选择器")
        print("3. 网络问题 - 检查是否能访问 https://gaokao.chsi.com.cn/")
        print("4. 反爬虫限制 - 增加请求间隔")


if __name__ == '__main__':
    main()
