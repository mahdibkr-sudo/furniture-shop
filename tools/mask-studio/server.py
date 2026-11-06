"""
Mask Studio Local Backend Server
Serves the Mask Studio Web UI and handles 1-click automatic saving to js/masks-data.js.
Uses standard Python library (no external pip dependencies required).
"""

import os
import sys
import json
import re
import io
import base64
import webbrowser
from http.server import HTTPServer, SimpleHTTPRequestHandler
import socket

PORT = 8090

# Set project root (two levels up from tools/mask-studio)
STUDIO_DIR = os.path.dirname(os.path.abspath(__file__))
TOOLS_DIR = os.path.dirname(STUDIO_DIR)
PROJECT_DIR = os.path.dirname(TOOLS_DIR)

MASKS_DATA_PATH = os.path.join(PROJECT_DIR, 'js', 'masks-data.js')

class StudioHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=PROJECT_DIR, **kwargs)

    def do_GET(self):
        # Redirect root to mask studio
        if self.path == '/' or self.path == '/index.html':
            self.send_response(302)
            self.send_header('Location', '/tools/mask-studio/index.html')
            self.end_headers()
            return

        if self.path == '/api/products':
            self.handle_get_products()
            return

        super().do_GET()

    def do_POST(self):
        if self.path == '/api/save-mask':
            self.handle_save_mask()
            return

        self.send_error(404, "Endpoint not found")

    def handle_get_products(self):
        try:
            # Exact catalog mappings matching js/script.js
            products = [
                {'id': 1, 'name': 'مبل راحتی مدل موچی', 'filename': 'product-1.webp', 'imagePath': '/images/product-1.webp'},
                {'id': 2, 'name': 'مبل راحتی مدل رادیس', 'filename': 'product-2.webp', 'imagePath': '/images/product-2.webp'},
                {'id': 3, 'name': 'مبل راحتی مدل ال تانیش', 'filename': 'product-3.webp', 'imagePath': '/images/product-3.webp'},
                {'id': 6, 'name': 'مبل راحتی مدل ایفوریا', 'filename': 'product-4.webp', 'imagePath': '/images/product-4.webp'},
                {'id': 7, 'name': 'مبل تکنفره ست موچی', 'filename': 'product-5.webp', 'imagePath': '/images/product-5.webp'},
                {'id': 8, 'name': 'مبل تکنفره ست ال تانیش', 'filename': 'product-6.webp', 'imagePath': '/images/product-6.webp'},
            ]

            # Check existing masks in masks-data.js
            existing_mask_keys = []
            if os.path.exists(MASKS_DATA_PATH):
                with open(MASKS_DATA_PATH, 'r', encoding='utf-8') as js_f:
                    js_c = js_f.read()
                matches = re.findall(r'(\d+)\s*:\s*["\']data:image/png;base64', js_c)
                existing_mask_keys = [int(k) for k in matches]

            for p in products:
                p['hasMask'] = p['id'] in existing_mask_keys

            response_data = {
                'success': True,
                'products': products
            }
            self.send_json(response_data)
        except Exception as e:
            self.send_json({'success': False, 'error': str(e)}, status=500)

    def handle_save_mask(self):
        try:
            content_length = int(self.headers.get('Content-Length', 0))
            post_body = self.rfile.read(content_length)
            req = json.loads(post_body.decode('utf-8'))

            prod_id = str(req.get('productId'))
            mask_data = req.get('maskDataUri', '').strip()

            if not prod_id or not mask_data.startswith('data:image/png;base64,'):
                self.send_json({'success': False, 'error': 'Invalid product ID or mask data URI'}, status=400)
                return

            if not os.path.exists(MASKS_DATA_PATH):
                self.send_json({'success': False, 'error': f'masks-data.js not found at {MASKS_DATA_PATH}'}, status=500)
                return

            with open(MASKS_DATA_PATH, 'r', encoding='utf-8') as js_f:
                js_content = js_f.read()

            # Backup
            bak_path = MASKS_DATA_PATH + '.bak'
            with open(bak_path, 'w', encoding='utf-8') as bak_f:
                bak_f.write(js_content)

            # Check if key exists in masks-data.js
            pattern = rf'({prod_id}\s*:\s*["\'])data:image/png;base64,[^"\']+(["\'])'
            if re.search(pattern, js_content):
                new_js, count = re.subn(pattern, rf'\g<1>{mask_data}\g<2>', js_content)
            else:
                # Insert key into sofaMasks object
                obj_pattern = r'(window\.sofaMasks\s*=\s*\{)'
                if re.search(obj_pattern, js_content):
                    replacement = rf'\g<1>\n    {prod_id}: "{mask_data}",'
                    new_js, count = re.subn(obj_pattern, replacement, js_content, count=1)
                else:
                    self.send_json({'success': False, 'error': 'Could not find window.sofaMasks in masks-data.js'}, status=500)
                    return

            with open(MASKS_DATA_PATH, 'w', encoding='utf-8') as js_f:
                js_f.write(new_js)

            # Calculate size
            b64_part = mask_data.split(',', 1)[1]
            raw_bytes = base64.b64decode(b64_part)

            self.send_json({
                'success': True,
                'message': f'ماسک محصول {prod_id} با موفقیت در masks-data.js ذخیره شد!',
                'productId': prod_id,
                'fileSizeBytes': len(raw_bytes),
                'savedPath': MASKS_DATA_PATH
            })
        except Exception as e:
            self.send_json({'success': False, 'error': str(e)}, status=500)

    def send_json(self, data, status=200):
        body = json.dumps(data, ensure_ascii=False).encode('utf-8')
        self.send_response(status)
        self.send_header('Content-Type', 'application/json; charset=utf-8')
        self.send_header('Content-Length', str(len(body)))
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        self.end_headers()
        self.wfile.write(body)

def find_available_port(start_port=8090):
    port = start_port
    while port < start_port + 50:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            if s.connect_ex(('127.0.0.1', port)) != 0:
                return port
            port += 1
    return start_port

def main():
    port = find_available_port(PORT)
    server_address = ('127.0.0.1', port)
    httpd = HTTPServer(server_address, StudioHandler)
    url = f"http://localhost:{port}/tools/mask-studio/index.html"
    
    print("=" * 65)
    print("  استودیوی گرافیکی ویرایش ماسک مبل (Sofa Mask Studio)")
    print("=" * 65)
    print(f"  سرور فعال شد: {url}")
    print(f"  پوشه پروژه: {PROJECT_DIR}")
    print(f"  مسیر ذخیره ماسک‌ها: {MASKS_DATA_PATH}")
    print("=" * 65)
    print("  برای خروج Ctrl+C را فشار دهید.")
    print("=" * 65)
    
    try:
        webbrowser.open(url)
    except Exception:
        pass
    
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nسرور متوقف شد.")
        httpd.server_close()

if __name__ == '__main__':
    main()
