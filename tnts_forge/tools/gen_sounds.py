"""Sintetiza los sonidos del mod (siseos de mecha, explosiones con caracter,
pitido de trampa, clicks del detonador y disparo del lanzador) y los convierte
a .ogg con ffmpeg. Los mismos .ogg se copian al Resource Pack de Bedrock.

Uso:  python tools/gen_sounds.py
"""
import math
import os
import random
import shutil
import struct
import subprocess

RATE = 44100
BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))  # tnts_forge
JAVA_SOUNDS = os.path.join(BASE, "src", "main", "resources", "assets", "tnts", "sounds")
BP_RP = os.path.normpath(os.path.join(BASE, "..", "TNTsLocos_RP", "sounds"))

NAMES = ["mega_tnt", "mini_tnt", "lava_tnt", "rapida_tnt", "hielo_tnt",
         "saltarina_tnt", "nuclear_tnt", "limpia_tnt", "rayo_tnt", "trampa_tnt",
         "oro_tnt", "obsidiana_tnt", "crio_tnt", "xp_tnt", "agua_tnt", "arena_tnt",
         "diamante_tnt", "esmeralda_tnt", "negra_tnt", "viento_tnt", "inferno_tnt",
         "hongo_tnt", "miel_tnt", "heal_tnt", "teleport_tnt", "confeti_tnt", "mina_tnt",
         "terremoto_tnt", "meteorito_tnt", "tormenta_tnt", "colosal_tnt", "supernova_tnt"]

# mecha: (duracion segundos, velocidad del siseo)
FUSES = {
    "mega_tnt": (1.10, 0.90), "mini_tnt": (0.70, 1.10), "lava_tnt": (1.00, 0.95),
    "rapida_tnt": (0.50, 1.25), "hielo_tnt": (1.00, 0.95), "saltarina_tnt": (0.80, 1.00),
    "nuclear_tnt": (1.40, 0.80), "limpia_tnt": (1.00, 0.95), "rayo_tnt": (0.90, 1.05),
    "trampa_tnt": (1.50, 0.75), "oro_tnt": (1.00, 1.00), "obsidiana_tnt": (1.00, 0.90),
    "crio_tnt": (1.00, 1.00), "xp_tnt": (0.90, 1.10), "agua_tnt": (0.90, 1.05),
    "arena_tnt": (0.80, 1.15), "diamante_tnt": (1.00, 1.05), "esmeralda_tnt": (1.00, 1.05),
    "negra_tnt": (1.20, 0.80), "viento_tnt": (0.70, 1.20), "inferno_tnt": (1.10, 0.90),
    "hongo_tnt": (0.90, 1.00), "miel_tnt": (0.90, 0.95), "heal_tnt": (1.00, 1.00),
    "teleport_tnt": (1.00, 1.00),    "confeti_tnt": (0.80, 1.10), "mina_tnt": (0.60, 1.20),
    "terremoto_tnt": (1.50, 0.70), "meteorito_tnt": (1.40, 0.75),
    "tormenta_tnt": (1.30, 0.80), "colosal_tnt": (1.80, 0.60),
    "supernova_tnt": (1.60, 0.65),
}

