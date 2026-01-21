import Link from "next/link";
import { FileText } from "lucide-react";

export function Footer() {
  return (
    <footer className="border-t bg-muted/50">
      <div className="container mx-auto px-4 py-12">
        <div className="hidden grid grid-cols-1 md:grid-cols-4 gap-8">
          <div className="space-y-4">
            <Link href="/" className="flex items-center space-x-2">
              <FileText className="h-6 w-6 text-primary" />
              <span className="text-lg font-bold">Easy AI</span>
            </Link>
            <p className="text-sm text-muted-foreground">
              AI智能论文写作平台，覆盖720+学科专业，10秒生成大纲，3分钟生成万字论文。
            </p>
          </div>

          <div className="hidden">
            <h3 className="font-semibold mb-4">产品服务</h3>
            <ul className="space-y-2 text-sm text-muted-foreground">
              <li><Link href="/write" className="hover:text-primary">论文写作</Link></li>
              <li><Link href="/outline" className="hover:text-primary">大纲生成</Link></li>
              <li><Link href="/check" className="hover:text-primary">论文检测</Link></li>
              <li><Link href="/templates" className="hover:text-primary">论文模板</Link></li>
              <li><Link href="/examples" className="hover:text-primary">论文示例</Link></li>
              <li><Link href="/pricing" className="hover:text-primary">价格方案</Link></li>
            </ul>
          </div>

          <div className="hidden">
            <h3 className="font-semibold mb-4">支持</h3>
            <ul className="space-y-2 text-sm text-muted-foreground">
              <li><Link href="/about" className="hover:text-primary">关于我们</Link></li>
              <li><Link href="/help" className="hover:text-primary">帮助中心</Link></li>
              <li><Link href="/contact" className="hover:text-primary">联系我们</Link></li>
            </ul>
          </div>

          <div className="hidden">
            <h3 className="font-semibold mb-4">法律条款</h3>
            <ul className="space-y-2 text-sm text-muted-foreground">
              <li><Link href="/terms" className="hover:text-primary">服务条款</Link></li>
              <li><Link href="/privacy" className="hover:text-primary">隐私政策</Link></li>
              <li><Link href="/disclaimer" className="hover:text-primary">免责声明</Link></li>
            </ul>
          </div>
        </div>

        <div className="border-t mt-8 pt-8 text-center text-sm text-muted-foreground">
          <p>&copy; {new Date().getFullYear()} Easy AI. All rights reserved.</p>
          <p className="mt-2">
            本平台仅供学习参考使用，请勿直接提交AI生成的内容作为学术成果。
          </p>
        </div>
      </div>
    </footer>
  );
}
