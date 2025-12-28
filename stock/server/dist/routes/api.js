import { Router } from 'express';
import { z } from 'zod';
import { store } from '../store.js';
import { EastmoneyProvider } from '../providers/eastmoney.js';
import { syncKlines, syncQuotes } from '../sync/sync.js';
import { calcMACD } from '../indicators/macd.js';
import { makeReco } from '../reco/recommender.js';
import { INDICES, getIndexBySymbol } from '../indices/indices.js';
import { getIndexKlines, getIndexQuote } from '../indices/eastmoneyIndex.js';
import { refreshNews } from '../news/service.js';
const provider = new EastmoneyProvider();
export const apiRouter = Router();
function extractHotNewsKeywords(max = 5) {
    const items = store.getNews(120);
    const text = items
        .map((x) => `${x.title ?? ''} ${(x.summary ?? '').slice(0, 120)}`)
        .join(' ')
        .toLowerCase();
    const candidates = [
        '美联储',
        '降息',
        '加息',
        '通胀',
        '美元',
        '关税',
        '出口',
        '人民币',
        'ai',
        '芯片',
        '半导体',
        '新能源',
        '光伏',
        '储能',
        '算力',
        '大模型',
        '汽车',
        '医药',
        '地产',
        '银行',
        '券商',
        '黄金',
        '原油'
    ];
    const scored = candidates
        .map((k) => {
        const key = k.toLowerCase();
        const re = new RegExp(key.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g');
        const n = (text.match(re) ?? []).length;
        return { k, n };
    })
        .filter((x) => x.n > 0)
        .sort((a, b) => b.n - a.n)
        .slice(0, Math.max(0, Math.min(10, max)));
    return scored.map((x) => x.k);
}
function makeFallbackRecoFromPrice(code, price, newsKeywords) {
    const entry = Number((price * 0.99).toFixed(2));
    const risk = Number((price * 0.95).toFixed(2));
    const exit = Number((price * 1.06).toFixed(2));
    const parts = [];
    parts.push('技术面：K线不足或免费源波动，已降级为基于现价的示例推荐');
    if (newsKeywords.length)
        parts.push(`时事面：近期高频主题 ${newsKeywords.slice(0, 5).join(' / ')}（仅供参考）`);
    parts.push('风控：建议严格设置止损线，避免单笔亏损扩大');
    return { code, ts: Date.now(), entry, exit, risk, reason: parts.join('；') };
}
apiRouter.get('/health', (_req, res) => {
    res.json({ ok: true, ts: Date.now() });
});
apiRouter.get('/hs300', (_req, res) => {
    const rows = store.getSymbolsByIndexTag('HS300').map((x) => ({ code: x.code, name: x.name, market: x.market }));
    res.json({ data: rows });
});
apiRouter.get('/indices', (_req, res) => {
    res.json({ data: INDICES });
});
apiRouter.get('/index/quotes', async (_req, res) => {
    const out = [];
    for (const idx of INDICES) {
        try {
            const quote = await getIndexQuote(idx.secid, idx.symbol);
            if (quote)
                out.push({ symbol: idx.symbol, name: idx.name, quote });
        }
        catch {
            // ignore
        }
    }
    res.json({ data: out });
});
apiRouter.get('/index/quote', async (req, res) => {
    const q = z.object({ symbol: z.string().min(1) }).safeParse(req.query);
    if (!q.success)
        return res.status(400).json({ error: 'bad_request' });
    const idx = getIndexBySymbol(q.data.symbol);
    if (!idx)
        return res.status(404).json({ error: 'not_found' });
    try {
        const quote = await getIndexQuote(idx.secid, idx.symbol);
        res.json({ data: { ...quote, name: idx.name } });
    }
    catch {
        res.status(502).json({ error: 'provider_error' });
    }
});
apiRouter.get('/index/kline', async (req, res) => {
    const q = z
        .object({
        symbol: z.string().min(1),
        limit: z.coerce.number().int().min(20).max(500).default(200)
    })
        .safeParse(req.query);
    if (!q.success)
        return res.status(400).json({ error: 'bad_request' });
    const idx = getIndexBySymbol(q.data.symbol);
    if (!idx)
        return res.status(404).json({ error: 'not_found' });
    try {
        const klines = await getIndexKlines(idx.secid, idx.symbol, q.data.limit);
        const ts = klines.map((k) => k.ts);
        const close = klines.map((k) => k.close);
        const macd = calcMACD(ts, close);
        res.json({ data: { klines, macd, name: idx.name } });
    }
    catch {
        res.status(502).json({ error: 'provider_error' });
    }
});
apiRouter.get('/quote', async (req, res) => {
    const q = z.object({ code: z.string().min(1) }).safeParse(req.query);
    if (!q.success)
        return res.status(400).json({ error: 'bad_request' });
    try {
        const quote = await provider.getQuote(q.data.code);
        res.json({ data: quote });
    }
    catch {
        res.status(502).json({ error: 'provider_error' });
    }
});
apiRouter.post('/news/refresh', async (_req, res) => {
    try {
        const n = await refreshNews();
        res.json({ ok: true, inserted: n });
    }
    catch {
        res.status(502).json({ error: 'provider_error' });
    }
});
apiRouter.get('/kline', async (req, res) => {
    const q = z
        .object({
        code: z.string().min(1),
        limit: z.coerce.number().int().min(20).max(500).default(200)
    })
        .safeParse(req.query);
    if (!q.success)
        return res.status(400).json({ error: 'bad_request' });
    // 先尝试从DB取；不足则在线补齐
    const rows = store.getKlines(q.data.code, q.data.limit);
    if (rows.length < Math.min(60, q.data.limit)) {
        try {
            await syncKlines(provider, q.data.code, q.data.limit);
        }
        catch {
            // ignore
        }
    }
    const rows2 = store.getKlines(q.data.code, q.data.limit);
    const ts = rows2.map((r) => r.ts);
    const close = rows2.map((r) => r.close);
    const macd = calcMACD(ts, close);
    res.json({
        data: {
            klines: rows2,
            macd
        }
    });
});
apiRouter.get('/reco', async (req, res) => {
    const q = z.object({ code: z.string().min(1), limit: z.coerce.number().int().min(60).max(500).default(200) }).safeParse(req.query);
    if (!q.success)
        return res.status(400).json({ error: 'bad_request' });
    try {
        await syncKlines(provider, q.data.code, q.data.limit);
    }
    catch {
        // ignore
    }
    const rows = store
        .getKlines(q.data.code, q.data.limit)
        .map((x) => ({ ts: x.ts, close: x.close }));
    const newsKeywords = extractHotNewsKeywords(5);
    const reco = makeReco(q.data.code, rows, newsKeywords);
    res.json({ data: reco });
});
apiRouter.get('/reco/top', async (req, res) => {
    const q = z
        .object({
        limit: z.coerce.number().int().min(1).max(50).default(20)
    })
        .safeParse(req.query);
    if (!q.success)
        return res.status(400).json({ error: 'bad_request' });
    const seed = [
        { code: 'SH600519', name: '贵州茅台', market: 'SH' },
        { code: 'SH601318', name: '中国平安', market: 'SH' },
        { code: 'SH600036', name: '招商银行', market: 'SH' },
        { code: 'SH600276', name: '恒瑞医药', market: 'SH' },
        { code: 'SH600887', name: '伊利股份', market: 'SH' },
        { code: 'SH601888', name: '中国中免', market: 'SH' },
        { code: 'SH600030', name: '中信证券', market: 'SH' },
        { code: 'SH601398', name: '工商银行', market: 'SH' },
        { code: 'SZ000333', name: '美的集团', market: 'SZ' },
        { code: 'SZ000651', name: '格力电器', market: 'SZ' },
        { code: 'SZ000001', name: '平安银行', market: 'SZ' },
        { code: 'SZ000858', name: '五粮液', market: 'SZ' },
        { code: 'SZ002415', name: '海康威视', market: 'SZ' },
        { code: 'SZ002594', name: '比亚迪', market: 'SZ' },
        { code: 'SZ300750', name: '宁德时代', market: 'SZ' },
        { code: 'SZ300059', name: '东方财富', market: 'SZ' },
        { code: 'SZ300760', name: '迈瑞医疗', market: 'SZ' },
        { code: 'SZ002475', name: '立讯精密', market: 'SZ' },
        { code: 'SZ002714', name: '牧原股份', market: 'SZ' },
        { code: 'SH601012', name: '隆基绿能', market: 'SH' }
    ];
    const topa = store.getSymbolsByIndexTag('TOPA');
    const uniq = new Map();
    for (const s of seed)
        uniq.set(s.code, s);
    for (const s of topa) {
        const mktPrefix = s.market === 'SH' ? 'SH' : 'SZ';
        const code = `${mktPrefix}${s.code}`;
        if (!uniq.has(code))
            uniq.set(code, { code, name: s.name, market: s.market });
    }
    const codes = Array.from(uniq.values()).map((x) => x.code);
    const newsKeywords = extractHotNewsKeywords(5);
    const out = [];
    const maxTry = Math.min(200, codes.length);
    for (let i = 0; i < maxTry && out.length < q.data.limit; i++) {
        const code = codes[i];
        let quote = null;
        try {
            await syncKlines(provider, code, 220);
        }
        catch {
            // ignore
        }
        const rows = store.getKlines(code, 220).map((x) => ({ ts: x.ts, close: x.close }));
        let reco = makeReco(code, rows, newsKeywords);
        if (!reco) {
            try {
                const qt = await provider.getQuote(code);
                quote = qt;
                if (qt?.price != null)
                    reco = makeFallbackRecoFromPrice(code, qt.price, newsKeywords);
            }
            catch {
                // ignore
            }
        }
        if (!quote) {
            try {
                quote = await provider.getQuote(code);
            }
            catch {
                // ignore
            }
        }
        if (reco) {
            const meta = uniq.get(code);
            out.push({ code, name: meta?.name ?? '', market: meta?.market ?? '', quote, reco });
        }
    }
    res.json({ data: out, meta: { keywords: newsKeywords } });
});
apiRouter.post('/sync/quotes', async (req, res) => {
    const body = z
        .object({
        codes: z.array(z.string().min(1)).min(1).max(300)
    })
        .safeParse(req.body);
    if (!body.success)
        return res.status(400).json({ error: 'bad_request' });
    try {
        const n = await syncQuotes(provider, body.data.codes, 20);
        res.json({ ok: true, inserted: n });
    }
    catch {
        res.status(502).json({ error: 'provider_error' });
    }
});
