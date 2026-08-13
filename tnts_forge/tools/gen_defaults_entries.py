#!/usr/bin/env python3
"""Genera las entradas Java para TntDefaults desde NEW_TNTS."""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_massive_tnts import NEW_TNTS

# Mapeo de categorias a TntEffect enum values
CATEGORY_MAP = {
    "EXPLODE": "none()",
    "FIRE": "of(TntEffect.FIRE)",
    "ICE": "of(TntEffect.FREEZES, TntEffect.SNOW)",
    "WATER": "of(TntEffect.WATER)",
    "LIGHTNING": "of(TntEffect.LIGHTNING)",
    "TOXIC": "of(TntEffect.TOXIC)",
    "WIND": "of(TntEffect.WIND)",
    "GRAVITY": "of(TntEffect.GRAVITY)",
    "BLACKHOLE": "of(TntEffect.BLACKHOLE)",
    "TELEPORT": "of(TntEffect.TELEPORT)",
    "HEAL": "of(TntEffect.HEAL)",
    "XP": "of(TntEffect.XP)",
    "GOLD": "of(TntEffect.GOLD)",
    "DIAMOND": "of(TntEffect.DIAMOND)",
    "LAUNCH": "of(TntEffect.LAUNCH)",
    "NUCLEAR": "of(TntEffect.NUCLEAR)",
    "STORM": "of(TntEffect.STORM)",
    "METEOR": "of(TntEffect.METEOR)",
    "EARTHQUAKE": "of(TntEffect.EARTHQUAKE)",
    "COLOSSAL": "of(TntEffect.COLOSSAL)",
    "SUPERNOVA": "of(TntEffect.SUPERNOVA)",
    "ENDER": "of(TntEffect.ENDER)",
    "SOLAR": "of(TntEffect.SOLAR)",
    "CONFETI": "of(TntEffect.CONFETTI)",
    "FUNGI": "of(TntEffect.FUNGI)",
    "HONEY": "of(TntEffect.HONEY)",
    "LUCKY": "of(TntEffect.LUCKY)",
    "PORTAL": "of(TntEffect.PORTAL)",
    "HOUSE": "of(TntEffect.HOUSE)",
    "MANSION": "of(TntEffect.MANSION)",
    "TUNNEL": "of(TntEffect.TUNNEL)",
    "TRAP": "of(TntEffect.TRAP)",
    "FREEZES": "of(TntEffect.FREEZES)",
    "FIREWORKS": "of(TntEffect.FIREWORKS)",
    "LAVA": "of(TntEffect.LAVA)",
}

# Mapa de si rompe bloques (basado en la categoría)
BREAKS = {
    "EXPLODE": True, "FIRE": True, "ICE": True, "WATER": False,
    "LIGHTNING": True, "TOXIC": False, "WIND": False, "GRAVITY": False,
    "BLACKHOLE": True, "TELEPORT": False, "HEAL": False, "XP": False,
    "GOLD": True, "DIAMOND": True, "LAUNCH": False, "NUCLEAR": True,
    "STORM": True, "METEOR": True, "EARTHQUAKE": True, "COLOSSAL": True,
    "SUPERNOVA": True, "ENDER": True, "SOLAR": True, "CONFETI": False,
    "FUNGI": False, "HONEY": False, "LUCKY": True, "PORTAL": False,
    "HOUSE": False, "MANSION": False, "TUNNEL": True, "TRAP": True,
    "FREEZES": True, "FIREWORKS": False, "LAVA": True,
}

FIRE = {
    "EXPLODE": True, "FIRE": True, "ICE": True, "WATER": False,
    "LIGHTNING": True, "TOXIC": False, "WIND": False, "GRAVITY": False,
    "BLACKHOLE": True, "TELEPORT": False, "HEAL": False, "XP": False,
    "GOLD": True, "DIAMOND": True, "LAUNCH": True, "NUCLEAR": True,
    "STORM": True, "METEOR": True, "EARTHQUAKE": True, "COLOSSAL": True,
    "SUPERNOVA": True, "ENDER": True, "SOLAR": True, "CONFETI": False,
    "FUNGI": False, "HONEY": False, "LUCKY": True, "PORTAL": False,
    "HOUSE": False, "MANSION": False, "TUNNEL": True, "TRAP": True,
    "FREEZES": True, "FIREWORKS": False, "LAVA": True,
}

# Mapa de fuse ticks
FUSE = {
    "EXPLODE": 40, "LAUNCH": 30, "NUCLEAR": 50, "COLOSSAL": 70,
    "SUPERNOVA": 65, "STORM": 50, "METEOR": 55, "EARTHQUAKE": 60,
    "BLACKHOLE": 40, "TOXIC": 45, "FIREWORKS": 40, "GRAVITY": 55,
    "ENDER": 40, "SOLAR": 40, "HOUSE": 40, "MANSION": 50,
    "LUCKY": 40, "PORTAL": 50, "TUNNEL": 40, "TRAP": 80,
}

lines = []
for name, effect, palette, radius, breaks, fire, recipe, count, *_ in NEW_TNTS:
    fx = FIRE.get(effect, True)
    bk = BREAKS.get(effect, True)
    fuse = FUSE.get(effect, 40)
    eff = CATEGORY_MAP.get(effect, "none()")
    # MapColor based on effect
    mc = "MapColor.COLOR_RED"
    if "TOXIC" in effect or "FIRE" in effect or "INFERNO" in effect:
        mc = "MapColor.COLOR_GREEN" if "TOXIC" in effect else "MapColor.COLOR_ORANGE"
    elif "WATER" in effect or "WIND" in effect or "FREEZES" in effect or "ICE" in effect:
        mc = "MapColor.COLOR_LIGHT_BLUE" if "ICE" in effect or "FREEZES" in effect else "MapColor.WATER"
    elif "NUCLEAR" in effect or "COLOSSAL" in effect or "SUPERNOVA" in effect:
        mc = "MapColor.COLOR_BLACK"
    elif "LIGHTNING" in effect or "STORM" in effect:
        mc = "MapColor.COLOR_BLUE"
    elif "GOLD" in effect:
        mc = "MapColor.GOLD"
    elif "DIAMOND" in effect:
        mc = "MapColor.COLOR_CYAN"
    elif "ENDER" in effect or "TELEPORT" in effect:
        mc = "MapColor.COLOR_PURPLE"
    elif "CONFETI" in effect or "FIREWORKS" in effect:
        mc = "MapColor.COLOR_MAGENTA"
    elif "SOLAR" in effect:
        mc = "MapColor.COLOR_ORANGE"
    elif "HOUSE" in effect:
        mc = "MapColor.WOOD"
    elif "EARTHQUAKE" in effect or "TUNNEL" in effect:
        mc = "MapColor.COLOR_BROWN"
    elif "HEAL" in effect:
        mc = "MapColor.COLOR_PINK"
    elif "XP" in effect:
        mc = "MapColor.COLOR_LIGHT_GREEN"
    elif "GRAVITY" in effect or "BLACKHOLE" in effect:
        mc = "MapColor.COLOR_PURPLE"
    elif "SAND" in effect:
        mc = "MapColor.SAND"

    lines.append(f'        add("{name}",      new TntProperties({radius}f, {str(bk).lower()}, {str(fx).lower()},  {fuse}, {eff}));  // {effect}')

print("\n".join(lines))
print(f"\n// Total: {len(lines)} entradas nuevas")
