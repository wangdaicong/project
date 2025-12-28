import { calcMACD } from '../indicators/macd.js';
export function makeReco(code, k, newsKeywords = []) {
    if (k.length < 35)
        return null;
    const ts = k.map((x) => x.ts);
    const close = k.map((x) => x.close);
    const macd = calcMACD(ts, close);
    const last = k[k.length - 1];
    const lastMacd = macd[macd.length - 1];
    const prevMacd = macd[macd.length - 2];
    const crossUp = prevMacd.macd <= 0 && lastMacd.macd > 0;
    const trendUp = close[close.length - 1] >= close[close.length - 6];
    const entry = Number((last.close * 0.985).toFixed(2));
    const risk = Number((last.close * 0.94).toFixed(2));
    const exit = Number((last.close * (trendUp ? 1.08 : 1.05)).toFixed(2));
    const reasonParts = [];
    reasonParts.push(`技术面：近5日${trendUp ? '偏强' : '震荡/偏弱'}；MACD柱 ${lastMacd.macd >= 0 ? '在0轴上方' : '在0轴下方'}`);
    if (crossUp)
        reasonParts.push('信号：MACD出现由弱转强的“翻红”迹象（仅供参考）');
    if (newsKeywords.length)
        reasonParts.push(`时事面：近期高频主题 ${newsKeywords.slice(0, 5).join(' / ')}（仅供参考）`);
    reasonParts.push('风控：建议严格设置止损线，避免单笔亏损扩大');
    return {
        code,
        ts: Date.now(),
        entry,
        exit,
        risk,
        reason: reasonParts.join('；')
    };
}