# explosion: (duracion, frecuencia del retumbo, ganancia, crepitar, cola larga, extra)
EXPLOSIONS = {
    "mega_tnt":      (2.20, 45, 1.00, 0.60, 0.50, []),
    "mini_tnt":      (0.60, 70, 0.70, 0.20, 0.00, []),
    "lava_tnt":      (1.60, 50, 0.95, 1.00, 0.30, []),
    "rapida_tnt":    (0.80, 65, 0.80, 0.30, 0.00, []),
    "hielo_tnt":     (1.80, 55, 0.90, 0.40, 0.30, [("shimmer", (1400.0,))]),
    "saltarina_tnt": (1.20, 60, 0.80, 0.30, 0.20, [("spring", ())]),
    "nuclear_tnt":   (4.00, 32, 1.20, 0.80, 2.00, []),
    "limpia_tnt":    (1.20, 58, 0.75, 0.10, 0.20, []),
    "rayo_tnt":      (1.90, 40, 1.00, 1.20, 0.40, [("crack", ())]),
    "trampa_tnt":    (1.40, 55, 0.90, 0.40, 0.20, []),
    "oro_tnt":       (1.60, 52, 0.90, 0.40, 0.30, [("coin", ())]),
    "obsidiana_tnt": (1.70, 40, 1.05, 0.50, 0.40, []),
    "crio_tnt":      (1.80, 58, 0.85, 0.30, 0.30, [("shimmer", (2200.0,))]),
    "xp_tnt":        (1.50, 56, 0.85, 0.30, 0.20, [("sparkle", ())]),
    "agua_tnt":      (1.50, 48, 0.80, 0.50, 0.30, [("splash", ())]),
    "arena_tnt":     (1.40, 54, 0.80, 0.90, 0.20, []),
    "diamante_tnt":  (1.70, 52, 0.95, 0.40, 0.30, [("sparkle", ())]),
    "esmeralda_tnt": (1.60, 54, 0.90, 0.40, 0.30, [("coin", ())]),
    "negra_tnt":     (2.20, 30, 1.10, 0.60, 1.20, []),
    "viento_tnt":    (1.00, 70, 0.70, 0.20, 0.00, [("shimmer", (900.0,))]),
    "inferno_tnt":   (1.80, 45, 1.00, 1.20, 0.40, []),
    "hongo_tnt":     (1.30, 60, 0.80, 0.50, 0.20, []),
    "miel_tnt":      (1.20, 62, 0.75, 0.30, 0.20, []),
    "heal_tnt":      (1.40, 58, 0.80, 0.20, 0.30, [("shimmer", (2600.0,))]),
    "teleport_tnt":  (1.50, 50, 0.85, 0.40, 0.40, [("warp", ())]),
    "confeti_tnt":   (1.20, 64, 0.70, 0.60, 0.10, [("sparkle", ())]),
    "mina_tnt":      (0.90, 68, 0.85, 0.30, 0.10, []),
    "terremoto_tnt":  (3.00, 28, 1.20, 1.20, 1.00, [("rumble", ())]),
    "meteorito_tnt":  (2.60, 38, 1.10, 1.00, 0.80, [("crack", ())]),
    "tormenta_tnt":   (2.40, 34, 1.15, 1.40, 0.60, [("crack", ())]),
    "colosal_tnt":    (3.40, 26, 1.30, 1.20, 1.40, []),
    "supernova_tnt":  (3.00, 36, 1.10, 0.80, 1.20, [("shimmer", (2600.0,)), ("sparkle", ())]),
}


def seed_of(name):
    return sum(ord(c) for c in name) * 7919 % 100000


def to_ogg(name, samples, out_dir):
    data = bytearray()
    for s in samples:
        v = int(max(-1.0, min(1.0, s)) * 32767)
        data += struct.pack("<h", v)
    os.makedirs(out_dir, exist_ok=True)
    path = os.path.join(out_dir, name + ".ogg")
    subprocess.run(
        ["ffmpeg", "-y", "-f", "s16le", "-ar", str(RATE), "-ac", "1", "-i", "-",
         "-c:a", "libvorbis", "-q:a", "5", path],
        input=bytes(data), stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=True)
    return path


def hiss(dur, seed, speed):
    """Siseo de mecha: ruido agudo con decaimiento y parpadeo."""
    rng = random.Random(seed)
    n = int(dur * RATE)
    out = []
    prev = 0.0
    for i in range(n):
        t = i / RATE
        env = min(1.0, t / 0.05) * math.exp(-t * 1.6 / dur)
        white = rng.uniform(-1.0, 1.0)
        hp = white - prev * 0.6
        prev = white
        flicker = 0.75 + 0.25 * math.sin(2 * math.pi * (3.0 + speed) * t)
        out.append(hp * env * 0.32 * flicker * speed)
    return out


