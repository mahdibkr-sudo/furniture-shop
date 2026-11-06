"""
Build Master Perfect Mask for Product 1 (مبل راحتی مدل موچی - Mochi Sofa).

Key Elements Handled with Sub-Pixel Precision:
1. High-res base segmentation from raw_mask_1 for natural sectional & chaise curves
2. Full left armchair reconstruction via connected-component fabric segmentation
3. Strict wooden coffee table exclusion confined to exact physical table polygon
4. Background plant pot & leaves exclusion strictly above right sofa backrest (dome curve y >= 464)
5. Elimination of backrest corner gaps and cushion holes
6. Clean subtraction of dark wood plinths and floor tiles
7. Gaussian sub-pixel anti-aliasing with edge padding (alpha=255 at x=0)
8. Injects optimized Base64 PNG mask directly into js/masks-data.js
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
prod1_path = os.path.join(project_dir, 'images', 'product-1.webp')
masks_data_path = os.path.join(project_dir, 'js', 'masks-data.js')
raw_mask_path = r'c:\Users\Mahdi\Desktop\ذذذذذذذذذذذذذ\raw_mask_1.png'

orig_img = Image.open(prod1_path).convert('RGB')
orig = np.array(orig_img)
h, w, _ = orig.shape
yy, xx = np.meshgrid(np.arange(h), np.arange(w), indexing='ij')

r = orig[:, :, 0].astype(int)
g = orig[:, :, 1].astype(int)
b = orig[:, :, 2].astype(int)

# 1. Base Mask from high-res segmentation
if os.path.exists(raw_mask_path):
    mask = np.array(Image.open(raw_mask_path)) > 100
else:
    with open(masks_data_path, 'r', encoding='utf-8') as f:
        js_c = f.read()
    m_cur = re.search(r'1:\s*["\']data:image/png;base64,([^"\']+)["\']', js_c)
    mask = np.array(Image.open(io.BytesIO(base64.b64decode(m_cur.group(1)))))[:, :, 3] > 100

# 2. Add Left Armchair (connected component fabric analysis)
in_armchair_box = (xx <= 220) & (yy >= 455) & (yy <= 845)
is_wood_base = (r < 115) & (g < 85) & (b < 65)
is_wall = (yy < 570) & (r < 170) & (g < 158) & (b < 148) & (r - b < 25)
is_table_zone = (xx >= 120) & (b < 105) & (r - b > 20)
is_top_rim = (xx >= 120) & (yy <= 565)
is_floor = (yy >= 838)

cand_armchair = in_armchair_box & (~is_wood_base) & (~is_wall) & (~is_table_zone) & (~is_top_rim) & (~is_floor)
k5 = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))
closed_armchair = cv2.morphologyEx(cand_armchair.astype(np.uint8), cv2.MORPH_CLOSE, k5)
num_labels, labels, stats, centroids = cv2.connectedComponentsWithStats(closed_armchair)
seat_label = labels[650, 100]
main_armchair = (labels == seat_label)
mask[main_armchair] = True

# 3. Clean Wooden Coffee Table (exact polygon confinement)
table_poly = np.array([
    (124, 520),
    (255, 500),
    (361, 565),
    (363, 734),
    (237, 780),
    (124, 565)
], dtype=np.int32)
table_mask = np.zeros((h, w), dtype=np.uint8)
cv2.fillPoly(table_mask, [table_poly], 1)

is_table_wood = (table_mask == 1) & (b < 120) & (r - b > 15) & (~main_armchair)
mask[is_table_wood] = False

# 4. Clean Plant Pot & Leaves (strictly above right sofa backrest)
dome_y = np.full(w, 464, dtype=int)
for cur_x in range(980, 1050):
    dome_y[cur_x] = int(510 - (cur_x - 980) * (46 / 70))
for cur_x in range(1050, 1385):
    dome_y[cur_x] = int(464 + (cur_x - 1050) * (28 / 335))

is_above_right_backrest = (xx >= 980) & (xx <= 1385) & (yy < dome_y[xx])
mask[is_above_right_backrest] = False

# Middle backrest top (x in [580, 965]):
mask[(xx >= 580) & (xx <= 965) & (yy < 398)] = False
# Corner sofa backrest top (x in [247, 580]):
mask[(xx >= 247) & (xx < 580) & (yy < 405)] = False

# 5. Clean Floor & Dark Wood Plinths
is_dark_wood = (r < 105) & (g < 75) & (b < 60)
mask[(xx < 225) & (yy >= 760) & is_dark_wood] = False
mask[(xx >= 225) & (xx < 800) & (yy >= 680) & is_dark_wood] = False
mask[(xx >= 800) & (yy >= 820) & is_dark_wood] = False

# Floor cutoffs:
mask[(xx < 225) & (yy >= 838)] = False
mask[(xx >= 225) & (xx < 850) & (yy >= 880)] = False
mask[(xx >= 850) & (yy >= 1025)] = False

# 6. Fill internal fabric holes
filled = binary_fill_holes(mask)
filled[is_table_wood] = False
filled[is_above_right_backrest] = False
mask = filled

# 7. Sub-pixel Anti-Aliasing with Edge-Padding
pad_w = 6
padded = np.pad(mask, ((pad_w, pad_w), (pad_w, pad_w)), mode='edge')
alpha_float = gaussian_filter(padded.astype(np.float32), sigma=0.85)
alpha_u8 = np.clip(alpha_float[pad_w:-pad_w, pad_w:-pad_w] * 255.0, 0, 255).astype(np.uint8)

# Post-filter clamp:
alpha_u8[is_table_wood] = 0
alpha_u8[is_above_right_backrest] = 0
alpha_u8[(xx < 225) & (yy >= 838)] = 0
alpha_u8[(xx >= 225) & (xx < 850) & (yy >= 880)] = 0
alpha_u8[(xx >= 850) & (yy >= 1025)] = 0
alpha_u8[(yy >= 720) & is_dark_wood] = 0

print(f"Master mask active pixels: {np.count_nonzero(alpha_u8 > 0)}, solid: {np.count_nonzero(alpha_u8 > 200)}")

# 8. Encode to Base64 PNG
rgba = np.zeros((h, w, 4), dtype=np.uint8)
rgba[:, :, 3] = alpha_u8
mask_out = Image.fromarray(rgba, mode='RGBA')

buf = io.BytesIO()
mask_out.save(buf, format='PNG', optimize=True)
b64_new = base64.b64encode(buf.getvalue()).decode('ascii')
new_data_uri = f"data:image/png;base64,{b64_new}"

# 9. Inject into js/masks-data.js
with open(masks_data_path, 'r', encoding='utf-8') as f:
    js_content = f.read()

pattern = r'(1:\s*["\'])data:image/png;base64,[^"\']+(["\'])'
replacement = rf'\g<1>{new_data_uri}\g<2>'
new_js_content, count = re.subn(pattern, replacement, js_content)

if count == 1:
    with open(masks_data_path, 'w', encoding='utf-8') as f:
        f.write(new_js_content)
    print(f"SUCCESS: Successfully generated and wrote perfected master mask to {masks_data_path}")
else:
    raise RuntimeError(f"Failed to match mask 1 in masks-data.js, matches={count}")
