import React from 'react';
import { 
  GraduationCap, FileText, Briefcase, BookOpen, PenTool, 
  Building2, BarChart3, Wrench, ChevronRight 
} from 'lucide-react';

const categories = [
  {
    name: '论文专区',
    icon: GraduationCap,
    items: ['毕业论文', '期刊论文', '职称论文', '课题论文', '专升本论文', '课程论文']
  },
  {
    name: '论文周边',
    icon: FileText,
    items: ['开题报告', '文献综述', '论文任务书', '答辩稿', '调查报告', '中期总结', '课题申报书', '结题报告']
  },
  {
    name: '毕业必备',
    icon: GraduationCap,
    items: ['课程论文', '课程作业', '实习报告', '毕业设计报告', '职业规划']
  },
  {
    name: '课程论文',
    icon: BookOpen,
    items: ['课程论文', '期末论文', '形势与政策', '劳动教育', '信息安全', '就业创业课程', '创业规划书']
  },
  {
    name: '作业大全',
    icon: PenTool,
    items: ['期末论文', '课程作业', '写作文', '心得体会', '读后感', '观后感', '科学小论文']
  },
  {
    name: '实习就业',
    icon: Briefcase,
    items: ['职业规划', '实习报告', '实训报告', '实习周报', '实习月报', '社会实践报告', '求职信']
  },
  {
    name: '校园生活',
    icon: Building2,
    items: ['写报告', '申请书', '计划书', '读后感', '调查问卷', '思想汇报', '自我鉴定', '入党申请书']
  },
  {
    name: '教学助手',
    icon: BookOpen,
    items: ['教材', '专著', '教学设计', '教学反思', '课题申报书', '结题报告', 'AI扩写']
  },
  {
    name: '长文写作',
    icon: FileText,
    items: ['自定义写作', 'AI写书', '写总结', '写方案', 'AI扩写', 'AI仿写']
  },
  {
    name: '办公文案',
    icon: Wrench,
    items: ['写报告', '工作报告', '演讲稿', '心得体会', '写方案', '发言稿']
  },
  {
    name: '分析/报告',
    icon: BarChart3,
    items: ['调研报告', '需求报告', '调查报告', '可行性分析']
  }
];

function Sidebar({ activeCategory, setActiveCategory, activePaperType, setActivePaperType }) {
  return (
    <aside className="w-64 bg-white min-h-[calc(100vh-64px)] shadow-lg overflow-y-auto">
      <div className="p-4">
        {categories.map((category) => {
          const Icon = category.icon;
          const isActive = activeCategory === category.name;
          
          return (
            <div key={category.name} className="mb-2">
              <button
                onClick={() => setActiveCategory(isActive ? '' : category.name)}
                className={`w-full flex items-center justify-between p-3 rounded-lg transition-all duration-200 ${
                  isActive ? 'bg-blue-50 text-blue-600' : 'hover:bg-gray-50 text-gray-700'
                }`}
              >
                <div className="flex items-center space-x-3">
                  <Icon className="w-5 h-5" />
                  <span className="font-medium">{category.name}</span>
                </div>
                <ChevronRight className={`w-4 h-4 transition-transform duration-200 ${isActive ? 'rotate-90' : ''}`} />
              </button>
              
              {isActive && (
                <div className="ml-4 mt-2 space-y-1">
                  {category.items.map((item) => (
                    <button
                      key={item}
                      onClick={() => setActivePaperType(item)}
                      className={`w-full text-left px-4 py-2 rounded-lg text-sm transition-colors duration-200 ${
                        activePaperType === item 
                          ? 'bg-blue-500 text-white' 
                          : 'text-gray-600 hover:bg-gray-100'
                      }`}
                    >
                      {item}
                    </button>
                  ))}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </aside>
  );
}

export default Sidebar;
