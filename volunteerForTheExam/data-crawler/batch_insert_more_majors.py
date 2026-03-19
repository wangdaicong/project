"""
为更多大学批量添加专业
覆盖50+所重点大学
"""
from api_helper import APIHelper
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 专业模板（与之前相同）
ENGINEERING_MAJORS = [
    {"name": "计算机科学与技术", "category": "工学", "degree": "本科", "duration": 4},
    {"name": "软件工程", "category": "工学", "degree": "本科", "duration": 4},
    {"name": "人工智能", "category": "工学", "degree": "本科", "duration": 4},
    {"name": "数据科学与大数据技术", "category": "工学", "degree": "本科", "duration": 4},
    {"name": "网络空间安全", "category": "工学", "degree": "本科", "duration": 4},
    {"name": "电子信息工程", "category": "工学", "degree": "本科", "duration": 4},
    {"name": "通信工程", "category": "工学", "degree": "本科", "duration": 4},
    {"name": "自动化", "category": "工学", "degree": "本科", "duration": 4},
    {"name": "机械工程", "category": "工学", "degree": "本科", "duration": 4},
    {"name": "土木工程", "category": "工学", "degree": "本科", "duration": 4},
    {"name": "电气工程及其自动化", "category": "工学", "degree": "本科", "duration": 4},
]

SCIENCE_MAJORS = [
    {"name": "数学与应用数学", "category": "理学", "degree": "本科", "duration": 4},
    {"name": "物理学", "category": "理学", "degree": "本科", "duration": 4},
    {"name": "化学", "category": "理学", "degree": "本科", "duration": 4},
    {"name": "生物科学", "category": "理学", "degree": "本科", "duration": 4},
    {"name": "统计学", "category": "理学", "degree": "本科", "duration": 4},
]

ECONOMICS_MAJORS = [
    {"name": "经济学", "category": "经济学", "degree": "本科", "duration": 4},
    {"name": "金融学", "category": "经济学", "degree": "本科", "duration": 4},
    {"name": "国际经济与贸易", "category": "经济学", "degree": "本科", "duration": 4},
]

MANAGEMENT_MAJORS = [
    {"name": "工商管理", "category": "管理学", "degree": "本科", "duration": 4},
    {"name": "会计学", "category": "管理学", "degree": "本科", "duration": 4},
    {"name": "市场营销", "category": "管理学", "degree": "本科", "duration": 4},
    {"name": "财务管理", "category": "管理学", "degree": "本科", "duration": 4},
    {"name": "人力资源管理", "category": "管理学", "degree": "本科", "duration": 4},
]

LITERATURE_MAJORS = [
    {"name": "汉语言文学", "category": "文学", "degree": "本科", "duration": 4},
    {"name": "英语", "category": "文学", "degree": "本科", "duration": 4},
    {"name": "新闻学", "category": "文学", "degree": "本科", "duration": 4},
]

LAW_MAJORS = [
    {"name": "法学", "category": "法学", "degree": "本科", "duration": 4},
]

MEDICAL_MAJORS = [
    {"name": "临床医学", "category": "医学", "degree": "本科", "duration": 5},
    {"name": "药学", "category": "医学", "degree": "本科", "duration": 4},
    {"name": "护理学", "category": "医学", "degree": "本科", "duration": 4},
]

EDUCATION_MAJORS = [
    {"name": "教育学", "category": "教育学", "degree": "本科", "duration": 4},
    {"name": "学前教育", "category": "教育学", "degree": "本科", "duration": 4},
]

