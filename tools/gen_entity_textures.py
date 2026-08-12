"""Genera las texturas 128x64 de entidad del GOLEM de TNT (v2) y del AVIÓN de TNT.

El atlas usa el formato ESTANDAR de cubo de entidad de Minecraft:
para un cubo de tamano (sx,sy,sz) con texOffs (u,v):
  top    = (u+sz,     v)     sx x sz
  bottom = (u+sz+sx,  v)     sx x sz
  right  = (u,        v+sz)  sz x sy
  front  = (u+sz,     v+sz)  sx x sy
  left   = (u+sz+sx,  v+sz)  sz x sy
  back   = (u+sz+sx+sz,v+sz) sx x sy

Rects de los .java (bake 128x64):
  Golem:  head(56,0)10x8x10   body(0,0)14x16x14   arms(96,0)/(96,20)6x14x6
          legs(0,32)/(24,32)6x8x6  fuse(120,0)2x3x2  flame(100,48)3x4x3
  Avión:  fuselage(0,0)12x8x24  nose(72,0)8x5x6  wings(72,16)/(0,48)14x2x10
          tailFin(60,48)2x10x4  tailWing(72,48)16x2x5  propeller(96,56)10x1x2
          flame(56,32)6x6x6
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


def paint_cube(img, u, v, sx, sy, sz, face_fn):
    """Pinta las 6 caras de un cubo. face_fn(x0,y0,w,h,name) pinta cada cara."""
    faces = {
        'top': (u + sz, v, sx, sz),
        'bottom': (u + sz + sx, v, sx, sz),
        'right': (u, v + sz, sz, sy),
        'front': (u + sz, v + sz, sx, sy),
        'left': (u + sz + sx, v + sz, sz, sy),
        'back': (u + sz + sx + sz, v + sz, sx, sy),
    }
    for name, (x0, y0, w, h) in faces.items():
        face_fn(name, x0, y0, w, h)


def fill_rect(img, x0, y0, w, h, color):
    d = ImageDraw.Draw(img)
    d.rectangle([x0, y0, x0 + w - 1, y0 + h - 1], fill=color)


def tnt_side(img, x0, y0, w, h, face=False, gold_rivets=False):
    """Cara lateral de TNT: rojo con degradado + franja blanca central + remaches."""
    d = ImageDraw.Draw(img)
    for y in range(h):
        t = y / max(1, h - 1)
        col = tuple(int(RED[i] + (RED_D[i] - RED[i]) * t) for i in range(3))
        d.line([(x0, y0 + y), (x0 + w - 1, y0 + y)], fill=col)
    band_y0 = y0 + h // 2 - 2
    d.rectangle([x0, band_y0, x0 + w - 1, band_y0 + 3], fill=WHITE)
    rivet = GOLD if gold_rivets else DARK
    for x in range(x0 + 1, x0 + w - 1, 3):
        d.rectangle([x, band_y0 + 1, x + 1, band_y0 + 2], fill=rivet)
    if face:
        cy = band_y0
        # ojos grandes y sonrisa sobre la franja
        d.ellipse([x0 + w // 2 - 6, cy - 5, x0 + w // 2 - 2, cy - 1], fill=(20, 20, 22))
        d.ellipse([x0 + w // 2 + 2, cy - 5, x0 + w // 2 + 6, cy - 1], fill=(20, 20, 22))
        d.ellipse([x0 + w // 2 - 5, cy - 4, x0 + w // 2 - 3, cy - 2], fill=WHITE)
        d.ellipse([x0 + w // 2 + 3, cy - 4, x0 + w // 2 + 5, cy - 2], fill=WHITE)
        d.rectangle([x0 + w // 2 - 2, cy + 2, x0 + w // 2 + 2, cy + 3], fill=(20, 20, 22))


def tnt_top(img, x0, y0, w, h):
    """Cara superior de TNT: rojo con borde blanco."""
    fill_rect(img, x0, y0, w, h, RED)
    d = ImageDraw.Draw(img)
    d.rectangle([x0 + 1, y0 + 1, x0 + w - 2, y0 + h - 2], outline=WHITE, width=1)


def tnt_bottom(img, x0, y0, w, h):
    fill_rect(img, x0, y0, w, h, RED_D)


def gen_golem():
    img = Image.new("RGB", (W, H), (0, 0, 0))

    # CABEZA 10x8x10 en (56,0): la cara va en FRONT (x+), que es (66,10)-(76,18)
    def head_face(name, x0, y0, w, h):
        if name == 'front':
            tnt_side(img, x0, y0, w, h, face=True, gold_rivets=False)
        elif name in ('top', 'bottom'):
            tnt_top(img, x0, y0, w, h) if name == 'top' else tnt_bottom(img, x0, y0, w, h)
        else:
            tnt_side(img, x0, y0, w, h)
    paint_cube(img, 56, 0, 10, 8, 10, head_face)

    # CUERPO 14x16x14 en (0,0): cara frontal con sonrisa ancha
    def body_face(name, x0, y0, w, h):
        if name == 'front':
            tnt_side(img, x0, y0, w, h, face=True)
        elif name in ('top', 'bottom'):
            tnt_top(img, x0, y0, w, h) if name == 'top' else tnt_bottom(img, x0, y0, w, h)
        else:
            tnt_side(img, x0, y0, w, h)
    paint_cube(img, 0, 0, 14, 16, 14, body_face)

    # BRAZOS 6x14x6: marron TNT con franja blanca en el "puño"
    def arm_face(name, x0, y0, w, h):
        if name in ('top', 'bottom'):
            fill_rect(img, x0, y0, w, h, BROWN)
        else:
            tnt_side(img, x0, y0, w, h, gold_rivets=True)
    paint_cube(img, 96, 0, 6, 14, 6, arm_face)
    paint_cube(img, 96, 20, 6, 14, 6, arm_face)

    # PIERNAS 6x8x6
    def leg_face(name, x0, y0, w, h):
        if name in ('top', 'bottom'):
            fill_rect(img, x0, y0, w, h, BROWN_D)
        else:
            tnt_side(img, x0, y0, w, h, gold_rivets=False)
    paint_cube(img, 0, 32, 6, 8, 6, leg_face)
    paint_cube(img, 24, 32, 6, 8, 6, leg_face)

    # MECHA 2x3x2 en (120,0)
    def fuse_face(name, x0, y0, w, h):
        fill_rect(img, x0, y0, w, h, (60, 45, 30))
    paint_cube(img, 120, 0, 2, 3, 2, fuse_face)

    # LLAMA 3x4x3 en (100,48)
    def flame_face(name, x0, y0, w, h):
        if name == 'top':
            fill_rect(img, x0, y0, w, h, YELLOW)
        else:
            fill_rect(img, x0, y0, w, h, ORANGE)
    paint_cube(img, 100, 48, 3, 4, 3, flame_face)
    img.save("tnts_forge/src/main/resources/assets/tnts/textures/entity/tnt_golem.png")


def gen_rocket():
    img = Image.new("RGB", (W, H), (0, 0, 0))

    # FUSELAJE 12x8x24 en (0,0): franja TNT + letras a los lados
    def fuse_face(name, x0, y0, w, h):
        if name == 'front':
            tnt_side(img, x0, y0, w, h, gold_rivets=True)
        elif name == 'top':
            tnt_top(img, x0, y0, w, h)
        elif name == 'bottom':
            tnt_bottom(img, x0, y0, w, h)
        else:
            tnt_side(img, x0, y0, w, h, gold_rivets=True)
    paint_cube(img, 0, 0, 12, 8, 24, fuse_face)

    # NARIZ 8x5x6 en (72,0): cuña roja oscura
    def nose_face(name, x0, y0, w, h):
        fill_rect(img, x0, y0, w, h, RED_L if name == 'front' else RED_D)
    paint_cube(img, 72, 0, 8, 5, 6, nose_face)

    # ALAS 14x2x10: gris hierro con borde
    def wing_face(name, x0, y0, w, h):
        fill_rect(img, x0, y0, w, h, IRON if name != 'bottom' else IRON_D)
        if name in ('top', 'bottom'):
            d = ImageDraw.Draw(img)
            d.rectangle([x0, y0, x0 + w - 1, y0 + h - 1], outline=IRON_D, width=1)
    paint_cube(img, 72, 16, 14, 2, 10, wing_face)
    paint_cube(img, 0, 48, 14, 2, 10, wing_face)

    # TIMON VERTICAL 2x10x4 en (60,48)
    def fin_face(name, x0, y0, w, h):
        fill_rect(img, x0, y0, w, h, RED)
        if name == 'front':
            d = ImageDraw.Draw(img)
            d.rectangle([x0, y0, x0 + w - 1, y0 + h - 1], outline=WHITE, width=1)
    paint_cube(img, 60, 48, 2, 10, 4, fin_face)

    # PLANO HORIZONTAL 16x2x5 en (72,48)
    def tw_face(name, x0, y0, w, h):
        fill_rect(img, x0, y0, w, h, IRON if name != 'bottom' else IRON_D)
    paint_cube(img, 72, 48, 16, 2, 5, tw_face)

    # HELICE 10x1x2 en (96,56): madera
    def prop_face(name, x0, y0, w, h):
        fill_rect(img, x0, y0, w, h, BROWN)
    paint_cube(img, 96, 56, 10, 1, 2, prop_face)

    # LLAMA 6x6x6 en (56,32)
    def flame_face(name, x0, y0, w, h):
        if name == 'top':
            fill_rect(img, x0, y0, w, h, YELLOW)
        else:
            fill_rect(img, x0, y0, w, h, ORANGE)
    paint_cube(img, 56, 32, 6, 6, 6, flame_face)
    img.save("tnts_forge/src/main/resources/assets/tnts/textures/entity/tnt_rocket.png")


if __name__ == "__main__":
    gen_golem()
    gen_rocket()
    print("generadas tnt_golem.png y tnt_rocket.png (v2)")
