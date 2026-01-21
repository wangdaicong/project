import { FileText, Users, Shield, Zap } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";

const stats = [
  { label: "注册用户", value: "100,000+" },
  { label: "生成论文", value: "500,000+" },
  { label: "覆盖学科", value: "720+" },
  { label: "用户满意度", value: "98%" },
];

const values = [
  {
    icon: Zap,
    title: "高效便捷",
    description: "利用先进的AI技术，大幅提升论文写作效率，让学术创作更加轻松。",
  },
  {
    icon: Shield,
    title: "安全可靠",
    description: "采用双重认证机制，保护用户隐私和论文安全，确保一人一稿。",
  },
  {
    icon: Users,
    title: "专业服务",
    description: "覆盖720+学科专业，提供专业的学术写作支持和客户服务。",
  },
  {
    icon: FileText,
    title: "质量保障",
    description: "智能优化引擎确保论文质量，查重率保障让您写作无忧。",
  },
];

export default function AboutPage() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1">
        {/* Hero Section */}
        <section className="py-20 bg-gradient-to-b from-primary/5 to-background">
          <div className="container mx-auto px-4 text-center">
            <h1 className="text-4xl md:text-5xl font-bold mb-6">关于66论文写作</h1>
            <p className="text-xl text-muted-foreground max-w-3xl mx-auto">
              我们致力于利用人工智能技术，为学生、研究人员和专业人士提供高效、专业的论文写作辅助服务，
              让学术创作变得更加简单高效。
            </p>
          </div>
        </section>

        {/* Stats Section */}
        <section className="py-16">
          <div className="container mx-auto px-4">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
              {stats.map((stat, index) => (
                <div key={index} className="text-center">
                  <div className="text-4xl font-bold text-primary mb-2">
                    {stat.value}
                  </div>
                  <div className="text-muted-foreground">{stat.label}</div>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Mission Section */}
        <section className="py-16 bg-muted/30">
          <div className="container mx-auto px-4">
            <div className="max-w-3xl mx-auto text-center">
              <h2 className="text-3xl font-bold mb-6">我们的使命</h2>
              <p className="text-lg text-muted-foreground mb-8">
                在人工智能快速发展的时代，我们相信技术应该服务于教育和学术研究。
                66论文写作平台的使命是成为学术创作的得力助手，帮助用户提高写作效率，
                激发创作灵感，同时始终倡导学术诚信和原创精神。
              </p>
              <p className="text-lg text-muted-foreground">
                我们不鼓励直接提交AI生成的内容，而是希望用户将我们的服务作为学习和参考工具，
                在AI辅助的基础上进行深入思考和创作，最终产出真正属于自己的学术成果。
              </p>
            </div>
          </div>
        </section>

        {/* Values Section */}
        <section className="py-16">
          <div className="container mx-auto px-4">
            <h2 className="text-3xl font-bold text-center mb-12">我们的价值观</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {values.map((value, index) => (
                <Card key={index} className="text-center">
                  <CardHeader>
                    <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center mx-auto mb-4">
                      <value.icon className="h-6 w-6 text-primary" />
                    </div>
                    <CardTitle>{value.title}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground">{value.description}</p>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        </section>

        {/* Contact Section */}
        <section className="py-16 bg-muted/30">
          <div className="container mx-auto px-4 text-center">
            <h2 className="text-3xl font-bold mb-6">联系我们</h2>
            <p className="text-muted-foreground mb-8 max-w-2xl mx-auto">
              如果您有任何问题、建议或合作意向，欢迎随时与我们联系。
            </p>
            <div className="flex flex-col md:flex-row gap-8 justify-center items-center">
              <div>
                <div className="font-semibold">客服邮箱</div>
                <div className="text-muted-foreground">support@thesis66.com</div>
              </div>
              <div>
                <div className="font-semibold">商务合作</div>
                <div className="text-muted-foreground">business@thesis66.com</div>
              </div>
              <div>
                <div className="font-semibold">工作时间</div>
                <div className="text-muted-foreground">周一至周五 9:00-18:00</div>
              </div>
            </div>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  );
}
