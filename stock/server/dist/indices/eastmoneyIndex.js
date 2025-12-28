import axios from 'axios';
function normPrice(v) {
    if (typeof v !== 'number' || !Number.isFinite(v))
        return null;
    return Math.abs(v) > 100000 ? v / 100 : v;
}
function normPct(v) {
    if (typeof v !== 'number' || !Number.isFinite(v))
        return null;
    return Math.abs(v) > 100 ? v / 100 : v;
}
export async function getIndexQuote(secid, symbol) {
    const url = 'https://push2.eastmoney.com/api/qt/stock/get';
    const resp = await axios.get(url, {
        timeout: 10000,
        params: {
            secid,
            fltt: 2,
            invt: 2,
            fields: 'f43,f44,f45,f46,f47,f48,f49,f50,f51,f52,f60,f170'
        }
    });
    const d = resp.data?.data;
    if (!d)
        return null;
    const now = Date.now();
    const price = normPrice(d.f43);
    const open = normPrice(d.f46);
    const high = normPrice(d.f44);
    const low = normPrice(d.f45);
    const prevClose = normPrice(d.f60);
    const pct = normPct(d.f170);
    const volume = typeof d.f47 === 'number' ? d.f47 : null;
    const amount = typeof d.f48 === 'number' ? d.f48 : null;
    return {
        code: symbol,
        ts: now,
        price,
        open,
        high,
        low,
        prevClose,
        volume,
        amount,
        pct
    };
}
export async function getIndexKlines(secid, symbol, limit) {
    const url = 'https://push2his.eastmoney.com/api/qt/stock/kline/get';
    const resp = await axios.get(url, {
        timeout: 12000,
        params: {
            secid,
            klt: 101,
            fqt: 1,
            lmt: Math.max(10, Math.min(1000, limit)),
            fields1: 'f1,f2,f3,f4,f5,f6',
            fields2: 'f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61'
        }
    });
    const kl = resp.data?.data?.klines;
    if (!Array.isArray(kl))
        return [];
    return kl
        .map((line) => {
        const parts = String(line).split(',');
        if (parts.length < 7)
            return null;
        const [date, open, close, high, low, vol, amt] = parts;
        const ts = new Date(date).getTime();
        if (!Number.isFinite(ts))
            return null;
        const o = Number(open);
        const c = Number(close);
        const h = Number(high);
        const l = Number(low);
        if (![o, c, h, l].every((v) => Number.isFinite(v)))
            return null;
        return {
            code: symbol,
            ts,
            open: o,
            close: c,
            high: h,
            low: l,
            volume: Number.isFinite(Number(vol)) ? Number(vol) : null,
            amount: Number.isFinite(Number(amt)) ? Number(amt) : null
        };
    })
        .filter((x) => Boolean(x))
        .sort((a, b) => a.ts - b.ts);
}
