"use client";

import { useState } from "react";
import Link from "next/link";
import { FileText, Eye, ChevronRight, Star, Clock, BookOpen } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";

const examples = {
  outlines: [
    {
      id: 1,
      title: "人工智能在医疗诊断中的应用研究",
      subject: "计算机科学",
      level: "硕士",
      preview: `一、绪论
  1.1 研究背景
  1.2 研究目的与意义
  1.3 国内外研究现状
  1.4 研究方法与技术路线

二、人工智能与医疗诊断概述
  2.1 人工智能技术发展历程
  2.2 医疗诊断的基本流程
  2.3 AI在医疗领域的应用场景

三、基于深度学习的医学影像诊断
  3.1 医学影像数据特点
  3.2 卷积神经网络在影像诊断中的应用
  3.3 典型案例分析

四、AI辅助诊断系统设计与实现
  4.1 系统架构设计
  4.2 核心算法实现
  4.3 系统测试与评估

五、结论与展望
  5.1 研究结论
  5.2 研究局限性
  5.3 未来研究方向`,
    },
    {
      id: 2,
      title: "新媒体环境下企业品牌传播策略研究",
      subject: "市场营销",
      level: "本科",
      preview: `一、绪论
  1.1 研究背景
  1.2 研究意义
  1.3 文献综述
  1.4 研究方法

二、新媒体与品牌传播理论基础
  2.1 新媒体的概念与特征
  2.2 品牌传播理论
  2.3 新媒体对品牌传播的影响

三、企业新媒体品牌传播现状分析
  3.1 主要新媒体平台分析
  3.2 企业新媒体运营现状
  3.3 存在的问题与挑战

四、新媒体品牌传播策略
  4.1 内容营销策略
  4.2 社交媒体互动策略
  4.3 KOL合作策略
  4.4 数据驱动的精准传播

五、案例分析
  5.1 成功案例分析
  5.2 经验总结

六、结论与建议`,
    },
    {
      id: 3,
      title: "乡村振兴战略下农村电商发展路径研究",
      subject: "经济学",
      level: "硕士",
      preview: `一、绪论
  1.1 研究背景与意义
  1.2 国内外研究综述
  1.3 研究内容与方法
  1.4 创新点

二、相关概念与理论基础
  2.1 乡村振兴战略内涵
  2.2 农村电商概念界定
  2.3 理论基础

三、我国农村电商发展现状
  3.1 发展历程
  3.2 发展规模与特点
  3.3 主要模式分析

四、农村电商发展面临的问题
  4.1 基础设施不完善
  4.2 人才短缺
  4.3 物流配送困难
  4.4 品牌建设不足

五、农村电商发展路径优化
  5.1 完善基础设施建设
  5.2 加强人才培养
  5.3 创新物流模式
  5.4 打造区域品牌

六、结论与展望`,
    },
  ],
  papers: [
    {
      id: 1,
      title: "基于机器学习的股票价格预测研究",
      subject: "金融学",
      level: "本科",
      wordCount: 8000,
      abstract: "本文研究了机器学习算法在股票价格预测中的应用。通过对比分析LSTM、随机森林、支持向量机等算法的预测效果，发现LSTM模型在捕捉股价时间序列特征方面具有明显优势...",
    },
    {
      id: 2,
      title: "社交媒体对大学生消费行为的影响研究",
      subject: "市场营销",
      level: "本科",
      wordCount: 6500,
      abstract: "本研究以问卷调查和深度访谈为主要研究方法，探讨社交媒体对大学生消费行为的影响机制。研究发现，社交媒体通过信息传播、社会比较、意见领袖影响等途径显著影响大学生的消费决策...",
    },
    {
      id: 3,
      title: "双减政策下小学课后服务质量提升策略研究",
      subject: "教育学",
      level: "硕士",
      wordCount: 12000,
      abstract: "本文以双减政策为背景，通过对多所小学课后服务实施情况的调研，分析当前课后服务存在的问题，并从课程设计、师资配置、管理机制等方面提出质量提升策略...",
    },
  ],
};

