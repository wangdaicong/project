export type MarketSymbol = {
  code: string;
  name: string;
  market: 'SH' | 'SZ';
};

export type Quote = {
  code: string;
  ts: number; // ms
  price: number | null;
  open: number | null;
  high: number | null;
  low: number | null;
  prevClose: number | null;
  volume: number | null;
  amount: number | null;
  pct: number | null;
  sector?: string | null;
};

export type Kline = {
  code: string;
  ts: number; // ms
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number | null;
  amount: number | null;
};

export interface MarketDataProvider {
  name: string;
  getHS300Symbols(): Promise<MarketSymbol[]>;
  getTopAShareSymbols(limit: number): Promise<MarketSymbol[]>;
  getQuote(code: string): Promise<Quote | null>;
  getKlines(code: string, limit: number): Promise<Kline[]>;
}
