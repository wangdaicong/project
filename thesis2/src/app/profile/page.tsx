"use client";

import { useState } from "react";
import { User, Mail, Lock, Bell, Shield, CreditCard, LogOut } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Header } from "@/components/Header";
import { Footer } from "@/components/Footer";
import { useToast } from "@/components/ui/use-toast";

export default function ProfilePage() {
  const [profile, setProfile] = useState({
    name: "用户",
    email: "user@example.com",
    phone: "",
  });
  const [notifications, setNotifications] = useState({
    email: true,
    marketing: false,
    updates: true,
  });
  const { toast } = useToast();

  const handleSaveProfile = () => {
    toast({
      title: "保存成功",
      description: "您的个人信息已更新",
    });
  };

  const handleChangePassword = () => {
    toast({
      title: "功能开发中",
      description: "密码修改功能正在开发中",
    });
  };

  const handleSaveNotifications = () => {
    toast({
      title: "保存成功",
      description: "通知设置已更新",
    });
  };

  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1 py-8">
        <div className="container mx-auto px-4 max-w-4xl">
          <h1 className="text-3xl font-bold mb-8">个人中心</h1>

          <Tabs defaultValue="profile" className="w-full">
            <TabsList className="grid w-full grid-cols-4">
              <TabsTrigger value="profile">个人信息</TabsTrigger>
              <TabsTrigger value="security">安全设置</TabsTrigger>
              <TabsTrigger value="notifications">通知设置</TabsTrigger>
              <TabsTrigger value="subscription">订阅管理</TabsTrigger>
            </TabsList>

            {/* Profile Tab */}
            <TabsContent value="profile" className="mt-6">
              <Card>
                <CardHeader>
                  <CardTitle>个人信息</CardTitle>
                  <CardDescription>管理您的账户基本信息</CardDescription>
                </CardHeader>
                <CardContent className="space-y-6">
                  <div className="flex items-center gap-6">
                    <div className="w-20 h-20 rounded-full bg-primary/10 flex items-center justify-center">
                      <User className="h-10 w-10 text-primary" />
                    </div>
                    <div>
                      <Button variant="outline" size="sm">
                        更换头像
                      </Button>
                      <p className="text-sm text-muted-foreground mt-1">
                        支持 JPG、PNG 格式，最大 2MB
                      </p>
                    </div>
                  </div>

                  <div className="grid gap-4">
                    <div>
                      <label className="text-sm font-medium mb-2 block">
                        用户名
                      </label>
                      <Input
                        value={profile.name}
                        onChange={(e) =>
                          setProfile({ ...profile, name: e.target.value })
                        }
                      />
                    </div>
                    <div>
                      <label className="text-sm font-medium mb-2 block">
                        邮箱地址
                      </label>
                      <Input
                        type="email"
                        value={profile.email}
                        onChange={(e) =>
                          setProfile({ ...profile, email: e.target.value })
                        }
                      />
                    </div>
                    <div>
                      <label className="text-sm font-medium mb-2 block">
                        手机号码
                      </label>
                      <Input
                        type="tel"
                        placeholder="请输入手机号码"
                        value={profile.phone}
                        onChange={(e) =>
                          setProfile({ ...profile, phone: e.target.value })
                        }
                      />
                    </div>
                  </div>

                  <Button onClick={handleSaveProfile}>保存更改</Button>
                </CardContent>
              </Card>
            </TabsContent>

            {/* Security Tab */}
            <TabsContent value="security" className="mt-6">
              <div className="space-y-6">
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Lock className="h-5 w-5" />
                      修改密码
                    </CardTitle>
                    <CardDescription>定期更换密码以保护账户安全</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div>
                      <label className="text-sm font-medium mb-2 block">
                        当前密码
                      </label>
                      <Input type="password" placeholder="请输入当前密码" />
                    </div>
                    <div>
                      <label className="text-sm font-medium mb-2 block">
                        新密码
                      </label>
                      <Input type="password" placeholder="请输入新密码" />
                    </div>
                    <div>
                      <label className="text-sm font-medium mb-2 block">
                        确认新密码
                      </label>
                      <Input type="password" placeholder="请再次输入新密码" />
                    </div>
                    <Button onClick={handleChangePassword}>更新密码</Button>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Shield className="h-5 w-5" />
                      两步验证
                    </CardTitle>
                    <CardDescription>
                      启用两步验证以增强账户安全性
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="font-medium">两步验证</p>
                        <p className="text-sm text-muted-foreground">
                          当前状态：未启用
                        </p>
                      </div>
                      <Button
                        variant="outline"
                        onClick={() =>
                          toast({
                            title: "功能开发中",
                            description: "两步验证功能正在开发中",
                          })
                        }
                      >
                        启用
                      </Button>
                    </div>
                  </CardContent>
                </Card>

                <Card className="border-destructive/50">
                  <CardHeader>
                    <CardTitle className="text-destructive">危险操作</CardTitle>
                    <CardDescription>
                      以下操作不可逆，请谨慎操作
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="font-medium">删除账户</p>
                        <p className="text-sm text-muted-foreground">
                          永久删除您的账户和所有数据
                        </p>
                      </div>
                      <Button
                        variant="destructive"
                        onClick={() =>
                          toast({
                            title: "确认删除",
                            description: "请联系客服完成账户删除",
                          })
                        }
                      >
                        删除账户
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </TabsContent>

            {/* Notifications Tab */}
            <TabsContent value="notifications" className="mt-6">
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Bell className="h-5 w-5" />
                    通知设置
                  </CardTitle>
                  <CardDescription>管理您接收通知的方式</CardDescription>
                </CardHeader>
                <CardContent className="space-y-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-medium">邮件通知</p>
                      <p className="text-sm text-muted-foreground">
                        接收重要的账户和服务通知
                      </p>
                    </div>
                    <input
                      type="checkbox"
                      checked={notifications.email}
                      onChange={(e) =>
                        setNotifications({
                          ...notifications,
                          email: e.target.checked,
                        })
                      }
                      className="h-5 w-5"
                    />
                  </div>

                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-medium">营销邮件</p>
                      <p className="text-sm text-muted-foreground">
                        接收优惠活动和促销信息
                      </p>
                    </div>
                    <input
                      type="checkbox"
                      checked={notifications.marketing}
                      onChange={(e) =>
                        setNotifications({
                          ...notifications,
                          marketing: e.target.checked,
                        })
                      }
                      className="h-5 w-5"
                    />
                  </div>

                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-medium">产品更新</p>
                      <p className="text-sm text-muted-foreground">
                        接收新功能和产品更新通知
                      </p>
                    </div>
                    <input
                      type="checkbox"
                      checked={notifications.updates}
                      onChange={(e) =>
                        setNotifications({
                          ...notifications,
                          updates: e.target.checked,
                        })
                      }
                      className="h-5 w-5"
                    />
                  </div>

                  <Button onClick={handleSaveNotifications}>保存设置</Button>
                </CardContent>
              </Card>
            </TabsContent>

            {/* Subscription Tab */}
            <TabsContent value="subscription" className="mt-6">
              <div className="space-y-6">
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <CreditCard className="h-5 w-5" />
                      当前订阅
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center justify-between p-4 bg-muted/50 rounded-lg">
                      <div>
                        <p className="font-semibold text-lg">免费版</p>
                        <p className="text-sm text-muted-foreground">
                          每日3次大纲生成，每日1次论文生成
                        </p>
                      </div>
                      <Button asChild>
                        <a href="/pricing">升级方案</a>
                      </Button>
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>使用统计</CardTitle>
                    <CardDescription>本月使用情况</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-4">
                      <div>
                        <div className="flex justify-between mb-2">
                          <span>大纲生成</span>
                          <span>2 / 3 次</span>
                        </div>
                        <div className="h-2 bg-muted rounded-full overflow-hidden">
                          <div
                            className="h-full bg-primary"
                            style={{ width: "66%" }}
                          />
                        </div>
                      </div>
                      <div>
                        <div className="flex justify-between mb-2">
                          <span>论文生成</span>
                          <span>1 / 1 次</span>
                        </div>
                        <div className="h-2 bg-muted rounded-full overflow-hidden">
                          <div
                            className="h-full bg-primary"
                            style={{ width: "100%" }}
                          />
                        </div>
                      </div>
                    </div>
                    <p className="text-sm text-muted-foreground mt-4">
                      配额将于每月1日重置
                    </p>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>支付历史</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground text-center py-8">
                      暂无支付记录
                    </p>
                  </CardContent>
                </Card>
              </div>
            </TabsContent>
          </Tabs>

          {/* Logout Button */}
          <div className="mt-8">
            <Button
              variant="outline"
              className="w-full"
              onClick={() =>
                toast({
                  title: "退出登录",
                  description: "您已成功退出登录",
                })
              }
            >
              <LogOut className="mr-2 h-4 w-4" />
              退出登录
            </Button>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
}
