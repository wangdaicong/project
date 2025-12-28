import { Router, type Request, type Response } from 'express';
import { z } from 'zod';
import { store } from '../store.js';
import { EastmoneyProvider } from '../providers/eastmoney.js';
import { syncKlines, syncQuotes } from '../sync/sync.js';
import { calcMACD } from '../indicators/macd.js';
import { computeRecoScore, makeReco } from '../reco/recommender.js';
import { INDICES, getIndexBySymbol } from '../indices/indices.js';
import { getIndexKlines, getIndexQuote } from '../indices/eastmoneyIndex.js';
import { refreshNews } from '../news/service.js';
import { getRecoTopCache, setRecoTopCache } from '../reco/cache.js';
import { insertRecoRunWithItems } from '../reco/history.js';

const provider = new EastmoneyProvider();

export const apiRouter = Router();

function withTimeout<T>(p: Promise<T>, ms: number): Promise<T> {
  return new Promise<T>((resolve, reject) => {
    const t = setTimeout(() => reject(new Error('timeout')), ms);
    p.then((v) => {
      clearTimeout(t);
      resolve(v);
    }).catch((e) => {
      clearTimeout(t);
      reject(e);
    });
  });
}

type ThemeRule = {
  theme: string;
  weight: number;
  keywords: string[];
  sectorHints: string[];
};

const THEME_RULES: ThemeRule[] = [
  {
    theme: '利率/汇率/外部流动性',
    weight: 6,
    keywords: ['美联储', '降息', '加息', '通胀', '美元', '人民币', '汇率'],
    sectorHints: ['银行', '券商', '保险', '证券', '多元金融']
  },
  {
    theme: '半导体/算力/AI',
    weight: 6,
    keywords: ['芯片', '半导体', 'ai', '算力', '大模型'],
    sectorHints: ['半导体', '电子', '计算机', '通信', '软件']
  },
  {
    theme: 'AI应用/软件',
    weight: 5,
    keywords: ['ai', '大模型', '模型', 'agent', '应用'],
    sectorHints: ['软件', '计算机', '互联网', '传媒', '游戏']
  },
  {
    theme: '机器人/自动化',
    weight: 6,
    keywords: ['机器人', '人形机器人', '自动化', '机器视觉'],
    sectorHints: ['机器人', '自动化', '机械', '电气设备', '工业']
  },
  {
    theme: '低空经济/无人机',
    weight: 6,
    keywords: ['低空经济', '无人机', 'eVTOL', '飞行汽车', '通航'],
    sectorHints: ['航空', '航天', '军工', '无人机', '通用航空']
  },
  {
    theme: '军工/航天',
    weight: 6,
    keywords: ['军工', '航天', '卫星', '导弹', '国防'],
    sectorHints: ['军工', '航空', '航天', '卫星', '国防']
  },
  {
    theme: '出海/外需',
    weight: 4,
    keywords: ['出海', '海外', '欧洲', '美国', '东南亚'],
    sectorHints: ['汽车', '家电', '消费电子', '轻工', '纺织', '机械']
  },
  {
    theme: '并购重组/资产注入',
    weight: 5,
    keywords: ['并购', '重组', '资产注入', '借壳', '股权转让'],
    sectorHints: ['证券', '多元金融', '国企', '央企']
  },
  {
    theme: '国企改革/央企',
    weight: 4,
    keywords: ['国企改革', '央企', '混改', '国资委'],
    sectorHints: ['国企', '央企', '电力', '能源', '交通', '军工']
  },
  {
    theme: '数据要素/信创/网络安全',
    weight: 5,
    keywords: ['数据要素', '信创', '国产替代', '网络安全', '密码'],
    sectorHints: ['计算机', '软件', '安全', '通信', '电子']
  },
  {
    theme: '算力/服务器/光模块',
    weight: 6,
    keywords: ['算力', '服务器', '光模块', 'cpo', 'gpu'],
    sectorHints: ['通信', '光通信', '电子', '计算机', '数据中心']
  },
  {
    theme: '新能源链条',
    weight: 5,
    keywords: ['新能源', '光伏', '储能'],
    sectorHints: ['电力设备', '光伏', '风电', '锂电', '电池']
  },
  {
    theme: '电池/锂矿/材料',
    weight: 5,
    keywords: ['锂电', '电池', '锂矿', '钠电', '固态电池'],
    sectorHints: ['电池', '锂电', '有色', '材料', '化工']
  },
  {
    theme: '能源/大宗',
    weight: 5,
    keywords: ['原油', '油价', '黄金'],
    sectorHints: ['石油', '石化', '煤炭', '有色', '黄金']
  },
  {
    theme: '有色/稀土/小金属',
    weight: 5,
    keywords: ['稀土', '铜', '铝', '锂', '钴', '镍', '小金属'],
    sectorHints: ['有色', '稀土', '金属', '材料']
  },
  {
    theme: '汽车产业',
    weight: 4,
    keywords: ['汽车'],
    sectorHints: ['汽车', '零部件']
  },
  {
    theme: '医药健康',
    weight: 4,
    keywords: ['医药'],
    sectorHints: ['医药']
  },
  {
    theme: '创新药/医疗器械',
    weight: 4,
    keywords: ['创新药', '集采', '医疗器械'],
    sectorHints: ['医药', '医疗']
  },
  {
    theme: '地产链条',
    weight: 4,
    keywords: ['地产'],
    sectorHints: ['房地产', '建材', '家居', '建筑']
  },
  {
    theme: '外贸/关税/出口',
    weight: 4,
    keywords: ['关税', '出口'],
    sectorHints: ['航运', '港口', '家电', '纺织', '轻工', '汽车']
  },
  {
    theme: '消费复苏/文旅',
    weight: 3,
    keywords: ['消费', '旅游', '文旅', '免税'],
    sectorHints: ['消费', '旅游', '酒店', '免税', '餐饮']
  }
];

