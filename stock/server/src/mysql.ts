import mysql from 'mysql2/promise';
import crypto from 'node:crypto';

let pool: mysql.Pool | null = null;

export type MysqlConfig = {
  host: string;
  port: number;
  user: string;
  password: string;
  database: string;
};

function decryptAes256Gcm(enc: string, keyB64: string): string {
  const key = Buffer.from(keyB64, 'base64');
  if (key.length !== 32) throw new Error('bad_key');

  const parts = String(enc || '').split(':');
  if (parts.length !== 3) throw new Error('bad_enc');
  const iv = Buffer.from(parts[0]!, 'base64');
  const data = Buffer.from(parts[1]!, 'base64');
  const tag = Buffer.from(parts[2]!, 'base64');

  const decipher = crypto.createDecipheriv('aes-256-gcm', key, iv);
  decipher.setAuthTag(tag);
  const out = Buffer.concat([decipher.update(data), decipher.final()]);
  return out.toString('utf8');
}

function resolveMysqlPassword(): string {
  const enc = process.env.MYSQL_PASSWORD_ENC;
  const key = process.env.MYSQL_PASSWORD_KEY;
  if (enc && key) {
    return decryptAes256Gcm(enc, key);
  }
  const pwd = process.env.MYSQL_PASSWORD;
  if (!pwd) throw new Error('MYSQL_PASSWORD_NOT_SET');
  return pwd;
}

export function getMysqlConfigFromEnv(): MysqlConfig {
  const user = process.env.MYSQL_USER;
  if (!user) throw new Error('MYSQL_USER_NOT_SET');
  return {
    host: process.env.MYSQL_HOST ?? '127.0.0.1',
    port: Number(process.env.MYSQL_PORT ?? 3306),
    user,
    password: resolveMysqlPassword(),
    database: process.env.MYSQL_DB ?? 'stock'
  };
}

export async function getMysqlPool(): Promise<mysql.Pool> {
  if (pool) return pool;
  const cfg = getMysqlConfigFromEnv();
  pool = mysql.createPool({
    host: cfg.host,
    port: cfg.port,
    user: cfg.user,
    password: cfg.password,
    database: cfg.database,
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
  });
  return pool;
}

export async function ensureMysqlTables(): Promise<void> {
  const p = await getMysqlPool();
  await p.execute(`
    CREATE TABLE IF NOT EXISTS reco_top_cache (
      cache_key VARCHAR(32) NOT NULL COMMENT '缓存键（例如 top:20）',
      ts DATETIME NOT NULL COMMENT '缓存写入时间',
      payload LONGTEXT NOT NULL COMMENT '缓存内容JSON',
      PRIMARY KEY (cache_key)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐Top缓存（秒回）';
  `);

  // 兼容：表已存在但缺少注释时补齐
  try {
    await p.execute(
      "ALTER TABLE reco_top_cache " +
        "MODIFY COLUMN cache_key VARCHAR(32) NOT NULL COMMENT '缓存键（例如 top:20）', " +
        "MODIFY COLUMN ts DATETIME NOT NULL COMMENT '缓存写入时间', " +
        "MODIFY COLUMN payload LONGTEXT NOT NULL COMMENT '缓存内容JSON'"
    );
  } catch {
    // ignore
  }

  // 迁移：若旧 ts 为 BIGINT，则迁移为 DATETIME（保持列名 ts）
  try {
    const [cols] = await p.query<any[]>(
      "SELECT DATA_TYPE as t FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='reco_top_cache' AND COLUMN_NAME='ts' LIMIT 1"
    );
    const t = String(cols?.[0]?.t ?? '').toLowerCase();
    if (t === 'bigint') {
      await p.execute("ALTER TABLE reco_top_cache ADD COLUMN ts_dt DATETIME NULL COMMENT '缓存写入时间(迁移列)' AFTER cache_key");
      await p.execute("UPDATE reco_top_cache SET ts_dt = FROM_UNIXTIME(ts/1000) WHERE ts_dt IS NULL AND ts > 0");
      await p.execute('ALTER TABLE reco_top_cache DROP COLUMN ts');
      await p.execute("ALTER TABLE reco_top_cache CHANGE COLUMN ts_dt ts DATETIME NOT NULL COMMENT '缓存写入时间'");
    }
  } catch {
    // ignore
  }
}

export async function mysqlHealthy(): Promise<boolean> {
  try {
    const p = await getMysqlPool();
    await p.query('SELECT 1');
    return true;
  } catch {
    return false;
  }
}
