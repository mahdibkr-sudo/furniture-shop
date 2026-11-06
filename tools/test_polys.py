import numpy as np
from PIL import Image, ImageDraw
import cv2

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
orig_np = np.array(orig)
h, w, _ = orig_np.shape

# 1. Define armchair polygon/contour:
# Let's define the boundary of the armchair:
# Backrest:
# (0, 435) -> (30, 434) -> (60, 440) -> (85, 460) -> (105, 490) -> (120, 525) -> (128, 555)
# Crease to table:
# (128, 555) -> (150, 550) -> (175, 545) -> (185, 540) -> (188, 565)
# Right curve along the table:
# (188, 565) -> (194, 580) -> (200, 600) -> (203, 630) -> (204, 660) -> (203, 690) -> (198, 720) -> (190, 745) -> (180, 765)
# Bottom curve along wood plinth:
# (180, 765) -> (150, 772) -> (120, 779) -> (90, 785) -> (60, 790) -> (30, 796) -> (0, 800)
# Left border:
# (0, 800) -> (0, 435)

armchair_pts = np.array([
    [0, 435], [20, 434], [40, 434], [60, 440], [75, 450], [90, 465], [105, 490], [115, 515], [125, 540], [130, 555],
    [150, 550], [170, 545], [185, 540], [188, 565],
    [194, 580], [199, 600], [202, 620], [204, 640], [204, 660], [203, 680], [200, 705], [195, 730], [188, 750], [180, 765],
    [155, 771], [130, 777], [105, 782], [80, 787], [55, 791], [30, 796], [0, 801]
], dtype=np.int32)

# 2. Define Table Polygon (the brown wooden table to be EXCLUDED):
# Top back-left: (240, 492)
# Top back-right: (378, 436)
# Top front-right: (510, 500)
# Top front-left: (342, 562)
# Bottom-left: (240, 765)
# Bottom-front: (380, 768)
# Bottom-right: (532, 702)

table_pts = np.array([
    [240, 492],
    [378, 436],
    [510, 500],
    [532, 702],
    [380, 768],
    [240, 765]
], dtype=np.int32)

# Create preview
vis = orig_np.copy()
cv2.polylines(vis, [armchair_pts], True, (0, 255, 0), 2)
cv2.polylines(vis, [table_pts], True, (0, 0, 255), 2)

Image.fromarray(vis[:900, :600]).save(r'c:\Users\Mahdi\Desktop\بببببب\test_polygons.png')
print("Saved test_polygons.png")
