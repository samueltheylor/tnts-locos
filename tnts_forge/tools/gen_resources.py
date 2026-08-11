"""Genera los recursos del mod de Forge: texturas (normal + encendida ANIMADA),
blockstates, modelos, recetas (con tags), loot tables y el item Detonador.

Uso:  python tools/gen_resources.py
"""
import gzip
import json
import os
import struct
import zlib

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))  # tnts_forge
RES = os.path.join(BASE, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "tnts")
DATA = os.path.join(RES, "data", "tnts")
TEX_DIR = os.path.join(ASSETS, "textures", "block")
ITEM_TEX_DIR = os.path.join(ASSETS, "textures", "item")
ENTITY_TEX_DIR = os.path.join(ASSETS, "textures", "entity")

# Bloques (TNTs)
NAMES = ["mega_tnt", "mini_tnt", "lava_tnt", "rapida_tnt", "hielo_tnt",
         "saltarina_tnt", "nuclear_tnt", "limpia_tnt", "rayo_tnt", "trampa_tnt", "oro_tnt",
         "obsidiana_tnt", "crio_tnt", "xp_tnt", "agua_tnt", "arena_tnt",
         "diamante_tnt", "esmeralda_tnt", "negra_tnt", "viento_tnt", "inferno_tnt",
         "hongo_tnt", "miel_tnt", "heal_tnt", "teleport_tnt", "confeti_tnt", "mina_tnt",
         "terremoto_tnt", "meteorito_tnt", "tormenta_tnt", "colosal_tnt", "supernova_tnt",
         "toxica_tnt", "fuegos_tnt", "gravitatoria_tnt",
         "ender_tnt", "bubble_tnt", "solar_tnt"]

# Recetas: nombre -> (ingredientes, cantidad). Cada ingrediente ("item", id) o ("tag", id).
TNT_RECIPES = {
    "mega_tnt":      ([("item", "minecraft:tnt")] * 4, 1),
    "mini_tnt":      ([("item", "minecraft:tnt"), ("tag", "forge:gunpowder")], 2),
    "lava_tnt":      ([("item", "minecraft:tnt"), ("item", "minecraft:lava_bucket")], 1),
    "rapida_tnt":    ([("item", "minecraft:tnt"), ("tag", "forge:dusts/redstone")], 1),
    "hielo_tnt":     ([("item", "minecraft:tnt"), ("item", "minecraft:water_bucket")], 1),
    "saltarina_tnt": ([("item", "minecraft:tnt"), ("tag", "forge:slimeballs")], 1),
    "nuclear_tnt":   ([("item", "minecraft:tnt"), ("tag", "forge:ingots/netherite"), ("tag", "forge:gunpowder")], 1),
    "limpia_tnt":    ([("item", "minecraft:tnt"), ("item", "minecraft:feather")], 1),
    "rayo_tnt":      ([("item", "minecraft:tnt"), ("item", "minecraft:lightning_rod")], 1),
    "trampa_tnt":    ([("item", "minecraft:tnt"), ("item", "minecraft:clock")], 1),
    "oro_tnt":       ([("item", "minecraft:tnt"), ("tag", "forge:ingots/gold")], 1),
    "obsidiana_tnt": ([("item", "minecraft:tnt"), ("item", "minecraft:obsidian")], 1),
    "crio_tnt":      ([("item", "minecraft:tnt"), ("item", "minecraft:blue_ice")], 1),
    "xp_tnt":        ([("item", "minecraft:tnt"), ("item", "minecraft:experience_bottle")], 1),
    "agua_tnt":      ([("item", "minecraft:tnt"), ("item", "minecraft:sponge")], 1),
    "arena_tnt":     ([("item", "minecraft:tnt"), ("tag", "forge:sand")], 1),
    "diamante_tnt":  ([("item", "minecraft:tnt"), ("item", "minecraft:diamond")], 1),
    "esmeralda_tnt": ([("item", "minecraft:tnt"), ("item", "minecraft:emerald")], 1),
    "negra_tnt":     ([("item", "minecraft:tnt"), ("item", "minecraft:crying_obsidian")], 1),
    "viento_tnt":    ([("item", "minecraft:tnt"), ("item", "minecraft:phantom_membrane")], 1),
    "inferno_tnt":   ([("item", "minecraft:tnt"), ("item", "minecraft:magma_cream")], 1),
    "hongo_tnt":     ([("item", "minecraft:tnt"), ("item", "minecraft:red_mushroom")], 1),
    "miel_tnt":      ([("item", "minecraft:tnt"), ("item", "minecraft:honeycomb")], 1),
    "heal_tnt":      ([("item", "minecraft:tnt"), ("item", "minecraft:ghast_tear")], 1),
    "teleport_tnt":  ([("item", "minecraft:tnt"), ("item", "minecraft:ender_pearl")], 1),
    "confeti_tnt":   ([("item", "minecraft:tnt"), ("item", "minecraft:red_dye"), ("item", "minecraft:yellow_dye"), ("item", "minecraft:blue_dye")], 1),
    "mina_tnt":      ([("item", "minecraft:tnt"), ("item", "minecraft:stone_pressure_plate")], 1),
    "terremoto_tnt":  ([("item", "minecraft:tnt"), ("item", "minecraft:pointed_dripstone"), ("item", "minecraft:magma_block")], 1),
    "meteorito_tnt":  ([("item", "minecraft:tnt"), ("item", "minecraft:end_stone"), ("item", "minecraft:fire_charge")], 1),
    "tormenta_tnt":   ([("item", "minecraft:tnt"), ("item", "minecraft:lightning_rod"), ("tag", "forge:dusts/redstone")], 1),
    "colosal_tnt":    ([("item", "tnts:mega_tnt"), ("item", "tnts:mega_tnt"), ("item", "minecraft:tnt")], 1),
    "supernova_tnt":  ([("item", "minecraft:tnt"), ("item", "minecraft:nether_star"), ("item", "minecraft:glowstone")], 1),
    "toxica_tnt":     ([("item", "minecraft:tnt"), ("item", "minecraft:spider_eye"), ("item", "minecraft:fermented_spider_eye")], 1),
    "fuegos_tnt":     ([("item", "minecraft:tnt"), ("item", "minecraft:firework_rocket")], 1),
    "gravitatoria_tnt":([("item", "minecraft:tnt"), ("item", "minecraft:anvil")], 1),
    "ender_tnt":     ([("item", "minecraft:tnt"), ("item", "minecraft:chorus_fruit"), ("item", "minecraft:end_stone")], 1),
    "bubble_tnt":    ([("item", "minecraft:tnt"), ("item", "minecraft:pufferfish"), ("item", "minecraft:kelp")], 1),
    "solar_tnt":     ([("item", "minecraft:tnt"), ("item", "minecraft:blaze_powder"), ("item", "minecraft:glowstone")], 1),
    "tnt_manual":    ([("item", "minecraft:book"), ("item", "minecraft:tnt")], 1),
    "grenade":       ([("item", "minecraft:tnt"), ("item", "minecraft:string")], 2),
    "detonator":     ([("tag", "forge:ingots/iron"), ("tag", "forge:dusts/redstone"), ("item", "minecraft:tnt")], 1),
}

# Paleta por TNT: (base, franja, borde, mota, letras)
PALETTES = {
    "mega_tnt":      ("#7f1d1d", "#f5f5f4", "#450a0a", "#ef4444", "#450a0a"),
    "mini_tnt":      ("#475569", "#f5f5f4", "#0f172a", "#cbd5e1", "#0f172a"),
    "lava_tnt":      ("#c2410c", "#fef3c7", "#431407", "#fde047", "#7c2d12"),
    "rapida_tnt":    ("#14532d", "#f5f5f4", "#052e16", "#a3e635", "#052e16"),
    "hielo_tnt":     ("#0284c7", "#e0f2fe", "#0c4a6e", "#e0f2fe", "#075985"),
    "saltarina_tnt": ("#6d28d9", "#f5f3ff", "#2e1065", "#f5d0fe", "#2e1065"),
    "nuclear_tnt":   ("#292524", "#facc15", "#0a0a0a", "#86efac", "#0a0a0a"),
    "limpia_tnt":    ("#e7e5e4", "#ffffff", "#57534e", "#d6d3d1", "#57534e"),
    "rayo_tnt":      ("#1e3a8a", "#dbeafe", "#172554", "#fde047", "#1e3a8a"),
    "trampa_tnt":    ("#78350f", "#fef3c7", "#292524", "#f59e0b", "#292524"),
    "oro_tnt":       ("#a16207", "#fef9c3", "#422006", "#fde047", "#713f12"),
    "obsidiana_tnt": ("#312e81", "#c4b5fd", "#1e1b4b", "#8b5cf6", "#1e1b4b"),  # obsidiana
    "crio_tnt":      ("#155e75", "#a5f3fc", "#083344", "#67e8f9", "#164e63"),  # hielo azul
    "xp_tnt":        ("#4d7c0f", "#ecfccb", "#1a2e05", "#a3e635", "#365314"),  # experiencia
    "agua_tnt":      ("#075985", "#bae6fd", "#082f49", "#38bdf8", "#0c4a6e"),  # agua
    "arena_tnt":     ("#ca8a04", "#fef9c3", "#713f12", "#fde047", "#854d0e"),  # arena
    "diamante_tnt":  ("#0e7490", "#cffafe", "#164e63", "#67e8f9", "#155e75"),  # diamante
    "esmeralda_tnt": ("#047857", "#d1fae5", "#064e3b", "#34d399", "#065f46"),  # esmeralda
    "negra_tnt":     ("#1c1917", "#a855f7", "#000000", "#d8b4fe", "#000000"),  # agujero negro
    "viento_tnt":    ("#94a3b8", "#f8fafc", "#334155", "#e2e8f0", "#475569"),  # viento
    "inferno_tnt":   ("#9a3412", "#fde047", "#450a0a", "#f97316", "#7c2d12"),  # inferno
    "hongo_tnt":     ("#b91c1c", "#fef3c7", "#7f1d1d", "#fca5a5", "#991b1b"),  # setas
    "miel_tnt":      ("#b45309", "#fef3c7", "#78350f", "#fbbf24", "#92400e"),  # miel
    "heal_tnt":      ("#be185d", "#fce7f3", "#831843", "#f9a8d4", "#9d174d"),  # curacion
    "teleport_tnt":  ("#3b0764", "#e9d5ff", "#1e1b4b", "#c084fc", "#2e1065"),  # teletransporte
    "confeti_tnt":   ("#db2777", "#ffffff", "#4c0519", "#f472b6", "#831843"),  # confeti
    "mina_tnt":      ("#44403c", "#e7e5e4", "#1c1917", "#a8a29e", "#292524"),  # mina
    "terremoto_tnt":  ("#78350f", "#fef3c7", "#451a03", "#d97706", "#451a03"),  # tierra/terremoto
    "meteorito_tnt":  ("#1c1917", "#f97316", "#0a0a0a", "#fb923c", "#0a0a0a"),  # meteorito/fuego
    "tormenta_tnt":   ("#1e1b4b", "#fde047", "#0c0a2a", "#facc15", "#0c0a2a"),  # tormenta/relampago
    "colosal_tnt":    ("#7f1d1d", "#fef9c3", "#450a0a", "#fde047", "#450a0a"),  # colosal/rojo oscuro
    "supernova_tnt":  ("#4c1d95", "#fef9c3", "#2e1065", "#fcd34d", "#2e1065"),  # supernova/purpura dorada
    "toxica_tnt":     ("#14532d", "#dcfce7", "#052e16", "#84cc16", "#052e16"),  # toxica/verde veneno
    "fuegos_tnt":     ("#831843", "#fce7f3", "#4c0519", "#f472b6", "#4c0519"),  # fuegos artificiales/rosa
    "gravitatoria_tnt":("#3b0764", "#e9d5ff", "#1e1b4b", "#a855f7", "#1e1b4b"),  # gravitatoria/purpura
    "ender_tnt":     ("#4c1d95", "#f5f3ff", "#2e1065", "#d8b4fe", "#2e1065"),  # end/purpura oscuro
    "bubble_tnt":    ("#0369a1", "#bae6fd", "#082f49", "#7dd3fc", "#082f49"),  # burbuja/agua clara
    "solar_tnt":     ("#9a3412", "#fef08a", "#450a0a", "#fbbf24", "#7c2d12"),  # solar/amarillo fuego
}


