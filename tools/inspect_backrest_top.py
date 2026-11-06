import numpy as np
from PIL import Image

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
orig_np = np.array(orig)

for y in range(435, 470, 5):
    print(f"--- y={y} ---")
    for x in range(0, 50, 5):
        rgb = tuple(orig_np[y, x])
        print(f"  x={x}: RGB={rgb}")
