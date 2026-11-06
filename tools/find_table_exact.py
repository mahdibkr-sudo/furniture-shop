from PIL import Image
import numpy as np

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
orig_np = np.array(orig)

# Crop from x: 100 to 450, y: 400 to 800
x_off, y_off = 100, 400
crop = orig_np[400:800, 100:450].copy()

r = crop[:, :, 0].astype(int)
g = crop[:, :, 1].astype(int)
b = crop[:, :, 2].astype(int)

# In the table:
# The wood surface is warm brown/tan:
# Surface has R > G > B, with R in [90, 220], G in [50, 160], B in [20, 120], and R - B > 35
is_table_color = (r > 60) & (r < 230) & (g > 30) & (g < 170) & (b > 10) & (b < 130) & (r - b > 25) & (r - g > 15)

vis = crop.copy()
vis[is_table_color] = [0, 255, 0] # mark green

# Also draw coordinate lines every 50px
from PIL import ImageDraw
vis_img = Image.fromarray(vis)
draw = ImageDraw.Draw(vis_img)
for y in range(0, 400, 50):
    draw.line([(0, y), (350, y)], fill=(255, 0, 0))
    draw.text((2, y + 2), str(y_off + y), fill=(255, 255, 0))
for x in range(0, 350, 50):
    draw.line([(x, 0), (x, 400)], fill=(255, 0, 0))
    draw.text((x + 2, 2), str(x_off + x), fill=(255, 255, 0))

vis_img.save(r'c:\Users\Mahdi\Desktop\بببببب\table_exact_coords.png')
print("Saved table_exact_coords.png")
