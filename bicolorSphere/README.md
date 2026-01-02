# bicolorSphere

一个可运行的 **Spring Boot + MySQL** 示例项目（前端为静态页面 `static/index.html`）：

- 抓取中彩网历史开奖数据并入库
- 提供趋势、冷热号、遗漏、预测、回测、策略推荐、导出等 API
- 前端提供趋势表、同步、预测弹窗、统计弹窗

> 说明：本项目的“预测/推荐/回测/统计”均为基于历史数据的实验性功能，仅供娱乐。

## 1. 环境要求

- JDK 8+（本项目当前以 Java 8 运行通过）
- Maven 3.8+
- MySQL 8+

## 2. MySQL 初始化

1) 创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS bicolorSphere DEFAULT CHARACTER SET utf8mb4;
```

2) 配置账号密码：默认使用 `root/1234`（见 `src/main/resources/application.yml`）。

> 项目启动时会自动执行 `classpath:schema.sql` 创建表。

## 3. 启动

在项目根目录执行：

```bash
mvn spring-boot:run
```

如遇 Maven 证书问题（PKIX），可以使用项目根目录提供的镜像配置运行：

```bash
mvn -s .\mvn-settings.xml spring-boot:run
```

启动后访问：

- 前端页面：`http://localhost:8080/`
- 健康检查：`GET http://localhost:8080/api/health`

## 4. 同步数据

第一次使用建议先同步 1~5 页：

- `POST http://localhost:8080/api/sync?fromPage=1&toPage=5`

后续可使用“补齐缺失数据”接口（前端“同步数据”按钮默认调用）：

- `POST http://localhost:8080/api/sync/missing?maxPages=120&stopAfterNoInsertPages=3`

同步后刷新首页即可看到图表。

## 5. API 列表

- `GET /api/health`
- `POST /api/sync?fromPage=1&toPage=5`
- `POST /api/sync/missing?maxPages=120&stopAfterNoInsertPages=3`

### 5.1 开奖数据

- `GET /api/draws?page=0&size=20`
- `GET /api/draws/search?drawNoFrom=&drawNoTo=&dateFrom=&dateTo=&includeRed=&includeBlue=&page=0&size=20`
- `GET /api/draws/export?drawNoFrom=&drawNoTo=&dateFrom=&dateTo=&includeRed=&includeBlue=&maxRows=5000`

### 5.2 统计与趋势

- `GET /api/trend?latestN=300`
- `GET /api/hotcold?latestN=300`
- `GET /api/omission?latestN=300`

> 前端“统计”按钮为纯前端计算：基于当前已加载的 `/api/trend` 数据统计
> - 和值
> - 跨度
> - 区间比（1-11 / 12-22 / 23-33）
> - 奇偶比

### 5.3 预测

- `GET /api/predict?latestN=300&strategy=hybrid&count=5`

支持策略（`strategy`）：

- `frequency_top`：热号优先
- `omission_top`：遗漏优先
- `hybrid`：综合（热度+遗漏）
- `weighted_random`：加权随机
- `zone_balanced`：分区均衡（默认按 2:2:2 思路采样）
- `markov`：马尔科夫链（相邻期转移得分 + 平滑）
- `bayes`：贝叶斯推理（上一期特征桶 -> 下一期号码条件频率）
- `ml`：机器学习（轻量线性打分：热度+遗漏+近期趋势+马尔科夫）

支持约束参数（可选）：

- `minSum` / `maxSum`：和值范围
- `minSpan` / `maxSpan`：跨度范围
- `minOdd` / `maxOdd`：奇数个数范围
- `zoneRatio`：区间比，如 `2:2:2`
- `danReds` / `killReds`：红胆/红杀（空格或逗号分隔）
- `danBlues` / `killBlues`：蓝胆/蓝杀（空格或逗号分隔）
- `maxTry`：不满足约束时的最大重试次数

### 5.4 回测与策略推荐

- `GET /api/backtest?strategy=hybrid&trainWindow=200&testCount=50`
- `GET /api/recommend?trainWindow=200&testCount=80`

## 6. 说明

- 前端页面：`http://localhost:8080/`
- “预测/推荐/回测/统计”均为基于历史数据的实验性功能，仅供娱乐，不构成任何保证或建议。
