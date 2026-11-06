import numpy as np
from PIL import Image

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
orig_np = np.array(orig)

h, w, _ = orig_np.shape
print(f"Image shape: {h}x{w}")

# Let's inspect the bounding box of the brown table
# We can find where the brown table is by checking RGB values or visual grid
# Let's save a grid overlay of the left region [x: 0..650, y: 250..950] with coordinate markers!

crop = orig_np[250:950, 0:650].copy()
# Draw grid every 50 pixels
for y in range(0, crop.shape[0], 50):
    crop[y, :, :] = [255, 0, 0]
for x in range(0, crop.shape[1], 50):
    crop[:, x, :] = [255, 0, 0]

# Add labels using PIL
grid_img = Image.fromarray(crop)
grid_img.save(r'c:\Users\Mahdi\Desktop\بببببب\grid_left.png')
print("Grid saved as grid_left.png (x offset: 0, y offset: 250)")
