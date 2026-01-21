import { AlertTriangle } from "lucide-react";
import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import { Card, CardContent } from "@/components/ui/card";

export default function DisclaimerPage() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1 py-12">
        <div className="container mx-auto px-4 max-w-4xl">
          <h1 className="text-4xl font-bold mb-8">免责声明</h1>
          
          <Card className="mb-8 border-yellow-500/50 bg-yellow-500/5">
            <CardContent className="pt-6">
              <div className="flex items-start gap-4">
                <AlertTriangle className="h-6 w-6 text-yellow-500 flex-shrink-0 mt-1" />
                <div>
                  <h2 className="font-semibold text-lg mb-2">重要提示</h2>
                  <p className="text-muted-foreground">
                    使用本平台前，请务必仔细阅读以下免责声明。继续使用本平台即表示您已阅读、
                    理解并同意以下所有条款。
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          <div className="prose prose-gray max-w-none space-y-6">
            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">1. 服务性质</h2>
              <p className="text-muted-foreground leading-relaxed">
                66论文写作平台（以下简称"本平台"）是一个基于人工智能技术的写作辅助工具。
                本平台提供的所有内容生成服务仅供学习参考和灵感启发之用，不构成任何形式的
                学术成果或专业建议。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">2. 学术诚信</h2>
              <p className="text-muted-foreground leading-relaxed mb-4">
                <strong>本平台明确声明：</strong>
              </p>
              <ul className="list-disc list-inside space-y-2 text-muted-foreground">
                <li>本平台生成的内容不应被直接作为原创学术成果提交</li>
                <li>直接提交AI生成的论文可能违反学校、机构或出版物的学术诚信政策</li>
                <li>用户应当对生成的内容进行充分的审核、修改和完善</li>
                <li>用户需自行承担因违反学术规范而产生的一切后果</li>
              </ul>
              <p className="text-muted-foreground leading-relaxed mt-4">
                根据教育部相关规定，AI代写属于学术不端行为，可能面临严重处分，包括但不限于
                论文作废、学位撤销、学籍处分等。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">3. 内容准确性</h2>
              <p className="text-muted-foreground leading-relaxed">
                本平台基于人工智能技术生成内容，可能存在以下问题：
              </p>
              <ul className="list-disc list-inside space-y-2 text-muted-foreground mt-4">
                <li>事实性错误或数据不准确</li>
                <li>引用来源可能不存在或不准确</li>
                <li>逻辑推理可能存在漏洞</li>
                <li>专业术语使用可能不当</li>
                <li>内容可能与现有文献存在相似之处</li>
              </ul>
              <p className="text-muted-foreground leading-relaxed mt-4">
                用户必须对所有生成的内容进行独立验证，本平台不对内容的准确性、完整性、
                时效性或适用性作任何保证。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">4. 查重率声明</h2>
              <p className="text-muted-foreground leading-relaxed">
                本平台声称的查重率（约10%左右）仅为参考估计值，实际查重结果可能因以下因素
                而有所不同：
              </p>
              <ul className="list-disc list-inside space-y-2 text-muted-foreground mt-4">
                <li>不同查重系统（知网、维普、万方等）的算法差异</li>
                <li>数据库更新时间差异</li>
                <li>论文主题和领域的特殊性</li>
                <li>用户对内容的修改程度</li>
              </ul>
              <p className="text-muted-foreground leading-relaxed mt-4">
                本平台不保证生成的内容能够通过任何特定的查重检测。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">5. 知识产权</h2>
              <p className="text-muted-foreground leading-relaxed">
                AI生成内容的著作权归属在法律上存在不确定性。用户应当了解：
              </p>
              <ul className="list-disc list-inside space-y-2 text-muted-foreground mt-4">
                <li>AI生成的内容可能不受著作权法保护</li>
                <li>生成的内容可能与他人作品存在相似之处</li>
                <li>用户对生成内容的使用可能涉及知识产权风险</li>
              </ul>
              <p className="text-muted-foreground leading-relaxed mt-4">
                本平台不对因使用生成内容而产生的任何知识产权纠纷承担责任。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">6. 责任限制</h2>
              <p className="text-muted-foreground leading-relaxed mb-4">
                在法律允许的最大范围内，本平台不对以下情况承担任何责任：
              </p>
              <ul className="list-disc list-inside space-y-2 text-muted-foreground">
                <li>用户因使用本平台服务而遭受的任何直接或间接损失</li>
                <li>用户因违反学术诚信规定而受到的任何处分或处罚</li>
                <li>生成内容的任何错误、遗漏或不准确</li>
                <li>因服务中断、延迟或故障造成的任何损失</li>
                <li>第三方对用户提起的任何索赔或诉讼</li>
              </ul>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">7. 用户责任</h2>
              <p className="text-muted-foreground leading-relaxed">
                用户在使用本平台时，应当：
              </p>
              <ul className="list-disc list-inside space-y-2 text-muted-foreground mt-4">
                <li>遵守所有适用的法律法规</li>
                <li>遵守所在学校或机构的学术诚信政策</li>
                <li>对生成的内容进行独立审核和验证</li>
                <li>对最终提交的作品承担全部责任</li>
                <li>不将本平台用于任何非法或不道德的目的</li>
              </ul>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">8. 正确使用建议</h2>
              <p className="text-muted-foreground leading-relaxed mb-4">
                我们建议用户以以下方式使用本平台：
              </p>
              <ul className="list-disc list-inside space-y-2 text-muted-foreground">
                <li><strong>作为灵感工具</strong>：用AI生成思路和框架，但核心内容自己撰写</li>
                <li><strong>用于文献梳理</strong>：让AI帮助整理文献综述，但重要引用要亲自核对</li>
                <li><strong>辅助语言表达</strong>：借鉴AI的表述方式，但保持自己的学术风格</li>
                <li><strong>始终人工审核</strong>：对AI生成的内容进行严格审查和修改</li>
              </ul>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">9. 声明更新</h2>
              <p className="text-muted-foreground leading-relaxed">
                本平台保留随时修改本免责声明的权利。修改后的声明将在本页面发布，
                用户继续使用本平台即视为接受修改后的声明。建议用户定期查看本页面
                以了解最新信息。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">10. 联系方式</h2>
              <p className="text-muted-foreground leading-relaxed">
                如果您对本免责声明有任何疑问，请联系我们：
              </p>
              <p className="text-muted-foreground mt-2">
                客服邮箱：support@thesis66.com
              </p>
            </section>

            <div className="mt-8 p-4 bg-muted/50 rounded-lg">
              <p className="text-sm text-muted-foreground text-center">
                最后更新日期：2025年1月1日
              </p>
            </div>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
}
