"use client";

import { useState } from "react";
import Link from "next/link";
import { 
  FileText, 
  Clock, 
  Download, 
  Trash2, 
  Eye, 
  Plus,
  BarChart3,
  Calendar,
  Star
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import { useToast } from "@/components/ui/use-toast";

// 模拟用户数据
const mockPapers = [
  {
    id: 1,
    title: "人工智能在教育领域的应用研究",
    subject: "计算机科学",
    wordCount: 8500,
    createdAt: "2025-01-20",
    status: "completed",
  },
  {
    id: 2,
    title: "新媒体时代下的品牌营销策略分析",
    subject: "市场营销",
    wordCount: 6200,
    createdAt: "2025-01-18",
    status: "completed",
  },
  {
    id: 3,
    title: "乡村振兴战略下的农村电商发展研究",
    subject: "经济学",
    wordCount: 10000,
    createdAt: "2025-01-15",
    status: "completed",
  },
];

const mockOutlines = [
  {
    id: 1,
    title: "大数据技术在金融风控中的应用",
    subject: "金融学",
    createdAt: "2025-01-21",
  },
  {
    id: 2,
    title: "后疫情时代旅游业复苏策略研究",
    subject: "旅游管理",
    createdAt: "2025-01-19",
  },
];

const stats = [
  { label: "生成论文", value: "3", icon: FileText },
  { label: "生成大纲", value: "5", icon: BarChart3 },
  { label: "本月使用", value: "8次", icon: Calendar },
  { label: "会员等级", value: "免费版", icon: Star },
];

export default function DashboardPage() {
  const [papers] = useState(mockPapers);
  const [outlines] = useState(mockOutlines);
  const { toast } = useToast();

  const handleDelete = (type: string, id: number) => {
    toast({
      title: "删除成功",
      description: `${type}已删除`,
    });
  };

  const handleDownload = (title: string) => {
    toast({
      title: "开始下载",
      description: `正在下载《${title}》`,
    });
  };

  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1 py-8">
        <div className="container mx-auto px-4">
          {/* Welcome Section */}
          <div className="mb-8">
            <h1 className="text-3xl font-bold mb-2">欢迎回来，用户</h1>
            <p className="text-muted-foreground">
              管理您的论文和大纲，查看使用统计
            </p>
          </div>

          {/* Stats */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
            {stats.map((stat, index) => (
              <Card key={index}>
                <CardContent className="pt-6">
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                      <stat.icon className="h-5 w-5 text-primary" />
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">{stat.label}</p>
                      <p className="text-2xl font-bold">{stat.value}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>

          {/* Quick Actions */}
          <div className="flex flex-wrap gap-4 mb-8">
            <Button asChild>
              <Link href="/write">
                <Plus className="mr-2 h-4 w-4" />
                新建论文
              </Link>
            </Button>
            <Button variant="outline" asChild>
              <Link href="/outline">
                <FileText className="mr-2 h-4 w-4" />
                生成大纲
              </Link>
            </Button>
            <Button variant="outline" asChild>
              <Link href="/check">
                <BarChart3 className="mr-2 h-4 w-4" />
                论文检测
              </Link>
            </Button>
          </div>

          {/* Content Tabs */}
          <Tabs defaultValue="papers" className="w-full">
            <TabsList>
              <TabsTrigger value="papers">我的论文</TabsTrigger>
              <TabsTrigger value="outlines">我的大纲</TabsTrigger>
            </TabsList>

            <TabsContent value="papers" className="mt-6">
              {papers.length > 0 ? (
                <div className="space-y-4">
                  {papers.map((paper) => (
                    <Card key={paper.id}>
                      <CardContent className="pt-6">
                        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                          <div className="flex-1">
                            <h3 className="font-semibold text-lg mb-1">
                              {paper.title}
                            </h3>
                            <div className="flex flex-wrap gap-4 text-sm text-muted-foreground">
                              <span className="flex items-center gap-1">
                                <FileText className="h-4 w-4" />
                                {paper.subject}
                              </span>
                              <span className="flex items-center gap-1">
                                <BarChart3 className="h-4 w-4" />
                                {paper.wordCount}字
                              </span>
                              <span className="flex items-center gap-1">
                                <Clock className="h-4 w-4" />
                                {paper.createdAt}
                              </span>
                            </div>
                          </div>
                          <div className="flex gap-2">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => toast({ title: "查看论文", description: "功能开发中" })}
                            >
                              <Eye className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleDownload(paper.title)}
                            >
                              <Download className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleDelete("论文", paper.id)}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              ) : (
                <Card>
                  <CardContent className="pt-6 text-center py-12">
                    <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                    <h3 className="text-lg font-semibold mb-2">暂无论文</h3>
                    <p className="text-muted-foreground mb-4">
                      您还没有生成任何论文，开始创建您的第一篇论文吧
                    </p>
                    <Button asChild>
                      <Link href="/write">开始写作</Link>
                    </Button>
                  </CardContent>
                </Card>
              )}
            </TabsContent>

            <TabsContent value="outlines" className="mt-6">
              {outlines.length > 0 ? (
                <div className="space-y-4">
                  {outlines.map((outline) => (
                    <Card key={outline.id}>
                      <CardContent className="pt-6">
                        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                          <div className="flex-1">
                            <h3 className="font-semibold text-lg mb-1">
                              {outline.title}
                            </h3>
                            <div className="flex flex-wrap gap-4 text-sm text-muted-foreground">
                              <span className="flex items-center gap-1">
                                <FileText className="h-4 w-4" />
                                {outline.subject}
                              </span>
                              <span className="flex items-center gap-1">
                                <Clock className="h-4 w-4" />
                                {outline.createdAt}
                              </span>
                            </div>
                          </div>
                          <div className="flex gap-2">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => toast({ title: "查看大纲", description: "功能开发中" })}
                            >
                              <Eye className="h-4 w-4" />
                            </Button>
                            <Button
                              size="sm"
                              onClick={() => toast({ title: "生成论文", description: "功能开发中" })}
                            >
                              生成论文
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleDelete("大纲", outline.id)}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              ) : (
                <Card>
                  <CardContent className="pt-6 text-center py-12">
                    <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                    <h3 className="text-lg font-semibold mb-2">暂无大纲</h3>
                    <p className="text-muted-foreground mb-4">
                      您还没有生成任何大纲，开始创建您的第一个大纲吧
                    </p>
                    <Button asChild>
                      <Link href="/outline">生成大纲</Link>
                    </Button>
                  </CardContent>
                </Card>
              )}
            </TabsContent>
          </Tabs>

          {/* Upgrade Banner */}
          <Card className="mt-8 bg-gradient-to-r from-primary/10 to-primary/5 border-primary/20">
            <CardContent className="pt-6">
              <div className="flex flex-col md:flex-row items-center justify-between gap-4">
                <div>
                  <h3 className="text-lg font-semibold mb-1">升级到专业版</h3>
                  <p className="text-muted-foreground">
                    解锁无限次论文生成、更多学科支持和优先客服
                  </p>
                </div>
                <Button asChild>
                  <Link href="/pricing">查看方案</Link>
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </main>

      <Footer />
    </div>
  );
}
