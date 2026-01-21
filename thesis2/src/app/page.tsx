"use client";

import Link from "next/link";
import { 
  FileText, 
  Zap, 
  Shield, 
  Clock, 
  CheckCircle, 
  ArrowRight,
  BookOpen,
  GraduationCap,
  Briefcase,
  Users
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";

const features = [
  {
    icon: Clock,
    title: "10秒生成大纲",
    description: "输入论文题目，AI智能分析并生成结构化大纲，支持自定义修改"
  },
  {
    icon: Zap,
    title: "3分钟万字论文",
    description: "基于AI深度优化引擎，极速生成高质量论文初稿"
  },
  {
    icon: Shield,
    title: "低查重保障",
    description: "知网查重率10%左右，超过15%可申请退款"
  },
  {
    icon: CheckCircle,
    title: "720+学科覆盖",
    description: "覆盖理工医农文法经管等全部学科门类"
  }
];

const steps = [
  {
    step: "01",
    title: "输入论文信息",
    description: "填写论文题目、学科专业、字数要求等基本信息"
  },
  {
    step: "02",
    title: "生成论文大纲",
    description: "AI智能分析，10秒生成结构化大纲，支持自定义调整"
  },
  {
    step: "03",
    title: "生成论文全文",
    description: "基于大纲，3分钟生成万字论文初稿"
  },
  {
    step: "04",
    title: "下载论文",
    description: "双重认证保护，安全下载您的专属论文"
  }
];

const userTypes = [
  {
    icon: GraduationCap,
    title: "学生群体",
    description: "快速完成课程论文、毕业论文及开题报告"
  },
  {
    icon: BookOpen,
    title: "研究人员",
    description: "高效生成研究报告与期刊论文初稿"
  },
  {
    icon: Briefcase,
    title: "职场人士",
    description: "一键产出行业分析、项目方案等专业文档"
  },
  {
    icon: Users,
    title: "教育工作者",
    description: "辅助完成教学研究论文与学术材料"
  }
];

export default function HomePage() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      
      <main className="flex-1">
        {/* Hero Section */}
        <section className="relative py-20 lg:py-32 overflow-hidden">
          <div className="absolute inset-0 hero-gradient opacity-10"></div>
          <div className="container mx-auto px-4 relative">
            <div className="max-w-4xl mx-auto text-center">
              <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold mb-6 animate-fade-in">
                <span className="gradient-text">AI智能论文写作</span>
                <br />
                <span className="text-foreground">让学术创作更高效</span>
              </h1>
              <p className="text-xl text-muted-foreground mb-8 max-w-2xl mx-auto">
                覆盖720+学科专业，10秒生成大纲，3分钟生成万字论文
                <br />
                知网查重率10%左右，超过15%可退款
              </p>
              <div className="flex flex-col sm:flex-row gap-4 justify-center">
                <Button size="xl" asChild className="animate-pulse-glow">
                  <Link href="/write">
                    <FileText className="mr-2 h-5 w-5" />
                    开始写作
                  </Link>
                </Button>
                <Button size="xl" variant="outline" asChild>
                  <Link href="/outline">
                    生成论文大纲
                    <ArrowRight className="ml-2 h-5 w-5" />
                  </Link>
                </Button>
              </div>
              <p className="mt-6 text-sm text-muted-foreground">
                已有 <span className="font-semibold text-primary">100,000+</span> 用户使用
              </p>
            </div>
          </div>
        </section>

        {/* Features Section */}
        <section className="py-20 bg-muted/30">
          <div className="container mx-auto px-4">
            <div className="text-center mb-12">
              <h2 className="text-3xl font-bold mb-4">为什么选择 Easy AI</h2>
              <p className="text-muted-foreground max-w-2xl mx-auto">
                采用最先进的AI技术，为您提供高效、安全、专业的论文写作服务
              </p>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {features.map((feature, index) => (
                <Card key={index} className="card-hover border-0 shadow-lg">
                  <CardHeader>
                    <div className="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center mb-4">
                      <feature.icon className="h-6 w-6 text-primary" />
                    </div>
                    <CardTitle className="text-xl">{feature.title}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <CardDescription className="text-base">
                      {feature.description}
                    </CardDescription>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        </section>

        {/* How It Works Section */}
        <section className="py-20">
          <div className="container mx-auto px-4">
            <div className="text-center mb-12">
              <h2 className="text-3xl font-bold mb-4">简单四步，轻松完成论文</h2>
              <p className="text-muted-foreground max-w-2xl mx-auto">
                从输入题目到下载成稿，全程智能化操作
              </p>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
              {steps.map((item, index) => (
                <div key={index} className="relative">
                  <div className="text-6xl font-bold text-primary/10 mb-4">
                    {item.step}
                  </div>
                  <h3 className="text-xl font-semibold mb-2">{item.title}</h3>
                  <p className="text-muted-foreground">{item.description}</p>
                  {index < steps.length - 1 && (
                    <div className="hidden lg:block absolute top-8 right-0 w-1/2 h-0.5 bg-gradient-to-r from-primary/20 to-transparent"></div>
                  )}
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* User Types Section */}
        <section className="py-20 bg-muted/30">
          <div className="container mx-auto px-4">
            <div className="text-center mb-12">
              <h2 className="text-3xl font-bold mb-4">适用人群</h2>
              <p className="text-muted-foreground max-w-2xl mx-auto">
                无论您是学生、研究人员还是职场人士，我们都能满足您的需求
              </p>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {userTypes.map((type, index) => (
                <Card key={index} className="card-hover text-center">
                  <CardHeader>
                    <div className="w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center mx-auto mb-4">
                      <type.icon className="h-8 w-8 text-primary" />
                    </div>
                    <CardTitle>{type.title}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <CardDescription>{type.description}</CardDescription>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        </section>

        {/* CTA Section */}
        <section className="py-20">
          <div className="container mx-auto px-4">
            <div className="max-w-4xl mx-auto text-center bg-gradient-to-r from-primary to-purple-600 rounded-2xl p-12 text-white">
              <h2 className="text-3xl font-bold mb-4">准备好开始了吗？</h2>
              <p className="text-lg opacity-90 mb-8">
                立即体验AI论文写作，让您的学术创作更加高效
              </p>
              <Button size="xl" variant="secondary" asChild>
                <Link href="/write">
                  开始写作
                  <ArrowRight className="ml-2 h-5 w-5" />
                </Link>
              </Button>
            </div>
          </div>
        </section>

        {/* Disclaimer */}
        <section className="py-8 bg-yellow-50 dark:bg-yellow-900/20 border-y border-yellow-200 dark:border-yellow-800">
          <div className="container mx-auto px-4">
            <div className="flex items-start gap-4 max-w-4xl mx-auto">
              <Shield className="h-6 w-6 text-yellow-600 flex-shrink-0 mt-1" />
              <div>
                <h3 className="font-semibold text-yellow-800 dark:text-yellow-200 mb-2">
                  使用提示
                </h3>
                <p className="text-sm text-yellow-700 dark:text-yellow-300">
                  本平台生成的内容仅供学习参考使用。直接提交AI生成论文可能违反学术规范，
                  请务必进行人工审核和修改。AI生成内容的著作权归属存在法律模糊区，
                  重要成果建议进行原创认证登记。
                </p>
              </div>
            </div>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  );
}
