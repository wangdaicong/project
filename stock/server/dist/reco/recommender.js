import { calcMACD } from '../indicators/macd.js';
export function computeRecoScore(reco) {
    let s = 0;
    for (const sig of reco.signals || []) {
        if (sig.bias === 'bullish')
            s += 4;
        else if (sig.bias === 'bearish')
            s -= 4;
        else
            s += 0;
        if (sig.key === 'macd_cross' && sig.value.includes('可能'))
            s += 4;
        if (sig.key === 'macd_hist' && sig.value.includes('0轴上方'))
            s += 2;
        if (sig.key === 'trend_5d' && sig.value.includes('偏强'))
            s += 2;
    }
    const k = Array.isArray(reco.newsKeywords) ? reco.newsKeywords.length : 0;
    s += Math.min(6, k * 1.5);
    return Number(s.toFixed(2));
}
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
    const macdBias = lastMacd.macd > 0 ? 'bullish' : lastMacd.macd < 0 ? 'bearish' : 'neutral';
    const trendBias = trendUp ? 'bullish' : 'bearish';
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
    const signals = [
        {
            key: 'trend_5d',
            label: '近5日趋势',
            value: trendUp ? '偏强' : '震荡/偏弱',
            bias: trendBias
        },
        {
            key: 'macd_hist',
            label: 'MACD柱',
            value: lastMacd.macd >= 0 ? '在0轴上方' : '在0轴下方',
            bias: macdBias
        },
        {
            key: 'macd_cross',
            label: 'MACD翻红',
            value: crossUp ? '可能出现' : '未出现',
            bias: crossUp ? 'bullish' : 'neutral'
        }
    ];
    const triggers = [];
    if (crossUp)
        triggers.push('MACD柱由负转正（翻红）后保持 1-3 个交易日确认');
    triggers.push('若回踩不破风控价（止损价）且量能配合，可考虑分批入场');
    const risks = [];
    risks.push('免费数据源可能延迟/缺失，信号存在误差');
    risks.push('若跌破风控价建议止损；不建议重仓单一标的');
    if (!trendUp)
        risks.push('近5日趋势偏弱，存在继续回撤风险');
    const summary = `${trendUp ? '偏强' : '偏弱'} / MACD${lastMacd.macd >= 0 ? '强' : '弱'}${crossUp ? '（翻红）' : ''}`;
    const score = computeRecoScore({ signals, newsKeywords: newsKeywords.slice(0, 5) });
    return {
        code,
        ts: Date.now(),
        entry,
        exit,
        risk,
        score,
        summary,
        signals,
        triggers,
        risks,
        newsKeywords: newsKeywords.slice(0, 5),
        reason: reasonParts.join('；')
    };
}