# ---------- utilidades PNG ----------

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
            raw += bytes(get_pixel(x % w, y))
    png = b"\x89PNG\r\n\x1a\n"
    png += png_chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
    png += png_chunk(b"IDAT", zlib.compress(raw, 9))
    png += png_chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)


def hex_rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i : i + 2], 16) for i in (0, 2, 4)) + (255,)


FRAMES = 6
FRAME_HEIGHTS = [5, 3, 4, 2, 4, 3]  # altura de la llama por frame (animacion suave)

# Letras "TNT" para la cara superior (3x5 cada una, como la TNT de vanilla)
TNT_GLYPHS = {
    "T": ("111", "010", "010", "010", "010"),
    "N": ("101", "111", "111", "111", "101"),
}


def tnt_glyph(ix, iy):
    """Devuelve True/False si (ix, iy) (interior 12x12 de la cara superior)
    pinta una letra, o None si esta fuera de las letras."""
    lx = ix
    ly = iy - 3  # 5 filas de letra, centradas en 12 de alto
    if not (0 <= ly < 5):
        return None
    col = 0
    for ch in "TNT":
        if col <= lx < col + 3:
            return TNT_GLYPHS[ch][ly][lx - col] == "1"
        col += 4  # letra de 3 + hueco de 1
    return None


def pixel_side(x, y, pal):
    """Cara lateral estilo TNT mejorada: borde oscuro con sombreado,
    franja clara central, remaches metálicos y motas de pólvora."""
    base, band, border, dot, text_dark = (hex_rgb(c) for c in pal)
    if x in (0, 15) or y in (0, 15):
        return border
    # Sombreado lateral (izquierda más oscura)
    if x == 1:
        r, g, b, a = border
        return (min(r + 15, 255), min(g + 15, 255), min(b + 15, 255), a)
    # Franja central (6-9, estilo vanilla)
    if 5 <= y <= 10:
        c = band
        # Remaches metálicos en la franja (cada 4 px)
        if (x % 4 == 0) and y in (6, 9):
            return text_dark
        # Sombreado en la franja (bordes)
        if y in (5, 10):
            r, g, b, a = band
            return (max(r - 15, 0), max(g - 15, 0), max(b - 15, 0), a)
    else:
        c = base
        # Grano de pólvora (más disperso)
        if (x * 7 + y * 13) % 19 == 0:
            return dot
        # Sombreado superior (brillo)
        if y == 1:
            r, g, b, a = base
            return (min(r + 20, 255), min(g + 20, 255), min(b + 20, 255), a)
        # Sombreado inferior (oscuridad)
        if y == 14:
            r, g, b, a = base
            return (max(r - 20, 0), max(g - 20, 0), max(b - 20, 0), a)
    return c


def pixel_top(x, y, pal):
    """Cara superior mejorada: marco interior oscuro, letras TNT CLARAS en
    el centro con brillo, fondo con gradiente sutil."""
    base, band, border, dot, text_dark = (hex_rgb(c) for c in pal)
    if x in (0, 15) or y in (0, 15):
        return border
    if x in (1, 14) or y in (1, 14):
        return text_dark
    g = tnt_glyph(x - 2, y - 2)
    if g is True:
        # Brillo en la parte superior de las letras
        if y == 3:
            r, g, b, a = band
            return (min(r + 10, 255), min(g + 10, 255), min(b + 10, 255), a)
        return band
    if g is False:
        # Mota sutil
        if (x * 3 + y * 5) % 17 == 0:
            return dot
        # Gradiente sutil en el fondo
        if y <= 2:
            r, g, b, a = base
            return (min(r + 10, 255), min(g + 10, 255), min(b + 10, 255), a)
    return base


def pixel_bottom(x, y, pal):
    """Cara inferior mejorada: lisa con motitas de pólvora y sombreado."""
    base, band, border, dot, text_dark = (hex_rgb(c) for c in pal)
    if x in (0, 15) or y in (0, 15):
        return border
    if x in (1, 14) or y in (1, 14):
        return text_dark
    # Mota de pólvora (más sutil)
    if (x * 7 + y * 13) % 27 == 0:
        return dot
    # Gradiente sutil (superior más claro)
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


def pixel_side_lit(x, y, frame, pal):
    """Cara lateral encendida mejorada: llama animada con más frames y brillo,
    humo sutil en los bordes."""
    fh = FRAME_HEIGHTS[frame % len(FRAME_HEIGHTS)]
    if y <= fh:
        # Llama principal con brillo
        base_flame = flame_px(x, y, frame, fh)
        # Brillo blanco en la punta de la llama
        if y <= 1 and (x + frame) % 3 == 0:
            return (255, 255, 200, 255)
        # Humo oscuro en los bordes de la llama
        if (x == 0 or x == 15) and y <= 2:
            return (60, 50, 40, 200)
        return base_flame
    return pixel_side(x, y, pal)


def pixel_top_lit(x, y, frame, pal):
    """Cara superior encendida mejorada: llama con gradiente más suave,
    chispas brillantes y brasas cálidas."""
    # Chispas brillantes (más frecuentes)
    if (x + y + frame) % 4 == 0:
        return (255, 245, 160, 255)  # chispa amarilla
    if (x * 5 + y * 3 + frame * 7) % 13 == 0:
        return (255, 200, 80, 255)   # chispa naranja
    # Gradiente de llama (más suave)
    t = 1 - (y / 15)
    r = 255
    g = int(80 + 175 * t)
    b = int(5 + 50 * t)
    # Brasas cálidas
    if (x * 3 + y * 7 + frame) % 9 == 0:
        return (180, 50, 15, 255)
    # Brillo central
    if 6 <= x <= 9 and 6 <= y <= 9 and frame % 2 == 0:
        return (255, 255, 180, 255)
    return (r, g, b, 255)


