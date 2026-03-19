# -*- coding: utf-8 -*-
"""
插入就业相关示例数据
"""
import pymysql
import json

# 数据库配置
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'root',
    'database': 'volunteer_exam',
    'charset': 'utf8mb4'
}

def insert_sample_data():
    """插入示例数据"""
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    try:
        print("开始插入示例数据...")
        
        # 1. 插入行业数据
        print("\n1. 插入行业数据...")
        industries = [
            ('互联网IT', '技术', 15000, 12.5, 500000, '包括软件开发、人工智能、大数据等领域', '上升', 
             json.dumps(["北京", "上海", "深圳", "杭州"], ensure_ascii=False)),
            ('金融', '金融', 12000, 5.2, 300000, '包括银行、证券、保险、投资等', '稳定',
             json.dumps(["北京", "上海", "深圳", "广州"], ensure_ascii=False)),
            ('医疗健康', '医疗', 10000, 8.3, 200000, '包括临床医疗、医药研发、医疗器械等', '上升',
             json.dumps(["北京", "上海", "广州", "成都"], ensure_ascii=False)),
            ('教育培训', '教育', 8000, 6.5, 250000, '包括K12教育、高等教育、职业培训等', '稳定',
             json.dumps(["北京", "上海", "广州", "成都"], ensure_ascii=False)),
            ('制造业', '制造', 7000, 3.2, 400000, '包括汽车、电子、机械等传统制造业', '稳定',
             json.dumps(["上海", "深圳", "苏州", "东莞"], ensure_ascii=False)),
            ('电子商务', '互联网', 11000, 10.5, 180000, '包括电商平台、跨境电商、社交电商等', '上升',
             json.dumps(["杭州", "上海", "深圳", "广州"], ensure_ascii=False)),
            ('建筑工程', '工程', 9000, 4.8, 350000, '包括建筑设计、施工管理、工程监理等', '稳定',
             json.dumps(["北京", "上海", "广州", "成都"], ensure_ascii=False)),
            ('文化传媒', '文化', 8500, 7.2, 120000, '包括广告、影视、出版、新媒体等', '上升',
             json.dumps(["北京", "上海", "深圳", "杭州"], ensure_ascii=False))
        ]
        
        cursor.executemany(
            """INSERT INTO industry (name, category, avg_salary, growth_rate, job_count, 
               description, trend, hot_cities) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)""",
            industries
        )
        print(f"✓ 插入了 {len(industries)} 条行业数据")
        
        # 2. 插入职业数据
        print("\n2. 插入职业数据...")
        careers = [
            ('软件工程师', 1, 18000, '12k-30k', '本科',
             json.dumps(["Java", "Python", "数据结构", "算法"], ensure_ascii=False),
             '初级工程师 -> 中级工程师 -> 高级工程师 -> 技术专家/架构师',
             '负责软件系统的设计、开发和维护', 150000),
            ('算法工程师', 1, 25000, '18k-50k', '硕士',
             json.dumps(["机器学习", "深度学习", "Python", "数学"], ensure_ascii=False),
             '算法工程师 -> 高级算法工程师 -> 算法专家/科学家',
             '负责AI算法的研发和优化', 50000),
            ('产品经理', 1, 20000, '15k-40k', '本科',
             json.dumps(["产品设计", "需求分析", "项目管理", "沟通能力"], ensure_ascii=False),
             '产品助理 -> 产品经理 -> 高级产品经理 -> 产品总监',
             '负责产品规划、设计和运营', 80000),
            ('金融分析师', 2, 15000, '10k-30k', '本科',
             json.dumps(["财务分析", "Excel", "金融建模", "数据分析"], ensure_ascii=False),
             '分析师 -> 高级分析师 -> 投资经理 -> 投资总监',
             '负责金融产品分析和投资建议', 60000),
            ('临床医生', 3, 12000, '8k-25k', '硕士',
             json.dumps(["临床诊断", "医学知识", "沟通能力"], ensure_ascii=False),
             '住院医师 -> 主治医师 -> 副主任医师 -> 主任医师',
             '负责疾病诊断和治疗', 100000),
            ('教师', 4, 9000, '6k-15k', '本科',
             json.dumps(["教学能力", "学科知识", "沟通能力"], ensure_ascii=False),
             '教师 -> 骨干教师 -> 学科带头人 -> 特级教师',
             '负责教学和学生管理', 150000),
            ('机械工程师', 5, 10000, '7k-18k', '本科',
             json.dumps(["机械设计", "CAD", "工程制图"], ensure_ascii=False),
             '助理工程师 -> 工程师 -> 高级工程师 -> 总工程师',
             '负责机械产品设计和研发', 80000),
            ('电商运营', 6, 10000, '7k-20k', '本科',
             json.dumps(["运营策划", "数据分析", "营销推广"], ensure_ascii=False),
             '运营专员 -> 运营经理 -> 运营总监',
             '负责电商平台运营和推广', 70000)
        ]
        
        cursor.executemany(
            """INSERT INTO career (name, industry_id, avg_salary, salary_range, 
               education_requirement, skill_requirements, career_path, description, job_count)
               VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)""",
            careers
        )
        print(f"✓ 插入了 {len(careers)} 条职业数据")
        
        # 3. 插入升学路径数据
        print("\n3. 插入升学路径数据...")
        paths = [
            ('强基计划', '特殊招生',
             '选拔培养有志于服务国家重大战略需求且综合素质优秀或基础学科拔尖的学生',
             '高考成绩优异，对基础学科有浓厚兴趣',
             json.dumps(["清华大学", "北京大学", "复旦大学", "上海交通大学"], ensure_ascii=False),
             json.dumps(["数学", "物理", "化学", "生物", "历史", "哲学"], ensure_ascii=False),
             json.dumps([{"month":3,"event":"报名"},{"month":6,"event":"高考"},{"month":7,"event":"校测"}], ensure_ascii=False),
             '本硕博衔接培养，优质教育资源，国家重点支持',
             '专业选择受限，主要为基础学科，不能转专业',
             '成绩优异，对数学、物理、化学、生物、历史、哲学等基础学科有浓厚兴趣的学生'),
            ('综合评价', '特殊招生',
             '高考成绩+校测成绩+学业水平测试成绩综合评价录取',
             '高考成绩良好，综合素质强',
             json.dumps(["南方科技大学", "上海科技大学", "昆山杜克大学"], ensure_ascii=False),
             json.dumps(["理工类", "文科类", "医学类"], ensure_ascii=False),
             json.dumps([{"month":4,"event":"报名"},{"month":6,"event":"高考"},{"month":6,"event":"校测"}], ensure_ascii=False),
             '多一次录取机会，看重综合素质',
             '需要参加校测，准备时间紧张',
             '综合素质强，有特长或竞赛经历的学生'),
            ('专升本', '升学途径',
             '专科毕业后通过考试升入本科院校',
             '专科在读或毕业生',
             json.dumps(["各省本科院校"], ensure_ascii=False),
             json.dumps(["所有专业"], ensure_ascii=False),
             json.dumps([{"month":3,"event":"报名"},{"month":4,"event":"考试"},{"month":6,"event":"录取"}], ensure_ascii=False),
             '获得本科学历，提升就业竞争力',
             '需要额外2年时间，竞争激烈',
             '有上进心，希望提升学历的专科生')
        ]
        
        cursor.executemany(
            """INSERT INTO enrollment_path (name, type, description, requirements, 
               universities, majors, timeline, advantages, disadvantages, suitable_students)
               VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)""",
            paths
        )
        print(f"✓ 插入了 {len(paths)} 条升学路径数据")
        
        # 4. 插入城市就业数据
        print("\n4. 插入城市就业数据...")
        cities = [
            ('北京', '北京', '一线', 12000, 800000,
             json.dumps(["互联网", "金融", "教育", "文化"], ensure_ascii=False), 6000, '高'),
            ('上海', '上海', '一线', 11500, 750000,
             json.dumps(["金融", "互联网", "制造", "贸易"], ensure_ascii=False), 5500, '高'),
            ('深圳', '广东', '一线', 11000, 600000,
             json.dumps(["互联网", "金融", "电子", "贸易"], ensure_ascii=False), 5000, '高'),
            ('广州', '广东', '一线', 10000, 550000,
             json.dumps(["贸易", "金融", "制造", "互联网"], ensure_ascii=False), 4500, '高'),
            ('杭州', '浙江', '新一线', 10000, 400000,
             json.dumps(["互联网", "电商", "金融", "文化"], ensure_ascii=False), 4000, '高'),
            ('成都', '四川', '新一线', 8000, 350000,
             json.dumps(["互联网", "游戏", "电子", "金融"], ensure_ascii=False), 3000, '中'),
            ('武汉', '湖北', '新一线', 7500, 300000,
             json.dumps(["光电子", "互联网", "汽车", "金融"], ensure_ascii=False), 2800, '中'),
            ('南京', '江苏', '新一线', 9000, 320000,
             json.dumps(["互联网", "金融", "制造", "教育"], ensure_ascii=False), 3500, '中'),
            ('西安', '陕西', '新一线', 7000, 280000,
             json.dumps(["互联网", "航空航天", "电子", "教育"], ensure_ascii=False), 2500, '中'),
            ('重庆', '重庆', '新一线', 7500, 300000,
             json.dumps(["汽车", "电子", "互联网", "金融"], ensure_ascii=False), 2800, '中')
        ]
        
        cursor.executemany(
            """INSERT INTO city_employment (city, province, tier, avg_salary, job_count,
               hot_industries, living_cost, development_potential)
               VALUES (%s, %s, %s, %s, %s, %s, %s, %s)""",
            cities
        )
        print(f"✓ 插入了 {len(cities)} 条城市就业数据")
        
        conn.commit()
        print("\n✅ 所有示例数据插入成功！")
        
        # 验证数据
        print("\n验证数据...")
        cursor.execute("SELECT COUNT(*) FROM industry")
        print(f"行业数据: {cursor.fetchone()[0]} 条")
        
        cursor.execute("SELECT COUNT(*) FROM career")
        print(f"职业数据: {cursor.fetchone()[0]} 条")
        
        cursor.execute("SELECT COUNT(*) FROM enrollment_path")
        print(f"升学路径: {cursor.fetchone()[0]} 条")
        
        cursor.execute("SELECT COUNT(*) FROM city_employment")
        print(f"城市就业数据: {cursor.fetchone()[0]} 条")
        
    except Exception as e:
        conn.rollback()
        print(f"\n❌ 错误: {e}")
        raise
    finally:
        cursor.close()
        conn.close()

if __name__ == '__main__':
    insert_sample_data()
