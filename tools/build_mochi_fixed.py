import numpy as np
from PIL import Image, ImageDraw
import cv2
from scipy.ndimage import gaussian_filter, binary_opening, binary_closing

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
orig_np = np.array(orig)
h, w, _ = orig_np.shape

# Load raw rembg mask
raw_mask = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\raw_mask_1.png')
raw_np = np.array(raw_mask) > 100

# Start with mask from rembg
mask = raw_np.copy()

# -------------------------------------------------------------
# 1. ADD LEFT ARMCHAIR
# -------------------------------------------------------------
# Armchair polygon coordinates:
armchair_polygon = [
    (0, 427),
    (15, 426),
    (35, 426),
    (50, 436),
    (65, 458),
    (75, 488),
    (85, 528),
    (90, 560),
    (92, 570),
    (110, 565),
    (126, 560),
    (138, 580),
    (147, 600),
    (152, 620),
    (154, 640),
    (154, 660),
    (152, 680),
    (149, 700),
    (143, 720),
    (135, 740),
    (128, 750),
    (100, 758),
    (75, 765),
    (50, 772),
    (25, 777),
    (0, 782)
]

armchair_mask = np.zeros((h, w), dtype=np.uint8)
cv2.fillPoly(armchair_mask, [np.array(armchair_polygon, dtype=np.int32)], 1)

# Add armchair to main mask
mask[armchair_mask == 1] = True

# -------------------------------------------------------------
# 2. SUBTRACT BROWN WOODEN TABLE
# -------------------------------------------------------------
# Exact table polygon:
# Top back-left: (124, 523)
# Top back-right (meets corner sofa): (247, 469)
# Top front-right rim: (362, 532)
# Bottom front-right on floor: (372, 730)
# Bottom front-left on floor: (237, 764)
# Left corner meeting armchair at bottom: (126, 560)
table_polygon = [
    (124, 523),
    (247, 469),
    (363, 532),
    (373, 732),
    (236, 765),
    (126, 560)
]

table_mask = np.zeros((h, w), dtype=np.uint8)
cv2.fillPoly(table_mask, [np.array(table_polygon, dtype=np.int32)], 1)

# Subtract table from mask
mask[table_mask == 1] = False

# Also any brown wood pixels around the table boundary (in case of slight bevel edges)
r = orig_np[:, :, 0].astype(int)
g = orig_np[:, :, 1].astype(int)
b = orig_np[:, :, 2].astype(int)

# Detect wood tones in table vicinity [x: 120..385, y: 460..780]
yy, xx = np.meshgrid(np.arange(h), np.arange(w), indexing='ij')
in_table_box = (xx >= 120) & (xx <= 385) & (yy >= 465) & (yy <= 775)
is_wood_brown = (r > 65) & (r < 235) & (g > 30) & (g < 170) & (b > 10) & (b < 135) & (r - b > 28) & (r - g > 12)
mask[in_table_box & is_wood_brown] = False

# -------------------------------------------------------------
# 3. SUBTRACT DARK WOOD PLINTHS (BASES) & FLOOR
# -------------------------------------------------------------
# Under armchair: y >= 750
is_dark_wood = (r < 90) & (g < 65) & (b < 50)
mask[(xx < 150) & (yy >= 745) & is_dark_wood] = False

# Under corner sofa & middle sofas: dark wood plinth
mask[(yy >= 620) & is_dark_wood] = False
mask[yy >= 860] = False # Floor cutoff

# -------------------------------------------------------------
# 4. SUBTRACT PLANT POT (BEHIND RIGHT SOFA)
# -------------------------------------------------------------
# Plant pot is at x in [980, 1150], y in [380, 470]
# Color of the cyan pot in original image:
# Let's inspect pot color or cut it out
mask[(xx >= 980) & (xx <= 1160) & (yy <= 470)] = False

# -------------------------------------------------------------
# 5. SMOOTH EDGES (FEATHERING)
# -------------------------------------------------------------
mask_clean = binary_closing(mask, structure=np.ones((5, 5)))
mask_clean = binary_opening(mask_clean, structure=np.ones((3, 3)))

alpha = gaussian_filter(mask_clean.astype(np.float32), sigma=1.0)
alpha_u8 = np.clip(alpha * 255.0, 0, 255).astype(np.uint8)

# -------------------------------------------------------------
# 6. SAVE TEST COMPOSITES FOR INSPECTION
# -------------------------------------------------------------
# Save full highlight
highlight = orig_np.copy()
highlight[alpha_u8 > 128] = (highlight[alpha_u8 > 128].astype(float) * 0.35 + np.array([0, 210, 255]) * 0.65).astype(np.uint8)
highlight_img = Image.fromarray(highlight)
highlight_img.save(r'c:\Users\Mahdi\Desktop\بببببب\mochi_fixed_full_highlight.png')

# Save zoom on left side (armchair + table + corner sofa)
crop_left = highlight_img.crop((0, 320, 600, 850))
crop_left.save(r'c:\Users\Mahdi\Desktop\بببببب\mochi_fixed_left_zoom.png')

# Save realistic fabric tint (blue & warm brown)
def blend_multiply(orig_rgb, tint_rgb, a):
    tint_arr = np.array(tint_rgb, dtype=np.float32)
    orig_f = orig_rgb.astype(np.float32)
    multiplied = (orig_f * tint_arr) / 255.0
    af = a[:, :, None] / 255.0
    res = orig_f * (1.0 - af) + multiplied * af
    return np.clip(res, 0, 255).astype(np.uint8)

blue_comp = blend_multiply(orig_np, [60, 120, 210], alpha_u8)
Image.fromarray(blue_comp).save(r'c:\Users\Mahdi\Desktop\بببببب\mochi_blue_comp.png')
Image.fromarray(blue_comp).crop((0, 320, 600, 850)).save(r'c:\Users\Mahdi\Desktop\بببببب\mochi_blue_left_zoom.png')

print("All fixed mochi images saved successfully!")
