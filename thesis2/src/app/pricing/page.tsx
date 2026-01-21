"use client";

import { Check } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import Link from "next/link";

const plans = [
  {
    name: "免费版",
    price: "0",
    description: "适合初次体验用户",
    features: [
      "每日3次大纲生成",
      "每日1次论文生成",
      "最多3000字论文",
      "基础学科支持",
      "在线客服支持",
    ],
    buttonText: "免费开始",
    buttonVariant: "outline" as const,
    popular: false,
  },
  {
    name: "专业版",
    price: "99",
    period: "/月",
    description: "适合学生和研究人员",
    features: [
      "无限次大纲生成",
      "每日20次论文生成",
      "最多20000字论文",
      "720+学科全覆盖",
      "查重率保障",
      "优先客服支持",
      "论文修改建议",
    ],
    buttonText: "立即订阅",
    buttonVariant: "default" as const,
    popular: true,
  },
  {
    name: "企业版",
    price: "299",
    period: "/月",
    description: "适合团队和机构",
    features: [
      "无限次大纲生成",
      "无限次论文生成",
      "不限字数",
      "720+学科全覆盖",
      "查重率保障",
      "专属客服经理",
      "API接口支持",
      "团队协作功能",
      "定制化服务",
    ],
    buttonText: "联系销售",
    buttonVariant: "outline" as const,
    popular: false,
  },
];

export default function PricingPage() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1 py-20">
        <div className="container mx-auto px-4">
          <div className="text-center mb-12">
            <h1 className="text-4xl font-bold mb-4">选择适合您的方案</h1>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              灵活的定价方案，满足不同用户的需求
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto">
            {plans.map((plan, index) => (
              <Card
                key={index}
                className={`relative ${
                  plan.popular
                    ? "border-primary shadow-lg scale-105"
                    : ""
                }`}
              >
                {plan.popular && (
                  <div className="absolute -top-4 left-1/2 -translate-x-1/2">
                    <span className="bg-primary text-primary-foreground text-sm font-medium px-4 py-1 rounded-full">
                      最受欢迎
                    </span>
                  </div>
                )}
                <CardHeader className="text-center">
                  <CardTitle className="text-2xl">{plan.name}</CardTitle>
                  <CardDescription>{plan.description}</CardDescription>
                  <div className="mt-4">
                    <span className="text-4xl font-bold">¥{plan.price}</span>
                    {plan.period && (
                      <span className="text-muted-foreground">{plan.period}</span>
                    )}
                  </div>
                </CardHeader>
                <CardContent>
                  <ul className="space-y-3">
                    {plan.features.map((feature, i) => (
                      <li key={i} className="flex items-center gap-2">
                        <Check className="h-5 w-5 text-primary flex-shrink-0" />
                        <span className="text-sm">{feature}</span>
                      </li>
                    ))}
                  </ul>
                </CardContent>
                <CardFooter>
                  <Button
                    className="w-full"
                    variant={plan.buttonVariant}
                    size="lg"
                    asChild
                  >
                    <Link href="/write">{plan.buttonText}</Link>
                  </Button>
                </CardFooter>
              </Card>
            ))}
          </div>

          {/* FAQ Section */}
          <div className="mt-20 max-w-3xl mx-auto">
            <h2 className="text-2xl font-bold text-center mb-8">常见问题</h2>
            <div className="space-y-6">
              <div>
                <h3 className="font-semibold mb-2">查重率保障是什么意思？</h3>
                <p className="text-muted-foreground">
                  我们承诺生成的论文在知网查重率约为10%左右，如果超过15%，您可以联系客服申请退款。
                </p>
              </div>
              <div>
                <h3 className="font-semibold mb-2">可以随时取消订阅吗？</h3>
                <p className="text-muted-foreground">
                  是的，您可以随时取消订阅。取消后，您仍可使用服务直到当前计费周期结束。
                </p>
              </div>
              <div>
                <h3 className="font-semibold mb-2">生成的论文可以直接提交吗？</h3>
                <p className="text-muted-foreground">
                  我们建议您将生成的内容作为参考和初稿，进行人工审核和修改后再使用。
                  直接提交AI生成内容可能违反学术规范。
                </p>
              </div>
              <div>
                <h3 className="font-semibold mb-2">支持哪些支付方式？</h3>
                <p className="text-muted-foreground">
                  我们支持微信支付、支付宝、银行卡等多种支付方式。
                </p>
              </div>
            </div>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
}
