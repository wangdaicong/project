"""
批量插入真实专业数据
为各大学添加常见专业
"""
from api_helper import APIHelper
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 常见专业数据（按学科门类分类）
# 注意：university_id 需要对应数据库中的实际ID

# 工学类专业
ENGINEERING_MAJORS = [
    {"name": "计算机科学与技术", "category": "工学", "degree": "本科", "duration": 4, "description": "培养计算机科学与技术领域的高级专门人才"},
    {"name": "软件工程", "category": "工学", "degree": "本科", "duration": 4, "description": "培养软件开发、测试、维护等方面的专业人才"},
    {"name": "人工智能", "category": "工学", "degree": "本科", "duration": 4, "description": "培养人工智能理论与应用的专业人才"},
    {"name": "数据科学与大数据技术", "category": "工学", "degree": "本科", "duration": 4, "description": "培养大数据分析与应用的专业人才"},
    {"name": "网络空间安全", "category": "工学", "degree": "本科", "duration": 4, "description": "培养网络安全防护与管理的专业人才"},
    {"name": "电子信息工程", "category": "工学", "degree": "本科", "duration": 4, "description": "培养电子信息系统设计与应用的专业人才"},
    {"name": "通信工程", "category": "工学", "degree": "本科", "duration": 4, "description": "培养通信技术研发与应用的专业人才"},
    {"name": "自动化", "category": "工学", "degree": "本科", "duration": 4, "description": "培养自动控制系统设计与应用的专业人才"},
    {"name": "机械工程", "category": "工学", "degree": "本科", "duration": 4, "description": "培养机械设计制造及自动化的专业人才"},
    {"name": "车辆工程", "category": "工学", "degree": "本科", "duration": 4, "description": "培养汽车设计制造与研发的专业人才"},
    {"name": "土木工程", "category": "工学", "degree": "本科", "duration": 4, "description": "培养建筑工程设计与施工的专业人才"},
    {"name": "建筑学", "category": "工学", "degree": "本科", "duration": 5, "description": "培养建筑设计与规划的专业人才"},
    {"name": "电气工程及其自动化", "category": "工学", "degree": "本科", "duration": 4, "description": "培养电力系统及自动化的专业人才"},
    {"name": "航空航天工程", "category": "工学", "degree": "本科", "duration": 4, "description": "培养航空航天器设计与制造的专业人才"},
    {"name": "材料科学与工程", "category": "工学", "degree": "本科", "duration": 4, "description": "培养新材料研发与应用的专业人才"},
    {"name": "化学工程与工艺", "category": "工学", "degree": "本科", "duration": 4, "description": "培养化工生产与工艺设计的专业人才"},
    {"name": "生物工程", "category": "工学", "degree": "本科", "duration": 4, "description": "培养生物技术应用与研发的专业人才"},
    {"name": "环境工程", "category": "工学", "degree": "本科", "duration": 4, "description": "培养环境保护与治理的专业人才"},
]

# 理学类专业
SCIENCE_MAJORS = [
    {"name": "数学与应用数学", "category": "理学", "degree": "本科", "duration": 4, "description": "培养数学理论与应用的专业人才"},
    {"name": "信息与计算科学", "category": "理学", "degree": "本科", "duration": 4, "description": "培养科学计算与信息处理的专业人才"},
    {"name": "物理学", "category": "理学", "degree": "本科", "duration": 4, "description": "培养物理学理论与实验的专业人才"},
    {"name": "应用物理学", "category": "理学", "degree": "本科", "duration": 4, "description": "培养物理学应用与技术开发的专业人才"},
    {"name": "化学", "category": "理学", "degree": "本科", "duration": 4, "description": "培养化学理论与实验的专业人才"},
    {"name": "应用化学", "category": "理学", "degree": "本科", "duration": 4, "description": "培养化学应用与技术开发的专业人才"},
    {"name": "生物科学", "category": "理学", "degree": "本科", "duration": 4, "description": "培养生物学理论与实验的专业人才"},
    {"name": "生物技术", "category": "理学", "degree": "本科", "duration": 4, "description": "培养生物技术应用与研发的专业人才"},
    {"name": "统计学", "category": "理学", "degree": "本科", "duration": 4, "description": "培养统计分析与数据处理的专业人才"},
]

