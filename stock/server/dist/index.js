import express from 'express';
import cors from 'cors';
import path from 'node:path';
import { apiRouter } from './routes/api.js';
import { store } from './store.js';
import { EastmoneyProvider } from './providers/eastmoney.js';
import { refreshTopAShares, syncQuotes } from './sync/sync.js';
import { refreshNews } from './news/service.js';
const app = express();
app.use(cors());
app.use(express.json({ limit: '1mb' }));
app.use('/api', apiRouter);
const publicDir = path.resolve(process.cwd(), 'public');
app.use('/', express.static(publicDir));
const provider = new EastmoneyProvider();
function isChinaTradingTime(now = new Date()) {
    // 以本机时区为准（通常你本地就是中国时区 UTC+8）。
    // 交易日：周一到周五；时段：09:30-11:30，13:00-15:00
    const day = now.getDay();
    if (day === 0 || day === 6)
        return false;
    const hh = now.getHours();
    const mm = now.getMinutes();
    const minutes = hh * 60 + mm;
    const amStart = 9 * 60 + 30;
    const amEnd = 11 * 60 + 30;
    const pmStart = 13 * 60;
    const pmEnd = 15 * 60;
    return (minutes >= amStart && minutes <= amEnd) || (minutes >= pmStart && minutes <= pmEnd);
}
async function bootstrap() {
    // 尝试初始化“活跃A股”股票池（用于推荐/扩展同步）
    const existingTop = store.countSymbols('TOPA');
    if (!existingTop) {
        try {
            await refreshTopAShares(provider, Number(process.env.TOPA_SIZE ?? 800));
        }
        catch {
            // ignore
        }
    }
    // 新闻：启动后先拉一次，并定时刷新（默认 10 分钟）
    try {
        await refreshNews();
    }
    catch {
        // ignore
    }
    const newsIntervalMs = Number(process.env.NEWS_REFRESH_INTERVAL_MS ?? 15 * 60_000);
    setInterval(async () => {
        try {
            await refreshNews();
        }
        catch {
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
        const uniq = new Map();
        for (const s of topa) {
            const mktPrefix = s.market === 'SH' ? 'SH' : 'SZ';
            const code = `${mktPrefix}${s.code}`;
            if (!uniq.has(code))
                uniq.set(code, code);
        }
        const universe = Array.from(uniq.values());
        if (!universe.length)
            return;
        if (cursor >= universe.length)
            cursor = 0;
        const slice = universe.slice(cursor, cursor + windowSize);
        cursor += windowSize;
        try {
            await syncQuotes(provider, slice, batchSize);
        }
        catch {
            // ignore
        }
    }
    try {
        await syncWindowOnce();
    }
    catch {
        // ignore
    }
    setInterval(async () => {
        if (!isChinaTradingTime())
            return;
        await syncWindowOnce();
    }, intervalMs);
}
const port = Number(process.env.PORT ?? 5180);
app.listen(port, () => {
    // eslint-disable-next-line no-console
    console.log(`[stock-server] http://localhost:${port}`);
    void bootstrap();
});
