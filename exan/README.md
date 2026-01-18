# Exan 在线考试系统（K12 MVP + 可扩展综合平台）

## 1. 项目概览
本仓库包含一个在线考试系统的 MVP（优先 K12），后端基于 Spring Boot + MySQL，前端基于 Vue3 + Element Plus。

当前已实现：
- 试卷列表（按学段/学科归类，按年月日倒序展示）
- 爬虫/同步骨架：支持将试卷数据同步落库（当前为示例同步接口，便于后续接入真实来源）
- 练习闭环：创建练习会话 -> 作答 -> 自动判分 -> 错题记录
- 趣味功能：Redis 日榜（ZSET）
- 单次付费（Mock）：订单创建、mock 支付回调、权益发放与查询

## 2. 目录结构
- `exan-server/`：后端（Spring Boot 3 / Java 17 / MyBatis-Plus / SQL init / Redis）
- `exan-web/`：前端（Vite / Vue3 / Element Plus / Pinia / Router / Axios）

## 3. 技术栈
### 3.1 后端
- Spring Boot 3.x
- MySQL 5.6+（推荐 8.0）
- Spring SQL init（schema.sql/data.sql 初始化）
- MyBatis-Plus
- Spring Security（当前为放行模式，预留后续 JWT/RBAC）
- Redis（排行榜）
- springdoc-openapi（Swagger UI）

### 3.2 前端
- Vue 3 + Vite
- Element Plus
- Pinia
- Vue Router
- Axios

## 4. 环境要求
- JDK 17+
- Maven 3+
- Node.js 18+
- MySQL 5.6+（当前兼容 5.6；推荐 8.0）
- Redis 6+

## 5. 配置说明
### 5.1 后端配置
后端配置文件：`exan-server/src/main/resources/application.yml`

关键配置项：
- MySQL：
  - `spring.datasource.url`
  - `spring.datasource.username`
  - `spring.datasource.password`
- Redis：
  - `spring.data.redis.host`
  - `spring.data.redis.port`
- 文件上传：
  - `spring.servlet.multipart.max-file-size`
  - `spring.servlet.multipart.max-request-size`

说明：
- 当前不再提供“题库导入/审核”前端入口，内容来源以“试卷爬取/同步落库”为主。

### 5.2 前端配置
- `exan-web/vite.config.ts`：已配置代理
  - `/api` -> `http://localhost:8080`

## 6. 数据库
### 6.1 初始化方式
- 使用 Spring Boot SQL init 在启动时初始化表结构与种子数据。
- 启动后端时会自动执行：
  - `exan-server/src/main/resources/schema.sql`
  - `exan-server/src/main/resources/data.sql`

编码说明：
- 已在后端配置启用 `spring.sql.init.encoding=UTF-8`，用于避免 `data.sql` 中文种子数据出现乱码。
- 若你在修复前已启动并写入了乱码数据，需清理对应表/库后重启，或用同步接口重新写入。

说明：
- 当前环境兼容 MySQL 5.6（Flyway 10 已不支持 MySQL 5.6，因此移除 Flyway）。
- 若你希望更严格的版本化迁移，可在升级到 MySQL 8 后重新引入 Flyway。

### 6.2 核心表（摘要）
- 基础维度：`edu_stage`、`subject`、`knowledge_point`
- 试卷：`paper`、`paper_question`
- 练习/考试：`exam_session`、`exam_session_question`、`exam_answer`
- 学习闭环：`wrong_question`、`user_favorite_question`
- 单次付费（Mock）：`product`、`orders`、`entitlement`
- 排行榜：Redis ZSET（并预留 `leaderboard_snapshot`）

## 7. 启动与验证
### 7.1 准备数据库与缓存
1) MySQL 创建数据库：`exan`
2) 启动 Redis

### 7.2 启动后端
在 `exan-server/` 目录运行：
- `mvn spring-boot:run`

验证：
- 健康检查：`GET http://localhost:8080/api/health`
- Swagger：`http://localhost:8080/swagger-ui.html`

### 7.3 启动前端
在 `exan-web/` 目录运行：
- `npm install`
- `npm run dev`

Windows 注意：若你在 PowerShell 中执行 `npm` 报脚本执行策略错误，可改用 cmd 运行：
- `npm.cmd run dev`

打开：
- `http://localhost:5173/`

## 8. 功能使用说明（端到端闭环）
### 8.1 试卷列表
页面：`/`

行为：
- 首页选择：学段 -> 年级 -> 学科
- 省份导航：可在试卷列表上方按省份/直辖市/自治区切换筛选（MVP：从试卷标题中解析）
- 下方直接展示对应的试卷列表（按 `paper_date` 倒序）
- 点击某条试卷进入详情页：`/papers/{id}`（MVP：当前仅展示元信息）

### 8.2 试卷同步（爬虫骨架）
接口：`POST /admin/crawlers/papers/sync-demo?stageId=...&subjectId=...`

说明：
- 目前提供示例同步逻辑（用于验证页面与接口链路）。
- 接入真实爬虫后，将按学段/学科把试卷落库，并支持按日期倒序查询。

### 8.3 用户侧：练习
页面：`/practice`

流程：
- 选择学段/学科 -> 开始练习（随机抽题，要求题目状态为 `ONLINE`）
- 作答后提交 -> 交卷 -> 得分统计
- 错题会写入 `wrong_question`

### 8.4 日榜
- 交卷后会写入 Redis ZSET：`lb:daily:{subjectId}:{yyyyMMdd}`
- 接口：`GET /api/leaderboards/daily?subjectId=...`

## 9. 接口清单（节选）
### 9.1 Meta
- `GET /api/meta/stages`
- `GET /api/meta/subjects?stageId=`

### 9.2 试卷
- `GET /api/papers?stageId=&subjectId=&grade=&regionCode=&limit=`（按日期倒序，`grade`、`regionCode` 可选）
- `GET /api/papers/{id}`（试卷详情，MVP：元信息）

### 9.3 试卷同步（爬虫骨架）
- `POST /admin/crawlers/papers/sync-demo?stageId=&subjectId=`

### 9.4 练习
- `POST /api/practice/sessions`
- `GET /api/practice/sessions/{id}`
- `POST /api/practice/sessions/{id}/answers`
- `POST /api/practice/sessions/{id}/submit`

### 9.5 订单（Mock）
- `POST /api/orders`
- `POST /api/orders/{orderNo}/mock-pay`
- `GET /api/orders`
- `GET /api/orders/entitlements`

## 10. 重要说明
- 当前为 MVP 跑通版本：
  - 登录鉴权未完全接入，前端默认带 `X-User-Id: 1` 模拟用户。
  - 主观题未实现。
  - 真题来源合规策略与 PDF/图片解析为后续阶段。

## 11. 下一阶段规划（建议）
- 更新包（Update Package）：将导入任务发布为“更新包”，前台展示更新动态
- 订阅/通知：按学科/地区订阅更新包
- OCR/PDF 导入：导入任务异步化（MQ/任务调度）
- 权限体系：JWT + RBAC（学生/运营/管理员）
