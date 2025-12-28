import axios from 'axios';
import { MarketDataProvider, MarketSymbol, Quote, Kline } from './types.js';

function normalizeAStockCode(code: string): { secid: string; code: string } | null {
  // Accept: 600000, 000001, 300750, SH600000, SZ000001
  const raw = code.trim().toUpperCase();
  if (/^(SH|SZ)\d{6}$/.test(raw)) {
    const mkt = raw.slice(0, 2);
    const num = raw.slice(2);
    const secid = `${mkt === 'SH' ? '1' : '0'}.${num}`;
    return { secid, code: raw };
  }
  if (/^\d{6}$/.test(raw)) {
    const mkt = raw.startsWith('6') ? 'SH' : 'SZ';
    const secid = `${mkt === 'SH' ? '1' : '0'}.${raw}`;
    return { secid, code: `${mkt}${raw}` };
  }
  return null;
}

function normPrice(v: any): number | null {
  if (typeof v !== 'number' || !Number.isFinite(v)) return null;
  // 东方财富部分接口/环境会返回“分”为单位的整数（例如 141413 -> 1414.13）；
  // 也可能直接返回带小数的真实值（例如 3963.68）。
  // 这里用阈值做自适应缩放，避免把 3963.68 错误除以 100。
  return Math.abs(v) > 100000 ? v / 100 : v;
}

function normPct(v: any): number | null {
  if (typeof v !== 'number' || !Number.isFinite(v)) return null;
  // 有的环境返回 0.32 表示 0.32%，也可能返回 32 表示 32%。
  return Math.abs(v) > 100 ? v / 100 : v;
}

const EASTMONEY_UT = 'fa5fd1943c7b386f172d6893dbfba10b';

export class EastmoneyProvider implements MarketDataProvider {
  name = 'eastmoney_free';

  async getHS300Symbols(): Promise<MarketSymbol[]> {
    // 说明：免费接口可能会变化/限流；失败时上层应降级到本地缓存。
    // 这里使用东方财富“clist”接口，尝试拉取沪深300成分（字段/参数可能随时间变化）。
    const url = 'https://push2.eastmoney.com/api/qt/clist/get';

    const resp = await axios.get(url, {
      timeout: 10000,
      params: {
        pn: 1,
        pz: 300,
        po: 1,
        np: 1,
        fltt: 2,
        invt: 2,
        fid: 'f3',
        // fs 含义：板块/指数成分筛选。不同环境可能要调整。
        // 这里使用“沪深300”成分的常见筛选写法之一：b:BK0500 不稳定，因此上层要有降级。
        fs: 'b:BK0500',
        fields: 'f12,f14,f13'
      }
    });

    const diff: any[] | undefined = resp.data?.data?.diff;
    if (!Array.isArray(diff)) return [];

    return diff
      .map((x) => {
        const num = String(x.f12 ?? '').trim();
        const name = String(x.f14 ?? '').trim();
        const mkt = Number(x.f13);
        if (!/^[0-9]{6}$/.test(num) || !name) return null;
        const market: 'SH' | 'SZ' = mkt === 1 ? 'SH' : 'SZ';
        return { code: num, name, market } as MarketSymbol;
      })
      .filter((x): x is MarketSymbol => Boolean(x));
  }