export default function ExamplesPage() {
  const [selectedOutline, setSelectedOutline] = useState(examples.outlines[0]);

  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1 py-12">
        <div className="container mx-auto px-4">
          {/* Header */}
          <div className="text-center mb-12">
            <h1 className="text-4xl font-bold mb-4">论文示例</h1>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              查看AI生成的论文大纲和论文示例，了解我们的写作质量
            </p>
          </div>

          <Tabs defaultValue="outlines" className="w-full">
            <TabsList className="grid w-full max-w-md mx-auto grid-cols-2 mb-8">
              <TabsTrigger value="outlines">大纲示例</TabsTrigger>
              <TabsTrigger value="papers">论文示例</TabsTrigger>
            </TabsList>

            {/* Outlines Tab */}
            <TabsContent value="outlines">
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Outline List */}
                <div className="space-y-4">
                  {examples.outlines.map((outline) => (
                    <Card
                      key={outline.id}
                      className={`cursor-pointer transition-all ${
                        selectedOutline.id === outline.id
                          ? "border-primary shadow-md"
                          : "hover:border-primary/50"
                      }`}
                      onClick={() => setSelectedOutline(outline)}
                    >
                      <CardContent className="pt-4">
                        <div className="flex items-start justify-between">
                          <div className="flex-1">
                            <h3 className="font-semibold mb-1 line-clamp-2">
                              {outline.title}
                            </h3>
                            <div className="flex gap-2 text-xs text-muted-foreground">
                              <span className="bg-muted px-2 py-0.5 rounded">
                                {outline.subject}
                              </span>
                              <span className="bg-muted px-2 py-0.5 rounded">
                                {outline.level}
                              </span>
                            </div>
                          </div>
                          <ChevronRight className="h-5 w-5 text-muted-foreground flex-shrink-0" />
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>

                {/* Outline Preview */}
                <div className="lg:col-span-2">
                  <Card className="h-full">
                    <CardHeader>
                      <CardTitle>{selectedOutline.title}</CardTitle>
                      <CardDescription>
                        {selectedOutline.subject} · {selectedOutline.level}论文大纲
                      </CardDescription>
                    </CardHeader>
                    <CardContent>
                      <pre className="whitespace-pre-wrap text-sm bg-muted/50 p-4 rounded-lg overflow-auto max-h-[500px]">
                        {selectedOutline.preview}
                      </pre>
                      <div className="mt-4 flex gap-4">
                        <Button asChild>
                          <Link href="/outline">生成类似大纲</Link>
                        </Button>
                        <Button variant="outline" asChild>
                          <Link href="/write">基于此大纲写作</Link>
                        </Button>
                      </div>
                    </CardContent>
                  </Card>
                </div>
              </div>
            </TabsContent>

            {/* Papers Tab */}
            <TabsContent value="papers">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {examples.papers.map((paper) => (
                  <Card key={paper.id} className="flex flex-col">
                    <CardHeader>
                      <div className="flex items-center gap-2 mb-2">
                        <FileText className="h-5 w-5 text-primary" />
                        <span className="text-xs bg-primary/10 text-primary px-2 py-0.5 rounded">
                          {paper.level}
                        </span>
                      </div>
                      <CardTitle className="text-lg line-clamp-2">
                        {paper.title}
                      </CardTitle>
                      <CardDescription className="line-clamp-3">
                        {paper.abstract}
                      </CardDescription>
                    </CardHeader>
                    <CardContent className="flex-1 flex flex-col justify-end">
                      <div className="flex items-center gap-4 text-sm text-muted-foreground mb-4">
                        <span className="flex items-center gap-1">
                          <BookOpen className="h-4 w-4" />
                          {paper.subject}
                        </span>
                        <span className="flex items-center gap-1">
                          <FileText className="h-4 w-4" />
                          {paper.wordCount}字
                        </span>
                      </div>
                      <Button variant="outline" className="w-full">
                        <Eye className="h-4 w-4 mr-2" />
                        查看详情
                      </Button>
                    </CardContent>
                  </Card>
                ))}
              </div>

              <div className="mt-8 text-center">
                <p className="text-muted-foreground mb-4">
                  以上示例均由AI生成，仅供参考
                </p>
                <Button size="lg" asChild>
                  <Link href="/write">立即开始写作</Link>
                </Button>
              </div>
            </TabsContent>
          </Tabs>

          {/* Features */}
          <div className="mt-16 grid grid-cols-1 md:grid-cols-3 gap-6">
            <Card>
              <CardContent className="pt-6 text-center">
                <Star className="h-10 w-10 text-primary mx-auto mb-4" />
                <h3 className="font-semibold mb-2">高质量输出</h3>
                <p className="text-sm text-muted-foreground">
                  基于先进AI模型，生成结构清晰、逻辑严密的学术内容
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="pt-6 text-center">
                <Clock className="h-10 w-10 text-primary mx-auto mb-4" />
                <h3 className="font-semibold mb-2">极速生成</h3>
                <p className="text-sm text-muted-foreground">
                  10秒生成大纲，3分钟完成万字论文初稿
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="pt-6 text-center">
                <BookOpen className="h-10 w-10 text-primary mx-auto mb-4" />
                <h3 className="font-semibold mb-2">720+学科</h3>
                <p className="text-sm text-muted-foreground">
                  覆盖理工医农文法经管等全部学科门类
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
