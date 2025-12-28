# A股实时数据同步与推荐（MVP）

## 运行

在 `server` 目录执行：

- `npm install --include=dev`
- `npm run dev`

然后打开：

- http://localhost:5180

如果你本机终端出现 `node` / `npm` 找不到（Windows PATH 未刷新），可以用：

- `set PATH=C:\Program Files\nodejs;%PATH% && npm.cmd run dev`

## 说明


### 技术栈

- **后端**：Node.js + TypeScript + Express
- **前端**：静态 `index.html` + **ECharts**（CDN 引入）
- **数据抓取**：axios
- **新闻解析**：fast-xml-parser + iconv-lite（用于兼容 GBK/GB2312 等编码）
- **本地存储**：MySQL（优先）+ JSON 文件兜底（`server/data/store.json`）

### MySQL（本地）

连接参数通过环境变量提供：

- `MYSQL_HOST`
- `MYSQL_PORT`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `MYSQL_PASSWORD_ENC`（可选：加密后的密码，AES-256-GCM）
- `MYSQL_PASSWORD_KEY`（可选：解密密钥，base64，32字节）
- `MYSQL_DB`

密码加密说明（可选）：

- 加密格式：`MYSQL_PASSWORD_ENC = iv_base64:cipher_base64:tag_base64`
- 加密算法：AES-256-GCM

生成示例（PowerShell / Windows）：

```bash
node -e "const crypto=require('crypto'); const pwd=process.env.PWD||'1234'; const key=crypto.randomBytes(32); const iv=crypto.randomBytes(12); const c=crypto.createCipheriv('aes-256-gcm',key,iv); const enc=Buffer.concat([c.update(pwd,'utf8'),c.final()]); const tag=c.getAuthTag(); console.log('MYSQL_PASSWORD_KEY='+key.toString('base64')); console.log('MYSQL_PASSWORD_ENC='+iv.toString('base64')+':'+enc.toString('base64')+':'+tag.toString('base64'));"
```

注意：`MYSQL_PASSWORD_ENC` 和 `MYSQL_PASSWORD_KEY` **需要同时设置**才会启用解密；否则回退到 `MYSQL_PASSWORD`。

服务启动时会自动建表：

- `symbols`（股票池：HS300/TOPA 等）
- `quotes`（最新报价，每个 code 1 行）
- `klines`（日K，主键 code+ts）
- `news`（RSS 聚合）
- `reco_top_cache`（推荐 Top 缓存）

说明：

- 读取优先使用 MySQL 初始化加载到内存的数据；MySQL 不可用时自动回退到 JSON。
- 写入时同步落 JSON，同时异步写入 MySQL（MySQL 出错不影响主流程）。

### 数据源 / 资源（免费弱实时）

- **A股个股行情/日K**：东方财富相关公开接口（可能变更/限流）
- **主要指数行情/日K**：东方财富相关公开接口（可能变更/限流）
- **推荐辅助（时事关键词）**：后台定时聚合公开 RSS，用于提取热点关键词增强推荐理由（前端不展示新闻列表）
- **图表组件**：ECharts CDN

### 已实现模块

- **个股模块**：
  - 报价（现价、涨跌幅、开高低昨收等）
  - 日K线
  - 指标：MACD（DIF/DEA/MACD柱）
  - 推荐价位：入仓/出仓/风险价（基于技术面信号生成，具体可按需替换）
- **指数模块**：
  - 内置指数：上证/深成指/创业板/沪深300/中证500/科创50
  - 指数报价（顶部卡片自动刷新）
  - 指数日K + MACD（后端接口保留，可用于后续扩展）
- **推荐模块（重点）**：
  - 推荐 20 只个股（技术面评分为主 + 时事热点关键词辅助增强解释与排序）
  - 推荐卡片：入仓/出仓/风险价 + 推荐理由；点击可一键查看K线与MACD

### 主要接口

- `GET /api/health`
- `GET /api/quote?code=600000`
- `GET /api/kline?code=600000&limit=200`
- `GET /api/reco?code=600000`
- `GET /api/reco/top?limit=20`
- `GET /api/indices`
- `GET /api/index/quotes`
- `GET /api/index/quote?symbol=SH000300`
- `GET /api/index/kline?symbol=SH000300&limit=200`
- `POST /api/news/refresh`

### 前端设置项（首页右上角「设置」）

- **推荐列表刷新间隔**：定时重新拉取 `GET /api/reco/top`，刷新推荐卡片。
- **个股/图表刷新间隔**：当你已加载某只个股时，定时刷新其报价/日K/推荐。

设置会保存在浏览器的 `localStorage`（键：`stock_mvp_cfg_v1`），刷新页面不会丢。

### 使用到的东方财富免费接口（可能变更/限流）

- **个股报价**：`https://push2.eastmoney.com/api/qt/stock/get`
- **个股日K**：`https://push2his.eastmoney.com/api/qt/stock/kline/get`
- **指数报价**：`https://push2.eastmoney.com/api/qt/stock/get`（使用指数 secid）
- **指数日K**：`https://push2his.eastmoney.com/api/qt/stock/kline/get`（使用指数 secid）
- **股票池（活跃A股 TOPA）**：`https://push2.eastmoney.com/api/qt/clist/get`

### 同步策略

- 个股报价：基于“活跃A股股票池（TOPA）”按窗口分批轮询同步（交易时段生效），避免免费源被打爆；若免费源波动导致 TOPA 为空，推荐模块使用内置兜底股票池。
- 推荐：前端加载时拉取 `reco/top`；优先读取 MySQL `reco_top_cache`（默认 TTL 30 分钟），缓存未命中再现算并回写。
- 预计算：工作日 08:50（默认，可用 `RECO_PRECOMPUTE_MINUTES` 调整）自动触发一次 `/api/reco/top?limit=20` 计算，把结果提前写入 MySQL 缓存。
- 新闻（仅作推荐辅助）：启动后刷新一次，默认每 15 分钟刷新（可用 `NEWS_REFRESH_INTERVAL_MS` 调整）。

### 已知限制

- 免费源接口可能变更/限流，出现空数据属于正常现象。
- 本项目的“推荐价位/理由”基于可解释的规则与免费数据源，仍不构成投资建议。
- 本项目通过 MySQL/JSON 缓存与降级策略尽量保证页面可用。

### 下一步建议

- 仅使用免费源的前提下，增加更多冗余数据源与自动降级（多源聚合/多站点RSS/接口失败回退到缓存）
- 增加更多指标（KDJ/RSI/BOLL）与多周期K线（分钟级，仍需注意免费源限流）
- 做“主题/行业/概念”轻量映射（关键词→主题→候选股票池），强化推荐解释性
- 引入自选股、分组看板与简单回测（可继续沿用本地 MySQL/JSON 存储）

页面内容仅供学习研究，不构成投资建议。
