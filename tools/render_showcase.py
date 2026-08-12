"""Render isométrico del AVIÓN y del GOLEM de TNT (imágenes de Modrinth).

Reconstruye las cajas de los modelos Java (mismas texOffs y posiciones) y
proyecta sus caras visibles con una proyección isométrica, muestreando la
textura real 128x64 de cada entidad. Genera dos PNG 1600x900 con fondo
degradado, sombra y título.

Convención de los modelos de entidad de Minecraft: y negativa = ARRIBA
(la llama del golem está en y=-30.5, los pies en y=0). La cara con ojos del
golem va en +z (textura 'front'), la nariz del avión apunta a +z.
"""
import math
import os

from PIL import Image, ImageDraw, ImageFont

W, H = 1600, 900

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))  # tnts_locos
TEX = os.path.join(ROOT, "tnts_forge", "src", "main", "resources",
                   "assets", "tnts", "textures", "entity")
OUT = os.path.join(ROOT, "showcase")

# ---------------------------------------------------------------------------
# modelos: (texOffs_u, texOffs_v, min(x,y,z), size(sx,sy,sz))
# yaw = rotación alrededor del eje vertical (grados), para un 3/4 más dinámico
# ---------------------------------------------------------------------------

GOLEM = {
    "texture": os.path.join(TEX, "tnt_golem.png"),
    "cubes": [
        ("cabeza",   (56, 0),  (-5.0, -24.0, -5.0), (10.0, 8.0, 10.0)),
        ("cuerpo",   (0, 0),   (-7.0, -16.0, -7.0), (14.0, 16.0, 14.0)),
        ("brazoIzq", (96, 0),  (-14.0, -16.0, -3.0), (6.0, 14.0, 6.0)),
        ("brazoDer", (96, 20), (8.0, -16.0, -3.0), (6.0, 14.0, 6.0)),
        ("piernaIzq", (0, 32), (-7.5, -8.0, -3.0), (6.0, 8.0, 6.0)),
        ("piernaDer", (24, 32), (1.5, -8.0, -3.0), (6.0, 8.0, 6.0)),
        ("mecha",    (120, 0), (-1.0, -27.0, 2.0), (2.0, 3.0, 2.0)),
        ("llama",    (100, 48), (-1.5, -30.5, 1.5), (3.0, 4.0, 3.0)),
    ],
    "yaw": 0.0,
    "camera": "front",   # muestra la cara con ojos (+z)
    "title": "Golem de TNT",
    "subtitle": "Tu aliado explosivo que te sigue y explota al mando",
}

PLANE = {
    "texture": os.path.join(TEX, "tnt_rocket.png"),
    "cubes": [
        ("fuselaje", (0, 0),   (-6.0, -8.0, -12.0), (12.0, 8.0, 24.0)),
        ("nariz",    (72, 0),  (-4.0, -6.0, 12.0), (8.0, 5.0, 6.0)),
        ("alaIzq",   (72, 16), (-20.0, -6.0, -2.0), (14.0, 2.0, 10.0)),
        ("alaDer",   (0, 48),  (6.0, -6.0, -2.0), (14.0, 2.0, 10.0)),
        ("timon",    (60, 48), (-1.0, -12.0, -14.0), (2.0, 10.0, 4.0)),
        ("planoCola", (72, 48), (-8.0, -5.0, -14.0), (16.0, 2.0, 5.0)),
        ("helice",   (96, 56), (-5.0, -10.0, 16.0), (10.0, 1.0, 2.0)),
        ("llama",    (56, 32), (-3.0, -9.0, -20.0), (6.0, 6.0, 6.0)),
    ],
    "yaw": 32.0,
    "camera": "front",   # nariz + ala + un vistazo de la llama trasera
    "title": "Avión de TNT",
    "subtitle": "Móntalo, despega y bombardea con TNTs desde el aire",
}

# direcciones de cámara en espacio modelo (x, y, z): +y del mundo es -y modelo
CAMERA_V = {
    "front": (1.0, -0.5774, 1.0),   # desde +x, arriba y +z -> ve +x, +z y el techo
    "back":  (1.0, -0.5774, -1.0),  # desde +x, arriba y -z -> ve +x, -z y el techo
}

COS30 = math.cos(math.radians(30.0))


def rot_yaw(x, z, a):
    """Rota (x, z) alrededor del eje vertical un ángulo a (radianes)."""
    c, s = math.cos(a), math.sin(a)
    return x * c + z * s, -x * s + z * c


def proj(x, y, z, yaw):
    """Proyección isométrica estándar con y modelo -y=arriba."""
    x, z = rot_yaw(x, z, yaw)
    px = (x - z) * COS30
    py = y + (x + z) * 0.5
    return px, py


# ---------------------------------------------------------------------------
# caras: (normal, pts en orden TL,TR,BR,BL, nombre de región de textura)
# ---------------------------------------------------------------------------