def boom(dur, seed, freq, gain, crackle, tail, extras):
    """Explosion: retumbo grave + ruido + crepitar, con cola opcional."""
    rng = random.Random(seed)
    n = int(dur * RATE)
    out = []
    lp = 0.0
    lp2 = 0.0
    for i in range(n):
        t = i / RATE
        white = rng.uniform(-1.0, 1.0)
        lp = lp * 0.80 + white * 0.20
        lp2 = lp2 * 0.60 + white * 0.40
        noise = lp * math.exp(-t * 5.0) * 0.9 + lp2 * math.exp(-t * 9.0) * 0.35
        rumble = math.sin(2 * math.pi * freq * t) * math.exp(-t * 2.6) * 0.9
        crack = 0.0
        if crackle and rng.random() < 0.25:
            crack = rng.uniform(-1.0, 1.0) * math.exp(-t * 7.0) * crackle * 0.35
        s = (noise * 0.8 + rumble * 0.6 + crack) * gain
        if tail > 0:
            s += math.sin(2 * math.pi * 55 * t) * math.exp(-t * 1.2) * tail * 0.5
        for extra, args in extras:
            s += EXTRA_FNS[extra](t, *args)
        out.append(s)
    return out


def _shimmer(t, f, amp=0.18):
    """Chirridos agudos de cristal (hielo / crio)."""
    if 0.15 < t < 1.2:
        return math.sin(2 * math.pi * f * t) * math.exp(-t * 3.0) * amp * (
                0.5 + 0.5 * math.sin(2 * math.pi * 11 * t))
    return 0.0


def _coin(t, amp=0.22):
    """Ding metalico de moneda (oro)."""
    if t < 0.5:
        return math.sin(2 * math.pi * 1800 * t) * math.exp(-t * 8.0) * amp
    return 0.0


def _sparkle(t, amp=0.12):
    """Chirrido ascendente (experiencia)."""
    if 0.1 < t < 0.7:
        return math.sin(2 * math.pi * 2400 * t * t) * math.exp(-t * 4.0) * amp
    return 0.0


def _splash(t, amp=0.30):
    """Chapoteo (agua)."""
    if t < 0.6:
        return math.sin(2 * math.pi * (400 + 900 * t) * t) * math.exp(-t * 6.0) * amp
    return 0.0


def _crack(t, amp=0.80):
    """Crepitar agudo inicial (rayo)."""
    if t < 0.08:
        return math.sin(2 * math.pi * (300 + t * 9000) * t) * (1 - t / 0.08) * amp
    return 0.0


def _spring(t, amp=0.30):
    """Boing hacia arriba (saltarina)."""
    if t < 0.4:
        return math.sin(2 * math.pi * (250 + 1600 * t) * t) * math.exp(-t * 6.0) * amp
    return 0.0


def _warp(t, amp=0.30):
    """Glissando ascendente (teletransportadora)."""
    if t < 0.5:
        return math.sin(2 * math.pi * (300 + 1400 * t) * t) * math.exp(-t * 5.0) * amp
    return 0.0


def _rumble(t, amp=0.30):
    """Retumbo grave sordo de terremoto."""
    if t < 1.2:
        return (math.sin(2 * math.pi * 36 * t) * 0.5
                + math.sin(2 * math.pi * 52 * t) * 0.3) * math.exp(-t * 2.2) * amp
    return 0.0


EXTRA_FNS = {
    "shimmer": _shimmer, "coin": _coin, "sparkle": _sparkle,
    "splash": _splash, "crack": _crack, "spring": _spring, "warp": _warp,
    "rumble": _rumble,
}


def beep():
    """Pitido corto de cuenta atras (TNT Trampa)."""
    dur = 0.10
    n = int(dur * RATE)
    out = []
    for i in range(n):
        t = i / RATE
        env = min(1.0, t / 0.008) * min(1.0, (dur - t) / 0.03)
        out.append(math.sin(2 * math.pi * 1150 * t) * env * 0.5)
    return out


def click():
    """Doble blip del Detonador Remoto."""
    out = [0.0] * int(0.16 * RATE)

    def blip(start, freq, dur, amp):
        n = int(dur * RATE)
        for i in range(n):
            t = i / RATE
            env = min(1.0, t / 0.005) * min(1.0, (dur - t) / 0.02)
            out[int(start * RATE) + i] += math.sin(2 * math.pi * freq * t) * env * amp

    blip(0.00, 1500, 0.04, 0.5)
    blip(0.09, 950, 0.05, 0.5)
    return out


