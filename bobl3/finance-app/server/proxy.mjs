import http from 'http';
import { URL } from 'url';

const PORT = 3001;
const HOST = '127.0.0.1';

const server = http.createServer(async (req, res) => {
  // Set CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Accept');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const reqUrl = new URL(req.url || '/', `http://${HOST}:${PORT}`);

  // Route: /api/finance/chart/:symbol or /api/chart?symbol=...
  if (reqUrl.pathname.startsWith('/api/finance/chart/') || reqUrl.pathname.startsWith('/api/chart')) {
    let symbol = '';
    if (reqUrl.pathname.startsWith('/api/finance/chart/')) {
      symbol = reqUrl.pathname.replace('/api/finance/chart/', '').trim();
    } else {
      symbol = reqUrl.searchParams.get('symbol') || '';
    }

    if (!symbol) {
      res.writeHead(400, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: 'Missing symbol parameter' }));
      return;
    }

    const range = reqUrl.searchParams.get('range') || '1d';
    const interval = reqUrl.searchParams.get('interval') || '5m';

    const yahooUrl = `https://query1.finance.yahoo.com/v8/finance/chart/${encodeURIComponent(symbol)}?range=${encodeURIComponent(range)}&interval=${encodeURIComponent(interval)}&includePrePost=false`;

    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 5000);

      const yahooRes = await fetch(yahooUrl, {
        signal: controller.signal,
        headers: {
          'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko)',
          'Accept': 'application/json'
        }
      });

      clearTimeout(timeoutId);

      const data = await yahooRes.text();
      res.writeHead(yahooRes.status, { 'Content-Type': 'application/json' });
      res.end(data);
    } catch (err) {
      res.writeHead(502, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: 'Proxy upstream failure', message: err instanceof Error ? err.message : String(err) }));
    }
    return;
  }

  // Health check route
  if (reqUrl.pathname === '/health' || reqUrl.pathname === '/') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ status: 'ok', service: 'finance-backend-proxy' }));
    return;
  }

  res.writeHead(404, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify({ error: 'Not Found' }));
});

server.listen(PORT, HOST, () => {
  console.log(`Finance API proxy running on http://${HOST}:${PORT}`);
});
