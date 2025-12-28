import fs from 'node:fs';
import path from 'node:path';
const dataDir = path.resolve(process.cwd(), 'data');
if (!fs.existsSync(dataDir))
    fs.mkdirSync(dataDir, { recursive: true });
const storePath = path.resolve(dataDir, 'store.json');
function readStore() {
    if (!fs.existsSync(storePath)) {
        return { symbols: [], quotes: [], klines: [], news: [] };
    }
    const raw = fs.readFileSync(storePath, 'utf-8');
    try {
        const parsed = JSON.parse(raw);
        return {
            symbols: Array.isArray(parsed.symbols) ? parsed.symbols : [],
            quotes: Array.isArray(parsed.quotes) ? parsed.quotes : [],
            klines: Array.isArray(parsed.klines) ? parsed.klines : [],
            news: Array.isArray(parsed.news) ? parsed.news : []
        };
    }
    catch {
        return { symbols: [], quotes: [], klines: [], news: [] };
    }
}
function writeStore(data) {
    fs.writeFileSync(storePath, JSON.stringify(data), 'utf-8');
}
export const store = {
    getSymbolsByIndexTag(indexTag) {
        const s = readStore();
        return s.symbols.filter((x) => x.index_tag === indexTag).sort((a, b) => a.code.localeCompare(b.code));
    },
    upsertSymbols(indexTag, rows) {
        const s = readStore();
        const map = new Map(s.symbols.map((x) => [`${x.index_tag ?? ''}:${x.code}`, x]));
        for (const r of rows) {
            map.set(`${indexTag}:${r.code}`, { code: r.code, name: r.name, market: r.market, index_tag: indexTag });
        }
        s.symbols = Array.from(map.values());
        writeStore(s);
    },
    countSymbols(indexTag) {
        const s = readStore();
        return s.symbols.filter((x) => x.index_tag === indexTag).length;
    },
    replaceQuotes(rows) {
        const s = readStore();
        // quotes 只保留每个 code 最新 1 条
        const map = new Map();
        for (const q of s.quotes)
            map.set(q.code, q);
        for (const q of rows)
            map.set(q.code, q);
        s.quotes = Array.from(map.values());
        writeStore(s);
    },
    upsertKlines(code, rows) {
        const s = readStore();
        const keep = s.klines.filter((x) => x.code !== code);
        // 同一 code 内按 ts 去重
        const map = new Map();
        for (const r of rows)
            map.set(r.ts, r);
        const merged = Array.from(map.values()).sort((a, b) => a.ts - b.ts);
        s.klines = keep.concat(merged);
        writeStore(s);
    },
    getKlines(code, limit) {
        const s = readStore();
        const rows = s.klines.filter((x) => x.code === code).sort((a, b) => a.ts - b.ts);
        const lmt = Math.max(1, Math.min(1000, limit));
        return rows.slice(Math.max(0, rows.length - lmt));
    },
    getNews(limit) {
        const s = readStore();
        return s.news
            .slice()
            .sort((a, b) => b.ts - a.ts)
            .slice(0, Math.max(1, Math.min(200, limit)));
    },
    replaceNews(items) {
        const s = readStore();
        const map = new Map();
        for (const it of s.news)
            map.set(it.id, it);
        const before = map.size;
        for (const it of items)
            map.set(it.id, it);
        const merged = Array.from(map.values()).sort((a, b) => b.ts - a.ts).slice(0, 500);
        s.news = merged;
        writeStore(s);
        const after = new Set(merged.map((x) => x.id)).size;
        return Math.max(0, after - before);
    }
};
