const CACHE = "lantext-shell-v11";
const SHELL = ["/", "/index.html", "/app.js", "/styles.css", "/favicon.svg"];

self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(CACHE).then(async (cache) => {
      for (const url of SHELL) {
        const res = await fetch(url, { cache: "no-store" });
        if (!res.ok) throw new Error("shell " + url);
        await cache.put(url, res);
      }
    }).then(() => self.skipWaiting()),
  );
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.filter((k) => k !== CACHE).map((k) => caches.delete(k))),
    ).then(() => self.clients.claim()),
  );
});

self.addEventListener("fetch", (event) => {
  if (event.request.method !== "GET") return;
  const url = new URL(event.request.url);
  if (url.origin !== self.location.origin) return;
  if (url.pathname === "/sw.js" || url.pathname.startsWith("/api/")) return;

  const isShell =
    event.request.mode === "navigate" ||
    url.pathname === "/" ||
    url.pathname === "/index.html" ||
    SHELL.includes(url.pathname);

  if (!isShell) return;

  event.respondWith(networkThenCache(event.request, url.pathname));
});

async function networkThenCache(request, pathname) {
  try {
    const res = await fetch(request, { signal: AbortSignal.timeout(8000) });
    if (res.ok) {
      const cache = await caches.open(CACHE);
      cache.put(request, res.clone());
      if (pathname === "/" || pathname === "/index.html") {
        cache.put("/", res.clone());
        cache.put("/index.html", res.clone());
      }
      return res;
    }
    const cached = await fromCache(request);
    if (cached) return cached;
    if (request.mode === "navigate") return offlinePage();
    return res;
  } catch (_) {
    const cached = await fromCache(request);
    if (cached) return cached;
    if (request.mode === "navigate") return offlinePage();
    throw _;
  }
}

function offlinePage() {
  return new Response(
    "<!DOCTYPE html><html><head><meta charset=utf-8><title>LanText</title></head><body style='font-family:system-ui;padding:48px;text-align:center'><h1>Can't reach the phone</h1><p>Reload this tab once the phone is on the LAN.</p><p><button onclick='location.reload()'>Reload</button></p></body></html>",
    { headers: { "Content-Type": "text/html; charset=utf-8" } },
  );
}

async function fromCache(request) {
  const cache = await caches.open(CACHE);
  return (
    (await cache.match(request, { ignoreSearch: true })) ||
    (await cache.match("/index.html")) ||
    (await cache.match("/"))
  );
}
