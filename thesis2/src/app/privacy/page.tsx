import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";

export default function PrivacyPage() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1 py-12">
        <div className="container mx-auto px-4 max-w-4xl">
          <h1 className="text-4xl font-bold mb-8">隐私政策</h1>
          
          <div className="prose prose-gray max-w-none space-y-6">
            <p className="text-muted-foreground">
              最后更新日期：2025年1月1日
            </p>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">1. 信息收集</h2>
              <p className="text-muted-foreground leading-relaxed mb-4">
                我们可能收集以下类型的信息：
              </p>
              <ul className="list-disc list-inside space-y-2 text-muted-foreground">
                <li><strong>账户信息</strong>：注册时提供的邮箱地址、密码等</li>
                <li><strong>使用信息</strong>：您输入的论文题目、学科专业、生成的内容等</li>
                <li><strong>设备信息</strong>：浏览器类型、操作系统、IP地址等</li>
                <li><strong>支付信息</strong>：订阅付费服务时的支付相关信息</li>
              </ul>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">2. 信息使用</h2>
              <p className="text-muted-foreground leading-relaxed mb-4">
                我们使用收集的信息用于以下目的：
              </p>
              <ul className="list-disc list-inside space-y-2 text-muted-foreground">
                <li>提供、维护和改进我们的服务</li>
                <li>处理您的请求和交易</li>
                <li>发送服务相关的通知和更新</li>
                <li>防止欺诈和滥用行为</li>
                <li>进行数据分析以改进用户体验</li>
              </ul>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">3. 信息存储</h2>
              <p className="text-muted-foreground leading-relaxed">
                我们采用行业标准的安全措施保护您的个人信息。您的账户信息将被加密存储。
                为保护您的隐私，我们不会长期保存您生成的论文内容，建议您及时下载保存。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">4. 信息共享</h2>
              <p className="text-muted-foreground leading-relaxed mb-4">
                我们不会出售您的个人信息。我们可能在以下情况下共享您的信息：
              </p>
              <ul className="list-disc list-inside space-y-2 text-muted-foreground">
                <li>经您明确同意</li>
                <li>为遵守法律法规或响应法律程序</li>
                <li>与服务提供商合作以提供服务（如支付处理）</li>
                <li>在公司合并、收购或资产出售的情况下</li>
              </ul>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">5. Cookie使用</h2>
              <p className="text-muted-foreground leading-relaxed">
                我们使用Cookie和类似技术来改善用户体验、分析网站流量和个性化内容。
                您可以通过浏览器设置管理Cookie偏好，但禁用Cookie可能影响部分功能的使用。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">6. 数据安全</h2>
              <p className="text-muted-foreground leading-relaxed">
                我们采用双重认证机制和数据加密技术保护您的信息安全。但请注意，
                互联网传输不能保证100%安全，我们无法保证信息在传输过程中的绝对安全。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">7. 您的权利</h2>
              <p className="text-muted-foreground leading-relaxed mb-4">
                根据适用的数据保护法律，您可能享有以下权利：
              </p>
              <ul className="list-disc list-inside space-y-2 text-muted-foreground">
                <li>访问您的个人信息</li>
                <li>更正不准确的信息</li>
                <li>删除您的个人信息</li>
                <li>限制或反对信息处理</li>
                <li>数据可携带性</li>
              </ul>
              <p className="text-muted-foreground leading-relaxed mt-4">
                如需行使这些权利，请联系我们的客服团队。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">8. 未成年人保护</h2>
              <p className="text-muted-foreground leading-relaxed">
                我们的服务面向18岁以上的用户。我们不会故意收集未成年人的个人信息。
                如果我们发现收集了未成年人的信息，我们将采取措施删除相关数据。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">9. 政策更新</h2>
              <p className="text-muted-foreground leading-relaxed">
                我们可能会不时更新本隐私政策。更新后的政策将在本页面发布，
                并注明更新日期。建议您定期查看本政策以了解最新信息。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">10. 联系我们</h2>
              <p className="text-muted-foreground leading-relaxed">
                如果您对本隐私政策有任何疑问或需要行使您的权利，请联系我们：
              </p>
              <p className="text-muted-foreground mt-2">
                隐私问题邮箱：privacy@thesis66.com
              </p>
              <p className="text-muted-foreground">
                客服邮箱：support@thesis66.com
              </p>
            </section>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
}
