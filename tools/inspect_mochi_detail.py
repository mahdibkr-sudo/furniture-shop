import numpy as np
from PIL import Image

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
mask = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\current_mask_1.png')

orig_np = np.array(orig)
mask_np = np.array(mask)
alpha = mask_np[:, :, 3]

# Create a visual highlight of the current mask (in green or cyan)
highlight = orig_np.copy()
# Highlight masked pixels in bright cyan/blue
highlight[alpha > 128] = (highlight[alpha > 128].astype(float) * 0.4 + np.array([0, 200, 255]) * 0.6).astype(np.uint8)

highlight_img = Image.fromarray(highlight)
highlight_img.save(r'c:\Users\Mahdi\Desktop\بببببب\mochi_current_highlight.png')

# Also crop the left side (e.g. x from 0 to 800, y from 200 to 900)
crop_left = highlight_img.crop((0, 200, 750, 950))
crop_left.save(r'c:\Users\Mahdi\Desktop\بببببب\mochi_crop_left.png')

# Also save original crop for reference
orig.crop((0, 200, 750, 950)).save(r'c:\Users\Mahdi\Desktop\بببببب\mochi_crop_left_orig.png')

print("Saved mochi_current_highlight.png and mochi_crop_left.png")
