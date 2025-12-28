import axios from 'axios';
import { XMLParser } from 'fast-xml-parser';
import crypto from 'node:crypto';
import iconv from 'iconv-lite';
import { NewsItem } from '../store.js';

export type RssFeed = {
  name: string;
  url: string;
};

const parser = new XMLParser({
  ignoreAttributes: false,
  attributeNamePrefix: '@_'
});

function toArray<T>(v: T | T[] | undefined | null): T[] {
  if (!v) return [];
  return Array.isArray(v) ? v : [v];
}

function hashId(s: string) {
  return crypto.createHash('sha256').update(s).digest('hex').slice(0, 24);
}

function decodeXml(buf: ArrayBuffer | Buffer | string): string {
  if (typeof buf === 'string') return buf;
  const b = Buffer.isBuffer(buf) ? buf : Buffer.from(buf);
  // 尝试从 XML 声明里读编码
  const head = b.subarray(0, 256).toString('ascii');
  const m = head.match(/encoding\s*=\s*['\"]([^'\"]+)['\"]/i);
  const encRaw = (m?.[1] ?? 'utf-8').trim().toLowerCase();
  const enc = encRaw === 'gb2312' ? 'gbk' : encRaw;
  try {
    if (enc && enc !== 'utf-8' && enc !== 'utf8') {
      return iconv.decode(b, enc);
    }
  } catch {
    // ignore
  }
  return b.toString('utf-8');
}

function parseRss(xml: string, source: string): NewsItem[] {
  const obj: any = parser.parse(xml);
  const items = toArray(obj?.rss?.channel?.item);
  return items
    .map((it: any) => {
      const title = String(it?.title ?? '').trim();
      const link = String(it?.link ?? '').trim();
      const pubDate = String(it?.pubDate ?? '').trim();
      const desc = String(it?.description ?? '').trim();
      const ts = pubDate ? Date.parse(pubDate) : Date.now();
      if (!title || !link) return null;
      return {
        id: hashId(source + '|' + link),
        ts: Number.isFinite(ts) ? ts : Date.now(),
        title,
        url: link,
        source,
        summary: desc ? desc.replace(/\s+/g, ' ').slice(0, 300) : null
      } satisfies NewsItem;
    })
    .filter((x: any): x is NewsItem => Boolean(x));
}

function parseAtom(xml: string, source: string): NewsItem[] {
  const obj: any = parser.parse(xml);
  const entries = toArray(obj?.feed?.entry);
  return entries
    .map((it: any) => {
      const title = String(it?.title ?? '').trim();
      const links = toArray(it?.link);
      const link = String(links?.[0]?.['@_href'] ?? links?.[0] ?? '').trim();
      const updated = String(it?.updated ?? it?.published ?? '').trim();
      const summary = String(it?.summary ?? it?.content ?? '').trim();
      const ts = updated ? Date.parse(updated) : Date.now();
      if (!title || !link) return null;
      return {
        id: hashId(source + '|' + link),
        ts: Number.isFinite(ts) ? ts : Date.now(),
        title,
        url: link,
        source,
        summary: summary ? summary.replace(/\s+/g, ' ').slice(0, 300) : null
      } satisfies NewsItem;
    })
    .filter((x: any): x is NewsItem => Boolean(x));
}

export async function fetchRssFeeds(feeds: RssFeed[]): Promise<NewsItem[]> {
  const results = await Promise.all(
    feeds.map(async (f) => {
      try {
        const resp = await axios.get(f.url, {
          timeout: 12000,
          responseType: 'arraybuffer',
          headers: {
            'user-agent': 'stock-mvp/0.1 (+https://localhost)',
            accept: 'application/rss+xml, application/atom+xml, application/xml, text/xml, */*'
          }
        });
        const xml = decodeXml(resp.data ?? '');
        const rssItems = parseRss(xml, f.name);
        if (rssItems.length) return rssItems;
        return parseAtom(xml, f.name);
      } catch {
        return [] as NewsItem[];
      }
    })
  );
  return results.flat().sort((a, b) => b.ts - a.ts);
}

export const DEFAULT_FEEDS: RssFeed[] = [
  // 说明：公开RSS源可能随时变更或受网络影响；这里提供多源冗余。
  { name: '中新网-财经', url: 'http://www.chinanews.com.cn/rss/finance.xml' },
  { name: '新浪财经-热门', url: 'http://rss.sina.com.cn/roll/finance/hot_roll.xml' },
  { name: '新浪财经-国内', url: 'http://rss.sina.com.cn/roll/finance/china_roll.xml' },
  { name: 'FT中文网', url: 'https://www.ftchinese.com/rss/news' },
  { name: '华尔街见闻', url: 'https://wallstreetcn.com/rss.xml' }
];
