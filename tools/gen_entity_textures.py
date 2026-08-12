"""Genera las texturas 128x64 de entidad del Golem de TNT y del TNT Cohete.

El atlas usa el formato ESTANDAR de cubo de entidad de Minecraft:
para un cubo de tamano (sx,sy,sz) con texOffs (u,v):
  top    = (u+sz,     v)     sx x sz
  bottom = (u+sz+sx,  v)     sx x sz
  right  = (u,        v+sz)  sz x sy
  front  = (u+sz,     v+sz)  sx x sy
  left   = (u+sz+sx,  v+sz)  sz x sy
  back   = (u+sz+sx+sz,v+sz) sx x sy

Los rects de los .java:
  Golem:  body(0,0)16^3  arms(0,32)4x10x4  legs(32,32)/(48,32)4x6x4
          fuse(64,32)2x3x2  flame(72,32)3x4x3
  Cohete: body(0,0)16^3  nozzle(64,0)6x4x6  flame(64,16)5x6x5
"""
from PIL import Image, ImageDraw

W, H = 128, 64
RED = (165, 30, 42)
RED_D = (122, 22, 31)
RED_L = (190, 42, 52)
WHITE = (238, 236, 228)
GOLD = (175, 125, 18)
GOLD_L = (251, 191, 36)
BROWN = (106, 78, 50)
BROWN_D = (80, 58, 38)
IRON = (120, 120, 125)
IRON_D = (85, 85, 90)
YELLOW = (255, 200, 60)
ORANGE = (240, 120, 30)
DARK = (30, 8, 10)


def paint_cube(img, u, v, sx, sy, sz, side, top, bottom, front=None, left=None, back=None, right=None):
    """Pinta las 6 caras de un cubo segun el layout de entidad."""
    d = ImageDraw.Draw(img)
    front = front or side
    left = left or side
    back = back or side
    right = right or side
    # top
    d.rectangle([u + sz, v, u + sz + sx, v + sz], fill=top)
    # bottom
    d.rectangle([u + sz + sx, v, u + sz + sx + sx, v + sz], fill=bottom)
    # right (x-)
    d.rectangle([u, v + sz, u + sz, v + sz + sy], fill=right)
    # front (x+)
    d.rectangle([u + sz, v + sz, u + sz + sx, v + sz + sy], fill=front)
    # left (z+)
    d.rectangle([u + sz + sx, v + sz, u + sz + sx + sz, v + sz + sy], fill=left)
    # back (x-)
    d.rectangle([u + sz + sx + sz, v + sz, u + sz + sx + sz + sx, v + sz + sy], fill=back)


