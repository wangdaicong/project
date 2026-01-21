import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";

export default function TermsPage() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1 py-12">
        <div className="container mx-auto px-4 max-w-4xl">
          <h1 className="text-4xl font-bold mb-8">服务条款</h1>
          
          <div className="prose prose-gray max-w-none space-y-6">
            <p className="text-muted-foreground">
              最后更新日期：2025年1月1日
            </p>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">1. 服务说明</h2>
              <p className="text-muted-foreground leading-relaxed">
                66论文写作平台（以下简称"本平台"）是一个基于人工智能技术的论文写作辅助工具。
                本平台提供论文大纲生成、论文内容生成等服务，旨在帮助用户提高写作效率，
                激发创作灵感。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">2. 用户责任</h2>
              <p className="text-muted-foreground leading-relaxed mb-4">
                用户在使用本平台服务时，应当遵守以下规定：
              </p>
              <ul className="list-disc list-inside space-y-2 text-muted-foreground">
                <li>遵守中华人民共和国相关法律法规</li>
                <li>遵守学术诚信原则，不得将AI生成内容直接作为原创作品提交</li>
                <li>对生成的内容进行人工审核和修改</li>
                <li>不得利用本平台从事任何违法违规活动</li>
                <li>不得侵犯他人知识产权或其他合法权益</li>
              </ul>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">3. 学术诚信声明</h2>
              <p className="text-muted-foreground leading-relaxed">
                本平台生成的内容仅供学习参考使用。用户应当了解，直接提交AI生成的论文内容
                可能违反学校或机构的学术规范，可能导致学术不端的处分。用户应当对生成的内容
                进行充分的审核、修改和完善，确保最终提交的作品符合学术诚信要求。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">4. 知识产权</h2>
              <p className="text-muted-foreground leading-relaxed">
                本平台的软件、技术、界面设计等知识产权归本平台所有。用户通过本平台生成的内容，
                其著作权归属存在法律模糊区域。我们建议用户对重要成果进行原创认证登记。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">5. 服务费用</h2>
              <p className="text-muted-foreground leading-relaxed">
                本平台提供免费和付费服务。付费服务的具体价格和内容以平台公布的信息为准。
                用户在购买付费服务前，应当仔细阅读相关说明。已支付的费用，除本条款另有规定外，
                一般不予退还。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">6. 退款政策</h2>
              <p className="text-muted-foreground leading-relaxed">
                如果用户使用本平台生成的论文在知网查重率超过15%，用户可以在7个工作日内
                联系客服申请退款，需提供官方检测报告。其他情况下，订阅费用不予退款。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">7. 免责声明</h2>
              <p className="text-muted-foreground leading-relaxed mb-4">
                本平台不对以下情况承担责任：
              </p>
              <ul className="list-disc list-inside space-y-2 text-muted-foreground">
                <li>用户因违反学术诚信规定而受到的处分</li>
                <li>AI生成内容的准确性、完整性或适用性</li>
                <li>因用户自身原因导致的任何损失</li>
                <li>因不可抗力导致的服务中断或数据丢失</li>
              </ul>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">8. 服务变更与终止</h2>
              <p className="text-muted-foreground leading-relaxed">
                本平台有权根据业务发展需要，变更、中断或终止部分或全部服务。
                对于付费用户，我们会提前通知并妥善处理相关事宜。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">9. 条款修改</h2>
              <p className="text-muted-foreground leading-relaxed">
                本平台有权根据需要修改本服务条款。修改后的条款将在平台上公布，
                用户继续使用本平台服务即视为接受修改后的条款。
              </p>
            </section>

            <section>
              <h2 className="text-2xl font-semibold mt-8 mb-4">10. 联系方式</h2>
              <p className="text-muted-foreground leading-relaxed">
                如果您对本服务条款有任何疑问，请联系我们：
              </p>
              <p className="text-muted-foreground mt-2">
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
