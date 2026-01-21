import { Code, Key, Zap, Shield, FileText, Copy } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import Link from "next/link";

const endpoints = [
  {
    method: "POST",
    path: "/api/generate-outline",
    description: "生成论文大纲",
    params: [
      { name: "title", type: "string", required: true, description: "论文题目" },
      { name: "subject", type: "string", required: false, description: "学科专业" },
      { name: "paperType", type: "string", required: false, description: "论文类型" },
      { name: "wordCount", type: "string", required: false, description: "字数要求" },
      { name: "educationLevel", type: "string", required: false, description: "学历层次" },
      { name: "requirements", type: "string", required: false, description: "其他要求" },
    ],
  },
  {
    method: "POST",
    path: "/api/generate-paper",
    description: "生成完整论文",
    params: [
      { name: "title", type: "string", required: true, description: "论文题目" },
      { name: "outline", type: "string", required: true, description: "论文大纲" },
      { name: "subject", type: "string", required: false, description: "学科专业" },
      { name: "paperType", type: "string", required: false, description: "论文类型" },
      { name: "wordCount", type: "string", required: false, description: "字数要求" },
      { name: "educationLevel", type: "string", required: false, description: "学历层次" },
      { name: "requirements", type: "string", required: false, description: "其他要求" },
    ],
  },
];

const codeExamples = {
  curl: `curl -X POST https://api.thesis66.com/v1/generate-outline \\
  -H "Authorization: Bearer YOUR_API_KEY" \\
  -H "Content-Type: application/json" \\
  -d '{
    "title": "人工智能在教育领域的应用研究",
    "subject": "计算机科学",
    "paperType": "graduation",
    "educationLevel": "bachelor"
  }'`,
  javascript: `const response = await fetch('https://api.thesis66.com/v1/generate-outline', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer YOUR_API_KEY',
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    title: '人工智能在教育领域的应用研究',
    subject: '计算机科学',
    paperType: 'graduation',
    educationLevel: 'bachelor',
  }),
});

const reader = response.body.getReader();
const decoder = new TextDecoder();

while (true) {
  const { done, value } = await reader.read();
  if (done) break;
  console.log(decoder.decode(value));
}`,
  python: `import requests

response = requests.post(
    'https://api.thesis66.com/v1/generate-outline',
    headers={
        'Authorization': 'Bearer YOUR_API_KEY',
        'Content-Type': 'application/json',
    },
    json={
        'title': '人工智能在教育领域的应用研究',
        'subject': '计算机科学',
        'paperType': 'graduation',
        'educationLevel': 'bachelor',
    },
    stream=True
)

for chunk in response.iter_content(chunk_size=None):
    print(chunk.decode('utf-8'), end='')`,
};

