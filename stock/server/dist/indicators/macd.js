function ema(values, period) {
    const k = 2 / (period + 1);
    const out = [];
    let prev = null;
    for (const v of values) {
        if (prev === null) {
            prev = v;
        }
        else {
            prev = v * k + prev * (1 - k);
        }
        out.push(prev);
    }
    return out;
}
export function calcMACD(ts, close, fast = 12, slow = 26, signal = 9) {
    if (ts.length !== close.length)
        return [];
    if (close.length === 0)
        return [];
    const emaFast = ema(close, fast);
    const emaSlow = ema(close, slow);
    const dif = close.map((_, i) => emaFast[i] - emaSlow[i]);
    const dea = ema(dif, signal);
    const macd = dif.map((v, i) => (v - dea[i]) * 2);
    return ts.map((t, i) => ({ ts: t, dif: dif[i], dea: dea[i], macd: macd[i] }));
}
