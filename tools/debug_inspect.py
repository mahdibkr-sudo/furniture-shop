import base64, io, re
from PIL import Image
import numpy as np

with open('js/masks-data.js', 'r', encoding='utf-8') as f:
    text = f.read()

products = {
    '1': 'images/product-1.webp',
    '2': 'images/product-2.webp',
    '3': 'images/product-3.webp',
    '6': 'images/product-4.webp',
    '7': 'images/product-5.webp',
    '8': 'images/product-6.webp'
}

tint_color = np.array([141, 91, 76], dtype=np.float32)

for pid, path in products.items():
    pattern = rf'["\']?{pid}["\']?\s*:\s*["\']data:image/png;base64,([^"\']+)["\']'
    m = re.search(pattern, text)
    if not m:
        print('Not found:', pid)
        continue
    mask_bytes = base64.b64decode(m.group(1))
    mask_img = Image.open(io.BytesIO(mask_bytes)).convert('RGBA')
    orig = Image.open(path).convert('RGB')
    
    mask_img.save(f'debug_mask_{pid}.png')
    
    orig_np = np.array(orig, dtype=np.float32)
    mask_np = np.array(mask_img.resize(orig.size), dtype=np.float32)
    alpha = mask_np[:, :, 3] / 255.0 * 0.94
    
    multiplied = (orig_np * tint_color) / 255.0
    comp = orig_np * (1.0 - alpha[:, :, None]) + multiplied * alpha[:, :, None]
    comp = np.clip(comp, 0, 255).astype(np.uint8)
    Image.fromarray(comp).save(f'debug_comp_{pid}.png')
    print(f'Saved debug_comp_{pid}.png')
