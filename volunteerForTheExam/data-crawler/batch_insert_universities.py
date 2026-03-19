"""
批量插入真实院校数据
通过API快速导入常见大学数据
"""
from api_helper import APIHelper
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 真实的大学数据（985/211/双一流院校）
UNIVERSITIES_DATA = [
    # 985院校
    {"name": "清华大学", "province": "北京", "city": "北京", "level": "985/211/双一流", "type": "综合", "website": "https://www.tsinghua.edu.cn"},
    {"name": "北京大学", "province": "北京", "city": "北京", "level": "985/211/双一流", "type": "综合", "website": "https://www.pku.edu.cn"},
    {"name": "复旦大学", "province": "上海", "city": "上海", "level": "985/211/双一流", "type": "综合", "website": "https://www.fudan.edu.cn"},
    {"name": "上海交通大学", "province": "上海", "city": "上海", "level": "985/211/双一流", "type": "综合", "website": "https://www.sjtu.edu.cn"},
    {"name": "浙江大学", "province": "浙江", "city": "杭州", "level": "985/211/双一流", "type": "综合", "website": "https://www.zju.edu.cn"},
    {"name": "中国科学技术大学", "province": "安徽", "city": "合肥", "level": "985/211/双一流", "type": "理工", "website": "https://www.ustc.edu.cn"},
    {"name": "南京大学", "province": "江苏", "city": "南京", "level": "985/211/双一流", "type": "综合", "website": "https://www.nju.edu.cn"},
    {"name": "中国人民大学", "province": "北京", "city": "北京", "level": "985/211/双一流", "type": "综合", "website": "https://www.ruc.edu.cn"},
    {"name": "北京航空航天大学", "province": "北京", "city": "北京", "level": "985/211/双一流", "type": "理工", "website": "https://www.buaa.edu.cn"},
    {"name": "北京理工大学", "province": "北京", "city": "北京", "level": "985/211/双一流", "type": "理工", "website": "https://www.bit.edu.cn"},
    {"name": "北京师范大学", "province": "北京", "city": "北京", "level": "985/211/双一流", "type": "师范", "website": "https://www.bnu.edu.cn"},
    {"name": "南开大学", "province": "天津", "city": "天津", "level": "985/211/双一流", "type": "综合", "website": "https://www.nankai.edu.cn"},
    {"name": "天津大学", "province": "天津", "city": "天津", "level": "985/211/双一流", "type": "理工", "website": "https://www.tju.edu.cn"},
    {"name": "哈尔滨工业大学", "province": "黑龙江", "city": "哈尔滨", "level": "985/211/双一流", "type": "理工", "website": "https://www.hit.edu.cn"},
    {"name": "西安交通大学", "province": "陕西", "city": "西安", "level": "985/211/双一流", "type": "综合", "website": "https://www.xjtu.edu.cn"},
    {"name": "华中科技大学", "province": "湖北", "city": "武汉", "level": "985/211/双一流", "type": "综合", "website": "https://www.hust.edu.cn"},
    {"name": "武汉大学", "province": "湖北", "city": "武汉", "level": "985/211/双一流", "type": "综合", "website": "https://www.whu.edu.cn"},
    {"name": "中山大学", "province": "广东", "city": "广州", "level": "985/211/双一流", "type": "综合", "website": "https://www.sysu.edu.cn"},
    {"name": "四川大学", "province": "四川", "city": "成都", "level": "985/211/双一流", "type": "综合", "website": "https://www.scu.edu.cn"},
    {"name": "山东大学", "province": "山东", "city": "济南", "level": "985/211/双一流", "type": "综合", "website": "https://www.sdu.edu.cn"},
    {"name": "厦门大学", "province": "福建", "city": "厦门", "level": "985/211/双一流", "type": "综合", "website": "https://www.xmu.edu.cn"},
    {"name": "东南大学", "province": "江苏", "city": "南京", "level": "985/211/双一流", "type": "综合", "website": "https://www.seu.edu.cn"},
    {"name": "同济大学", "province": "上海", "city": "上海", "level": "985/211/双一流", "type": "理工", "website": "https://www.tongji.edu.cn"},
    {"name": "华东师范大学", "province": "上海", "city": "上海", "level": "985/211/双一流", "type": "师范", "website": "https://www.ecnu.edu.cn"},
    {"name": "大连理工大学", "province": "辽宁", "city": "大连", "level": "985/211/双一流", "type": "理工", "website": "https://www.dlut.edu.cn"},
    {"name": "东北大学", "province": "辽宁", "city": "沈阳", "level": "985/211/双一流", "type": "理工", "website": "https://www.neu.edu.cn"},
    {"name": "吉林大学", "province": "吉林", "city": "长春", "level": "985/211/双一流", "type": "综合", "website": "https://www.jlu.edu.cn"},
    {"name": "湖南大学", "province": "湖南", "city": "长沙", "level": "985/211/双一流", "type": "综合", "website": "https://www.hnu.edu.cn"},
    {"name": "中南大学", "province": "湖南", "city": "长沙", "level": "985/211/双一流", "type": "综合", "website": "https://www.csu.edu.cn"},
    {"name": "重庆大学", "province": "重庆", "city": "重庆", "level": "985/211/双一流", "type": "综合", "website": "https://www.cqu.edu.cn"},
    {"name": "电子科技大学", "province": "四川", "city": "成都", "level": "985/211/双一流", "type": "理工", "website": "https://www.uestc.edu.cn"},
    {"name": "西北工业大学", "province": "陕西", "city": "西安", "level": "985/211/双一流", "type": "理工", "website": "https://www.nwpu.edu.cn"},
    {"name": "兰州大学", "province": "甘肃", "city": "兰州", "level": "985/211/双一流", "type": "综合", "website": "https://www.lzu.edu.cn"},
    
    # 211院校
    {"name": "北京交通大学", "province": "北京", "city": "北京", "level": "211/双一流", "type": "理工", "website": "https://www.bjtu.edu.cn"},
    {"name": "北京科技大学", "province": "北京", "city": "北京", "level": "211/双一流", "type": "理工", "website": "https://www.ustb.edu.cn"},
    {"name": "北京化工大学", "province": "北京", "city": "北京", "level": "211/双一流", "type": "理工", "website": "https://www.buct.edu.cn"},
    {"name": "北京邮电大学", "province": "北京", "city": "北京", "level": "211/双一流", "type": "理工", "website": "https://www.bupt.edu.cn"},
    {"name": "中国农业大学", "province": "北京", "city": "北京", "level": "985/211/双一流", "type": "农林", "website": "https://www.cau.edu.cn"},
    {"name": "北京林业大学", "province": "北京", "city": "北京", "level": "211/双一流", "type": "农林", "website": "https://www.bjfu.edu.cn"},
    {"name": "北京中医药大学", "province": "北京", "city": "北京", "level": "211/双一流", "type": "医药", "website": "https://www.bucm.edu.cn"},
    {"name": "中央财经大学", "province": "北京", "city": "北京", "level": "211/双一流", "type": "财经", "website": "https://www.cufe.edu.cn"},
    {"name": "对外经济贸易大学", "province": "北京", "city": "北京", "level": "211/双一流", "type": "财经", "website": "https://www.uibe.edu.cn"},
    {"name": "中国政法大学", "province": "北京", "city": "北京", "level": "211/双一流", "type": "政法", "website": "https://www.cupl.edu.cn"},
    {"name": "华北电力大学", "province": "北京", "city": "北京", "level": "211/双一流", "type": "理工", "website": "https://www.ncepu.edu.cn"},
    {"name": "中国传媒大学", "province": "北京", "city": "北京", "level": "211/双一流", "type": "语言", "website": "https://www.cuc.edu.cn"},
    {"name": "中央民族大学", "province": "北京", "city": "北京", "level": "985/211/双一流", "type": "民族", "website": "https://www.muc.edu.cn"},
    {"name": "河北工业大学", "province": "河北", "city": "天津", "level": "211/双一流", "type": "理工", "website": "https://www.hebut.edu.cn"},
    {"name": "太原理工大学", "province": "山西", "city": "太原", "level": "211/双一流", "type": "理工", "website": "https://www.tyut.edu.cn"},
    {"name": "内蒙古大学", "province": "内蒙古", "city": "呼和浩特", "level": "211/双一流", "type": "综合", "website": "https://www.imu.edu.cn"},
    {"name": "辽宁大学", "province": "辽宁", "city": "沈阳", "level": "211/双一流", "type": "综合", "website": "https://www.lnu.edu.cn"},
    {"name": "大连海事大学", "province": "辽宁", "city": "大连", "level": "211/双一流", "type": "理工", "website": "https://www.dlmu.edu.cn"},
    {"name": "延边大学", "province": "吉林", "city": "延边", "level": "211/双一流", "type": "综合", "website": "https://www.ybu.edu.cn"},
    {"name": "东北师范大学", "province": "吉林", "city": "长春", "level": "211/双一流", "type": "师范", "website": "https://www.nenu.edu.cn"},
    {"name": "东北农业大学", "province": "黑龙江", "city": "哈尔滨", "level": "211/双一流", "type": "农林", "website": "https://www.neau.edu.cn"},
    {"name": "东北林业大学", "province": "黑龙江", "city": "哈尔滨", "level": "211/双一流", "type": "农林", "website": "https://www.nefu.edu.cn"},
    {"name": "华东理工大学", "province": "上海", "city": "上海", "level": "211/双一流", "type": "理工", "website": "https://www.ecust.edu.cn"},
    {"name": "东华大学", "province": "上海", "city": "上海", "level": "211/双一流", "type": "理工", "website": "https://www.dhu.edu.cn"},
    {"name": "上海财经大学", "province": "上海", "city": "上海", "level": "211/双一流", "type": "财经", "website": "https://www.shufe.edu.cn"},
    {"name": "上海大学", "province": "上海", "city": "上海", "level": "211/双一流", "type": "综合", "website": "https://www.shu.edu.cn"},
    {"name": "苏州大学", "province": "江苏", "city": "苏州", "level": "211/双一流", "type": "综合", "website": "https://www.suda.edu.cn"},
    {"name": "南京航空航天大学", "province": "江苏", "city": "南京", "level": "211/双一流", "type": "理工", "website": "https://www.nuaa.edu.cn"},
    {"name": "南京理工大学", "province": "江苏", "city": "南京", "level": "211/双一流", "type": "理工", "website": "https://www.njust.edu.cn"},
    {"name": "中国矿业大学", "province": "江苏", "city": "徐州", "level": "211/双一流", "type": "理工", "website": "https://www.cumt.edu.cn"},
    {"name": "河海大学", "province": "江苏", "city": "南京", "level": "211/双一流", "type": "理工", "website": "https://www.hhu.edu.cn"},
    {"name": "江南大学", "province": "江苏", "city": "无锡", "level": "211/双一流", "type": "综合", "website": "https://www.jiangnan.edu.cn"},
    {"name": "南京农业大学", "province": "江苏", "city": "南京", "level": "211/双一流", "type": "农林", "website": "https://www.njau.edu.cn"},
    {"name": "中国药科大学", "province": "江苏", "city": "南京", "level": "211/双一流", "type": "医药", "website": "https://www.cpu.edu.cn"},
    {"name": "南京师范大学", "province": "江苏", "city": "南京", "level": "211/双一流", "type": "师范", "website": "https://www.njnu.edu.cn"},
    {"name": "安徽大学", "province": "安徽", "city": "合肥", "level": "211/双一流", "type": "综合", "website": "https://www.ahu.edu.cn"},
    {"name": "合肥工业大学", "province": "安徽", "city": "合肥", "level": "211/双一流", "type": "理工", "website": "https://www.hfut.edu.cn"},
    {"name": "福州大学", "province": "福建", "city": "福州", "level": "211/双一流", "type": "理工", "website": "https://www.fzu.edu.cn"},
    {"name": "南昌大学", "province": "江西", "city": "南昌", "level": "211/双一流", "type": "综合", "website": "https://www.ncu.edu.cn"},
    {"name": "中国海洋大学", "province": "山东", "city": "青岛", "level": "985/211/双一流", "type": "综合", "website": "https://www.ouc.edu.cn"},
    {"name": "中国石油大学(华东)", "province": "山东", "city": "青岛", "level": "211/双一流", "type": "理工", "website": "https://www.upc.edu.cn"},
    {"name": "郑州大学", "province": "河南", "city": "郑州", "level": "211/双一流", "type": "综合", "website": "https://www.zzu.edu.cn"},
    {"name": "华中农业大学", "province": "湖北", "city": "武汉", "level": "211/双一流", "type": "农林", "website": "https://www.hzau.edu.cn"},
    {"name": "华中师范大学", "province": "湖北", "city": "武汉", "level": "211/双一流", "type": "师范", "website": "https://www.ccnu.edu.cn"},
    {"name": "中南财经政法大学", "province": "湖北", "city": "武汉", "level": "211/双一流", "type": "财经", "website": "https://www.zuel.edu.cn"},
    {"name": "湖南师范大学", "province": "湖南", "city": "长沙", "level": "211/双一流", "type": "师范", "website": "https://www.hunnu.edu.cn"},
    {"name": "华南理工大学", "province": "广东", "city": "广州", "level": "985/211/双一流", "type": "理工", "website": "https://www.scut.edu.cn"},
    {"name": "华南师范大学", "province": "广东", "city": "广州", "level": "211/双一流", "type": "师范", "website": "https://www.scnu.edu.cn"},
    {"name": "暨南大学", "province": "广东", "city": "广州", "level": "211/双一流", "type": "综合", "website": "https://www.jnu.edu.cn"},
    {"name": "广西大学", "province": "广西", "city": "南宁", "level": "211/双一流", "type": "综合", "website": "https://www.gxu.edu.cn"},
    {"name": "海南大学", "province": "海南", "city": "海口", "level": "211/双一流", "type": "综合", "website": "https://www.hainanu.edu.cn"},
    {"name": "西南交通大学", "province": "四川", "city": "成都", "level": "211/双一流", "type": "理工", "website": "https://www.swjtu.edu.cn"},
    {"name": "西南财经大学", "province": "四川", "city": "成都", "level": "211/双一流", "type": "财经", "website": "https://www.swufe.edu.cn"},
    {"name": "西南大学", "province": "重庆", "city": "重庆", "level": "211/双一流", "type": "综合", "website": "https://www.swu.edu.cn"},
    {"name": "贵州大学", "province": "贵州", "city": "贵阳", "level": "211/双一流", "type": "综合", "website": "https://www.gzu.edu.cn"},
    {"name": "云南大学", "province": "云南", "city": "昆明", "level": "211/双一流", "type": "综合", "website": "https://www.ynu.edu.cn"},
    {"name": "西北大学", "province": "陕西", "city": "西安", "level": "211/双一流", "type": "综合", "website": "https://www.nwu.edu.cn"},
    {"name": "西安电子科技大学", "province": "陕西", "city": "西安", "level": "211/双一流", "type": "理工", "website": "https://www.xidian.edu.cn"},
    {"name": "长安大学", "province": "陕西", "city": "西安", "level": "211/双一流", "type": "理工", "website": "https://www.chd.edu.cn"},
    {"name": "西北农林科技大学", "province": "陕西", "city": "杨凌", "level": "985/211/双一流", "type": "农林", "website": "https://www.nwsuaf.edu.cn"},
    {"name": "青海大学", "province": "青海", "city": "西宁", "level": "211/双一流", "type": "综合", "website": "https://www.qhu.edu.cn"},
    {"name": "宁夏大学", "province": "宁夏", "city": "银川", "level": "211/双一流", "type": "综合", "website": "https://www.nxu.edu.cn"},
    {"name": "新疆大学", "province": "新疆", "city": "乌鲁木齐", "level": "211/双一流", "type": "综合", "website": "https://www.xju.edu.cn"},
    {"name": "石河子大学", "province": "新疆", "city": "石河子", "level": "211/双一流", "type": "综合", "website": "https://www.shzu.edu.cn"},
    
    # 其他重点本科院校
    {"name": "深圳大学", "province": "广东", "city": "深圳", "level": "本科", "type": "综合", "website": "https://www.szu.edu.cn"},
    {"name": "南方科技大学", "province": "广东", "city": "深圳", "level": "双一流", "type": "理工", "website": "https://www.sustech.edu.cn"},
    {"name": "首都医科大学", "province": "北京", "city": "北京", "level": "本科", "type": "医药", "website": "https://www.ccmu.edu.cn"},
    {"name": "北京语言大学", "province": "北京", "city": "北京", "level": "本科", "type": "语言", "website": "https://www.blcu.edu.cn"},
    {"name": "外交学院", "province": "北京", "city": "北京", "level": "双一流", "type": "政法", "website": "https://www.cfau.edu.cn"},
    {"name": "中国人民公安大学", "province": "北京", "city": "北京", "level": "双一流", "type": "政法", "website": "https://www.ppsuc.edu.cn"},
    {"name": "北京电影学院", "province": "北京", "city": "北京", "level": "本科", "type": "艺术", "website": "https://www.bfa.edu.cn"},
    {"name": "中央音乐学院", "province": "北京", "city": "北京", "level": "双一流", "type": "艺术", "website": "https://www.ccom.edu.cn"},
    {"name": "中央美术学院", "province": "北京", "city": "北京", "level": "双一流", "type": "艺术", "website": "https://www.cafa.edu.cn"},
    {"name": "上海科技大学", "province": "上海", "city": "上海", "level": "本科", "type": "理工", "website": "https://www.shanghaitech.edu.cn"},
    {"name": "上海外国语大学", "province": "上海", "city": "上海", "level": "211/双一流", "type": "语言", "website": "https://www.shisu.edu.cn"},
    {"name": "上海音乐学院", "province": "上海", "city": "上海", "level": "双一流", "type": "艺术", "website": "https://www.shcmusic.edu.cn"},
    {"name": "南京邮电大学", "province": "江苏", "city": "南京", "level": "双一流", "type": "理工", "website": "https://www.njupt.edu.cn"},
    {"name": "南京信息工程大学", "province": "江苏", "city": "南京", "level": "双一流", "type": "理工", "website": "https://www.nuist.edu.cn"},
    {"name": "杭州电子科技大学", "province": "浙江", "city": "杭州", "level": "本科", "type": "理工", "website": "https://www.hdu.edu.cn"},
    {"name": "宁波大学", "province": "浙江", "city": "宁波", "level": "双一流", "type": "综合", "website": "https://www.nbu.edu.cn"},
    {"name": "中国美术学院", "province": "浙江", "city": "杭州", "level": "双一流", "type": "艺术", "website": "https://www.caa.edu.cn"},
    {"name": "西湖大学", "province": "浙江", "city": "杭州", "level": "本科", "type": "理工", "website": "https://www.westlake.edu.cn"},
]


def main():
    """批量导入院校数据"""
    logger.info("=" * 60)
    logger.info("开始批量导入真实院校数据")
    logger.info(f"共 {len(UNIVERSITIES_DATA)} 所院校")
    logger.info("=" * 60)
    
    api = APIHelper()
    
    # 检查后端服务
    if not api.check_backend_status():
        logger.error("后端服务未运行，请先启动后端服务")
        return
    
    # 批量导入
    success_count = api.batch_insert_universities(UNIVERSITIES_DATA)
    
    logger.info("=" * 60)
    logger.info(f"批量导入完成！")
    logger.info(f"成功: {success_count}/{len(UNIVERSITIES_DATA)}")
    logger.info("=" * 60)
    logger.info("")
    logger.info("验证数据：")
    logger.info("  mysql -u root -p")
    logger.info("  USE volunteer_exam;")
    logger.info("  SELECT COUNT(*) FROM university;")
    logger.info("  SELECT name, province, level FROM university LIMIT 20;")


if __name__ == '__main__':
    main()
