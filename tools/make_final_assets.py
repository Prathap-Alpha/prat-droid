"""Sky-blue theme assets: white ring (no gold), white RAPTOR wordmark."""
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

RES = r"C:\Users\PrathapAsus\work\prat-droid\app\src\main\res"
SRC = r"C:\Users\PrathapAsus\Downloads\bcat-portal-extracted\acca-botswana-portal\client\public\panel\prathap-ganesharajah.jpg"

NAVY = (13, 27, 42, 255)
WHITE = (255, 255, 255, 255)
LEGACY = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
FG = {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}
SS = 4


def circular(photo, d):
    n = d * SS
    p = photo.resize((n, n), Image.LANCZOS).convert("RGBA")
    m = Image.new("L", (n, n), 0)
    ImageDraw.Draw(m).ellipse((0, 0, n, n), fill=255)
    p.putalpha(m)
    return p.resize((d, d), Image.LANCZOS)


def ring(canvas, d, w, color):
    s = canvas * SS
    layer = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    off = (s - d * SS) // 2
    ImageDraw.Draw(layer).ellipse((off, off, off + d * SS, off + d * SS), outline=color, width=max(1, w * SS))
    return layer.resize((canvas, canvas), Image.LANCZOS)


def face_crop(im):
    w, h = im.size
    side = int(min(w, h) * 0.58)
    cx, cy = int(w * 0.50), int(h * 0.34)
    l = max(0, cx - side // 2); t = max(0, cy - side // 2)
    return im.crop((l, t, min(w, l + side), min(h, t + side)))


def main():
    src = Image.open(SRC).convert("RGB")

    # splash photo: white ring
    big = circular(src, 640)
    out = Image.new("RGBA", (640, 640), (0, 0, 0, 0))
    out.paste(big, (0, 0), big)
    out = Image.alpha_composite(out, ring(640, 632, 8, WHITE))
    out.save(os.path.join(RES, "drawable", "splash_photo.png"))

    face = face_crop(src)
    for dens, size in FG.items():
        d = os.path.join(RES, f"mipmap-{dens}"); os.makedirs(d, exist_ok=True)
        c = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        cd = int(size * 0.60); ph = circular(face, cd); off = (size - cd) // 2
        c.paste(ph, (off, off), ph)
        c = Image.alpha_composite(c, ring(size, int(size * 0.62), max(2, int(size * 0.018)), WHITE))
        c.save(os.path.join(d, "ic_launcher_foreground.png"))
    for dens, size in LEGACY.items():
        d = os.path.join(RES, f"mipmap-{dens}"); os.makedirs(d, exist_ok=True)
        s = size * SS
        base = Image.new("RGBA", (s, s), (0, 0, 0, 0))
        ImageDraw.Draw(base).ellipse((0, 0, s, s), fill=NAVY)
        base = base.resize((size, size), Image.LANCZOS)
        cd = int(size * 0.82); ph = circular(face, cd); off = (size - cd) // 2
        base.paste(ph, (off, off), ph)
        base = Image.alpha_composite(base, ring(size, int(size * 0.95), max(1, int(size * 0.04)), WHITE))
        base.save(os.path.join(d, "ic_launcher.png"))
        base.save(os.path.join(d, "ic_launcher_round.png"))

    # white RAPTOR wordmark, navy drop-shadow for legibility on sky blue
    font = ImageFont.truetype(r"C:\Windows\Fonts\impact.ttf", 220)
    text = "RAPTOR"
    tmp = ImageDraw.Draw(Image.new("RGBA", (10, 10)))
    bb = tmp.textbbox((0, 0), text, font=font)
    tw, th = bb[2] - bb[0], bb[3] - bb[1]
    canvas = Image.new("RGBA", (tw + 120, th + 120), (0, 0, 0, 0))
    d = ImageDraw.Draw(canvas)
    ox, oy = 60 - bb[0], 60 - bb[1]
    d.text((ox + 5, oy + 6), text, font=font, fill=(13, 27, 42, 150))  # navy shadow
    d.text((ox, oy), text, font=font, fill=WHITE)
    w, h = canvas.size
    shear = 0.22
    canvas = canvas.transform((w + int(h * shear), h), Image.AFFINE, (1, shear, -shear * h, 0, 1, 0), resample=Image.BICUBIC)
    canvas.save(os.path.join(RES, "drawable", "raptor_wordmark.png"))
    print("sky-theme assets regenerated")


if __name__ == "__main__":
    main()
