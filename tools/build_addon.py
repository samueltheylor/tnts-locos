"""Genera las texturas PNG de las TNTs (normal + encendida) y empaqueta el .mcaddon.

Uso:  python tools/build_addon.py
"""
import json
import os
import struct
import zipfile
import zlib

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BP = os.path.join(BASE, "TNTsLocos_BP")
RP = os.path.join(BASE, "TNTsLocos_RP")
BLOCK_TEX_DIR = os.path.join(RP, "textures", "blocks")


def png_chunk(tag, data):
    return (
        struct.pack(">I", len(data))
        + tag
        + data
        + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)
    )


def write_png(path, size, get_pixel):
    w, h = size
    raw = b""
    for y in range(h):
        raw += b"\x00"  # filtro: none
        for x in range(w):
            raw += bytes(get_pixel(x, y))
    png = b"\x89PNG\r\n\x1a\n"
    png += png_chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
    png += png_chunk(b"IDAT", zlib.compress(raw, 9))
    png += png_chunk(b"IEND", b"")
    with open(path, "wb") as f:
        f.write(png)


def hex_rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i : i + 2], 16) for i in (0, 2, 4)) + (255,)


FRAMES = 6
FRAME_HEIGHTS = [5, 3, 4, 2, 4, 3]  # altura de la llama por frame (parpadeo)

# Letras "TNT" para la cara superior (3x5 cada una, como la TNT de vanilla)
TNT_GLYPHS = {
    "T": ("111", "010", "010", "010", "010"),
    "N": ("101", "111", "111", "111", "101"),
}


def tnt_glyph(ix, iy):
    """Devuelve True/False si (ix, iy) (interior 12x12 de la cara superior)
    pinta una letra, o None si esta fuera de las letras."""
    ly = iy - 3  # 5 filas de letra, centradas en 12 de alto
    if not (0 <= ly < 5):
        return None
    col = 0
    for ch in "TNT":
        if col <= ix < col + 3:
            return TNT_GLYPHS[ch][ly][ix - col] == "1"
        col += 4  # letra de 3 + hueco de 1
    return None


