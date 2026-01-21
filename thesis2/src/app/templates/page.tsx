"use client";

import { useState } from "react";
import Link from "next/link";
import { FileText, Download, Eye, Search, Filter } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import { useToast } from "@/components/ui/use-toast";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

const templates = [
  {
    id: 1,
    title: "本科毕业论文模板",
    category: "毕业论文",
    subject: "通用",
    description: "标准本科毕业论文格式，包含封面、摘要、目录、正文、参考文献等完整结构",
    downloads: 12580,
  },
  {
    id: 2,
    title: "硕士学位论文模板",
    category: "毕业论文",
    subject: "通用",
    description: "符合硕士学位论文要求的完整模板，包含中英文摘要、文献综述等",
    downloads: 8920,
  },
  {
    id: 3,
    title: "开题报告模板",
    category: "开题报告",
    subject: "通用",
    description: "研究生开题报告标准模板，包含研究背景、文献综述、研究方法等",
    downloads: 6540,
  },
  {
    id: 4,
    title: "课程论文模板",
    category: "课程论文",
    subject: "通用",
    description: "适用于各类课程论文的通用模板，结构简洁规范",
    downloads: 15230,
  },
  {
    id: 5,
    title: "计算机专业毕业论文模板",
    category: "毕业论文",
    subject: "计算机科学",
    description: "计算机专业毕业论文模板，包含系统设计、实现、测试等章节",
    downloads: 4560,
  },
  {
    id: 6,
    title: "经济学论文模板",
    category: "毕业论文",
    subject: "经济学",
    description: "经济学专业论文模板，包含理论分析、实证研究、政策建议等",
    downloads: 3890,
  },
  {
    id: 7,
    title: "教育学论文模板",
    category: "毕业论文",
    subject: "教育学",
    description: "教育学专业论文模板，适用于教育研究类论文",
    downloads: 2780,
  },
  {
    id: 8,
    title: "期刊论文模板",
    category: "期刊论文",
    subject: "通用",
    description: "学术期刊投稿论文模板，符合主流期刊格式要求",
    downloads: 5670,
  },
  {
    id: 9,
    title: "文献综述模板",
    category: "研究报告",
    subject: "通用",
    description: "文献综述写作模板，帮助系统梳理研究领域文献",
    downloads: 4120,
  },
  {
    id: 10,
    title: "调研报告模板",
    category: "研究报告",
    subject: "管理学",
    description: "市场调研报告模板，包含调研方法、数据分析、结论建议",
    downloads: 3450,
  },
];

const categories = ["全部", "毕业论文", "课程论文", "开题报告", "期刊论文", "研究报告"];

export default function TemplatesPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("全部");
  const { toast } = useToast();

  const filteredTemplates = templates.filter((template) => {
    const matchesSearch =
      template.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      template.description.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory =
      selectedCategory === "全部" || template.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  const handleDownload = (title: string) => {
    toast({
      title: "开始下载",
      description: `正在下载《${title}》`,
    });
  };

  const handlePreview = (title: string) => {
    toast({
      title: "预览功能",
      description: `预览功能开发中`,
    });
  };

  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1 py-12">
        <div className="container mx-auto px-4">
          {/* Header */}
          <div className="text-center mb-12">
            <h1 className="text-4xl font-bold mb-4">论文模板库</h1>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              精选各类论文模板，助您快速开始写作
            </p>
          </div>

          {/* Filters */}
          <div className="flex flex-col md:flex-row gap-4 mb-8">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="搜索模板..."
                className="pl-10"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <Select value={selectedCategory} onValueChange={setSelectedCategory}>
              <SelectTrigger className="w-full md:w-48">
                <Filter className="h-4 w-4 mr-2" />
                <SelectValue placeholder="选择分类" />
              </SelectTrigger>
              <SelectContent>
                {categories.map((category) => (
                  <SelectItem key={category} value={category}>
                    {category}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {/* Templates Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredTemplates.map((template) => (
              <Card key={template.id} className="flex flex-col">
                <CardHeader>
                  <div className="flex items-start justify-between">
                    <div className="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center mb-3">
                      <FileText className="h-6 w-6 text-primary" />
                    </div>
                    <span className="text-xs bg-muted px-2 py-1 rounded">
                      {template.category}
                    </span>
                  </div>
                  <CardTitle className="text-lg">{template.title}</CardTitle>
                  <CardDescription>{template.description}</CardDescription>
                </CardHeader>
                <CardContent className="flex-1 flex flex-col justify-end">
                  <div className="flex items-center justify-between text-sm text-muted-foreground mb-4">
                    <span>{template.subject}</span>
                    <span>{template.downloads.toLocaleString()} 次下载</span>
                  </div>
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      className="flex-1"
                      onClick={() => handlePreview(template.title)}
                    >
                      <Eye className="h-4 w-4 mr-1" />
                      预览
                    </Button>
                    <Button
                      size="sm"
                      className="flex-1"
                      onClick={() => handleDownload(template.title)}
                    >
                      <Download className="h-4 w-4 mr-1" />
                      下载
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>

          {filteredTemplates.length === 0 && (
            <div className="text-center py-12">
              <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">未找到相关模板</h3>
              <p className="text-muted-foreground">
                尝试使用其他关键词或分类进行搜索
              </p>
            </div>
          )}

          {/* CTA */}
          <div className="mt-12 text-center">
            <Card className="max-w-2xl mx-auto bg-primary/5 border-primary/20">
              <CardContent className="pt-6">
                <h3 className="text-xl font-semibold mb-2">
                  没有找到合适的模板？
                </h3>
                <p className="text-muted-foreground mb-4">
                  使用我们的AI写作功能，自动生成符合您需求的论文结构
                </p>
                <Button asChild>
                  <Link href="/write">开始AI写作</Link>
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
}
