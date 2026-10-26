// OrçaPRO — service worker simples. index.html sempre da rede primeiro (atualização instantânea).
const CACHE = 'orcapro-v1';
const ARQUIVOS = ['./', 'index.html', 'html2canvas.min.js', 'jspdf.umd.min.js', 'manifest.webmanifest', 'icon-192.png', 'icon-512.png'];
self.addEventListener('install', e => { e.waitUntil(caches.open(CACHE).then(c => c.addAll(ARQUIVOS).catch(() => {})).then(() => self.skipWaiting())); });
self.addEventListener('activate', e => { e.waitUntil(caches.keys().then(ks => Promise.all(ks.filter(k => k !== CACHE).map(k => caches.delete(k)))).then(() => self.clients.claim())); });
self.addEventListener('fetch', e => {
  const req = e.request;
  if (req.method !== 'GET') return;
  const url = new URL(req.url);
  if (url.origin !== location.origin) return;               // Firebase, Google etc. passam direto
  e.respondWith(fetch(req).then(res => { const cp = res.clone(); caches.open(CACHE).then(c => c.put(req, cp)).catch(() => {}); return res; })
    .catch(() => caches.match(req).then(r => r || caches.match('index.html'))));
});
