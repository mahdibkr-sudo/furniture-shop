import numpy as np
from PIL import Image

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
orig_np = np.array(orig)

# For y from 560 to 775, find where the fabric ends (transition from fabric to brown table)
# Fabric has: high brightness and low R-B difference (cream: R~220, G~200, B~180, R-B ~ 40)
# Table has: lower brightness and higher R-B difference or wood brown:
# (R~120-170, G~70-110, B~30-65, R-B ~ 70-100, B < 80)
print("--- Armchair right edge vs table ---")
for y in range(560, 780, 10):
    # Scan from x=100 to 220
    edge_x = None
    for x in range(120, 220):
        r, g, b = [int(c) for c in orig_np[y, x]]
        # If pixel is table (wood)
        if (r - b > 55 and b < 90) or (r < 150 and b < 80):
            edge_x = x
            break
    print(f"y={y}: edge_x={edge_x}")
