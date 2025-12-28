export function filterNews(items, q) {
    const kw = (q ?? '').trim();
    if (!kw)
        return items;
    const k = kw.toLowerCase();
    return items.filter((it) => (it.title + ' ' + (it.summary ?? '')).toLowerCase().includes(k));
}
