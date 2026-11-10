// The phone's certificate is self-signed. A service worker that caches this
// page keeps serving "Can't reach the phone" after the browser stops applying
// the certificate exception to the worker's own fetches. Install only to retire
// a worker that was registered by an older page.
self.addEventListener("install", (event) => {
  event.waitUntil(self.skipWaiting());
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(keys.map((key) => caches.delete(key))))
      .then(() => self.registration.unregister()),
  );
});
