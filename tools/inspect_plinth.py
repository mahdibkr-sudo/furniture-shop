import numpy as np
from PIL import Image

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
orig_np = np.array(orig)

# For x = 10, 40, 70, 100, 130, find the y where fabric ends and dark plinth starts:
# Fabric has brightness > 150, plinth has brightness < 90
for x in [10, 40, 70, 100, 130]:
    print(f"--- x={x} ---")
    for y in range(740, 810, 5):
        rgb = tuple(orig_np[y, x])
        bright = sum(rgb) / 3
        print(f"  y={y}: RGB={rgb}, bright={bright:.1f}")
