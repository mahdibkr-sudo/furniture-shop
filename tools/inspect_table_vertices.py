import numpy as np
from PIL import Image

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
orig_np = np.array(orig)

# Let's inspect rows around the table edges
# Top back-right corner of table:
# Let's inspect y around 460-480, x around 240-260:
print("--- Top back-right corner (around x=240..255, y=465..475) ---")
for y in range(466, 474):
    row_rgb = [tuple(orig_np[y, x]) for x in range(242, 252)]
    print(f"y={y}: {row_rgb}")

# Front-right corner of table (around x=360..375, y=525..540):
print("--- Front-right corner (around x=358..372, y=530..536) ---")
for y in range(530, 536):
    row_rgb = [tuple(orig_np[y, x]) for x in range(358, 372)]
    print(f"y={y}: {row_rgb}")

# Bottom-right corner of table on floor (around x=365..380, y=725..735):
print("--- Bottom-right corner on floor (around x=365..378, y=726..732) ---")
for y in range(726, 732):
    row_rgb = [tuple(orig_np[y, x]) for x in range(365, 378)]
    print(f"y={y}: {row_rgb}")

# Bottom-left corner of table on floor (around x=230..245, y=760..770):
print("--- Bottom-left corner on floor (around x=232..245, y=760..768) ---")
for y in range(760, 768):
    row_rgb = [tuple(orig_np[y, x]) for x in range(232, 245)]
    print(f"y={y}: {row_rgb}")
