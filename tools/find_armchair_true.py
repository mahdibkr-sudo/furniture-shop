from PIL import Image, ImageDraw
import numpy as np

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
orig_np = np.array(orig)

# Crop armchair x: 0 to 200, y: 350 to 850
crop = orig_np[350:850, 0:200].copy()

vis_img = Image.fromarray(crop)
draw = ImageDraw.Draw(vis_img)
for y in range(0, 500, 50):
    draw.line([(0, y), (200, y)], fill=(255, 0, 0))
    draw.text((2, y + 2), str(350 + y), fill=(255, 255, 0))
for x in range(0, 200, 50):
    draw.line([(x, 0), (x, 500)], fill=(255, 0, 0))
    draw.text((x + 2, 2), str(x), fill=(255, 255, 0))

vis_img.save(r'c:\Users\Mahdi\Desktop\بببببب\armchair_true_coords.png')
print("Saved armchair_true_coords.png")
