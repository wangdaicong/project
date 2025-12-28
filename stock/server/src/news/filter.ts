import { NewsItem } from '../store.js';

export function filterNews(items: NewsItem[], q: string | null) {
  const kw = (q ?? '').trim();
  if (!kw) return items;
  const k = kw.toLowerCase();
  return items.filter((it) => (it.title + ' ' + (it.summary ?? '')).toLowerCase().includes(k));
}
