"""
批量导入多个省份的分数线数据
支持一次性导入多个CSV文件
"""
import os
import sys
import logging
from import_scores_from_csv import import_scores_from_csv

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


def batch_import(year, provinces):
    """
    批量导入多个省份的数据
    
    Args:
        year: 年份，如 2023
        provinces: 省份列表，如 ['北京', '上海', '广东']
    """
    logger.info("=" * 60)
    logger.info(f"开始批量导入 {year} 年分数线数据")
    logger.info(f"省份列表: {', '.join(provinces)}")
    logger.info("=" * 60)
    
    total_success = 0
    total_failed = 0
    
    for province in provinces:
        csv_file = f'scores_{year}_{province}.csv'
        
        if os.path.exists(csv_file):
            logger.info(f"\n{'='*60}")
            logger.info(f"正在导入 {province} 数据...")
            logger.info(f"文件: {csv_file}")
            logger.info(f"{'='*60}")
            
            try:
                import_scores_from_csv(csv_file)
                total_success += 1
                logger.info(f"✓ {province} 数据导入成功")
            except Exception as e:
                total_failed += 1
                logger.error(f"✗ {province} 数据导入失败: {e}")
        else:
            total_failed += 1
            logger.warning(f"✗ 未找到文件: {csv_file}")
    
    logger.info("\n" + "=" * 60)
    logger.info("批量导入完成！")
    logger.info(f"成功: {total_success}/{len(provinces)}")
    logger.info(f"失败: {total_failed}/{len(provinces)}")
    logger.info("=" * 60)


def batch_import_all_files(pattern='scores_*.csv'):
    """
    导入当前目录下所有匹配的CSV文件
    
    Args:
        pattern: 文件名模式，默认 'scores_*.csv'
    """
    import glob
    
    csv_files = glob.glob(pattern)
    
    if not csv_files:
        logger.warning(f"未找到匹配的文件: {pattern}")
        return
    
    logger.info("=" * 60)
    logger.info(f"找到 {len(csv_files)} 个CSV文件")
    logger.info("=" * 60)
    
    for csv_file in csv_files:
        logger.info(f"\n正在导入: {csv_file}")
        try:
            import_scores_from_csv(csv_file)
            logger.info(f"✓ {csv_file} 导入成功")
        except Exception as e:
            logger.error(f"✗ {csv_file} 导入失败: {e}")


if __name__ == '__main__':
    # 方式1：指定年份和省份列表
    year = 2023
    provinces = ['北京', '上海', '广东', '浙江', '江苏']
    batch_import(year, provinces)
    
    # 方式2：导入所有CSV文件（取消注释使用）
    # batch_import_all_files('scores_*.csv')
