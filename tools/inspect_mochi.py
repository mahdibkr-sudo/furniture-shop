import base64
import re
from PIL import Image
import io
import numpy as np

with open(r'c:\Users\Mahdi\Desktop\بببببب\js\masks-data.js', 'r', encoding='utf-8') as f:
    content = f.read()

m = re.search(r'1:\s*"data:image/png;base64,([^"]+)"', content)
if m:
    b64_data = m.group(1)
    mask_bytes = base64.b64decode(b64_data)
    mask_img = Image.open(io.BytesIO(mask_bytes))
    mask_img.save(r'c:\Users\Mahdi\Desktop\بببببب\current_mask_1.png')
    print('Current mask 1 extracted! Size:', mask_img.size, 'Mode:', mask_img.mode)
else:
    print('Could not find mask for 1')

orig1 = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp')
print('Product 1 orig size:', orig1.size, 'Mode:', orig1.mode)
