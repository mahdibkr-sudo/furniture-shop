import numpy as np
from PIL import Image, ImageDraw

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
orig_np = np.array(orig)

# Crop left armchair
x1, y1, x2, y2 = 0, 320, 260, 920
arm_crop = orig_np[y1:y2, x1:x2].copy()

arm_img = Image.fromarray(arm_crop)
draw = ImageDraw.Draw(arm_img)

for y in range(0, y2 - y1, 20):
    draw.line([(0, y), (x2 - x1, y)], fill=(255, 0, 0, 100))
    draw.text((2, y + 2), str(y1 + y), fill=(255, 255, 0))

for x in range(0, x2 - x1, 20):
    draw.line([(x, 0), (x, y2 - y1)], fill=(255, 0, 0, 100))
    draw.text((x + 2, 2), str(x1 + x), fill=(255, 255, 0))

arm_img.save(r'c:\Users\Mahdi\Desktop\بببببب\armchair_detailed_grid.png')
print("Saved armchair_detailed_grid.png")
