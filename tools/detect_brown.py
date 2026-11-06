from PIL import Image
import numpy as np

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
orig_np = np.array(orig)

# Let's crop x: 200 to 700, y: 400 to 900
crop = orig_np[450:850, 250:650].copy()

# Find the brown color:
# In the table, R is between 80 and 190, G is between 40 and 120, B is between 20 and 80, and R - B > 40
r = crop[:, :, 0].astype(int)
g = crop[:, :, 1].astype(int)
b = crop[:, :, 2].astype(int)

is_brown = (r > 70) & (r < 210) & (g > 35) & (g < 140) & (b > 15) & (b < 95) & (r - b > 40)

# Highlight brown pixels in red
vis = crop.copy()
vis[is_brown] = [255, 0, 0]

Image.fromarray(vis).save(r'c:\Users\Mahdi\Desktop\بببببب\table_detected_brown.png')
print("Brown detected pixel count in crop:", np.sum(is_brown))