def face_quad(normal, x0, y0, z0, sx, sy, sz):
    if normal == "+y":    # cara en y0+sy (mundo: debajo) -> textura 'top'
        pts = [(x0, y0 + sy, z0 + sz), (x0 + sx, y0 + sy, z0 + sz),
               (x0 + sx, y0 + sy, z0), (x0, y0 + sy, z0)]
        return pts, "top"
    if normal == "-y":    # cara en y0 (mundo: techo) -> textura 'bottom'
        pts = [(x0, y0, z0 + sz), (x0 + sx, y0, z0 + sz),
               (x0 + sx, y0, z0), (x0, y0, z0)]
        return pts, "bottom"
    if normal == "+z":
        pts = [(x0, y0 + sy, z0 + sz), (x0 + sx, y0 + sy, z0 + sz),
               (x0 + sx, y0, z0 + sz), (x0, y0, z0 + sz)]
        return pts, "front"
    if normal == "-z":
        pts = [(x0 + sx, y0 + sy, z0), (x0, y0 + sy, z0),
               (x0, y0, z0), (x0 + sx, y0, z0)]
        return pts, "back"
    if normal == "+x":
        pts = [(x0 + sx, y0 + sy, z0 + sz), (x0 + sx, y0 + sy, z0),
               (x0 + sx, y0, z0), (x0 + sx, y0, z0 + sz)]
        return pts, "right"
    if normal == "-x":
        pts = [(x0, y0 + sy, z0), (x0, y0 + sy, z0 + sz),
               (x0, y0, z0 + sz), (x0, y0, z0)]
        return pts, "left"
    raise ValueError(normal)


def tex_region(u, v, sx, sy, sz, name):
    """Región de la textura 128x64 para una cara (layout estándar de entidad)."""
    regions = {
        "top":    (u + sz, v, sx, sz),
        "bottom": (u + sz + sx, v, sx, sz),
        "right":  (u, v + sz, sz, sy),
        "front":  (u + sz, v + sz, sx, sy),
        "left":   (u + sz + sx, v + sz, sz, sy),
        "back":   (u + sz + sx + sz, v + sz, sx, sy),
    }
    x0, y0, w, h = regions[name]
    return [(x0, y0), (x0 + w, y0), (x0 + w, y0 + h), (x0, y0 + h)]


# ---------------------------------------------------------------------------
# fondo + composicion
# ---------------------------------------------------------------------------

def make_background(palette):
    img = Image.new("RGB", (W, H))
    d = ImageDraw.Draw(img)
    top, bot = palette
    for y in range(H):
        t = y / H
        col = tuple(int(top[i] + (bot[i] - top[i]) * t) for i in range(3))
        d.line([(0, y), (W, y)], fill=col)
    return img


def draw_glow(img, cx, cy, radius, color, alpha_max):
    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    steps = 24
    for i in range(steps, 0, -1):
        r = radius * i / steps
        a = int(alpha_max * (1 - i / steps) ** 2)
        gd.ellipse([cx - r, cy - r, cx + r, cy + r], fill=color + (a,))
    img.paste(glow, (0, 0), glow)


def load_font(size, bold=True):
    candidates = [
        r"C:\Windows\Fonts\segoeuib.ttf" if bold else r"C:\Windows\Fonts\segoeui.ttf",
        r"C:\Windows\Fonts\arialbd.ttf" if bold else r"C:\Windows\Fonts\arial.ttf",
        r"C:\Windows\Fonts\bahnschrift.ttf",
    ]
    for path in candidates:
        try:
            return ImageFont.truetype(path, size)
        except Exception:
            continue
    return ImageFont.load_default()


def draw_title(img, title, subtitle, subtitle_color):
    d = ImageDraw.Draw(img)
    big = load_font(92)
    small = load_font(44)
    big2 = load_font(40)
    d.text((70, 60), "TNTs  Locos", font=big2, fill=(255, 230, 150, 255))
    d.text((70, 640), title, font=big, fill=(255, 255, 255, 255))
    d.text((72, 748), subtitle, font=small, fill=subtitle_color)
    badge = load_font(36)
    d.rounded_rectangle([70, 826, 320, 874], radius=24, fill=(190, 30, 45, 255))
    d.text((100, 832), "42 TNTs  •  Forge 1.20.1", font=badge, fill=(255, 255, 255, 255))


def build_faces(cfg):
    """Genera todas las caras con sus normales (giradas por yaw)."""
    yaw = math.radians(cfg["yaw"])
    faces = []
    for name, (u, v), (x0, y0, z0), (sx, sy, sz) in cfg["cubes"]:
        for normal in ("+y", "-y", "+x", "-x", "+z", "-z"):
            pts, region = face_quad(normal, x0, y0, z0, sx, sy, sz)
            # normal en espacio modelo, girada por yaw
            if normal == "+y":
                nx, ny, nz = 0.0, 1.0, 0.0
            elif normal == "-y":
                nx, ny, nz = 0.0, -1.0, 0.0
            else:
                nx, nz = rot_yaw(*{"+x": (1, 0), "-x": (-1, 0),
                                   "+z": (0, 1), "-z": (0, -1)}[normal], yaw)
                ny = 0.0
            faces.append({
                "name": name, "u": u, "v": v, "sx": sx, "sy": sy, "sz": sz,
                "normal": (nx, ny, nz), "region": region, "pts": pts,
            })
    return faces


