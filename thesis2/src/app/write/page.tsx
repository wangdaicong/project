"use client";

import { useEffect, useRef, useState } from "react";
import { FileText, Loader2, Download, Copy, Check, ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import { useToast } from "@/components/ui/use-toast";
import { subjectCategories, paperTypes, wordCounts, educationLevels } from "@/lib/subjects";

type ModelMode = "standard" | "genius";
type OutlineDepth = "2" | "3";
type Language = "zh" | "en" | "ja" | "ko" | "ru" | "th";

export default function WritePage() {
  const [step, setStep] = useState(1);
  const autoGeneratePaperRef = useRef(false);
  const [title, setTitle] = useState("");
  const [subject, setSubject] = useState("");
  const [paperType, setPaperType] = useState("");
  const [wordCount, setWordCount] = useState("");
  const [educationLevel, setEducationLevel] = useState("");
  const [modelMode, setModelMode] = useState<ModelMode>("genius");
  const [outlineDepth, setOutlineDepth] = useState<OutlineDepth>("2");
  const [language, setLanguage] = useState<Language>("zh");
  const [requirements, setRequirements] = useState("");
  const [outline, setOutline] = useState("");
  const [paper, setPaper] = useState("");
  const [isGeneratingOutline, setIsGeneratingOutline] = useState(false);
  const [isGeneratingPaper, setIsGeneratingPaper] = useState(false);
  const [progress, setProgress] = useState(0);
  const [copied, setCopied] = useState(false);
  const { toast } = useToast();

  const saveDraft = () => {
    sessionStorage.setItem(
      "write_draft",
      JSON.stringify({
        title,
        subject,
        paperType,
        wordCount,
        educationLevel,
        requirements,
        outline,
        modelMode,
        outlineDepth,
        language,
      })
    );
  };

  useEffect(() => {
    saveDraft();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    title,
    subject,
    paperType,
    wordCount,
    educationLevel,
    requirements,
    outline,
    modelMode,
    outlineDepth,
    language,
  ]);

  useEffect(() => {
    const raw = sessionStorage.getItem("write_prefill");
    const draftRaw = sessionStorage.getItem("write_draft");
    if (!raw && !draftRaw) return;

    if (sessionStorage.getItem("write_auto_generate_paper") === "1") {
      autoGeneratePaperRef.current = true;
      sessionStorage.removeItem("write_auto_generate_paper");
    }

    try {
      const data = JSON.parse(raw || draftRaw || "{}") as Partial<{
        title: string;
        subject: string;
        paperType: string;
        wordCount: string;
        educationLevel: string;
        requirements: string;
        outline: string;
        modelMode: ModelMode;
        outlineDepth: OutlineDepth;
        language: Language;
      }>;

      if (typeof data.title === "string") setTitle(data.title);
      if (typeof data.subject === "string") setSubject(data.subject);
      if (typeof data.paperType === "string") setPaperType(data.paperType);
      if (typeof data.wordCount === "string") setWordCount(data.wordCount);
      if (typeof data.educationLevel === "string") setEducationLevel(data.educationLevel);
      if (typeof data.requirements === "string") setRequirements(data.requirements);
      if (typeof data.outline === "string") setOutline(data.outline);
      if (data.modelMode === "standard" || data.modelMode === "genius") setModelMode(data.modelMode);
      if (data.outlineDepth === "2" || data.outlineDepth === "3") setOutlineDepth(data.outlineDepth);
      if (data.language === "zh" || data.language === "en" || data.language === "ja" || data.language === "ko" || data.language === "ru" || data.language === "th") {
        setLanguage(data.language);
      }

      if (typeof data.outline === "string" && data.outline.trim()) {
        setStep(2);
      }
    } catch {
      // ignore
    } finally {
      sessionStorage.removeItem("write_prefill");
    }
  }, []);

  useEffect(() => {
    if (!autoGeneratePaperRef.current) return;
    if (step !== 2) return;
    if (!outline.trim()) return;
    if (isGeneratingPaper) return;

    autoGeneratePaperRef.current = false;
    handleGeneratePaper();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [step, outline, isGeneratingPaper]);

  const handleGenerateOutline = async () => {
    if (!title.trim()) {
      toast({
        title: "请输入论文题目",
        description: "论文题目是必填项",
        variant: "destructive",
      });
      return;
    }

    if (!subject.trim()) {
      toast({
        title: "请选择学科专业",
        description: "学科专业是必填项",
        variant: "destructive",
      });
      return;
    }

    if (!paperType.trim()) {
      toast({
        title: "请选择论文类型",
        description: "论文类型是必填项",
        variant: "destructive",
      });
      return;
    }

    if (!wordCount.trim()) {
      toast({
        title: "请选择字数要求",
        description: "字数要求是必填项",
        variant: "destructive",
      });
      return;
    }

    if (!educationLevel.trim()) {
      toast({
        title: "请选择学历层次",
        description: "学历层次是必填项",
        variant: "destructive",
      });
      return;
    }

    setIsGeneratingOutline(true);
    setOutline("");

    try {
      const response = await fetch("/api/generate-outline", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          title,
          subject,
          paperType,
          wordCount,
          educationLevel,
          requirements,
          modelMode,
          outlineDepth,
          language,
        }),
      });

      if (!response.ok) {
        const errText = await response.text().catch(() => "");
        throw new Error(errText || `生成失败(${response.status})`);
      }

      const reader = response.body?.getReader();
      const decoder = new TextDecoder();

      if (reader) {
        while (true) {
          const { done, value } = await reader.read();
          if (done) break;
          const text = decoder.decode(value);
          setOutline((prev) => prev + text);
        }
      }

      setStep(2);
      toast({
        title: "大纲生成成功",
        description: "请检查大纲内容，满意后点击生成论文",
      });
    } catch (error) {
      toast({
        title: "生成失败",
        description: error instanceof Error ? error.message : "请稍后重试",
        variant: "destructive",
      });
    } finally {
      setIsGeneratingOutline(false);
    }
  };

  const handleGeneratePaper = async () => {
    if (!outline.trim()) {
      toast({
        title: "请先生成大纲",
        description: "需要先生成大纲才能生成论文",
        variant: "destructive",
      });
      return;
    }

    setIsGeneratingPaper(true);
    setPaper("");
    setProgress(0);

    const progressInterval = setInterval(() => {
      setProgress((prev) => Math.min(prev + Math.random() * 5, 95));
    }, 500);

    try {
      const response = await fetch("/api/generate-paper", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          title,
          subject,
          paperType,
          wordCount,
          educationLevel,
          requirements,
          outline,
          modelMode,
          language,
        }),
      });

      if (!response.ok) {
        const errText = await response.text().catch(() => "");
        throw new Error(errText || `生成失败(${response.status})`);
      }

      const reader = response.body?.getReader();
      const decoder = new TextDecoder();

      if (reader) {
        while (true) {
          const { done, value } = await reader.read();
          if (done) break;
          const text = decoder.decode(value);
          setPaper((prev) => prev + text);
        }
      }

      clearInterval(progressInterval);
      setProgress(100);
      setStep(3);
      toast({
        title: "论文生成成功",
        description: "您可以下载或复制论文内容",
      });
    } catch (error) {
      toast({
        title: "生成失败",
        description: error instanceof Error ? error.message : "请稍后重试",
        variant: "destructive",
      });
    } finally {
      clearInterval(progressInterval);
      setIsGeneratingPaper(false);
    }
  };

  const handleDownload = () => {
    const blob = new Blob([paper], { type: "text/plain;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `${title || "论文"}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    toast({
      title: "下载成功",
      description: "论文已保存到本地",
    });
  };

  const handleCopy = async () => {
    await navigator.clipboard.writeText(paper);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
    toast({
      title: "复制成功",
      description: "论文内容已复制到剪贴板",
    });
  };

  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1 py-12">
        <div className="container mx-auto px-4">
          <div className="max-w-5xl mx-auto">
            <div className="text-center mb-8">
              <h1 className="text-3xl font-bold mb-4">
                <FileText className="inline-block mr-2 h-8 w-8 text-primary" />
                AI论文写作
              </h1>
              <p className="text-muted-foreground">
                3分钟生成万字论文，知网查重率10%左右
              </p>
            </div>

            {/* Progress Steps */}
            <div className="flex items-center justify-center mb-8">
              {[1, 2, 3].map((s) => (
                <div key={s} className="flex items-center">
                  <div
                    className={`w-10 h-10 rounded-full flex items-center justify-center font-semibold ${
                      step >= s
                        ? "bg-primary text-primary-foreground"
                        : "bg-muted text-muted-foreground"
                    }`}
                  >
                    {s}
                  </div>
                  <span className={`ml-2 ${step >= s ? "text-foreground" : "text-muted-foreground"}`}>
                    {s === 1 ? "填写信息" : s === 2 ? "确认大纲" : "生成论文"}
                  </span>
                  {s < 3 && (
                    <div className={`w-16 h-0.5 mx-4 ${step > s ? "bg-primary" : "bg-muted"}`} />
                  )}
                </div>
              ))}
            </div>

            <Tabs value={`step-${step}`} className="w-full">
              {/* Step 1: Input Form */}
              <TabsContent value="step-1">
                <Card>
                  <CardHeader>
                    <CardTitle>填写论文信息</CardTitle>
                    <CardDescription>请填写论文的基本信息</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="md:col-span-2">
                        <label className="text-sm font-medium mb-2 block">
                          论文题目 <span className="text-destructive">*</span>
                        </label>
                        <Input
                          placeholder="请输入论文题目"
                          value={title}
                          onChange={(e) => setTitle(e.target.value)}
                        />
                      </div>

                      <div>
                        <label className="text-sm font-medium mb-2 block">学科专业</label>
                        <Select value={subject} onValueChange={setSubject}>
                          <SelectTrigger>
                            <SelectValue placeholder="选择学科专业" />
                          </SelectTrigger>
                          <SelectContent>
                            {subjectCategories.map((category) => (
                              <div key={category.name}>
                                <div className="px-2 py-1.5 text-sm font-semibold text-muted-foreground">
                                  {category.name}
                                </div>
                                {category.subjects.map((sub) => (
                                  <SelectItem key={sub} value={sub}>
                                    {sub}
                                  </SelectItem>
                                ))}
                              </div>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>

                      <div>
                        <label className="text-sm font-medium mb-2 block">论文类型</label>
                        <Select value={paperType} onValueChange={setPaperType}>
                          <SelectTrigger>
                            <SelectValue placeholder="选择论文类型" />
                          </SelectTrigger>
                          <SelectContent>
                            {paperTypes.map((type) => (
                              <SelectItem key={type.value} value={type.value}>
                                {type.label}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>

                      <div>
                        <label className="text-sm font-medium mb-2 block">字数要求</label>
                        <Select value={wordCount} onValueChange={setWordCount}>
                          <SelectTrigger>
                            <SelectValue placeholder="选择字数要求" />
                          </SelectTrigger>
                          <SelectContent>
                            {wordCounts.map((wc) => (
                              <SelectItem key={wc.value} value={wc.value}>
                                {wc.label}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>

                      <div>
                        <label className="text-sm font-medium mb-2 block">学历层次</label>
                        <Select value={educationLevel} onValueChange={setEducationLevel}>
                          <SelectTrigger>
                            <SelectValue placeholder="选择学历层次" />
                          </SelectTrigger>
                          <SelectContent>
                            {educationLevels.map((level) => (
                              <SelectItem key={level.value} value={level.value}>
                                {level.label}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>

                      <div>
                        <label className="text-sm font-medium mb-2 block">模型</label>
                        <Select value={modelMode} onValueChange={(v) => setModelMode(v as ModelMode)}>
                          <SelectTrigger>
                            <SelectValue placeholder="选择模型" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="standard">标准模型</SelectItem>
                            <SelectItem value="genius">5.0(Genius Writer)(DeepSeek R1 学术加强版)</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>

                      <div>
                        <label className="text-sm font-medium mb-2 block">大纲层级</label>
                        <Select value={outlineDepth} onValueChange={(v) => setOutlineDepth(v as OutlineDepth)}>
                          <SelectTrigger>
                            <SelectValue placeholder="选择大纲层级" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="2">二级</SelectItem>
                            <SelectItem value="3">三级</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>

                      <div>
                        <label className="text-sm font-medium mb-2 block">语言</label>
                        <Select value={language} onValueChange={(v) => setLanguage(v as Language)}>
                          <SelectTrigger>
                            <SelectValue placeholder="选择语言" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="zh">中文</SelectItem>
                            <SelectItem value="en">英语</SelectItem>
                            <SelectItem value="ja">日语</SelectItem>
                            <SelectItem value="ko">韩语</SelectItem>
                            <SelectItem value="ru">俄语</SelectItem>
                            <SelectItem value="th">泰语</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>

                      <div className="md:col-span-2">
                        <label className="text-sm font-medium mb-2 block">其他要求</label>
                        <Textarea
                          placeholder="请输入其他特殊要求（可选）"
                          value={requirements}
                          onChange={(e) => setRequirements(e.target.value)}
                          rows={3}
                        />
                      </div>
                    </div>

                    <Button
                      className="w-full mt-6"
                      size="lg"
                      onClick={handleGenerateOutline}
                      disabled={isGeneratingOutline}
                    >
                      {isGeneratingOutline ? (
                        <>
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                          正在生成大纲...
                        </>
                      ) : (
                        <>
                          生成大纲
                          <ArrowRight className="ml-2 h-4 w-4" />
                        </>
                      )}
                    </Button>

                    {outline.trim() && !isGeneratingOutline && (
                      <Button
                        className="w-full mt-3"
                        size="lg"
                        variant="outline"
                        onClick={() => setStep(2)}
                      >
                        返回大纲确认
                      </Button>
                    )}
                  </CardContent>
                </Card>
              </TabsContent>

              {/* Step 2: Outline Review */}
              <TabsContent value="step-2">
                <Card>
                  <CardHeader>
                    <CardTitle>确认论文大纲</CardTitle>
                    <CardDescription>
                      请检查大纲内容，可以直接编辑修改
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <Textarea
                      value={outline}
                      onChange={(e) => setOutline(e.target.value)}
                      className="min-h-[400px] font-mono text-sm mb-6"
                    />
                    <div className="flex gap-4">
                      {!isGeneratingPaper && (
                        <Button
                          variant="outline"
                          onClick={() => {
                            saveDraft();
                            setStep(1);
                          }}
                        >
                          返回修改
                        </Button>
                      )}
                      <Button
                        className="flex-1"
                        size="lg"
                        onClick={handleGeneratePaper}
                        disabled={isGeneratingPaper}
                      >
                        {isGeneratingPaper ? (
                          <>
                            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                            正在生成论文...
                          </>
                        ) : (
                          <>
                            生成论文
                            <ArrowRight className="ml-2 h-4 w-4" />
                          </>
                        )}
                      </Button>
                    </div>
                    {isGeneratingPaper && (
                      <div className="mt-6">
                        <Progress value={progress} className="h-2" />
                        <p className="text-sm text-muted-foreground text-center mt-2">
                          正在生成论文... {Math.round(progress)}%
                        </p>
                      </div>
                    )}
                  </CardContent>
                </Card>
              </TabsContent>

              {/* Step 3: Paper Result */}
              <TabsContent value="step-3">
                <Card>
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <div>
                        <CardTitle>论文生成完成</CardTitle>
                        <CardDescription>
                          字数：约 {paper.length} 字
                        </CardDescription>
                      </div>
                      <div className="flex gap-2">
                        <Button variant="outline" onClick={handleCopy}>
                          {copied ? (
                            <Check className="h-4 w-4 mr-2" />
                          ) : (
                            <Copy className="h-4 w-4 mr-2" />
                          )}
                          复制
                        </Button>
                        <Button onClick={handleDownload}>
                          <Download className="h-4 w-4 mr-2" />
                          下载
                        </Button>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <Textarea
                      value={paper}
                      onChange={(e) => setPaper(e.target.value)}
                      className="min-h-[500px] font-mono text-sm"
                    />
                    <div className="mt-6 p-4 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg">
                      <p className="text-sm text-yellow-800 dark:text-yellow-200">
                        <strong>提示：</strong>
                        本内容仅供学习参考，请务必进行人工审核和修改后再使用。
                        直接提交AI生成内容可能违反学术规范。
                      </p>
                    </div>
                  </CardContent>
                </Card>
              </TabsContent>
            </Tabs>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
}
