export type IndexDef = {
  symbol: string;
  name: string;
  secid: string;
};

export const INDICES: IndexDef[] = [
  { symbol: 'SH000001', name: '上证指数', secid: '1.000001' },
  { symbol: 'SZ399001', name: '深证成指', secid: '0.399001' },
  { symbol: 'SZ399006', name: '创业板指', secid: '0.399006' },
  { symbol: 'SH000300', name: '沪深300', secid: '1.000300' },
  { symbol: 'SH000905', name: '中证500', secid: '1.000905' },
  { symbol: 'SH000688', name: '科创50', secid: '1.000688' }
];

export function getIndexBySymbol(symbol: string): IndexDef | null {
  const s = symbol.trim().toUpperCase();
  return INDICES.find((x) => x.symbol === s) ?? null;
}
