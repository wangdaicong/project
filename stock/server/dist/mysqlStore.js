import { getMysqlPool } from './mysql.js';
export async function createStockTables() {
    const p = await getMysqlPool();
    await p.execute(`
    CREATE TABLE IF NOT EXISTS symbols (
      index_tag VARCHAR(16) NOT NULL COMMENT '股票池标签（HS300/TOPA等）',
      code VARCHAR(16) NOT NULL COMMENT '证券代码（不含SH/SZ前缀）',
      name VARCHAR(128) NOT NULL COMMENT '名称',
      market VARCHAR(8) NOT NULL COMMENT '市场：SH/SZ',
      inserted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入表时间',
      PRIMARY KEY (index_tag, code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票池成分表';
  `);
    try {
        await p.execute("ALTER TABLE symbols " +
            "MODIFY COLUMN index_tag VARCHAR(16) NOT NULL COMMENT '股票池标签（HS300/TOPA等）', " +
            "MODIFY COLUMN code VARCHAR(16) NOT NULL COMMENT '证券代码（不含SH/SZ前缀）', " +
            "MODIFY COLUMN name VARCHAR(128) NOT NULL COMMENT '名称', " +
            "MODIFY COLUMN market VARCHAR(8) NOT NULL COMMENT '市场：SH/SZ', " +
            "ADD COLUMN inserted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入表时间'");
    }
    catch {
        // ignore
    }
    try {
        await p.execute("ALTER TABLE symbols MODIFY COLUMN inserted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入表时间'");
    }
    catch {
        // ignore
    }
    await p.execute(`
    CREATE TABLE IF NOT EXISTS quotes (
      code VARCHAR(16) NOT NULL COMMENT '证券代码（含SH/SZ前缀）',
      name_cn VARCHAR(128) NULL COMMENT '中文名',
      ts DATETIME NOT NULL COMMENT '报价时间',
      price DOUBLE NULL COMMENT '最新价',
      open DOUBLE NULL COMMENT '开盘价',
      high DOUBLE NULL COMMENT '最高价',
      low DOUBLE NULL COMMENT '最低价',
      prev_close DOUBLE NULL COMMENT '昨收',
      volume DOUBLE NULL COMMENT '成交量',
      amount DOUBLE NULL COMMENT '成交额',
      pct DOUBLE NULL COMMENT '涨跌幅(%)',
      sector VARCHAR(255) NULL COMMENT '行业/板块（弱结构化）',
      inserted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入表时间',
      PRIMARY KEY (code),
      KEY idx_ts (ts)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='最新报价（每个code一行）';
  `);
    try {
        await p.execute("ALTER TABLE quotes " +
            "MODIFY COLUMN code VARCHAR(16) NOT NULL COMMENT '证券代码（含SH/SZ前缀）', " +
            "ADD COLUMN name_cn VARCHAR(128) NULL COMMENT '中文名' AFTER code, " +
            "MODIFY COLUMN price DOUBLE NULL COMMENT '最新价', " +
            "MODIFY COLUMN open DOUBLE NULL COMMENT '开盘价', " +
            "MODIFY COLUMN high DOUBLE NULL COMMENT '最高价', " +
            "MODIFY COLUMN low DOUBLE NULL COMMENT '最低价', " +
            "MODIFY COLUMN prev_close DOUBLE NULL COMMENT '昨收', " +
            "MODIFY COLUMN volume DOUBLE NULL COMMENT '成交量', " +
            "MODIFY COLUMN amount DOUBLE NULL COMMENT '成交额', " +
            "MODIFY COLUMN pct DOUBLE NULL COMMENT '涨跌幅(%)', " +
            "MODIFY COLUMN sector VARCHAR(255) NULL COMMENT '行业/板块（弱结构化）'");
    }
    catch {
        // ignore
    }
    // quotes.ts 迁移：BIGINT -> DATETIME
    try {
        const [cols] = await p.query("SELECT DATA_TYPE as t FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='quotes' AND COLUMN_NAME='ts' LIMIT 1");
        const t = String(cols?.[0]?.t ?? '').toLowerCase();
        if (t === 'bigint') {
            await p.execute("ALTER TABLE quotes ADD COLUMN ts_dt DATETIME NULL COMMENT '报价时间(迁移列)' AFTER name_cn");
            await p.execute("UPDATE quotes SET ts_dt = FROM_UNIXTIME(ts/1000) WHERE ts_dt IS NULL AND ts > 0");
            await p.execute('ALTER TABLE quotes DROP INDEX idx_ts');
            await p.execute('ALTER TABLE quotes DROP COLUMN ts');
            await p.execute("ALTER TABLE quotes CHANGE COLUMN ts_dt ts DATETIME NOT NULL COMMENT '报价时间'");
            await p.execute('ALTER TABLE quotes ADD KEY idx_ts (ts)');
        }
        else {
            await p.execute("ALTER TABLE quotes MODIFY COLUMN ts DATETIME NOT NULL COMMENT '报价时间'");
        }
    }
    catch {
        // ignore
    }
    // quotes.inserted_at 迁移：BIGINT -> DATETIME
    try {
        const [cols] = await p.query("SELECT DATA_TYPE as t FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='quotes' AND COLUMN_NAME='inserted_at' LIMIT 1");
        const t = String(cols?.[0]?.t ?? '').toLowerCase();
        if (t === 'bigint') {
            await p.execute("ALTER TABLE quotes ADD COLUMN inserted_at_dt DATETIME NULL COMMENT '入表时间(迁移列)' AFTER sector");
            await p.execute("UPDATE quotes SET inserted_at_dt = FROM_UNIXTIME(inserted_at/1000) WHERE inserted_at_dt IS NULL AND inserted_at > 0");
            await p.execute('ALTER TABLE quotes DROP COLUMN inserted_at');
            await p.execute("ALTER TABLE quotes CHANGE COLUMN inserted_at_dt inserted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入表时间'");
        }
        else {
            await p.execute("ALTER TABLE quotes MODIFY COLUMN inserted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入表时间'");
        }
    }
    catch {
        // ignore
    }
    await p.execute(`
    CREATE TABLE IF NOT EXISTS klines (
      code VARCHAR(16) NOT NULL COMMENT '证券代码（含SH/SZ前缀，或指数symbol）',
      ts DATETIME NOT NULL COMMENT 'K线时间',
      open DOUBLE NOT NULL COMMENT '开盘价',
      high DOUBLE NOT NULL COMMENT '最高价',
      low DOUBLE NOT NULL COMMENT '最低价',
      close DOUBLE NOT NULL COMMENT '收盘价',
      volume DOUBLE NULL COMMENT '成交量',
      amount DOUBLE NULL COMMENT '成交额',
      PRIMARY KEY (code, ts),
      KEY idx_ts (ts)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='K线（日K为主，主键 code+ts）';
  `);
    try {
        await p.execute("ALTER TABLE klines " +
            "MODIFY COLUMN code VARCHAR(16) NOT NULL COMMENT '证券代码（含SH/SZ前缀，或指数symbol）', " +
            "MODIFY COLUMN ts DATETIME NOT NULL COMMENT 'K线时间', " +
            "MODIFY COLUMN open DOUBLE NOT NULL COMMENT '开盘价', " +
            "MODIFY COLUMN high DOUBLE NOT NULL COMMENT '最高价', " +
            "MODIFY COLUMN low DOUBLE NOT NULL COMMENT '最低价', " +
            "MODIFY COLUMN close DOUBLE NOT NULL COMMENT '收盘价', " +
            "MODIFY COLUMN volume DOUBLE NULL COMMENT '成交量', " +
            "MODIFY COLUMN amount DOUBLE NULL COMMENT '成交额'");
    }
    catch {
        // ignore
    }
    try {
        const [cols] = await p.query("SELECT DATA_TYPE as t FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='klines' AND COLUMN_NAME='ts' LIMIT 1");
        const t = String(cols?.[0]?.t ?? '').toLowerCase();
        if (t === 'bigint') {
            await p.execute("ALTER TABLE klines ADD COLUMN ts_dt DATETIME NULL COMMENT 'K线时间(迁移列)' AFTER code");
            await p.execute("UPDATE klines SET ts_dt = FROM_UNIXTIME(ts/1000) WHERE ts_dt IS NULL AND ts > 0");
            await p.execute('ALTER TABLE klines DROP INDEX idx_ts');
            await p.execute('ALTER TABLE klines DROP PRIMARY KEY');
            await p.execute('ALTER TABLE klines DROP COLUMN ts');
            await p.execute("ALTER TABLE klines CHANGE COLUMN ts_dt ts DATETIME NOT NULL COMMENT 'K线时间'");
            await p.execute('ALTER TABLE klines ADD PRIMARY KEY (code, ts)');
            await p.execute('ALTER TABLE klines ADD KEY idx_ts (ts)');
        }
    }
    catch {
        // ignore
    }
    await p.execute(`
    CREATE TABLE IF NOT EXISTS news (
      id VARCHAR(128) NOT NULL COMMENT '新闻ID（hash）',
      ts DATETIME NOT NULL COMMENT '发布时间',
      title VARCHAR(255) NOT NULL COMMENT '标题',
      url VARCHAR(512) NOT NULL COMMENT '链接',
      source VARCHAR(64) NOT NULL COMMENT '来源',
      summary TEXT NULL COMMENT '摘要',
      inserted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入表时间',
      PRIMARY KEY (id),
      KEY idx_ts (ts)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻聚合（RSS/Atom）';
  `);
    try {
        await p.execute("ALTER TABLE news " +
            "MODIFY COLUMN id VARCHAR(128) NOT NULL COMMENT '新闻ID（hash）', " +
            "MODIFY COLUMN ts DATETIME NOT NULL COMMENT '发布时间', " +
            "MODIFY COLUMN title VARCHAR(255) NOT NULL COMMENT '标题', " +
            "MODIFY COLUMN url VARCHAR(512) NOT NULL COMMENT '链接', " +
            "MODIFY COLUMN source VARCHAR(64) NOT NULL COMMENT '来源', " +
            "MODIFY COLUMN summary TEXT NULL COMMENT '摘要'");
    }
    catch {
        // ignore
    }
    try {
        await p.execute("ALTER TABLE news MODIFY COLUMN inserted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入表时间'");
    }
    catch {
        // ignore
    }
    // news.ts 迁移
    try {
        const [cols] = await p.query("SELECT DATA_TYPE as t FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='news' AND COLUMN_NAME='ts' LIMIT 1");
        const t = String(cols?.[0]?.t ?? '').toLowerCase();
        if (t === 'bigint') {
            await p.execute("ALTER TABLE news ADD COLUMN ts_dt DATETIME NULL COMMENT '发布时间(迁移列)' AFTER id");
            await p.execute("UPDATE news SET ts_dt = FROM_UNIXTIME(ts/1000) WHERE ts_dt IS NULL AND ts > 0");
            await p.execute('ALTER TABLE news DROP INDEX idx_ts');
            await p.execute('ALTER TABLE news DROP COLUMN ts');
            await p.execute("ALTER TABLE news CHANGE COLUMN ts_dt ts DATETIME NOT NULL COMMENT '发布时间'");
            await p.execute('ALTER TABLE news ADD KEY idx_ts (ts)');
        }
    }
    catch {
        // ignore
    }
    // news.inserted_at 迁移
    try {
        const [cols] = await p.query("SELECT DATA_TYPE as t FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='news' AND COLUMN_NAME='inserted_at' LIMIT 1");
        const t = String(cols?.[0]?.t ?? '').toLowerCase();
        if (t === 'bigint') {
            await p.execute("ALTER TABLE news ADD COLUMN inserted_at_dt DATETIME NULL COMMENT '入表时间(迁移列)' AFTER summary");
            await p.execute("UPDATE news SET inserted_at_dt = FROM_UNIXTIME(inserted_at/1000) WHERE inserted_at_dt IS NULL AND inserted_at > 0");
            await p.execute('ALTER TABLE news DROP COLUMN inserted_at');
            await p.execute("ALTER TABLE news CHANGE COLUMN inserted_at_dt inserted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入表时间'");
        }
        else {
            await p.execute("ALTER TABLE news MODIFY COLUMN inserted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入表时间'");
        }
    }
    catch {
        // ignore
    }
    // 数据保留：按 inserted_at 每月清理一个月前数据（需要 MySQL event_scheduler）
    // 说明：触发器无法按月调度；这里只能用 EVENT。
    try {
        await p.query('SET GLOBAL event_scheduler = ON');
    }
    catch (e) {
        console.error('[mysql] enable event_scheduler failed', e);
    }
    try {
        await p.query('DROP EVENT IF EXISTS ev_purge_quotes_2m');
    }
    catch {
        // ignore
    }
    try {
        await p.query("CREATE EVENT IF NOT EXISTS ev_purge_quotes_1m " +
            "ON SCHEDULE EVERY 1 MONTH STARTS (CURRENT_TIMESTAMP + INTERVAL 1 MONTH) " +
            "DO DELETE FROM quotes WHERE inserted_at < (NOW() - INTERVAL 1 MONTH)");
    }
    catch (e) {
        console.error('[mysql] create event ev_purge_quotes_1m failed', e);
    }
    try {
        await p.query('DROP EVENT IF EXISTS ev_purge_news_2m');
    }
    catch {
        // ignore
    }
    try {
        await p.query("CREATE EVENT IF NOT EXISTS ev_purge_news_1m " +
            "ON SCHEDULE EVERY 1 MONTH STARTS (CURRENT_TIMESTAMP + INTERVAL 1 MONTH) " +
            "DO DELETE FROM news WHERE inserted_at < (NOW() - INTERVAL 1 MONTH)");
    }
    catch (e) {
        console.error('[mysql] create event ev_purge_news_1m failed', e);
    }
}
export async function fetchAllQuotes(limitRows = 6000) {
    const p = await getMysqlPool();
    const lmt = Math.max(1, Math.min(50_000, Math.floor(limitRows || 6000)));
    const [rows] = await p.query('SELECT * FROM quotes ORDER BY ts DESC LIMIT ?', [lmt]);
    return (rows ?? []).map((r) => ({
        code: String(r.code),
        name_cn: r.name_cn == null ? null : String(r.name_cn),
        ts: (r.ts instanceof Date ? r.ts.getTime() : new Date(r.ts).getTime()),
        price: r.price == null ? null : Number(r.price),
        open: r.open == null ? null : Number(r.open),
        high: r.high == null ? null : Number(r.high),
        low: r.low == null ? null : Number(r.low),
        prev_close: r.prev_close == null ? null : Number(r.prev_close),
        volume: r.volume == null ? null : Number(r.volume),
        amount: r.amount == null ? null : Number(r.amount),
        pct: r.pct == null ? null : Number(r.pct),
        sector: r.sector == null ? null : String(r.sector),
        inserted_at: r.inserted_at == null ? undefined : (r.inserted_at instanceof Date ? r.inserted_at.getTime() : new Date(r.inserted_at).getTime())
    }));
}
export async function fetchSymbolsByIndexTag(indexTag) {
    const p = await getMysqlPool();
    const [rows] = await p.query('SELECT code,name,market,index_tag FROM symbols WHERE index_tag=? ORDER BY code', [indexTag]);
    return (rows ?? []).map((r) => ({ code: String(r.code), name: String(r.name), market: String(r.market), index_tag: String(r.index_tag) }));
}
export async function fetchAllSymbols() {
    const p = await getMysqlPool();
    const [rows] = await p.query('SELECT code,name,market,index_tag FROM symbols');
    return (rows ?? []).map((r) => ({ code: String(r.code), name: String(r.name), market: String(r.market), index_tag: String(r.index_tag) }));
}
export async function countSymbolsByIndexTag(indexTag) {
    const p = await getMysqlPool();
    const [rows] = await p.query('SELECT COUNT(1) as c FROM symbols WHERE index_tag=?', [indexTag]);
    return Number(rows?.[0]?.c ?? 0);
}
export async function upsertSymbols(indexTag, rows) {
    if (!rows.length)
        return;
    const p = await getMysqlPool();
    const now = new Date();
    const values = rows.map((r) => [indexTag, r.code, r.name, r.market, now]);
    await p.query('INSERT INTO symbols (index_tag, code, name, market, inserted_at) VALUES ? ON DUPLICATE KEY UPDATE name=VALUES(name), market=VALUES(market), inserted_at=VALUES(inserted_at)', [values]);
}
export async function fetchQuote(code) {
    const p = await getMysqlPool();
    const [rows] = await p.query('SELECT * FROM quotes WHERE code=? LIMIT 1', [code]);
    const r = rows?.[0];
    if (!r)
        return null;
    return {
        code: String(r.code),
        name_cn: r.name_cn == null ? null : String(r.name_cn),
        ts: (r.ts instanceof Date ? r.ts.getTime() : new Date(r.ts).getTime()),
        price: r.price == null ? null : Number(r.price),
        open: r.open == null ? null : Number(r.open),
        high: r.high == null ? null : Number(r.high),
        low: r.low == null ? null : Number(r.low),
        prev_close: r.prev_close == null ? null : Number(r.prev_close),
        volume: r.volume == null ? null : Number(r.volume),
        amount: r.amount == null ? null : Number(r.amount),
        pct: r.pct == null ? null : Number(r.pct),
        sector: r.sector == null ? null : String(r.sector),
        inserted_at: r.inserted_at == null ? undefined : (r.inserted_at instanceof Date ? r.inserted_at.getTime() : new Date(r.inserted_at).getTime())
    };
}
export async function replaceQuotes(rows) {
    if (!rows.length)
        return;
    const p = await getMysqlPool();
    const insertedAt = new Date();
    const values = rows.map((q) => [
        q.code,
        q.name_cn ?? null,
        new Date(q.ts),
        q.price,
        q.open,
        q.high,
        q.low,
        q.prev_close,
        q.volume,
        q.amount,
        q.pct,
        q.sector ?? null,
        q.inserted_at == null ? insertedAt : new Date(q.inserted_at)
    ]);
    await p.query('INSERT INTO quotes (code,name_cn,ts,price,open,high,low,prev_close,volume,amount,pct,sector,inserted_at) VALUES ? ON DUPLICATE KEY UPDATE name_cn=VALUES(name_cn), ts=VALUES(ts), price=VALUES(price), open=VALUES(open), high=VALUES(high), low=VALUES(low), prev_close=VALUES(prev_close), volume=VALUES(volume), amount=VALUES(amount), pct=VALUES(pct), sector=VALUES(sector), inserted_at=VALUES(inserted_at)', [values]);
}
export async function upsertKlines(code, rows) {
    if (!rows.length)
        return;
    const p = await getMysqlPool();
    const values = rows.map((k) => [k.code, new Date(k.ts), k.open, k.high, k.low, k.close, k.volume, k.amount]);
    await p.query('INSERT INTO klines (code,ts,open,high,low,close,volume,amount) VALUES ? ON DUPLICATE KEY UPDATE open=VALUES(open), high=VALUES(high), low=VALUES(low), close=VALUES(close), volume=VALUES(volume), amount=VALUES(amount)', [values]);
}
export async function fetchKlinesByCode(code, limit) {
    const p = await getMysqlPool();
    const lmt = Math.max(1, Math.min(1000, Math.floor(limit || 200)));
    const [rows] = await p.query('SELECT code,ts,open,high,low,close,volume,amount FROM klines WHERE code=? ORDER BY ts DESC LIMIT ?', [code, lmt]);
    const list = (rows ?? []).map((r) => ({
        code: String(r.code),
        ts: (r.ts instanceof Date ? r.ts.getTime() : new Date(r.ts).getTime()),
        open: Number(r.open),
        high: Number(r.high),
        low: Number(r.low),
        close: Number(r.close),
        volume: r.volume == null ? null : Number(r.volume),
        amount: r.amount == null ? null : Number(r.amount)
    }));
    return list.sort((a, b) => a.ts - b.ts);
}
export async function fetchRecentKlines(limitRows) {
    const p = await getMysqlPool();
    const lmt = Math.max(1, Math.min(200_000, Math.floor(limitRows || 50_000)));
    const [rows] = await p.query('SELECT code,ts,open,high,low,close,volume,amount FROM klines ORDER BY ts DESC LIMIT ?', [lmt]);
    return (rows ?? []).map((r) => ({
        code: String(r.code),
        ts: (r.ts instanceof Date ? r.ts.getTime() : new Date(r.ts).getTime()),
        open: Number(r.open),
        high: Number(r.high),
        low: Number(r.low),
        close: Number(r.close),
        volume: r.volume == null ? null : Number(r.volume),
        amount: r.amount == null ? null : Number(r.amount)
    }));
}
export async function fetchNews(limit) {
    const p = await getMysqlPool();
    const lmt = Math.max(1, Math.min(500, Math.floor(limit || 120)));
    const [rows] = await p.query('SELECT id,ts,title,url,source,summary,inserted_at FROM news ORDER BY ts DESC LIMIT ?', [lmt]);
    return (rows ?? []).map((r) => ({
        id: String(r.id),
        ts: (r.ts instanceof Date ? r.ts.getTime() : new Date(r.ts).getTime()),
        title: String(r.title),
        url: String(r.url),
        source: String(r.source),
        summary: r.summary == null ? null : String(r.summary),
        inserted_at: r.inserted_at == null ? undefined : (r.inserted_at instanceof Date ? r.inserted_at.getTime() : new Date(r.inserted_at).getTime())
    }));
}
export async function replaceNews(items) {
    if (!items.length)
        return 0;
    const p = await getMysqlPool();
    const insertedAt = new Date();
    const values = items.map((it) => [it.id, new Date(it.ts), it.title, it.url, it.source, it.summary ?? null, it.inserted_at == null ? insertedAt : new Date(it.inserted_at)]);
    await p.query('INSERT INTO news (id,ts,title,url,source,summary,inserted_at) VALUES ? ON DUPLICATE KEY UPDATE ts=VALUES(ts), title=VALUES(title), url=VALUES(url), source=VALUES(source), summary=VALUES(summary), inserted_at=VALUES(inserted_at)', [values]);
    return items.length;
}