def render_model(cfg, canvas, proj_fn, scale, offx, offy, shadow=True):
    """Dibuja las caras visibles (n·cam > 0), ordenadas por profundidad."""
    tex = Image.open(cfg["texture"]).convert("RGBA")
    cam = CAMERA_V[cfg["camera"]]
    cam_len = math.sqrt(sum(c * c for c in cam))
    faces = build_faces(cfg)

    draw_list = []
    for f in faces:
        n = f["normal"]
        if n[0] * cam[0] + n[1] * cam[1] + n[2] * cam[2] <= 0.0:
            continue  # cara oculta
        pts = [proj_fn(x, y, z) for x, y, z in f["pts"]]
        # profundidad a lo largo de la cámara: lejos primero
        cxm = sum(p[0] for p in pts) / 4
        cym = sum(p[1] for p in pts) / 4
        depth = -(cxm * cam[0] + cym * cam[1]) / cam_len
        f = dict(f)
        f["pts"] = pts
        f["depth"] = depth
        f["cy"] = cym
        draw_list.append(f)
    draw_list.sort(key=lambda f: (f["depth"], f["cy"]))

    if shadow and draw_list:
        minx = min(p[0] for f in draw_list for p in f["pts"])
        maxx = max(p[0] for f in draw_list for p in f["pts"])
        rw = int((maxx - minx) * scale * 0.55)
        sh = Image.new("RGBA", (W, H), (0, 0, 0, 0))
        sd = ImageDraw.Draw(sh)
        sd.ellipse([offx - rw, offy + 120, offx + rw, offy + 190],
                   fill=(0, 0, 0, 95))
        canvas.paste(sh, (0, 0), sh)

    for f in draw_list:
        pts = [(offx + p[0] * scale, offy + p[1] * scale) for p in f["pts"]]
        bx0 = min(p[0] for p in pts)
        by0 = min(p[1] for p in pts)
        bx1 = max(p[0] for p in pts)
        by1 = max(p[1] for p in pts)
        tw, th = int(math.ceil(bx1 - bx0)), int(math.ceil(by1 - by0))
        if tw <= 1 or th <= 1:
            continue
        src = tex_region(f["u"], f["v"], f["sx"], f["sy"], f["sz"], f["region"])
        quad_data = [src[0][0], src[0][1], src[1][0], src[1][1],
                     src[2][0], src[2][1], src[3][0], src[3][1]]
        face = Image.new("RGBA", (tw, th), (0, 0, 0, 0))
        face = tex.transform((tw, th), Image.Transform.QUAD, quad_data,
                             resample=Image.Resampling.BILINEAR)
        shade = {"+x": 1.0, "-x": 0.85, "+z": 1.0, "-z": 0.85,
                 "+y": 0.8, "-y": 1.08}.get(f["region"], 1.0)
        if shade != 1.0:
            r, g, b, a = face.split()
            r = r.point(lambda v: min(255, int(v * shade)))
            g = g.point(lambda v: min(255, int(v * shade)))
            b = b.point(lambda v: min(255, int(v * shade)))
            face = Image.merge("RGBA", (r, g, b, a))
        if face.size != (tw, th):
            face = face.resize((tw, th))
        canvas.paste(face, (int(bx0), int(by0)), face)


def render(cfg, out_path, palette, accent):
    canvas = make_background(palette)
    cx, cy = W - 520, 440

    # escala y centrado: ajusta a la caja proyectada del modelo
    yaw = math.radians(cfg["yaw"])
    all_pts = []
    for _, _, (x0, y0, z0), (sx, sy, sz) in cfg["cubes"]:
        for x in (x0, x0 + sx):
            for y in (y0, y0 + sy):
                for z in (z0, z0 + sz):
                    all_pts.append(proj(x, y, z, yaw))
    minx = min(p[0] for p in all_pts)
    maxx = max(p[0] for p in all_pts)
    miny = min(p[1] for p in all_pts)
    maxy = max(p[1] for p in all_pts)
    bw, bh = maxx - minx, maxy - miny
    scale = min(470.0 / bw, 560.0 / bh)
    cxm, cym = (minx + maxx) / 2, (miny + maxy) / 2
    offx = cx - cxm * scale
    offy = cy - cym * scale

    draw_glow(canvas, cx, cy, 470, accent, 60)
    render_model(cfg, canvas, lambda x, y, z: proj(x, y, z, yaw),
                 scale, offx, offy)
    draw_title(canvas, cfg["title"], cfg["subtitle"], accent)

    vig = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    vd = ImageDraw.Draw(vig)
    for i in range(70):
        a = int(60 * (1 - i / 70))
        vd.rectangle([i, i, W - i, H - i], outline=(0, 0, 0, a))
    canvas.paste(vig, (0, 0), vig)
    canvas.save(out_path)
    print("guardado:", out_path, canvas.size)


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    render(GOLEM, os.path.join(OUT, "golem.png"),
           ((24, 10, 30), (10, 6, 20)), (255, 130, 60))
    render(PLANE, os.path.join(OUT, "avion.png"),
           ((18, 26, 44), (8, 10, 26)), (120, 190, 255))
    print("listo")
