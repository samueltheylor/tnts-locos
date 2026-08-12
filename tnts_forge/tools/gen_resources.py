"""Genera los recursos del mod de Forge: texturas (normal + encendida ANIMADA),
blockstates, modelos, recetas (con tags), loot tables y el item Detonador.

Uso:  python tools/gen_resources.py
"""
import gzip
import json
import os
import struct
import zlib

from PIL import Image as PILImage

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
         "ender_tnt", "bubble_tnt", "solar_tnt",
         "casa_tnt", "mansion_tnt"]

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
    "casa_tnt":      ([("item", "minecraft:tnt"), ("item", "minecraft:oak_log"), ("item", "minecraft:oak_planks"), ("item", "minecraft:glass")], 1),
    "mansion_tnt":   ([("item", "minecraft:tnt"), ("item", "minecraft:obsidian"), ("item", "minecraft:diamond"), ("item", "minecraft:gold_block"), ("item", "minecraft:bookshelf")], 1),
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
    "casa_tnt":      ("#7c4a03", "#fef3c7", "#451a03", "#d97706", "#451a03"),  # casa/madera acogedora
    "mansion_tnt":   ("#312e81", "#e0f2fe", "#1e1b4b", "#67e8f9", "#1e1b4b"),  # mansion/lujo azul noche
}


# ---------- utilidades PNG ----------


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


def make_boots_texture():
    """Botas de TNT: bota roja con franja TNT, suela oscura y mecha en la punta."""
    def pixel(x, y):
        if x in (0, 15) or y in (0, 15):
            return (0, 0, 0, 0)
        RED = (190, 30, 45, 255)
        RED_LIGHT = (220, 60, 75, 255)
        RED_DARK = (120, 18, 25, 255)
        BAND = (245, 245, 244, 255)
        EDGE = (35, 30, 28, 255)
        SOLE = (60, 40, 30, 255)
        # perfil de la bota: ancho 4-11
        if not (4 <= x <= 11 and 3 <= y <= 13):
            return (0, 0, 0, 0)
        # caña (3-6)
        if 3 <= y <= 6:
            if x in (4, 11) or y == 3:
                return EDGE
            if y == 6:
                return RED_DARK
            return RED_LIGHT if y == 4 else RED
        # empeine (7-11)
        if 7 <= y <= 11:
            if x in (4, 11):
                return EDGE
            if y in (9, 10):
                return BAND  # franja TNT
            return RED_LIGHT if y == 7 else RED
        # suela
        if y in (12, 13):
            return SOLE
        return (0, 0, 0, 0)

    write_png(os.path.join(ITEM_TEX_DIR, "tnt_boots.png"), (16, 16), pixel)


def make_helmet_texture():
    """Casco de TNT: casco rojo con franja TNT, cresta y remaches."""
    def pixel(x, y):
        if x in (0, 15) or y in (0, 15):
            return (0, 0, 0, 0)
        RED = (190, 30, 45, 255)
        RED_LIGHT = (220, 60, 75, 255)
        RED_DARK = (120, 18, 25, 255)
        BAND = (245, 245, 244, 255)
        EDGE = (35, 30, 28, 255)
        # cresta superior
        if 3 <= x <= 5 and y == 2:
            return RED_DARK
        # casco (3-12)
        if not (3 <= x <= 12 and 3 <= y <= 11):
            return (0, 0, 0, 0)
        if x in (3, 12) or y == 11:
            return EDGE
        # franja TNT
        if y in (6, 7):
            return BAND
        # remaches
        if (x, y) in ((5, 4), (10, 4)):
            return (235, 200, 120, 255)
        if y == 3:
            return RED_LIGHT
        if y == 10:
            return RED_DARK
        if x in (4, 11):
            return RED_DARK
        return RED

    write_png(os.path.join(ITEM_TEX_DIR, "tnt_helmet.png"), (16, 16), pixel)