# 更多大学列表（ID从17开始，因为前16所已添加）
MORE_UNIVERSITIES = [
    # 985院校（继续）
    (17, "北京师范大学", "师范"),
    (18, "南开大学", "综合"),
    (19, "天津大学", "理工"),
    (20, "哈尔滨工业大学", "理工"),
    (21, "西安交通大学", "综合"),
    (22, "华中科技大学", "综合"),
    (23, "武汉大学", "综合"),
    (24, "中山大学", "综合"),
    (25, "四川大学", "综合"),
    (26, "山东大学", "综合"),
    (27, "厦门大学", "综合"),
    (28, "东南大学", "综合"),
    (29, "同济大学", "理工"),
    (30, "华东师范大学", "师范"),
    
    # 211院校
    (34, "北京交通大学", "理工"),
    (35, "北京科技大学", "理工"),
    (36, "北京化工大学", "理工"),
    (37, "北京邮电大学", "理工"),
    (38, "中国农业大学", "农林"),
    (42, "中央财经大学", "财经"),
    (43, "对外经济贸易大学", "财经"),
    (44, "中国政法大学", "政法"),
    (46, "中国传媒大学", "语言"),
    (57, "华东理工大学", "理工"),
    (58, "东华大学", "理工"),
    (59, "上海财经大学", "财经"),
    (60, "上海大学", "综合"),
    (61, "苏州大学", "综合"),
    (62, "南京航空航天大学", "理工"),
    (63, "南京理工大学", "理工"),
    (65, "河海大学", "理工"),
    (66, "江南大学", "综合"),
    (69, "南京师范大学", "师范"),
    (70, "安徽大学", "综合"),
    (71, "合肥工业大学", "理工"),
    (72, "福州大学", "理工"),
    (73, "南昌大学", "综合"),
    (76, "郑州大学", "综合"),
    (78, "华中师范大学", "师范"),
    (79, "中南财经政法大学", "财经"),
    (80, "湖南师范大学", "师范"),
    (81, "华南理工大学", "理工"),
    (82, "华南师范大学", "师范"),
    (83, "暨南大学", "综合"),
    (86, "西南交通大学", "理工"),
    (87, "西南财经大学", "财经"),
    (88, "西南大学", "综合"),
    (91, "西北大学", "综合"),
    (92, "西安电子科技大学", "理工"),
]


def create_major_data(major_template, university_id):
    """创建专业数据"""
    major = major_template.copy()
    major['university_id'] = university_id
    return major


def main():
    """为更多大学批量添加专业"""
    logger.info("=" * 60)
    logger.info("为更多大学批量添加专业")
    logger.info(f"目标院校数: {len(MORE_UNIVERSITIES)}")
    logger.info("=" * 60)
    
    api = APIHelper()
    
    # 检查后端服务
    if not api.check_backend_status():
        logger.error("后端服务未运行，请先启动后端服务")
        return
    
    majors_to_insert = []
    
    for univ_id, univ_name, univ_type in MORE_UNIVERSITIES:
        logger.info(f"为 {univ_name} ({univ_type}) 准备专业...")
        
        if univ_type == "综合":
            # 综合大学：工学+理学+经济+管理+文学+法学
            templates = (ENGINEERING_MAJORS + SCIENCE_MAJORS + ECONOMICS_MAJORS + 
                        MANAGEMENT_MAJORS + LITERATURE_MAJORS + LAW_MAJORS)
        elif univ_type == "理工":
            # 理工大学：工学+理学+管理
            templates = ENGINEERING_MAJORS + SCIENCE_MAJORS + MANAGEMENT_MAJORS
        elif univ_type == "师范":
            # 师范大学：文学+理学+教育+管理
            templates = LITERATURE_MAJORS + SCIENCE_MAJORS + EDUCATION_MAJORS + MANAGEMENT_MAJORS
        elif univ_type == "财经":
            # 财经大学：经济+管理+法学
            templates = ECONOMICS_MAJORS + MANAGEMENT_MAJORS + LAW_MAJORS
        elif univ_type == "政法":
            # 政法大学：法学+管理
            templates = LAW_MAJORS + MANAGEMENT_MAJORS
        elif univ_type == "农林":
            # 农林大学：理学+管理
            templates = SCIENCE_MAJORS + MANAGEMENT_MAJORS
        elif univ_type == "语言":
            # 语言大学：文学+管理
            templates = LITERATURE_MAJORS + MANAGEMENT_MAJORS
        else:
            templates = MANAGEMENT_MAJORS  # 默认只添加管理类
        
        for major_template in templates:
            major = create_major_data(major_template, univ_id)
            majors_to_insert.append(major)
    
    logger.info(f"共准备插入 {len(majors_to_insert)} 条专业数据")
    
    # 批量导入
    success_count = api.batch_insert_majors(majors_to_insert)
    
    logger.info("=" * 60)
    logger.info(f"批量导入完成！")
    logger.info(f"成功: {success_count}/{len(majors_to_insert)}")
    logger.info("=" * 60)


if __name__ == '__main__':
    main()
