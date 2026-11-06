import numpy as np
from PIL import Image
import cv2

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
orig_np = np.array(orig)

# Let's crop the left region [0:880, 0:260]
h, w, _ = orig_np.shape
arm_mask = np.zeros((h, w), dtype=np.uint8)

# The left armchair is located in x: [0, 210], y: [340, 805]
# Let's inspect RGB/HSV in this region:
# Background wall (grey): R~130-150, G~125-145, B~120-140. Saturation is very low (< 0.12).
# Armchair fabric:
# Backrest: R~150-170, G~140-160, B~135-155 (shaded top), but textured and warmer than wall
# Seat: R~210-245, G~185-225, B~160-200. Very high brightness and warm tint.
# Wood base: R~50-70, G~35-45, B~20-30. Very dark (brightness < 60).
# Floor: R~200-220, G~195-215, B~190-210 (neutral grey/tile, low saturation).

# Let's test a GrabCut on the left armchair!
# GrabCut with a bounding box around the armchair:
# rect = (x, y, w, h)
rect = (0, 340, 215, 465) # covers the armchair down to y=805
mask_gc = np.zeros((h, w), np.uint8)
bgdModel = np.zeros((1, 65), np.float64)
fgdModel = np.zeros((1, 65), np.float64)

# Run grabcut with rect
cv2.grabCut(orig_np, mask_gc, rect, bgdModel, fgdModel, 5, cv2.GC_INIT_WITH_RECT)
mask_armchair = np.where((mask_gc == 2) | (mask_gc == 0), 0, 1).astype('uint8')

# Exclude wood base: y > 765 where R < 90 or L < 80
gray = cv2.cvtColor(orig_np, cv2.COLOR_RGB2GRAY)
mask_armchair[(gray < 80) & (np.arange(h)[:, None] > 750)] = 0

# Save visualization of GrabCut result on left armchair
vis = orig_np.copy()
vis[mask_armchair == 1] = (vis[mask_armchair == 1].astype(float) * 0.4 + np.array([0, 255, 0]) * 0.6).astype(np.uint8)
Image.fromarray(vis[:950, :400]).save(r'c:\Users\Mahdi\Desktop\بببببب\test_armchair_grabcut.png')

print("Grabcut test done, saved test_armchair_grabcut.png")
