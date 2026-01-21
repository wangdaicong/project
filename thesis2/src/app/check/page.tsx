"use client";

import { useState } from "react";
import { Upload, FileText, CheckCircle, AlertCircle, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import { useToast } from "@/components/ui/use-toast";

export default function CheckPage() {
  const [content, setContent] = useState("");
  const [isChecking, setIsChecking] = useState(false);
  const [result, setResult] = useState<{
    similarity: number;
    aiProbability: number;
    wordCount: number;
    suggestions: string[];
  } | null>(null);
  const { toast } = useToast();

  const handleCheck = async () => {
    if (!content.trim()) {
      toast({
        title: "请输入内容",
        description: "请粘贴或输入需要检测的论文内容",
        variant: "destructive",
      });
      return;
    }

    if (content.length < 100) {
      toast({
        title: "内容太短",
        description: "请输入至少100字的内容进行检测",
        variant: "destructive",
      });
      return;
    }

    setIsChecking(true);
    setResult(null);

    // 模拟检测过程
    await new Promise((resolve) => setTimeout(resolve, 3000));

    // 模拟检测结果
    const wordCount = content.length;
    const similarity = Math.random() * 15 + 5; // 5-20%
    const aiProbability = Math.random() * 30 + 10; // 10-40%

    const suggestions = [
      "建议对第2段进行改写，降低与已有文献的相似度",
      "第4段的表述方式较为常见，建议使用更具个人特色的表达",
      "引用部分建议添加更多原创分析",
      "结论部分可以增加更多个人见解",
    ];

    setResult({
      similarity: Math.round(similarity * 10) / 10,
      aiProbability: Math.round(aiProbability * 10) / 10,
      wordCount,
      suggestions: suggestions.slice(0, Math.floor(Math.random() * 3) + 2),
    });

    setIsChecking(false);
  };

  const getSimilarityColor = (value: number) => {
    if (value < 10) return "text-green-500";
    if (value < 20) return "text-yellow-500";
    return "text-red-500";
  };

  const getAIProbabilityColor = (value: number) => {
    if (value < 20) return "text-green-500";
    if (value < 40) return "text-yellow-500";
    return "text-red-500";
  };

  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1 py-12">
        <div className="container mx-auto px-4">
          {/* Header */}
          <div className="text-center mb-12">
            <h1 className="text-4xl font-bold mb-4">论文检测</h1>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              智能检测论文查重率和AI生成概率，提供优化建议
            </p>
          </div>

          <div className="max-w-4xl mx-auto">
            <Tabs defaultValue="text" className="w-full">
              <TabsList className="grid w-full grid-cols-2 mb-6">
                <TabsTrigger value="text">粘贴文本</TabsTrigger>
                <TabsTrigger value="file">上传文件</TabsTrigger>
              </TabsList>

              <TabsContent value="text">
                <Card>
                  <CardHeader>
                    <CardTitle>输入论文内容</CardTitle>
                    <CardDescription>
                      粘贴您的论文内容，我们将进行智能检测
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <Textarea
                      placeholder="请粘贴您的论文内容（至少100字）..."
                      rows={12}
                      value={content}
                      onChange={(e) => setContent(e.target.value)}
                    />
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">
                        当前字数：{content.length}
                      </span>
                      <Button onClick={handleCheck} disabled={isChecking}>
                        {isChecking ? (
                          <>
                            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                            检测中...
                          </>
                        ) : (
                          <>
                            <CheckCircle className="mr-2 h-4 w-4" />
                            开始检测
                          </>
                        )}
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              </TabsContent>

              <TabsContent value="file">
                <Card>
                  <CardHeader>
                    <CardTitle>上传文件</CardTitle>
                    <CardDescription>
                      支持 .doc, .docx, .pdf, .txt 格式
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="border-2 border-dashed rounded-lg p-12 text-center">
                      <Upload className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                      <p className="text-lg font-medium mb-2">
                        拖拽文件到此处或点击上传
                      </p>
                      <p className="text-sm text-muted-foreground mb-4">
                        支持 Word、PDF、TXT 格式，最大 10MB
                      </p>
                      <Button
                        variant="outline"
                        onClick={() =>
                          toast({
                            title: "功能开发中",
                            description: "文件上传功能正在开发中，请使用粘贴文本方式",
                          })
                        }
                      >
                        选择文件
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              </TabsContent>
            </Tabs>

            {/* Results */}
            {result && (
              <div className="mt-8 space-y-6">
                <h2 className="text-2xl font-bold">检测结果</h2>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <Card>
                    <CardContent className="pt-6">
                      <div className="text-center">
                        <FileText className="h-8 w-8 text-primary mx-auto mb-2" />
                        <p className="text-sm text-muted-foreground">字数统计</p>
                        <p className="text-3xl font-bold">{result.wordCount}</p>
                      </div>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardContent className="pt-6">
                      <div className="text-center">
                        <CheckCircle className={`h-8 w-8 mx-auto mb-2 ${getSimilarityColor(result.similarity)}`} />
                        <p className="text-sm text-muted-foreground">文本相似度</p>
                        <p className={`text-3xl font-bold ${getSimilarityColor(result.similarity)}`}>
                          {result.similarity}%
                        </p>
                      </div>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardContent className="pt-6">
                      <div className="text-center">
                        <AlertCircle className={`h-8 w-8 mx-auto mb-2 ${getAIProbabilityColor(result.aiProbability)}`} />
                        <p className="text-sm text-muted-foreground">AI生成概率</p>
                        <p className={`text-3xl font-bold ${getAIProbabilityColor(result.aiProbability)}`}>
                          {result.aiProbability}%
                        </p>
                      </div>
                    </CardContent>
                  </Card>
                </div>

                {/* Detailed Analysis */}
                <Card>
                  <CardHeader>
                    <CardTitle>详细分析</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-6">
                    <div>
                      <div className="flex justify-between mb-2">
                        <span>文本相似度</span>
                        <span className={getSimilarityColor(result.similarity)}>
                          {result.similarity}%
                        </span>
                      </div>
                      <Progress value={result.similarity} className="h-2" />
                      <p className="text-sm text-muted-foreground mt-2">
                        {result.similarity < 10
                          ? "相似度较低，符合原创要求"
                          : result.similarity < 20
                          ? "相似度适中，建议适当修改"
                          : "相似度较高，建议大幅修改"}
                      </p>
                    </div>

                    <div>
                      <div className="flex justify-between mb-2">
                        <span>AI生成概率</span>
                        <span className={getAIProbabilityColor(result.aiProbability)}>
                          {result.aiProbability}%
                        </span>
                      </div>
                      <Progress value={result.aiProbability} className="h-2" />
                      <p className="text-sm text-muted-foreground mt-2">
                        {result.aiProbability < 20
                          ? "AI特征较低，内容较为自然"
                          : result.aiProbability < 40
                          ? "存在一定AI特征，建议人工润色"
                          : "AI特征明显，建议大幅改写"}
                      </p>
                    </div>
                  </CardContent>
                </Card>

                {/* Suggestions */}
                <Card>
                  <CardHeader>
                    <CardTitle>优化建议</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <ul className="space-y-3">
                      {result.suggestions.map((suggestion, index) => (
                        <li key={index} className="flex items-start gap-3">
                          <span className="w-6 h-6 rounded-full bg-primary/10 text-primary text-sm flex items-center justify-center flex-shrink-0">
                            {index + 1}
                          </span>
                          <span className="text-muted-foreground">{suggestion}</span>
                        </li>
                      ))}
                    </ul>
                  </CardContent>
                </Card>
              </div>
            )}

            {/* Info Cards */}
            <div className="mt-12 grid grid-cols-1 md:grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">关于查重检测</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground text-sm">
                    本检测工具提供预估的文本相似度分析，帮助您在正式提交前了解论文的原创性。
                    实际查重结果以知网、维普、万方等官方检测系统为准。
                  </p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">关于AI检测</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground text-sm">
                    AI生成概率检测可以帮助您了解文本的AI特征程度。建议对AI生成的内容进行
                    人工修改和润色，增加个人特色和原创性。
                  </p>
                </CardContent>
              </Card>
            </div>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
}
