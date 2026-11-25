// Service Worker for {{raw-name}}
// Cache version updated at build time: {ts}
const CACHE_NAME = '{{artifact/id}}-v{ts}';
const ASSETS_TO_CACHE = [
  './',
  './index.html',
  './style.css',
  './js/main.js',
  './manifest.json',
  './icon.svg'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(ASSETS_TO_CACHE);
    }).then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames
          .filter((name) => {
            return name.startsWith('{{artifact/id}}-') && name !== CACHE_NAME;
          })
          .map((name) => caches.delete(name))
      );
    }).then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  const req = event.request;
  const url = new URL(req.url);

  // Never cache API calls or non-GET requests
  if (req.method !== 'GET' || url.pathname.startsWith('/api')) {
    return;
  }

  // Network-first with cache fallback for app shell
  event.respondWith(
    fetch(req)
      .then((networkResponse) => {
        if (networkResponse &&
            networkResponse.status === 200 &&
            networkResponse.type === 'basic') {
          const responseToCache = networkResponse.clone();
          caches.open(CACHE_NAME).then((cache) => {
            cache.put(req, responseToCache);
          });
        }
        return networkResponse;
      })
      .catch(() => {
        return caches.match(req, { ignoreSearch: true })
          .then((cachedResponse) => {
            if (cachedResponse) {
              return cachedResponse;
            }
            if (req.mode === 'navigate') {
              return caches.match('./index.html')
                .then((resp) => resp || caches.match('./'));
            }
          });
      })
  );
});
