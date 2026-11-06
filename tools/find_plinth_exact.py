import numpy as np
from PIL import Image

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
orig_np = np.array(orig)

# Find the plinth line for x from 0 to 180:
# For each x, scan downwards from y=750 to 900, find the first pixel where R < 95 and G < 65 and B < 50
print("--- Armchair plinth line (x vs y) ---")
plinth_pts = []
for x in range(0, 185, 10):
    found_y = None
    for y in range(750, 880):
        r, g, b = [int(c) for c in orig_np[y, x]]
        if r < 100 and g < 70 and b < 55:
            found_y = y
            break
    print(f"x={x}: plinth_y={found_y}")
