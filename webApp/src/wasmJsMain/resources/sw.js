self.addEventListener('install', (event) => {
    self.skipWaiting(); // Force worker to activate instantly
});

self.addEventListener('activate', (event) => {
    event.waitUntil(self.clients.claim()); // Take over the page immediately
});

self.addEventListener('fetch', (event) => {
    if (event.request.cache === 'only-if-cached' && event.request.mode !== 'same-origin') {
        return;
    }
    
    event.respondWith(
        fetch(event.request).then((response) => {
            // Copy the original response so we can modify the headers
            const newHeaders = new Headers(response.headers);
            
            // Inject the required Cross-Origin headers!
            newHeaders.set('Cross-Origin-Embedder-Policy', 'require-corp');
            newHeaders.set('Cross-Origin-Opener-Policy', 'same-origin');
            
            return new Response(response.body, {
                status: response.status,
                statusText: response.statusText,
                headers: newHeaders
            });
        }).catch(e => console.error(e))
    );
});