function computeThemeBoost(sector: string | undefined | null, newsKeywords: string[]): { boost: number; links: string[] } {
  const sec = String(sector || '').toLowerCase();
  const kws = (newsKeywords || []).map((x) => String(x || '')).filter(Boolean);
  if (!kws.length) return { boost: 0, links: [] };

  let boost = 0;
  const links: string[] = [];
  for (const rule of THEME_RULES) {
    const hitKw = rule.keywords.find((k) => kws.some((x) => x.toLowerCase().includes(k.toLowerCase()) || k.toLowerCase().includes(x.toLowerCase())));
    if (!hitKw) continue;
    if (!sec) continue;
    const hitSec = rule.sectorHints.find((h) => sec.includes(h.toLowerCase()));
    if (!hitSec) continue;
    boost += rule.weight;
    if (links.length < 5) links.push(`${hitKw} → ${rule.theme}（匹配：${hitSec}，+${rule.weight}）`);
  }
  return { boost, links };
}

function makeThemeFirstRecoFromQuote(code: string, quote: any, newsKeywords: string[]): any {
  const price = quote?.price != null ? Number(quote.price) : null;
  const entry = price != null ? Number((price * 0.99).toFixed(2)) : 0;
  const risk = price != null ? Number((price * 0.95).toFixed(2)) : 0;
  const exit = price != null ? Number((price * 1.06).toFixed(2)) : 0;

  const theme = computeThemeBoost(quote?.sector, newsKeywords);
  const signals: any[] = [];
  if (theme.boost > 0) {
    signals.push({ key: 'theme', label: '主题强度', value: `命中 +${theme.boost}`, bias: 'bullish' });
  } else {
    signals.push({ key: 'theme', label: '主题强度', value: '未命中', bias: 'neutral' });
  }

  const triggers: string[] = [];
  if (theme.links.length) triggers.push(`主题链路：${theme.links.join('；')}`);
  triggers.push('时事面：基于近24小时RSS热点关键词与板块匹配（免费源，可能延迟/缺失）');

  const risks: string[] = [];
  risks.push('时事关键词为弱结构化文本提取，存在误判/滞后风险');
  risks.push('建议结合走势与成交量确认，不建议重仓单一标的');

  const baseScore = theme.boost * 10;
  const pct = quote?.pct != null ? Number(quote.pct) : 0;
  const liq = quote?.amount != null ? Math.min(5, Math.log10(Math.max(1, Number(quote.amount))) - 8) : 0;
  const score = Number((baseScore + pct + liq).toFixed(2));

  return {
    code,
    ts: Date.now(),
    entry,
    exit,
    risk,
    score,
    summary: theme.boost > 0 ? `主题驱动 +${theme.boost}` : '主题驱动（弱）',
    signals,
    triggers,
    risks,
    newsKeywords: newsKeywords.slice(0, 5),
    reason: theme.links.length ? theme.links.join('；') : '时事面暂未命中强主题，已按流动性/涨跌幅兜底'
  };
}

