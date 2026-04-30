const CACHE_NAME = 'dmrc-roster-v1';
const ASSETS = [
    '/dmrc-roster-pwa/',
    '/dmrc-roster-pwa/index.html',
    '/dmrc-roster-pwa/dmrc-roster-pwa.js',
    '/dmrc-roster-pwa/manifest.json',
    '/dmrc-roster-pwa/styles.css'
];

self.addEventListener('install', e => {
    e.waitUntil(
        caches.open(CACHE_NAME).then(cache => cache.addAll(ASSETS))
    );
    self.skipWaiting();
});

self.addEventListener('activate', e => {
    e.waitUntil(
        caches.keys().then(keys =>
            Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))
        )
    );
    self.clients.claim();
});

self.addEventListener('fetch', e => {
    if (e.request.url.includes('supabase.co')) {
        e.respondWith(fetch(e.request).catch(() => caches.match('/dmrc-roster-pwa/index.html')));
        return;
    }
    e.respondWith(
        caches.match(e.request).then(cached => cached || fetch(e.request))
    );
});
