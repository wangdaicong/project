"use client";

import Link from "next/link";
import { FileText, Menu, X } from "lucide-react";
import { useState } from "react";
import { Button } from "@/components/ui/button";

export function Header() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto flex h-16 items-center justify-between px-4">
        <Link href="/" className="flex items-center space-x-2">
          <FileText className="h-8 w-8 text-primary" />
          <span className="text-xl font-bold gradient-text">Easy AI</span>
        </Link>

        {/* Desktop Navigation */}
        <nav className="hidden md:flex items-center space-x-6">
          <Link href="/" className="text-sm font-medium hover:text-primary transition-colors">
            首页
          </Link>
          <Link href="/write" className="text-sm font-medium hover:text-primary transition-colors">
            开始写作
          </Link>
          <Link href="/outline" className="text-sm font-medium hover:text-primary transition-colors">
            生成大纲
          </Link>
          <Link href="/check" className="text-sm font-medium hover:text-primary transition-colors">
            论文检测
          </Link>
          <Link href="/help" className="text-sm font-medium hover:text-primary transition-colors">
            帮助
          </Link>
        </nav>

        <div className="hidden md:flex items-center space-x-4">
          <Button variant="ghost" asChild>
            <Link href="/login">登录</Link>
          </Button>
        </div>

        {/* Mobile Menu Button */}
        <button
          className="md:hidden p-2"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
        >
          {mobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
        </button>
      </div>

      {/* Mobile Navigation */}
      {mobileMenuOpen && (
        <div className="md:hidden border-t bg-background">
          <nav className="flex flex-col p-4 space-y-4">
            <Link href="/" className="text-sm font-medium hover:text-primary" onClick={() => setMobileMenuOpen(false)}>
              首页
            </Link>
            <Link href="/write" className="text-sm font-medium hover:text-primary" onClick={() => setMobileMenuOpen(false)}>
              开始写作
            </Link>
            <Link href="/outline" className="text-sm font-medium hover:text-primary" onClick={() => setMobileMenuOpen(false)}>
              生成大纲
            </Link>
            <Link href="/check" className="text-sm font-medium hover:text-primary" onClick={() => setMobileMenuOpen(false)}>
              论文检测
            </Link>
            <Link href="/help" className="text-sm font-medium hover:text-primary" onClick={() => setMobileMenuOpen(false)}>
              帮助
            </Link>
            <div className="flex flex-col space-y-2 pt-4 border-t">
              <Button variant="outline" asChild>
                <Link href="/login">登录</Link>
              </Button>
            </div>
          </nav>
        </div>
      )}
    </header>
  );
}
