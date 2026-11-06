"""
Build Master Perfect Mask for Product 2 (مبل راحتی مدل رادیس - Radis Sofa Model).

Key Elements Handled with Sub-Pixel Precision:
1. High-resolution organic contours for all 5 furniture modules:
   - Left 2-seater sofa (white bouclé) with complete backrest and seat
   - Middle modular sofa (taupe fabric) with full left shoulder and complete seat under tray
   - Right loveseat sofa (taupe fabric) with full curved backrest and front edge
   - Front-left round armchair (white bouclé) spanning full natural width (x in [59, 522])
   - Front-right round armchair (white bouclé) spanning full natural width (x in [896, 1358])
2. 100% strict exclusion (zero bleeding, alpha = 0):
   - Top ceiling and wall (y < 288, right wall slats)
   - Middle wooden console table strictly bounded to y <= 615 (x in [488, 555])
   - Middle wooden tray table & samovar strictly bounded to y <= 465 (x in [695, 785])
   - The 5 armrest wooden shelves
   - Front armchair vertical wooden straps (FL: x in [232, 267]; FR: x in [1142, 1177])
   - Armchair dark wood feet / bottom floor shadows (y >= 995 with dark color signature)
   - Bottom tile floor (y >= 1035)
3. Sub-pixel edge-padded Gaussian blur anti-aliasing (sigma=0.85, mode='edge')
4. Injects optimized Base64 RGBA PNG mask directly into js/masks-data.js
"""

import os
import io
import re
import base64
import numpy as np
from PIL import Image
import cv2
from scipy.ndimage import gaussian_filter, binary_fill_holes

project_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
prod2_path = os.path.join(project_dir, 'images', 'product-2.webp')
masks_data_path = os.path.join(project_dir, 'js', 'masks-data.js')
backup_masks_path = r'c:\Users\Mahdi\Desktop\بببببب_backup_fabric_customizer\js\masks-data.js'

orig_img = Image.open(prod2_path).convert('RGB')
orig = np.array(orig_img)
h, w, _ = orig.shape
yy, xx = np.meshgrid(np.arange(h), np.arange(w), indexing='ij')

r = orig[:, :, 0].astype(int)
g = orig[:, :, 1].astype(int)
b = orig[:, :, 2].astype(int)

# 1. Base Mask: Load original clean silhouette from backup if available, else current
if os.path.exists(backup_masks_path):
    with open(backup_masks_path, 'r', encoding='utf-8') as f:
        c_bak = f.read()
    m_bak = re.search(r'2:\s*["\']data:image/png;base64,([^"\']+)["\']', c_bak)
    base_alpha_raw = np.array(Image.open(io.BytesIO(base64.b64decode(m_bak.group(1)))))[..., 3]
    mask_u8 = (np.array(Image.fromarray(base_alpha_raw).resize((w, h), Image.Resampling.LANCZOS)) > 80).astype(np.uint8)
else:
    with open(masks_data_path, 'r', encoding='utf-8') as f:
        c_cur = f.read()
    m_cur = re.search(r'2:\s*["\']data:image/png;base64,([^"\']+)["\']', c_cur)
    base_alpha_raw = np.array(Image.open(io.BytesIO(base64.b64decode(m_cur.group(1)))))[..., 3]
    mask_u8 = (base_alpha_raw > 80).astype(np.uint8)

# -------------------------------------------------------------
# 2. FILL FABRIC REGIONS ONLY
# -------------------------------------------------------------
# (A) Middle sofa left cushion shoulder:
is_taupe = (r >= 100) & (r <= 200) & (g >= 70) & (g <= 170) & (b >= 50) & (b <= 150)
mask_u8[(yy >= 335) & (yy <= 425) & (xx >= 500) & (xx <= 560) & is_taupe] = 1

# (B) Middle sofa seat cushion under tray (y in [465, 560], x in [690, 790]):
mask_u8[(yy >= 465) & (yy <= 560) & (xx >= 690) & (xx <= 790) & is_taupe] = 1

# (C) Fill small holes inside each furniture piece individually:
for y1, y2, x1, x2 in [
    (300, 620, 0, 488),
    (288, 570, 500, 980),
    (320, 650, 980, 1448),
    (650, 1010, 40, 530),
    (650, 1010, 910, 1330)
]:
    sub = mask_u8[y1:y2, x1:x2]
    mask_u8[y1:y2, x1:x2] = binary_fill_holes(sub).astype(np.uint8)

# -------------------------------------------------------------
# 3. SUBTRACT NON-FABRIC REGIONS (WOOD, WALL, FEET)
# -------------------------------------------------------------
# A. Top Wall:
mask_u8[yy < 288] = 0
mask_u8[(xx >= 1430) & (yy <= 430)] = 0
mask_u8[xx >= 1445] = 0

