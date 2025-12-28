import { store } from '../store.js';
import { DEFAULT_FEEDS, fetchRssFeeds } from './rss.js';
export async function refreshNews() {
    const items = await fetchRssFeeds(DEFAULT_FEEDS);
    if (!items.length)
        return 0;
    return store.replaceNews(items);
}
