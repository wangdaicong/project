import Database from 'better-sqlite3';
import fs from 'node:fs';
import path from 'node:path';
const dataDir = path.resolve(process.cwd(), 'data');
if (!fs.existsSync(dataDir))
    fs.mkdirSync(dataDir, { recursive: true });
const dbPath = path.resolve(dataDir, 'app.db');
export const db = new Database(dbPath);
db.pragma('journal_mode = WAL');
db.exec(`
CREATE TABLE IF NOT EXISTS symbols (
  code TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  market TEXT NOT NULL,
  index_tag TEXT
);

CREATE TABLE IF NOT EXISTS quotes (
  code TEXT NOT NULL,
  ts INTEGER NOT NULL,
  price REAL,
  open REAL,
  high REAL,
  low REAL,
  prev_close REAL,
  volume REAL,
  amount REAL,
  pct REAL,
  PRIMARY KEY (code, ts)
);

CREATE TABLE IF NOT EXISTS klines (
  code TEXT NOT NULL,
  ts INTEGER NOT NULL,
  open REAL NOT NULL,
  high REAL NOT NULL,
  low REAL NOT NULL,
  close REAL NOT NULL,
  volume REAL,
  amount REAL,
  PRIMARY KEY (code, ts)
);

CREATE INDEX IF NOT EXISTS idx_quotes_code_ts ON quotes(code, ts);
CREATE INDEX IF NOT EXISTS idx_klines_code_ts ON klines(code, ts);
`);
