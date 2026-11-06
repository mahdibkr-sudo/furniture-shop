import numpy as np
from PIL import Image, ImageDraw
import cv2

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
orig_np = np.array(orig)

# Crop the table region
x1, y1, x2, y2 = 180, 400, 520, 800
table_crop = orig_np[y1:y2, x1:x2].copy()

# Find edges using Canny
gray = cv2.cvtColor(table_crop, cv2.COLOR_RGB2GRAY)
edges = cv2.Canny(gray, 30, 100)

# Save edges and color crop with a high-resolution grid (every 20 pixels)
table_img = Image.fromarray(table_crop)
draw = ImageDraw.Draw(table_img)

for y in range(0, y2 - y1, 20):
    draw.line([(0, y), (x2 - x1, y)], fill=(255, 0, 0, 100))
    # Draw label on the left
    draw.text((2, y + 2), str(y1 + y), fill=(255, 255, 0))

for x in range(0, x2 - x1, 20):
    draw.line([(x, 0), (x, y2 - y1)], fill=(255, 0, 0, 100))
    draw.text((x + 2, 2), str(x1 + x), fill=(255, 255, 0))

table_img.save(r'c:\Users\Mahdi\Desktop\بببببب\table_detailed_grid.png')
Image.fromarray(edges).save(r'c:\Users\Mahdi\Desktop\بببببب\table_edges.png')
print("Saved table_detailed_grid.png and table_edges.png")