def tnt_pattern(img, u, v, sx, sy, sz, band=True, face=False):
    """Cubo de TNT: laterales rojos con franja blanca, top con letras TNT."""
    d = ImageDraw.Draw(img)
    # laterales: rojo con degradado vertical
    side = Image.new("RGB", (sx, sy))
    sd = ImageDraw.Draw(side)
    for y in range(sy):
        t = y / max(1, sy - 1)
        col = tuple(int(RED[i] + (RED_D[i] - RED[i]) * t) for i in range(3))
        sd.line([(0, y), (sx - 1, y)], fill=col)
    # franja blanca central (altura 4) con remaches oscuros
    if band:
        band_y0 = sy // 2 - 2
        sd.rectangle([0, band_y0, sx - 1, band_y0 + 3], fill=WHITE)
        for x in range(1, sx - 1, 2):
            sd.rectangle([x, band_y0 + 1, x + 1, band_y0 + 2], fill=DARK)
    if face:
        # cara amigable: ojos grandes y sonrisa sobre la franja
        cy = band_y0
        sd.ellipse([sx // 2 - 6, cy - 4, sx // 2 - 2, cy], fill=(20, 20, 22))
        sd.ellipse([sx // 2 + 2, cy - 4, sx // 2 + 6, cy], fill=(20, 20, 22))
        sd.ellipse([sx // 2 - 5, cy - 3, sx // 2 - 3, cy - 1], fill=WHITE)
        sd.ellipse([sx // 2 + 3, cy - 3, sx // 2 + 5, cy - 1], fill=WHITE)
        sd.rectangle([sx // 2 - 2, cy + 2, sx // 2 + 2, cy + 3], fill=(20, 20, 22))
    for x in range(sx):
        for y in range(sy):
            img.putpixel((u + sz + x, v + sz + y), side.getpixel((x, y)))
    # top: rojo con TNT blanca
    top = Image.new("RGB", (sx, sz), RED)
    td = ImageDraw.Draw(top)
    td.rectangle([2, 2, sx - 3, sz - 3], outline=WHITE, width=1)
    letters = "TNT"
    if sx >= 8:
        # letras TNT simples
        for li, ch in enumerate(letters):
            lx = 2 + li * (sx - 4) // 3
            td.text((lx, sz // 2 - 2), ch, fill=WHITE)
    for x in range(sx):
        for y in range(sz):
            img.putpixel((u + sz + x, v + y), top.getpixel((x, y)))
    # bottom: rojo oscuro
    bot = Image.new("RGB", (sx, sz), RED_D)
    for x in range(sx):
        for y in range(sz):
            img.putpixel((u + sz + sx + x, v + y), bot.getpixel((x, y)))
    # back y right: rojo con remaches
    for ox in range(sz):
        for oy in range(sy):
            img.putpixel((u + ox, v + sz + oy), (RED_L if oy < sy // 2 else RED))
            img.putpixel((u + sz + sx + sz + ox, v + sz + oy), (RED if oy < sy // 2 else RED_D))


def gen_golem():
    img = Image.new("RGB", (W, H), (0, 0, 0))
    # cuerpo 16^3 en texOffs(0,0): la cara va en FRONT (x+) que es (16,16)-(32,32)
    tnt_pattern(img, 0, 0, 16, 16, 16, band=True, face=False)
    d = ImageDraw.Draw(img)
    # cara amigable en la cara frontal (16,16)-(32,32): franja en y 22-25
    d.rectangle([16, 22, 31, 25], fill=WHITE)
    d.rectangle([16, 23, 31, 24], fill=DARK)  # separacion de la franja
    # ojos grandes (por encima de la franja)
    d.ellipse([19, 18, 24, 22], fill=(20, 20, 22))
    d.ellipse([24, 18, 29, 22], fill=(20, 20, 22))
    d.ellipse([20, 19, 23, 21], fill=WHITE)
    d.ellipse([25, 19, 28, 21], fill=WHITE)
    # sonrisa debajo de la franja
    d.arc([20, 25, 28, 30], 20, 160, fill=(20, 20, 22), width=1)
    # brazos 4x10x4 en (0,32) y (16,32): marron TNT
    paint_cube(img, 0, 32, 4, 10, 4, BROWN, BROWN, BROWN_D)
    paint_cube(img, 16, 32, 4, 10, 4, BROWN, BROWN, BROWN_D)
    # piernas 4x6x4 en (32,32) y (48,32)
    paint_cube(img, 32, 32, 4, 6, 4, BROWN_D, BROWN, BROWN_D)
    paint_cube(img, 48, 32, 4, 6, 4, BROWN_D, BROWN, BROWN_D)
    # mecha 2x3x2 en (64,32)
    paint_cube(img, 64, 32, 2, 3, 2, (60, 45, 30), (60, 45, 30), (40, 30, 20))
    # llama 3x4x3 en (72,32)
    paint_cube(img, 72, 32, 3, 4, 3, ORANGE, YELLOW, (255, 120, 20))
    d.rectangle([74, 33, 74, 33], fill=YELLOW)
    img.save("tnts_forge/src/main/resources/assets/tnts/textures/entity/tnt_golem.png")


def gen_rocket():
    img = Image.new("RGB", (W, H), (0, 0, 0))
    # cuerpo 16^3 en (0,0) con franja clasica (sin cara)
    tnt_pattern(img, 0, 0, 16, 16, 16, band=True, face=False)
    d = ImageDraw.Draw(img)
    # propulsor 6x4x6 en (64,0): hierro oscuro
    paint_cube(img, 64, 0, 6, 4, 6, IRON, IRON, IRON_D)
    d.rectangle([67, 0, 68, 3], fill=IRON_D)  # detalle de tubo
    # llama 5x6x5 en (64,16)
    paint_cube(img, 64, 16, 5, 6, 5, ORANGE, YELLOW, (255, 120, 20))
    d.rectangle([66, 18, 67, 19], fill=YELLOW)
    img.save("tnts_forge/src/main/resources/assets/tnts/textures/entity/tnt_rocket.png")


if __name__ == "__main__":
    gen_golem()
    gen_rocket()
    print("generadas tnt_golem.png y tnt_rocket.png")