def make_leggings_texture():
    """Pantalon de TNT: pierna roja con franja TNT, cintura oscura y remaches."""
    def pixel(x, y):
        if x in (0, 15) or y in (0, 15):
            return (0, 0, 0, 0)
        RED = (190, 30, 45, 255)
        RED_LIGHT = (220, 60, 75, 255)
        RED_DARK = (120, 18, 25, 255)
        BAND = (245, 245, 244, 255)
        EDGE = (35, 30, 28, 255)
        # perfil del pantalon: dos piernas (4-7 y 9-12)
        leg = x if x <= 7 else x - 1
        if not (4 <= leg <= 7 and 3 <= y <= 13):
            if 4 <= x <= 11 and y == 3:
                return EDGE  # cintura
            return (0, 0, 0, 0)
        if y == 3:
            return EDGE  # cintura
        if x in (4, 7, 9, 12):
            return EDGE  # borde de la pierna
        if y in (8, 9):
            return BAND  # franja TNT
        if y == 10:
            return RED_DARK
        if y == 4:
            return RED_LIGHT
        return RED

    write_png(os.path.join(ITEM_TEX_DIR, "tnt_leggings.png"), (16, 16), pixel)


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
    """Capas de armadura de TNT (64x32) con el layout REAL de vanilla 1.20.1.

    layer_1 (la usan casco, peto Y botas): cabeza, cuerpo, brazos y piernas.
      - cabeza: top (8,0)-(15,7), bottom (16,0)-(23,7), caras (0,8)-(31,15)
      - cuerpo: (16,16)-(39,31)  brazos: (40,16)-(55,31)  piernas: (0,16)-(15,31)
    layer_2 (solo la usan los pantalones): piernas (0,16)-(15,31)
      + falda del cuerpo (16,24)-(39,31).
    """
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
    GOLD = (235, 200, 120, 255)
    GOLD_D = (160, 120, 50, 255)

    def band_fn(x, y, shade):
        # franja blanca de TNT con sombreado
        return BAND if not shade else BAND_SHADE

    def tnt_face(x, y, w, h, shade=False):
        # patron de un cubo rojo con franja central TNT y bordes
        if x in (0, w - 1) or y in (0, h - 1):
            return EDGE
        if h >= 12:
            if y in (h // 2 - 1, h // 2):
                return band_fn(x, y, shade)
            if y == h // 2 + 1:
                return RED_VDARK
        elif h >= 6:
            if y in (2, 3):
                return band_fn(x, y, shade)
        if x in (1, w - 2):
            return RED_DARK
        if y == h - 2:
            return RED_VDARK
        if y == 1:
            return RED_LIGHT
        return RED

    # ---------- layer 1: cabeza ----------
    def head_top(x, y):  # top (8,0)-(15,7)
        if x in (0, 7) or y in (0, 7):
            return EDGE
        if (x, y) in ((2, 2), (5, 2), (2, 5), (5, 5)):
            return GOLD  # remaches dorados
        return RED_LIGHT if y == 1 else RED

    def head_side(x, y):  # caras (0,8)-(31,15): 4 caras de 8x8
        cx = x % 8
        if cx in (0, 7) or y in (8, 15):
            return EDGE
        if y in (11, 12):
            return BAND if cx not in (3, 4) else BAND_SHADE
        if y == 13:
            return RED_VDARK
        if cx in (1, 6):
            return RED_DARK
        if y == 9:
            return RED_LIGHT
        return RED

    def head_bottom(x, y):  # bottom (16,0)-(23,7)
        return RED_VDARK

    # ---------- layer 1: cuerpo (16,16)-(39,31) ----------
    def body_px(x, y):
        lx = x - 16
        ly = y - 16
        if not (0 <= lx <= 23 and 0 <= ly <= 15):
            return (0, 0, 0, 0)
        if ly <= 3:
            # cara superior/inferior del cubo del cuerpo (8x4)
            if lx in (0, 7, 8, 15, 16, 23) or ly in (0, 3):
                return EDGE
            if (lx, ly) in ((11, 1), (20, 1)):
                return GOLD
            return RED_LIGHT if ly == 1 else RED
        # caras laterales (4 caras de 8x12: 16-19, 20-27, 28-35, 32-39)
        fx = (lx - 4) % 8
        if fx < 0:
            fx += 8
        if lx in (4, 7) or ly == 15:
            return EDGE
        if ly in (9, 10):
            return BAND if fx not in (3, 4) else BAND_SHADE
        if ly == 11:
            return RED_VDARK
        if fx in (0, 7):
            return EDGE
        if fx in (1, 6):
            return RED_DARK
        if ly == 5:
            return RED_LIGHT
        return RED

    # ---------- layer 1: brazos (40,16)-(55,31) ----------
    def arm_px(x, y):
        lx = x - 40
        ly = y - 16
        if not (0 <= lx <= 15 and 0 <= ly <= 15):
            return (0, 0, 0, 0)
        if ly <= 3:
            # top/bottom del brazo (4x4 en 44-47 / 48-51)
            if 4 <= lx <= 11:
                if lx in (4, 7, 8, 11) or ly in (0, 3):
                    return EDGE
                return RED_LIGHT if ly == 1 else RED
            return (0, 0, 0, 0)
        # caras laterales (4 caras de 4x12: 40-43, 44-47, 48-51, 52-55)
        fx = (lx - 4) % 4
        if lx in (4, 7) or ly == 15:
            return EDGE
        if ly in (9, 10):
            return BAND if fx not in (1, 2) else BAND_SHADE
        if ly == 11:
            return RED_VDARK
        if fx == 0 or fx == 3:
            return EDGE
        if fx == 1:
            return RED_DARK
        if ly == 5:
            return RED_LIGHT
        return RED

    # ---------- layer 1 + layer 2: piernas (0,16)-(15,31) ----------
    def leg_px(x, y, boot=False):
        lx = x % 8
        ly = y - 16
        if not (0 <= lx <= 7 and 0 <= ly <= 15):
            return (0, 0, 0, 0)
        if ly <= 3:
            # top/bottom de la pierna (4x4 en 4-7 / 8-11)
            if not (4 <= x % 16 <= 11):
                return (0, 0, 0, 0)
            if x % 16 in (4, 7, 8, 11) or ly in (0, 3):
                return EDGE
            return RED_LIGHT if ly == 1 else RED
        if lx in (0, 7) or ly == 15:
            return EDGE
        if boot:
            # bota: franja a media altura y suela oscura
            if ly in (8, 9):
                return BAND if lx not in (3, 4) else BAND_SHADE
            if ly >= 12:
                return RED_VDARK
            if ly in (10, 11):
                return RED_DARK
            if ly == 5:
                return RED_LIGHT
            return RED
        # pantalon: franja a media pierna
        if ly in (6, 7):
            return BAND if lx not in (3, 4) else BAND_SHADE
        if ly == 8:
            return RED_VDARK
        if lx in (1, 6):
            return RED_DARK
        if ly == 5:
            return RED_LIGHT
        return RED

    def pixel1(x, y):
        # cabeza: top (8,0)-(15,7), bottom (16,0)-(23,7), caras (0,8)-(31,15)
        if 8 <= x <= 15 and 0 <= y <= 7:
            return head_top(x - 8, y)
        if 16 <= x <= 23 and 0 <= y <= 7:
            return head_bottom(x - 16, y)
        if 0 <= x <= 31 and 8 <= y <= 15:
            return head_side(x, y)
        # cuerpo
        if 16 <= x <= 39 and 16 <= y <= 31:
            return body_px(x, y)
        # brazos
        if 40 <= x <= 55 and 16 <= y <= 31:
            return arm_px(x, y)
        # piernas (botas)
        if 0 <= x <= 15 and 16 <= y <= 31:
            return leg_px(x, y, boot=True)
        return (0, 0, 0, 0)

    write_png(os.path.join(ASSETS, "textures", "models", "armor", "tnt_layer_1.png"), (64, 32), pixel1)

    def pixel2(x, y):
        # pantalones: piernas completas
        if 0 <= x <= 15 and 16 <= y <= 31:
            return leg_px(x, y, boot=False)
        # falda del pantalon (16,24)-(39,31)
        if 16 <= x <= 39 and 24 <= y <= 31:
            return tnt_face(x - 16, y - 24, 8, 8, x >= 24)
        return (0, 0, 0, 0)

    write_png(os.path.join(ASSETS, "textures", "models", "armor", "tnt_layer_2.png"), (64, 32), pixel2)

    # ---------- capa de la CORONA del Rey (modelo 3D custom) ----------
    # banda 10x2x10 @ texOffs(0,0): top (10,0)-(19,9), bottom (20,0)-(29,9),
    #   caras laterales (0,10)-(39,11)
    # 4 puntas 2x4x2 @ texOffs(40,0): top (42,0)-(43,1), bottom (44,0)-(45,1),
    #   caras (40,2)-(47,5)
    # punta central 2x6x2 @ texOffs(48,0): top (50,0)-(51,1), bottom (52,0)-(53,1),
    #   caras (48,2)-(55,7)
    GEM = (200, 35, 60, 255)

    def gold_px(lx, ly, w, h):
        # patron dorado con borde oscuro y brillo en el borde superior
        if lx in (0, w - 1) or ly in (0, h - 1):
            return GOLD_D
        if ly == 1:
            return (255, 224, 150, 255)
        if (lx + ly) % 2 == 0:
            return GOLD
        return (215, 175, 95, 255)

    def crown_px(x, y):
        # banda
        if 10 <= x <= 19 and 0 <= y <= 9:
            return gold_px(x - 10, y, 10, 10)
        if 20 <= x <= 29 and 0 <= y <= 9:
            return GOLD_D
        if 0 <= x <= 39 and 10 <= y <= 11:
            cx = x % 10
            if cx in (0, 9):
                return GOLD_D
            if cx in (4, 5):
                return GEM  # gema roja en la cara frontal
            return gold_px(cx, y - 10, 10, 2)
        # puntas de las esquinas (comparten UV)
        if 42 <= x <= 43 and 0 <= y <= 1:
            return GOLD
        if 44 <= x <= 45 and 0 <= y <= 1:
            return GOLD_D
        if 40 <= x <= 47 and 2 <= y <= 5:
            return gold_px(x - 40, y - 2, 8, 4)
        # punta central
        if 50 <= x <= 51 and 0 <= y <= 1:
            return GOLD
        if 52 <= x <= 53 and 0 <= y <= 1:
            return GOLD_D
        if 48 <= x <= 55 and 2 <= y <= 7:
            return gold_px(x - 48, y - 2, 8, 6)
        return (0, 0, 0, 0)

    write_png(os.path.join(ASSETS, "textures", "models", "armor", "tnt_king_crown_layer_1.png"),
              (64, 32), crown_px)


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


def make_tnt_king_sword_texture():
    """Espada del Rey TNT: hoja ancha estilo TNT con filo metálico,
    guarda dorada con gema roja, mango y pomo detallados."""
    def pixel(x, y):
        RED = (200, 45, 60, 255)
        RED_D = (140, 25, 38, 255)
        RED_L = (240, 90, 105, 255)
        BAND = (240, 238, 230, 255)
        GOLD = (251, 191, 36, 255)
        GOLD_D = (180, 130, 20, 255)
        GOLD_L = (255, 230, 120, 255)
        EDGE = (60, 10, 18, 255)
        STEEL = (185, 190, 200, 255)
        STEEL_D = (140, 145, 155, 255)
        GEM = (255, 50, 50, 255)
        GEM_D = (180, 30, 30, 255)
        MANGO = (100, 70, 40, 255)
        MANGO_D = (70, 50, 28, 255)
        # hoja ancha: columna central de x=7 a x=12, y=0 a y=9
        if 7 <= x <= 12 and 0 <= y <= 9:
            # filo de acero en los bordes
            if x == 7 or x == 12:
                return STEEL_D
            if x == 8 or x == 11:
                return STEEL
            # franja TNT central
            if 4 <= y <= 6:
                return BAND
            if y == 3 or y == 7:
                return RED_L
            if x == 9 and y in (4, 5):
                # T de TNT en la hoja
                return GOLD
            if y == 5 and 9 <= x <= 10:
                return GOLD
            return RED
        # punta de la hoja: 3 px de filo puntiagudo
        if 8 <= x <= 11 and y == 0:
            return STEEL_D
        # guarda dorada (barrier)
        if y == 10 and 5 <= x <= 13:
            if x in (5, 13):
                return GOLD_D
            return GOLD if x % 2 == 0 else GOLD_L
        # mango de madera con wrapping de cuero
        if 6 <= x <= 9 and 11 <= y <= 14:
            if y % 2 == 0:
                return MANGO_D
            return MANGO
        # pomo dorado con gema roja
        if 5 <= x <= 10 and y == 15:
            if x == 7 or x == 8:
                return GEM
            if x == 6 or x == 9:
                return GEM_D
            return GOLD
        return (0, 0, 0, 0)

    write_png(os.path.join(ITEM_TEX_DIR, "tnt_king_sword.png"), (16, 16), pixel)


def make_tnt_shield_texture():
    """Escudo de TNT: escudo redondeado con borde metálico, franja TNT,
    remaches dorados, T grabada y mecha encendida arriba."""
    def pixel(x, y):
        RED = (190, 40, 55, 255)
        RED_D = (130, 24, 36, 255)
        RED_L = (235, 85, 100, 255)
        BAND = (240, 238, 230, 255)
        BAND_D = (195, 192, 185, 255)
        EDGE = (70, 12, 20, 255)
        STEEL = (175, 180, 190, 255)
        STEEL_D = (130, 135, 145, 255)
        GOLD = (251, 191, 36, 255)
        GOLD_D = (180, 130, 20, 255)
        FLAME = (255, 180, 40, 255)
        FLAME_D = (200, 100, 20, 255)
        cx, cy = 7.5, 8.0
        d = ((x - cx) ** 2 + ((y - cy) * 1.3) ** 2) ** 0.5
        # borde metálico exterior
        if d > 6.6:
            return (0, 0, 0, 0)
        if d > 5.8:
            return STEEL_D if (x + y) % 3 == 0 else STEEL
        if d > 5.2:
            return EDGE
        # mecha encendida en la punta superior
        if y <= 2 and 6 <= x <= 9:
            if y == 0 and 7 <= x <= 8:
                return FLAME
            if y == 1 and x in (7, 8):
                return FLAME_D
            return GOLD_D
        # franja TNT
        if 7 <= y <= 9:
            return BAND if (x + y) % 2 == 0 else BAND_D
        # letra T grabada en el centro
        if 5 <= x <= 10 and 4 <= y <= 6:
            if y == 4 and 6 <= x <= 9:
                return GOLD
            if x == 7 and 4 <= y <= 6:
                return GOLD
        # remaches dorados
        if (x, y) in ((4, 5), (11, 5), (4, 12), (11, 12), (5, 3), (10, 3)):
            return GOLD if ((x + y) % 2 == 0) else GOLD_D
        if d < 2.0:
            return RED_L
        return RED if (x + y) % 2 == 0 else RED_D

    write_png(os.path.join(ITEM_TEX_DIR, "tnt_shield.png"), (16, 16), pixel)


def make_king_texture():
    """Textura 128x64 del Rey TNT (v4) en el FORMATO ESTANDAR DE CUBO DE
    ENTIDAD de Minecraft. Para un cubo texOffs(u,v) de tamano w x h x d:
      top:    (u+d,     v)   ancho w, alto d
      bottom: (u+d+w,   v)   ancho w, alto d
      left:   (u,       v+d) ancho d, alto h
      front:  (u+d,     v+d) ancho w, alto h
      right:  (u+d+w,   v+d) ancho d, alto h
      back:   (u+d+w+d, v+d) ancho w, alto h
    Antes se dibujaba en formato de BLOQUE (caras arriba) y Minecraft leia
    top/bottom ahi: los costados del cuerpo salian TRANSPARENTES.
    """
    RED = (165, 30, 42, 255)
    RED_D = (115, 18, 28, 255)
    RED_L = (210, 60, 75, 255)
    BAND = (238, 236, 228, 255)
    BAND_D = (190, 188, 180, 255)
    EDGE = (55, 12, 18, 255)
    GOLD = (251, 191, 36, 255)
    GOLD_D = (175, 125, 18, 255)
    GOLD_L = (255, 230, 120, 255)
    GEM = (200, 35, 45, 255)

    img = PILImage.new("RGBA", (128, 64), (0, 0, 0, 0))
    pxs = img.load()

    def body_side(lx, ly, face):
        """Cara lateral del cuerpo (16x16). face=True = frontal con la cara."""
        if lx in (0, 15) or ly in (0, 15):
            return EDGE
        # CARA ENFADADA: se dibuja ENCIMA de la franja blanca (antes del check)
        if face and 2 <= lx <= 13 and 2 <= ly <= 13:
            # OJOS (2 filas, py 4-5) con pupila
            if 4 <= ly <= 5:
                if 3 <= lx <= 5:
                    return (40, 25, 5, 255) if (ly == 5 and lx == 4) else (255, 220, 50, 255)
                if 10 <= lx <= 12:
                    return (40, 25, 5, 255) if (ly == 5 and lx == 11) else (255, 220, 50, 255)
            # CEJAS GROSAS inclinadas (py 2-3)
            if ly == 3 and 3 <= lx <= 5:
                return GOLD
            if ly == 3 and 10 <= lx <= 12:
                return GOLD
            if ly == 2 and (lx == 3 or lx == 12):
                return GOLD_L
            # BOCA con dientes (py 9-10)
            if ly in (9, 10) and 5 <= lx <= 10:
                return (30, 12, 14, 255)
            if ly == 9 and lx in (5, 10):
                return (255, 245, 235, 255)
            if ly == 10 and lx in (7, 8):
                return (255, 245, 235, 255)
        # franja blanca TNT (los rasgos de la cara ya devolvieron antes)
        if 5 <= ly <= 10:
            return BAND_D if ly in (5, 10) else BAND
        if (lx, ly) in ((2, 2), (13, 2), (2, 13), (13, 13)):
            return GOLD
        if ly == 3:
            return RED_L
        if ly == 12:
            return RED_D
        if lx in (1, 14):
            return RED_D
        return RED

    def crown_side(lx, ly):
        if lx in (0, 11) or ly in (0, 3):
            return GOLD_D
        if ly == 1 and lx in (2, 4, 6, 8):
            return GEM
        if ly == 1 and lx == 10:
            return GOLD_L
        return GOLD

    def arm_side(lx, ly):
        """Cara lateral del brazo (4x12)."""
        if lx in (0, 3) or ly in (0, 11):
            return EDGE
        if 4 <= ly <= 7:
            return BAND_D if ly in (4, 7) else BAND
        if (lx, ly) in ((1, 1), (2, 1)):
            return GOLD
        return RED_L if ly == 1 else RED

    def spike_side(lx, ly, tall):
        h = 8 if tall else 6
        if ly == h - 1:
            return GOLD_L
        return GOLD if ly % 2 == 0 else GOLD_D

    def fill_box(u, v, w, h, d, faces):
        """Dibuja un cubo w x h x d con texOffs (u,v) en formato de entidad.
        faces: dict con 'top','bottom','left','front','right','back', cada
        uno fn(lx, ly) -> color."""
        for lx in range(w):
            for ly in range(d):
                pxs[u + d + lx, v + ly] = faces["top"](lx, ly)
        for lx in range(w):
            for ly in range(d):
                pxs[u + d + w + lx, v + ly] = faces["bottom"](lx, ly)
        for lx in range(d):
            for ly in range(h):
                pxs[u + lx, v + d + ly] = faces["left"](lx, ly)
        for lx in range(w):
            for ly in range(h):
                pxs[u + d + lx, v + d + ly] = faces["front"](lx, ly)
        for lx in range(d):
            for ly in range(h):
                pxs[u + d + w + lx, v + d + ly] = faces["right"](lx, ly)
        for lx in range(w):
            for ly in range(h):
                pxs[u + d + w + d + lx, v + d + ly] = faces["back"](lx, ly)

    # ---- cuerpo 16x16x16 @ texOffs(0,0) ----
    fill_box(0, 0, 16, 16, 16, {
        "front": lambda lx, ly: body_side(lx, ly, True),
        "left": lambda lx, ly: body_side(lx, ly, False),
        "right": lambda lx, ly: body_side(lx, ly, False),
        "back": lambda lx, ly: body_side(lx, ly, False),
        "top": lambda lx, ly: (
            GOLD_D if lx in (0, 15) or ly in (0, 15)
            else GOLD if (lx, ly) in ((4, 4), (11, 4), (4, 11), (11, 11))
            else (130, 18, 28, 255)),
        "bottom": lambda lx, ly: (90, 12, 20, 255),
    })

    # ---- corona 12x4x12 @ texOffs(0,32) ----
    fill_box(0, 32, 12, 4, 12, {
        "front": crown_side,
        "left": crown_side,
        "right": crown_side,
        "back": crown_side,
        "top": lambda lx, ly: (
            GOLD_D if lx in (0, 11) or ly in (0, 11)
            else GEM if lx in (5, 6) and ly in (5, 6)
            else GOLD_L),
        "bottom": lambda lx, ly: GOLD_D,
    })

    # ---- 4 puntas cortas 2x6x2 @ texOffs(48,56,64,72, 32) ----
    for su in (48, 56, 64, 72):
        fill_box(su, 32, 2, 6, 2, {
            "front": lambda lx, ly: spike_side(lx, ly, False),
            "left": lambda lx, ly: spike_side(lx, ly, False),
            "right": lambda lx, ly: spike_side(lx, ly, False),
            "back": lambda lx, ly: spike_side(lx, ly, False),
            "top": lambda lx, ly: GOLD_L,
            "bottom": lambda lx, ly: GOLD_D,
        })
    # ---- punta central 2x8x2 @ texOffs(80,32) ----
    fill_box(80, 32, 2, 8, 2, {
        "front": lambda lx, ly: spike_side(lx, ly, True),
        "left": lambda lx, ly: spike_side(lx, ly, True),
        "right": lambda lx, ly: spike_side(lx, ly, True),
        "back": lambda lx, ly: spike_side(lx, ly, True),
        "top": lambda lx, ly: GOLD_L,
        "bottom": lambda lx, ly: GOLD_D,
    })

    # ---- brazos 4x12x4 @ texOffs(0,48) y (16,48) ----
    for bu in (0, 16):
        fill_box(bu, 48, 4, 12, 4, {
            "front": arm_side,
            "left": arm_side,
            "right": arm_side,
            "back": arm_side,
            "top": lambda lx, ly: (200, 45, 60, 255),
            "bottom": lambda lx, ly: (150, 30, 42, 255),
        })

    # ---- mecha 2x3x2 @ texOffs(32,48) ----
    def fuse_side(lx, ly):
        return (130, 95, 45, 255) if lx else (70, 50, 25, 255)

    fill_box(32, 48, 2, 3, 2, {
        "front": fuse_side,
        "left": fuse_side,
        "right": fuse_side,
        "back": fuse_side,
        "top": lambda lx, ly: (160, 120, 60, 255),
        "bottom": lambda lx, ly: (90, 60, 30, 255),
    })

    # ---- llama 3x4x3 @ texOffs(40,48) ----
    flame_cols = [(255, 245, 180, 255), (255, 200, 60, 255),
                  (255, 140, 30, 255), (200, 80, 20, 255)]
    fill_box(40, 48, 3, 4, 3, {
        "front": lambda lx, ly: flame_cols[ly],
        "left": lambda lx, ly: flame_cols[ly],
        "right": lambda lx, ly: flame_cols[ly],
        "back": lambda lx, ly: flame_cols[ly],
        "top": lambda lx, ly: (255, 220, 110, 255),
        "bottom": lambda lx, ly: (220, 100, 25, 255),
    })

    # ---- cejas 6x2x1 @ texOffs(52,48) y (76,48) ----
    def brow_side(lx, ly):
        return GOLD if lx else GOLD_D

    for bu in (52, 76):
        fill_box(bu, 48, 6, 2, 1, {
            "front": brow_side,
            "left": brow_side,
            "right": brow_side,
            "back": brow_side,
            "top": lambda lx, ly: GOLD_L,
            "bottom": lambda lx, ly: GOLD_D,
        })

    img.save(os.path.join(ENTITY_TEX_DIR, "tnt_king.png"))

    # ===== FASES VISUALES: grietas progresivas =====
    # (atlas en formato de cubo: las 4 caras laterales del cuerpo estan en
    #  y=16..32, x=0..64; el top en (16,0)-(32,16); la corona en y=32..48)
    def draw_crack(pxs, x0, y0, x1, y1, width=0):
        CRACK = (34, 13, 17, 255)
        CRACK_D = (18, 7, 9, 255)
        x0, y0, x1, y1 = int(x0), int(y0), int(x1), int(y1)
        dx, dy = abs(x1 - x0), -abs(y1 - y0)
        sx, sy = (1 if x0 < x1 else -1), (1 if y0 < y1 else -1)
        err = dx + dy
        while True:
            for ox in range(-width, width + 1):
                for oy in range(-width, width + 1):
                    if ox * ox + oy * oy <= width * width:
                        xx, yy = x0 + ox, y0 + oy
                        if 0 <= xx < 128 and 0 <= yy < 64:
                            pxs[xx, yy] = CRACK if (xx * 7 + yy * 13) % 2 else CRACK_D
            if x0 == x1 and y0 == y1:
                break
            e2 = 2 * err
            if e2 >= dy:
                err += dy
                x0 += sx
            if e2 <= dx:
                err += dx
                y0 += sy

    def apply_cracks(pxs, level):
        if level >= 1:
            # cara LEFT (0,16)
            draw_crack(pxs, 2, 18, 8, 27)
            draw_crack(pxs, 13, 17, 11, 24)
            # cara FRONT (16,16): grieta vertical central entre los ojos
            draw_crack(pxs, 23, 17, 24, 24)
            draw_crack(pxs, 18, 28, 21, 30)
            # cara RIGHT (32,16)
            draw_crack(pxs, 37, 17, 41, 26)
            draw_crack(pxs, 44, 18, 39, 28)
            # cara BACK (48,16)
            draw_crack(pxs, 50, 18, 50, 27)
            draw_crack(pxs, 55, 17, 57, 25)
        if level >= 2:
            # mas grietas, mas gruesas
            draw_crack(pxs, 23, 17, 23, 29, 1)   # central mas larga
            draw_crack(pxs, 17, 19, 20, 22, 1)
            draw_crack(pxs, 27, 18, 30, 24, 1)
            draw_crack(pxs, 2, 26, 9, 30, 1)
            draw_crack(pxs, 37, 25, 45, 30, 1)
            draw_crack(pxs, 49, 17, 52, 23, 1)
            # grietas en el TOP del cubo (16,0)-(32,16)
            draw_crack(pxs, 24, 2, 24, 12, 1)
            draw_crack(pxs, 18, 6, 29, 8, 1)
            # corona agrietada
            draw_crack(pxs, 12, 40, 20, 47, 1)
            draw_crack(pxs, 26, 44, 32, 47, 1)
            draw_crack(pxs, 36, 33, 42, 40, 1)
            # puntas de la corona agrietadas
            for su in (48, 56, 64, 72, 80):
                draw_crack(pxs, su + 3, 32, su + 4, 38, 1)
            # cantos del cubo oscurecidos (grietas en los bordes)
            for x in (16, 32, 48):
                for yy in range(17, 31):
                    if (x + yy) % 3 == 0:
                        pxs[x, yy] = (26, 10, 13, 255)

    apply_cracks(pxs, 1)
    img.save(os.path.join(ENTITY_TEX_DIR, "tnt_king_damaged.png"))
    apply_cracks(pxs, 2)
    img.save(os.path.join(ENTITY_TEX_DIR, "tnt_king_damaged2.png"))


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
        "entity.tnt_king.charge": {"sounds": ["tnts:entity/tnt_king_charge"]},
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

    # doble salto con las botas de TNT
    write(os.path.join(adv_dir, "double_jump.json"), {
        "parent": "tnts:root",
        "display": {
            "icon": {"item": "tnts:tnt_boots"},
            "title": {"translate": "advancements.tnts.double_jump.title"},
            "description": {"translate": "advancements.tnts.double_jump.description"},
            "frame": "task",
            "show_toast": True,
            "announce_to_chat": True,
            "hidden": False,
        },
        "criteria": {"jump": {"trigger": "tnts:double_jumped"}},
    })

    # vision nocturna del casco de TNT (trigger vanilla de efectos)
    write(os.path.join(adv_dir, "night_vision.json"), {
        "parent": "tnts:root",
        "display": {
            "icon": {"item": "tnts:tnt_helmet"},
            "title": {"translate": "advancements.tnts.night_vision.title"},
            "description": {"translate": "advancements.tnts.night_vision.description"},
            "frame": "task",
            "show_toast": True,
            "announce_to_chat": True,
            "hidden": False,
        },
        "criteria": {"effect": {"trigger": "minecraft:effects_changed",
                                 "conditions": {"effects": {"minecraft:night_vision": {}}}}},
    })

    # set bonus del Rey: 2 piezas
    write(os.path.join(adv_dir, "king_set_2.json"), {
        "parent": "tnts:defeat_king",
        "display": {
            "icon": {"item": "tnts:tnt_king_sword"},
            "title": {"translate": "advancements.tnts.king_set_2.title"},
            "description": {"translate": "advancements.tnts.king_set_2.description"},
            "frame": "goal",
            "show_toast": True,
            "announce_to_chat": True,
            "hidden": False,
        },
        "criteria": {"set": {"trigger": "tnts:king_set", "conditions": {"min": 2}}},
    })

    # set bonus del Rey: 3 piezas
    write(os.path.join(adv_dir, "king_set_3.json"), {
        "parent": "tnts:king_set_2",
        "display": {
            "icon": {"item": "tnts:tnt_king_crown"},
            "title": {"translate": "advancements.tnts.king_set_3.title"},
            "description": {"translate": "advancements.tnts.king_set_3.description"},
            "frame": "goal",
            "show_toast": True,
            "announce_to_chat": True,
            "hidden": False,
        },
        "criteria": {"set": {"trigger": "tnts:king_set", "conditions": {"min": 3}}},
    })

    # set bonus del Rey: 4 piezas
    write(os.path.join(adv_dir, "king_set_4.json"), {
        "parent": "tnts:king_set_3",
        "display": {
            "icon": {"item": "tnts:tnt_shield"},
            "title": {"translate": "advancements.tnts.king_set_4.title"},
            "description": {"translate": "advancements.tnts.king_set_4.description"},
            "frame": "challenge",
            "show_toast": True,
            "announce_to_chat": True,
            "hidden": False,
        },
        "criteria": {"set": {"trigger": "tnts:king_set", "conditions": {"min": 4}}},
    })

    # derrotar al Rey guardian del almacen
    write(os.path.join(adv_dir, "king_guardian_defeated.json"), {
        "parent": "tnts:root",
        "display": {
            "icon": {"item": "tnts:mini_tnt"},
            "title": {"translate": "advancements.tnts.king_guardian_defeated.title"},
            "description": {"translate": "advancements.tnts.king_guardian_defeated.description"},
            "frame": "goal",
            "show_toast": True,
            "announce_to_chat": True,
            "hidden": False,
        },
        "criteria": {"kill": {"trigger": "tnts:king_guardian_defeated"}},
    })

    # ver al Rey agrietarse del todo (fase visual 2)
    write(os.path.join(adv_dir, "king_phase.json"), {
        "parent": "tnts:defeat_king",
        "display": {
            "icon": {"item": "tnts:mega_tnt"},
            "title": {"translate": "advancements.tnts.king_phase.title"},
            "description": {"translate": "advancements.tnts.king_phase.description"},
            "frame": "goal",
            "show_toast": True,
            "announce_to_chat": True,
            "hidden": True,
        },
        "criteria": {"phase": {"trigger": "tnts:king_phase", "conditions": {"min": 2}}},
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
    "block.tnts.casa_tnt": "TNT Casa",
    "block.tnts.mansion_tnt": "TNT Mansión",
    "block.tnts.tnt_table": "Mesa de TNTs",
    "block.tnts.tnt_altar": "Altar del Rey TNT",
    "item.tnts.tnt_manual": "Manual de TNTs",
    "item.tnts.tnt_disc": "Disco de TNTs Locas",
    "item.record.music.tnts.tema.desc": "Codebuff - Tema de TNTs Locas",
    "item.tnts.tnt_king_crown": "Corona del Rey TNT",
    "item.tnts.tnt_king_sword": "Espada del Rey TNT",
    "item.tnts.tnt_shield": "Escudo de TNT",
    "item.tnts.tnt_king_spawn_egg": "Generar Rey TNT",
    "entity.tnts.tnt_king": "Rey TNT",
    "entity.minecraft.villager.tnt_expert": "Experto en TNTs",
    "tnts.manual.hint": "Todas las TNTs del mod: efecto y radio (config)",
    "tnts.manual.power": "Radio: %s",
    "advancements.tnts.defeat_king.title": "¡El Rey ha caído!",
    "advancements.tnts.defeat_king.description": "Derrota al Rey TNT y consigue su corona",
    "advancements.tnts.double_jump.title": "¡Doble salto!",
    "advancements.tnts.double_jump.description": "Haz un impulso de explosión con las Botas de TNT",
    "advancements.tnts.night_vision.title": "Visión de TNT",
    "advancements.tnts.night_vision.description": "Ponte el Casco de TNT y ve en la oscuridad",
    "advancements.tnts.king_set_2.title": "Poder del Rey",
    "advancements.tnts.king_set_2.description": "Lleva 2 piezas del set del Rey: la Espada enciende TNTs al doble de radio",
    "advancements.tnts.king_set_3.title": "Escudo Real",
    "advancements.tnts.king_set_3.description": "Lleva 3 piezas del set del Rey: eres inmune a tus propias TNTs",
    "advancements.tnts.king_set_4.title": "Aura del Rey",
    "advancements.tnts.king_set_4.description": "Lleva las 4 piezas del set del Rey: tu aura enciende TNTs cercanas",
    "advancements.tnts.king_guardian_defeated.title": "Guardia derrotada",
    "advancements.tnts.king_guardian_defeated.description": "Derrota al Rey guardián del Almacén de TNT",
    "advancements.tnts.king_phase.title": "Se está agrietando...",
    "advancements.tnts.king_phase.description": "Lleva al Rey TNT al borde: obsérvalo agrietarse del todo",
    "item.tnts.detonator": "Detonador Remoto",
    "item.tnts.launcher": "Lanzador de TNT",
    "item.tnts.tnt_arrow": "Flecha de TNT",
    "item.tnts.grenade": "Granada de TNT",
    "item.tnts.tnt_chestplate": "Peto de TNT",
    "item.tnts.tnt_boots": "Botas de TNT",
    "item.tnts.tnt_helmet": "Casco de TNT",
    "item.tnts.tnt_leggings": "Pantalón de TNT",
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
    "block.tnts.casa_tnt": "House TNT",
    "block.tnts.mansion_tnt": "Mansion TNT",
    "block.tnts.tnt_table": "TNT Workbench",
    "block.tnts.tnt_altar": "TNT King Altar",
    "item.tnts.tnt_manual": "TNT Manual",
    "item.tnts.tnt_disc": "TNTs Locos Disc",
    "item.record.music.tnts.tema.desc": "Codebuff - TNTs Locos Theme",
    "item.tnts.tnt_king_crown": "TNT King Crown",
    "item.tnts.tnt_king_sword": "TNT King Sword",
    "item.tnts.tnt_shield": "TNT Shield",
    "item.tnts.tnt_king_spawn_egg": "TNT King Spawn Egg",
    "entity.tnts.tnt_king": "TNT King",
    "entity.minecraft.villager.tnt_expert": "TNT Expert",
    "tnts.manual.hint": "All the mod's TNTs: effect and radius (config)",
    "tnts.manual.power": "Radius: %s",
    "advancements.tnts.defeat_king.title": "The King has fallen!",
    "advancements.tnts.defeat_king.description": "Defeat the TNT King and claim his crown",
    "advancements.tnts.double_jump.title": "Double jump!",
    "advancements.tnts.double_jump.description": "Blast off with the TNT Boots",
    "advancements.tnts.night_vision.title": "TNT Vision",
    "advancements.tnts.night_vision.description": "Wear the TNT Helmet and see in the dark",
    "advancements.tnts.king_set_2.title": "King's Power",
    "advancements.tnts.king_set_2.description": "Wear 2 King set pieces: the Sword primes TNTs at double range",
    "advancements.tnts.king_set_3.title": "Royal Shield",
    "advancements.tnts.king_set_3.description": "Wear 3 King set pieces: become immune to your own TNTs",
    "advancements.tnts.king_set_4.title": "King's Aura",
    "advancements.tnts.king_set_4.description": "Wear all 4 King set pieces: your aura primes nearby TNTs",
    "advancements.tnts.king_guardian_defeated.title": "Guard down",
    "advancements.tnts.king_guardian_defeated.description": "Defeat the guardian King of the TNT Warehouse",
    "advancements.tnts.king_phase.title": "It's cracking...",
    "advancements.tnts.king_phase.description": "Bring the TNT King to the brink: watch him crack completely",
    "item.tnts.detonator": "Remote Detonator",
    "item.tnts.launcher": "TNT Launcher",
    "item.tnts.tnt_arrow": "TNT Arrow",
    "item.tnts.grenade": "TNT Grenade",
    "item.tnts.tnt_chestplate": "TNT Chestplate",
    "item.tnts.tnt_boots": "TNT Boots",
    "item.tnts.tnt_helmet": "TNT Helmet",
    "item.tnts.tnt_leggings": "TNT Leggings",
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
    "casa_tnt": "Genera una casa acogedora de madera, lista para vivir.",
    "mansion_tnt": "Genera una mansion de lujo con dos plantas, torres y decoracion.",
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
    "casa_tnt": "Builds a cozy wooden house, ready to live in.",
    "mansion_tnt": "Builds a luxury mansion with two floors, towers and decor.",
}

# ---- Português (pt_br) ----
LANG_PT = {
    "itemGroup.tnts": "TNTs Loucas",
    "block.tnts.mega_tnt": "Mega TNT",
    "block.tnts.mini_tnt": "Mini TNT",
    "block.tnts.lava_tnt": "TNT de Lava",
    "block.tnts.rapida_tnt": "TNT Instantânea",
    "block.tnts.hielo_tnt": "TNT de Gelo",
    "block.tnts.saltarina_tnt": "TNT Saltitante",
    "block.tnts.nuclear_tnt": "TNT Nuclear",
    "block.tnts.limpia_tnt": "TNT Limpa",
    "block.tnts.rayo_tnt": "TNT de Raio",
    "block.tnts.trampa_tnt": "TNT Armadilha",
    "block.tnts.oro_tnt": "TNT de Ouro",
    "block.tnts.obsidiana_tnt": "TNT de Obsidiana",
    "block.tnts.crio_tnt": "TNT Criogênica",
    "block.tnts.xp_tnt": "TNT de Experiência",
    "block.tnts.agua_tnt": "TNT de Água",
    "block.tnts.arena_tnt": "TNT de Areia",
    "block.tnts.diamante_tnt": "TNT de Diamante",
    "block.tnts.esmeralda_tnt": "TNT de Esmeralda",
    "block.tnts.negra_tnt": "TNT Buraco Negro",
    "block.tnts.viento_tnt": "TNT de Vento",
    "block.tnts.inferno_tnt": "TNT Inferno",
    "block.tnts.hongo_tnt": "TNT de Cogumelos",
    "block.tnts.miel_tnt": "TNT de Mel",
    "block.tnts.heal_tnt": "TNT Curativa",
    "block.tnts.teleport_tnt": "TNT Teletransportadora",
    "block.tnts.confeti_tnt": "TNT de Confete",
    "block.tnts.mina_tnt": "Mina TNT",
    "block.tnts.terremoto_tnt": "TNT Terremoto",
    "block.tnts.meteorito_tnt": "TNT Meteoro",
    "block.tnts.tormenta_tnt": "TNT Tempestade",
    "block.tnts.colosal_tnt": "TNT Colossal",
    "block.tnts.supernova_tnt": "TNT Supernova",
    "block.tnts.toxica_tnt": "TNT Tóxica",
    "block.tnts.fuegos_tnt": "TNT de Fogos de Artifício",
    "block.tnts.gravitatoria_tnt": "TNT Gravitacional",
    "block.tnts.ender_tnt": "TNT do Fim",
    "block.tnts.bubble_tnt": "TNT Bolha",
    "block.tnts.solar_tnt": "TNT Solar",
    "block.tnts.casa_tnt": "TNT Casa",
    "block.tnts.mansion_tnt": "TNT Mansão",
    "block.tnts.tnt_table": "Mesa de TNTs",
    "block.tnts.tnt_altar": "Altar do Rei TNT",
    "item.tnts.tnt_manual": "Manual de TNTs",
    "item.tnts.tnt_disc": "Disco de TNTs Loucas",
    "item.record.music.tnts.tema.desc": "Codebuff - Tema de TNTs Loucas",
    "item.tnts.tnt_king_crown": "Coroa do Rei TNT",
    "item.tnts.tnt_king_sword": "Espada do Rei TNT",
    "item.tnts.tnt_shield": "Escudo de TNT",
    "item.tnts.tnt_king_spawn_egg": "Gerar Rei TNT",
    "entity.tnts.tnt_king": "Rei TNT",
    "entity.minecraft.villager.tnt_expert": "Especialista em TNTs",
    "tnts.manual.hint": "Todas as TNTs do mod: efeito e raio (config)",
    "tnts.manual.power": "Raio: %s",
    "advancements.tnts.defeat_king.title": "O Rei caiu!",
    "advancements.tnts.defeat_king.description": "Derrote o Rei TNT e pegue a coroa dele",
    "advancements.tnts.double_jump.title": "Pulo duplo!",
    "advancements.tnts.double_jump.description": "Faça um impulso de explosão com as Botas de TNT",
    "advancements.tnts.night_vision.title": "Visão TNT",
    "advancements.tnts.night_vision.description": "Use o Capacete de TNT e enxergue no escuro",
    "advancements.tnts.king_set_2.title": "Poder do Rei",
    "advancements.tnts.king_set_2.description": "Use 2 peças do set do Rei: a Espada acende TNTs com dobro de alcance",
    "advancements.tnts.king_set_3.title": "Escudo Real",
    "advancements.tnts.king_set_3.description": "Use 3 peças do set do Rei: fique imune às suas próprias TNTs",
    "advancements.tnts.king_set_4.title": "Aura do Rei",
    "advancements.tnts.king_set_4.description": "Use as 4 peças do set do Rei: sua aura acende TNTs próximas",
    "advancements.tnts.king_guardian_defeated.title": "Guarda derrotado",
    "advancements.tnts.king_guardian_defeated.description": "Derrote o Rei guardião do Armazém de TNT",
    "advancements.tnts.king_phase.title": "Está rachando...",
    "advancements.tnts.king_phase.description": "Leve o Rei TNT ao limite: veja-o rachar por completo",
    "item.tnts.detonator": "Detonador Remoto",
    "item.tnts.launcher": "Lançador de TNT",
    "item.tnts.tnt_arrow": "Flecha de TNT",
    "item.tnts.grenade": "Granada de TNT",
    "item.tnts.tnt_chestplate": "Peitoral de TNT",
    "item.tnts.tnt_boots": "Botas de TNT",
    "item.tnts.tnt_helmet": "Capacete de TNT",
    "item.tnts.tnt_leggings": "Calças de TNT",
    "item.tnts.tnt_pickaxe": "Picareta de TNT",
    "tnts.config.title": "Config de TNTs Loucas",
    "tnts.config.hint": "Escolha um preset (aplica na hora, sem reiniciar) ou edite tnts-common.toml",
    "tnts.config.open": "Abrir pasta de config",
    "tnts.preset.locura": "💥 Loucura total",
    "tnts.preset.equilibrado": "⚖️ Equilibrado",
    "tnts.preset.suave": "🕊️ Suave",
    "tnts.preset.active": "Preset ativo: x%s (poder) — aplica na hora em singleplayer",
    "advancements.tnts.root.title": "TNTs Loucas!",
    "advancements.tnts.root.description": "Consiga qualquer TNT do mod",
    "advancements.tnts.first_boom.title": "A mecha!",
    "advancements.tnts.first_boom.description": "Acenda uma TNT com isqueiro",
    "advancements.tnts.all_variants.title": "Coleção explosiva",
    "advancements.tnts.all_variants.description": "Exploda uma TNT de cada tipo",
    "advancements.tnts.mass_detonation.title": "Detonação em massa!",
    "advancements.tnts.mass_detonation.description": "Detone 10 ou mais TNTs de uma vez com o Detonador Remoto",
    "tnts.title.mass_detonation": "Detonação em massa!",
    "tnts.subtitle.mass_detonation": "%s TNTs acesas de uma vez",
}

MANUAL_PT = {
    "mega_tnt": "Explosão enorme. Clássica, mas com mais boom.",
    "mini_tnt": "Pequena e discreta. Ideal para demolições finas.",
    "lava_tnt": "Deixa poças de lava na cratera.",
    "rapida_tnt": "Mecha instantânea: 1 segundo e já era.",
    "hielo_tnt": "Congela a água e cobre a área de neve.",
    "saltarina_tnt": "Lança todo mundo pelos ares.",
    "nuclear_tnt": "Coluna de fumaça e brilho radioativo.",
    "limpia_tnt": "Não quebra blocos: só danifica entidades.",
    "rayo_tnt": "Invoca 3 raios ao redor da cratera.",
    "trampa_tnt": "Pia que nem louca antes de explodir.",
    "oro_tnt": "Chuva de barras de ouro.",
    "obsidiana_tnt": "Transforma a cratera em obsidiana.",
    "crio_tnt": "Congela mobs e jogadores.",
    "xp_tnt": "Solta orbes de experiência.",
    "agua_tnt": "Inunda a área e apaga o fogo.",
    "arena_tnt": "Avalanche de areia caindo do céu.",
    "diamante_tnt": "Chuva de diamantes.",
    "esmeralda_tnt": "Chuva de esmeraldas.",
    "negra_tnt": "Abre um buraco negro 3D que devora blocos, itens e seres vivos.",
    "viento_tnt": "Rajada que empurra tudo para longe. Não quebra blocos.",
    "inferno_tnt": "Incendeia a área e invoca mobs do Nether.",
    "hongo_tnt": "Espalha cogumelos e micélio por toda parte.",
    "miel_tnt": "Pegajosa: lentidão extrema e blocos de mel.",
    "heal_tnt": "Estouro suave que cura e dá regeneração.",
    "teleport_tnt": "Teletransporta seres vivos para lugares aleatórios.",
    "confeti_tnt": "Chuva de partículas coloridas. Festa total.",
    "mina_tnt": "Explode ao pisar. Pia antes de estourar.",
    "terremoto_tnt": "Cratera de raio 9, rachaduras com lava e lança tudo lá em cima.",
    "meteorito_tnt": "8-12 meteoros 3D caem do céu com rastro de fogo.",
    "tormenta_tnt": "3 raios na hora e uma nuvem que castiga por 8 segundos.",
    "colosal_tnt": "Explosão em 3 ondas progressivas. A maior de todas.",
    "supernova_tnt": "Flash ofuscante, 400-600 XP e esfera de luz 3D.",
    "toxica_tnt": "Nuvem verde que envenena e enfraquece. Não quebra blocos.",
    "fuegos_tnt": "Lança 8-16 foguetes coloridos ao céu.",
    "gravitatoria_tnt": "Gravidade brutal: esmaga tudo contra o chão.",
    "ender_tnt": "Teletransporta seres vivos e invoca endermites.",
    "bubble_tnt": "Suga para a cratera como um redemoinho e derrete o gelo.",
    "solar_tnt": "Faz dia, limpa o tempo e incendeia uma área enorme.",
    "casa_tnt": "Gera uma casa aconchegante de madeira, pronta para morar.",
    "mansion_tnt": "Gera uma mansão de luxo com dois andares, torres e decoração.",
}

# ---- Deutsch (de_de) ----
LANG_DE = {
    "itemGroup.tnts": "Verückte TNTs",
    "block.tnts.mega_tnt": "Mega-TNT",
    "block.tnts.mini_tnt": "Mini-TNT",
    "block.tnts.lava_tnt": "Lava-TNT",
    "block.tnts.rapida_tnt": "Sofort-TNT",
    "block.tnts.hielo_tnt": "Eis-TNT",
    "block.tnts.saltarina_tnt": "Hüpf-TNT",
    "block.tnts.nuclear_tnt": "Atom-TNT",
    "block.tnts.limpia_tnt": "Saubere TNT",
    "block.tnts.rayo_tnt": "Blitz-TNT",
    "block.tnts.trampa_tnt": "Fallen-TNT",
    "block.tnts.oro_tnt": "Gold-TNT",
    "block.tnts.obsidiana_tnt": "Obsidian-TNT",
    "block.tnts.crio_tnt": "Kryo-TNT",
    "block.tnts.xp_tnt": "XP-TNT",
    "block.tnts.agua_tnt": "Wasser-TNT",
    "block.tnts.arena_tnt": "Sand-TNT",
    "block.tnts.diamante_tnt": "Diamant-TNT",
    "block.tnts.esmeralda_tnt": "Smaragd-TNT",
    "block.tnts.negra_tnt": "Schwarzes-Loch-TNT",
    "block.tnts.viento_tnt": "Wind-TNT",
    "block.tnts.inferno_tnt": "Inferno-TNT",
    "block.tnts.hongo_tnt": "Pilz-TNT",
    "block.tnts.miel_tnt": "Honig-TNT",
    "block.tnts.heal_tnt": "Heil-TNT",
    "block.tnts.teleport_tnt": "Teleport-TNT",
    "block.tnts.confeti_tnt": "Konfetti-TNT",
    "block.tnts.mina_tnt": "TNT-Mine",
    "block.tnts.terremoto_tnt": "Erdbeben-TNT",
    "block.tnts.meteorito_tnt": "Meteor-TNT",
    "block.tnts.tormenta_tnt": "Sturm-TNT",
    "block.tnts.colosal_tnt": "Kolossal-TNT",
    "block.tnts.supernova_tnt": "Supernova-TNT",
    "block.tnts.toxica_tnt": "Gift-TNT",
    "block.tnts.fuegos_tnt": "Feuerwerks-TNT",
    "block.tnts.gravitatoria_tnt": "Schwerkraft-TNT",
    "block.tnts.ender_tnt": "End-TNT",
    "block.tnts.bubble_tnt": "Blasen-TNT",
    "block.tnts.solar_tnt": "Solar-TNT",
    "block.tnts.casa_tnt": "Haus-TNT",
    "block.tnts.mansion_tnt": "Herrenhaus-TNT",
    "block.tnts.tnt_table": "TNT-Werkbank",
    "block.tnts.tnt_altar": "TNT-Königs-Altar",
    "item.tnts.tnt_manual": "TNT-Handbuch",
    "item.tnts.tnt_disc": "TNTs Locos-Schallplatte",
    "item.record.music.tnts.tema.desc": "Codebuff - TNTs Locos Thema",
    "item.tnts.tnt_king_crown": "Krone des TNT-Königs",
    "item.tnts.tnt_king_sword": "Schwert des TNT-Königs",
    "item.tnts.tnt_shield": "TNT-Schild",
    "item.tnts.tnt_king_spawn_egg": "TNT-König spawnen",
    "entity.tnts.tnt_king": "TNT-König",
    "entity.minecraft.villager.tnt_expert": "TNT-Experte",
    "tnts.manual.hint": "Alle TNTs des Mods: Effekt und Radius (Config)",
    "tnts.manual.power": "Radius: %s",
    "advancements.tnts.defeat_king.title": "Der König ist gefallen!",
    "advancements.tnts.defeat_king.description": "Besiege den TNT-König und nimm seine Krone",
    "advancements.tnts.double_jump.title": "Doppelsprung!",
    "advancements.tnts.double_jump.description": "Mach einen Explosionssprung mit den TNT-Stiefeln",
    "advancements.tnts.night_vision.title": "TNT-Sicht",
    "advancements.tnts.night_vision.description": "Trage den TNT-Helm und sieh im Dunkeln",
    "advancements.tnts.king_set_2.title": "Macht des Königs",
    "advancements.tnts.king_set_2.description": "Trage 2 Königsteile: Das Schwert zündet TNTs mit doppelter Reichweite",
    "advancements.tnts.king_set_3.title": "Königsschild",
    "advancements.tnts.king_set_3.description": "Trage 3 Königsteile: Werde immun gegen deine eigenen TNTs",
    "advancements.tnts.king_set_4.title": "Aura des Königs",
    "advancements.tnts.king_set_4.description": "Trage alle 4 Königsteile: Deine Aura zündet nahe TNTs",
    "advancements.tnts.king_guardian_defeated.title": "Wache besiegt",
    "advancements.tnts.king_guardian_defeated.description": "Besiege den Wächterkönig des TNT-Lagers",
    "advancements.tnts.king_phase.title": "Es rissst...",
    "advancements.tnts.king_phase.description": "Bring den TNT-König an den Rand: Sieh ihn ganz zerbrechen",
    "item.tnts.detonator": "Fernzünder",
    "item.tnts.launcher": "TNT-Werfer",
    "item.tnts.tnt_arrow": "TNT-Pfeil",
    "item.tnts.grenade": "TNT-Granate",
    "item.tnts.tnt_chestplate": "TNT-Brustpanzer",
    "item.tnts.tnt_boots": "TNT-Stiefel",
    "item.tnts.tnt_helmet": "TNT-Helm",
    "item.tnts.tnt_leggings": "TNT-Hose",
    "item.tnts.tnt_pickaxe": "TNT-Spitzhacke",
    "tnts.config.title": "TNTs Locos Konfiguration",
    "tnts.config.hint": "Wähle ein Preset (sofort anwendbar, ohne Neustart) oder bearbeite tnts-common.toml",
    "tnts.config.open": "Config-Ordner öffnen",
    "tnts.preset.locura": "💥 Volle Dröhnung",
    "tnts.preset.equilibrado": "⚖️ Ausgeglichen",
    "tnts.preset.suave": "🕊️ Sanft",
    "tnts.preset.active": "Aktives Preset: x%s (Stärke) — wird sofort in Einzelspieler angewendet",
    "advancements.tnts.root.title": "Verückte TNTs!",
    "advancements.tnts.root.description": "Beschaffe irgendeine TNT aus dem Mod",
    "advancements.tnts.first_boom.title": "Zündung!",
    "advancements.tnts.first_boom.description": "Zünde eine TNT mit einem Feuerzeug an",
    "advancements.tnts.all_variants.title": "Explosive Sammlung",
    "advancements.tnts.all_variants.description": "Zünde eine TNT von jedem Typ",
    "advancements.tnts.mass_detonation.title": "Massen-Detonation!",
    "advancements.tnts.mass_detonation.description": "Zünde 10+ TNTs gleichzeitig mit dem Fernzünder",
    "tnts.title.mass_detonation": "Massen-Detonation!",
    "tnts.subtitle.mass_detonation": "%s TNTs gleichzeitig gezündet",
}

MANUAL_DE = {
    "mega_tnt": "Riesige Explosion. Klassisch, aber mit mehr Bum.",
    "mini_tnt": "Klein und dezent. Ideal für feine Abbrüche.",
    "lava_tnt": "Hinterlässt Lavaseen im Krater.",
    "rapida_tnt": "Sofortige Zündschnur: 1 Sekunde und weg.",
    "hielo_tnt": "Friert Wasser ein und bedeckt die Gegend mit Schnee.",
    "saltarina_tnt": "Schleudert alle in die Luft.",
    "nuclear_tnt": "Rauchsäule und radioaktiver Schein.",
    "limpia_tnt": "Zerbricht keine Blöcke: schadet nur Wesen.",
    "rayo_tnt": "Beschwört 3 Blitze rund um den Krater.",
    "trampa_tnt": "Piept wie verrückt, bevor sie explodiert.",
    "oro_tnt": "Regen aus Goldbarren.",
    "obsidiana_tnt": "Verwandelt den Krater in Obsidian.",
    "crio_tnt": "Friert Mobs und Spieler ein.",
    "xp_tnt": "Lässt Erfahrungsorbes fallen.",
    "agua_tnt": "Flutet die Gegend und löscht Feuer.",
    "arena_tnt": "Sandlawine vom Himmel.",
    "diamante_tnt": "Regen aus Diamanten.",
    "esmeralda_tnt": "Regen aus Smaragden.",
    "negra_tnt": "Öffnet ein 3D-Schwarzes Loch, das Blöcke, Items und Wesen verschlingt.",
    "viento_tnt": "Böe, die alles wegstößt. Zerstört keine Blöcke.",
    "inferno_tnt": "Entzündet die Gegend und ruft Nether-Mobs.",
    "hongo_tnt": "Verbreitet Pilze und Myzel überall.",
    "miel_tnt": "Klebrig: extreme Langsamkeit und Honigblöcke.",
    "heal_tnt": "Sanfter Stoß, der heilt und Regeneration gibt.",
    "teleport_tnt": "Teleportiert Lebewesen an zufällige Orte.",
    "confeti_tnt": "Bunter Partikelregen. Volle Party.",
    "mina_tnt": "Explodiert beim Betreten. Piept vorher.",
    "terremoto_tnt": "Krater mit Radius 9, Lava-Spalten und wirft alles hoch.",
    "meteorito_tnt": "8-12 3D-Meteore fallen mit Feuerspur vom Himmel.",
    "tormenta_tnt": "3 Blitze sofort plus eine strafende 8-Sekunden-Wolke.",
    "colosal_tnt": "Explosion in 3 progressiven Wellen. Die größte.",
    "supernova_tnt": "Blendender Blitz, 400-600 XP und eine 3D-Lichtkugel.",
    "toxica_tnt": "Grüne Wolke, die vergiftet und schwächt. Zerstört keine Blöcke.",
    "fuegos_tnt": "Schießt 8-16 bunte Raketen in den Himmel.",
    "gravitatoria_tnt": "Brutale Schwerkraft: zermalmt alles auf den Boden.",
    "ender_tnt": "Teleportiert Lebewesen und ruft Endermiten.",
    "bubble_tnt": "Saugt wie ein Strudel in den Krater und schmilzt Eis.",
    "solar_tnt": "Macht Tag, klärt das Wetter und entzündet eine riesige Fläche.",
    "casa_tnt": "Erzeugt ein gemütliches Holzhaus, bereit zum Wohnen.",
    "mansion_tnt": "Erzeugt eine Luxusvilla mit zwei Stockwerken, Türmen und Deko.",
}

# ---- Français (fr_fr) ----
LANG_FR = {
    "itemGroup.tnts": "TNTs Dingo",
    "block.tnts.mega_tnt": "Méga TNT",
    "block.tnts.mini_tnt": "Mini TNT",
    "block.tnts.lava_tnt": "TNT de Lave",
    "block.tnts.rapida_tnt": "TNT Instantanée",
    "block.tnts.hielo_tnt": "TNT de Glace",
    "block.tnts.saltarina_tnt": "TNT Rebondissante",
    "block.tnts.nuclear_tnt": "TNT Nucléaire",
    "block.tnts.limpia_tnt": "TNT Propre",
    "block.tnts.rayo_tnt": "TNT de Foudre",
    "block.tnts.trampa_tnt": "TNT Piège",
    "block.tnts.oro_tnt": "TNT d'Or",
    "block.tnts.obsidiana_tnt": "TNT d'Obsidienne",
    "block.tnts.crio_tnt": "TNT Cryogénique",
    "block.tnts.xp_tnt": "TNT d'Expérience",
    "block.tnts.agua_tnt": "TNT d'Eau",
    "block.tnts.arena_tnt": "TNT de Sable",
    "block.tnts.diamante_tnt": "TNT de Diamant",
    "block.tnts.esmeralda_tnt": "TNT d'Émeraude",
    "block.tnts.negra_tnt": "TNT Trou Noir",
    "block.tnts.viento_tnt": "TNT de Vent",
    "block.tnts.inferno_tnt": "TNT Inferno",
    "block.tnts.hongo_tnt": "TNT de Champignons",
    "block.tnts.miel_tnt": "TNT de Miel",
    "block.tnts.heal_tnt": "TNT Curative",
    "block.tnts.teleport_tnt": "TNT Téléportation",
    "block.tnts.confeti_tnt": "TNT de Confettis",
    "block.tnts.mina_tnt": "Mine TNT",
    "block.tnts.terremoto_tnt": "TNT Tremblement de terre",
    "block.tnts.meteorito_tnt": "TNT Météore",
    "block.tnts.tormenta_tnt": "TNT Tempête",
    "block.tnts.colosal_tnt": "TNT Colossale",
    "block.tnts.supernova_tnt": "TNT Supernova",
    "block.tnts.toxica_tnt": "TNT Toxique",
    "block.tnts.fuegos_tnt": "TNT de Feux d'artifice",
    "block.tnts.gravitatoria_tnt": "TNT Gravitationnelle",
    "block.tnts.ender_tnt": "TNT de l'End",
    "block.tnts.bubble_tnt": "TNT Bulle",
    "block.tnts.solar_tnt": "TNT Solaire",
    "block.tnts.casa_tnt": "TNT Maison",
    "block.tnts.mansion_tnt": "TNT Manoir",
    "block.tnts.tnt_table": "Table de TNTs",
    "block.tnts.tnt_altar": "Autel du Roi TNT",
    "item.tnts.tnt_manual": "Manuel de TNTs",
    "item.tnts.tnt_disc": "Disque de TNTs Dingo",
    "item.record.music.tnts.tema.desc": "Codebuff - Thème TNTs Dingo",
    "item.tnts.tnt_king_crown": "Couronne du Roi TNT",
    "item.tnts.tnt_king_sword": "Épée du Roi TNT",
    "item.tnts.tnt_shield": "Bouclier TNT",
    "item.tnts.tnt_king_spawn_egg": "Invoquer le Roi TNT",
    "entity.tnts.tnt_king": "Roi TNT",
    "entity.minecraft.villager.tnt_expert": "Expert en TNTs",
    "tnts.manual.hint": "Toutes les TNTs du mod : effet et rayon (config)",
    "tnts.manual.power": "Rayon : %s",
    "advancements.tnts.defeat_king.title": "Le Roi est tombé !",
    "advancements.tnts.defeat_king.description": "Vainquez le Roi TNT et récupérez sa couronne",
    "advancements.tnts.double_jump.title": "Double saut !",
    "advancements.tnts.double_jump.description": "Faites un bond explosif avec les Bottes TNT",
    "advancements.tnts.night_vision.title": "Vision TNT",
    "advancements.tnts.night_vision.description": "Portez le Casque TNT et voyez dans le noir",
    "advancements.tnts.king_set_2.title": "Pouvoir du Roi",
    "advancements.tnts.king_set_2.description": "Portez 2 pièces du set du Roi : l'Épée allume les TNT à double portée",
    "advancements.tnts.king_set_3.title": "Bouclier Royal",
    "advancements.tnts.king_set_3.description": "Portez 3 pièces du set du Roi : soyez immunisé contre vos propres TNT",
    "advancements.tnts.king_set_4.title": "Aura du Roi",
    "advancements.tnts.king_set_4.description": "Portez les 4 pièces du set du Roi : votre aura allume les TNT proches",
    "advancements.tnts.king_guardian_defeated.title": "Garde vaincu",
    "advancements.tnts.king_guardian_defeated.description": "Vainquez le Roi gardien de l'Entrepôt TNT",
    "advancements.tnts.king_phase.title": "Ça se fissure...",
    "advancements.tnts.king_phase.description": "Menez le Roi TNT au bord : regardez-le se fissurer complètement",
    "item.tnts.detonator": "Détonateur à distance",
    "item.tnts.launcher": "Lanceur de TNT",
    "item.tnts.tnt_arrow": "Flèche TNT",
    "item.tnts.grenade": "Grenade TNT",
    "item.tnts.tnt_chestplate": "Plastron TNT",
    "item.tnts.tnt_boots": "Bottes TNT",
    "item.tnts.tnt_helmet": "Casque TNT",
    "item.tnts.tnt_leggings": "Pantalon TNT",
    "item.tnts.tnt_pickaxe": "Pioche TNT",
    "tnts.config.title": "Config de TNTs Dingo",
    "tnts.config.hint": "Choisissez un préréglage (application immédiate, sans redémarrer) ou modifiez tnts-common.toml",
    "tnts.config.open": "Ouvrir le dossier de config",
    "tnts.preset.locura": "💥 Folie totale",
    "tnts.preset.equilibrado": "⚖️ Équilibré",
    "tnts.preset.suave": "🕊️ Doux",
    "tnts.preset.active": "Préréglage actif : x%s (puissance) — appliqué immédiatement en solo",
    "advancements.tnts.root.title": "TNTs Dingo !",
    "advancements.tnts.root.description": "Obtenez n'importe quelle TNT du mod",
    "advancements.tnts.first_boom.title": "À la mèche !",
    "advancements.tnts.first_boom.description": "Allumez une TNT avec un briquet",
    "advancements.tnts.all_variants.title": "Collection explosive",
    "advancements.tnts.all_variants.description": "Faites exploser une TNT de chaque type",
    "advancements.tnts.mass_detonation.title": "Détonation massive !",
    "advancements.tnts.mass_detonation.description": "Détonnez 10+ TNTs d'un coup avec le Détonateur à distance",
    "tnts.title.mass_detonation": "Détonation massive !",
    "tnts.subtitle.mass_detonation": "%s TNTs allumées d'un coup",
}

MANUAL_FR = {
    "mega_tnt": "Explosion énorme. Classique, mais avec plus de boum.",
    "mini_tnt": "Petite et discrète. Idéale pour la démolition fine.",
    "lava_tnt": "Laisse des flaques de lave dans le cratère.",
    "rapida_tnt": "Mèche instantanée : 1 seconde et terminé.",
    "hielo_tnt": "Gèle l'eau et couvre la zone de neige.",
    "saltarina_tnt": "Envoie tout le monde dans les airs.",
    "nuclear_tnt": "Colonne de fumée et lueur radioactive.",
    "limpia_tnt": "Ne casse pas les blocs : ne blesse que les entités.",
    "rayo_tnt": "Invoque 3 éclairs autour du cratère.",
    "trampa_tnt": "Bip frénétique avant d'exploser.",
    "oro_tnt": "Pluie de lingots d'or.",
    "obsidiana_tnt": "Transforme le cratère en obsidienne.",
    "crio_tnt": "Gèle les mobs et les joueurs.",
    "xp_tnt": "Lâche des orbes d'expérience.",
    "agua_tnt": "Inonde la zone et éteint le feu.",
    "arena_tnt": "Avalanche de sable tombant du ciel.",
    "diamante_tnt": "Pluie de diamants.",
    "esmeralda_tnt": "Pluie d'émeraudes.",
    "negra_tnt": "Ouvre un trou noir 3D qui dévore blocs, items et êtres vivants.",
    "viento_tnt": "Rafale qui repousse tout. Ne casse pas les blocs.",
    "inferno_tnt": "Enflamme la zone et invoque des mobs du Nether.",
    "hongo_tnt": "Répand champignons et mycélium partout.",
    "miel_tnt": "Collante : lenteur extrême et blocs de miel.",
    "heal_tnt": "Éclat doux qui soigne et donne la régénération.",
    "teleport_tnt": "Téléporte les êtres vivants au hasard.",
    "confeti_tnt": "Pluie de particules colorées. Fête totale.",
    "mina_tnt": "Explose quand on marche dessus. Bip d'abord.",
    "terremoto_tnt": "Cratère de rayon 9, fissures de lave, projette tout très haut.",
    "meteorito_tnt": "8-12 météores 3D tombent du ciel avec traînée de feu.",
    "tormenta_tnt": "3 éclairs instantanés plus un nuage punitif de 8 s.",
    "colosal_tnt": "Explosion en 3 vagues progressives. La plus grosse.",
    "supernova_tnt": "Flash aveuglant, 400-600 XP et sphère de lumière 3D.",
    "toxica_tnt": "Nuage vert qui empoisonne et affaiblit. Ne casse pas les blocs.",
    "fuegos_tnt": "Lance 8-16 fusées colorées dans le ciel.",
    "gravitatoria_tnt": "Gravité brutale : écrase tout au sol.",
    "ender_tnt": "Téléporte les êtres vivants et invoque des endermites.",
    "bubble_tnt": "Aspire vers le cratère comme un tourbillon et fait fondre la glace.",
    "solar_tnt": "Fait jour, dégage le temps et enflamme une grande zone.",
    "casa_tnt": "Génère une maison en bois chaleureuse, prête à habiter.",
    "mansion_tnt": "Génère un manoir de luxe à deux étages, avec tours et déco.",
}


def write_lang():
    # (lang, manual, patron title, patron description)
    langs = [
        (LANG_ES, MANUAL_ES, "Craftea la {n}", "Consigue una {n} crafteándola"),
        (LANG_EN, MANUAL_EN, "Craft the {n}", "Craft a {n}"),
        (LANG_PT, MANUAL_PT, "Crafte a {n}", "Consiga uma {n} crafteando-a"),
        (LANG_DE, MANUAL_DE, "Stelle die {n} her", "Stelle eine {n} her"),
        (LANG_FR, MANUAL_FR, "Fabriquez la {n}", "Obtenez une {n} en la fabriquant"),
    ]
    files = {"es_es.json": LANG_ES, "en_us.json": LANG_EN,
             "pt_br.json": LANG_PT, "de_de.json": LANG_DE, "fr_fr.json": LANG_FR}
    for name in NAMES:
        for lang, manual, t_pat, d_pat in langs:
            label = lang[f"block.tnts.{name}"]
            lang[f"advancements.tnts.craft.{name}.title"] = t_pat.format(n=label)
            lang[f"advancements.tnts.craft.{name}.description"] = d_pat.format(n=label)
            if name in manual:
                lang[f"tnts.manual.{name}"] = manual[name]
    for fname, lang in files.items():
        write(os.path.join(ASSETS, "lang", fname), lang)


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
    make_boots_texture()
    make_helmet_texture()
    make_leggings_texture()
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
    make_tnt_king_sword_texture()
    make_tnt_shield_texture()
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
    for item in ("tnt_manual", "tnt_disc", "tnt_king_crown", "tnt_king_sword", "tnt_shield"):
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
    # botas de TNT: 4 TNT (dos a dos) + casco de TNT: 5 TNT
    write(os.path.join(DATA, "recipes", "tnt_boots.json"), {
        "type": "minecraft:crafting_shaped",
        "pattern": ["T T", "T T"],
        "key": {"T": {"item": "minecraft:tnt"}},
        "result": {"item": "tnts:tnt_boots"},
    })
    write(os.path.join(DATA, "recipes", "tnt_helmet.json"), {
        "type": "minecraft:crafting_shaped",
        "pattern": ["TTT", "T T"],
        "key": {"T": {"item": "minecraft:tnt"}},
        "result": {"item": "tnts:tnt_helmet"},
    })
    # pantalon de TNT: 7 TNT (vanilla leggings)
    write(os.path.join(DATA, "recipes", "tnt_leggings.json"), {
        "type": "minecraft:crafting_shaped",
        "pattern": ["TTT", "T T", "T T"],
        "key": {"T": {"item": "minecraft:tnt"}},
        "result": {"item": "tnts:tnt_leggings"},
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

    # modelos de item del lanzador, flecha, peto, botas, casco, pico y granada
    for item in ("launcher", "tnt_arrow", "tnt_chestplate", "tnt_boots", "tnt_helmet",
                 "tnt_leggings", "tnt_pickaxe", "grenade"):
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
