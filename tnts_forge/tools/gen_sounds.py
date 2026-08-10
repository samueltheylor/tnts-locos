"""Sintetiza los sonidos del mod (siseos de mecha, explosiones con caracter,
pitido de trampa, clicks del detonador y disparo del lanzador) y los convierte
a .ogg con ffmpeg. Los mismos .ogg se copian al Resource Pack de Bedrock.

Calidad v2:
  * Explosiones reales (dominio publico / CC0) de la biblioteca
    "Red Library: Explosions" (USC Cinema / Sunset Editorial Collection,
    archive.org, https://archive.org/details/Red_Library_Explosions) se
    superponen bajo la capa sintetizada: dan el golpe y el cuerpo reales.
    Samples convertidos a mono 22050 Hz en tools/samples/ (licencia CC0 1.0,
    ver tools/samples/README.md).
  * Reverberacion Schroeder (4 comb + 2 allpass) en Python puro: sala y
    humedad por TNT (las masivas suenan en un espacio enorme).
  * Sub-grave descendente + transiente agudo al inicio de la explosion.
  * Eco de canon (aecho de ffmpeg) para las TNTs masivas.
  * Chispas (crackle) en la mecha + reverb ligero de sala.

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
SAMPLES_DIR = os.path.join(BASE, "tools", "samples")

NAMES = ["mega_tnt", "mini_tnt", "lava_tnt", "rapida_tnt", "hielo_tnt",
         "saltarina_tnt", "nuclear_tnt", "limpia_tnt", "rayo_tnt", "trampa_tnt",
         "oro_tnt", "obsidiana_tnt", "crio_tnt", "xp_tnt", "agua_tnt", "arena_tnt",
         "diamante_tnt", "esmeralda_tnt", "negra_tnt", "viento_tnt", "inferno_tnt",
         "hongo_tnt", "miel_tnt", "heal_tnt", "teleport_tnt", "confeti_tnt", "mina_tnt",
         "terremoto_tnt", "meteorito_tnt", "tormenta_tnt", "colosal_tnt", "supernova_tnt",
         "toxica_tnt", "fuegos_tnt", "gravitatoria_tnt"]

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
    "toxica_tnt": (1.10, 0.85),
    "fuegos_tnt": (0.90, 1.10),
    "gravitatoria_tnt": (1.30, 0.75),
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
    "toxica_tnt":     (1.80, 42, 0.90, 0.40, 0.40, [("sparkle", ())]),
    "fuegos_tnt":     (1.60, 55, 0.80, 0.90, 0.20, [("sparkle", ())]),
    "gravitatoria_tnt":(2.20, 34, 1.10, 0.60, 0.80, []),
}

# ---- samples reales CC0 (tools/samples/sample_N.wav, 22050 Hz mono) ----
# 1 Booming Blast | 2 Huge Explosion w/ Wood & Sand | 3 Short Explosion w/ Debris
# 4 Explosion w/ Debris | 5 Huge Explosion w/ Long Decay | 6 Huge Explosion
SAMPLE_FILES = [f"sample_{i}.wav" for i in range(1, 7)]


def seed_of(name):
    return sum(ord(c) for c in name) * 7919 % 100000


# ---------------- muestras reales ----------------

def _load_wav(path):
    """Lee un WAV PCM s16 mono y devuelve lista de floats en [-1, 1]."""
    with open(path, "rb") as f:
        data = f.read()
    assert data[:4] == b"RIFF" and data[8:12] == b"WAVE", f"no es WAV: {path}"
    pos = 12
    fmt = None
    while pos < len(data) and fmt is None:
        cid = data[pos:pos + 4]
        size = struct.unpack_from("<I", data, pos + 4)[0]
        if cid == b"fmt ":
            fmt = struct.unpack_from("<HHIIHH", data, pos + 8)
        pos += 8 + size
    channels, rate, bits = fmt[0], fmt[2], fmt[5]
    assert channels == 1 and bits == 16, f"esperaba mono s16: {path} {fmt}"
    pos = 12
    while pos < len(data):
        cid = data[pos:pos + 4]
        size = struct.unpack_from("<I", data, pos + 4)[0]
        if cid == b"data":
            raw = data[pos + 8:pos + 8 + size]
            n = len(raw) // 2
            vals = [struct.unpack_from("<h", raw, i * 2)[0] / 32767.0 for i in range(n)]
            return vals, rate
        pos += 8 + size
    raise ValueError(f"sin chunk data: {path}")


def _resample(x, src_rate, dst_rate):
    """Interpolacion lineal de src_rate a dst_rate."""
    if src_rate == dst_rate:
        return x
    n_out = int(len(x) * dst_rate / src_rate)
    step = src_rate / dst_rate
    out = []
    for j in range(n_out):
        p = j * step
        i = int(p)
        if i >= len(x) - 1:
            out.append(x[-1])
        else:
            f = p - i
            out.append(x[i] * (1 - f) + x[i + 1] * f)
    return out


def _stretch(x, factor):
    """Re-muestrea x a len(x)*factor (factor<1 = mas lento y mas grave)."""
    if factor == 1.0:
        return x
    out_len = int(len(x) * factor)
    step = (len(x) - 1) / max(1, out_len - 1)
    out = []
    for j in range(out_len):
        p = j * step
        i = int(p)
        if i >= len(x) - 1:
            out.append(x[-1])
        else:
            f = p - i
            out.append(x[i] * (1 - f) + x[i + 1] * f)
    return out


_SAMPLE_CACHE = {}


def get_sample(idx):
    """Sample real CC0 resampleado a 44100 Hz mono (cacheado)."""
    if idx not in _SAMPLE_CACHE:
        path = os.path.join(SAMPLES_DIR, SAMPLE_FILES[idx])
        vals, rate = _load_wav(path)
        _SAMPLE_CACHE[idx] = _resample(vals, rate, RATE)
    return _SAMPLE_CACHE[idx]


def _attack_point(x, frac=0.15):
    """Primer indice donde la senal supera `frac` del pico (el golpe)."""
    peak = max(abs(v) for v in x) or 1.0
    for i, v in enumerate(x):
        if abs(v) > frac * peak:
            return max(0, i - int(0.03 * RATE))  # 30 ms antes para el ataque
    return 0


def layer_sample(x, dur, seed, mix):
    """Superpone el sample real (golpe + cuerpo) bajo la capa sintetizada.

    Corta desde el punto de ataque, lo estira si hace falta (mas lento =
    mas grave, natural en explosiones) y aplica una caida suave al final.
    """
    s = get_sample(seed % len(SAMPLE_FILES))
    start = _attack_point(s)
    avail = len(s) - start
    n = int(dur * RATE)
    if avail < n:
        # estirar: ralentiza y baja el tono
        step = avail / n
        for j in range(n):
            p = start + j * step
            i = int(p)
            f = p - i
            x[j] += (s[i] * (1 - f) + s[i + 1] * f) * mix
    else:
        # cortar a la duracion con fundido final
        fade = int(0.12 * RATE)
        for j in range(n):
            v = s[start + j]
            if n - j < fade:
                v *= (n - j) / fade
            x[j] += v * mix
    return x


# ---------------- reverb Schroeder ----------------

class _Comb:
    __slots__ = ("buf", "idx", "fb")

    def __init__(self, delay_s, fb):
        self.buf = [0.0] * max(1, int(delay_s * RATE))
        self.idx = 0
        self.fb = fb

    def tick(self, x):
        b = self.buf
        i = self.idx
        y = b[i]
        b[i] = x + y * self.fb
        self.idx = i + 1
        if self.idx >= len(b):
            self.idx = 0
        return y


class _Allpass:
    __slots__ = ("buf", "idx", "g")

    def __init__(self, delay_s, g):
        self.buf = [0.0] * max(1, int(delay_s * RATE))
        self.idx = 0
        self.g = g

    def tick(self, x):
        b = self.buf
        i = self.idx
        d = b[i]
        b[i] = x + d * self.g
        self.idx = i + 1
        if self.idx >= len(b):
            self.idx = 0
        return -self.g * x + d


def reverb(x, room, wet=0.35, dry=1.0, tail=1.2):
    """Reverberacion Schroeder (4 comb + 2 allpass). room 0-100."""
    f = 0.4 + room / 100.0 * 1.1
    fb = 0.72 + room / 100.0 * 0.18
    combs = [_Comb(0.0297 * f, fb - 0.02), _Comb(0.0371 * f, fb - 0.01),
             _Comb(0.0411 * f, fb), _Comb(0.0437 * f, fb - 0.03)]
    aps = [_Allpass(0.0050 * f, 0.7), _Allpass(0.0017 * f, 0.7)]
    out = []
    n = len(x) + int(tail * RATE)
    for i in range(n):
        s = x[i] if i < len(x) else 0.0
        acc = combs[0].tick(s) + combs[1].tick(s) + combs[2].tick(s) + combs[3].tick(s)
        acc = aps[0].tick(acc)
        acc = aps[1].tick(acc)
        out.append(s * dry + acc * wet)
    return out


# ---------------- sintesis ----------------

def hiss(dur, seed, speed):
    """Siseo de mecha: ruido agudo, parpadeo, chispas y ligera sala."""
    rng = random.Random(seed)
    n = int(dur * RATE)
    out = []
    prev = 0.0
    pop = 0.0  # chispa activa (decae por muestra)
    for i in range(n):
        t = i / RATE
        env = min(1.0, t / 0.05) * math.exp(-t * 1.6 / dur)
        white = rng.uniform(-1.0, 1.0)
        hp = white - prev * 0.6
        prev = white
        flicker = 0.75 + 0.25 * math.sin(2 * math.pi * (3.0 + speed) * t)
        # chispas: ~5 pops/seg, cada uno un click corto que decae en ~9 ms
        if pop <= 0.0 and rng.random() < 5.0 / RATE:
            pop = 1.0
        crackle = rng.uniform(-1.0, 1.0) * pop * 0.55
        pop *= 0.985
        out.append((hp * env * 0.30 * flicker * speed) + crackle)
    return reverb(out, room=18, wet=0.12, dry=1.0, tail=0.45)


def boom(dur, seed, freq, gain, crackle, tail, extras):
    """Explosion: transiente + sub-grave + retumbo + crepitar + sample real."""
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
        # cuerpo: ruido grave que decae
        noise = lp * math.exp(-t * 5.0) * 0.9 + lp2 * math.exp(-t * 9.0) * 0.35
        # retumbo grave con sub-grave descendente al inicio (el "golpe")
        sub = math.sin(2 * math.pi * (freq * 2.2 - 60.0 * min(t, 0.5)) * t) * math.exp(-t * 6.0)
        rumble = math.sin(2 * math.pi * freq * t) * math.exp(-t * 2.6)
        # transiente agudo inicial (el "crack" seco)
        crack = 0.0
        if crackle and (rng.random() < 0.25 or t < 0.012):
            crack = rng.uniform(-1.0, 1.0) * math.exp(-t * 7.0) * crackle * 0.4
        s = (noise * 0.75 + rumble * 0.55 + sub * 0.45 + crack) * gain
        if tail > 0:
            s += math.sin(2 * math.pi * 55 * t) * math.exp(-t * 1.2) * tail * 0.5
        for extra, args in extras:
            s += EXTRA_FNS[extra](t, *args)
        out.append(s)
    # capa real CC0 bajo la sintesis (golpe y cuerpo reales)
    mix = 0.22 + min(dur, 4.0) * 0.07
    layer_sample(out, dur, seed, mix)
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


def soft_limiter(x, target=0.85):
    """Normaliza el pico EXACTAMENTE a `target` y lo limita suavemente (tanh).
    Todos los sonidos quedan con pico consistente; el tamano lo dan la
    duracion, la frecuencia y la cola (no el volumen bruto)."""
    peak = max(abs(v) for v in x) or 1.0
    k = 1.1
    drive = math.tanh(k)
    return [math.tanh(v * k / peak) / drive * target for v in x]


def canyon_echo(x, delays=(0.4, 0.8, 1.3), decays=(0.28, 0.14, 0.07)):
    """Eco de canon para las masivas: repeticiones que decaen, sin tocar el
    sonido seco (a diferencia de aecho de ffmpeg, que atenua todo el output)."""
    out = list(x)
    for d, dec in zip(delays, decays):
        off = int(d * RATE)
        for i in range(off, len(x)):
            out[i] += x[i - off] * dec
    return out


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
    """Remolino grave de la bola negra (Agujero Negro): viento de tornado REAL
    (CC0, SSE Library: WIND) bajado de tono (~0.78x, mas grave y amenazante),
    con espiral de ~5 Hz y sub-grave con vibrato. Bucle de 1.6s con
    envolvente suave para que repita sin clics. En Java/Bedrock el pitch
    sube con cada pulso (0.7 + 0.15 * pulsos)."""
    dur = 1.6
    pitch = 0.78
    s, rate = _load_wav(os.path.join(SAMPLES_DIR, "vortex.wav"))
    s = _resample(s, rate, RATE)
    start = int(5.0 * RATE)  # seccion estable (el tornado es uniforme)
    win = s[start:start + int(dur * RATE / pitch)]
    base = _stretch(win, pitch)
    n = len(base)
    out = []
    lp = 0.0
    lp_hi = 0.0
    for i in range(n):
        t = i / RATE
        x = base[i]
        # paso bajo doble: el viento queda grave, sin la frialdad del aire agudo
        lp = lp * 0.82 + x * 0.18
        lp_hi = lp_hi * 0.94 + (x - lp_hi) * 0.06
        # espiral: modulacion de amplitud a ~5 Hz (sensacion de giro)
        swirl = 0.5 + 0.5 * math.sin(2 * math.pi * 5.0 * t)
        # retumbo subgrave con vibrato lento
        rumble = math.sin(2 * math.pi * (48 + 4 * math.sin(2 * math.pi * 2.3 * t)) * t)
        # envolvente de bucle: sube y baja suave para que el loop no clique
        env = min(1.0, t / 0.2) * min(1.0, (dur - t) / 0.25)
        out.append((lp * 0.65 + lp_hi * 0.35 + rumble * 0.45)
                   * (0.35 + 0.65 * swirl) * env)
    return soft_limiter(out, 0.80)


# ---------------- salida ----------------

def to_ogg(name, samples, out_dir, filters=None):
    data = bytearray()
    for s in samples:
        v = int(max(-1.0, min(1.0, s)) * 32767)
        data += struct.pack("<h", v)
    os.makedirs(out_dir, exist_ok=True)
    path = os.path.join(out_dir, name + ".ogg")
    args = ["ffmpeg", "-y", "-f", "s16le", "-ar", str(RATE), "-ac", "1", "-i", "-"]
    if filters:
        args += ["-af", filters]
    args += ["-c:a", "libvorbis", "-q:a", "6", path]
    subprocess.run(args, input=bytes(data),
                   stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=True)
    return path


def main():
    block_dir = os.path.join(JAVA_SOUNDS, "block", "tnt")
    item_dir = os.path.join(JAVA_SOUNDS, "item")

    for name in NAMES:
        # mecha: siseo con chispas + sala ligera
        dur, speed = FUSES[name]
        to_ogg(f"fuse_{name}", soft_limiter(hiss(dur, seed_of(name), speed), 0.80), block_dir)

        # explosion: sintesis + sample real CC0 + reverb por tamano + eco canon
        dur, freq, gain, crackle, tail, extras = EXPLOSIONS[name]
        body = boom(dur, seed_of(name) + 1, freq, gain, crackle, tail, extras)
        room = min(95.0, 25.0 + dur * 17.0)
        wet = min(0.60, 0.16 + dur * 0.10)
        rtail = min(2.5, 0.4 + dur * 0.5)
        out = reverb(body, room, wet=wet, dry=1.0, tail=rtail)
        if dur >= 2.4:
            # eco de canon para las masivas (despues se re-normaliza)
            out = canyon_echo(out)
        out = soft_limiter(out, min(0.90, 0.62 + gain * 0.15))
        to_ogg(f"explode_{name}", out, block_dir)

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
