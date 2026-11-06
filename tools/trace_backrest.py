import numpy as np
from PIL import Image

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
orig_np = np.array(orig)

# For y from 458 to 570, find the last x where pixel is fabric (brightness > 180)
print("--- Backrest exact boundary (y vs x) ---")
coords = []
for y in range(458, 575, 4):
    edge_x = 0
    for x in range(0, 140):
        r, g, b = [int(c) for c in orig_np[y, x]]
        if (r + g + b) / 3 > 175:
            edge_x = x
    coords.append((edge_x, y))
    print(f"y={y}: edge_x={edge_x}")