def make_tnt_textures(name, pal):
    """Texturas estilo vanilla TNT: cara superior con letras, inferior lisa,
    lateral con franja, y las variantes encendidas animadas (tira vertical)."""
    write_png(os.path.join(TEX_DIR, f"tnts_{name}.png"), (16, 16),
              lambda x, y: pixel_side(x, y, pal))
    write_png(os.path.join(TEX_DIR, f"tnts_{name}_top.png"), (16, 16),
              lambda x, y: pixel_top(x, y, pal))
    write_png(os.path.join(TEX_DIR, f"tnts_{name}_bottom.png"), (16, 16),
              lambda x, y: pixel_bottom(x, y, pal))
    write_png(os.path.join(TEX_DIR, f"tnts_{name}_lit.png"), (16, 16 * FRAMES),
              lambda x, y: pixel_side_lit(x, y % 16, y // 16, pal))
    write_png(os.path.join(TEX_DIR, f"tnts_{name}_top_lit.png"), (16, 16 * FRAMES),
              lambda x, y: pixel_top_lit(x, y % 16, y // 16, pal))
    for suffix in ("_lit", "_top_lit"):
        with open(os.path.join(TEX_DIR, f"tnts_{name}{suffix}.png.mcmeta"), "w", encoding="utf-8") as f:
            json.dump({"animation": {"frametime": 2, "width": 16, "height": 16}}, f)


def make_detonator_texture():
    """Detonador remoto mejorado: cuerpo metálico con textura, antena, LED y botón.
    Se ve como un dispositivo real de detonación."""
    def pixel(x, y):
        # Fondo transparente
        if x in (0, 15) or y in (0, 15):
            return (0, 0, 0, 0)
        # Antena (línea delgada con brillo)
        if x == 8 and y == 0:
            return (180, 180, 195, 255)  # punta antena
        if x == 8 and y in (1, 2, 3):
            return (140, 140, 155, 255)  # antena
        if x in (7, 9) and y == 3:
            return (100, 100, 115, 255)  # base antena
        # Cuerpo principal (gradiente metálico)
        if 2 <= x <= 13 and 4 <= y <= 13:
            # Borde exterior oscuro
            if x in (2, 13) or y in (4, 13):
                return (30, 30, 38, 255)
            # Interior con gradiente
            if x in (3, 12):
                return (55, 55, 65, 255)  # borde lateral oscuro
            if y == 5 and x in (3, 12):
                return (100, 100, 115, 255)  # brillo superior
            # Pantalla LED (zona verde)
            if 5 <= x <= 10 and 5 <= y <= 7:
                if x in (5, 10) or y in (5, 7):
                    return (20, 60, 20, 255)  # borde LED
                return (30, 180, 50, 255)    # pantalla verde
            # Botón rojo grande
            if 5 <= x <= 10 and 9 <= y <= 12:
                if x in (5, 10) or y in (9, 12):
                    return (120, 20, 20, 255)  # borde botón
                if x in (6, 9) and y in (10, 11):
                    return (255, 70, 70, 255)  # brillo botón
                return (200, 35, 35, 255)    # botón rojo
            # Zona media (textura metálica)
            if (x * 3 + y * 7) % 11 == 0:
                return (90, 90, 105, 255)  # grano metálico
            return (75, 75, 88, 255)       # cuerpo metálico
        return (0, 0, 0, 0)

    write_png(os.path.join(ITEM_TEX_DIR, "detonator.png"), (16, 16), pixel)


def make_launcher_texture():
    """Lanzador de TNT mejorado: tubo de hierro con sombreado, boca amplia,
    botón rojo con brillo y detalles mecánicos."""
    def pixel(x, y):
        if x in (0, 15) or y in (0, 15):
            return (0, 0, 0, 0)
        # Tubo principal (4-11, 2-13)
        if 4 <= x <= 11 and 2 <= y <= 13:
            # Borde exterior oscuro
            if x in (4, 11) or y in (2, 13):
                return (35, 35, 42, 255)
            # Boca abierta (parte superior)
            if y == 3:
                if x in (5, 10):
                    return (60, 60, 70, 255)  # bisagra
                return (45, 45, 55, 255)       # interior boca
            # Interior oscuro (boca)
            if y == 4:
                return (20, 20, 25, 255)  # fondo oscuro boca
            # Borde metálico de la boca
            if y == 5 and x in (5, 10):
                return (120, 120, 135, 255)  # brillo borde
            # Cuerpo principal con gradiente
            if 6 <= y <= 10:
                if x in (5, 10):
                    if y == 6:
                        return (110, 110, 125, 255)  # brillo superior
                    return (80, 80, 92, 255)         # lateral
                # Botón rojo
                if 7 <= x <= 9 and y in (7, 8):
                    if x == 7 and y == 7:
                        return (255, 80, 80, 255)  # brillo botón
                    if x in (7, 9) or y in (7, 8):
                        return (200, 40, 40, 255)  # borde botón
                    return (180, 30, 30, 255)       # botón
                # Detalles mecánicos
                if y == 9 and x in (6, 8):
                    return (100, 100, 115, 255)  # tornillo
                return (70, 70, 82, 255)         # cuerpo
            # Base inferior
            if y == 11:
                if x in (5, 10):
                    return (50, 50, 60, 255)
                return (60, 60, 70, 255)
            if y == 12:
                return (40, 40, 48, 255)  # base oscura
        return (0, 0, 0, 0)

    write_png(os.path.join(ITEM_TEX_DIR, "launcher.png"), (16, 16), pixel)


def make_chestplate_texture():
    """Peto de TNT mejorado: armadura roja con sombreado, franja TNT,
    correas de cuero y hombreras reforzadas."""
    def pixel(x, y):
        # Fondo transparente
        if x in (0, 15) or y in (0, 15):
            return (0, 0, 0, 0)
        RED = (190, 30, 45, 255)
        RED_LIGHT = (220, 60, 75, 255)
        RED_DARK = (120, 18, 25, 255)
        BAND = (245, 245, 244, 255)
        EDGE = (35, 30, 28, 255)
        LEATHER = (110, 72, 40, 255)
        LEATHER_DARK = (75, 48, 25, 255)
        # --- Hombros reforzados (2-4 y 11-13, 3-5) ---
        if 2 <= x <= 4 and 3 <= y <= 5:
            if y == 3:
                return (160, 28, 38, 255)  # borde superior
            if x in (2, 4):
                return RED_DARK
            if y == 4 and x == 3:
                return (220, 60, 75, 255)  # brillo hombro
            return RED
        if 11 <= x <= 13 and 3 <= y <= 5:
            if y == 3:
                return (160, 28, 38, 255)
            if x in (11, 13):
                return RED_DARK
            if y == 4 and x == 12:
                return (220, 60, 75, 255)
            return RED
        # --- Correas de cuero (verticales) ---
        if (x in (5, 6, 9, 10)) and 4 <= y <= 13:
            if x in (5, 10):
                return LEATHER_DARK  # borde oscuro de la correa
            if y in (4, 13):
                return LEATHER_DARK  # borde superior/inferior
            # Hebilla de metal cada 3 filas
            if y in (6, 9, 12) and x in (5, 10):
                return (180, 175, 165, 255)  # brillo metálico
            return LEATHER
        # --- Cuerpo del peto (zonas entre correas) ---
        if 3 <= x <= 13 and 6 <= y <= 13:
            # Borde exterior
            if x in (3, 13) or y == 13:
                return EDGE
            # Franja TNT blanca (clásica)
            if y in (9, 10):
                return BAND
            # Sombreado
            if y == 6:
                return RED_LIGHT  # brillo superior
            if y == 7:
                return (205, 40, 55, 255)
            if x in (7, 8) and y == 11:
                # Logo TNT mini (centro)
                if x == 7:
                    return (255, 255, 255, 255)  # T blanca
                return (200, 35, 45, 255)
            if y == 12:
                return RED_DARK  # sombra inferior
            if x in (3, 4, 11, 12):
                return RED_DARK  # sombra lateral
            return RED
        return (0, 0, 0, 0)

    write_png(os.path.join(ITEM_TEX_DIR, "tnt_chestplate.png"), (16, 16), pixel)


def make_pickaxe_texture():
    """Pico de TNT mejorado: mango de madera con veta, cabeza roja con
    sombreado metálico, filo brillante y detalles de TNT."""
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
        # Mango de madera (diagonal 2,14 → 7,9)
        if 2 <= x <= 7:
            row = 14 - (x - 2)
            if y == row:
                if (x + y) % 3 == 0:
                    return WOOD_DARK   # veta oscura
                return WOOD
            if y == row - 1 and x >= 3:
                return WOOD_LIGHT  # borde superior del mango (brillo)
        # Cabeza de pico (estilo TNT, 4-13, 2-8)
        if 4 <= x <= 13 and 2 <= y <= 8:
            # Filo metálico (punta izquierda)
            if x == 4 and y == 5:
                return METAL  # punta del filo
            if x == 4 and y in (4, 6):
                return METAL_DARK
            # Borde exterior
            if x in (4, 13) or y in (2, 8):
                return EDGE
            # Borde interior superior
            if y == 3:
                if x == 5:
                    return RED_LIGHT  # brillo
                return RED_DARK
            # Cuerpo rojo con franja TNT
            if y == 5:
                return BAND  # franja blanca clásica
            if y == 6:
                return (200, 40, 55, 255)  # sombra bajo la franja
            # Brillo metálico del filo
            if x == 5 and y in (4, 6, 7):
                return METAL
            if x == 6 and y in (2, 3):
                return METAL_DARK
            # Toroide rojo
            if y == 7:
                return RED_DARK  # sombra inferior
            if y == 4:
                return RED_LIGHT if x in (7, 8) else RED
            return RED
        return (0, 0, 0, 0)

    write_png(os.path.join(ITEM_TEX_DIR, "tnt_pickaxe.png"), (16, 16), pixel)


def make_grenade_texture():
    """Granada de TNT mejorada: bola esférica con sombreado, franja TNT,
    mecha detallada con chispas y brillo."""
    def pixel(x, y):
        if x in (0, 15) or y in (0, 15):
            return (0, 0, 0, 0)
        RED = (190, 30, 45, 255)
        RED_LIGHT = (225, 65, 80, 255)
        RED_DARK = (110, 15, 22, 255)
        EDGE = (80, 10, 14, 255)
        BAND = (240, 240, 235, 255)
        # Mecha encendida (parte superior)
        if 10 <= x <= 12 and 1 <= y <= 4:
            if y == 1 and x == 11:
                return (255, 250, 180, 255)  # chispa principal
            if y == 2 and x in (11, 12):
                return (255, 220, 100, 255)  # llama
            if y == 2 and x == 10:
                return (255, 180, 60, 255)  # llama lateral
            if y == 3 and x == 11:
                return (200, 140, 40, 255)  # llama baja
            if y == 4 and x == 11:
                return (110, 80, 40, 255)  # mecha
            return (90, 65, 30, 255)       # mecha
        # Base de la mecha
        if 10 <= x <= 12 and y == 5:
            if x == 11:
                return (60, 40, 18, 255)  # mecha gruesa
            return (40, 30, 15, 255)
        # Bola esférica (5-13, 5-14)
        if 3 <= x <= 12 and 5 <= y <= 14:
            # Distancia al centro para efecto esférico
            cx, cy = 7.5, 9.5
            dist = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            # Borde exterior oscuro
            if dist > 4.8:
                return EDGE if dist > 5.2 else RED_DARK
            # Franja TNT (parte central)
            if y in (9, 10):
                if dist > 2.5:
                    return BAND  # borde blanco
                return (200, 200, 195, 255)  # blanco sombreado
            # Sombreado esférico
            if dist < 2.0:
                return RED_LIGHT  # zona iluminada
            if dist < 3.0:
                return RED
            if dist < 4.0:
                return RED_DARK  # zona en sombra
            return EDGE
        return (0, 0, 0, 0)

    write_png(os.path.join(ITEM_TEX_DIR, "grenade.png"), (16, 16), pixel)


def make_black_hole_texture():
    """Textura 64x32 del Agujero Negro con NUCLEO REAL:
    - Filas 0-15: gradientes para las barras de los anillos de acrecion
      (morado brillante para el exterior, rosa->blanco para el interior).
    - Filas 16-31: disco negro del nucleo (el portal), negro absoluto con
      un tinte morado apenas visible."""
    def pixel(x, y):
        if y < 16:
            if x < 32:
                # anillo exterior: morado profundo -> brillante -> profundo
                t = x / 31.0
                m = 1.0 - abs(t - 0.5) * 2.0
                return (int(60 + 150 * m), int(15 + 95 * m), int(130 + 125 * m), 255)
            else:
                # anillo interior: rosa -> blanco -> rosa
                t = (x - 32) / 31.0
                m = 1.0 - abs(t - 0.5) * 2.0
                return (int(205 + 50 * m), int(130 + 125 * m), int(235 + 20 * m), 255)
        else:
            # nucleo: disco negro absoluto con tinte morado apenas visible
            g = 3 if ((x * 7 + y * 13) % 9 == 0) else 1
            return (g, 1, 6, 255)

    write_png(os.path.join(ENTITY_TEX_DIR, "black_hole.png"), (64, 32), pixel)


def make_meteor_texture():
    """Textura 64x32 del meteorito 3D: roca gris con brasas naranjas y
    grietas brillantes (4 caras laterales iguales)."""
    def pixel(x, y):
        if y >= 16 and x >= 64:
            return (60, 40, 30, 255)
        if y < 16 and x < 64:
            px = x % 16
            # grieta brillante
            if (px + y) % 7 == 0:
                return (255, 160, 60, 255)  # brasa
            if (px * 3 + y * 5) % 13 == 0:
                return (150, 70, 30, 255)   # brasa tenue
            # cuerpo rocoso con gradiente
            if y < 4:
                return (90, 80, 75, 255)    # mas oscuro arriba (en sombra)
            if y > 11:
                return (180, 90, 50, 255)   # mas rojo abajo (quemado)
            return (130, 95, 70, 255)       # roca
        return (40, 30, 25, 255)            # cara superior/inferior

    write_png(os.path.join(ENTITY_TEX_DIR, "meteor.png"), (64, 32), pixel)


def make_storm_cloud_texture():
    """Textura 64x32 de la nube de tormenta 3D: gris oscuro con zonas mas
    claras (algodon de nube) y chispas electricas amarillas."""
    def pixel(x, y):
        if y < 16 and x < 64:
            px = x % 16
            # chispas electricas
            if (px * 7 + y * 11) % 19 == 0:
                return (255, 220, 80, 255)
            # base gris oscura con gradiente
            if y < 3:
                return (70, 70, 80, 255)    # cima mas clara
            if y > 12:
                return (35, 35, 42, 255)    # base oscura
            if (px + y) % 5 == 0:
                return (55, 55, 65, 255)    # bolas de nube
            return (45, 45, 52, 255)
        return (45, 45, 52, 255)            # superior/inferior

    write_png(os.path.join(ENTITY_TEX_DIR, "storm_cloud.png"), (64, 32), pixel)


def make_supernova_texture():
    """Textura 64x32 de la supernova 3D: esfera dorada translucida con
    nucleo blanco brillante y corona naranja."""
    def sphere(px, py):
        cx, cy = 7.5, 7.5
        d = ((px - cx) ** 2 + (py - cy) ** 2) ** 0.5
        if d > 6.5:
            return (0, 0, 0, 0)             # transparente
        if d > 5.8:
            return (255, 200, 80, 180)      # corona naranja
        if d > 4.5:
            return (255, 240, 160, 220)     # disco dorado
        if d > 2.0:
            return (255, 255, 230, 255)     # interior claro
        return (255, 255, 255, 255)         # nucleo blanco

    def pixel(x, y):
        if y < 16 and x < 64:
            return sphere(x % 16, y)
        return (0, 0, 0, 0)                 # superior/inferior transparentes

    write_png(os.path.join(ENTITY_TEX_DIR, "supernova.png"), (64, 32), pixel)


def make_armor_layer():
    """Capa de armadura del Peto de TNT (64x32) mejorada: torso y brazos rojos
    con sombreado, franja TNT, correas, hombreras reforzadas y detalles.
    Regiones reales del layout de vanilla (cuerpo en x 16-47, brazos en x 48-55)."""
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
        # Borde exterior
        if x in (16, 47) or y == 31:
            return EDGE
        # Hombrera superior (gradiente)
        if y == 20:
            return RED_LIGHT  # brillo
        if y == 21:
            return (210, 48, 62, 255)
        # Correas de cuero
        if x in (22, 23, 30, 31, 38, 39):
            if y in (20, 31):
                return LEATHER_DARK
            if y in (24, 27, 30):
                return METAL  # hebillas
            return LEATHER
        # Cuerpo rojo
        if y in (24, 25):
            return BAND if x not in (22, 23, 30, 31, 38, 39) else BAND_SHADE
        if y in (26, 27):
            return RED_VDARK  # sombra bajo la franja
        # Logo TNT mini en el centro
        if y == 23 and x in (29, 30, 31, 32, 33, 34):
            if x in (29, 30, 34):
                return (255, 255, 255, 255)  # T y parte de N
            if x in (31, 32):
                return (245, 245, 244, 255)  # franja interior
            return (200, 35, 45, 255)
        # Sombreado
        if x <= 18:
            return RED_DARK  # lado izquierdo oscuro
        if x >= 45:
            return RED_DARK  # lado derecho oscuro
        if y == 30:
            return RED_VDARK  # borde inferior
        return RED

    def arm_px(x, y):
        if not (48 <= x <= 55 and 16 <= y <= 31):
            return (0, 0, 0, 0)
        if x in (48, 55) or y == 31:
            return EDGE
        # Hombrera reforzada
        if y in (16, 17):
            return RED_DARK if x in (48, 55) else RED_LIGHT
        # Correa
        if x in (50, 51):
            if y in (20, 28):
                return METAL  # hebillas
            return LEATHER
        # Franja
        if y in (24, 25):
            return BAND
        # Sombreado
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

    write_png(os.path.join(ASSETS, "textures", "models", "armor", "tnt_layer_1.png"), (64, 32), pixel)


def make_arrow_texture():
    """Flecha de TNT mejorada: punta roja con franja TNT, palo de madera
    con plumas y detalles."""
    def pixel(x, y):
        WOOD = (139, 90, 43, 255)
        WOOD_DARK = (100, 62, 28, 255)
        WOOD_LIGHT = (170, 115, 55, 255)
        RED = (190, 30, 45, 255)
        RED_LIGHT = (220, 60, 75, 255)
        EDGE = (35, 30, 28, 255)
        BAND = (245, 245, 244, 255)
        # Palo diagonal (2,13) → (7,8) con sombreado
        if 2 <= x <= 7:
            row = 13 - (x - 2)
            if y == row:
                return WOOD_DARK if (x + y) % 3 == 0 else WOOD
            if y == row - 1 and x >= 3:
                return WOOD_LIGHT  # brillo del palo
        # Plumas (parte trasera)
        if x == 2 and y in (12, 13):
            return (180, 175, 165, 255)  # plumas claras
        if x == 1 and y == 13:
            return (160, 155, 145, 255)
        # Cabeza TNT (7-14, 1-8)
        if 7 <= x <= 14 and 1 <= y <= 8:
            # Borde
            if x in (7, 14) or y in (1, 8):
                return EDGE
            # Punta metálica
            if x == 13 and y == 4:
                return (140, 140, 155, 255)  # brillo punta
            if x == 13 and y in (3, 5):
                return (80, 80, 92, 255)
            # Franja blanca
            if y in (4, 5):
                return BAND
            # Sombreado
            if y == 2:
                return RED_LIGHT
            if y == 7:
                return (120, 18, 25, 255)  # sombra
            return RED
        return (0, 0, 0, 0)

    write_png(os.path.join(ITEM_TEX_DIR, "tnt_arrow.png"), (16, 16), pixel)


def make_tnt_table_texture():
    """Mesa de TNTs (estacion de trabajo del aldeano): mesa de madera con un
    bloque de TNT sobre ella y herramientas de herreria."""
    def pixel(x, y):
        WOOD = (139, 90, 43, 255)
        WOOD_DARK = (100, 62, 28, 255)
        WOOD_LIGHT = (172, 118, 58, 255)
        RED = (190, 30, 45, 255)
        RED_DARK = (120, 18, 25, 255)
        BAND = (245, 245, 244, 255)
        EDGE = (35, 30, 28, 255)
        # patas de la mesa
        if x in (2, 13) and 10 <= y <= 15:
            return WOOD_DARK
        # tablero de la mesa
        if 1 <= x <= 14 and 8 <= y <= 10:
            if y == 8:
                return WOOD_LIGHT
            if y == 10:
                return WOOD_DARK
            if (x * 3 + y * 7) % 11 == 0:
                return WOOD_DARK
            return WOOD
        # bloque de TNT encima
        if 4 <= x <= 11 and 2 <= y <= 7:
            if x in (4, 11) or y in (2, 7):
                return EDGE
            if y in (4, 5):
                return BAND
            if y == 3:
                return (215, 45, 60, 255)
            return RED
        # mecha encendida
        if x == 8 and y == 1:
            return (255, 220, 100, 255)
        if x == 8 and y == 0:
            return (255, 250, 180, 255)
        return (0, 0, 0, 0)

    write_png(os.path.join(TEX_DIR, "tnt_table.png"), (16, 16), pixel)


def make_tnt_altar_texture():
    """Altar del Rey TNT: plataforma de obsidiana con un bloque de TNT
    brillante en el centro y runas moradas."""
    def pixel(x, y):
        OBS = (36, 24, 66, 255)
        OBS_L = (62, 42, 104, 255)
        OBS_D = (22, 14, 40, 255)
        PURPLE = (168, 85, 247, 255)
        RED = (190, 30, 45, 255)
        RED_L = (225, 65, 80, 255)
        BAND = (245, 245, 244, 255)
        EDGE = (12, 8, 22, 255)
        # pedestal de obsidiana
        if 1 <= x <= 14 and 11 <= y <= 15:
            if y == 15 or x in (1, 14):
                return EDGE
            if y == 11:
                return OBS_L
            if (x * 5 + y * 3) % 9 == 0:
                return PURPLE
            return OBS
        # corona de piedra con runas
        if 3 <= x <= 12 and 8 <= y <= 10:
            if x in (3, 12):
                return OBS_D
            if (x * 3 + y * 7) % 11 == 0:
                return PURPLE
            return OBS_L
        # bloque de TNT central brillante
        if 4 <= x <= 11 and 2 <= y <= 7:
            if x in (4, 11) or y in (2, 7):
                return EDGE
            if y in (4, 5):
                return BAND
            if y == 3:
                return RED_L
            return RED
        # brillo superior
        if 7 <= x <= 8 and y == 1:
            return (255, 240, 160, 255)
        return (0, 0, 0, 0)

    write_png(os.path.join(TEX_DIR, "tnt_altar.png"), (16, 16), pixel)


def make_tnt_manual_texture():
    """Manual de TNTs: un libro rojo con la letra T y una mecha."""
    def pixel(x, y):
        RED = (190, 30, 45, 255)
        RED_D = (120, 18, 25, 255)
        RED_L = (225, 65, 80, 255)
        PAGE = (245, 240, 225, 255)
        EDGE = (60, 8, 14, 255)
        # cuerpo del libro
        if 2 <= x <= 13 and 3 <= y <= 13:
            if x in (2, 13) or y in (3, 13):
                return EDGE
            if y == 4:
                return RED_L
            # lomo
            if x in (5, 6):
                if y in (8, 9):
                    return PAGE
                return RED_D
            # letra T blanca en la portada
            if 8 <= x <= 11 and 7 <= y <= 11:
                if y == 7 or x == 9:
                    return PAGE
                return RED
            return RED
        # mecha en la esquina
        if x == 13 and y == 2:
            return (110, 80, 40, 255)
        if x == 14 and y == 1:
            return (255, 220, 100, 255)
        return (0, 0, 0, 0)

    write_png(os.path.join(ITEM_TEX_DIR, "tnt_manual.png"), (16, 16), pixel)


def make_tnt_disc_texture():
    """Disco de musica de TNTs: disco negro con centro rojo y franja TNT."""
    def pixel(x, y):
        cx, cy = 7.5, 7.5
        d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
        if d > 7.0:
            return (0, 0, 0, 0)
        if d > 6.2:
            return (30, 30, 38, 255)   # borde
        if d < 2.2:
            return (190, 30, 45, 255)  # centro rojo
        # franja blanca
        if 2.2 <= d < 3.4:
            return (245, 245, 244, 255)
        # brillo del vinilo
        if (x + y) % 3 == 0:
            return (70, 70, 82, 255)
        return (45, 45, 52, 255)

    write_png(os.path.join(ITEM_TEX_DIR, "tnt_disc.png"), (16, 16), pixel)


def make_tnt_king_crown_texture():
    """Corona del Rey TNT: corona dorada con gemas rojas."""
    def pixel(x, y):
        GOLD = (251, 191, 36, 255)
        GOLD_D = (180, 130, 20, 255)
        GOLD_L = (255, 225, 110, 255)
        EDGE = (110, 75, 8, 255)
        GEM = (200, 35, 45, 255)
        # base de la corona
        if 3 <= x <= 12 and 6 <= y <= 12:
            if x in (3, 12) or y in (6, 12):
                return EDGE
            # gemas
            if y in (9, 10) and x in (5, 7, 9):
                return GEM
            if y == 7 and x in (5, 6, 9, 10):
                return GOLD_L
            return GOLD
        # puntas de la corona
        for px, height in ((4, 4), (6, 3), (8, 4), (10, 3), (12, 4)):
            if x == px and y <= height:
                return GOLD if y < height else GOLD_L
            if x == px - 1 and y <= height:
                return GOLD_D
        return (0, 0, 0, 0)

    write_png(os.path.join(ITEM_TEX_DIR, "tnt_king_crown.png"), (16, 16), pixel)


def make_king_texture():
    """Textura 64x64 del Rey TNT: cubo gigante de TNT con cara enfadada
    (ojos amarillos y cejas) y corona dorada encima."""
    def tnt_side(px, py, face=False):
        # cara lateral del cubo (16x16)
        RED = (150, 24, 36, 255)
        RED_D = (105, 15, 24, 255)
        RED_L = (195, 40, 55, 255)
        BAND = (235, 235, 230, 255)
        EDGE = (45, 10, 16, 255)
        if px in (0, 15) or py in (0, 15):
            return EDGE
        if 5 <= py <= 10:
            if py in (5, 10):
                r, g, b, a = BAND
                return (max(r - 25, 0), max(g - 25, 0), max(b - 25, 0), a)
            return BAND
        if face and 4 <= px <= 11 and 3 <= py <= 12:
            # cara enfadada: ojos amarillos + cejas + boca
            if py in (5, 6) and px in (4, 5, 6, 9, 10, 11):
                return (255, 214, 60, 255)          # ojos
            if py == 4 and px in (4, 11):
                return (255, 214, 60, 255)          # puntas de las cejas
            if py in (9, 10) and px in (6, 7, 8, 9):
                return (20, 8, 10, 255)             # boca gruesa
            if py in (3, 12):
                return (190, 30, 45, 255)           # zona de la cara
        if py == 3:
            return RED_L
        if py == 12:
            return RED_D
        if px == 1:
            return RED_D
        return RED

    def pixel(x, y):
        # cuerpo: filas 0-15 -> 4 caras laterales; filas 16-31 -> top/bottom
        if y < 16:
            face = 0 <= x < 16  # la cara frontal lleva la cara enfadada
            return tnt_side(x % 16, y, face)
        if y < 32:
            # cara superior (oscura con borde claro)
            if x < 16:
                if x % 16 in (0, 15) or y % 16 in (0, 15):
                    return (255, 214, 60, 255)      # borde dorado
                return (130, 18, 28, 255)
            # cara inferior
            return (90, 12, 20, 255)
        # corona: filas 32-47 -> 4 lados (12x4 cada uno), filas 48-63 -> top/bottom
        if y < 36:
            side = (x // 12) % 4
            px = x % 12
            py = y - 32
            if px in (0, 11) or py in (0, 3):
                return (110, 75, 8, 255)            # borde oro oscuro
            if py == 1 and px in (2, 4, 6, 8):
                return (200, 35, 45, 255)           # gemas rojas
            if py == 1 and px == 10:
                return (255, 225, 110, 255)
            return (251, 191, 36, 255)              # oro
        if 48 <= y < 52:
            px = x % 12
            py = y - 48
            if px in (0, 11) or py in (0, 3):
                return (110, 75, 8, 255)
            return (255, 225, 110, 255)             # top dorado
        if 52 <= y < 56:
            return (150, 100, 14, 255)              # bottom dorado
        return (0, 0, 0, 255)

    write_png(os.path.join(ENTITY_TEX_DIR, "tnt_king.png"), (64, 64), pixel)


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)


def write_sounds_json():
    sounds = {
        "block.tnt.beep": {"sounds": ["tnts:block/tnt/beep"]},
        "entity.black_hole.loop": {"sounds": ["tnts:entity/black_hole_loop"]},
        "item.detonator.click": {"sounds": ["tnts:item/detonator_click"]},
        "item.detonator.burst": {"sounds": ["tnts:item/detonator_burst"]},
        "item.launcher.shoot": {"sounds": ["tnts:item/launcher_shoot"]},
        "item.shears.defuse": {"sounds": ["tnts:item/shears_defuse"]},
        "entity.tnt_king.roar": {"sounds": ["tnts:entity/tnt_king_roar"]},
        "music.tnts.tema": {"sounds": ["tnts:music/tnts_tema"]},
    }
    for name in NAMES:
        sounds[f"block.tnt.fuse.{name}"] = {"sounds": [f"tnts:block/tnt/fuse_{name}"]}
        sounds[f"block.tnt.explode.{name}"] = {"sounds": [f"tnts:block/tnt/explode_{name}"]}
    write(os.path.join(ASSETS, "sounds.json"), sounds)


def write_advancements():
    adv_dir = os.path.join(DATA, "advancements")
    tags_item = os.path.join(DATA, "tags", "items")
    tags_block = os.path.join(DATA, "tags", "blocks")

    # tags
    write(os.path.join(tags_item, "tnts.json"),
          {"replace": False, "values": [f"tnts:{n}" for n in NAMES]})
    write(os.path.join(tags_item, "igniters.json"),
          {"replace": False, "values": ["minecraft:flint_and_steel", "minecraft:fire_charge"]})
    write(os.path.join(tags_block, "tnts_blocks.json"),
          {"replace": False, "values": [f"tnts:{n}" for n in NAMES]})

    # raiz
    write(os.path.join(adv_dir, "root.json"), {
        "display": {
            "icon": {"item": "tnts:mega_tnt"},
            "title": {"translate": "advancements.tnts.root.title"},
            "description": {"translate": "advancements.tnts.root.description"},
            "background": "minecraft:textures/block/tnt_side.png",
            "frame": "task",
            "show_toast": True,
            "announce_to_chat": True,
            "hidden": False,
        },
        "criteria": {
            "got_tnt": {"trigger": "minecraft:inventory_changed",
                         "conditions": {"items": [{"tag": "tnts:tnts"}]}}
        },
    })

    # encender la primera
    write(os.path.join(adv_dir, "first_boom.json"), {
        "parent": "tnts:root",
        "display": {
            "icon": {"item": "minecraft:flint_and_steel"},
            "title": {"translate": "advancements.tnts.first_boom.title"},
            "description": {"translate": "advancements.tnts.first_boom.description"},
            "frame": "task",
            "show_toast": True,
            "announce_to_chat": True,
            "hidden": False,
        },
        "criteria": {"ignite": {"trigger": "tnts:ignited"}},
    })

    # craftear cada variante
    for name in NAMES:
        write(os.path.join(adv_dir, f"craft_{name}.json"), {
            "parent": "tnts:root",
            "display": {
                "icon": {"item": f"tnts:{name}"},
                "title": {"translate": f"advancements.tnts.craft.{name}.title"},
                "description": {"translate": f"advancements.tnts.craft.{name}.description"},
                "frame": "task",
                "show_toast": True,
                "announce_to_chat": True,
                "hidden": False,
            },
            "criteria": {"craft": {"trigger": "minecraft:recipe_crafted",
                                     "conditions": {"recipe_id": f"tnts:{name}"}}},
        })

    # vencer al Rey TNT
    write(os.path.join(adv_dir, "defeat_king.json"), {
        "parent": "tnts:root",
        "display": {
            "icon": {"item": "tnts:tnt_king_crown"},
            "title": {"translate": "advancements.tnts.defeat_king.title"},
            "description": {"translate": "advancements.tnts.defeat_king.description"},
            "frame": "challenge",
            "show_toast": True,
            "announce_to_chat": True,
            "hidden": False,
        },
        "criteria": {
            "kill": {"trigger": "minecraft:player_killed_entity",
                      "conditions": {"entity": [{"condition": "minecraft:entity_properties",
                                                   "predicate": {"type": "tnts:tnt_king"},
                                                   "entity": "this"}]}}
        },
    })

    # detonacion masiva con el detonador (10+ de golpe)
    write(os.path.join(adv_dir, "mass_detonation.json"), {
        "parent": "tnts:root",
        "display": {
            "icon": {"item": "tnts:detonator"},
            "title": {"translate": "advancements.tnts.mass_detonation.title"},
            "description": {"translate": "advancements.tnts.mass_detonation.description"},
            "frame": "goal",
            "show_toast": True,
            "announce_to_chat": True,
            "hidden": False,
        },
        "criteria": {"mass": {"trigger": "tnts:mass_detonated"}},
    })

    # explotar todas las variantes (reto oculto)
    write(os.path.join(adv_dir, "all_variants.json"), {
        "parent": "tnts:root",
        "display": {
            "icon": {"item": "tnts:nuclear_tnt"},
            "title": {"translate": "advancements.tnts.all_variants.title"},
            "description": {"translate": "advancements.tnts.all_variants.description"},
            "frame": "challenge",
            "show_toast": True,
            "announce_to_chat": True,
            "hidden": True,
        },
        "criteria": {
            f"explode_{n}": {"trigger": "tnts:exploded",
                              "conditions": {"block": f"tnts:{n}"}}
            for n in NAMES
        },
    })


LANG_ES = {
    "itemGroup.tnts": "TNTs Locas",
    "block.tnts.mega_tnt": "Mega TNT",
    "block.tnts.mini_tnt": "Mini TNT",
    "block.tnts.lava_tnt": "TNT de Lava",
    "block.tnts.rapida_tnt": "TNT Instantánea",
    "block.tnts.hielo_tnt": "TNT de Hielo",
    "block.tnts.saltarina_tnt": "TNT Saltarina",
    "block.tnts.nuclear_tnt": "TNT Nuclear",
    "block.tnts.limpia_tnt": "TNT Limpia",
    "block.tnts.rayo_tnt": "TNT de Rayo",
    "block.tnts.trampa_tnt": "TNT Trampa",
    "block.tnts.oro_tnt": "TNT de Oro",
    "block.tnts.obsidiana_tnt": "TNT de Obsidiana",
    "block.tnts.crio_tnt": "TNT Criogénica",
    "block.tnts.xp_tnt": "TNT de Experiencia",
    "block.tnts.agua_tnt": "TNT de Agua",
    "block.tnts.arena_tnt": "TNT de Arena",
    "block.tnts.diamante_tnt": "TNT de Diamante",
    "block.tnts.esmeralda_tnt": "TNT de Esmeralda",
    "block.tnts.negra_tnt": "TNT Agujero Negro",
    "block.tnts.viento_tnt": "TNT de Viento",
    "block.tnts.inferno_tnt": "TNT Inferno",
    "block.tnts.hongo_tnt": "TNT de Setas",
    "block.tnts.miel_tnt": "TNT de Miel",
    "block.tnts.heal_tnt": "TNT Curativa",
    "block.tnts.teleport_tnt": "TNT Teletransportadora",
    "block.tnts.confeti_tnt": "TNT de Confeti",
    "block.tnts.mina_tnt": "Mina TNT",
    "block.tnts.terremoto_tnt": "TNT Terremoto",
    "block.tnts.meteorito_tnt": "TNT Meteorito",
    "block.tnts.tormenta_tnt": "TNT Tormenta",
    "block.tnts.colosal_tnt": "TNT Colosal",
    "block.tnts.supernova_tnt": "TNT Supernova",
    "block.tnts.toxica_tnt": "TNT Tóxica",
    "block.tnts.fuegos_tnt": "TNT de Fuegos Artificiales",
    "block.tnts.gravitatoria_tnt": "TNT Gravitatoria",
    "block.tnts.ender_tnt": "TNT del End",
    "block.tnts.bubble_tnt": "TNT Burbuja",
    "block.tnts.solar_tnt": "TNT Solar",
    "block.tnts.tnt_table": "Mesa de TNTs",
    "block.tnts.tnt_altar": "Altar del Rey TNT",
    "item.tnts.tnt_manual": "Manual de TNTs",
    "item.tnts.tnt_disc": "Disco de TNTs Locas",
    "item.record.music.tnts.tema.desc": "Codebuff - Tema de TNTs Locas",
    "item.tnts.tnt_king_crown": "Corona del Rey TNT",
    "item.tnts.tnt_king_spawn_egg": "Generar Rey TNT",
    "entity.tnts.tnt_king": "Rey TNT",
    "entity.minecraft.villager.tnt_expert": "Experto en TNTs",
    "tnts.manual.hint": "Todas las TNTs del mod: efecto y radio (config)",
    "tnts.manual.power": "Radio: %s",
    "advancements.tnts.defeat_king.title": "¡El Rey ha caído!",
    "advancements.tnts.defeat_king.description": "Derrota al Rey TNT y consigue su corona",
    "item.tnts.detonator": "Detonador Remoto",
    "item.tnts.launcher": "Lanzador de TNT",
    "item.tnts.tnt_arrow": "Flecha de TNT",
    "item.tnts.grenade": "Granada de TNT",
    "item.tnts.tnt_chestplate": "Peto de TNT",
    "item.tnts.tnt_pickaxe": "Pico de TNT",
    "tnts.config.title": "Config de TNTs Locas",
    "tnts.config.hint": "Elige un preset (se aplica al momento, sin reiniciar) o edita tnts-common.toml",
    "tnts.config.open": "Abrir carpeta de config",
    "tnts.preset.locura": "💥 Locura total",
    "tnts.preset.equilibrado": "⚖️ Equilibrado",
    "tnts.preset.suave": "🕊️ Suave",
    "tnts.preset.active": "Preset activo: x%s (poder) — se aplica al momento en singleplayer",
    "advancements.tnts.root.title": "¡TNTs Locas!",
    "advancements.tnts.root.description": "Consigue cualquier TNT del mod",
    "advancements.tnts.first_boom.title": "¡A la mecha!",
    "advancements.tnts.first_boom.description": "Enciende una TNT con un mechero",
    "advancements.tnts.all_variants.title": "Colección explosiva",
    "advancements.tnts.all_variants.description": "Explota una TNT de cada tipo",
    "advancements.tnts.mass_detonation.title": "¡Detonación masiva!",
    "advancements.tnts.mass_detonation.description": "Detona 10 o más TNTs de golpe con el Detonador Remoto",
    "tnts.title.mass_detonation": "¡Detonación masiva!",
    "tnts.subtitle.mass_detonation": "%s TNTs encendidas de golpe",
}

LANG_EN = {
    "itemGroup.tnts": "TNTs Locos",
    "block.tnts.mega_tnt": "Mega TNT",
    "block.tnts.mini_tnt": "Mini TNT",
    "block.tnts.lava_tnt": "Lava TNT",
    "block.tnts.rapida_tnt": "Instant TNT",
    "block.tnts.hielo_tnt": "Ice TNT",
    "block.tnts.saltarina_tnt": "Bouncy TNT",
    "block.tnts.nuclear_tnt": "Nuclear TNT",
    "block.tnts.limpia_tnt": "Clean TNT",
    "block.tnts.rayo_tnt": "Lightning TNT",
    "block.tnts.trampa_tnt": "Trap TNT",
    "block.tnts.oro_tnt": "Gold TNT",
    "block.tnts.obsidiana_tnt": "Obsidian TNT",
    "block.tnts.crio_tnt": "Cryogenic TNT",
    "block.tnts.xp_tnt": "XP TNT",
    "block.tnts.agua_tnt": "Water TNT",
    "block.tnts.arena_tnt": "Sand TNT",
    "block.tnts.diamante_tnt": "Diamond TNT",
    "block.tnts.esmeralda_tnt": "Emerald TNT",
    "block.tnts.negra_tnt": "Black Hole TNT",
    "block.tnts.viento_tnt": "Wind TNT",
    "block.tnts.inferno_tnt": "Inferno TNT",
    "block.tnts.hongo_tnt": "Mushroom TNT",
    "block.tnts.miel_tnt": "Honey TNT",
    "block.tnts.heal_tnt": "Healing TNT",
    "block.tnts.teleport_tnt": "Teleport TNT",
    "block.tnts.confeti_tnt": "Confetti TNT",
    "block.tnts.mina_tnt": "TNT Mine",
    "block.tnts.terremoto_tnt": "Earthquake TNT",
    "block.tnts.meteorito_tnt": "Meteor TNT",
    "block.tnts.tormenta_tnt": "Storm TNT",
    "block.tnts.colosal_tnt": "Colossal TNT",
    "block.tnts.supernova_tnt": "Supernova TNT",
    "block.tnts.toxica_tnt": "Toxic TNT",
    "block.tnts.fuegos_tnt": "Fireworks TNT",
    "block.tnts.gravitatoria_tnt": "Gravity TNT",
    "block.tnts.ender_tnt": "End TNT",
    "block.tnts.bubble_tnt": "Bubble TNT",
    "block.tnts.solar_tnt": "Solar TNT",
    "block.tnts.tnt_table": "TNT Workbench",
    "block.tnts.tnt_altar": "TNT King Altar",
    "item.tnts.tnt_manual": "TNT Manual",
    "item.tnts.tnt_disc": "TNTs Locos Disc",
    "item.record.music.tnts.tema.desc": "Codebuff - TNTs Locos Theme",
    "item.tnts.tnt_king_crown": "TNT King Crown",
    "item.tnts.tnt_king_spawn_egg": "TNT King Spawn Egg",
    "entity.tnts.tnt_king": "TNT King",
    "entity.minecraft.villager.tnt_expert": "TNT Expert",
    "tnts.manual.hint": "All the mod's TNTs: effect and radius (config)",
    "tnts.manual.power": "Radius: %s",
    "advancements.tnts.defeat_king.title": "The King has fallen!",
    "advancements.tnts.defeat_king.description": "Defeat the TNT King and claim his crown",
    "item.tnts.detonator": "Remote Detonator",
    "item.tnts.launcher": "TNT Launcher",
    "item.tnts.tnt_arrow": "TNT Arrow",
    "item.tnts.grenade": "TNT Grenade",
    "item.tnts.tnt_chestplate": "TNT Chestplate",
    "item.tnts.tnt_pickaxe": "TNT Pickaxe",
    "tnts.config.title": "TNTs Locos Config",
    "tnts.config.hint": "Pick a preset (applies instantly, no restart) or edit tnts-common.toml",
    "tnts.config.open": "Open config folder",
    "tnts.preset.locura": "💥 Total Madness",
    "tnts.preset.equilibrado": "⚖️ Balanced",
    "tnts.preset.suave": "🕊️ Gentle",
    "tnts.preset.active": "Active preset: x%s power — applies instantly in singleplayer",
    "advancements.tnts.root.title": "Crazy TNTs!",
    "advancements.tnts.root.description": "Get any TNT from the mod",
    "advancements.tnts.first_boom.title": "Light the fuse!",
    "advancements.tnts.first_boom.description": "Ignite a TNT with flint and steel",
    "advancements.tnts.all_variants.title": "Explosive collection",
    "advancements.tnts.all_variants.description": "Explode one of every TNT type",
    "advancements.tnts.mass_detonation.title": "Mass detonation!",
    "advancements.tnts.mass_detonation.description": "Detonate 10+ TNTs at once with the Remote Detonator",
    "tnts.title.mass_detonation": "Mass detonation!",
    "tnts.subtitle.mass_detonation": "%s TNTs ignited at once",
}


# Descripciones del Manual de TNTs (tnts.manual.<nombre>)
MANUAL_ES = {
    "mega_tnt": "Explosion enorme. Clasica pero con mas boom.",
    "mini_tnt": "Pequena y discreta. Ideal para demoliciones finas.",
    "lava_tnt": "Deja charcos de lava en el crater.",
    "rapida_tnt": "Mecha instantanea: 1 segundo y fuera.",
    "hielo_tnt": "Congela el agua y cubre la zona de nieve.",
    "saltarina_tnt": "Lanza a todo el mundo por los aires.",
    "nuclear_tnt": "Columna de humo y resplandor radiactivo.",
    "limpia_tnt": "No rompe bloques: solo daña a las entidades.",
    "rayo_tnt": "Invoca 3 rayos alrededor del crater.",
    "trampa_tnt": "Pita como loca antes de explotar.",
    "oro_tnt": "Lluvia de lingotes de oro.",
    "obsidiana_tnt": "Convierte el crater en obsidiana.",
    "crio_tnt": "Congela a los mobs y jugadores.",
    "xp_tnt": "Suelta orbes de experiencia.",
    "agua_tnt": "Inunda la zona y apaga el fuego.",
    "arena_tnt": "Avalancha de arena que cae del cielo.",
    "diamante_tnt": "Lluvia de diamantes.",
    "esmeralda_tnt": "Lluvia de esmeraldas.",
    "negra_tnt": "Abre un agujero negro 3D que devora bloques, items y seres vivos.",
    "viento_tnt": "Rafaga que empuja todo lejos. No rompe bloques.",
    "inferno_tnt": "Incendia el area e invoca mobs del Nether.",
    "hongo_tnt": "Esparce setas y micelio por todos lados.",
    "miel_tnt": "Pegajosa: lentitud extrema y bloques de miel.",
    "heal_tnt": "Estallido suave que cura y da regeneracion.",
    "teleport_tnt": "Teletransporta a los seres vivos a sitios aleatorios.",
    "confeti_tnt": "Lluvia de particulas de colores. Fiesta total.",
    "mina_tnt": "Explota al pisarla. Pita antes de reventar.",
    "terremoto_tnt": "Cratere de radio 9, grietas con lava y lanza todo muy alto.",
    "meteorito_tnt": "8-12 meteoritos 3D caen del cielo con estela de fuego.",
    "tormenta_tnt": "3 rayos al instante y una nube que castiga 8 segundos.",
    "colosal_tnt": "Explosion en 3 oleadas progresivas. La mas grande.",
    "supernova_tnt": "Destello cegador, 400-600 XP y esfera de luz 3D.",
    "toxica_tnt": "Nube verde que envenena y debilita. No rompe bloques.",
    "fuegos_tnt": "Lanza 8-16 cohetes de colores al cielo.",
    "gravitatoria_tnt": "Gravedad brutal: aplasta a todo contra el suelo.",
    "ender_tnt": "Teletransporta a los seres vivos e invoca endermites.",
    "bubble_tnt": "Succiona hacia el crater como un remolino y derrite el hielo.",
    "solar_tnt": "Hace dia, despeja el tiempo e incendia una gran area.",
}

MANUAL_EN = {
    "mega_tnt": "Huge explosion. Classic but with more boom.",
    "mini_tnt": "Small and discreet. Great for fine demolition.",
    "lava_tnt": "Leaves lava pools in the crater.",
    "rapida_tnt": "Instant fuse: 1 second and gone.",
    "hielo_tnt": "Freezes water and covers the area with snow.",
    "saltarina_tnt": "Launches everyone into the air.",
    "nuclear_tnt": "Smoke column and radioactive glow.",
    "limpia_tnt": "Doesn't break blocks: only damages entities.",
    "rayo_tnt": "Summons 3 lightning bolts around the crater.",
    "trampa_tnt": "Beeps like crazy before exploding.",
    "oro_tnt": "Shower of gold ingots.",
    "obsidiana_tnt": "Turns the crater into obsidian.",
    "crio_tnt": "Freezes mobs and players.",
    "xp_tnt": "Drops experience orbs.",
    "agua_tnt": "Floods the area and puts out fire.",
    "arena_tnt": "Sand avalanche falling from the sky.",
    "diamante_tnt": "Shower of diamonds.",
    "esmeralda_tnt": "Shower of emeralds.",
    "negra_tnt": "Opens a 3D black hole that devours blocks, items and mobs.",
    "viento_tnt": "Gust that pushes everything away. No block breaking.",
    "inferno_tnt": "Ignites the area and summons Nether mobs.",
    "hongo_tnt": "Spreads mushrooms and mycelium everywhere.",
    "miel_tnt": "Sticky: extreme slowness and honey blocks.",
    "heal_tnt": "Soft burst that heals and grants regeneration.",
    "teleport_tnt": "Teleports living beings to random places.",
    "confeti_tnt": "Colorful particle rain. Total party.",
    "mina_tnt": "Explodes when stepped on. Beeps first.",
    "terremoto_tnt": "Radius-9 crater, lava fissures, launches everything sky-high.",
    "meteorito_tnt": "8-12 3D meteors fall from the sky with a fire trail.",
    "tormenta_tnt": "3 lightning bolts instantly plus a punishing 8s cloud.",
    "colosal_tnt": "Explosion in 3 progressive waves. The biggest one.",
    "supernova_tnt": "Blinding flash, 400-600 XP and a 3D light sphere.",
    "toxica_tnt": "Green cloud that poisons and weakens. No block breaking.",
    "fuegos_tnt": "Launches 8-16 colorful rockets into the sky.",
    "gravitatoria_tnt": "Brutal gravity: crushes everything to the ground.",
    "ender_tnt": "Teleports living beings and summons endermites.",
    "bubble_tnt": "Sucks toward the crater like a whirlpool and melts ice.",
    "solar_tnt": "Makes it day, clears the weather and ignites a huge area.",
}


def write_lang():
    for name in NAMES:
        es = LANG_ES[f"block.tnts.{name}"]
        en = LANG_EN[f"block.tnts.{name}"]
        LANG_ES[f"advancements.tnts.craft.{name}.title"] = f"Craftea la {es}"
        LANG_ES[f"advancements.tnts.craft.{name}.description"] = f"Consigue una {es} crafteándola"
        LANG_EN[f"advancements.tnts.craft.{name}.title"] = f"Craft the {en}"
        LANG_EN[f"advancements.tnts.craft.{name}.description"] = f"Craft a {en}"
        if name in MANUAL_ES:
            LANG_ES[f"tnts.manual.{name}"] = MANUAL_ES[name]
        if name in MANUAL_EN:
            LANG_EN[f"tnts.manual.{name}"] = MANUAL_EN[name]
    write(os.path.join(ASSETS, "lang", "es_es.json"), LANG_ES)
    write(os.path.join(ASSETS, "lang", "en_us.json"), LANG_EN)


def nbt_string(s):
    return struct.pack(">H", len(s)) + s.encode("utf-8")


def nbt_int(v):
    return struct.pack(">i", v)


def nbt_compound_el(tag_type, name, payload):
    return bytes([tag_type]) + nbt_string(name) + payload


def nbt_list_int(values):
    return b"\x03" + struct.pack(">i", len(values)) + b"".join(nbt_int(v) for v in values)


def nbt_list_compound(compounds):
    return b"\x0a" + struct.pack(">i", len(compounds)) + b"".join(compounds)


def make_empty_template(path):
    """Estructura NBT 16x16x16 vacia (formato legacy con DataVersion <= 3437,
    que 1.20.1 sigue aceptando) para los GameTests."""
    palette_entry = nbt_compound_el(0x08, "Name", nbt_string("minecraft:air")) + b"\x00"
    payload = (
        nbt_compound_el(0x09, "size", nbt_list_int([16, 16, 16]))
        + nbt_compound_el(0x09, "entities", nbt_list_compound([]))
        + nbt_compound_el(0x09, "palette", nbt_list_compound([palette_entry]))
        + nbt_compound_el(0x09, "blocks", nbt_list_compound([]))
        + nbt_compound_el(0x03, "DataVersion", nbt_int(3437))
    )
    data = b"\x0a" + b"\x00\x00" + payload + b"\x00"
    os.makedirs(os.path.dirname(path), exist_ok=True)
    # NbtIo.readCompressed espera GZIP (los .nbt de estructuras van comprimidos)
    with open(path, "wb") as f:
        f.write(gzip.compress(data))


def make_logo(path):
    """Icono 128x128 del mod: bloque de TNT rojo con franja naranja sobre fondo oscuro."""
    def pixel(x, y):
        if not (16 <= x < 112 and 16 <= y < 112):
            return (28, 28, 44, 255)
        if x < 24 or y < 24 or x >= 104 or y >= 104:
            return (15, 15, 24, 255)
        if 52 <= y <= 76:
            return (251, 146, 60, 255)
        return (190, 30, 45, 255)

    write_png(path, (128, 128), pixel)


def ingredient_json(ing):
    kind, value = ing
    return {kind: value}


def main():
    os.makedirs(TEX_DIR, exist_ok=True)
    os.makedirs(ITEM_TEX_DIR, exist_ok=True)
    for name in NAMES:
        make_tnt_textures(name, PALETTES[name])
    make_detonator_texture()
    make_launcher_texture()
    make_arrow_texture()
    make_chestplate_texture()
    make_pickaxe_texture()
    make_grenade_texture()
    make_black_hole_texture()
    make_meteor_texture()
    make_storm_cloud_texture()
    make_supernova_texture()
    make_armor_layer()
    make_tnt_table_texture()
    make_tnt_altar_texture()
    make_tnt_manual_texture()
    make_tnt_disc_texture()
    make_tnt_king_crown_texture()
    make_king_texture()

    for name in NAMES:
        # blockstates: lit=false / lit=true
        write(os.path.join(ASSETS, "blockstates", f"{name}.json"), {
            "variants": {
                "lit=false": {"model": f"tnts:block/{name}"},
                "lit=true": {"model": f"tnts:block/{name}_lit"},
            }
        })
        # modelos de bloque (normal + encendido): cubo estilo TNT de vanilla
        # con cara superior (letras), inferior (lisa) y lateral (franja).
        for suffix in ("", "_lit"):
            write(os.path.join(ASSETS, "models", "block", f"{name}{suffix}.json"), {
                "parent": "minecraft:block/cube_bottom_top",
                "textures": {
                    "top": f"tnts:block/tnts_{name}_top{suffix}",
                    "bottom": f"tnts:block/tnts_{name}_bottom",
                    "side": f"tnts:block/tnts_{name}{suffix}",
                },
            })
        # modelo de item
        write(os.path.join(ASSETS, "models", "item", f"{name}.json"), {
            "parent": f"tnts:block/{name}"
        })
        # loot tables (se deja caer el bloque si se rompe por otros medios)
        write(os.path.join(DATA, "loot_tables", "blocks", f"{name}.json"), {
            "type": "minecraft:block",
            "pools": [
                {
                    "rolls": 1,
                    "entries": [{"type": "minecraft:item", "name": f"tnts:{name}"}],
                    "conditions": [{"condition": "minecraft:survives_explosion"}],
                }
            ],
        })

    # recetas (mega_tnt es 2x2, el resto shapeless)
    for name, (ingredients, count) in TNT_RECIPES.items():
        if name == "mega_tnt":
            recipe = {
                "type": "minecraft:crafting_shaped",
                "pattern": ["TT", "TT"],
                "key": {"T": {"item": "minecraft:tnt"}},
                "result": {"item": f"tnts:{name}"},
            }
        else:
            recipe = {
                "type": "minecraft:crafting_shapeless",
                "ingredients": [ingredient_json(i) for i in ingredients],
                "result": {"item": f"tnts:{name}", "count": count},
            }
        write(os.path.join(DATA, "recipes", f"{name}.json"), recipe)

    # modelo del detonador
    write(os.path.join(ASSETS, "models", "item", "detonator.json"), {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": "tnts:item/detonator"},
    })

    # bloques especiales (no son TNTs): mesa de trabajo y altar del Rey
    for bname in ("tnt_table", "tnt_altar"):
        write(os.path.join(ASSETS, "blockstates", f"{bname}.json"), {
            "variants": {"": {"model": f"tnts:block/{bname}"}}
        })
        write(os.path.join(ASSETS, "models", "block", f"{bname}.json"), {
            "parent": "minecraft:block/cube_all",
            "textures": {"all": f"tnts:block/{bname}"},
        })
        write(os.path.join(ASSETS, "models", "item", f"{bname}.json"), {
            "parent": f"tnts:block/{bname}"
        })
        write(os.path.join(DATA, "loot_tables", "blocks", f"{bname}.json"), {
            "type": "minecraft:block",
            "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"tnts:{bname}"}]}],
        })

    # items especiales (textura plana)
    for item in ("tnt_manual", "tnt_disc", "tnt_king_crown"):
        write(os.path.join(ASSETS, "models", "item", f"{item}.json"), {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"tnts:item/{item}"},
        })
    # huevo de invocacion del Rey TNT
    write(os.path.join(ASSETS, "models", "item", "tnt_king_spawn_egg.json"), {
        "parent": "minecraft:item/template_spawn_egg"
    })

    # recetas de la mesa de TNTs y el altar del Rey (shaped)
    write(os.path.join(DATA, "recipes", "tnt_table.json"), {
        "type": "minecraft:crafting_shaped",
        "pattern": ["PPP", "PTP", "PPP"],
        "key": {"P": {"tag": "minecraft:planks"}, "T": {"item": "minecraft:tnt"}},
        "result": {"item": "tnts:tnt_table"},
    })
    write(os.path.join(DATA, "recipes", "tnt_altar.json"), {
        "type": "minecraft:crafting_shaped",
        "pattern": ["OOO", "OTO", "OOO"],
        "key": {"O": {"item": "minecraft:obsidian"}, "T": {"item": "tnts:mega_tnt"}},
        "result": {"item": "tnts:tnt_altar"},
    })
    # disco de musica: un anillo de TNTs con una esmeralda en el centro
    write(os.path.join(DATA, "recipes", "tnt_disc.json"), {
        "type": "minecraft:crafting_shaped",
        "pattern": ["TTT", "TET", "TTT"],
        "key": {"T": {"item": "minecraft:tnt"}, "E": {"item": "minecraft:emerald"}},
        "result": {"item": "tnts:tnt_disc"},
    })

    # recetas del peto de TNT y el pico de TNT (shaped)
    write(os.path.join(DATA, "recipes", "tnt_chestplate.json"), {
        "type": "minecraft:crafting_shaped",
        "pattern": ["T T", "TTT", "TTT"],
        "key": {"T": {"item": "minecraft:tnt"}},
        "result": {"item": "tnts:tnt_chestplate"},
    })
    write(os.path.join(DATA, "recipes", "tnt_pickaxe.json"), {
        "type": "minecraft:crafting_shaped",
        "pattern": ["TTT", " S ", " S "],
        "key": {"T": {"item": "minecraft:tnt"}, "S": {"item": "minecraft:stick"}},
        "result": {"item": "tnts:tnt_pickaxe"},
    })

    # recetas del lanzador y la flecha (shaped)
    write(os.path.join(DATA, "recipes", "launcher.json"), {
        "type": "minecraft:crafting_shaped",
        "pattern": ["II", "IT"],
        "key": {"I": {"tag": "forge:ingots/iron"}, "T": {"item": "minecraft:tnt"}},
        "result": {"item": "tnts:launcher"},
    })
    write(os.path.join(DATA, "recipes", "tnt_arrow.json"), {
        "type": "minecraft:crafting_shaped",
        "pattern": ["T", "S", "S"],
        "key": {"T": {"item": "minecraft:tnt"}, "S": {"item": "minecraft:stick"}},
        "result": {"item": "tnts:tnt_arrow", "count": 2},
    })

    # modelos de item del lanzador, flecha, peto, pico y granada
    for item in ("launcher", "tnt_arrow", "tnt_chestplate", "tnt_pickaxe", "grenade"):
        write(os.path.join(ASSETS, "models", "item", f"{item}.json"), {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"tnts:item/{item}"},
        })

    # sonidos, advancements, tags e idiomas
    write_sounds_json()
    write_advancements()
    write_lang()

    # plantilla NBT vacia para los GameTests + logo del mod
    make_empty_template(os.path.join(RES, "data", "tnts", "structures", "empty.nbt"))
    make_logo(os.path.join(RES, "tnts_logo.png"))

    print(f"Recursos generados para {len(NAMES)} TNTs + items especiales (manual, disco, corona, aldeano, Rey TNT), con texturas animadas.")


if __name__ == "__main__":
    main()
