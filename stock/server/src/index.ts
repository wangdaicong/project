import express from 'express';
import cors from 'cors';
import path from 'node:path';
import util from 'node:util';
import { apiRouter } from './routes/api.js';
import { store } from './store.js';
import { initStore } from './store.js';
import { EastmoneyProvider } from './providers/eastmoney.js';
import { refreshTopAShares, syncQuotes } from './sync/sync.js';
import { refreshNews } from './news/service.js';
import { ensureMysqlTables } from './mysql.js';
import { createStockTables } from './mysqlStore.js';

const app = express();
app.use(cors());
app.use(express.json({ limit: '1mb' }));

app.use('/api', apiRouter);

const publicDir = path.resolve(process.cwd(), 'public');
app.use('/', express.static(publicDir));

const provider = new EastmoneyProvider();

process.on('uncaughtException', (err) => {
  // eslint-disable-next-line no-console
  console.error('[uncaughtException]', err instanceof Error ? err.stack ?? err.message : util.inspect(err, { depth: 5 }));
});

process.on('unhandledRejection', (reason) => {
  // eslint-disable-next-line no-console
  console.error('[unhandledRejection]', reason instanceof Error ? reason.stack ?? reason.message : util.inspect(reason, { depth: 5 }));
});

function isChinaTradingTime(now = new Date()): boolean {
  // 以本机时区为准（通常你本地就是中国时区 UTC+8）。
  // 交易日：周一到周五；时段：09:30-11:30，13:00-15:00
  const day = now.getDay();
  if (day === 0 || day === 6) return false;
  const hh = now.getHours();
  const mm = now.getMinutes();
  const minutes = hh * 60 + mm;
  const amStart = 9 * 60 + 30;
  const amEnd = 11 * 60 + 30;
  const pmStart = 13 * 60;
  const pmEnd = 15 * 60;
  return (minutes >= amStart && minutes <= amEnd) || (minutes >= pmStart && minutes <= pmEnd);
}

function isWeekday(now = new Date()): boolean {
  const day = now.getDay();
  return day !== 0 && day !== 6;
}

function minutesOfDay(now = new Date()): number {
  return now.getHours() * 60 + now.getMinutes();
}

async function precomputeRecoTopOnce(port: number): Promise<void> {
  try {
    // 通过本机回环触发 /api/reco/top 计算并写入 MySQL 缓存
    const url = `http://127.0.0.1:${port}/api/reco/top?limit=20`;
    // Node18+ 自带 fetch
    await fetch(url, { method: 'GET' });
  } catch {
    // ignore
  }
}

async function bootstrap() {
  // MySQL：初始化表（失败不影响主流程）
  try {
    await ensureMysqlTables();
    await createStockTables();
  } catch (e) {
    // ignore
  }

  try {
    await initStore();
  } catch {
    // ignore
  }

  // 尝试初始化“活跃A股”股票池（用于推荐/扩展同步）
  const existingTop = store.countSymbols('TOPA');
  if (!existingTop) {
    try {
      await refreshTopAShares(provider, Number(process.env.TOPA_SIZE ?? 800));
    } catch {
      // ignore
    }
  }

  // 新闻：启动后先拉一次，并定时刷新（默认 10 分钟）
  try {
    await refreshNews();
  } catch {
    // ignore
  }
  const newsIntervalMs = Number(process.env.NEWS_REFRESH_INTERVAL_MS ?? 15 * 60_000);
  setInterval(async () => {
    try {
      await refreshNews();
    } catch {
      // ignore
    }
  }, newsIntervalMs);

  // 弱实时：报价轮询同步（分批），避免免费源被打爆
  const intervalMs = Number(process.env.QUOTE_SYNC_INTERVAL_MS ?? 30_000);
  const batchSize = Number(process.env.QUOTE_SYNC_BATCH ?? 25);
  const windowSize = Number(process.env.QUOTE_SYNC_WINDOW ?? 180);

  let cursor = 0;
  async function syncWindowOnce() {
    const topa = store.getSymbolsByIndexTag('TOPA');
    const uniq = new Map<string, string>();
    for (const s of topa) {
      const mktPrefix = s.market === 'SH' ? 'SH' : 'SZ';
      const code = `${mktPrefix}${s.code}`;
      if (!uniq.has(code)) uniq.set(code, code);
    }
    const universe = Array.from(uniq.values());
    if (!universe.length) return;

    if (cursor >= universe.length) cursor = 0;
    const slice = universe.slice(cursor, cursor + windowSize);
    cursor += windowSize;

    try {
      await syncQuotes(provider, slice, batchSize);
    } catch {
      // ignore
    }
  }

  try {
    await syncWindowOnce();
  } catch {
    // ignore
  }

  setInterval(async () => {
    if (!isChinaTradingTime()) return;
    try {
      await syncWindowOnce();
    } catch {
      // ignore
    }
  }, intervalMs);

  // 预计算推荐：工作日 08:50（开市前）触发一次，并每 1 分钟检查
  const targetMin = Number(process.env.RECO_PRECOMPUTE_MINUTES ?? (8 * 60 + 50));
  let lastPrecomputeDay = '';
  setInterval(async () => {
    const now = new Date();
    if (!isWeekday(now)) return;
    if (minutesOfDay(now) !== targetMin) return;
    const dayKey = `${now.getFullYear()}-${now.getMonth() + 1}-${now.getDate()}`;
    if (dayKey === lastPrecomputeDay) return;
    lastPrecomputeDay = dayKey;
    try {
      await precomputeRecoTopOnce(port);
    } catch {
      // ignore
    }
  }, 60_000);
}

const port = Number(process.env.PORT ?? 5180);
app.listen(port, () => {
  // eslint-disable-next-line no-console
  console.log(`[stock-server] http://localhost:${port}`);
  void bootstrap();
});
