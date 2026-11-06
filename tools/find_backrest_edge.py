import numpy as np
from PIL import Image

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
orig_np = np.array(orig)

# For y from 420 to 570, scan from x=0 to 150 to find where fabric ends and grey wall begins:
# Wall has: neutral grey, R~130-150, G~125-145, B~120-140, Saturation < 0.08
# Fabric has: cream texture, R > G > B, warmer hue, or higher brightness
# Let's inspect rows:
print("--- Armchair backrest outer edge ---")
for y in range(426, 570, 10):
    edge_x = None
    for x in range(0, 150):
        r, g, b = [int(c) for c in orig_np[y, x]]
        # Wall is neutral grey: abs(r-g) < 10 and abs(g-b) < 10 and r < 155
        # If we see wall, this is beyond the edge
        # Or let's see where the gradient is steepest:
        pass
    # Print the RGB values for a few points along the row:
    # Let's find edge by color difference:
    diffs = []
    for x in range(1, 140):
        # difference between current pixel and next pixel
        r1, g1, b1 = [int(c) for c in orig_np[y, x-1]]
        r2, g2, b2 = [int(c) for c in orig_np[y, x+1]]
        d = abs(r2 - r1) + abs(g2 - g1) + abs(b2 - b1)
        diffs.append((d, x))
    # Or print where (r - b) drops from > 20 (fabric) to < 12 (grey wall)
    found = None
    for x in range(0, 140):
        r, g, b = [int(c) for c in orig_np[y, x]]
        if (r - b < 12 and abs(g - b) < 8) or (r < 140 and g < 135 and b < 130):
            found = x
            break
    print(f"y={y}: wall_starts_at_x={found}")
