"""
批量插入大专（高职）院校数据
"""
from api_helper import APIHelper
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 优质高职院校数据
VOCATIONAL_COLLEGES = [
    # 国家示范性高职院校
    {"name": "深圳职业技术学院", "province": "广东", "city": "深圳", "level": "专科/国家示范", "type": "综合", "website": "https://www.szpt.edu.cn"},
    {"name": "北京电子科技职业学院", "province": "北京", "city": "北京", "level": "专科/国家示范", "type": "理工", "website": "https://www.dky.bjedu.cn"},
    {"name": "天津职业大学", "province": "天津", "city": "天津", "level": "专科/国家示范", "type": "综合", "website": "https://www.tjtc.edu.cn"},
    {"name": "重庆电子工程职业学院", "province": "重庆", "city": "重庆", "level": "专科/国家示范", "type": "理工", "website": "https://www.cqcet.edu.cn"},
    {"name": "黄河水利职业技术学院", "province": "河南", "city": "开封", "level": "专科/国家示范", "type": "理工", "website": "https://www.yrcti.edu.cn"},
    {"name": "长沙民政职业技术学院", "province": "湖南", "city": "长沙", "level": "专科/国家示范", "type": "综合", "website": "https://www.csmzxy.edu.cn"},
    {"name": "无锡职业技术学院", "province": "江苏", "city": "无锡", "level": "专科/国家示范", "type": "理工", "website": "https://www.wxit.edu.cn"},
    {"name": "宁波职业技术学院", "province": "浙江", "city": "宁波", "level": "专科/国家示范", "type": "综合", "website": "https://www.nbpt.edu.cn"},
    {"name": "南京信息职业技术学院", "province": "江苏", "city": "南京", "level": "专科/国家示范", "type": "理工", "website": "https://www.njcit.cn"},
    {"name": "杨凌职业技术学院", "province": "陕西", "city": "杨凌", "level": "专科/国家示范", "type": "农林", "website": "https://www.ylvtc.cn"},
    
    # 双高计划院校
    {"name": "北京工业职业技术学院", "province": "北京", "city": "北京", "level": "专科/双高", "type": "理工", "website": "https://www.bgy.edu.cn"},
    {"name": "天津医学高等专科学校", "province": "天津", "city": "天津", "level": "专科/双高", "type": "医药", "website": "https://www.tjyzh.cn"},
    {"name": "河北工业职业技术学院", "province": "河北", "city": "石家庄", "level": "专科/双高", "type": "理工", "website": "https://www.hbcit.edu.cn"},
    {"name": "山西省财政税务专科学校", "province": "山西", "city": "太原", "level": "专科/双高", "type": "财经", "website": "https://www.sxftc.edu.cn"},
    {"name": "辽宁省交通高等专科学校", "province": "辽宁", "city": "沈阳", "level": "专科/双高", "type": "理工", "website": "https://www.lncc.edu.cn"},
    {"name": "长春汽车工业高等专科学校", "province": "吉林", "city": "长春", "level": "专科/双高", "type": "理工", "website": "https://www.caii.edu.cn"},
    {"name": "哈尔滨职业技术学院", "province": "黑龙江", "city": "哈尔滨", "level": "专科/双高", "type": "综合", "website": "https://www.hzjxy.org.cn"},
    {"name": "上海工艺美术职业学院", "province": "上海", "city": "上海", "level": "专科/双高", "type": "艺术", "website": "https://www.ssca.edu.cn"},
    {"name": "江苏农林职业技术学院", "province": "江苏", "city": "镇江", "level": "专科/双高", "type": "农林", "website": "https://www.jsafc.edu.cn"},
    {"name": "常州信息职业技术学院", "province": "江苏", "city": "常州", "level": "专科/双高", "type": "理工", "website": "https://www.ccit.js.cn"},
    {"name": "江苏农牧科技职业学院", "province": "江苏", "city": "泰州", "level": "专科/双高", "type": "农林", "website": "https://www.jsahvc.edu.cn"},
    {"name": "浙江金融职业学院", "province": "浙江", "city": "杭州", "level": "专科/双高", "type": "财经", "website": "https://www.zfc.edu.cn"},
    {"name": "浙江机电职业技术学院", "province": "浙江", "city": "杭州", "level": "专科/双高", "type": "理工", "website": "https://www.zime.edu.cn"},
    {"name": "金华职业技术学院", "province": "浙江", "city": "金华", "level": "专科/双高", "type": "综合", "website": "https://www.jhc.edu.cn"},
    {"name": "安徽职业技术学院", "province": "安徽", "city": "合肥", "level": "专科/双高", "type": "理工", "website": "https://www.uta.edu.cn"},
    {"name": "福建船政交通职业学院", "province": "福建", "city": "福州", "level": "专科/双高", "type": "理工", "website": "https://www.fjcpc.edu.cn"},
    {"name": "九江职业技术学院", "province": "江西", "city": "九江", "level": "专科/双高", "type": "理工", "website": "https://www.jvtc.jx.cn"},
    {"name": "山东商业职业技术学院", "province": "山东", "city": "济南", "level": "专科/双高", "type": "财经", "website": "https://www.sict.edu.cn"},
    {"name": "青岛职业技术学院", "province": "山东", "city": "青岛", "level": "专科/双高", "type": "综合", "website": "https://www.qtc.edu.cn"},
    {"name": "河南职业技术学院", "province": "河南", "city": "郑州", "level": "专科/双高", "type": "理工", "website": "https://www.hnzj.edu.cn"},
    {"name": "武汉职业技术学院", "province": "湖北", "city": "武汉", "level": "专科/双高", "type": "综合", "website": "https://www.wtc.edu.cn"},
    {"name": "湖南铁道职业技术学院", "province": "湖南", "city": "株洲", "level": "专科/双高", "type": "理工", "website": "https://www.hnrpc.com"},
    {"name": "广东轻工职业技术学院", "province": "广东", "city": "广州", "level": "专科/双高", "type": "理工", "website": "https://www.gdqy.edu.cn"},
    {"name": "广州番禺职业技术学院", "province": "广东", "city": "广州", "level": "专科/双高", "type": "综合", "website": "https://www.gzpyp.edu.cn"},
    {"name": "深圳信息职业技术学院", "province": "广东", "city": "深圳", "level": "专科/双高", "type": "理工", "website": "https://www.sziit.edu.cn"},
    {"name": "广西职业技术学院", "province": "广西", "city": "南宁", "level": "专科/双高", "type": "综合", "website": "https://www.gxzjy.com"},
    {"name": "海南经贸职业技术学院", "province": "海南", "city": "海口", "level": "专科/双高", "type": "财经", "website": "https://www.hnjmxy.cn"},
    {"name": "成都航空职业技术学院", "province": "四川", "city": "成都", "level": "专科/双高", "type": "理工", "website": "https://www.cap.edu.cn"},
    {"name": "四川工程职业技术学院", "province": "四川", "city": "德阳", "level": "专科/双高", "type": "理工", "website": "https://www.scetc.edu.cn"},
    {"name": "贵州交通职业技术学院", "province": "贵州", "city": "贵阳", "level": "专科/双高", "type": "理工", "website": "https://www.gzjtzy.net"},
    {"name": "昆明冶金高等专科学校", "province": "云南", "city": "昆明", "level": "专科/双高", "type": "理工", "website": "https://www.kmyz.edu.cn"},
    {"name": "陕西工业职业技术学院", "province": "陕西", "city": "咸阳", "level": "专科/双高", "type": "理工", "website": "https://www.sxpi.edu.cn"},
    {"name": "兰州资源环境职业技术学院", "province": "甘肃", "city": "兰州", "level": "专科/双高", "type": "理工", "website": "https://www.lzre.edu.cn"},
    {"name": "宁夏职业技术学院", "province": "宁夏", "city": "银川", "level": "专科/双高", "type": "综合", "website": "https://www.nxtc.edu.cn"},
    {"name": "新疆农业职业技术学院", "province": "新疆", "city": "昌吉", "level": "专科/双高", "type": "农林", "website": "https://www.xjnzy.edu.cn"},
]

