"""Regenerate Prat-Droid assets from the seated portrait.
- splash_photo.png : full portrait, circular, large (for the splash)
- mipmap icons     : face-cropped circle, navy field, gold ring (all densities)
"""
import os
from PIL import Image, ImageDraw

SRC = r"C:\Users\PrathapAsus\Downloads\bcat-portal-extracted\acca-botswana-portal\client\public\panel\prathap-ganesharajah.jpg"
RES = r"C:\Users\PrathapAsus\work\prat-droid\app\src\main\res"

NAVY = (13, 27, 42, 255)
GOLD = (244, 166, 35, 255)
LEGACY = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
FG = {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}
SS = 4


def circular(photo, diameter):
    d = diameter * SS
    p = photo.resize((d, d), Image.LANCZOS).convert("RGBA")
    mask = Image.new("L", (d, d), 0)
    ImageDraw.Draw(mask).ellipse((0, 0, d, d), fill=255)
    p.putalpha(mask)
    return p.resize((diameter, diameter), Image.LANCZOS)


def ring(canvas, diameter, width):
    s = canvas * SS
    layer = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    d = diameter * SS
    off = (s - d) // 2
    ImageDraw.Draw(layer).ellipse((off, off, off + d, off + d), outline=GOLD, width=max(1, width * SS))
    return layer.resize((canvas, canvas), Image.LANCZOS)


def face_crop(im):
    w, h = im.size
    side = int(min(w, h) * 0.58)
    cx, cy = int(w * 0.50), int(h * 0.34)
    left = max(0, cx - side // 2)
    top = max(0, cy - side // 2)
    return im.crop((left, top, min(w, left + side), min(h, top + side)))


def main():
    src = Image.open(SRC).convert("RGB")

    # Splash: full portrait, big circle, gold ring
    big = circular(src, 640)
    out = Image.new("RGBA", (640, 640), (0, 0, 0, 0))
    out.paste(big, (0, 0), big)
    out = Image.alpha_composite(out, ring(640, 632, 8))
    os.makedirs(os.path.join(RES, "drawable"), exist_ok=True)
    out.save(os.path.join(RES, "drawable", "splash_photo.png"))

    face = face_crop(src)

    for dens, size in FG.items():
        d = os.path.join(RES, f"mipmap-{dens}")
        os.makedirs(d, exist_ok=True)
        canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        cd = int(size * 0.60)
        photo = circular(face, cd)
        off = (size - cd) // 2
        canvas.paste(photo, (off, off), photo)
        canvas = Image.alpha_composite(canvas, ring(size, int(size * 0.62), max(2, int(size * 0.018))))
        canvas.save(os.path.join(d, "ic_launcher_foreground.png"))

    for dens, size in LEGACY.items():
        d = os.path.join(RES, f"mipmap-{dens}")
        os.makedirs(d, exist_ok=True)
        s = size * SS
        base = Image.new("RGBA", (s, s), (0, 0, 0, 0))
        ImageDraw.Draw(base).ellipse((0, 0, s, s), fill=NAVY)
        base = base.resize((size, size), Image.LANCZOS)
        cd = int(size * 0.82)
        photo = circular(face, cd)
        off = (size - cd) // 2
        base.paste(photo, (off, off), photo)
        base = Image.alpha_composite(base, ring(size, int(size * 0.95), max(1, int(size * 0.04))))
        base.save(os.path.join(d, "ic_launcher.png"))
        base.save(os.path.join(d, "ic_launcher_round.png"))

    print("assets regenerated from seated portrait")


if __name__ == "__main__":
    main()
