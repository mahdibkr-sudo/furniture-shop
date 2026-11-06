import numpy as np
from PIL import Image
import colorsys

orig = Image.open(r'c:\Users\Mahdi\Desktop\بببببب\images\product-1.webp').convert('RGB')
orig_np = np.array(orig)

def print_rgb_hsl(name, coords):
    print(f"--- {name} ---")
    for (x, y) in coords:
        rgb = orig_np[y, x]
        r, g, b = [c / 255.0 for c in rgb]
        h, l, s = colorsys.rgb_to_hls(r, g, b)
        print(f"  ({x}, {y}): RGB={tuple(rgb)}, H={h*360:.1f}deg, S={s*100:.1f}%, L={l*100:.1f}%")

# Table points (top surface, front face)
table_coords = [
    (300, 480), # Table top surface
    (350, 480), # Table top surface
    (380, 520), # Table front/side
    (330, 580), # Table front face
    (380, 650), # Table front face
    (420, 600), # Table right side
    (240, 500), # Table left rim
    (480, 520), # Table right rim
]
print_rgb_hsl("BROWN TABLE", table_coords)

# Left armchair fabric
armchair_coords = [
    (50, 450),  # Backrest
    (50, 550),  # Seat upper
    (100, 650), # Seat front
    (150, 680), # Seat front right
    (180, 620), # Seat right edge near table
]
print_rgb_hsl("LEFT ARMCHAIR FABRIC", armchair_coords)

# Corner sofa fabric
corner_sofa_coords = [
    (450, 350), # Corner sofa backrest behind table
    (500, 380), # Corner sofa backrest
    (550, 500), # Corner sofa seat
    (500, 550), # Corner sofa seat left
]
print_rgb_hsl("CORNER SOFA FABRIC", corner_sofa_coords)

# Wood plinth
plinth_coords = [
    (50, 840),  # Left armchair plinth
    (100, 830), # Left armchair plinth
    (550, 650), # Corner sofa plinth
    (800, 730), # Middle sofa plinth
]
print_rgb_hsl("WOOD PLINTH (BASE)", plinth_coords)