export default function ApiDocsPage() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1 py-12">
        <div className="container mx-auto px-4">
          {/* Header */}
          <div className="text-center mb-12">
            <h1 className="text-4xl font-bold mb-4">API 文档</h1>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              将AI论文写作能力集成到您的应用中
            </p>
          </div>

          {/* Features */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
            <Card>
              <CardContent className="pt-6">
                <Zap className="h-10 w-10 text-primary mb-4" />
                <h3 className="font-semibold mb-2">高性能</h3>
                <p className="text-sm text-muted-foreground">
                  流式响应，实时获取生成内容，无需等待完整结果
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="pt-6">
                <Shield className="h-10 w-10 text-primary mb-4" />
                <h3 className="font-semibold mb-2">安全可靠</h3>
                <p className="text-sm text-muted-foreground">
                  API密钥认证，HTTPS加密传输，保护数据安全
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="pt-6">
                <Code className="h-10 w-10 text-primary mb-4" />
                <h3 className="font-semibold mb-2">易于集成</h3>
                <p className="text-sm text-muted-foreground">
                  RESTful API设计，支持多种编程语言
                </p>
              </CardContent>
            </Card>
          </div>

          {/* Authentication */}
          <Card className="mb-8">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Key className="h-5 w-5" />
                认证方式
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-muted-foreground mb-4">
                所有API请求都需要在请求头中包含API密钥进行认证：
              </p>
              <div className="bg-muted p-4 rounded-lg font-mono text-sm">
                Authorization: Bearer YOUR_API_KEY
              </div>
              <p className="text-sm text-muted-foreground mt-4">
                您可以在<Link href="/profile" className="text-primary hover:underline">个人中心</Link>获取API密钥。
                企业版用户可获得更高的调用配额。
              </p>
            </CardContent>
          </Card>

          {/* Endpoints */}
          <Card className="mb-8">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <FileText className="h-5 w-5" />
                API 端点
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              {endpoints.map((endpoint, index) => (
                <div key={index} className="border rounded-lg p-4">
                  <div className="flex items-center gap-3 mb-3">
                    <span className="bg-green-100 text-green-700 text-xs font-semibold px-2 py-1 rounded">
                      {endpoint.method}
                    </span>
                    <code className="text-sm font-mono">{endpoint.path}</code>
                  </div>
                  <p className="text-muted-foreground mb-4">{endpoint.description}</p>
                  <div className="overflow-x-auto">
                    <table className="w-full text-sm">
                      <thead>
                        <tr className="border-b">
                          <th className="text-left py-2 pr-4">参数</th>
                          <th className="text-left py-2 pr-4">类型</th>
                          <th className="text-left py-2 pr-4">必填</th>
                          <th className="text-left py-2">说明</th>
                        </tr>
                      </thead>
                      <tbody>
                        {endpoint.params.map((param, i) => (
                          <tr key={i} className="border-b last:border-0">
                            <td className="py-2 pr-4 font-mono text-primary">
                              {param.name}
                            </td>
                            <td className="py-2 pr-4 text-muted-foreground">
                              {param.type}
                            </td>
                            <td className="py-2 pr-4">
                              {param.required ? (
                                <span className="text-red-500">是</span>
                              ) : (
                                <span className="text-muted-foreground">否</span>
                              )}
                            </td>
                            <td className="py-2 text-muted-foreground">
                              {param.description}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              ))}
            </CardContent>
          </Card>

          {/* Code Examples */}
          <Card className="mb-8">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Code className="h-5 w-5" />
                代码示例
              </CardTitle>
            </CardHeader>
            <CardContent>
              <Tabs defaultValue="curl">
                <TabsList>
                  <TabsTrigger value="curl">cURL</TabsTrigger>
                  <TabsTrigger value="javascript">JavaScript</TabsTrigger>
                  <TabsTrigger value="python">Python</TabsTrigger>
                </TabsList>
                {Object.entries(codeExamples).map(([lang, code]) => (
                  <TabsContent key={lang} value={lang}>
                    <div className="relative">
                      <pre className="bg-muted p-4 rounded-lg overflow-x-auto text-sm">
                        <code>{code}</code>
                      </pre>
                    </div>
                  </TabsContent>
                ))}
              </Tabs>
            </CardContent>
          </Card>

          {/* Rate Limits */}
          <Card className="mb-8">
            <CardHeader>
              <CardTitle>调用限制</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b">
                      <th className="text-left py-2 pr-4">方案</th>
                      <th className="text-left py-2 pr-4">大纲生成</th>
                      <th className="text-left py-2 pr-4">论文生成</th>
                      <th className="text-left py-2">并发数</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr className="border-b">
                      <td className="py-2 pr-4 font-medium">免费版</td>
                      <td className="py-2 pr-4 text-muted-foreground">3次/天</td>
                      <td className="py-2 pr-4 text-muted-foreground">1次/天</td>
                      <td className="py-2 text-muted-foreground">1</td>
                    </tr>
                    <tr className="border-b">
                      <td className="py-2 pr-4 font-medium">专业版</td>
                      <td className="py-2 pr-4 text-muted-foreground">无限制</td>
                      <td className="py-2 pr-4 text-muted-foreground">20次/天</td>
                      <td className="py-2 text-muted-foreground">3</td>
                    </tr>
                    <tr>
                      <td className="py-2 pr-4 font-medium">企业版</td>
                      <td className="py-2 pr-4 text-muted-foreground">无限制</td>
                      <td className="py-2 pr-4 text-muted-foreground">无限制</td>
                      <td className="py-2 text-muted-foreground">10</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </CardContent>
          </Card>

          {/* CTA */}
          <div className="text-center">
            <Card className="max-w-2xl mx-auto bg-primary/5 border-primary/20">
              <CardContent className="pt-6">
                <h3 className="text-xl font-semibold mb-2">准备开始？</h3>
                <p className="text-muted-foreground mb-4">
                  升级到企业版获取API访问权限和更高的调用配额
                </p>
                <div className="flex gap-4 justify-center">
                  <Button asChild>
                    <Link href="/pricing">查看价格</Link>
                  </Button>
                  <Button variant="outline" asChild>
                    <Link href="/contact">联系销售</Link>
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
}
