"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import {
  FileText,
  Loader2,
  RefreshCw,
  Copy,
  Check,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import { useToast } from "@/components/ui/use-toast";
import { subjectCategories, educationLevels, paperTypes, wordCounts } from "@/lib/subjects";

const modelOptions = [
  { value: "standard", label: "标准模型" },
  { value: "genius", label: "5.0(Genius Writer)(DeepSeek R1 学术加强版)" },
];

const outlineDepthOptions = [
  { value: "2", label: "二级大纲" },
  { value: "3", label: "三级大纲" },
];

const languageOptions = [
  { value: "zh", label: "中文" },
  { value: "en", label: "英语" },
  { value: "ja", label: "日语" },
  { value: "ko", label: "韩语" },
  { value: "ru", label: "俄语" },
  { value: "th", label: "泰语" },
];

export default function OutlinePage() {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [subject, setSubject] = useState("");
  const [paperType, setPaperType] = useState("");
  const [educationLevel, setEducationLevel] = useState("");
  const [wordCount, setWordCount] = useState("");
  const [modelMode, setModelMode] = useState<"standard" | "genius">("genius");
  const [outlineDepth, setOutlineDepth] = useState<"2" | "3">("2");
  const [language, setLanguage] = useState<"zh" | "en" | "ja" | "ko" | "ru" | "th">("zh");
  const [requirements, setRequirements] = useState("");
  const [outline, setOutline] = useState("");
  const [isManualOutline, setIsManualOutline] = useState(false);
  const [isGenerating, setIsGenerating] = useState(false);
  const [copied, setCopied] = useState(false);
  const { toast } = useToast();

  const handleGoWrite = () => {
    if (!title.trim()) {
      toast({
        title: "请填写论文题目",
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

    if (!outline.trim()) {
      toast({
        title: "请先生成或输入大纲",
        description: "需要有大纲内容才能生成论文",
        variant: "destructive",
      });
      return;
    }

    const ok = window.confirm("是否确认当前大纲并开始生成论文？");
    if (!ok) return;

    sessionStorage.setItem(
      "write_prefill",
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
    sessionStorage.setItem("write_auto_generate_paper", "1");
    router.push("/write");
  };

  const handleGenerate = async () => {
    if (!title.trim()) {
      toast({
        title: "请输入论文题目",
        description: "论文题目是必填项",
        variant: "destructive",
      });
      return;
    }

    setIsGenerating(true);
    setOutline("");

    try {
      const response = await fetch("/api/generate-outline", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          title,
          subject,
          paperType,
          educationLevel,
          wordCount,
          requirements,
          modelMode,
          outlineDepth,
          language,
        }),
      });

      if (!response.ok) {
        const errText = await response.text().catch(() => "生成失败");
        throw new Error(errText || "生成失败");
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

      toast({
        title: "大纲生成成功",
        description: "您可以根据需要修改大纲内容",
      });
    } catch (error) {
      toast({
        title: "生成失败",
        description: error instanceof Error ? error.message : "请稍后重试或检查网络连接",
        variant: "destructive",
      });
    } finally {
      setIsGenerating(false);
    }
  };

  const handleCopy = async () => {
    await navigator.clipboard.writeText(outline);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
    toast({
      title: "复制成功",
      description: "大纲内容已复制到剪贴板",
    });
  };

  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1 py-12">
        <div className="container mx-auto px-4">
          <div className="max-w-6xl mx-auto">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">
              <Card className="bg-gradient-to-br from-sky-50/80 to-indigo-50/70 border-sky-100">
                <CardContent className="pt-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="md:col-span-2">
                      <div className="text-sm font-medium mb-2">论文题目</div>
                      <Input
                        className="bg-white"
                        placeholder="请输入论文题目"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                      />
                    </div>

                    <div>
                      <div className="text-sm font-medium mb-2">学科专业</div>
                      <Select value={subject} onValueChange={setSubject}>
                        <SelectTrigger className="bg-white">
                          <SelectValue placeholder="请选择学科专业" />
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
                      <div className="text-sm font-medium mb-2">论文类型</div>
                      <Select value={paperType} onValueChange={setPaperType}>
                        <SelectTrigger className="bg-white">
                          <SelectValue placeholder="请选择论文类型" />
                        </SelectTrigger>
                        <SelectContent>
                          {paperTypes.map((x) => (
                            <SelectItem key={x.value} value={x.value}>
                              {x.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>

                    <div>
                      <div className="text-sm font-medium mb-2">字数要求</div>
                      <Select value={wordCount} onValueChange={setWordCount}>
                        <SelectTrigger className="bg-white">
                          <SelectValue placeholder="请选择字数" />
                        </SelectTrigger>
                        <SelectContent>
                          {wordCounts.map((x) => (
                            <SelectItem key={x.value} value={x.value}>
                              {x.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>

                    <div>
                      <div className="text-sm font-medium mb-2">学历层次</div>
                      <Select value={educationLevel} onValueChange={setEducationLevel}>
                        <SelectTrigger className="bg-white">
                          <SelectValue placeholder="请选择学历层次" />
                        </SelectTrigger>
                        <SelectContent>
                          {educationLevels.map((x) => (
                            <SelectItem key={x.value} value={x.value}>
                              {x.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>

                    <div>
                      <div className="text-sm font-medium mb-2">模型</div>
                      <Select value={modelMode} onValueChange={(v) => setModelMode(v as "standard" | "genius")}>
                        <SelectTrigger className="bg-white">
                          <SelectValue placeholder="请选择模型" />
                        </SelectTrigger>
                        <SelectContent>
                          {modelOptions.map((x) => (
                            <SelectItem key={x.value} value={x.value}>
                              {x.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>

                    <div>
                      <div className="text-sm font-medium mb-2">大纲层级</div>
                      <Select value={outlineDepth} onValueChange={(v) => setOutlineDepth(v as "2" | "3")}>
                        <SelectTrigger className="bg-white">
                          <SelectValue placeholder="请选择大纲层级" />
                        </SelectTrigger>
                        <SelectContent>
                          {outlineDepthOptions.map((x) => (
                            <SelectItem key={x.value} value={x.value}>
                              {x.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>

                    <div>
                      <div className="text-sm font-medium mb-2">输出语言</div>
                      <Select value={language} onValueChange={(v) => setLanguage(v as "zh" | "en" | "ja" | "ko" | "ru" | "th")}>
                        <SelectTrigger className="bg-white">
                          <SelectValue placeholder="请选择语言" />
                        </SelectTrigger>
                        <SelectContent>
                          {languageOptions.map((x) => (
                            <SelectItem key={x.value} value={x.value}>
                              {x.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>

                    <div className="md:col-span-2">
                      <div className="text-sm font-medium mb-2">其他要求（可选）</div>
                      <Textarea
                        placeholder="例如：研究方法、案例地区、格式要求、参考文献数量等"
                        value={requirements}
                        onChange={(e) => setRequirements(e.target.value)}
                        rows={3}
                        className="bg-white"
                      />
                    </div>

                    <div className="md:col-span-2">
                      <Button size="lg" onClick={handleGenerate} disabled={isGenerating} className="w-full">
                        {isGenerating ? (
                          <>
                            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                            正在生成...
                          </>
                        ) : (
                          <>
                            <FileText className="mr-2 h-4 w-4" />
                            生成论文大纲
                          </>
                        )}
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card className="lg:sticky lg:top-24">
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <div>
                      <CardTitle>论文大纲</CardTitle>
                      <CardDescription>生成的大纲内容（可编辑）</CardDescription>
                    </div>
                    <div className="flex items-center gap-3">
                      {outline.trim() && !isGenerating && (
                        <Button size="sm" onClick={handleGoWrite}>
                          生成论文
                        </Button>
                      )}
                      <Button
                        type="button"
                        size="sm"
                        variant="outline"
                        onClick={() => setIsManualOutline((v) => !v)}
                      >
                        {isManualOutline ? "关闭手动输入" : "手动输入大纲"}
                      </Button>

                      {outline && (
                        <div className="flex gap-2">
                          <Button variant="outline" size="sm" onClick={handleCopy}>
                            {copied ? <Check className="h-4 w-4" /> : <Copy className="h-4 w-4" />}
                          </Button>
                          <Button variant="outline" size="sm" onClick={handleGenerate} disabled={isGenerating}>
                            <RefreshCw className="h-4 w-4" />
                          </Button>
                        </div>
                      )}
                    </div>
                  </div>
                </CardHeader>
                <CardContent>
                  {outline || isManualOutline ? (
                    <Textarea
                      value={outline}
                      onChange={(e) => setOutline(e.target.value)}
                      placeholder="请在此手动输入/粘贴论文大纲..."
                      className="min-h-[520px] font-mono text-sm"
                    />
                  ) : (
                    <div className="min-h-[520px] flex items-center justify-center text-muted-foreground">
                      {isGenerating ? (
                        <div className="text-center">
                          <Loader2 className="h-8 w-8 animate-spin mx-auto mb-4" />
                          <p>正在生成大纲...</p>
                        </div>
                      ) : (
                        <p>填写左侧信息后点击“生成论文大纲”</p>
                      )}
                    </div>
                  )}
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
