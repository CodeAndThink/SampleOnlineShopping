"""Local-only static server and catalog proxy. No third-party dependencies."""
import argparse
import http.client
import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlsplit

ROOT = Path(__file__).parent
FILES = {'/': ('index.html', 'text/html'), '/app.js': ('app.js', 'text/javascript'), '/style.css': ('style.css', 'text/css')}

class Handler(BaseHTTPRequestHandler):
    def respond(self, status, data, content_type):
        self.send_response(status)
        self.send_header('Content-Type', content_type + '; charset=utf-8')
        self.send_header('Content-Length', str(len(data)))
        self.send_header('Cache-Control', 'no-store')
        self.end_headers()
        self.wfile.write(data)

    def handle_request(self):
        path = urlsplit(self.path).path
        if path == '/api/products' and self.command in ('GET', 'POST'):
            if self.command == 'POST' and self.headers.get('Origin') != 'http://localhost:' + str(self.server.server_port):
                return self.respond(403, b'Invalid origin', 'text/plain')
            length = int(self.headers.get('Content-Length', '0'))
            if length > 65536:
                return self.respond(413, b'Body too large', 'text/plain')
            headers = {k: self.headers[k] for k in ('Authorization', 'Content-Type') if k in self.headers}
            conn = http.client.HTTPConnection('localhost', self.server.backend_port, timeout=10)
            try:
                conn.request(self.command, self.path, body=self.rfile.read(length) if length else None, headers=headers)
                response = conn.getresponse()
                self.respond(response.status, response.read(), response.getheader('Content-Type', 'application/json').split(';')[0])
            except (OSError, http.client.HTTPException):
                self.respond(502, json.dumps({'error': 'Không kết nối được catalog. Kiểm tra server và --backend-port.'}).encode(), 'application/json')
            finally:
                conn.close()
        elif self.command == 'GET' and path in FILES:
            filename, mime = FILES[path]
            self.respond(200, (ROOT / filename).read_bytes(), mime)
        else:
            self.respond(404, b'Not found', 'text/plain')

    do_GET = handle_request
    do_POST = handle_request

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--port', type=int, default=5173)
    parser.add_argument('--backend-port', type=int, default=8080)
    args = parser.parse_args()
    server = ThreadingHTTPServer(('127.0.0.1', args.port), Handler)
    server.backend_port = args.backend_port
    print(f'Mở http://localhost:{args.port} • Catalog localhost:{args.backend_port}', flush=True)
    server.serve_forever()