def make_tnt_textures(path, base, band, border, dot, text_dark):
    """Genera las texturas 16x16 estilo TNT de vanilla: cara superior con las
    letras TNT, inferior lisa y lateral con franja, mas las variantes
    encendidas (lit) como tiras verticales de 6 frames para el flipbook.
    """
    base = hex_rgb(base)
    band = hex_rgb(band)
    border = hex_rgb(border)
    dot = hex_rgb(dot)
    text_dark = hex_rgb(text_dark)

    def pixel_side(x, y):
        if x in (0, 15) or y in (0, 15):
            return border
        # Sombreado lateral
        if x == 1:
            r, g, b, a = border
            return (min(r + 15, 255), min(g + 15, 255), min(b + 15, 255), a)
        if 5 <= y <= 10:
            c = band
            if (x % 4 == 0) and y in (6, 9):
                return text_dark
            if y in (5, 10):
                r, g, b, a = band
                return (max(r - 15, 0), max(g - 15, 0), max(b - 15, 0), a)
        else:
            c = base
            if (x * 7 + y * 13) % 19 == 0:
                return dot
            if y == 1:
                r, g, b, a = base
                return (min(r + 20, 255), min(g + 20, 255), min(b + 20, 255), a)
            if y == 14:
                r, g, b, a = base
                return (max(r - 20, 0), max(g - 20, 0), max(b - 20, 0), a)
        return c

    def pixel_top(x, y):
        if x in (0, 15) or y in (0, 15):
            return border
        if x in (1, 14) or y in (1, 14):
            return text_dark
        g = tnt_glyph(x - 2, y - 2)
        if g is True:
            if y == 3:
                r, g, b, a = band
                return (min(r + 10, 255), min(g + 10, 255), min(b + 10, 255), a)
            return band
        if g is False:
            if (x * 3 + y * 5) % 17 == 0:
                return dot
            if y <= 2:
                r, g, b, a = base
                return (min(r + 10, 255), min(g + 10, 255), min(b + 10, 255), a)
        return base

    def pixel_bottom(x, y):
        if x in (0, 15) or y in (0, 15):
            return border
        if x in (1, 14) or y in (1, 14):
            return text_dark
        if (x * 7 + y * 13) % 27 == 0:
            return dot
        if y <= 2:
            r, g, b, a = base
            return (min(r + 8, 255), min(g + 8, 255), min(b + 8, 255), a)
        if y >= 13:
            r, g, b, a = base
            return (max(r - 10, 0), max(g - 10, 0), max(b - 10, 0), a)
        return base

    def flame_px(x, y, frame, flame_h):
        t = y / max(1, flame_h)
        r = 255
        g = int(80 + 175 * (1 - t))
        b = int(10 + 40 * (1 - t))
        if (x * 7 + y * 13 + frame * 5) % 7 == 0:
            return (255, 235, 130, 255)
        return (r, g, b, 255)

    def pixel_side_lit(x, y, frame):
        fh = FRAME_HEIGHTS[frame % len(FRAME_HEIGHTS)]
        if y <= fh:
            base_flame = flame_px(x, y, frame, fh)
            if y <= 1 and (x + frame) % 3 == 0:
                return (255, 255, 200, 255)
            if (x == 0 or x == 15) and y <= 2:
                return (60, 50, 40, 200)
            return base_flame
        return pixel_side(x, y)

    def pixel_top_lit(x, y, frame):
        if (x + y + frame) % 4 == 0:
            return (255, 245, 160, 255)
        if (x * 5 + y * 3 + frame * 7) % 13 == 0:
            return (255, 200, 80, 255)
        t = 1 - (y / 15)
        r = 255
        g = int(80 + 175 * t)
        b = int(5 + 50 * t)
        if (x * 3 + y * 7 + frame) % 9 == 0:
            return (180, 50, 15, 255)
        if 6 <= x <= 9 and 6 <= y <= 9 and frame % 2 == 0:
            return (255, 255, 180, 255)
        return (r, g, b, 255)

    stem = path[:-4]
    write_png(path, (16, 16), lambda x, y: pixel_side(x, y))
    write_png(stem + "_top.png", (16, 16), lambda x, y: pixel_top(x, y))
    write_png(stem + "_bottom.png", (16, 16), lambda x, y: pixel_bottom(x, y))
    write_png(stem + "_lit.png", (16, 16 * FRAMES),
              lambda x, y: pixel_side_lit(x, y % 16, y // 16))
    write_png(stem + "_top_lit.png", (16, 16 * FRAMES),
              lambda x, y: pixel_top_lit(x, y % 16, y // 16))


def make_chestplate_texture(path):
    """Peto de TNT mejorado: armadura roja con sombreado, franja TNT,
    correas de cuero y hombreras reforzadas."""
    def pixel(x, y):
        if x in (0, 15) or y in (0, 15):
            return (0, 0, 0, 0)
        RED = (190, 30, 45, 255)
        RED_LIGHT = (220, 60, 75, 255)
        RED_DARK = (120, 18, 25, 255)
        BAND = (245, 245, 244, 255)
        EDGE = (35, 30, 28, 255)
        LEATHER = (110, 72, 40, 255)
        LEATHER_DARK = (75, 48, 25, 255)
        # Hombros reforzados
        if 2 <= x <= 4 and 3 <= y <= 5:
            if y == 3:
                return (160, 28, 38, 255)
            if x in (2, 4):
                return RED_DARK
            if y == 4 and x == 3:
                return (220, 60, 75, 255)
            return RED
        if 11 <= x <= 13 and 3 <= y <= 5:
            if y == 3:
                return (160, 28, 38, 255)
            if x in (11, 13):
                return RED_DARK
            if y == 4 and x == 12:
                return (220, 60, 75, 255)
            return RED
        # Correas de cuero con hebillas
        if (x in (5, 6, 9, 10)) and 4 <= y <= 13:
            if x in (5, 10):
                return LEATHER_DARK
            if y in (4, 13):
                return LEATHER_DARK
            if y in (6, 9, 12) and x in (5, 10):
                return (180, 175, 165, 255)
            return LEATHER
        # Cuerpo del peto
        if 3 <= x <= 13 and 6 <= y <= 13:
            if x in (3, 13) or y == 13:
                return EDGE
            if y in (9, 10):
                return BAND
            if y == 6:
                return RED_LIGHT
            if y == 7:
                return (205, 40, 55, 255)
            if x in (7, 8) and y == 11:
                return (255, 255, 255, 255) if x == 7 else (200, 35, 45, 255)
            if y == 12:
                return RED_DARK
            if x in (3, 4, 11, 12):
                return RED_DARK
            return RED
        return (0, 0, 0, 0)

    write_png(path, (16, 16), pixel)


def make_pickaxe_texture(path):
    """Pico de TNT mejorado: mango de madera con veta, cabeza roja con
    sombreado metalico, filo brillante y detalles de TNT."""
    def pixel(x, y):
        WOOD = (139, 90, 43, 255)
        WOOD_DARK = (100, 62, 28, 255)
        WOOD_LIGHT = (170, 115, 55, 255)
        RED = (190, 30, 45, 255)
        RED_LIGHT = (220, 60, 75, 255)
        RED_DARK = (120, 18, 25, 255)
        BAND = (245, 245, 244, 255)
        EDGE = (35, 30, 28, 255)
        METAL = (140, 140, 155, 255)
        METAL_DARK = (80, 80, 92, 255)
        # Mango de madera con veta
        if 2 <= x <= 7:
            row = 14 - (x - 2)
            if y == row:
                if (x + y) % 3 == 0:
                    return WOOD_DARK
                return WOOD
            if y == row - 1 and x >= 3:
                return WOOD_LIGHT
        # Cabeza de pico
        if 4 <= x <= 13 and 2 <= y <= 8:
            if x == 4 and y == 5:
                return METAL
            if x == 4 and y in (4, 6):
                return METAL_DARK
            if x in (4, 13) or y in (2, 8):
                return EDGE
            if y == 3:
                return RED_LIGHT if x == 5 else RED_DARK
            if y == 5:
                return BAND
            if y == 6:
                return (200, 40, 55, 255)
            if x == 5 and y in (4, 6, 7):
                return METAL
            if x == 6 and y in (2, 3):
                return METAL_DARK
            if y == 7:
                return RED_DARK
            if y == 4:
                return RED_LIGHT if x in (7, 8) else RED
            return RED
        return (0, 0, 0, 0)

    write_png(path, (16, 16), pixel)


def make_black_hole_texture(path):
    """Textura 64x32 de la bola negra 3D mejorada: esfera oscura con anillo
    purpura brillante, brillo radial y corona de energia."""
    def sphere(px, py):
        cx, cy = 7.5, 7.5
        d = ((px - cx) ** 2 + (py - cy) ** 2) ** 0.5
        if d > 6.5:
            return (5, 2, 15, 255)
        if d > 6.0:
            return (40, 10, 80, 255)
        if d > 5.5:
            return (130, 50, 200, 255)
        if d > 4.8:
            return (200, 120, 255, 255)
        if d > 4.4:
            return (160, 80, 220, 255)
        if d > 3.0:
            return (15, 5, 30, 255)
        if d > 1.5:
            return (5, 1, 12, 255)
        return (0, 0, 0, 255)

    def pixel(x, y):
        if y < 16 and x < 64:
            return sphere(x % 16, y)
        if y == 0 or y == 15 or x == 0 or x == 63:
            return (5, 2, 15, 255)
        return (8, 3, 18, 255)

    write_png(path, (64, 32), pixel)


def make_armor_layer(path):
    """Capa de armadura del Peto de TNT (64x32) mejorada: torso y brazos rojos
    con sombreado, franja TNT, correas y hombreras reforzadas."""
    RED = (190, 30, 45, 255)
    RED_LIGHT = (228, 66, 82, 255)
    RED_DARK = (120, 18, 25, 255)
    RED_VDARK = (80, 10, 15, 255)
    BAND = (245, 245, 244, 255)
    BAND_SHADE = (220, 220, 215, 255)
    EDGE = (35, 30, 28, 255)
    LEATHER = (110, 72, 40, 255)
    LEATHER_DARK = (75, 48, 25, 255)
    METAL = (180, 175, 165, 255)

    def body_px(x, y):
        if not (16 <= x <= 47 and 20 <= y <= 31):
            return (0, 0, 0, 0)
        if x in (16, 47) or y == 31:
            return EDGE
        if y == 20:
            return RED_LIGHT
        if y == 21:
            return (210, 48, 62, 255)
        if x in (22, 23, 30, 31, 38, 39):
            if y in (20, 31):
                return LEATHER_DARK
            if y in (24, 27, 30):
                return METAL
            return LEATHER
        if y in (24, 25):
            return BAND if x not in (22, 23, 30, 31, 38, 39) else BAND_SHADE
        if y in (26, 27):
            return RED_VDARK
        if y == 23 and x in (29, 30, 31, 32, 33, 34):
            if x in (29, 30, 34):
                return (255, 255, 255, 255)
            if x in (31, 32):
                return (245, 245, 244, 255)
            return (200, 35, 45, 255)
        if x <= 18:
            return RED_DARK
        if x >= 45:
            return RED_DARK
        if y == 30:
            return RED_VDARK
        return RED

    def arm_px(x, y):
        if not (48 <= x <= 55 and 16 <= y <= 31):
            return (0, 0, 0, 0)
        if x in (48, 55) or y == 31:
            return EDGE
        if y in (16, 17):
            return RED_DARK if x in (48, 55) else RED_LIGHT
        if x in (50, 51):
            if y in (20, 28):
                return METAL
            return LEATHER
        if y in (24, 25):
            return BAND
        if x in (49, 54):
            return RED_DARK
        if y == 26:
            return RED_VDARK
        return RED

    def pixel(x, y):
        p = body_px(x, y)
        if p[3] != 0:
            return p
        return arm_px(x, y)

    write_png(path, (64, 32), pixel)


def patch_block_material_instances():
    """Anade las caras up/down a todos los bloques TNT del BP para que usen
    las texturas superiores (letras TNT) e inferiores (lisas) nuevas."""
    blocks_dir = os.path.join(BP, "blocks")
    for fname in sorted(os.listdir(blocks_dir)):
        if not fname.endswith(".json"):
            continue
        path = os.path.join(blocks_dir, fname)
        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f)
        blk = data["minecraft:block"]
        base_mi = blk["components"]["minecraft:material_instances"]
        base_tex = base_mi.get("*", {}).get("texture")
        if base_tex:
            base_mi["up"] = {"texture": base_tex + "_top", "render_method": "opaque"}
            base_mi["down"] = {"texture": base_tex + "_bottom", "render_method": "opaque"}
        for perm in blk.get("permutations", []):
            pmi = perm.get("components", {}).get("minecraft:material_instances", {})
            tex = pmi.get("*", {}).get("texture")
            if tex:
                pmi["up"] = {"texture": tex.replace("_lit", "_top_lit"),
                              "render_method": "opaque"}
                pmi["down"] = {"texture": tex.replace("_lit", "_bottom"),
                                "render_method": "opaque"}
        with open(path, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2)


def make_detonator_texture(path):
    """Detonador remoto mejorado: cuerpo metalico con textura, antena, LED y boton."""
    def pixel(x, y):
        if x in (0, 15) or y in (0, 15):
            return (0, 0, 0, 0)
        # Antena
        if x == 8 and y == 0:
            return (180, 180, 195, 255)
        if x == 8 and y in (1, 2, 3):
            return (140, 140, 155, 255)
        if x in (7, 9) and y == 3:
            return (100, 100, 115, 255)
        # Cuerpo principal
        if 2 <= x <= 13 and 4 <= y <= 13:
            if x in (2, 13) or y in (4, 13):
                return (30, 30, 38, 255)
            if x in (3, 12):
                return (55, 55, 65, 255)
            if y == 5 and x in (3, 12):
                return (100, 100, 115, 255)
            # Pantalla LED verde
            if 5 <= x <= 10 and 5 <= y <= 7:
                if x in (5, 10) or y in (5, 7):
                    return (20, 60, 20, 255)
                return (30, 180, 50, 255)
            # Boton rojo grande
            if 5 <= x <= 10 and 9 <= y <= 12:
                if x in (5, 10) or y in (9, 12):
                    return (120, 20, 20, 255)
                if x in (6, 9) and y in (10, 11):
                    return (255, 70, 70, 255)
                return (200, 35, 35, 255)
            if (x * 3 + y * 7) % 11 == 0:
                return (90, 90, 105, 255)
            return (75, 75, 88, 255)
        return (0, 0, 0, 0)

    write_png(path, (16, 16), pixel)


def make_launcher_texture(path):
    """Lanzador de TNT mejorado: tubo de hierro con sombreado, boca amplia,
    boton rojo con brillo y detalles mecanicos."""
    def pixel(x, y):
        if x in (0, 15) or y in (0, 15):
            return (0, 0, 0, 0)
        if 4 <= x <= 11 and 2 <= y <= 13:
            if x in (4, 11) or y in (2, 13):
                return (35, 35, 42, 255)
            if y == 3:
                return (60, 60, 70, 255) if x in (5, 10) else (45, 45, 55, 255)
            if y == 4:
                return (20, 20, 25, 255)
            if y == 5 and x in (5, 10):
                return (120, 120, 135, 255)
            if 6 <= y <= 10:
                if x in (5, 10):
                    return (110, 110, 125, 255) if y == 6 else (80, 80, 92, 255)
                if 7 <= x <= 9 and y in (7, 8):
                    if x == 7 and y == 7:
                        return (255, 80, 80, 255)
                    return (200, 40, 40, 255)
                if y == 9 and x in (6, 8):
                    return (100, 100, 115, 255)
                return (70, 70, 82, 255)
            if y == 11:
                return (50, 50, 60, 255) if x in (5, 10) else (60, 60, 70, 255)
            if y == 12:
                return (40, 40, 48, 255)
        return (0, 0, 0, 0)

    write_png(path, (16, 16), pixel)


def make_grenade_texture(path):
    """Granada de TNT mejorada: bola esferica con sombreado, franja TNT,
    mecha detallada con chispas y brillo."""
    def pixel(x, y):
        RED = (190, 30, 45, 255)
        RED_LIGHT = (225, 65, 80, 255)
        RED_DARK = (110, 15, 22, 255)
        EDGE = (80, 10, 14, 255)
        BAND = (240, 240, 235, 255)
        # Mecha encendida
        if 10 <= x <= 12 and 1 <= y <= 4:
            if y == 1 and x == 11:
                return (255, 250, 180, 255)
            if y == 2 and x in (11, 12):
                return (255, 220, 100, 255)
            if y == 2 and x == 10:
                return (255, 180, 60, 255)
            if y == 3 and x == 11:
                return (200, 140, 40, 255)
            if y == 4 and x == 11:
                return (110, 80, 40, 255)
            return (90, 65, 30, 255)
        if 10 <= x <= 12 and y == 5:
            return (60, 40, 18, 255) if x == 11 else (40, 30, 15, 255)
        # Bola esferica
        if 3 <= x <= 12 and 5 <= y <= 14:
            cx, cy = 7.5, 9.5
            dist = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            if dist > 4.8:
                return EDGE if dist > 5.2 else RED_DARK
            if y in (9, 10):
                return BAND if dist > 2.5 else (200, 200, 195, 255)
            if dist < 2.0:
                return RED_LIGHT
            if dist < 3.0:
                return RED
            if dist < 4.0:
                return RED_DARK
            return EDGE
        return (0, 0, 0, 0)

    write_png(path, (16, 16), pixel)


def make_arrow_texture(path):
    """Flecha de TNT mejorada: punta roja con franja TNT, palo de madera
    con veta y plumas claras."""
    def pixel(x, y):
        WOOD = (139, 90, 43, 255)
        WOOD_DARK = (100, 62, 28, 255)
        WOOD_LIGHT = (170, 115, 55, 255)
        RED = (190, 30, 45, 255)
        RED_LIGHT = (220, 60, 75, 255)
        EDGE = (35, 30, 28, 255)
        BAND = (245, 245, 244, 255)
        # Palo diagonal (2,13) -> (7,8)
        if 2 <= x <= 7:
            row = 13 - (x - 2)
            if y == row:
                return WOOD_DARK if (x + y) % 3 == 0 else WOOD
            if y == row - 1 and x >= 3:
                return WOOD_LIGHT
        # Plumas
        if x == 2 and y in (12, 13):
            return (180, 175, 165, 255)
        if x == 1 and y == 13:
            return (160, 155, 145, 255)
        # Cabeza TNT (7-14, 1-8)
        if 7 <= x <= 14 and 1 <= y <= 8:
            if x in (7, 14) or y in (1, 8):
                return EDGE
            if x == 13 and y == 4:
                return (140, 140, 155, 255)
            if x == 13 and y in (3, 5):
                return (80, 80, 92, 255)
            if y in (4, 5):
                return BAND
            if y == 2:
                return RED_LIGHT
            if y == 7:
                return (120, 18, 25, 255)
            return RED
        return (0, 0, 0, 0)

    write_png(path, (16, 16), pixel)


def main():
    os.makedirs(BLOCK_TEX_DIR, exist_ok=True)

    # (base, franja, borde, mota, letras) por TNT
    textures = {
        "tnts_mega":      ("#7f1d1d", "#f5f5f4", "#450a0a", "#ef4444", "#450a0a"),  # rojo oscuro
        "tnts_mini":      ("#475569", "#f5f5f4", "#0f172a", "#cbd5e1", "#0f172a"),  # gris azulado
        "tnts_lava":      ("#c2410c", "#fef3c7", "#431407", "#fde047", "#7c2d12"),  # naranja/lava
        "tnts_rapida":    ("#14532d", "#f5f5f4", "#052e16", "#a3e635", "#052e16"),  # verde
        "tnts_hielo":     ("#0284c7", "#e0f2fe", "#0c4a6e", "#e0f2fe", "#075985"),  # azul hielo
        "tnts_saltarina": ("#6d28d9", "#f5f3ff", "#2e1065", "#f5d0fe", "#2e1065"),  # morado
        "tnts_nuclear":   ("#292524", "#facc15", "#0a0a0a", "#86efac", "#0a0a0a"),  # oscuro con franja amarilla
        "tnts_limpia":    ("#e7e5e4", "#ffffff", "#57534e", "#d6d3d1", "#57534e"),  # blanco/crema
        "tnts_rayo": ("#1e3a8a", "#dbeafe", "#172554", "#fde047", "#1e3a8a"),  # azul con chispas
        "tnts_trampa": ("#78350f", "#fef3c7", "#292524", "#f59e0b", "#292524"),  # marron trampa
        "tnts_oro": ("#a16207", "#fef9c3", "#422006", "#fde047", "#713f12"),  # dorado
        "tnts_obsidiana": ("#312e81", "#c4b5fd", "#1e1b4b", "#8b5cf6", "#1e1b4b"),  # obsidiana
        "tnts_crio": ("#155e75", "#a5f3fc", "#083344", "#67e8f9", "#164e63"),  # hielo azul
        "tnts_xp": ("#4d7c0f", "#ecfccb", "#1a2e05", "#a3e635", "#365314"),  # experiencia
        "tnts_agua": ("#075985", "#bae6fd", "#082f49", "#38bdf8", "#0c4a6e"),  # agua
        "tnts_arena": ("#ca8a04", "#fef9c3", "#713f12", "#fde047", "#854d0e"),  # arena
        "tnts_diamante": ("#0e7490", "#cffafe", "#164e63", "#67e8f9", "#155e75"),  # diamante
        "tnts_esmeralda": ("#047857", "#d1fae5", "#064e3b", "#34d399", "#065f46"),  # esmeralda
        "tnts_negra": ("#1c1917", "#a855f7", "#000000", "#d8b4fe", "#000000"),  # agujero negro
        "tnts_viento": ("#94a3b8", "#f8fafc", "#334155", "#e2e8f0", "#475569"),  # viento
        "tnts_inferno": ("#9a3412", "#fde047", "#450a0a", "#f97316", "#7c2d12"),  # inferno
        "tnts_hongo": ("#b91c1c", "#fef3c7", "#7f1d1d", "#fca5a5", "#991b1b"),  # setas
        "tnts_miel": ("#b45309", "#fef3c7", "#78350f", "#fbbf24", "#92400e"),  # miel
        "tnts_heal": ("#be185d", "#fce7f3", "#831843", "#f9a8d4", "#9d174d"),  # curacion
        "tnts_teleport": ("#3b0764", "#e9d5ff", "#1e1b4b", "#c084fc", "#2e1065"),  # teletransporte
        "tnts_confeti": ("#db2777", "#ffffff", "#4c0519", "#f472b6", "#831843"),  # confeti
        "tnts_mina": ("#44403c", "#e7e5e4", "#1c1917", "#a8a29e", "#292524"),  # mina
        "tnts_terremoto": ("#78350f", "#fef3c7", "#451a03", "#d97706", "#451a03"),  # terremoto
        "tnts_meteorito": ("#1c1917", "#f97316", "#0a0a0a", "#fb923c", "#0a0a0a"),  # meteorito
        "tnts_tormenta": ("#1e1b4b", "#fde047", "#0c0a2a", "#facc15", "#0c0a2a"),  # tormenta
        "tnts_colosal": ("#7f1d1d", "#fef9c3", "#450a0a", "#fde047", "#450a0a"),  # colosal
        "tnts_supernova": ("#4c1d95", "#fef9c3", "#2e1065", "#fcd34d", "#2e1065"),  # supernova
    }

    # Bloques generados automaticamente: id -> (color mapa, [ingredientes de la receta])
    GENERATED_BLOCKS = {
        "tnts:diamante_tnt": ("#0e7490", [("item", "minecraft:diamond")]),
        "tnts:esmeralda_tnt": ("#047857", [("item", "minecraft:emerald")]),
        "tnts:negra_tnt": ("#1c1917", [("item", "minecraft:crying_obsidian")]),
        "tnts:viento_tnt": ("#94a3b8", [("item", "minecraft:phantom_membrane")]),
        "tnts:inferno_tnt": ("#9a3412", [("item", "minecraft:magma_cream")]),
        "tnts:hongo_tnt": ("#b91c1c", [("item", "minecraft:red_mushroom")]),
        "tnts:miel_tnt": ("#b45309", [("item", "minecraft:honeycomb")]),
        "tnts:heal_tnt": ("#be185d", [("item", "minecraft:ghast_tear")]),
        "tnts:teleport_tnt": ("#3b0764", [("item", "minecraft:ender_pearl")]),
        "tnts:confeti_tnt": ("#db2777", [("item", "minecraft:red_dye"),
                                          ("item", "minecraft:yellow_dye"),
                                          ("item", "minecraft:blue_dye")]),
        "tnts:mina_tnt": ("#44403c", [("item", "minecraft:stone_pressure_plate")]),
        "tnts:terremoto_tnt": ("#78350f", [("item", "minecraft:pointed_dripstone"), ("item", "minecraft:magma_block")]),
        "tnts:meteorito_tnt": ("#1c1917", [("item", "minecraft:end_stone"), ("item", "minecraft:fire_charge")]),
        "tnts:tormenta_tnt": ("#1e1b4b", [("item", "minecraft:lightning_rod"), ("item", "minecraft:redstone")]),
        "tnts:colosal_tnt": ("#7f1d1d", [("item", "tnts:mega_tnt"), ("item", "tnts:mega_tnt"), ("item", "minecraft:tnt")]),
        "tnts:supernova_tnt": ("#4c1d95", [("item", "minecraft:nether_star"), ("item", "minecraft:glowstone")]),
    }

    def write_bedrock_block(block_id, color, ingredients):
        short = block_id.split(":")[1]
        tex = "tnts_" + short.replace("_tnt", "")
        block_json = {
            "format_version": "1.21.90",
            "minecraft:block": {
                "description": {
                    "identifier": block_id,
                    "menu_category": {"category": "construction"},
                    "states": {"tnts:lit": [False, True]},
                },
                "permutations": [
                    {
                        "condition": "query.block_state('tnts:lit') == true",
                        "components": {
                            "minecraft:material_instances": {
                                "*": {"texture": tex + "_lit", "render_method": "opaque"}
                            }
                        },
                    }
                ],
                "components": {
                    "minecraft:material_instances": {
                        "*": {"texture": tex, "render_method": "opaque"}
                    },
                    "minecraft:destructible_by_mining": {"seconds_to_destroy": 0.2},
                    "minecraft:map_color": color,
                    "minecraft:redstone_consumer": {"min_power": 1},
                    "tnts:ignite": {},
                },
            },
        }
        with open(os.path.join(BP, "blocks", short + ".json"), "w") as f:
            json.dump(block_json, f, indent=2)
        recipe_json = {
            "format_version": "1.21.90",
            "minecraft:recipe_shapeless": {
                "description": {"identifier": f"tnts:{short}_recipe", "tags": ["crafting_table"]},
                "ingredients": [{"item": "minecraft:tnt"}] + [{"item": i} for _, i in ingredients],
                "result": {"item": block_id, "count": 1},
            },
        }
        with open(os.path.join(BP, "recipes", short + "_recipe.json"), "w") as f:
            json.dump(recipe_json, f, indent=2)

    for block_id, (color, ingredients) in GENERATED_BLOCKS.items():
        write_bedrock_block(block_id, color, ingredients)
    for name, pal in textures.items():
        make_tnt_textures(os.path.join(BLOCK_TEX_DIR, name + ".png"), *pal)
    patch_block_material_instances()

    # Items: detonador, peto de TNT y pico de TNT
    item_tex_dir = os.path.join(RP, "textures", "items")
    armor_tex_dir = os.path.join(RP, "textures", "models", "armor")
    os.makedirs(item_tex_dir, exist_ok=True)
    os.makedirs(armor_tex_dir, exist_ok=True)
    make_detonator_texture(os.path.join(item_tex_dir, "detonator.png"))
    make_chestplate_texture(os.path.join(item_tex_dir, "tnt_chestplate.png"))
    make_pickaxe_texture(os.path.join(item_tex_dir, "tnt_pickaxe.png"))
    make_launcher_texture(os.path.join(item_tex_dir, "tnt_launcher.png"))
    make_grenade_texture(os.path.join(item_tex_dir, "tnt_grenade.png"))
    make_arrow_texture(os.path.join(item_tex_dir, "tnt_arrow.png"))
    entity_tex_dir = os.path.join(RP, "textures", "entity")
    os.makedirs(entity_tex_dir, exist_ok=True)
    make_black_hole_texture(os.path.join(entity_tex_dir, "black_hole.png"))
    # Bedrock usa el nombre corto del item para la capa de armadura en 3a persona
    make_armor_layer(os.path.join(armor_tex_dir, "tnt_chestplate_layer_1.png"))
    make_armor_layer(os.path.join(armor_tex_dir, "tnt_layer_1.png"))

    # item_texture.json con todos los items
    item_texture = {
        "resource_pack_name": "tnts_locos",
        "texture_name": "atlas.items",
        "texture_data": {
            "detonator": {"textures": "textures/items/detonator"},
            "tnt_chestplate": {"textures": "textures/items/tnt_chestplate"},
            "tnt_pickaxe": {"textures": "textures/items/tnt_pickaxe"},
            "tnt_launcher": {"textures": "textures/items/tnt_launcher"},
            "tnt_grenade": {"textures": "textures/items/tnt_grenade"},
            "tnt_arrow": {"textures": "textures/items/tnt_arrow"},
        },
    }
    with open(os.path.join(RP, "textures", "item_texture.json"), "w") as f:
        json.dump(item_texture, f, indent=2)

    # sound_definitions.json con los sonidos sintetizados (ver tnts_forge/tools/gen_sounds.py)
    sound_defs = {
        "format_version": "1.14.0",
        "sound_definitions": {
            "tnts.beep": {"category": "block", "sounds": ["sounds/block/tnt/beep"]},
            "tnts.black_hole.loop": {"category": "ambient", "sounds": ["sounds/entity/black_hole_loop"]},
            "tnts.detonator.click": {"category": "player", "sounds": ["sounds/item/detonator_click"]},
            "tnts.detonator.burst": {"category": "player", "sounds": ["sounds/item/detonator_burst"]},
            "tnts.launcher.shoot": {"category": "player", "sounds": ["sounds/item/launcher_shoot"]},
        },
    }
    for name in textures:
        var = name.replace("tnts_", "")
        sound_defs["sound_definitions"][f"tnts.fuse.{var}"] = {
            "category": "block", "sounds": [f"sounds/block/tnt/fuse_{var}"]}
        sound_defs["sound_definitions"][f"tnts.explode.{var}"] = {
            "category": "block", "sounds": [f"sounds/block/tnt/explode_{var}"]}
    with open(os.path.join(RP, "sound_definitions.json"), "w") as f:
        json.dump(sound_defs, f, indent=2)

    # terrain_texture.json completo (todas las variantes, normal + lit)
    terrain = {
        "resource_pack_name": "tnts_locos",
        "texture_name": "atlas.terrain",
        "padding": 8,
        "num_mip_levels": 4,
        "texture_data": {},
    }
    for name in textures:
        terrain["texture_data"][name] = {"textures": f"textures/blocks/{name}"}
        terrain["texture_data"][name + "_lit"] = {"textures": f"textures/blocks/{name}_lit"}
        terrain["texture_data"][name + "_top"] = {"textures": f"textures/blocks/{name}_top"}
        terrain["texture_data"][name + "_bottom"] = {"textures": f"textures/blocks/{name}_bottom"}
        terrain["texture_data"][name + "_top_lit"] = {"textures": f"textures/blocks/{name}_top_lit"}
    with open(os.path.join(RP, "textures", "terrain_texture.json"), "w") as f:
        json.dump(terrain, f, indent=2)

    # Flipbook: animacion de la llama (lateral y cara superior) de las variantes encendidas
    flipbook = {
        "flipbook_textures": [
            {
                "flipbook_texture": "textures/blocks/" + name + suffix,
                "atlas_tile": name + suffix,
                "ticks_per_frame": 2
            }
            for name in textures for suffix in ("_lit", "_top_lit")
        ]
    }
    with open(os.path.join(RP, "textures", "flipbook_textures.json"), "w") as f:
        json.dump(flipbook, f, indent=2)

    # Icono del pack 128x128 (TNT: fondo oscuro, bloque rojo con franja naranja)
    def icon_pixel(x, y):
        if not (16 <= x < 112 and 16 <= y < 112):
            return (28, 28, 44, 255)
        if x < 24 or y < 24 or x >= 104 or y >= 104:
            return (15, 15, 24, 255)
        if 52 <= y <= 76:
            return (251, 146, 60, 255)  # franja
        return (190, 30, 45, 255)  # cuerpo

    write_png(os.path.join(RP, "pack_icon.png"), (128, 128), icon_pixel)
    write_png(os.path.join(BP, "pack_icon.png"), (128, 128), icon_pixel)

    # Empaquetar .mcaddon (los dos packs dentro del zip)
    out = os.path.join(BASE, "TNTsLocos.mcaddon")
    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
        for folder in ("TNTsLocos_BP", "TNTsLocos_RP"):
            for root, _dirs, files in os.walk(os.path.join(BASE, folder)):
                for f in files:
                    full = os.path.join(root, f)
                    arc = os.path.join(folder, os.path.relpath(full, os.path.join(BASE, folder)))
                    z.write(full, arc)
    print("Texturas generadas y addon creado:")
    print(" ", out)


if __name__ == "__main__":
    main()
