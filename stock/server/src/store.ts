import { mysqlHealthy } from './mysql.js';
import {
  fetchAllQuotes,
  fetchAllSymbols,
  fetchKlinesByCode,
  fetchNews,
  replaceNews as mysqlReplaceNews,
  replaceQuotes as mysqlReplaceQuotes,
  upsertKlines as mysqlUpsertKlines,
  upsertSymbols as mysqlUpsertSymbols
} from './mysqlStore.js';

export type SymbolRow = {
  code: string;
  name: string;
  market: string;
  index_tag: string | null;
};

export type QuoteRow = {
  code: string;
  name_cn?: string | null;
  ts: number;
  price: number | null;
  open: number | null;
  high: number | null;
  low: number | null;
  prev_close: number | null;
  volume: number | null;
  amount: number | null;
  pct: number | null;
  sector?: string | null;
  inserted_at?: number;
};

export type KlineRow = {
  code: string;
  ts: number;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number | null;
  amount: number | null;
};

export type NewsItem = {
  id: string;
  ts: number;
  title: string;
  url: string;
  source: string;
  summary: string | null;
};

type StoreData = {
  symbols: SymbolRow[];
  quotes: QuoteRow[];
  klines: KlineRow[];
  news: NewsItem[];
};

let mem: StoreData = { symbols: [], quotes: [], klines: [], news: [] };
let mysqlReady = false;

export async function initStore(): Promise<void> {
  try {
    mysqlReady = await mysqlHealthy();
  } catch {
    mysqlReady = false;
  }

  if (!mysqlReady) return;

  try {
    const [symbols, quotes, news] = await Promise.all([fetchAllSymbols(), fetchAllQuotes(8000), fetchNews(500)]);
    if (symbols.length) mem.symbols = symbols;
    if (quotes.length) mem.quotes = quotes;
    if (news.length) mem.news = news;
  } catch {
    // ignore
  }
}

export const store = {
  getSymbolsByIndexTag(indexTag: string) {
    return mem.symbols.filter((x) => x.index_tag === indexTag).sort((a, b) => a.code.localeCompare(b.code));
  },

  getQuote(code: string) {
    const row = mem.quotes.find((x) => x.code === code) ?? null;
    return row;
  },

  upsertSymbols(indexTag: string, rows: Array<{ code: string; name: string; market: string }>) {
    const map = new Map(mem.symbols.map((x) => [`${x.index_tag ?? ''}:${x.code}`, x] as const));
    for (const r of rows) {
      map.set(`${indexTag}:${r.code}`, { code: r.code, name: r.name, market: r.market, index_tag: indexTag });
    }
    mem.symbols = Array.from(map.values());
    if (mysqlReady) void mysqlUpsertSymbols(indexTag, rows).catch(() => undefined);
  },

  countSymbols(indexTag: string) {
    return mem.symbols.filter((x) => x.index_tag === indexTag).length;
  },

  replaceQuotes(rows: QuoteRow[]) {
    // quotes 只保留每个 code 最新 1 条
    const map = new Map<string, QuoteRow>();
    for (const q of mem.quotes) map.set(q.code, q);
    for (const q of rows) map.set(q.code, q);
    mem.quotes = Array.from(map.values());
    if (mysqlReady) void mysqlReplaceQuotes(rows).catch(() => undefined);
  },

  upsertKlines(code: string, rows: KlineRow[]) {
    const keep = mem.klines.filter((x) => x.code !== code);
    // 同一 code 内按 ts 去重
    const map = new Map<number, KlineRow>();
    for (const r of rows) map.set(r.ts, r);
    const merged = Array.from(map.values()).sort((a, b) => a.ts - b.ts);
    mem.klines = keep.concat(merged);
    if (mysqlReady) void mysqlUpsertKlines(code, rows).catch(() => undefined);
  },

  getKlines(code: string, limit: number) {
    let rows = mem.klines.filter((x) => x.code === code).sort((a, b) => a.ts - b.ts);
    const lmt = Math.max(1, Math.min(1000, limit));
    const sliced = rows.slice(Math.max(0, rows.length - lmt));
    if (sliced.length >= Math.min(60, lmt)) return sliced;

    if (mysqlReady) {
      void fetchKlinesByCode(code, lmt)
        .then((fresh) => {
          if (!fresh.length) return;
          const keep = mem.klines.filter((x) => x.code !== code);
          mem.klines = keep.concat(fresh);
        })
        .catch(() => undefined);
    }
    return sliced;
  },

  getNews(limit: number) {
    return mem.news
      .slice()
      .sort((a, b) => b.ts - a.ts)
      .slice(0, Math.max(1, Math.min(200, limit)));
  },

  replaceNews(items: NewsItem[]) {
    const map = new Map<string, NewsItem>();
    for (const it of mem.news) map.set(it.id, it);
    const before = map.size;
    for (const it of items) map.set(it.id, it);
    const merged = Array.from(map.values()).sort((a, b) => b.ts - a.ts).slice(0, 500);
    mem.news = merged;
    const after = new Set(merged.map((x) => x.id)).size;
    if (mysqlReady) void mysqlReplaceNews(items).catch(() => undefined);
    return Math.max(0, after - before);
  }
};