# 经济学类专业
ECONOMICS_MAJORS = [
    {"name": "经济学", "category": "经济学", "degree": "本科", "duration": 4, "description": "培养经济理论与政策分析的专业人才"},
    {"name": "金融学", "category": "经济学", "degree": "本科", "duration": 4, "description": "培养金融理论与实务的专业人才"},
    {"name": "国际经济与贸易", "category": "经济学", "degree": "本科", "duration": 4, "description": "培养国际贸易与经济合作的专业人才"},
    {"name": "财政学", "category": "经济学", "degree": "本科", "duration": 4, "description": "培养财政税收理论与实务的专业人才"},
    {"name": "金融工程", "category": "经济学", "degree": "本科", "duration": 4, "description": "培养金融产品设计与风险管理的专业人才"},
]

# 管理学类专业
MANAGEMENT_MAJORS = [
    {"name": "工商管理", "category": "管理学", "degree": "本科", "duration": 4, "description": "培养企业管理与运营的专业人才"},
    {"name": "市场营销", "category": "管理学", "degree": "本科", "duration": 4, "description": "培养市场分析与营销策划的专业人才"},
    {"name": "会计学", "category": "管理学", "degree": "本科", "duration": 4, "description": "培养会计核算与财务管理的专业人才"},
    {"name": "财务管理", "category": "管理学", "degree": "本科", "duration": 4, "description": "培养企业财务决策与管理的专业人才"},
    {"name": "人力资源管理", "category": "管理学", "degree": "本科", "duration": 4, "description": "培养人力资源开发与管理的专业人才"},
    {"name": "行政管理", "category": "管理学", "degree": "本科", "duration": 4, "description": "培养公共行政管理的专业人才"},
    {"name": "物流管理", "category": "管理学", "degree": "本科", "duration": 4, "description": "培养物流系统规划与管理的专业人才"},
    {"name": "电子商务", "category": "管理学", "degree": "本科", "duration": 4, "description": "培养电子商务运营与管理的专业人才"},
    {"name": "信息管理与信息系统", "category": "管理学", "degree": "本科", "duration": 4, "description": "培养信息系统开发与管理的专业人才"},
]

# 文学类专业
LITERATURE_MAJORS = [
    {"name": "汉语言文学", "category": "文学", "degree": "本科", "duration": 4, "description": "培养语言文学研究与教学的专业人才"},
    {"name": "英语", "category": "文学", "degree": "本科", "duration": 4, "description": "培养英语语言文学与翻译的专业人才"},
    {"name": "新闻学", "category": "文学", "degree": "本科", "duration": 4, "description": "培养新闻采编与传播的专业人才"},
    {"name": "广告学", "category": "文学", "degree": "本科", "duration": 4, "description": "培养广告策划与创意的专业人才"},
    {"name": "传播学", "category": "文学", "degree": "本科", "duration": 4, "description": "培养媒体传播与公关的专业人才"},
]

# 法学类专业
LAW_MAJORS = [
    {"name": "法学", "category": "法学", "degree": "本科", "duration": 4, "description": "培养法律理论与实务的专业人才"},
    {"name": "知识产权", "category": "法学", "degree": "本科", "duration": 4, "description": "培养知识产权保护与管理的专业人才"},
    {"name": "社会学", "category": "法学", "degree": "本科", "duration": 4, "description": "培养社会调查与分析的专业人才"},
    {"name": "政治学与行政学", "category": "法学", "degree": "本科", "duration": 4, "description": "培养政治理论与公共管理的专业人才"},
]

# 医学类专业
MEDICAL_MAJORS = [
    {"name": "临床医学", "category": "医学", "degree": "本科", "duration": 5, "description": "培养临床诊疗的专业医师"},
    {"name": "口腔医学", "category": "医学", "degree": "本科", "duration": 5, "description": "培养口腔疾病诊疗的专业医师"},
    {"name": "预防医学", "category": "医学", "degree": "本科", "duration": 5, "description": "培养疾病预防与公共卫生的专业人才"},
    {"name": "中医学", "category": "医学", "degree": "本科", "duration": 5, "description": "培养中医诊疗的专业医师"},
    {"name": "药学", "category": "医学", "degree": "本科", "duration": 4, "description": "培养药物研发与应用的专业人才"},
    {"name": "护理学", "category": "医学", "degree": "本科", "duration": 4, "description": "培养临床护理的专业人才"},
]

# 教育学类专业
EDUCATION_MAJORS = [
    {"name": "教育学", "category": "教育学", "degree": "本科", "duration": 4, "description": "培养教育理论与管理的专业人才"},
    {"name": "学前教育", "category": "教育学", "degree": "本科", "duration": 4, "description": "培养幼儿教育的专业人才"},
    {"name": "小学教育", "category": "教育学", "degree": "本科", "duration": 4, "description": "培养小学教育的专业人才"},
    {"name": "体育教育", "category": "教育学", "degree": "本科", "duration": 4, "description": "培养体育教学与训练的专业人才"},
]

