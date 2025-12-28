import mysql from 'mysql2/promise';
import { getMysqlConfigFromEnv } from '../src/mysql.js';
import { ensureRecoHistoryTables } from '../src/reco/history.js';

async function main() {
  const cfg = getMysqlConfigFromEnv();
  const conn = await mysql.createConnection({
    host: cfg.host,
    port: cfg.port,
    user: cfg.user,
    password: cfg.password,
    database: cfg.database
  });

  try {
    await ensureRecoHistoryTables();
  } catch (e: any) {
    // eslint-disable-next-line no-console
    console.error('[ensureRecoHistoryTables failed]', e?.code ?? e?.message ?? e);
  }

  const [tables] = await conn.query<any[]>("SHOW TABLES LIKE 'reco_%'");
  let runs: any[] = [];
  let items: any[] = [];
  try {
    const [r] = await conn.query<any[]>('SELECT id,run_type,query_text,limit_n,took_ms,created_at FROM reco_run ORDER BY id DESC LIMIT 3');
    runs = r;
  } catch (e: any) {
    runs = [{ error: String(e?.code ?? e?.message ?? e) }];
  }

  try {
    const [i] = await conn.query<any[]>('SELECT run_id,COUNT(1) as c FROM reco_item GROUP BY run_id ORDER BY run_id DESC LIMIT 3');
    items = i;
  } catch (e: any) {
    items = [{ error: String(e?.code ?? e?.message ?? e) }];
  }

  // eslint-disable-next-line no-console
  console.log(JSON.stringify({ tables, runs, items }, null, 2));
  await conn.end();
}

main().catch((e) => {
  // eslint-disable-next-line no-console
  console.error(e);
  process.exitCode = 1;
});