function mapStoreQuote(row: any): any {
  if (!row) return null;
  return {
    code: row.code,
    ts: row.ts,
    price: row.price ?? null,
    open: row.open ?? null,
    high: row.high ?? null,
    low: row.low ?? null,
    prevClose: row.prev_close ?? null,
    volume: row.volume ?? null,
    amount: row.amount ?? null,
    pct: row.pct ?? null,
    sector: row.sector ?? null
  };
}

function extractHotNewsKeywords(max = 5): string[] {
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

function makeFallbackRecoFromPrice(code: string, price: number, newsKeywords: string[]): any {
  const entry = Number((price * 0.99).toFixed(2));
  const risk = Number((price * 0.95).toFixed(2));
  const exit = Number((price * 1.06).toFixed(2));
  const parts: string[] = [];
  parts.push('技术面：K线不足或免费源波动，已降级为基于现价的示例推荐');
  if (newsKeywords.length) parts.push(`时事面：近期高频主题 ${newsKeywords.slice(0, 5).join(' / ')}（仅供参考）`);
  parts.push('风控：建议严格设置止损线，避免单笔亏损扩大');
  return {
    code,
    ts: Date.now(),
    entry,
    exit,
    risk,
    score: computeRecoScore({
      signals: [{ key: 'degraded', label: '模式', value: 'K线不足，已降级', bias: 'neutral' }],
      newsKeywords: newsKeywords.slice(0, 5)
    }) - 10,
    summary: '数据不足/降级',
    signals: [
      { key: 'degraded', label: '模式', value: 'K线不足，已降级', bias: 'neutral' }
    ],
    triggers: ['等待日K补齐后再观察技术信号'],
    risks: ['免费数据源波动导致K线不足，建议降低交易频率'],
    newsKeywords: newsKeywords.slice(0, 5),
    reason: parts.join('；')
  };
}

apiRouter.get('/health', (_req: Request, res: Response) => {
  res.json({ ok: true, ts: Date.now() });
});

apiRouter.get('/hs300', (_req: Request, res: Response) => {
  const rows = store.getSymbolsByIndexTag('HS300').map((x) => ({ code: x.code, name: x.name, market: x.market }));
  res.json({ data: rows });
});

apiRouter.get('/indices', (_req: Request, res: Response) => {
  res.json({ data: INDICES });
});

apiRouter.get('/index/quotes', async (_req: Request, res: Response) => {
  const out: Array<{ symbol: string; name: string; quote: any }> = [];
  for (const idx of INDICES) {
    try {
      const quote = await getIndexQuote(idx.secid, idx.symbol);
      if (quote) out.push({ symbol: idx.symbol, name: idx.name, quote });
    } catch {
      // ignore
    }
  }
  res.json({ data: out });
});

apiRouter.get('/index/quote', async (req: Request, res: Response) => {
  const q = z.object({ symbol: z.string().min(1) }).safeParse(req.query);
  if (!q.success) return res.status(400).json({ error: 'bad_request' });

  const idx = getIndexBySymbol(q.data.symbol);
  if (!idx) return res.status(404).json({ error: 'not_found' });

  try {
    const quote = await getIndexQuote(idx.secid, idx.symbol);
    res.json({ data: { ...quote, name: idx.name } });
  } catch {
    res.status(502).json({ error: 'provider_error' });
  }
});

apiRouter.get('/index/kline', async (req: Request, res: Response) => {
  const q = z
    .object({
      symbol: z.string().min(1),
      limit: z.coerce.number().int().min(20).max(500).default(200)
    })
    .safeParse(req.query);
  if (!q.success) return res.status(400).json({ error: 'bad_request' });

  const idx = getIndexBySymbol(q.data.symbol);
  if (!idx) return res.status(404).json({ error: 'not_found' });

  try {
    const cached = store.getKlines(idx.symbol, q.data.limit);
    const cachedLenBefore = cached.length;
    let refreshed = false;

    if (cached.length < Math.min(60, q.data.limit)) {
      try {
        const fresh = await getIndexKlines(idx.secid, idx.symbol, q.data.limit);
        if (fresh.length) {
          store.upsertKlines(
            idx.symbol,
            fresh.map((k) => ({
              code: idx.symbol,
              ts: k.ts,
              open: k.open,
              high: k.high,
              low: k.low,
              close: k.close,
              volume: k.volume,
              amount: k.amount
            }))
          );
          refreshed = true;
        }
      } catch {
        // ignore
      }
    }

    const klines = store.getKlines(idx.symbol, q.data.limit);
    const cachedLenAfter = klines.length;
    const ts = klines.map((k) => k.ts);
    const close = klines.map((k) => k.close);
    const macd = calcMACD(ts, close);
    res.json({ data: { klines, macd, name: idx.name, meta: { cachedLenBefore, cachedLenAfter, refreshed } } });
  } catch {
    res.status(502).json({ error: 'provider_error' });
  }
});

apiRouter.get('/quote', async (req: Request, res: Response) => {
  const q = z.object({ code: z.string().min(1) }).safeParse(req.query);
  if (!q.success) return res.status(400).json({ error: 'bad_request' });

  try {
    const quote = await provider.getQuote(q.data.code);
    res.json({ data: quote });
  } catch {
    res.status(502).json({ error: 'provider_error' });
  }
});

apiRouter.post('/news/refresh', async (_req: Request, res: Response) => {
  try {
    const n = await refreshNews();
    res.json({ ok: true, inserted: n });
  } catch {
    res.status(502).json({ error: 'provider_error' });
  }
});

apiRouter.get('/kline', async (req: Request, res: Response) => {
  const q = z
    .object({
      code: z.string().min(1),
      limit: z.coerce.number().int().min(20).max(500).default(200)
    })
    .safeParse(req.query);
  if (!q.success) return res.status(400).json({ error: 'bad_request' });

  // 优先返回缓存，避免接口阻塞导致前端图表长期 loading；缓存不足则后台补拉
  const rows = store.getKlines(q.data.code, q.data.limit);
  if (rows.length < Math.min(60, q.data.limit)) {
    void syncKlines(provider, q.data.code, q.data.limit).catch(() => undefined);
  }

  const rows2 = rows.length ? rows : store.getKlines(q.data.code, q.data.limit);

  const ts = rows2.map((r: any) => r.ts as number);
  const close = rows2.map((r: any) => r.close as number);
  const macd = calcMACD(ts, close);

  res.json({
    data: {
      klines: rows2,
      macd
    }
  });
});

apiRouter.get('/reco', async (req: Request, res: Response) => {
  const q = z.object({ code: z.string().min(1), limit: z.coerce.number().int().min(60).max(500).default(200) }).safeParse(req.query);
  if (!q.success) return res.status(400).json({ error: 'bad_request' });

  // 优先用缓存计算推荐，避免等待免费源导致接口变慢；缓存不足则后台补拉并返回降级推荐
  const cached = store.getKlines(q.data.code, q.data.limit);
  if (cached.length < Math.min(60, q.data.limit)) {
    void syncKlines(provider, q.data.code, q.data.limit).catch(() => undefined);
  }

  const rows = cached.map((x) => ({ ts: x.ts, close: x.close })) as Array<{ ts: number; close: number }>;
  const newsKeywords = extractHotNewsKeywords(5);
  let reco = makeReco(q.data.code, rows, newsKeywords);
  if (!reco) {
    try {
      const qt = await provider.getQuote(q.data.code);
      if (qt?.price != null) reco = makeFallbackRecoFromPrice(q.data.code, qt.price, newsKeywords);
    } catch {
      // ignore
    }
  }
  res.json({ data: reco });
});

apiRouter.get('/reco/top', async (req: Request, res: Response) => {
  const q = z
    .object({
      limit: z.coerce.number().int().min(1).max(50).default(20)
    })
    .safeParse(req.query);
  if (!q.success) return res.status(400).json({ error: 'bad_request' });

  // MySQL 缓存优先：若缓存不过期则秒回（默认 30 分钟，可用环境变量覆盖）
  const cacheKey = `top:${q.data.limit}`;
  const cacheTtlMs = Number(process.env.RECO_TOP_CACHE_TTL_MS ?? 30 * 60_000);
  const cached = await getRecoTopCache(cacheKey);
  if (cached && cached.payload && Date.now() - cached.ts <= cacheTtlMs) {
    return res.json({
      data: cached.payload.data ?? cached.payload,
      meta: {
        ...(cached.payload.meta ?? {}),
        cache: 'mysql',
        cacheAgeMs: Date.now() - cached.ts
      }
    });
  }

  const seed: Array<{ code: string; name: string; market: string }> = [
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
  const uniq = new Map<string, { code: string; name: string; market: string }>();

  for (const s of seed) uniq.set(s.code, s);
  for (const s of topa) {
    const mktPrefix = s.market === 'SH' ? 'SH' : 'SZ';
    const code = `${mktPrefix}${s.code}`;
    if (!uniq.has(code)) uniq.set(code, { code, name: s.name, market: s.market });
  }

  const codes = Array.from(uniq.values()).map((x) => x.code);
  const newsKeywords = extractHotNewsKeywords(5);

  const t0 = Date.now();
  const want = q.data.limit;
  const timeBudgetMs = 10_000;

  const candidateCodes = codes.slice(0, 140);
  const concurrency = 18;
  const quoteTimeoutMs = 1800;

  function quickFallback() {
    const out: Array<{ code: string; name: string; market: string; quote: any; reco: any }> = [];
    const picked = new Set<string>();
    for (const code of candidateCodes) {
      if (out.length >= want) break;
      if (picked.has(code)) continue;
      const meta = uniq.get(code);
      const cached = mapStoreQuote(store.getQuote(code));
      const reco = makeThemeFirstRecoFromQuote(code, cached || {}, newsKeywords);
      out.push({ code, name: meta?.name ?? '', market: meta?.market ?? '', quote: cached, reco });
      picked.add(code);
    }
    for (const s of seed) {
      if (out.length >= want) break;
      if (picked.has(s.code)) continue;
      const reco = makeThemeFirstRecoFromQuote(s.code, {}, newsKeywords);
      out.push({ code: s.code, name: s.name, market: s.market, quote: null, reco });
      picked.add(s.code);
    }
    return out;
  }

  async function buildTop() {
    const deadline = t0 + timeBudgetMs;

    const quotes = new Map<string, any>();
    let fetched = 0;

    // 后台预热：把 sector 一起写入 store，提升后续主题链路命中（不阻塞本次响应）
    void syncQuotes(provider, candidateCodes.slice(0, 120), 20).catch(() => undefined);

    for (let i = 0; i < candidateCodes.length; i += concurrency) {
      if (Date.now() >= deadline) break;
      const batch = candidateCodes.slice(i, i + concurrency);
      const remaining = Math.max(0, deadline - Date.now());

      const results = await withTimeout(
        Promise.all(
          batch.map(async (code) => {
            const cached = mapStoreQuote(store.getQuote(code));
            // 缓存有价格就先用；如果 sector 缺失，尝试用短超时补一次 provider 报价
            if (cached && cached.price != null) {
              if (!cached.sector && remaining > 600) {
                try {
                  const qt = await withTimeout(provider.getQuote(code), Math.min(quoteTimeoutMs, remaining));
                  return { code, quote: qt ?? cached, from: 'provider' as const };
                } catch {
                  return { code, quote: cached, from: 'store' as const };
                }
              }
              return { code, quote: cached, from: 'store' as const };
            }
            try {
              const qt = await withTimeout(provider.getQuote(code), Math.min(quoteTimeoutMs, remaining));
              return { code, quote: qt, from: 'provider' as const };
            } catch {
              return { code, quote: null, from: 'timeout' as const };
            }
          })
        ),
        Math.min(2500, Math.max(600, remaining))
      ).catch(() => [] as any);

      for (const r of results) {
        if (r.quote) quotes.set(r.code, r.quote);
        fetched++;
      }
    }

    const scored: Array<{ code: string; name: string; market: string; quote: any; reco: any; score: number }> = [];
    for (const code of candidateCodes) {
      const qx = quotes.get(code) ?? null;
      if (!qx || qx.price == null) continue;
      const k0 = store.getKlines(code, 160);
      const k = (k0 || []).map((x: any) => ({ ts: Number(x.ts), close: Number(x.close) })).filter((x: any) => Number.isFinite(x.ts) && Number.isFinite(x.close));
      const reco = makeReco(code, k, newsKeywords) ?? makeThemeFirstRecoFromQuote(code, qx, newsKeywords);
      const meta = uniq.get(code);
      scored.push({ code, name: meta?.name ?? '', market: meta?.market ?? '', quote: qx, reco, score: Number(reco.score ?? 0) });
    }

    scored.sort((a, b) => b.score - a.score);
    const out: Array<{ code: string; name: string; market: string; quote: any; reco: any }> = scored.slice(0, want).map(({ score: _s, ...rest }) => rest);

    if (out.length < want) {
      const picked = new Set(out.map((x) => x.code));
      for (const code of candidateCodes) {
        if (out.length >= want) break;
        if (picked.has(code)) continue;
        const qx = quotes.get(code) ?? null;
        if (!qx || qx.price == null) continue;
        const k0 = store.getKlines(code, 160);
        const k = (k0 || []).map((x: any) => ({ ts: Number(x.ts), close: Number(x.close) })).filter((x: any) => Number.isFinite(x.ts) && Number.isFinite(x.close));
        const reco = makeReco(code, k, newsKeywords) ?? makeThemeFirstRecoFromQuote(code, qx, newsKeywords);
        const meta = uniq.get(code);
        out.push({ code, name: meta?.name ?? '', market: meta?.market ?? '', quote: qx, reco });
        picked.add(code);
      }
    }

    if (out.length < want) {
      const picked = new Set(out.map((x) => x.code));
      for (const s of seed) {
        if (out.length >= want) break;
        if (picked.has(s.code)) continue;
        const k0 = store.getKlines(s.code, 160);
        const k = (k0 || []).map((x: any) => ({ ts: Number(x.ts), close: Number(x.close) })).filter((x: any) => Number.isFinite(x.ts) && Number.isFinite(x.close));
        const reco = makeReco(s.code, k, newsKeywords) ?? makeThemeFirstRecoFromQuote(s.code, {}, newsKeywords);
        out.push({ code: s.code, name: s.name, market: s.market, quote: null, reco });
        picked.add(s.code);
      }
    }

    return { out, fetched, candidates: candidateCodes.length };
  }

  try {
    const r = await withTimeout(buildTop(), timeBudgetMs);
    const payload = {
      data: r.out,
      meta: {
        keywords: newsKeywords,
        tookMs: Date.now() - t0,
        timeBudgetMs,
        candidates: r.candidates,
        fetched: r.fetched
      }
    };
    void setRecoTopCache(cacheKey, Date.now(), payload).catch(() => undefined);
    void insertRecoRunWithItems(
      {
        runType: 'top',
        queryText: null,
        keywords: newsKeywords,
        limit: want,
        tookMs: Number(payload.meta.tookMs ?? 0)
      },
      (r.out || []).map((it: any, idx: number) => ({
        rank: idx + 1,
        code: String(it.code ?? ''),
        name: String(it.name ?? ''),
        market: String(it.market ?? ''),
        sector: it?.quote?.sector ?? null,
        ts: it?.quote?.ts ?? null,
        price: it?.quote?.price ?? null,
        pct: it?.quote?.pct ?? null,
        score: it?.reco?.score ?? null,
        entry: it?.reco?.entry ?? null,
        exit: it?.reco?.exit ?? null,
        risk: it?.reco?.risk ?? null,
        summary: it?.reco?.summary ?? null,
        reason: it?.reco?.reason ?? null,
        signals: it?.reco?.signals ?? null,
        triggers: it?.reco?.triggers ?? null,
        risks: it?.reco?.risks ?? null,
        newsKeywords: it?.reco?.newsKeywords ?? newsKeywords
      }))
    ).catch(() => undefined);
    res.json(payload);
  } catch {
    const out = quickFallback();
    const payload = {
      data: out,
      meta: {
        keywords: newsKeywords,
        tookMs: Date.now() - t0,
        timeBudgetMs,
        candidates: candidateCodes.length,
        fetched: 0,
        timeout: true
      }
    };
    void setRecoTopCache(cacheKey, Date.now(), payload).catch(() => undefined);
    void insertRecoRunWithItems(
      {
        runType: 'top',
        queryText: null,
        keywords: newsKeywords,
        limit: want,
        tookMs: Number(payload.meta.tookMs ?? 0)
      },
      (out || []).map((it: any, idx: number) => ({
        rank: idx + 1,
        code: String(it.code ?? ''),
        name: String(it.name ?? ''),
        market: String(it.market ?? ''),
        sector: it?.quote?.sector ?? null,
        ts: it?.quote?.ts ?? null,
        price: it?.quote?.price ?? null,
        pct: it?.quote?.pct ?? null,
        score: it?.reco?.score ?? null,
        entry: it?.reco?.entry ?? null,
        exit: it?.reco?.exit ?? null,
        risk: it?.reco?.risk ?? null,
        summary: it?.reco?.summary ?? null,
        reason: it?.reco?.reason ?? null,
        signals: it?.reco?.signals ?? null,
        triggers: it?.reco?.triggers ?? null,
        risks: it?.reco?.risks ?? null,
        newsKeywords: it?.reco?.newsKeywords ?? newsKeywords
      }))
    ).catch(() => undefined);
    res.json(payload);
  }
});

apiRouter.post('/reco/keyword', async (req: Request, res: Response) => {
  const q = z
    .object({
      q: z.string().min(1).max(40),
      limit: z.coerce.number().int().min(1).max(50).default(20)
    })
    .safeParse(req.body);
  if (!q.success) return res.status(400).json({ error: 'bad_request' });

  const userQ = q.data.q.trim();
  const want = q.data.limit;
  const t0 = Date.now();

  const seed: Array<{ code: string; name: string; market: string }> = [
    { code: 'SH600519', name: '贵州茅台', market: 'SH' },
    { code: 'SH601318', name: '中国平安', market: 'SH' },
    { code: 'SH600036', name: '招商银行', market: 'SH' },
    { code: 'SZ000001', name: '平安银行', market: 'SZ' },
    { code: 'SZ300750', name: '宁德时代', market: 'SZ' }
  ];

  const topa = store.getSymbolsByIndexTag('TOPA');
  const uniq = new Map<string, { code: string; name: string; market: string }>();
  for (const s of seed) uniq.set(s.code, s);
  for (const s of topa) {
    const mktPrefix = s.market === 'SH' ? 'SH' : 'SZ';
    const code = `${mktPrefix}${s.code}`;
    if (!uniq.has(code)) uniq.set(code, { code, name: s.name, market: s.market });
  }

  const codes = Array.from(uniq.values()).map((x) => x.code);
  const baseHot = extractHotNewsKeywords(5);
  const newsKeywords = Array.from(new Set([userQ, ...baseHot].filter(Boolean))).slice(0, 8);

  const timeBudgetMs = Number(process.env.RECO_KEYWORD_TIME_BUDGET_MS ?? 12_000);
  const concurrency = 18;
  const quoteTimeoutMs = 2200;
  const candidateCodes = codes.slice(0, 160);

  async function build() {
    const deadline = t0 + timeBudgetMs;
    const quotes = new Map<string, any>();
    let fetched = 0;

    void syncQuotes(provider, candidateCodes.slice(0, 120), 20).catch(() => undefined);

    for (let i = 0; i < candidateCodes.length; i += concurrency) {
      if (Date.now() >= deadline) break;
      const batch = candidateCodes.slice(i, i + concurrency);
      const remaining = Math.max(0, deadline - Date.now());

      const results = await withTimeout(
        Promise.all(
          batch.map(async (code) => {
            const cached = mapStoreQuote(store.getQuote(code));
            if (cached && cached.price != null) {
              if (!cached.sector && remaining > 700) {
                try {
                  const qt = await withTimeout(provider.getQuote(code), Math.min(quoteTimeoutMs, remaining));
                  return { code, quote: qt ?? cached };
                } catch {
                  return { code, quote: cached };
                }
              }
              return { code, quote: cached };
            }
            try {
              const qt = await withTimeout(provider.getQuote(code), Math.min(quoteTimeoutMs, remaining));
              return { code, quote: qt };
            } catch {
              return { code, quote: null };
            }
          })
        ),
        Math.min(2800, Math.max(600, remaining))
      ).catch(() => [] as any);

      for (const r of results) {
        if (r.quote) quotes.set(r.code, r.quote);
        fetched++;
      }
    }

    const scored: Array<{ code: string; name: string; market: string; quote: any; reco: any; score: number }> = [];
    for (const code of candidateCodes) {
      const qx = quotes.get(code) ?? null;
      if (!qx || qx.price == null) continue;
      const k0 = store.getKlines(code, 160);
      const k = (k0 || []).map((x: any) => ({ ts: Number(x.ts), close: Number(x.close) })).filter((x: any) => Number.isFinite(x.ts) && Number.isFinite(x.close));
      const reco = makeReco(code, k, newsKeywords) ?? makeThemeFirstRecoFromQuote(code, qx, newsKeywords);
      const meta = uniq.get(code);
      scored.push({ code, name: meta?.name ?? '', market: meta?.market ?? '', quote: qx, reco, score: Number(reco.score ?? 0) });
    }
    scored.sort((a, b) => b.score - a.score);
    const out: Array<{ code: string; name: string; market: string; quote: any; reco: any }> = scored.slice(0, want).map(({ score: _s, ...rest }) => rest);
    return { out, fetched, candidates: candidateCodes.length };
  }

  try {
    const r = await withTimeout(build(), timeBudgetMs);
    const payload = {
      data: r.out,
      meta: {
        q: userQ,
        keywords: newsKeywords,
        tookMs: Date.now() - t0,
        timeBudgetMs,
        candidates: r.candidates,
        fetched: r.fetched
      }
    };

    void insertRecoRunWithItems(
      {
        runType: 'keyword',
        queryText: userQ,
        keywords: newsKeywords,
        limit: want,
        tookMs: Number(payload.meta.tookMs ?? 0)
      },
      (r.out || []).map((it: any, idx: number) => ({
        rank: idx + 1,
        code: String(it.code ?? ''),
        name: String(it.name ?? ''),
        market: String(it.market ?? ''),
        sector: it?.quote?.sector ?? null,
        ts: it?.quote?.ts ?? null,
        price: it?.quote?.price ?? null,
        pct: it?.quote?.pct ?? null,
        score: it?.reco?.score ?? null,
        entry: it?.reco?.entry ?? null,
        exit: it?.reco?.exit ?? null,
        risk: it?.reco?.risk ?? null,
        summary: it?.reco?.summary ?? null,
        reason: it?.reco?.reason ?? null,
        signals: it?.reco?.signals ?? null,
        triggers: it?.reco?.triggers ?? null,
        risks: it?.reco?.risks ?? null,
        newsKeywords: it?.reco?.newsKeywords ?? newsKeywords
      }))
    ).catch(() => undefined);

    res.json(payload);
  } catch {
    res.status(504).json({ error: 'timeout' });
  }
});

apiRouter.post('/sync/quotes', async (req: Request, res: Response) => {
  const body = z
    .object({
      codes: z.array(z.string().min(1)).min(1).max(300)
    })
    .safeParse(req.body);
  if (!body.success) return res.status(400).json({ error: 'bad_request' });

  try {
    const n = await syncQuotes(provider, body.data.codes, 20);
    res.json({ ok: true, inserted: n });
  } catch {
    res.status(502).json({ error: 'provider_error' });
  }
});
