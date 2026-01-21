"use client";

import { useState } from "react";
import { Search, FileText, Zap, Shield, CreditCard, HelpCircle, ChevronDown, ChevronUp } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";

const helpCategories = [
  {
    icon: FileText,
    title: "论文写作",
    description: "关于论文生成的常见问题",
    articles: [
      { title: "如何生成论文大纲？", content: "在“生成大纲”页面输入论文题目，选择学科专业和论文类型，点击“生成大纲”按钮即可。系统会在10秒内生成结构化的论文大纲。" },
      { title: "如何生成完整论文？", content: "在“开始写作”页面填写论文信息，先生成大纲，确认大纲后点击“生成论文”按钮。系统会根据大纲生成完整的论文内容，通常需要3-5分钟。" },
      { title: "可以修改生成的大纲吗？", content: "可以。生成的大纲显示在文本框中，您可以直接编辑修改，调整章节结构、添加或删除内容，然后再生成论文。" },
      { title: "支持哪些学科专业？", content: "我们支持720+学科专业，涵盖理工医农文法经管等全部学科门类，包括计算机科学、经济学、法学、医学、教育学等。" },
    ],
  },
  {
    icon: Zap,
    title: "功能使用",
    description: "平台功能的使用指南",
    articles: [
      { title: "如何复制论文内容？", content: "在论文生成完成后，点击“复制内容”按钮即可将论文内容复制到剪贴板，然后可以粘贴到Word或其他文档编辑器中。" },
      { title: "如何下载论文？", content: "在论文生成完成后，点击“下载论文”按钮，系统会将论文保存为TXT文件下载到您的电脑。" },
      { title: "生成的论文可以直接使用吗？", content: "我们建议将生成的内容作为参考和初稿，进行人工审核和修改后再使用。直接提交AI生成内容可能违反学术规范。" },
      { title: "论文查重率是多少？", content: "我们的AI生成的论文在知网查重率约为10%左右。如果查重率超过15%，您可以联系客服申请退款。" },
    ],
  },
  {
    icon: CreditCard,
    title: "付费与订阅",
    description: "关于付费和订阅的问题",
    articles: [
      { title: "有哪些付费方案？", content: "我们提供免费版、专业版（99元/月）和企业版（299元/月）三种方案，满足不同用户的需求。" },
      { title: "如何升级到专业版？", content: "在“价格”页面选择专业版方案，点击“立即订阅”按钮，完成支付即可升级。" },
      { title: "可以退款吗？", content: "如果论文查重率超过15%，您可以联系客服申请退款。其他情况下，订阅费用不予退款。" },
      { title: "支持哪些支付方式？", content: "我们支持微信支付、支付宝、银行卡等多种支付方式。" },
    ],
  },
  {
    icon: Shield,
    title: "安全与隐私",
    description: "关于数据安全和隐私保护",
    articles: [
      { title: "我的论文数据安全吗？", content: "我们采用双重认证机制，保证论文一人一稿，防止论文内容被盗。所有数据传输都经过加密处理。" },
      { title: "论文会被保存吗？", content: "为了保护您的隐私，我们不会长期保存您的论文内容。论文生成后，您需要及时下载保存。" },
      { title: "如何保护我的账户安全？", content: "建议使用强密码，定期更换密码，不要在公共设备上保存登录状态。" },
      { title: "如何删除我的账户？", content: "您可以在个人中心申请删除账户，我们会在7个工作日内处理您的请求。" },
    ],
  },
];

export default function HelpPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [expandedCategory, setExpandedCategory] = useState<number | null>(0);
  const [expandedArticle, setExpandedArticle] = useState<string | null>(null);

  const filteredCategories = helpCategories.map((category) => ({
    ...category,
    articles: category.articles.filter(
      (article) =>
        article.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        article.content.toLowerCase().includes(searchQuery.toLowerCase())
    ),
  })).filter((category) => category.articles.length > 0 || searchQuery === "");

  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1 py-12">
        <div className="container mx-auto px-4">
          {/* Header */}
          <div className="text-center mb-12">
            <h1 className="text-4xl font-bold mb-4">帮助中心</h1>
            <p className="text-xl text-muted-foreground mb-8">
              有问题？在这里找到答案
            </p>
            <div className="max-w-md mx-auto relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground" />
              <Input
                type="text"
                placeholder="搜索问题..."
                className="pl-10"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>

          {/* Categories */}
          <div className="max-w-3xl mx-auto space-y-6">
            {filteredCategories.map((category, categoryIndex) => (
              <Card key={categoryIndex}>
                <CardHeader
                  className="cursor-pointer"
                  onClick={() =>
                    setExpandedCategory(
                      expandedCategory === categoryIndex ? null : categoryIndex
                    )
                  }
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                        <category.icon className="h-5 w-5 text-primary" />
                      </div>
                      <div>
                        <CardTitle className="text-lg">{category.title}</CardTitle>
                        <p className="text-sm text-muted-foreground">
                          {category.description}
                        </p>
                      </div>
                    </div>
                    {expandedCategory === categoryIndex ? (
                      <ChevronUp className="h-5 w-5 text-muted-foreground" />
                    ) : (
                      <ChevronDown className="h-5 w-5 text-muted-foreground" />
                    )}
                  </div>
                </CardHeader>
                {expandedCategory === categoryIndex && (
                  <CardContent className="pt-0">
                    <div className="space-y-3">
                      {category.articles.map((article, articleIndex) => (
                        <div
                          key={articleIndex}
                          className="border rounded-lg overflow-hidden"
                        >
                          <button
                            className="w-full px-4 py-3 text-left flex items-center justify-between hover:bg-muted/50 transition-colors"
                            onClick={() =>
                              setExpandedArticle(
                                expandedArticle === `${categoryIndex}-${articleIndex}`
                                  ? null
                                  : `${categoryIndex}-${articleIndex}`
                              )
                            }
                          >
                            <span className="font-medium">{article.title}</span>
                            {expandedArticle === `${categoryIndex}-${articleIndex}` ? (
                              <ChevronUp className="h-4 w-4 text-muted-foreground" />
                            ) : (
                              <ChevronDown className="h-4 w-4 text-muted-foreground" />
                            )}
                          </button>
                          {expandedArticle === `${categoryIndex}-${articleIndex}` && (
                            <div className="px-4 py-3 bg-muted/30 border-t">
                              <p className="text-muted-foreground">{article.content}</p>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  </CardContent>
                )}
              </Card>
            ))}
          </div>

          {/* Contact Support */}
          <div className="mt-12 text-center">
            <Card className="max-w-md mx-auto">
              <CardContent className="pt-6">
                <HelpCircle className="h-12 w-12 text-primary mx-auto mb-4" />
                <h3 className="text-lg font-semibold mb-2">还有其他问题？</h3>
                <p className="text-muted-foreground mb-4">
                  如果您没有找到答案，请联系我们的客服团队
                </p>
                <p className="text-sm">
                  客服邮箱：<a href="mailto:support@thesis66.com" className="text-primary hover:underline">support@thesis66.com</a>
                </p>
              </CardContent>
            </Card>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
}
