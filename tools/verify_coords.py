from PIL import Image, ImageDraw
import numpy as np

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
print("Image size:", orig.size)

# Let's test a few specific points on the image by drawing red crosshairs:
# 1. Armchair top: (60, 450)
# 2. Armchair right curve: (320, 650) or (220, 650)?
# 3. Table front face center: let's test where it is!

test_img = orig.copy()
draw = ImageDraw.Draw(test_img)

points_to_test = [
    ("Armchair backrest", 60, 450),
    ("Armchair seat", 150, 650),
    ("Armchair right curve 1", 220, 650),
    ("Armchair right curve 2", 280, 650),
    ("Table front center", 400, 650),
    ("Table top center", 350, 500),
    ("Corner sofa seat", 600, 500),
]

for label, x, y in points_to_test:
    draw.ellipse([(x-5, y-5), (x+5, y+5)], fill=(255, 0, 0), outline=(255, 255, 255))
    draw.text((x+8, y-6), f"{label} ({x},{y})", fill=(255, 255, 0))

test_img.crop((0, 350, 750, 850)).save(r'c:\Users\Mahdi\Desktop\بببببب\verify_coords_test.png')
print("Saved verify_coords_test.png")
