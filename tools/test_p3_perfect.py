import numpy as np
from PIL import Image
from scipy.ndimage import gaussian_filter, binary_opening, binary_closing
import colorsys

def rgb_to_hsl_np(rgb):
    r, g, b = rgb[:, :, 0], rgb[:, :, 1], rgb[:, :, 2]
    maxc = np.maximum(np.maximum(r, g), b)
    minc = np.minimum(np.minimum(r, g), b)
    l = (maxc + minc) / 2.0
    delta = maxc - minc
    s = np.zeros_like(l)
    nonzero = delta > 1e-5
    s_low = delta / (maxc + minc + 1e-7)
    s_high = delta / (2.0 - maxc - minc + 1e-7)
    s[nonzero & (l <= 0.5)] = s_low[nonzero & (l <= 0.5)]
    s[nonzero & (l > 0.5)] = s_high[nonzero & (l > 0.5)]
    h = np.zeros_like(l)
    rc = (maxc - r) / (delta + 1e-7)
    gc = (maxc - g) / (delta + 1e-7)
    bc = (maxc - b) / (delta + 1e-7)
    r_is_max = (r == maxc) & nonzero
    g_is_max = (g == maxc) & ~r_is_max & nonzero
    b_is_max = nonzero & ~r_is_max & ~g_is_max
    h[r_is_max] = (bc - gc)[r_is_max]
    h[g_is_max] = (2.0 + rc - bc)[g_is_max]
    h[b_is_max] = (4.0 + gc - rc)[b_is_max]
    h = (h / 6.0) % 1.0
    return h, s, l

def hsl_to_rgb_np(h, s, l):
    def hue_to_rgb(p, q, t):
        t = (t % 1.0)
        res = p + (q - p) * 6.0 * t
        cond1 = (t * 6.0 < 1.0)
        cond2 = (t * 2.0 < 1.0) & ~cond1
        cond3 = (t * 3.0 < 2.0) & ~cond1 & ~cond2
        cond4 = ~cond1 & ~cond2 & ~cond3
        out = np.zeros_like(t)
        out[cond1] = res[cond1]
        out[cond2] = q[cond2]
        out[cond3] = (p + (q - p) * (2.0/3.0 - t) * 6.0)[cond3]
        out[cond4] = p[cond4]
        return out

    q = np.where(l < 0.5, l * (1.0 + s), l + s - l * s)
    p = 2.0 * l - q
    r = hue_to_rgb(p, q, h + 1.0/3.0)
    g = hue_to_rgb(p, q, h)
    b = hue_to_rgb(p, q, h - 1.0/3.0)
    return np.stack([r, g, b], axis=-1)

def blend_css_color(base_rgb, tint_rgb):
    base_f = base_rgb.astype(np.float32) / 255.0
    tint_f = np.array(tint_rgb, dtype=np.float32) / 255.0
    h_base, s_base, l_base = rgb_to_hsl_np(base_f)
    tint_h, tint_l, tint_s = colorsys.rgb_to_hls(tint_f[0], tint_f[1], tint_f[2])
    h_new = np.full_like(l_base, tint_h)
    s_new = np.full_like(l_base, tint_s)
    res_f = hsl_to_rgb_np(h_new, s_new, l_base)
    return np.clip(res_f * 255.0, 0, 255).astype(np.uint8)

orig3 = Image.open('images/product-3.webp')
orig3_np = np.array(orig3)
raw3_np = np.array(Image.open('raw3_inspect.png'))

h, w = orig3_np.shape[:2]
yy, xx = np.meshgrid(np.arange(h), np.arange(w), indexing='ij')

# Start with raw_rembg
m3 = (raw3_np > 75).copy()

# 1. Left side bottom cutoff: left sofa fabric ends at y=732
m3[(xx < 865) & (yy >= 730)] = False

# 2. Right chaise bottom cutoff: chaise fabric ends at y=796
m3[(xx >= 865) & (yy >= 795)] = False

# 3. Chaise right edge: ends at x=1321
m3[xx > 1321] = False

# 4. Remove plant or background on right
m3[(xx > 1250) & (yy < 520)] = False

# 5. Clean morphology
m3 = binary_closing(m3, structure=np.ones((7, 7)))
m3 = binary_opening(m3, structure=np.ones((3, 3)))

# Smooth alpha edge
alpha = gaussian_filter(m3.astype(np.float32), sigma=1.0)
alpha_u8 = np.clip(alpha * 255.0, 0, 255).astype(np.uint8)

# Save mask
rgba = np.zeros((h, w, 4), dtype=np.uint8)
rgba[:, :, 3] = alpha_u8
Image.fromarray(rgba).save('test_p3_perfect_mask.png')

# Composite with blue
recolored = blend_css_color(orig3_np, [50, 110, 180])
alpha_f = alpha_u8.astype(np.float32)[:, :, None] / 255.0
comp = (orig3_np.astype(np.float32) * (1.0 - alpha_f) + recolored.astype(np.float32) * alpha_f).astype(np.uint8)
comp_img = Image.fromarray(comp)
comp_img.save('test_p3_perfect_comp.png')

# Crops
comp_img.crop((100, 640, 900, 800)).save('test_p3_left_perfect.png')
comp_img.crop((800, 700, 1350, 900)).save('test_p3_right_perfect.png')

print("Product 3 perfect test done.")
