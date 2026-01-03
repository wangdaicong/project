import { getMysqlPool } from '../mysql.js';
export async function ensureRecoHistoryTables() {
    const p = await getMysqlPool();
    try {
        await p.execute(`
      CREATE TABLE IF NOT EXISTS reco_run (
        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '推荐批次ID',
        run_type VARCHAR(16) NOT NULL COMMENT '推荐类型：top/keyword',
        query_text VARCHAR(128) NULL COMMENT '关键字（仅keyword）',
        keywords_json TEXT NULL COMMENT '用于本次计算的热点//关键字 JSON',
        limit_n INT NOT NULL COMMENT '返回数量',
        took_ms INT NOT NULL COMMENT '计算耗时毫秒',
        created_at DATETIME NOT NULL COMMENT '创建时间',
        PRIMARY KEY (id),
        KEY idx_created_at (created_at),
        KEY idx_run_type (run_type)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐历史：每次计算一条';
    `);
    }
    catch (e) {
        console.error('[mysql] create table reco_run failed', e);
        throw e;
    }
    try {
        const [cols] = await p.query("SELECT DATA_TYPE as t FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='reco_run' AND COLUMN_NAME='created_at' LIMIT 1");
        const t = String(cols?.[0]?.t ?? '').toLowerCase();
        if (t === 'bigint') {
            await p.execute("ALTER TABLE reco_run ADD COLUMN created_at_dt DATETIME NULL COMMENT '创建时间(迁移列)' AFTER took_ms");
            await p.execute("UPDATE reco_run SET created_at_dt = FROM_UNIXTIME(created_at/1000) WHERE created_at_dt IS NULL AND created_at > 0");
            await p.execute('ALTER TABLE reco_run DROP INDEX idx_created_at');
            await p.execute('ALTER TABLE reco_run DROP COLUMN created_at');
            await p.execute("ALTER TABLE reco_run CHANGE COLUMN created_at_dt created_at DATETIME NOT NULL COMMENT '创建时间'");
            await p.execute('ALTER TABLE reco_run ADD KEY idx_created_at (created_at)');
        }
        else {
            await p.execute("ALTER TABLE reco_run MODIFY COLUMN created_at DATETIME NOT NULL COMMENT '创建时间'");
        }
    }
    catch (e) {
        console.error('[mysql] migrate reco_run.created_at failed', e);
    }
    try {
        await p.execute(`
      CREATE TABLE IF NOT EXISTS reco_item (
        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
        run_id BIGINT NOT NULL COMMENT '关联 reco_run.id',
        rank_n INT NOT NULL COMMENT '名次，从1开始',
        code VARCHAR(16) NOT NULL COMMENT '证券代码（含SH/SZ前缀）',
        name VARCHAR(128) NOT NULL COMMENT '名称',
        market VARCHAR(8) NOT NULL COMMENT '市场：SH/SZ',
        sector VARCHAR(255) NULL COMMENT '行业/板块',
        quote_ts DATETIME NULL COMMENT '报价时间',
        price DOUBLE NULL COMMENT '最新价',
        pct DOUBLE NULL COMMENT '涨跌幅(%)',
        score DOUBLE NULL COMMENT '推荐分数',
        entry DOUBLE NULL COMMENT '建议入场价',
        exit_price DOUBLE NULL COMMENT '建议止盈价',
        risk DOUBLE NULL COMMENT '建议止损价',
        summary VARCHAR(255) NULL COMMENT '摘要',
        reason TEXT NULL COMMENT '理由',
        signals_json TEXT NULL COMMENT '信号 JSON',
        triggers_json TEXT NULL COMMENT '触发条件 JSON',
        risks_json TEXT NULL COMMENT '风险提示 JSON',
        news_keywords_json TEXT NULL COMMENT '热点/关键字 JSON',
        created_at DATETIME NOT NULL COMMENT '创建时间',
        PRIMARY KEY (id),
        KEY idx_run_id (run_id),
        KEY idx_code (code)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐历史明细：每只股票一行';
    `);
    }
    catch (e) {
        console.error('[mysql] create table reco_item failed', e);
        throw e;
    }
    try {
        const [cols] = await p.query("SELECT DATA_TYPE as t FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='reco_item' AND COLUMN_NAME='quote_ts' LIMIT 1");
        const t = String(cols?.[0]?.t ?? '').toLowerCase();
        if (t === 'bigint') {
            await p.execute("ALTER TABLE reco_item ADD COLUMN quote_ts_dt DATETIME NULL COMMENT '报价时间(迁移列)' AFTER sector");
            await p.execute("UPDATE reco_item SET quote_ts_dt = FROM_UNIXTIME(quote_ts/1000) WHERE quote_ts_dt IS NULL AND quote_ts > 0");
            await p.execute('ALTER TABLE reco_item DROP COLUMN quote_ts');
            await p.execute("ALTER TABLE reco_item CHANGE COLUMN quote_ts_dt quote_ts DATETIME NULL COMMENT '报价时间'");
        }
        else {
            await p.execute("ALTER TABLE reco_item MODIFY COLUMN quote_ts DATETIME NULL COMMENT '报价时间'");
        }
    }
    catch (e) {
        console.error('[mysql] migrate reco_item.quote_ts failed', e);
    }
    try {
        const [cols] = await p.query("SELECT DATA_TYPE as t FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='reco_item' AND COLUMN_NAME='created_at' LIMIT 1");
        const t = String(cols?.[0]?.t ?? '').toLowerCase();
        if (t === 'bigint') {
            await p.execute("ALTER TABLE reco_item ADD COLUMN created_at_dt DATETIME NULL COMMENT '创建时间(迁移列)' AFTER news_keywords_json");
            await p.execute("UPDATE reco_item SET created_at_dt = FROM_UNIXTIME(created_at/1000) WHERE created_at_dt IS NULL AND created_at > 0");
            await p.execute('ALTER TABLE reco_item DROP COLUMN created_at');
            await p.execute("ALTER TABLE reco_item CHANGE COLUMN created_at_dt created_at DATETIME NOT NULL COMMENT '创建时间'");
        }
        else {
            await p.execute("ALTER TABLE reco_item MODIFY COLUMN created_at DATETIME NOT NULL COMMENT '创建时间'");
        }
    }
    catch (e) {
        console.error('[mysql] migrate reco_item.created_at failed', e);
    }
}
export async function insertRecoRunWithItems(run, items) {
    try {
        await ensureRecoHistoryTables();
        const p = await getMysqlPool();
        const createdAt = new Date();
        const keywordsJson = JSON.stringify(run.keywords ?? []);
        const [r] = await p.execute('INSERT INTO reco_run (run_type, query_text, keywords_json, limit_n, took_ms, created_at) VALUES (?, ?, ?, ?, ?, ?)', [run.runType, run.queryText, keywordsJson, run.limit, run.tookMs, createdAt]);
        const runId = Number(r?.insertId ?? 0);
        if (!runId)
            return null;
        if (items.length) {
            const values = items.map((it) => [
                runId,
                it.rank,
                it.code,
                it.name,
                it.market,
                it.sector ?? null,
                it.ts == null ? null : new Date(it.ts),
                it.price ?? null,
                it.pct ?? null,
                it.score ?? null,
                it.entry ?? null,
                it.exit ?? null,
                it.risk ?? null,
                it.summary ?? null,
                it.reason ?? null,
                JSON.stringify(it.signals ?? null),
                JSON.stringify(it.triggers ?? null),
                JSON.stringify(it.risks ?? null),
                JSON.stringify(it.newsKeywords ?? null),
                createdAt
            ]);
            await p.query('INSERT INTO reco_item (run_id, rank_n, code, name, market, sector, quote_ts, price, pct, score, entry, exit_price, risk, summary, reason, signals_json, triggers_json, risks_json, news_keywords_json, created_at) VALUES ?', [values]);
        }
        return runId;
    }
    catch (e) {
        console.error('[mysql] insert reco history failed', e);
        return null;
    }
}
