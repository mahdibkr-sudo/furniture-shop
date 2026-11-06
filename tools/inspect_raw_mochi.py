from PIL import Image
import numpy as np

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
raw_mask = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\raw_mask_1.png')

orig_np = np.array(orig)
raw_np = np.array(raw_mask)

# Save visual inspection
highlight = orig_np.copy()
# Highlight in cyan
highlight[raw_np > 100] = (highlight[raw_np > 100].astype(float) * 0.4 + np.array([0, 200, 255]) * 0.6).astype(np.uint8)

highlight_img = Image.fromarray(highlight)
highlight_img.save(r'c:\Users\Mahdi\Desktop\بببببب\raw_highlight_1.png')

# Also crop left
crop_left = highlight_img.crop((0, 200, 750, 950))
crop_left.save(r'c:\Users\Mahdi\Desktop\بببببب\raw_crop_left_1.png')

print("Saved raw_highlight_1.png and raw_crop_left_1.png")