  async getTopAShareSymbols(limit: number): Promise<MarketSymbol[]> {
    const lmt = Math.max(50, Math.min(2000, Math.floor(limit || 300)));
    const url = 'https://push2.eastmoney.com/api/qt/clist/get';

    // 说明：这里使用“沪深A股”常见筛选写法之一，按成交额/涨跌等字段排序。
    // 免费接口可能随时变更，因此上层必须允许为空并走缓存/降级。
    const resp = await axios.get(url, {
      timeout: 12000,
      params: {
        pn: 1,
        pz: lmt,
        po: 1,
        np: 1,
        fltt: 2,
        invt: 2,
        fid: 'f6',
        fs: 'm:0+t:6,m:0+t:13,m:1+t:2,m:1+t:23',
        fields: 'f12,f14,f13'
      }
    });

    const diff: any[] | undefined = resp.data?.data?.diff;
    if (!Array.isArray(diff)) return [];

    return diff
      .map((x) => {
        const num = String(x.f12 ?? '').trim();
        const name = String(x.f14 ?? '').trim();
        const mkt = Number(x.f13);
        if (!/^[0-9]{6}$/.test(num) || !name) return null;
        const market: 'SH' | 'SZ' = mkt === 1 ? 'SH' : 'SZ';
        return { code: num, name, market } as MarketSymbol;
      })
      .filter((x): x is MarketSymbol => Boolean(x));
  }

  async getQuote(code: string): Promise<Quote | null> {
    const norm = normalizeAStockCode(code);
    if (!norm) return null;

    const url = 'https://push2.eastmoney.com/api/qt/stock/get';
    const resp = await axios.get(url, {
      timeout: 10000,
      params: {
        secid: norm.secid,
        fltt: 2,
        invt: 2,
        fields: 'f43,f44,f45,f46,f47,f48,f49,f50,f51,f52,f60,f127,f128,f170'
      }
    });

    const d = resp.data?.data;
    if (!d) return null;

    const now = Date.now();

    const price = normPrice(d.f43);
    const open = normPrice(d.f46);
    const high = normPrice(d.f44);
    const low = normPrice(d.f45);
    const prevClose = normPrice(d.f60);
    const pct = normPct(d.f170);
    const industry = typeof d.f127 === 'string' ? d.f127.trim() : '';
    const region = typeof d.f128 === 'string' ? d.f128.trim() : '';
    const sector = (industry || region) ? [industry, region].filter(Boolean).join(' / ') : null;

    const volume = typeof d.f47 === 'number' ? d.f47 : null;
    const amount = typeof d.f48 === 'number' ? d.f48 : null;

    return {
      code: norm.code,
      ts: now,
      price,
      open,
      high,
      low,
      prevClose,
      volume,
      amount,
      pct,
      sector
    };
  }

  async getKlines(code: string, limit: number): Promise<Kline[]> {
    const norm = normalizeAStockCode(code);
    if (!norm) return [];

    // K线接口同样可能变化，这里优先保证系统结构可运行。
    const url = 'https://push2his.eastmoney.com/api/qt/stock/kline/get';
    const resp = await axios.get(url, {
      timeout: 12000,
      params: {
        secid: norm.secid,
        ut: EASTMONEY_UT,
        klt: 101, // 101=日线，1=1分钟等（视接口定义）
        fqt: 1,
        beg: 0,
        end: 20500101,
        lmt: Math.max(10, Math.min(1000, limit)),
        fields1: 'f1,f2,f3,f4,f5,f6',
        fields2: 'f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61'
      }
    });

    const kl: string[] | undefined = resp.data?.data?.klines;
    if (!Array.isArray(kl)) return [];

    return kl
      .map((line) => {
        // "2025-12-27,开,收,高,低,量,额,..."
        const parts = String(line).split(',');
        if (parts.length < 7) return null;
        const [date, open, close, high, low, vol, amt] = parts;
        const ts = new Date(date).getTime();
        if (!Number.isFinite(ts)) return null;
        const o = Number(open);
        const c = Number(close);
        const h = Number(high);
        const l = Number(low);
        if (![o, c, h, l].every((v) => Number.isFinite(v))) return null;
        return {
          code: norm.code,
          ts,
          open: o,
          close: c,
          high: h,
          low: l,
          volume: Number.isFinite(Number(vol)) ? Number(vol) : null,
          amount: Number.isFinite(Number(amt)) ? Number(amt) : null
        } as Kline;
      })
      .filter((x): x is Kline => Boolean(x))
      .sort((a, b) => a.ts - b.ts);
  }
}