def burst():
    """Rafaga del Detonador (10+ TNTs): triple click rapido + boom final."""
    out = [0.0] * int(0.5 * RATE)

    def blip(start, freq, dur, amp):
        n = int(dur * RATE)
        for i in range(n):
            t = i / RATE
            env = min(1.0, t / 0.004) * min(1.0, (dur - t) / 0.02)
            out[int(start * RATE) + i] += math.sin(2 * math.pi * freq * t) * env * amp

    blip(0.00, 1500, 0.04, 0.5)
    blip(0.10, 1500, 0.04, 0.5)
    blip(0.20, 1900, 0.06, 0.6)
    blip(0.32, 800, 0.10, 0.7)
    return out


def shoot():
    """Disparo del Lanzador de TNT: thunk + whoosh."""
    n = int(0.35 * RATE)
    rng = random.Random(7)
    out = []
    for i in range(n):
        t = i / RATE
        thunk = math.sin(2 * math.pi * 150 * t) * math.exp(-t * 25) * 0.8
        whoosh = rng.uniform(-1.0, 1.0) * min(1.0, t / 0.1) * math.exp(-t * 6) * 0.25
        out.append(thunk * 0.6 + whoosh)
    return out


def whirl():
    """Remolino grave de la bola negra (Agujero Negro): retumbo grave
    modulado en espiral (~5 Hz) que suena como un vortice girando.
    Bucle de 1.6s con envolvente suave para que repita sin clics."""
    dur = 1.6
    n = int(dur * RATE)
    rng = random.Random(42)
    out = []
    lp = 0.0
    for i in range(n):
        t = i / RATE
        white = rng.uniform(-1.0, 1.0)
        lp = lp * 0.86 + white * 0.14  # ruido grave (filtro paso bajo)
        # espiral: modulacion de amplitud a ~5 Hz (sensacion de giro)
        swirl = 0.5 + 0.5 * math.sin(2 * math.pi * 5.0 * t)
        # retumbo subgrave con vibrato lento
        rumble = math.sin(2 * math.pi * (48 + 4 * math.sin(2 * math.pi * 2.3 * t)) * t)
        # envolvente de bucle: sube y baja suave para que el loop no clique
        env = min(1.0, t / 0.2) * min(1.0, (dur - t) / 0.25)
        out.append((lp * 0.9 + rumble * 0.5) * (0.35 + 0.65 * swirl) * env * 0.9)
    return out


def main():
    block_dir = os.path.join(JAVA_SOUNDS, "block", "tnt")
    item_dir = os.path.join(JAVA_SOUNDS, "item")

    for name in NAMES:
        dur, speed = FUSES[name]
        to_ogg(f"fuse_{name}", hiss(dur, seed_of(name), speed), block_dir)
        dur, freq, gain, crackle, tail, extras = EXPLOSIONS[name]
        to_ogg(f"explode_{name}", boom(dur, seed_of(name) + 1, freq, gain, crackle, tail, extras), block_dir)

    to_ogg("beep", beep(), block_dir)
    to_ogg("detonator_click", click(), item_dir)
    to_ogg("detonator_burst", burst(), item_dir)
    to_ogg("launcher_shoot", shoot(), item_dir)

    entity_dir = os.path.join(JAVA_SOUNDS, "entity")
    to_ogg("black_hole_loop", whirl(), entity_dir)

    # copiar los mismos .ogg al Resource Pack de Bedrock
    for root, _, files in os.walk(JAVA_SOUNDS):
        for f in files:
            rel = os.path.relpath(os.path.join(root, f), JAVA_SOUNDS)
            dst = os.path.join(BP_RP, rel)
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            shutil.copy2(os.path.join(root, f), dst)

    print(f"Sonidos generados: {len(NAMES) * 2 + 5} .ogg (Java + Bedrock)")


if __name__ == "__main__":
    main()
