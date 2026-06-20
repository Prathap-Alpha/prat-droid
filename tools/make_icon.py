"""Generate Prat-Droid launcher icons from a headshot.
Outputs adaptive foreground + legacy round/square mipmaps at all densities.
"""
import os
from PIL import Image, ImageDraw, ImageFilter

SRC = r"C:\Users\PrathapAsus\Downloads\Prathap_Headshot_60KB.jpg"
RES = r"C:\Users\PrathapAsus\work\prat-droid\app\src\main\res"

NAVY = (13, 27, 42, 255)     # #0D1B2A Alpha Direct dark navy
GOLD = (244, 166, 35, 255)   # #F4A623 Alpha Direct orange/gold

# launcher base 48dp -> densities
LEGACY = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
# adaptive foreground base 108dp
FG = {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}

SS = 4  # supersample for smooth edges


def face_crop(im):
    w, h = im.size
    side = int(min(w, h * 0.72))
    cx, cy = w // 2, int(h * 0.42)
    left = max(0, cx - side // 2)
    top = max(0, cy - side // 2)
    box = (left, top, min(w, left + side), min(h, top + side))
    return im.crop(box)


def circular(photo, diameter):
    """Return an RGBA circle of the photo at given diameter."""
    d = diameter * SS
    p = photo.resize((d, d), Image.LANCZOS).convert("RGBA")
    mask = Image.new("L", (d, d), 0)
    ImageDraw.Draw(mask).ellipse((0, 0, d, d), fill=255)
    p.putalpha(mask)
    return p.resize((diameter, diameter), Image.LANCZOS)


def ring(canvas_size, diameter, width):
    """Gold ring centered on a transparent canvas."""
    s = canvas_size * SS
    layer = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    dr = ImageDraw.Draw(layer)
    d = diameter * SS
    off = (s - d) // 2
    w = max(1, width * SS)
    dr.ellipse((off, off, off + d, off + d), outline=GOLD, width=w)
    return layer.resize((canvas_size, canvas_size), Image.LANCZOS)


def main():
    src = Image.open(SRC).convert("RGB")
    face = face_crop(src)

    # ---- Adaptive foreground: transparent, photo+ring inside 66% safe zone ----
    for dens, size in FG.items():
        d = os.path.join(RES, f"mipmap-{dens}")
        os.makedirs(d, exist_ok=True)
        canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        circ_d = int(size * 0.60)               # photo within safe zone
        photo = circular(face, circ_d)
        off = (size - circ_d) // 2
        canvas.paste(photo, (off, off), photo)
        rg = ring(size, int(size * 0.62), max(2, int(size * 0.018)))
        canvas = Image.alpha_composite(canvas, rg)
        canvas.save(os.path.join(d, "ic_launcher_foreground.png"))

    # ---- Legacy round + square: navy disc, photo, gold ring ----
    for dens, size in LEGACY.items():
        d = os.path.join(RES, f"mipmap-{dens}")
        os.makedirs(d, exist_ok=True)
        s = size * SS
        base = Image.new("RGBA", (s, s), (0, 0, 0, 0))
        dr = ImageDraw.Draw(base)
        dr.ellipse((0, 0, s, s), fill=NAVY)
        base = base.resize((size, size), Image.LANCZOS)
        circ_d = int(size * 0.82)
        photo = circular(face, circ_d)
        off = (size - circ_d) // 2
        base.paste(photo, (off, off), photo)
        rg = ring(size, int(size * 0.95), max(1, int(size * 0.04)))
        base = Image.alpha_composite(base, rg)
        base.save(os.path.join(d, "ic_launcher.png"))
        base.save(os.path.join(d, "ic_launcher_round.png"))

    # large icon for the splash screen (xxxhdpi drawable)
    dd = os.path.join(RES, "drawable")
    os.makedirs(dd, exist_ok=True)
    big = circular(face, 512)
    out = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
    out.paste(big, (0, 0), big)
    out = Image.alpha_composite(out, ring(512, 506, 10))
    out.save(os.path.join(dd, "splash_photo.png"))
    print("icons written under", RES)


if __name__ == "__main__":
    main()
