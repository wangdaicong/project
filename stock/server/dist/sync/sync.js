import { store } from '../store.js';
export async function refreshHS300(provider) {
    const symbols = await provider.getHS300Symbols();
    if (!symbols.length)
        return 0;
    store.upsertSymbols('HS300', symbols.map((s) => ({ code: s.code, name: s.name, market: s.market })));
    return symbols.length;
}
export async function refreshTopAShares(provider, limit = 800) {
    const symbols = await provider.getTopAShareSymbols(limit);
    if (!symbols.length)
        return 0;
    store.upsertSymbols('TOPA', symbols.map((s) => ({ code: s.code, name: s.name, market: s.market })));
    return symbols.length;
}
export async function syncQuotes(provider, codes, batchSize = 20) {
    const toWrite = [];
    let count = 0;
    for (let i = 0; i < codes.length; i += batchSize) {
        const batch = codes.slice(i, i + batchSize);
        const results = await Promise.all(batch.map(async (c) => {
            try {
                return await provider.getQuote(c);
            }
            catch {
                return null;
            }
        }));
        for (const q of results) {
            if (!q)
                continue;
            toWrite.push({
                code: q.code,
                ts: q.ts,
                price: q.price,
                open: q.open,
                high: q.high,
                low: q.low,
                prev_close: q.prevClose,
                volume: q.volume,
                amount: q.amount,
                pct: q.pct
            });
            count++;
        }
    }
    if (toWrite.length)
        store.replaceQuotes(toWrite);
    return count;
}
export async function syncKlines(provider, code, limit = 200) {
    const kl = await provider.getKlines(code, limit);
    if (!kl.length)
        return 0;
    store.upsertKlines(code, kl.map((k) => ({
        code: k.code,
        ts: k.ts,
        open: k.open,
        high: k.high,
        low: k.low,
        close: k.close,
        volume: k.volume,
        amount: k.amount
    })));
    return kl.length;
}