# B. Middle Console Table (strictly y <= 615, preserving armchair at y >= 700):
console_poly = np.array([
    (488, 412), (530, 412), (555, 437), (555, 615), (488, 615)
], dtype=np.int32)
cv2.fillPoly(mask_u8, [console_poly], 0)

# C. Middle Tray Table & Samovar (strictly y <= 465, preserving fabric seat at y >= 465):
tray_poly = np.array([
    (695, 310), (785, 310), (785, 465), (695, 465)
], dtype=np.int32)
cv2.fillPoly(mask_u8, [tray_poly], 0)

# D. The 5 Armrest Wooden Shelves:
shelf1 = np.array([(0, 498), (72, 498), (72, 525), (0, 525)], dtype=np.int32)
shelf2 = np.array([(440, 428), (488, 428), (488, 455), (440, 455)], dtype=np.int32)
shelf3 = np.array([(980, 412), (1035, 412), (1035, 435), (980, 435)], dtype=np.int32)
shelf4 = np.array([(1005, 455), (1072, 455), (1072, 495), (1005, 495)], dtype=np.int32)
shelf5 = np.array([(1270, 535), (1370, 535), (1370, 570), (1270, 570)], dtype=np.int32)
for s in [shelf1, shelf2, shelf3, shelf4, shelf5]:
    cv2.fillPoly(mask_u8, [s], 0)

# E. Armchair Wooden Straps:
strap_fl = np.array([
    (232, 852), (240, 843), (248, 841), (256, 843), (267, 852),
    (267, 1005), (232, 1005)
], dtype=np.int32)
strap_fr = np.array([
    (1142, 853), (1150, 846), (1158, 844), (1166, 846), (1177, 853),
    (1177, 1005), (1142, 1005)
], dtype=np.int32)
cv2.fillPoly(mask_u8, [strap_fl, strap_fr], 0)

# F. Armchair Dark Feet / Floor Shadows at the very bottom:
is_dark_feet = (r < 75) & (g < 60) & (b < 45)
mask_u8[(yy >= 995) & (xx >= 40) & (xx <= 530) & is_dark_feet] = 0
mask_u8[(yy >= 995) & (xx >= 910) & (xx <= 1330) & is_dark_feet] = 0
mask_u8[yy >= 1035] = 0

# -------------------------------------------------------------
# 4. SUB-PIXEL EDGE-PADDED ANTI-ALIASING
# -------------------------------------------------------------
pad = 12
padded = np.pad(mask_u8.astype(np.float32), ((pad, pad), (pad, pad)), mode='edge')
smoothed = gaussian_filter(padded, sigma=0.85)
alpha_u8 = np.clip(smoothed[pad:-pad, pad:-pad] * 255.0, 0, 255).astype(np.uint8)

alpha_u8[alpha_u8 > 245] = 255
alpha_u8[alpha_u8 < 8] = 0

# Strict clamps on hard wooden cutouts
cv2.fillPoly(alpha_u8, [console_poly, tray_poly, shelf1, shelf2, shelf3, shelf4, shelf5, strap_fl, strap_fr], 0)
alpha_u8[yy < 288] = 0
alpha_u8[yy >= 1035] = 0

print(f"Master Radis Mask: active={np.count_nonzero(alpha_u8 > 0)}, solid={np.count_nonzero(alpha_u8 > 200)}")

# -------------------------------------------------------------
# 5. ENCODE TO BASE64 RGBA PNG & INJECT INTO MASKS-DATA.JS
# -------------------------------------------------------------
rgba = np.zeros((h, w, 4), dtype=np.uint8)
rgba[:, :, 3] = alpha_u8
mask_out = Image.fromarray(rgba, mode='RGBA')

buf = io.BytesIO()
mask_out.save(buf, format='PNG', optimize=True)
b64_new = base64.b64encode(buf.getvalue()).decode('ascii')
new_data_uri = f"data:image/png;base64,{b64_new}"

with open(masks_data_path, 'r', encoding='utf-8') as f:
    js_content = f.read()

pattern = r'(2:\s*["\'])data:image/png;base64,[^"\']+(["\'])'
replacement = rf'\g<1>{new_data_uri}\g<2>'
new_js_content, count = re.subn(pattern, replacement, js_content)

if count == 1:
    with open(masks_data_path, 'w', encoding='utf-8') as f:
        f.write(new_js_content)
    print(f"SUCCESS: Successfully generated and wrote perfected master Radis mask to {masks_data_path}")
else:
    raise RuntimeError(f"Failed to match mask 2 in masks-data.js, matches={count}")
