import React, { useMemo, useState } from 'react';
import {
  Search,
  FileText,
  Zap,
  Shield,
  CreditCard,
  HelpCircle,
  ChevronDown,
  ChevronUp
} from 'lucide-react';

const helpCategories = [
  {
    icon: FileText,
    title: '论文写作',
    description: '关于论文生成的常见问题',
    articles: [
      { title: '如何生成论文大纲？', content: '在“生成大纲”页面输入论文题目，选择学科专业和论文类型，点击“生成大纲”按钮即可。系统会在10秒内生成结构化的论文大纲。' },
      { title: '如何生成完整论文？', content: '在“开始写作”页面填写论文信息，先生成大纲，确认大纲后点击“生成论文”按钮。系统会根据大纲生成完整的论文内容，通常需要3-5分钟。' },
      { title: '可以修改生成的大纲吗？', content: '可以。生成的大纲显示在文本框中，您可以直接编辑修改，调整章节结构、添加或删除内容，然后再生成论文。' },
      { title: '支持哪些学科专业？', content: '我们支持720+学科专业，涵盖理工医农文法经管等全部学科门类，包括计算机科学、经济学、法学、医学、教育学等。' }
    ]
  },
  {
    icon: Zap,
    title: '功能使用',
    description: '平台功能的使用指南',
    articles: [
      { title: '如何复制论文内容？', content: '在论文生成完成后，点击“复制内容”按钮即可将论文内容复制到剪贴板，然后可以粘贴到Word或其他文档编辑器中。' },
      { title: '如何下载论文？', content: '在论文生成完成后，点击“下载论文”按钮，系统会将论文保存为TXT文件下载到您的电脑。' },
      { title: '生成的论文可以直接使用吗？', content: '我们建议将生成的内容作为参考和初稿，进行人工审核和修改后再使用。直接提交AI生成内容可能违反学术规范。' },
      { title: '论文查重率是多少？', content: '我们的AI生成的论文在知网查重率约为10%左右。如果查重率超过15%，您可以联系客服申请退款。' }
    ]
  },
  {
    icon: CreditCard,
    title: '付费与订阅',
    description: '关于付费和订阅的问题',
    articles: [
      { title: '有哪些付费方案？', content: '我们提供免费版、专业版（99元/月）和企业版（299元/月）三种方案，满足不同用户的需求。' },
      { title: '如何升级到专业版？', content: '在“价格”页面选择专业版方案，点击“立即订阅”按钮，完成支付即可升级。' },
      { title: '可以退款吗？', content: '如果论文查重率超过15%，您可以联系客服申请退款。其他情况下，订阅费用不予退款。' },
      { title: '支持哪些支付方式？', content: '我们支持微信支付、支付宝、银行卡等多种支付方式。' }
    ]
  },
  {
    icon: Shield,
    title: '安全与隐私',
    description: '关于数据安全和隐私保护',
    articles: [
      { title: '我的论文数据安全吗？', content: '我们采用双重认证机制，保证论文一人一稿，防止论文内容被盗。所有数据传输都经过加密处理。' },
      { title: '论文会被保存吗？', content: '为了保护您的隐私，我们不会长期保存您的论文内容。论文生成后，您需要及时下载保存。' },
      { title: '如何保护我的账户安全？', content: '建议使用强密码，定期更换密码，不要在公共设备上保存登录状态。' },
      { title: '如何删除我的账户？', content: '您可以在个人中心申请删除账户，我们会在7个工作日内处理您的请求。' }
    ]
  }
];

function HelpPage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [expandedCategory, setExpandedCategory] = useState(0);
  const [expandedArticle, setExpandedArticle] = useState(null);

  const filteredCategories = useMemo(() => {
    const q = (searchQuery || '').toLowerCase();
    return helpCategories
      .map((category) => ({
        ...category,
        articles: category.articles.filter(
          (article) =>
            article.title.toLowerCase().includes(q) ||
            article.content.toLowerCase().includes(q)
        )
      }))
      .filter((category) => category.articles.length > 0 || !q);
  }, [searchQuery]);

  return (
    <main className="flex-1 p-6 overflow-y-auto">
      <div className="max-w-6xl mx-auto">
        <div className="glass-card p-6 mb-6">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-2xl font-bold text-gray-800 flex items-center">
                <HelpCircle className="w-7 h-7 mr-2 text-blue-500" />
                帮助中心
              </h2>
              <p className="text-gray-500 mt-1">有问题？在这里找到答案</p>
            </div>
            <div className="hidden md:flex items-center space-x-2 text-sm text-gray-500">
              <span className="px-3 py-1 bg-blue-100 text-blue-600 rounded-full">常见问题</span>
              <span className="px-3 py-1 bg-green-100 text-green-600 rounded-full">快速搜索</span>
            </div>
          </div>

          <div className="mt-5 max-w-md relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              type="text"
              placeholder="搜索问题..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="input-field pl-10"
            />
          </div>
        </div>

        <div className="space-y-6">
          {filteredCategories.map((category, categoryIndex) => {
            const Icon = category.icon;
            const isExpanded = expandedCategory === categoryIndex;
            return (
              <div key={category.title} className="glass-card p-6">
                <button
                  className="w-full text-left"
                  onClick={() =>
                    setExpandedCategory(isExpanded ? -1 : categoryIndex)
                  }
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                      <div className="w-10 h-10 rounded-full bg-blue-50 flex items-center justify-center">
                        <Icon className="w-5 h-5 text-blue-600" />
                      </div>
                      <div>
                        <div className="text-lg font-semibold text-gray-800">
                          {category.title}
                        </div>
                        <div className="text-sm text-gray-500">
                          {category.description}
                        </div>
                      </div>
                    </div>
                    {isExpanded ? (
                      <ChevronUp className="w-5 h-5 text-gray-400" />
                    ) : (
                      <ChevronDown className="w-5 h-5 text-gray-400" />
                    )}
                  </div>
                </button>

                {isExpanded && (
                  <div className="mt-4 space-y-3">
                    {category.articles.map((article, articleIndex) => {
                      const key = `${categoryIndex}-${articleIndex}`;
                      const expanded = expandedArticle === key;
                      return (
                        <div key={key} className="border border-gray-200 rounded-lg overflow-hidden bg-white">
                          <button
                            className="w-full px-4 py-3 text-left flex items-center justify-between hover:bg-gray-50 transition-colors"
                            onClick={() => setExpandedArticle(expanded ? null : key)}
                          >
                            <span className="font-medium text-gray-800">{article.title}</span>
                            {expanded ? (
                              <ChevronUp className="w-4 h-4 text-gray-400" />
                            ) : (
                              <ChevronDown className="w-4 h-4 text-gray-400" />
                            )}
                          </button>
                          {expanded && (
                            <div className="px-4 py-3 bg-gray-50 border-t border-gray-200">
                              <p className="text-gray-600 text-sm leading-relaxed">{article.content}</p>
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            );
          })}
        </div>

        <div className="glass-card p-6 mt-6">
          <div className="flex items-start space-x-4">
            <div className="w-12 h-12 rounded-full bg-blue-50 flex items-center justify-center flex-shrink-0">
              <HelpCircle className="w-6 h-6 text-blue-600" />
            </div>
            <div className="flex-1">
              <div className="text-lg font-semibold text-gray-800">还有其他问题？</div>
              <div className="text-sm text-gray-600 mt-1">
                如果您没有找到答案，请联系我们的客服团队
              </div>
              <div className="mt-4">
                <a
                  href="mailto:support@thesis66.com"
                  className="btn-secondary inline-flex"
                >
                  联系客服邮箱
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}

export default HelpPage;