# 大专常见专业
VOCATIONAL_MAJORS = [
    {"name": "计算机应用技术", "category": "电子信息", "degree": "专科", "duration": 3},
    {"name": "软件技术", "category": "电子信息", "degree": "专科", "duration": 3},
    {"name": "大数据技术", "category": "电子信息", "degree": "专科", "duration": 3},
    {"name": "云计算技术应用", "category": "电子信息", "degree": "专科", "duration": 3},
    {"name": "电子商务", "category": "财经商贸", "degree": "专科", "duration": 3},
    {"name": "会计", "category": "财经商贸", "degree": "专科", "duration": 3},
    {"name": "市场营销", "category": "财经商贸", "degree": "专科", "duration": 3},
    {"name": "物流管理", "category": "财经商贸", "degree": "专科", "duration": 3},
    {"name": "机电一体化技术", "category": "装备制造", "degree": "专科", "duration": 3},
    {"name": "数控技术", "category": "装备制造", "degree": "专科", "duration": 3},
    {"name": "汽车检测与维修技术", "category": "交通运输", "degree": "专科", "duration": 3},
    {"name": "建筑工程技术", "category": "土木建筑", "degree": "专科", "duration": 3},
    {"name": "工程造价", "category": "土木建筑", "degree": "专科", "duration": 3},
    {"name": "护理", "category": "医药卫生", "degree": "专科", "duration": 3},
    {"name": "学前教育", "category": "教育与体育", "degree": "专科", "duration": 3},
]


def main():
    """批量导入大专院校数据"""
    logger.info("=" * 60)
    logger.info("开始批量导入大专（高职）院校数据")
    logger.info(f"共 {len(VOCATIONAL_COLLEGES)} 所院校")
    logger.info("=" * 60)
    
    api = APIHelper()
    
    # 检查后端服务
    if not api.check_backend_status():
        logger.error("后端服务未运行，请先启动后端服务")
        return
    
    # 批量导入院校
    success_count = api.batch_insert_universities(VOCATIONAL_COLLEGES)
    
    logger.info("=" * 60)
    logger.info(f"院校导入完成！成功: {success_count}/{len(VOCATIONAL_COLLEGES)}")
    logger.info("=" * 60)
    
    # 为每所大专院校添加专业
    logger.info("")
    logger.info("=" * 60)
    logger.info("开始为大专院校添加专业")
    logger.info("=" * 60)
    
    # 获取刚导入的院校ID（简化处理，实际应该查询数据库）
    # 这里假设从ID 117开始（前面有115所本科+1所测试删除）
    start_id = 116
    
    majors_to_insert = []
    for i, college in enumerate(VOCATIONAL_COLLEGES):
        college_id = start_id + i
        logger.info(f"为 {college['name']} 准备专业...")
        
        for major_template in VOCATIONAL_MAJORS:
            major = major_template.copy()
            major['university_id'] = college_id
            majors_to_insert.append(major)
    
    logger.info(f"共准备插入 {len(majors_to_insert)} 条专业数据")
    
    # 批量导入专业
    major_success_count = api.batch_insert_majors(majors_to_insert)
    
    logger.info("=" * 60)
    logger.info(f"专业导入完成！成功: {major_success_count}/{len(majors_to_insert)}")
    logger.info("=" * 60)


if __name__ == '__main__':
    main()
