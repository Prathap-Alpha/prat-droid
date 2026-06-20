"""Generate Raptor wordmark PNG, angry roar WAV, and a small photo thumbnail (base64)."""
import base64, io, math, os, random, struct, wave
from PIL import Image, ImageDraw, ImageFont, ImageFilter

RES = r"C:\Users\PrathapAsus\work\prat-droid\app\src\main\res"
SPLASH = os.path.join(RES, "drawable", "splash_photo.png")
GOLD = (244, 166, 35, 255)


def make_wordmark():
    font = ImageFont.truetype(r"C:\Windows\Fonts\impact.ttf", 220)
    text = "RAPTOR"
    tmp = Image.new("RGBA", (1400, 360), (0, 0, 0, 0))
    d = ImageDraw.Draw(tmp)
    bb = d.textbbox((0, 0), text, font=font)
    tw, th = bb[2] - bb[0], bb[3] - bb[1]
    canvas = Image.new("RGBA", (tw + 120, th + 120), (0, 0, 0, 0))
    d = ImageDraw.Draw(canvas)
    ox, oy = 60 - bb[0], 60 - bb[1]
    # subtle dark edge for depth, then gold fill
    d.text((ox + 4, oy + 4), text, font=font, fill=(0, 0, 0, 160))
    d.text((ox, oy), text, font=font, fill=GOLD)
    # aggressive forward lean (italic shear)
    w, h = canvas.size
    shear = 0.22
    canvas = canvas.transform(
        (w + int(h * shear), h), Image.AFFINE,
        (1, shear, -shear * h, 0, 1, 0), resample=Image.BICUBIC,
    )
    # gold glow underlay
    glow = canvas.filter(ImageFilter.GaussianBlur(14))
    out = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    out = Image.alpha_composite(out, glow)
    out = Image.alpha_composite(out, canvas)
    # gold underline streak
    d = ImageDraw.Draw(out)
    y = out.height - 70
    d.line((40, y, out.width - 30, y), fill=GOLD, width=10)
    out.save(os.path.join(RES, "drawable", "raptor_wordmark.png"))
    print("wordmark", out.size)


def make_roar():
    sr, dur = 22050, 1.7
    n = int(sr * dur)
    os.makedirs(os.path.join(RES, "raw"), exist_ok=True)
    path = os.path.join(RES, "raw", "raptor_roar.wav")
    w = wave.open(path, "w")
    w.setnchannels(1); w.setsampwidth(2); w.setframerate(sr)
    frames = bytearray()
    for i in range(n):
        t = i / sr
        prog = min(t / dur, 1.0)
        f0 = 90 + 40 * math.sin(2 * math.pi * 6 * t)          # growl + vibrato
        saw = (2 * ((f0 * t) % 1) - 1) * 0.5
        growl = (1 if math.sin(2 * math.pi * f0 * t) >= 0 else -1) * 0.35
        fs = 400 + 750 * math.sin(math.pi * prog)             # screech sweep
        screech = math.sin(2 * math.pi * fs * t) * 0.25
        noise = (random.random() * 2 - 1) * 0.3               # rasp
        if t < 0.05:
            env = t / 0.05
        elif t > 1.2:
            env = max(0.0, (dur - t) / (dur - 1.2))
        else:
            env = 1.0
        trem = 0.7 + 0.3 * math.sin(2 * math.pi * 18 * t)     # angry tremolo
        s = (saw + growl + screech + noise) * 0.5 * env * trem
        s = max(-1.0, min(1.0, s))
        frames += struct.pack("<h", int(s * 32767 * 0.9))
    w.writeframes(bytes(frames)); w.close()
    print("roar", round(dur, 2), "s ->", path)


def make_thumb():
    im = Image.open(SPLASH).convert("RGBA").resize((150, 150), Image.LANCZOS)
    bg = Image.new("RGB", (150, 150), (12, 27, 44))
    bg.paste(im, (0, 0), im)
    buf = io.BytesIO()
    bg.save(buf, format="JPEG", quality=72)
    b64 = base64.b64encode(buf.getvalue()).decode()
    with open(r"C:\Users\PrathapAsus\work\prat-droid\tools\photo_thumb_b64.txt", "w") as f:
        f.write(b64)
    print("thumb b64 len", len(b64))


if __name__ == "__main__":
    make_wordmark()
    make_roar()
    make_thumb()
