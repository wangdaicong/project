import { ensureMysqlTables, getMysqlPool } from '../mysql.js';

export type RecoTopCacheRow = {
  cacheKey: string;
  ts: number;
  payload: any;
};

export async function getRecoTopCache(cacheKey: string): Promise<RecoTopCacheRow | null> {
  try {
    await ensureMysqlTables();
    const p = await getMysqlPool();
    const [rows] = await p.query<any[]>(
      'SELECT cache_key as cacheKey, ts, payload FROM reco_top_cache WHERE cache_key = ? LIMIT 1',
      [cacheKey]
    );
    const r = rows && rows[0];
    if (!r) return null;
    let payload: any = null;
    try {
      payload = JSON.parse(r.payload);
    } catch {
      payload = null;
    }
    const ts = r.ts instanceof Date ? r.ts.getTime() : new Date(r.ts).getTime();
    return { cacheKey: r.cacheKey, ts, payload };
  } catch {
    return null;
  }
}

export async function setRecoTopCache(cacheKey: string, ts: number, payload: any): Promise<boolean> {
  try {
    await ensureMysqlTables();
    const p = await getMysqlPool();
    const body = JSON.stringify(payload ?? null);
    await p.execute(
      'INSERT INTO reco_top_cache (cache_key, ts, payload) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE ts=VALUES(ts), payload=VALUES(payload)',
      [cacheKey, new Date(ts), body]
    );
    return true;
  } catch {
    return false;
  }
}