# 艺术学类专业
ART_MAJORS = [
    {"name": "音乐表演", "category": "艺术学", "degree": "本科", "duration": 4, "description": "培养音乐演奏与演唱的专业人才"},
    {"name": "美术学", "category": "艺术学", "degree": "本科", "duration": 4, "description": "培养美术创作与教学的专业人才"},
    {"name": "设计学", "category": "艺术学", "degree": "本科", "duration": 4, "description": "培养艺术设计与创意的专业人才"},
    {"name": "戏剧影视导演", "category": "艺术学", "degree": "本科", "duration": 4, "description": "培养影视导演与制作的专业人才"},
    {"name": "播音与主持艺术", "category": "艺术学", "degree": "本科", "duration": 4, "description": "培养播音主持的专业人才"},
]


def get_university_id_by_name(api, name):
    """根据院校名称获取ID"""
    # 这里简化处理，实际应该调用API查询
    # 为了演示，我们使用一些已知的ID
    university_map = {
        "清华大学": 7,
        "北京大学": 8,
        "复旦大学": 9,
        "上海交通大学": 10,
        "浙江大学": 11,
        "中国科学技术大学": 12,
        "南京大学": 13,
    }
    return university_map.get(name)


def create_major_data(major_template, university_id):
    """创建专业数据"""
    major = major_template.copy()
    major['university_id'] = university_id
    return major


def main():
    """批量导入专业数据"""
    logger.info("=" * 60)
    logger.info("开始批量导入专业数据")
    logger.info("=" * 60)
    
    api = APIHelper()
    
    # 检查后端服务
    if not api.check_backend_status():
        logger.error("后端服务未运行，请先启动后端服务")
        return
    
    # 合并所有专业模板
    all_major_templates = (
        ENGINEERING_MAJORS + 
        SCIENCE_MAJORS + 
        ECONOMICS_MAJORS + 
        MANAGEMENT_MAJORS + 
        LITERATURE_MAJORS + 
        LAW_MAJORS + 
        MEDICAL_MAJORS + 
        EDUCATION_MAJORS + 
        ART_MAJORS
    )
    
    logger.info(f"共 {len(all_major_templates)} 个专业模板")
    
    # 为前30所大学添加专业（可以根据需要调整）
    # 理工类大学添加工学、理学专业
    # 综合类大学添加所有专业
    # 财经类大学添加经济、管理专业
    
    majors_to_insert = []
    
    # 为清华、北大等顶尖综合大学添加所有专业
    top_universities = [
        (7, "清华大学", "综合"),
        (8, "北京大学", "综合"),
        (9, "复旦大学", "综合"),
        (10, "上海交通大学", "综合"),
        (11, "浙江大学", "综合"),
        (12, "中国科学技术大学", "理工"),
        (13, "南京大学", "综合"),
        (14, "中国人民大学", "综合"),
        (15, "北京航空航天大学", "理工"),
        (16, "北京理工大学", "理工"),
    ]
    
    for univ_id, univ_name, univ_type in top_universities:
        logger.info(f"为 {univ_name} 添加专业...")
        
        if univ_type == "综合":
            # 综合大学添加所有专业
            for major_template in all_major_templates:
                major = create_major_data(major_template, univ_id)
                majors_to_insert.append(major)
        elif univ_type == "理工":
            # 理工大学添加工学、理学、管理学专业
            for major_template in ENGINEERING_MAJORS + SCIENCE_MAJORS + MANAGEMENT_MAJORS:
                major = create_major_data(major_template, univ_id)
                majors_to_insert.append(major)
    
    logger.info(f"共准备插入 {len(majors_to_insert)} 条专业数据")
    
    # 批量导入
    success_count = api.batch_insert_majors(majors_to_insert)
    
    logger.info("=" * 60)
    logger.info(f"批量导入完成！")
    logger.info(f"成功: {success_count}/{len(majors_to_insert)}")
    logger.info("=" * 60)
    logger.info("")
    logger.info("验证数据：")
    logger.info("  mysql -u root -p")
    logger.info("  USE volunteer_exam;")
    logger.info("  SELECT COUNT(*) FROM major;")
    logger.info("  SELECT m.name, u.name as university FROM major m")
    logger.info("  JOIN university u ON m.university_id = u.id LIMIT 20;")


if __name__ == '__main__':
    main()
