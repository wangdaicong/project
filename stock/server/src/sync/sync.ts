import { store } from '../store.js';
import { MarketDataProvider } from '../providers/types.js';

export async function refreshHS300(provider: MarketDataProvider): Promise<number> {
  const symbols = await provider.getHS300Symbols();
  if (!symbols.length) return 0;

  store.upsertSymbols(
    'HS300',
    symbols.map((s) => ({ code: s.code, name: s.name, market: s.market }))
  );
  return symbols.length;
}

export async function refreshTopAShares(provider: MarketDataProvider, limit = 800): Promise<number> {
  const symbols = await provider.getTopAShareSymbols(limit);
  if (!symbols.length) return 0;

  store.upsertSymbols(
    'TOPA',
    symbols.map((s) => ({ code: s.code, name: s.name, market: s.market }))
  );
  return symbols.length;
}

export async function syncQuotes(provider: MarketDataProvider, codes: string[], batchSize = 20): Promise<number> {
  const toWrite: Array<{
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
  }> = [];

  const nameMap = new Map<string, string>();
  for (const s of store.getSymbolsByIndexTag('TOPA')) {
    const key = `${s.market === 'SH' ? 'SH' : 'SZ'}${s.code}`;
    if (!nameMap.has(key) && s.name) nameMap.set(key, s.name);
  }

  let count = 0;
  for (let i = 0; i < codes.length; i += batchSize) {
    const batch = codes.slice(i, i + batchSize);
    const results = await Promise.all(
      batch.map(async (c) => {
        try {
          return await provider.getQuote(c);
        } catch {
          return null;
        }
      })
    );

    for (const q of results) {
      if (!q) continue;
      toWrite.push({
        code: q.code,
        name_cn: nameMap.get(q.code) ?? null,
        ts: q.ts,
        price: q.price,
        open: q.open,
        high: q.high,
        low: q.low,
        prev_close: q.prevClose,
        volume: q.volume,
        amount: q.amount,
        pct: q.pct,
        sector: q.sector ?? null
      });
      count++;
    }
  }

  if (toWrite.length) store.replaceQuotes(toWrite);

  return count;
}

export async function syncKlines(provider: MarketDataProvider, code: string, limit = 200): Promise<number> {
  const kl = await provider.getKlines(code, limit);
  if (!kl.length) return 0;

  store.upsertKlines(
    code,
    kl.map((k) => ({
      code: k.code,
      ts: k.ts,
      open: k.open,
      high: k.high,
      low: k.low,
      close: k.close,
      volume: k.volume,
      amount: k.amount
    }))
  );

  return kl.length;
}
