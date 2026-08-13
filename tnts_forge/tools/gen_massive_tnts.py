#!/usr/bin/env python3
"""
Generador masivo de TNTs: define ~150 TNTs/dinamitas, las mapea a categorías
de efectos y genera TODO el código Java, texturas, recetas y lang.

Uso: python tools/gen_massive_tnts.py
"""
import os, sys, json, struct, zlib, shutil

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(BASE, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "tnts")
DATA = os.path.join(RES, "data", "tnts")
TEX_DIR = os.path.join(ASSETS, "textures", "block")
ITEM_TEX_DIR = os.path.join(ASSETS, "textures", "item")
JAVA_SRC = os.path.join(BASE, "src", "main", "java", "com", "tnts")

# ============================================================
# DEFINICIONES DE TODAS LAS TNTs NUEVAS
# ============================================================
# Cada entrada: (nombre, efecto_categoria, paleta_5colores, radio, rompe_bloques, incendia, receta, lang_es, lang_en)

# Categorías de efectos (reutilizan la lógica existente o crean nueva)
# EXPLODE=explosión simple, FIRE=fuego, ICE=hielo, WATER=agua, LIGHTNING=rayo,
# TOXIC=veneno, WIND=viento, GRAVITY=gravedad, BLACKHOLE=agujero negro,
# TELEPORT=teletransporte, HEAL=curación, XP=experiencia, GOLD=oro,
# DIAMOND=diamante, LAUNCH=lanzar, NUCLEAR=nuclear, STORM=tormenta,
# METEOR=meteorito, EARTHQUAKE=terremoto, COLOSSAL=colosal, SUPERNOVA=supernova,
# ENDER=end, SOLAR=solar, CONFETI=confeti, FUNGI=hongo, HONEY=miel,
# LUCKY=suerte, PORTAL=portal, HOUSE=casa, MANSION=mansion

# Las TNTs que ya existen NO se duplican. Solo las NUEVAS.
NEW_TNTS = [
    # === BASIC TNTs (categoría simple) ===
    ("acidic_tnt",       "TOXIC",    ("#65a30d", "#d9f99d", "#365314", "#84cc16", "#365314"), 4.0, False, True,
     [("tnt", ""), ("spider_eye", ""), ("vine", "")], 1,
     "TNT Ácida", "Acidic TNT", "TNT Acide", "TNT Säure", "TNT Ácida"),

    ("animal_tnt",      "HEAL",     ("#92400e", "#fef3c7", "#451a03", "#d97706", "#451a03"), 3.0, False, False,
     [("tnt", ""), ("wheat", ""), ("wheat_seeds", "")], 1,
     "TNT Animal", "Animal Kingdom", "Royaume Animal", "TNT Tierreich", "TNT Animal"),

    ("cannon_tnt",      "LAUNCH",   ("#44403c", "#e7e5e4", "#1c1917", "#a8a29e", "#1c1917"), 5.0, True, True,
     [("tnt", ""), ("iron_ingot", ""), ("gunpowder", "")], 1,
     "TNT Cañón", "Cannon TNT", "TNT Canon", "TNT Kanone", "TNT Canhão"),

    ("catalyst_tnt",    "NUCLEAR",  ("#9f1239", "#ffe4e6", "#4c0519", "#fb7185", "#4c0519"), 8.0, True, True,
     [("tnt", ""), ("blaze_powder", ""), ("redstone", "")], 1,
     "TNT Catalizador", "Catalyst TNT", "TNT Catalyseur", "TNT Katalysator", "TNT Catalisador"),

    ("chicxulub_tnt",   "COLOSSAL", ("#78350f", "#fef3c7", "#1c1917", "#fde047", "#1c1917"), 25.0, True, True,
     [("mega_tnt", "tnts:"), ("mega_tnt", "tnts:"), ("nether_star", "")], 1,
     "TNT Chicxulub", "Chicxulub", "Chicxulub", "Chicxulub", "Chicxulub"),

    ("continental_tnt", "EARTHQUAKE",("#78350f", "#fef3c7", "#451a03", "#a16207", "#451a03"), 15.0, True, True,
     [("tnt", ""), ("pointed_dripstone", ""), ("magma_block", ""), ("ender_pearl", "")], 1,
     "TNT Continental", "Continental Drift", "Dérive Continentale", "Kontinentalverschiebung", "Deriva Continental"),

    ("custom_tnt",      "EXPLODE",  ("#7f1d1d", "#f5f5f4", "#450a0a", "#ef4444", "#450a0a"), 6.0, True, True,
     [("tnt", ""), ("redstone", ""), ("gunpowder", "")], 1,
     "TNT Personalizada", "Custom TNT", "TNT Personnalisée", "TNT Benutzerdefiniert", "TNT Personalizada"),

    ("deimos_tnt",      "METEOR",   ("#1c1917", "#f97316", "#0a0a0a", "#fb923c", "#0a0a0a"), 12.0, True, True,
     [("tnt", ""), ("end_stone", ""), ("fire_charge", ""), ("diamond", "")], 1,
     "TNT Deimos", "Deimos", "Déimos", "Deimos", "Deimos"),

    ("dense_tnt",       "COLOSSAL", ("#292524", "#f5f5f4", "#0a0a0a", "#d6d3d1", "#0a0a0a"), 14.0, True, True,
     [("mega_tnt", "tnts:"), ("iron_block", ""), ("anvil", "")], 1,
     "TNT Densa", "Dense TNT", "TNT Dense", "TNT Dicht", "TNT Densa"),

    ("dust_bowl_tnt",   "SAND",     ("#ca8a04", "#fef9c3", "#713f12", "#fde047", "#713f12"), 8.0, True, True,
     [("tnt", ""), ("sand", ""), ("sandstone", ""), ("dead_bush", "")], 1,
     "TNT Polvo", "Dust Bowl", "Cuve à Poussière", "Staubbecken", "Tigela de Poeira"),

    ("end_gate_tnt",    "ENDER",    ("#4c1d95", "#f5f3ff", "#2e1065", "#d8b4fe", "#2e1065"), 6.0, True, True,
     [("tnt", ""), ("chorus_fruit", ""), ("ender_pearl", ""), ("end_stone", "")], 1,
     "TNT Portal End", "End Gate", "Portail de l'End", "End-Tor", "Portal do End"),

    ("firestorm_tnt",   "STORM",    ("#9a3412", "#fde047", "#450a0a", "#f97316", "#450a0a"), 10.0, True, True,
     [("tnt", ""), ("lightning_rod", ""), ("blaze_powder", ""), ("fire_charge", "")], 1,
     "TNT Tormenta de Fuego", "Firestorm TNT", "TNT Tempête de Feu", "Feuersturm-TNT", "TNT Tempestade de Fogo"),

    ("flower_tnt",      "FUNGI",    ("#16a34a", "#dcfce7", "#14532d", "#4ade80", "#14532d"), 5.0, False, True,
     [("tnt", ""), ("poppy", ""), ("dandelion", ""), ("oxeye_daisy", "")], 1,
     "TNT Floresta", "Flower Forest TNT", "TNT Forêt Florale", "Blumenwald-TNT", "TNT Floresta de Flores"),

    ("ghost_tnt",       "TELEPORT", ("#e2e8f0", "#ffffff", "#64748b", "#f1f5f9", "#64748b"), 4.0, False, True,
     [("tnt", ""), ("phantom_membrane", ""), ("ender_pearl", "")], 1,
     "TNT Fantasma", "Ghost TNT", "TNT Fantôme", "Geister-TNT", "TNT Fantasma"),

    ("giant_tnt",       "COLOSSAL", ("#dc2626", "#fef3c7", "#7f1d1d", "#fca5a1", "#7f1d1d"), 18.0, True, True,
     [("mega_tnt", "tnts:"), ("mega_tnt", "tnts:"), ("tnt", ""), ("tnt", "")], 1,
     "TNT Gigante", "Giant TNT", "TNT Géante", "Riesen-TNT", "TNT Gigante"),

    ("global_tnt",      "NUCLEAR",  ("#0f172a", "#fde047", "#020617", "#facc15", "#020617"), 30.0, True, True,
     [("colossal_tnt", "tnts:"), ("nether_star", ""), ("dragon_breath", "")], 1,
     "TNT Global", "Global Disaster", "Catastrophe Mondiale", "Globale Katastrophe", "Desastre Global"),

    ("gotthard_tnt",    "TUNNEL",   ("#44403c", "#e7e5e4", "#1c1917", "#a8a29e", "#1c1917"), 6.0, True, True,
     [("tnt", ""), ("iron_ingot", ""), ("redstone", ""), ("ender_pearl", "")], 1,
     "TNT Gotthard", "Gotthard Tunnel", "Tunnel du Gotthard", "Gotthard-Tunnel", "Túnel Gotthard"),

    ("heat_death_tnt",  "SUPERNOVA",("#1c1917", "#ef4444", "#0a0a0a", "#dc2626", "#0a0a0a"), 22.0, True, True,
     [("supernova_tnt", "tnts:"), ("nether_star", "")], 1,
     "TNT Muerte Térmica", "Heat Death", "Mort Thermique", "Hitzetod", "Morte Térmica"),

    ("heavens_gate_tnt","PORTAL",   ("#f0f9ff", "#e0f2fe", "#0c4a6e", "#7dd3fc", "#0c4a6e"), 3.0, False, False,
     [("tnt", ""), ("diamond", ""), ("nether_star", "")], 1,
     "TNT Puerta del Cielo", "Heavens Gate", "Porte des Cieux", "Himmelstor", "Portal do Céu"),

    ("hells_gate_tnt",  "INFERNO",  ("#7f1d1d", "#fde047", "#450a0a", "#ef4444", "#450a0a"), 8.0, True, True,
     [("tnt", ""), ("magma_cream", ""), ("blaze_rod", ""), ("netherrack", "")], 1,
     "TNT Puerta del Infierno", "Hells Gate", "Porte des Enfers", "Höllentor", "Portal do Inferno"),

    ("hexahedron_tnt",  "EXPLODE",  ("#1e1b4b", "#c4b5fd", "#1e1b4b", "#8b5cf6", "#1e1b4b"), 7.0, True, True,
     [("tnt", ""), ("amethyst_shard", ""), ("quartz", "")], 1,
     "TNT Hexaedro", "Hexahedron", "Hexaèdre", "Hexaeder", "Hexaedro"),

    ("hungry_tnt",      "BLACKHOLE",("#1c1917", "#a855f7", "#000000", "#d8b4fe", "#000000"), 12.0, True, True,
     [("negra_tnt", "tnts:"), ("soul_sand", ""), ("wither_skeleton_skull", "")], 1,
     "TNT Hambrienta", "Hungry TNT", "TNT Affamée", "Hungrige TNT", "TNT Faminta"),

    ("hyperion_tnt",    "SUPERNOVA",("#4c1d95", "#fef9c3", "#2e1065", "#fcd34d", "#2e1065"), 20.0, True, True,
     [("supernova_tnt", "tnts:"), ("nether_star", ""), ("echo_shard", "")], 1,
     "TNT Hyperion", "Hyperion", "Hyperion", "Hyperion", "Hyperion"),

    ("ice_age_tnt",     "FREEZES",  ("#0284c7", "#e0f2fe", "#0c4a6e", "#bae6fd", "#0c4a6e"), 14.0, True, True,
     [("tnt", ""), ("blue_ice", ""), ("powder_snow_bucket", ""), ("packed_ice", "")], 1,
     "TNT Edad de Hielo", "Ice Age", "Ère Glacière", "Eiszeit", "Era do Gelo"),

    ("icy_tnt",         "ICE",      ("#0ea5e9", "#e0f2fe", "#075985", "#38bdf8", "#075985"), 5.0, True, True,
     [("tnt", ""), ("ice", ""), ("snowball", "")], 1,
     "TNT Helada", "Icy TNT", "TNT Glaciale", "Eisige TNT", "TNT Gelada"),

    ("knockback_tnt",   "WIND",     ("#94a3b8", "#f8fafc", "#334155", "#e2e8f0", "#334155"), 4.0, False, False,
     [("tnt", ""), ("phantom_membrane", ""), ("feather", "")], 1,
     "TNT Empujón", "Knockback TNT", "TNT Poussée", "Rückstoß-TNT", "TNT Empurrão"),

    ("leaping_tnt",     "LAUNCH",   ("#6d28d9", "#f5f3ff", "#2e1065", "#a78bfa", "#2e1065"), 3.0, False, True,
     [("tnt", ""), ("slime_ball", ""), ("rabbit_foot", "")], 1,
     "TNT Saltarina Plus", "Leaping TNT", "TNT Sautillante", "Springende TNT", "TNT Saltitante"),

    ("levitating_tnt",  "GRAVITY",  ("#3b0764", "#e9d5ff", "#1e1b4b", "#c084fc", "#1e1b4b"), 4.0, False, True,
     [("tnt", ""), ("shulker_shell", ""), ("phantom_membrane", "")], 1,
     "TNT Levitación", "Levitating TNT", "TNT Lévitante", "Schwebende TNT", "TNT Levitação"),

    ("lightning_storm_tnt","STORM",  ("#1e3a8a", "#dbeafe", "#172554", "#60a5fa", "#172554"), 12.0, True, True,
     [("tnt", ""), ("lightning_rod", ""), ("lightning_rod", ""), ("redstone", "")], 1,
     "TNT Tormenta de Rayos", "Lightning Storm", "Tempête Électrique", "Gewittersturm", "Tempestade de Raios"),

    ("lucky_god_tnt",   "LUCKY",    ("#be185d", "#fdf4ff", "#500724", "#f0abfc", "#500724"), 8.0, True, True,
     [("luck_tnt", "tnts:"), ("nether_star", ""), ("golden_apple", "")], 1,
     "TNT Dios de la Suerte", "Lucky God", "Dieu de la Chance", "Glücksgott", "Deus da Sorte"),

    ("mankind_tnt",     "NUCLEAR",  ("#1c1917", "#ef4444", "#0a0a0a", "#dc2626", "#0a0a0a"), 16.0, True, True,
     [("tnt", ""), ("totem_of_undying", ""), ("nether_star", "")], 1,
     "TNT Humanidad", "Mankind's Mark", "Marque de l'Humanité", "Menschheitszeichen", "Marca da Humanidade"),

    ("midas_tnt",       "GOLD",     ("#a16207", "#fef9c3", "#422006", "#fde047", "#422006"), 6.0, True, True,
     [("oro_tnt", "tnts:"), ("gold_block", ""), ("golden_apple", "")], 1,
     "TNT Midas", "Midas TNT", "TNT Midas", "Midas-TNT", "TNT Midas"),

    ("mineral_tnt",     "DIAMOND",  ("#0e7490", "#cffafe", "#164e63", "#22d3ee", "#164e63"), 6.0, True, True,
     [("diamante_tnt", "tnts:"), ("diamond_ore", ""), ("emerald_ore", "")], 1,
     "TNT Mineral", "Mineral TNT", "TNT Minérale", "Mineral-TNT", "TNT Mineral"),

    ("mountaintop_tnt", "EARTHQUAKE",("#78350f", "#fef3c7", "#451a03", "#d97706", "#451a03"), 12.0, True, True,
     [("tnt", ""), ("pointed_dripstone", ""), ("stone", ""), ("gravel", "")], 1,
     "TNT Montaña", "Mountaintop Removal", "Démontage de Sommet", "Gipfelentfernung", "Remoção de Montanha"),

    ("particle_tnt",    "NUCLEAR",  ("#3b0764", "#e9d5ff", "#1e1b4b", "#a855f7", "#1e1b4b"), 5.0, False, True,
     [("tnt", ""), ("amethyst_shard", ""), ("glowstone_dust", "")], 1,
     "TNT Partículas", "Particle Physics TNT", "TNT Physique des Particules", "Teilchenphysik-TNT", "TNT Física de Partículas"),

    ("plantation_tnt",  "FUNGI",    ("#16a34a", "#dcfce7", "#14532d", "#22c55e", "#14532d"), 6.0, False, True,
     [("tnt", ""), ("oak_sapling", ""), ("bone_meal", ""), ("water_bucket", "")], 1,
     "TNT Plantación", "Plantation TNT", "TNT Plantation", "Pflanzungs-TNT", "TNT Plantação"),

    ("pompeii_tnt",     "INFERNO",  ("#78350f", "#fef3c7", "#1c1917", "#fde047", "#1c1917"), 12.0, True, True,
     [("tnt", ""), ("magma_cream", ""), ("soul_sand", ""), ("ancient_debris", "")], 1,
     "TNT Pompeya", "Pompeii", "Pompéi", "Pompeji", "Pompeia"),

    ("poseidon_tnt",    "WATER",    ("#075985", "#bae6fd", "#082f49", "#38bdf8", "#082f49"), 10.0, False, True,
     [("agua_tnt", "tnts:"), ("heart_of_the_sea", ""), ("nautilus_shell", "")], 1,
     "TNT Poseidón", "Poseidon's Wave", "Vague de Poséidon", "Poseidons Welle", "Onda de Poseidon"),

    ("present_tnt",     "LUCKY",    ("#dc2626", "#fef3c7", "#7f1d1d", "#fca5a1", "#7f1d1d"), 2.0, False, False,
     [("tnt", ""), ("paper", ""), ("string", ""), ("golden_apple", "")], 1,
     "TNT Regalo", "Present Drop", "Cadeau Tombant", "Geschenkfall", "Presente"),

    ("pulsar_tnt",      "STORM",    ("#1e1b4b", "#fde047", "#0c0a2a", "#facc15", "#0c0a2a"), 8.0, True, True,
     [("tnt", ""), ("lightning_rod", ""), ("echo_shard", ""), ("redstone", "")], 1,
     "TNT Pulsar", "Pulsar TNT", "TNT Pulsar", "Pulsar-TNT", "TNT Pulsar"),

    ("reset_tnt",       "EXPLODE",  ("#e7e5e4", "#ffffff", "#57534e", "#d6d3d1", "#57534e"), 8.0, True, True,
     [("limpia_tnt", "tnts:"), ("tnt", ""), ("tnt", "")], 1,
     "TNT Reset", "Reset TNT", "TNT Réinitialisation", "Reset-TNT", "TNT Reset"),

    ("reversed_tnt",    "EXPLODE",  ("#6d28d9", "#f5f3ff", "#2e1065", "#a78bfa", "#2e1065"), 5.0, True, True,
     [("tnt", ""), ("ender_pearl", ""), ("chorus_fruit", "")], 1,
     "TNT Inversa", "Reversed TNT", "TNT Inversée", "Umgekehrte TNT", "TNT Invertida"),

    ("roulette_tnt",    "LUCKY",    ("#b91c1c", "#fef3c7", "#7f1d1d", "#fca5a1", "#7f1d1d"), 6.0, True, True,
     [("luck_tnt", "tnts:"), ("gunpowder", ""), ("spider_eye", "")], 1,
     "TNT Ruleta", "Russian Roulette", "Russe Roulette", "Russisches Roulette", "Roleta Russa"),

    ("sinkhole_tnt",    "EARTHQUAKE",("#44403c", "#e7e5e4", "#1c1917", "#78716c", "#1c1917"), 8.0, True, True,
     [("tnt", ""), ("gravel", ""), ("sand", ""), ("clay", "")], 1,
     "TNT Sumidero", "Sinkhole TNT", "TNT Effondrement", "Erdloch-TNT", "TNT Sumidouro"),

    ("snowstorm_tnt",   "FREEZES",  ("#e0f2fe", "#ffffff", "#bae6fd", "#f0f9ff", "#bae6fd"), 8.0, False, True,
     [("hielo_tnt", "tnts:"), ("snow_block", ""), ("powder_snow_bucket", "")], 1,
     "TNT Tormenta de Nieve", "Snowstorm TNT", "TNT Blizzard", "Schneesturm-TNT", "TNT Nevasca"),

    ("squaring_tnt",    "EXPLODE",  ("#475569", "#f8fafc", "#1e293b", "#94a3b8", "#1e293b"), 6.0, True, True,
     [("tnt", ""), ("stone_bricks", ""), ("iron_ingot", "")], 1,
     "TNT Cuadrada", "Squaring TNT", "TNT Carrée", "Quadratische TNT", "TNT Quadrada"),

    ("tetrahedron_tnt", "EXPLODE",  ("#7c3aed", "#ede9fe", "#4c1d95", "#a78bfa", "#4c1d95"), 7.0, True, True,
     [("tnt", ""), ("amethyst_shard", ""), ("quartz", ""), ("glowstone_dust", "")], 1,
     "TNT Tetraedro", "Tetrahedron TNT", "TNT Tétraèdre", "Tetraeder-TNT", "TNT Tetraedro"),

    ("revolution_tnt",  "EARTHQUAKE",("#dc2626", "#fef3c7", "#7f1d1d", "#fca5a1", "#7f1d1d"), 10.0, True, True,
     [("tnt", ""), ("redstone", ""), ("iron_ingot", ""), ("gunpowder", "")], 1,
     "TNT Revolución", "The Revolution", "La Révolution", "Die Revolution", "A Revolução"),

    ("tnt_x2000",       "COLOSSAL", ("#7f1d1d", "#fef9c3", "#450a0a", "#ef4444", "#450a0a"), 22.0, True, True,
     [("colossal_tnt", "tnts:"), ("mega_tnt", "tnts:"), ("mega_tnt", "tnts:")], 1,
     "TNT x2000", "TNT x2000", "TNT x2000", "TNT x2000", "TNT x2000"),

    ("toxic_clouds_tnt","TOXIC",    ("#15803d", "#dcfce7", "#064e3b", "#4ade80", "#064e3b"), 10.0, False, True,
     [("toxica_tnt", "tnts:"), ("spider_eye", ""), ("fermented_spider_eye", ""), ("poisonous_potato", "")], 1,
     "TNT Nubes Tóxicas", "Toxic Clouds", "Nuages Toxiques", "Toxische Wolken", "Nuvens Tóxicas"),

    ("tsar_bomba_tnt",  "NUCLEAR",  ("#1c1917", "#ef4444", "#000000", "#dc2626", "#000000"), 35.0, True, True,
     [("global_tnt", "tnts:"), ("nether_star", ""), ("dragon_breath", ""), ("tnt", "")], 1,
     "TNT Tsar Bomba", "Tsar Bomba", "Tsar Bomba", "Zarenbombe", "Bomba Tsar"),

    ("unbreakable_tnt", "EXPLODE",  ("#1c1917", "#a8a29e", "#0a0a0a", "#78716c", "#0a0a0a"), 2.0, False, False,
     [("tnt", ""), ("obsidian", ""), ("crying_obsidian", "")], 1,
     "TNT Irrompible", "Unbreakable TNT", "TNT Incassable", "Unzerstörbare TNT", "TNT Irrompível"),

    ("vicious_tnt",     "INFERNO",  ("#9a3412", "#fef3c7", "#450a0a", "#f97316", "#450a0a"), 8.0, True, True,
     [("tnt", ""), ("blaze_rod", ""), ("magma_cream", ""), ("nether_wart", "")], 1,
     "TNT Viciosa", "Vicious TNT", "TNT Vicieuse", "Bösartige TNT", "TNT Viciosa"),

    ("wither_storm_tnt","NUCLEAR",  ("#1c1917", "#a855f7", "#0a0a0a", "#c084fc", "#0a0a0a"), 18.0, True, True,
     [("tnt", ""), ("wither_skeleton_skull", ""), ("soul_sand", ""), ("nether_star", "")], 1,
     "TNT Tormenta Wither", "Wither Storm", "Tempête Wither", "Wither-Sturm", "Tempestade Wither"),

    ("world_wools_tnt", "CONFETI",  ("#f472b6", "#ffffff", "#be185d", "#fda4af", "#be185d"), 6.0, False, False,
     [("tnt", ""), ("white_wool", ""), ("red_wool", ""), ("blue_wool", ""), ("yellow_wool", "")], 1,
     "TNT Mundial de Lanas", "World of Wools", "Monde de Laines", "Wollwelt", "Mundo de Lãs"),

    # === DOOMSDAY TNTs ===
    ("aether_tnt",      "PORTAL",   ("#f0f9ff", "#e0f2fe", "#0c4a6e", "#38bdf8", "#0c4a6e"), 6.0, True, True,
     [("tnt", ""), ("diamond", ""), ("nether_star", ""), ("chorus_fruit", "")], 1,
     "TNT Éter", "Aether TNT", "TNT Éther", "Äther-TNT", "TNT Éter"),

    ("asteroid_tnt",    "METEOR",   ("#1c1917", "#a8a29e", "#0a0a0a", "#78716c", "#0a0a0a"), 14.0, True, True,
     [("meteorito_tnt", "tnts:"), ("end_stone", ""), ("fire_charge", ""), ("iron_ingot", "")], 1,
     "TNT Cinturón de Asteroides", "Asteroid Belt", "Ceinture d'Astéroïdes", "Asteroidengürtel", "Cinturão de Asteroides"),

    ("atlantis_tnt",    "WATER",    ("#075985", "#bae6fd", "#082f49", "#0ea5e9", "#082f49"), 10.0, False, True,
     [("poseidon_tnt", "tnts:"), ("heart_of_the_sea", ""), ("sponge", ""), ("prismarine_crystals", "")], 1,
     "TNT Atlántida", "Atlantis", "Atlantide", "Atlantis", "Atlântida"),

    ("city_firework_tnt","FIREWORKS",("#831843", "#fce7f3", "#4c0519", "#f472b6", "#4c0519"), 5.0, False, False,
     [("fuegos_tnt", "tnts:"), ("firework_rocket", ""), ("firework_rocket", ""), ("glowstone_dust", "")], 1,
     "TNT Fuegos Ciudad", "City Firework", "Feu d'Artifice Urbain", "Stadtfeuerwerk", "Fogos de Artifício"),

    ("compressed_tnt",  "EXPLODE",  ("#44403c", "#e7e5e4", "#1c1917", "#a8a29e", "#1c1917"), 8.0, True, True,
     [("mega_tnt", "tnts:"), ("iron_block", ""), ("redstone", "")], 1,
     "TNT Comprimida", "Compressed TNT", "TNT Compressée", "Komprimierte TNT", "TNT Comprimida"),

    ("death_ray_tnt",   "NUCLEAR",  ("#7f1d1d", "#fef9c3", "#450a0a", "#ef4444", "#450a0a"), 12.0, True, True,
     [("tnt", ""), ("nether_star", ""), ("echo_shard", ""), ("ender_pearl", "")], 1,
     "TNT Rayo de la Muerte", "Death Ray", "Rayon de la Mort", "Todesstrahl", "Raio da Morte"),

    ("disintegrating_tnt","EXPLODE", ("#1c1917", "#a8a29e", "#0a0a0a", "#d6d3d1", "#0a0a0a"), 10.0, True, True,
     [("tnt", ""), ("magma_cream", ""), ("blaze_powder", ""), ("fire_charge", "")], 1,
     "TNT Desintegradora", "Disintegrating TNT", "TNT Désintégratrice", "Desintegrations-TNT", "TNT Desintegradora"),

    ("doomsday_tnt",    "NUCLEAR",  ("#7f1d1d", "#fef3c7", "#450a0a", "#dc2626", "#450a0a"), 25.0, True, True,
     [("global_tnt", "tnts:"), ("nether_star", ""), ("dragon_breath", "")], 1,
     "TNT Juicio Final", "Doomsday", "Jugement Dernier", "Weltuntergang", "Juízo Final"),

    ("evil_tnt",        "INFERNO",  ("#7f1d1d", "#fde047", "#450a0a", "#ef4444", "#450a0a"), 10.0, True, True,
     [("tnt", ""), ("soul_sand", ""), ("wither_skeleton_skull", ""), ("nether_wart", "")], 1,
     "TNT Maligna", "Evil TNT", "TNT Maléfique", "Böse TNT", "TNT Maligna"),

    ("extinction_tnt",  "NUCLEAR",  ("#1c1917", "#ef4444", "#000000", "#dc2626", "#000000"), 28.0, True, True,
     [("tsar_bomba_tnt", "tnts:"), ("nether_star", ""), ("dragon_breath", "")], 1,
     "TNT Extinción", "Extinction", "Extinction", "Aussterben", "Extinção"),

    ("fiery_hell_tnt",  "INFERNO",  ("#9a3412", "#fde047", "#450a0a", "#f97316", "#450a0a"), 12.0, True, True,
     [("inferno_tnt", "tnts:"), ("magma_cream", ""), ("blaze_rod", ""), ("netherrack", "")], 1,
     "TNT Infierno Ígneo", "Fiery Hell", "Enfer de Feu", "Feurige Hölle", "Inferno Ígneo"),

    ("flak_tnt",        "STORM",    ("#44403c", "#fde047", "#1c1917", "#facc15", "#1c1917"), 8.0, True, True,
     [("tnt", ""), ("iron_ingot", ""), ("gunpowder", ""), ("fire_charge", "")], 1,
     "TNT Flak", "Flak TNT", "TNT Flak", "Flak-TNT", "TNT Flak"),

    ("flat_earth_tnt",  "EARTHQUAKE",("#78350f", "#fef3c7", "#451a03", "#d97706", "#451a03"), 16.0, True, True,
     [("continental_tnt", "tnts:"), ("stone", ""), ("dirt", ""), ("gravel", "")], 1,
     "TNT Tierra Plana", "Flat Earth", "Terre Plate", "Flache Erde", "Terra Plana"),

    ("fluorine_tnt",    "TOXIC",    ("#65a30d", "#d9f99d", "#365314", "#a3e635", "#365314"), 6.0, False, True,
     [("acidic_tnt", "tnts:"), ("glowstone_dust", ""), ("spider_eye", "")], 1,
     "TNT Flúor", "Fluorine TNT", "TNT Fluor", "Fluor-TNT", "TNT Flúor"),

    ("flying_tnt",      "LAUNCH",   ("#94a3b8", "#f8fafc", "#334155", "#e2e8f0", "#334155"), 5.0, True, True,
     [("tnt", ""), ("elytra", ""), ("firework_rocket", "")], 1,
     "TNT Voladora", "Flying TNT", "TNT Volante", "Fliegende TNT", "TNT Voadora"),

    ("grande_finale_tnt","FIREWORKS",("#be185d", "#fdf4ff", "#500724", "#f0abfc", "#500724"), 10.0, False, False,
     [("fuegos_tnt", "tnts:"), ("firework_rocket", ""), ("firework_rocket", ""), ("nether_star", "")], 1,
     "TNT Gran Final", "Grande Finale", "Grand Final", "Großes Finale", "Grande Final"),

    ("heat_wave_tnt",   "SOLAR",    ("#9a3412", "#fef08a", "#450a0a", "#fbbf24", "#450a0a"), 10.0, True, True,
     [("solar_tnt", "tnts:"), ("blaze_powder", ""), ("magma_cream", "")], 1,
     "TNT Ola de Calor", "Heat Wave", "Canicule", "Hitzewelle", "Onda de Calor"),

    ("helix_tnt",       "NUCLEAR",  ("#4c1d95", "#fef9c3", "#2e1065", "#a855f7", "#2e1065"), 10.0, True, True,
     [("tnt", ""), ("amethyst_shard", ""), ("glowstone_dust", ""), ("echo_shard", "")], 1,
     "TNT Hélix", "Helix TNT", "TNT Hélice", "Helix-TNT", "TNT Hélice"),

    ("hydrogen_bomb_tnt","NUCLEAR",  ("#1c1917", "#fef3c7", "#000000", "#fde047", "#000000"), 40.0, True, True,
     [("tsar_bomba_tnt", "tnts:"), ("nether_star", ""), ("dragon_breath", ""), ("nether_star", "")], 1,
     "TNT Bomba de Hidrógeno", "Hydrogen Bomb", "Bombe Hydrogène", "Wasserstoffbombe", "Bomba de Hidrogênio"),

    ("illuminati_tnt",  "LIGHTNING", ("#1e1b4b", "#fde047", "#0c0a2a", "#facc15", "#0c0a2a"), 8.0, True, True,
     [("tnt", ""), ("lightning_rod", ""), ("ender_eye", ""), ("redstone", "")], 1,
     "TNT Illuminati", "Illuminati TNT", "TNT Illuminati", "Illuminati-TNT", "TNT Illuminati"),

    ("jumping_tnt",     "LAUNCH",   ("#6d28d9", "#f5f3ff", "#2e1065", "#a78bfa", "#2e1065"), 4.0, False, True,
     [("leaping_tnt", "tnts:"), ("slime_ball", ""), ("feather", "")], 1,
     "TNT Saltarina Mega", "Jumping TNT", "TNT Bondissante", "Springende TNT", "TNT Saltitante"),

    ("kola_borehole_tnt","EARTHQUAKE",("#44403c", "#e7e5e4", "#1c1917", "#78716c", "#1c1917"), 10.0, True, True,
     [("tnt", ""), ("pointed_dripstone", ""), ("deepslate", ""), ("lava_bucket", "")], 1,
     "TNT Pozo Kola", "Kola Borehole", "Forage de Kola", "Kola-Bohrung", "Poço Kola"),

    ("lucky_doomsday_tnt","LUCKY",   ("#be185d", "#fef3c7", "#500724", "#f0abfc", "#500724"), 15.0, True, True,
     [("lucky_god_tnt", "tnts:"), ("nether_star", ""), ("golden_apple", "")], 1,
     "TNT Juicio con Suerte", "Lucky Doomsday", "Jugement Chanceux", "Glücklicher Weltuntergang", "Juízo Sortudo"),

    ("meteor_storm_tnt","METEOR",    ("#1c1917", "#f97316", "#0a0a0a", "#ef4444", "#0a0a0a"), 16.0, True, True,
     [("meteorito_tnt", "tnts:"), ("fire_charge", ""), ("end_stone", ""), ("magma_block", "")], 1,
     "TNT Tormenta de Meteoritos", "Meteor Storm", "Tempête de Météorites", "Meteorensturm", "Tempestade de Meteoritos"),

    ("nether_tnt",      "INFERNO",  ("#7f1d1d", "#fde047", "#450a0a", "#ef4444", "#450a0a"), 10.0, True, True,
     [("tnt", ""), ("netherrack", ""), ("magma_cream", ""), ("soul_sand", "")], 1,
     "TNT Nether", "Nether TNT", "TNT Nether", "Nether-TNT", "TNT Nether"),

    ("phobos_tnt",      "METEOR",   ("#1c1917", "#f97316", "#0a0a0a", "#fb923c", "#0a0a0a"), 10.0, True, True,
     [("deimos_tnt", "tnts:"), ("end_stone", ""), ("fire_charge", "")], 1,
     "TNT Fobos", "Phobos", "Phobos", "Phobos", "Phobos"),

    ("solar_eruption_tnt","SOLAR",   ("#9a3412", "#fef9c3", "#450a0a", "#fde047", "#450a0a"), 14.0, True, True,
     [("solar_tnt", "tnts:"), ("blaze_powder", ""), ("glowstone", ""), ("nether_star", "")], 1,
     "TNT Erupción Solar", "Solar Eruption", "Éruption Solaire", "Sonneneruption", "Erupção Solar"),

    ("stone_cold_tnt",  "FREEZES",  ("#44403c", "#e7e5e4", "#1c1917", "#a8a29e", "#1c1917"), 8.0, True, True,
     [("hielo_tnt", "tnts:"), ("stone", ""), ("blue_ice", ""), ("packed_ice", "")], 1,
     "TNT Piedra Fría", "Stone Cold", "Pierre Glacée", "Eisiger Stein", "Pedra Fria"),

    ("structure_tnt",   "HOUSE",    ("#7c4a03", "#fef3c7", "#451a03", "#d97706", "#451a03"), 3.0, False, False,
     [("casa_tnt", "tnts:"), ("oak_log", ""), ("oak_planks", "")], 1,
     "TNT Estructura", "Structure TNT", "TNT Structure", "Struktur-TNT", "TNT Estrutura"),

    ("tnt_rain_tnt",    "STORM",    ("#1e3a8a", "#dbeafe", "#172554", "#60a5fa", "#172554"), 8.0, True, True,
     [("tormenta_tnt", "tnts:"), ("tnt", ""), ("tnt", ""), ("redstone", "")], 1,
     "TNT Lluvia de TNT", "TNT Rain", "Pluie de TNT", "TNT-Regen", "Chuva de TNT"),

    ("tnt_x10000",      "COLOSSAL", ("#7f1d1d", "#fef9c3", "#450a0a", "#ef4444", "#450a0a"), 50.0, True, True,
     [("tnt_x2000", "tnts:"), ("tnt_x2000", "tnts:"), ("tnt_x2000", "tnts:"), ("tnt_x2000", "tnts:"), ("tnt_x2000", "tnts:")], 1,
     "TNT x10000", "TNTx10000", "TNTx10000", "TNTx10000", "TNTx10000"),

    ("vredefort_tnt",   "EARTHQUAKE",("#78350f", "#fef3c7", "#1c1917", "#fde047", "#1c1917"), 20.0, True, True,
     [("chicxulub_tnt", "tnts:"), ("pointed_dripstone", ""), ("magma_block", ""), ("ancient_debris", "")], 1,
     "TNT Vredefort", "Vredefort", "Vredefort", "Vredefort", "Vredefort"),

    ("wasteland_tnt",   "TOXIC",    ("#78350f", "#fef3c7", "#451a03", "#a16207", "#451a03"), 10.0, True, True,
     [("toxica_tnt", "tnts:"), ("dead_bush", ""), ("soul_sand", ""), ("magma_cream", "")], 1,
     "TNT Páramo", "Wasteland TNT", "TNT Désolation", "Ödland-TNT", "TNT Ermo"),

    ("winter_tnt",      "FREEZES",  ("#e0f2fe", "#ffffff", "#bae6fd", "#f0f9ff", "#bae6fd"), 10.0, False, True,
     [("snowstorm_tnt", "tnts:"), ("snow_block", ""), ("blue_ice", ""), ("packed_ice", "")], 1,
     "TNT Invierno", "Winter TNT", "TNT Hiver", "Winter-TNT", "TNT Inverno"),

    # === DINAMITAS ===
    ("dynamite",         "EXPLODE",  ("#dc2626", "#fef3c7", "#7f1d1d", "#fca5a1", "#7f1d1d"), 3.0, True, True,
     [("gunpowder", ""), ("string", ""), ("paper", "")], 3,
     "Dinamita", "Dynamite", "Dynamite", "Dynamit", "Dinamite"),

    ("accelerating_dynamite","EXPLODE",("#ef4444", "#fef3c7", "#7f1d1d", "#fca5a1", "#7f1d1d"), 4.0, True, True,
     [("dynamite", "tnts:"), ("redstone", ""), ("gunpowder", "")], 2,
     "Dinamita Acelerante", "Accelerating Dynamite", "Dynamite Accélérante", "Beschleunigende Dynamit", "Dinamita Acelerante"),

    ("animal_dynamite",  "HEAL",     ("#92400e", "#fef3c7", "#451a03", "#d97706", "#451a03"), 3.0, False, False,
     [("dynamite", "tnts:"), ("wheat", ""), ("wheat_seeds", "")], 2,
     "Dinamita Animal", "Animal Dynamite", "Dynamite Animale", "Tier-Dynamit", "Dinamita Animal"),

    ("arrow_dynamite",   "LAUNCH",   ("#44403c", "#e7e5e4", "#1c1917", "#a8a29e", "#1c1917"), 4.0, True, True,
     [("dynamite", "tnts:"), ("arrow", ""), ("flint", "")], 2,
     "Dinamita de Flecha", "Arrow Dynamite", "Dynamite Flèche", "Pfeil-Dynamit", "Dinamita de Flecha"),

    ("big_dynamite",     "EXPLODE",  ("#b91c1c", "#fef3c7", "#7f1d1d", "#fca5a1", "#7f1d1d"), 6.0, True, True,
     [("dynamite", "tnts:"), ("dynamite", "tnts:"), ("dynamite", "tnts:"), ("gunpowder", "")], 1,
     "Dinamita Grande", "Big Dynamite", "Grosse Dynamite", "Große Dynamit", "Dinamita Grande"),

    ("bouncing_dynamite","LAUNCH",   ("#6d28d9", "#f5f3ff", "#2e1065", "#a78bfa", "#2e1065"), 3.0, False, True,
     [("dynamite", "tnts:"), ("slime_ball", ""), ("gunpowder", "")], 2,
     "Dinamita Rebote", "Bouncing Dynamite", "Dynamite Rebondissante", "Hüpfende Dynamit", "Dinamita Rebote"),

    ("chemical_dynamite","TOXIC",    ("#65a30d", "#d9f99d", "#365314", "#84cc16", "#365314"), 4.0, False, True,
     [("dynamite", "tnts:"), ("spider_eye", ""), ("fermented_spider_eye", "")], 2,
     "Dinamita Química", "Chemical Dynamite", "Dynamite Chimique", "Chemische Dynamit", "Dinamita Química"),

    ("christmas_dynamite","CONFETI",  ("#dc2626", "#fef3c7", "#7f1d1d", "#fca5a1", "#7f1d1d"), 3.0, False, False,
     [("dynamite", "tnts:"), ("red_dye", ""), ("green_dye", ""), ("gold_nugget", "")], 3,
     "Dinamita Navideña", "Christmas Dynamite", "Dynamite de Noël", "Weihnachts-Dynamit", "Dinamita Natal"),

    ("cluster_dynamite", "EXPLODE",  ("#b91c1c", "#fef3c7", "#7f1d1d", "#fca5a1", "#7f1d1d"), 5.0, True, True,
     [("dynamite", "tnts:"), ("dynamite", "tnts:"), ("gunpowder", ""), ("string", "")], 2,
     "Dinamita Racimo", "Cluster Dynamite", "Dynamite en Grappe", "Cluster-Dynamit", "Dinamita Cluster"),

    ("compact_dynamite", "EXPLODE",  ("#44403c", "#e7e5e4", "#1c1917", "#a8a29e", "#1c1917"), 4.0, True, True,
     [("dynamite", "tnts:"), ("iron_nugget", ""), ("gunpowder", "")], 3,
     "Dinamita Compacta", "Compact Dynamite", "Dynamite Compacte", "Kompakte Dynamit", "Dinamita Compacta"),

    ("cubic_dynamite",   "EXPLODE",  ("#1e1b4b", "#c4b5fd", "#1e1b4b", "#8b5cf6", "#1e1b4b"), 5.0, True, True,
     [("dynamite", "tnts:"), ("amethyst_shard", ""), ("quartz", "")], 2,
     "Dinamita Cúbica", "Cubic Dynamite", "Dynamite Cubique", "Würfel-Dynamit", "Dinamita Cúbica"),

    ("digging_dynamite", "TUNNEL",   ("#78350f", "#fef3c7", "#451a03", "#d97706", "#451a03"), 4.0, True, False,
     [("dynamite", "tnts:"), ("iron_pickaxe", ""), ("redstone", "")], 2,
     "Dinamita de Excavación", "Digging Dynamite", "Dynamite de Fouille", "Bau-Dynamit", "Dinamita de Escavação"),

    ("dripstone_dynamite","TUNNEL",  ("#44403c", "#e7e5e4", "#1c1917", "#78716c", "#1c1917"), 4.0, True, True,
     [("dynamite", "tnts:"), ("pointed_dripstone", ""), ("stone", "")], 2,
     "Dinamita Dripstone", "Dripstone Dynamite", "Dynamite Stalactite", "Tropfstein-Dynamit", "Dinamita Dripstone"),

    ("dynamite_firework","FIREWORKS", ("#831843", "#fce7f3", "#4c0519", "#f472b6", "#4c0519"), 3.0, False, False,
     [("dynamite", "tnts:"), ("firework_rocket", ""), ("glowstone_dust", "")], 3,
     "Dinamita Fuego Artificial", "Dynamite Firework", "Dynamite Feu d'Artifice", "Feuerwerk-Dynamit", "Dinamita Fogos"),

    ("dynamite_x5",     "EXPLODE",  ("#dc2626", "#fef3c7", "#7f1d1d", "#fca5a1", "#7f1d1d"), 4.0, True, True,
     [("dynamite", "tnts:"), ("dynamite", "tnts:"), ("dynamite", "tnts:"), ("dynamite", "tnts:"), ("dynamite", "tnts:")], 1,
     "Dinamita x5", "Dynamite x5", "Dynamite x5", "Dynamit x5", "Dinamita x5"),

    ("dynamite_x20",    "EXPLODE",  ("#b91c1c", "#fef3c7", "#7f1d1d", "#fca5a1", "#7f1d1d"), 8.0, True, True,
     [("dynamite_x5", "tnts:"), ("dynamite_x5", "tnts:"), ("dynamite_x5", "tnts:"), ("dynamite_x5", "tnts:")], 1,
     "Dinamita x20", "Dynamite x20", "Dynamite x20", "Dynamit x20", "Dinamita x20"),

    ("dynamite_x100",   "COLOSSAL", ("#7f1d1d", "#fef9c3", "#450a0a", "#ef4444", "#450a0a"), 15.0, True, True,
     [("dynamite_x20", "tnts:"), ("dynamite_x20", "tnts:"), ("dynamite_x20", "tnts:"), ("dynamite_x20", "tnts:"), ("dynamite_x20", "tnts:")], 1,
     "Dinamita x100", "Dynamite x100", "Dynamite x100", "Dynamit x100", "Dinamita x100"),

    ("dynamite_x500",   "NUCLEAR",  ("#1c1917", "#ef4444", "#000000", "#dc2626", "#000000"), 25.0, True, True,
     [("dynamite_x100", "tnts:"), ("dynamite_x100", "tnts:"), ("dynamite_x100", "tnts:"), ("dynamite_x100", "tnts:"), ("dynamite_x100", "tnts:")], 1,
     "Dinamita x500", "Dynamite x500", "Dynamite x500", "Dynamit x500", "Dinamita x500"),

    ("end_dynamite",     "ENDER",    ("#4c1d95", "#f5f3ff", "#2e1065", "#d8b4fe", "#2e1065"), 4.0, True, True,
     [("dynamite", "tnts:"), ("chorus_fruit", ""), ("ender_pearl", "")], 2,
     "Dinamita del End", "End Dynamite", "Dynamite de l'End", "End-Dynamit", "Dinamita do End"),

    ("ender_dynamite",   "TELEPORT", ("#3b0764", "#e9d5ff", "#1e1b4b", "#c084fc", "#1e1b4b"), 3.0, False, True,
     [("dynamite", "tnts:"), ("ender_pearl", ""), ("chorus_fruit", "")], 3,
     "Dinamita Ender", "Ender Dynamite", "Dynamite Ender", "Ender-Dynamit", "Dinamita Ender"),

    ("erupting_dynamite","INFERNO",  ("#9a3412", "#fde047", "#450a0a", "#f97316", "#450a0a"), 5.0, True, True,
     [("dynamite", "tnts:"), ("magma_cream", ""), ("blaze_powder", "")], 2,
     "Dinamita Eruptiva", "Erupting Dynamite", "Dynamite Éruptive", "Ausbruchs-Dynamit", "Dinamita Eruptiva"),

    ("farming_dynamite", "FUNGI",    ("#16a34a", "#dcfce7", "#14532d", "#4ade80", "#14532d"), 3.0, False, False,
     [("dynamite", "tnts:"), ("wheat_seeds", ""), ("bone_meal", "")], 3,
     "Dinamita Agrícola", "Farming Dynamite", "Dynamite Agricole", "Landwirtschafts-Dynamit", "Dinamita Agrícola"),

    ("fire_dynamite",    "FIRE",     ("#9a3412", "#fde047", "#450a0a", "#f97316", "#450a0a"), 4.0, True, True,
     [("dynamite", "tnts:"), ("flint_and_steel", ""), ("fire_charge", "")], 2,
     "Dinamita de Fuego", "Fire Dynamite", "Dynamite de Feu", "Feuer-Dynamit", "Dinamita de Fogo"),

    ("flat_dynamite",    "EARTHQUAKE",("#44403c", "#e7e5e4", "#1c1917", "#78716c", "#1c1917"), 6.0, True, True,
     [("dynamite", "tnts:"), ("stone", ""), ("iron_ingot", "")], 2,
     "Dinamita Plana", "Flat Dynamite", "Dynamite Plate", "Flache Dynamit", "Dinamita Plana"),

    ("floating_dynamite","LAUNCH",   ("#0369a1", "#bae6fd", "#082f49", "#38bdf8", "#082f49"), 3.0, False, True,
     [("dynamite", "tnts:"), ("phantom_membrane", ""), ("feather", "")], 3,
     "Dinamita Flotante", "Floating Dynamite", "Dynamite Flottante", "Schwebende Dynamit", "Dinamita Flutuante"),

    ("floating_island_dynamite","HOUSE",("#16a34a", "#dcfce7", "#14532d", "#4ade80", "#14532d"), 5.0, False, False,
     [("dynamite", "tnts:"), ("dirt", ""), ("grass_block", ""), ("oak_log", "")], 2,
     "Dinamita Isla Flotante", "Floating Island Dynamite", "Dynamite Île Flottante", "Schwebende-Insel-Dynamit", "Dinamita Ilha Flutuante"),

    ("freeze_dynamite",  "ICE",      ("#0ea5e9", "#e0f2fe", "#075985", "#38bdf8", "#075985"), 3.0, False, True,
     [("dynamite", "tnts:"), ("ice", ""), ("snowball", "")], 3,
     "Dinamita Congelante", "Freeze Dynamite", "Dynamite Congélation", "Einfrier-Dynamit", "Dinamita Congelante"),

    ("gravity_dynamite", "GRAVITY",  ("#3b0764", "#e9d5ff", "#1e1b4b", "#c084fc", "#1e1b4b"), 4.0, False, True,
     [("dynamite", "tnts:"), ("anvil", ""), ("iron_nugget", "")], 2,
     "Dinamita Gravitatoria", "Gravity Dynamite", "Dynamite Gravitationnelle", "Gravitations-Dynamit", "Dinamita Gravitacional"),

    ("grove_dynamite",   "FUNGI",    ("#16a34a", "#dcfce7", "#14532d", "#22c55e", "#14532d"), 3.0, False, False,
     [("dynamite", "tnts:"), ("oak_sapling", ""), ("bone_meal", "")], 3,
     "Dinamita Bosque", "Grove Dynamite", "Dynamite Bosquet", "Wald-Dynamit", "Dinamita Bosque"),

    ("hellfire_dynamite","INFERNO",  ("#7f1d1d", "#fde047", "#450a0a", "#ef4444", "#450a0a"), 5.0, True, True,
     [("dynamite", "tnts:"), ("blaze_rod", ""), ("magma_cream", ""), ("soul_sand", "")], 2,
     "Dinamita Infernal", "Hellfire Dynamite", "Dynamite Infernale", "Höllenfeuer-Dynamit", "Dinamita Infernal"),

    ("homing_dynamite",  "TELEPORT", ("#3b0764", "#e9d5ff", "#1e1b4b", "#c084fc", "#1e1b4b"), 4.0, True, True,
     [("dynamite", "tnts:"), ("ender_pearl", ""), ("blaze_powder", ""), ("redstone", "")], 2,
     "Dinamita Teledirigida", "Homing Dynamite", "Dynamite Autoguidée", "Lenk-Dynamit", "Dinamita Teleguiada"),

    ("honey_dynamite",   "HONEY",    ("#b45309", "#fef3c7", "#78350f", "#fbbf24", "#78350f"), 3.0, False, True,
     [("dynamite", "tnts:"), ("honeycomb", ""), ("slime_ball", "")], 3,
     "Dinamita de Miel", "Honey Dynamite", "Dynamite Miel", "Honig-Dynamit", "Dinamita de Mel"),

    ("ice_meteor_dynamite","ICE",    ("#0284c7", "#e0f2fe", "#0c4a6e", "#bae6fd", "#0c4a6e"), 5.0, True, True,
     [("dynamite", "tnts:"), ("blue_ice", ""), ("end_stone", ""), ("snowball", "")], 2,
     "Dinamita Meteoro de Hielo", "Ice Meteor Dynamite", "Dynamite Météore Glacial", "Eis-Meteor-Dynamit", "Dinamita Meteoro de Gelo"),

    ("igniter_dynamite", "FIRE",     ("#dc2626", "#fde047", "#7f1d1d", "#fca5a1", "#7f1d1d"), 3.0, True, True,
     [("dynamite", "tnts:"), ("flint_and_steel", ""), ("fire_charge", "")], 3,
     "Dinamita Encendedora", "Igniter Dynamite", "Dynamite Allumeuse", "Zünd-Dynamit", "Dinamita Acendedora"),

    ("lava_ocean_dynamite","LAVA",   ("#c2410c", "#fef3c7", "#431407", "#fde047", "#431407"), 6.0, True, True,
     [("dynamite", "tnts:"), ("lava_bucket", ""), ("magma_block", ""), ("netherrack", "")], 2,
     "Dinamita Océano de Lava", "Lava Ocean Dynamite", "Dynamite Océan de Lave", "Lava-Ozean-Dynamit", "Dinamita Oceano de Lava"),

    ("lightning_dynamite","STORM",   ("#1e3a8a", "#dbeafe", "#172554", "#60a5fa", "#172554"), 4.0, True, True,
     [("dynamite", "tnts:"), ("lightning_rod", ""), ("redstone", "")], 2,
     "Dinamita de Rayos", "Lightning Dynamite", "Dynamite Éclair", "Blitz-Dynamit", "Dinamita de Raios"),

    ("lucky_dynamite",   "LUCKY",    ("#be185d", "#fdf4ff", "#500724", "#f0abfc", "#500724"), 4.0, True, True,
     [("dynamite", "tnts:"), ("spider_eye", ""), ("golden_apple", "")], 2,
     "Dinamita de la Suerte", "Lucky Dynamite", "Dynamite Chance", "Glücks-Dynamit", "Dinamita da Sorte"),

    ("lush_dynamite",    "FUNGI",    ("#16a34a", "#dcfce7", "#14532d", "#86efac", "#14532d"), 3.0, False, False,
     [("dynamite", "tnts:"), ("azalea", ""), ("moss_block", ""), ("bone_meal", "")], 3,
     "Dinamita Exuberante", "Lush Dynamite", "Dynamite Luxuriante", "Üppige Dynamit", "Dinamita Exuberante"),

    ("meteor_dynamite",  "METEOR",   ("#1c1917", "#f97316", "#0a0a0a", "#fb923c", "#0a0a0a"), 5.0, True, True,
     [("dynamite", "tnts:"), ("end_stone", ""), ("fire_charge", "")], 2,
     "Dinamita Meteoro", "Meteor Dynamite", "Dynamite Météore", "Meteor-Dynamit", "Dinamita Meteoro"),

    ("miningflat_dynamite","TUNNEL",  ("#44403c", "#e7e5e4", "#1c1917", "#a8a29e", "#1c1917"), 5.0, True, False,
     [("dynamite", "tnts:"), ("iron_pickaxe", ""), ("iron_ingot", ""), ("redstone", "")], 2,
     "Dinamita Minera Plana", "Miningflat Dynamite", "Dynamite Miniage Plate", "Bergbau-Flach-Dynamit", "Dinamita Mineração"),

    ("multiplying_dynamite","EXPLODE",("#b91c1c", "#fef3c7", "#7f1d1d", "#fca5a1", "#7f1d1d"), 4.0, True, True,
     [("dynamite", "tnts:"), ("slime_ball", ""), ("gunpowder", ""), ("redstone", "")], 2,
     "Dinamita Multiplicante", "Multiplying Dynamite", "Dynamite Multiplicatrice", "Multiplizierende Dynamit", "Dinamita Multiplicadora"),

    ("nether_grove_dynamite","INFERNO",("#7f1d1d", "#fde047", "#450a0a", "#ef4444", "#450a0a"), 4.0, False, True,
     [("dynamite", "tnts:"), ("nether_wart", ""), ("soul_sand", ""), ("warped_fungus", "")], 2,
     "Dinamita Bosque Nether", "Nether Grove Dynamite", "Dynamite Bosquet du Nether", "Netherwald-Dynamit", "Dinamita Bosque Nether"),

    ("nuclear_dynamite","NUCLEAR",   ("#292524", "#facc15", "#0a0a0a", "#86efac", "#0a0a0a"), 10.0, True, True,
     [("dynamite", "tnts:"), ("gunpowder", ""), ("blaze_powder", ""), ("redstone", ""), ("iron_ingot", "")], 1,
     "Dinamita Nuclear", "Nuclear Dynamite", "Dynamite Nucléaire", "Kernwaffen-Dynamit", "Dinamita Nuclear"),

    ("nuclear_waste_dynamite","TOXIC",("#14532d", "#dcfce7", "#052e16", "#84cc16", "#052e16"), 6.0, False, True,
     [("nuclear_dynamite", "tnts:"), ("fermented_spider_eye", ""), ("poisonous_potato", "")], 2,
     "Dinamita Residuo Nuclear", "Nuclear Waste Dynamite", "Dynamite Déchets Nucléaires", "Nuklearabfall-Dynamit", "Dinamita Resíduo Nuclear"),

    ("ocean_dynamite",   "WATER",    ("#075985", "#bae6fd", "#082f49", "#38bdf8", "#082f49"), 4.0, False, True,
     [("dynamite", "tnts:"), ("water_bucket", ""), ("kelp", ""), ("prismarine_crystals", "")], 2,
     "Dinamita Oceánica", "Ocean Dynamite", "Dynamite Océanique", "Ozean-Dynamit", "Dinamita Oceânica"),

    ("physics_dynamite","GRAVITY",   ("#3b0764", "#e9d5ff", "#1e1b4b", "#a855f7", "#1e1b4b"), 5.0, True, True,
     [("dynamite", "tnts:"), ("anvil", ""), ("echo_shard", ""), ("amethyst_shard", "")], 2,
     "Dinamita de Física", "Physics Dynamite", "Dynamite Physique", "Physik-Dynamit", "Dinamita de Física"),

    ("picky_dynamite",   "EXPLODE",  ("#44403c", "#e7e5e4", "#1c1917", "#a8a29e", "#1c1917"), 3.0, True, True,
     [("dynamite", "tnts:"), ("iron_pickaxe", ""), ("redstone", "")], 3,
     "Dinamita Selectiva", "Picky Dynamite", "Dynamite Sélective", "Auswahl-Dynamit", "Dinamita Seletiva"),

    ("prism_dynamite",   "LIGHTNING",("#1e3a8a", "#dbeafe", "#172554", "#60a5fa", "#172554"), 4.0, True, True,
     [("dynamite", "tnts:"), ("prismarine_crystals", ""), ("lightning_rod", "")], 2,
     "Dinamita Prisma", "Prism Dynamite", "Dynamite Prisme", "Prisma-Dynamit", "Dinamita Prisma"),

    ("pulse_dynamite",   "STORM",    ("#1e1b4b", "#fde047", "#0c0a2a", "#facc15", "#0c0a2a"), 4.0, True, True,
     [("dynamite", "tnts:"), ("echo_shard", ""), ("redstone", ""), ("glowstone_dust", "")], 2,
     "Dinamita Pulso", "Pulse Dynamite", "Dynamite Impulsion", "Puls-Dynamit", "Dinamita Pulso"),

    ("rainbow_dynamite","CONFETI",   ("#db2777", "#ffffff", "#4c0519", "#f472b6", "#4c0519"), 3.0, False, False,
     [("dynamite", "tnts:"), ("red_dye", ""), ("yellow_dye", ""), ("blue_dye", ""), ("green_dye", "")], 3,
     "Dinamita Arcoíris", "Rainbow Dynamite", "Dynamite Arc-en-ciel", "Regenbogen-Dynamit", "Dinamita Arco-íris"),

    ("random_dynamite",  "LUCKY",    ("#be185d", "#fdf4ff", "#500724", "#f0abfc", "#500724"), 4.0, True, True,
     [("dynamite", "tnts:"), ("spider_eye", ""), ("ender_pearl", "")], 2,
     "Dinamita Aleatoria", "Random Dynamite", "Dynamite Aléatoire", "Zufällige Dynamit", "Dinamita Aleatória"),

    ("reaction_dynamite","EXPLODE",  ("#dc2626", "#fef3c7", "#7f1d1d", "#fca5a1", "#7f1d1d"), 6.0, True, True,
     [("dynamite", "tnts:"), ("dynamite", "tnts:"), ("gunpowder", ""), ("redstone", ""), ("redstone", "")], 1,
     "Dinamita en Reacción", "Reaction Dynamite", "Dynamite en Réaction", "Reaktions-Dynamit", "Dinamita em Reação"),

    ("ring_dynamite",    "EXPLODE",  ("#6d28d9", "#f5f3ff", "#2e1065", "#a78bfa", "#2e1065"), 5.0, True, True,
     [("dynamite", "tnts:"), ("amethyst_shard", ""), ("quartz", ""), ("glowstone_dust", "")], 2,
     "Dinamita Anillo", "Ring Dynamite", "Dynamite Anneau", "Ring-Dynamit", "Dinamita Anel"),

    ("roulette_dynamite","LUCKY",    ("#b91c1c", "#fef3c7", "#7f1d1d", "#fca5a1", "#7f1d1d"), 5.0, True, True,
     [("dynamite", "tnts:"), ("spider_eye", ""), ("gunpowder", ""), ("redstone", "")], 2,
     "Dinamita Ruleta", "Roulette Dynamite", "Dynamite Roulette", "Roulette-Dynamit", "Dinamita Roleta"),

    ("sculk_dynamite",   "ENDER",    ("#042f2e", "#ccfbf1", "#0f172a", "#5eead4", "#0f172a"), 4.0, True, True,
     [("dynamite", "tnts:"), ("sculk_sensor", ""), ("echo_shard", ""), ("ender_pearl", "")], 2,
     "Dinamita Sculk", "Sculk Dynamite", "Dynamite Sculk", "Sculk-Dynamit", "Dinamita Sculk"),

    ("sensor_dynamite",  "TRAP",     ("#44403c", "#e7e5e4", "#1c1917", "#a8a29e", "#1c1917"), 4.0, True, True,
     [("dynamite", "tnts:"), ("sculk_sensor", ""), ("redstone", ""), ("string", "")], 2,
     "Dinamita Sensor", "Sensor Dynamite", "Dynamite Capteur", "Sensor-Dynamit", "Dinamita Sensor"),

    ("shatterproof_dynamite","EXPLODE",("#e7e5e4", "#ffffff", "#57534e", "#d6d3d1", "#57534e"), 2.0, False, False,
     [("dynamite", "tnts:"), ("obsidian", ""), ("iron_ingot", "")], 3,
     "Dinamita Irrompible", "Shatterproof Dynamite", "Dynamite Incassable", "Bruchsichere Dynamit", "Dinamita Irrompível"),

    ("snow_dynamite",    "FREEZES",  ("#e0f2fe", "#ffffff", "#bae6fd", "#f0f9ff", "#bae6fd"), 3.0, False, True,
     [("dynamite", "tnts:"), ("snowball", ""), ("packed_ice", "")], 3,
     "Dinamita de Nieve", "Snow Dynamite", "Dynamite Neige", "Schnee-Dynamit", "Dinamita de Neve"),

    ("sphere_dynamite",  "EXPLODE",  ("#4c1d95", "#ede9fe", "#2e1065", "#a78bfa", "#2e1065"), 5.0, True, True,
     [("dynamite", "tnts:"), ("gunpowder", ""), ("iron_nugget", ""), ("quartz", "")], 2,
     "Dinamita Esférica", "Sphere Dynamite", "Dynamite Sphérique", "Kugel-Dynamit", "Dinamita Esférica"),

    ("spiral_dynamite",  "EXPLODE",  ("#7c3aed", "#ede9fe", "#4c1d95", "#a78bfa", "#4c1d95"), 4.0, True, True,
     [("dynamite", "tnts:"), ("amethyst_shard", ""), ("glowstone_dust", ""), ("quartz", "")], 2,
     "Dinamita Espiral", "Spiral Dynamite", "Dynamite Spirale", "Spiral-Dynamit", "Dinamita Espiral"),

    ("timer_dynamite",   "TRAP",     ("#78350f", "#fef3c7", "#292524", "#f59e0b", "#292524"), 4.0, True, True,
     [("dynamite", "tnts:"), ("clock", ""), ("redstone", ""), ("gunpowder", "")], 2,
     "Dinamita Temporizador", "Timer Dynamite", "Dynamite Minuteur", "Timer-Dynamit", "Dinamita Temporizador"),

    ("tunneling_dynamite","TUNNEL",  ("#44403c", "#e7e5e4", "#1c1917", "#a8a29e", "#1c1917"), 5.0, True, False,
     [("dynamite", "tnts:"), ("iron_pickaxe", ""), ("iron_ingot", ""), ("ender_pearl", ""), ("redstone", "")], 1,
     "Dinamita Tuneladora", "Tunneling Dynamite", "Dynamite Tunnelleuse", "Tunnel-Dynamit", "Dinamita Tuneladora"),

    ("ultralight_dynamite","EXPLODE",("#e2e8f0", "#ffffff", "#64748b", "#f1f5f9", "#64748b"), 2.0, True, True,
     [("dynamite", "tnts:"), ("feather", ""), ("string", ""), ("paper", "")], 4,
     "Dinamita Ultraligera", "Ultralight Dynamite", "Dynamite Ultralégère", "Ultraleicht-Dynamit", "Dinamita Ultraleve"),

    ("vaporize_dynamite","NUCLEAR",  ("#1c1917", "#ef4444", "#0a0a0a", "#dc2626", "#0a0a0a"), 8.0, True, True,
     [("dynamite", "tnts:"), ("blaze_powder", ""), ("fire_charge", ""), ("nether_wart", "")], 2,
     "Dinamita Vaporizadora", "Vaporize Dynamite", "Dynamite Vaporisatrice", "Verdampfungs-Dynamit", "Dinamita Vaporizadora"),

    ("withering_dynamite","TOXIC",   ("#1c1917", "#a855f7", "#0a0a0a", "#c084fc", "#0a0a0a"), 5.0, True, True,
     [("dynamite", "tnts:"), ("wither_rose", ""), ("soul_sand", ""), ("wither_skeleton_skull", "")], 2,
     "Dinamita Wither", "Withering Dynamite", "Dynamite Flétrissante", "Verwilderungs-Dynamit", "Dinamita Wither"),

    ("wool_dynamite",    "CONFETI",  ("#f472b6", "#ffffff", "#be185d", "#fda4af", "#be185d"), 3.0, False, False,
     [("dynamite", "tnts:"), ("white_wool", ""), ("red_wool", ""), ("blue_wool", "")], 3,
     "Dinamita de Lana", "Wool Dynamite", "Dynamite Laine", "Woll-Dynamit", "Dinamita de Lã"),

    ("xray_dynamite",    "LIGHTNING",("#1e3a8a", "#dbeafe", "#172554", "#60a5fa", "#172554"), 4.0, True, True,
     [("dynamite", "tnts:"), ("glowstone_dust", ""), ("echo_shard", ""), ("redstone", "")], 2,
     "Dinamita Rayos X", "XRay Dynamite", "Dynamite Rayons X", "Röntgen-Dynamit", "Dinamita Raios X"),
]


def main():
    print(f"[gen_massive_tnts] Generando {len(NEW_TNTS)} TNTs/dinamitas nuevas...")

    # 1. Leer archivos existentes
    tnt_effect_path = os.path.join(JAVA_SRC, "block", "TntEffect.java")
    tnt_defaults_path = os.path.join(JAVA_SRC, "config", "TntDefaults.java")
    mod_blocks_path = os.path.join(JAVA_SRC, "ModBlocks.java")
    mod_items_path = os.path.join(JAVA_SRC, "ModItems.java")

    # 2. Generar texturas para cada TNT nueva
    tex_dir = TEX_DIR
    item_tex_dir = ITEM_TEX_DIR
    os.makedirs(tex_dir, exist_ok=True)
    os.makedirs(item_tex_dir, exist_ok=True)

    # Reutilizar la función de texturas del gen_resources.py
    sys.path.insert(0, os.path.join(BASE, "tools"))
    from gen_resources import write_png, hex_rgb, pixel_side, pixel_top, pixel_bottom, pixel_side_lit, pixel_top_lit, FRAMES, FRAME_HEIGHTS

    for name, effect, palette, *_ in NEW_TNTS:
        if os.path.exists(os.path.join(tex_dir, f"{name}.png")):
            continue  # ya existe
        pal = list(palette)  # keep as hex strings, pixel_side calls hex_rgb
        # Cara lateral
        write_png(os.path.join(tex_dir, f"{name}.png"), (16, 16),
                  lambda x, y, _p=pal: pixel_side(x, y, _p))
        # Cara lateral encendida (6 frames)
        write_png(os.path.join(tex_dir, f"{name}_on.png"), (16, 16 * FRAMES),
                  lambda x, y, _p=pal: pixel_side_lit(x, y % 16, y // 16, _p))
        # Cara superior
        write_png(os.path.join(tex_dir, f"{name}_top.png"), (16, 16),
                  lambda x, y, _p=pal: pixel_top(x, y, _p))
        # Cara superior encendida
        write_png(os.path.join(tex_dir, f"{name}_top_on.png"), (16, 16 * FRAMES),
                  lambda x, y, _p=pal: pixel_top_lit(x, y % 16, y // 16, _p))
        # Cara inferior
        write_png(os.path.join(tex_dir, f"{name}_bottom.png"), (16, 16),
                  lambda x, y, _p=pal: pixel_bottom(x, y, _p))
        # Textura de item (misma que la cara lateral)
        write_png(os.path.join(item_tex_dir, f"{name}.png"), (16, 16),
                  lambda x, y, _p=pal: pixel_side(x, y, _p))
        print(f"  Texturas generadas: {name}")

    # 3. Generar blockstate JSON
    blockstates_dir = os.path.join(ASSETS, "blockstates")
    os.makedirs(blockstates_dir, exist_ok=True)
    for name, *_ in NEW_TNTS:
        bs = {
            "variants": {
                "lit=false,north=false,east=false,south=false,west=false,powered=false": {
                    "model": f"tnts:block/{name}"
                },
                "lit=true,north=false,east=false,south=false,west=false,powered=false": {
                    "model": f"tnts:block/{name}_on"
                }
            }
        }
        # Simplificar: solo lit y no lit
        bs = {
            "variants": {
                "lit=false": {"model": f"tnts:block/{name}"},
                "lit=true": {"model": f"tnts:block/{name}_on"}
            }
        }
        with open(os.path.join(blockstates_dir, f"{name}.json"), "w") as f:
            json.dump(bs, f, indent=2)

    # 4. Generar modelo de bloque JSON
    models_dir = os.path.join(ASSETS, "models", "block")
    os.makedirs(models_dir, exist_ok=True)
    for name, *_ in NEW_TNTS:
        # Modelo base
        model = {
            "textures": {
                "particle": f"tnts:block/{name}",
                "down": f"tnts:block/{name}_bottom",
                "up": f"tnts:block/{name}_top",
                "north": f"tnts:block/{name}",
                "east": f"tnts:block/{name}",
                "south": f"tnts:block/{name}",
                "west": f"tnts:block/{name}"
            },
            "elements": [
                {"from": [0, 0, 0], "to": [16, 16, 16], "faces": {
                    "down": {"uv": [0, 0, 16, 16], "texture": f"tnts:block/{name}_bottom"},
                    "up": {"uv": [0, 0, 16, 16], "texture": f"tnts:block/{name}_top"},
                    "north": {"uv": [0, 0, 16, 16], "texture": f"tnts:block/{name}"},
                    "east": {"uv": [0, 0, 16, 16], "texture": f"tnts:block/{name}"},
                    "south": {"uv": [0, 0, 16, 16], "texture": f"tnts:block/{name}"},
                    "west": {"uv": [0, 0, 16, 16], "texture": f"tnts:block/{name}"}
                }}
            ]
        }
        with open(os.path.join(models_dir, f"{name}.json"), "w") as f:
            json.dump(model, f, indent=2)
        # Modelo encendido
        model_on = dict(model)
        model_on["textures"] = {
            "particle": f"tnts:block/{name}",
            "down": f"tnts:block/{name}_bottom",
            "up": f"tnts:block/{name}_top_on",
            "north": f"tnts:block/{name}_on",
            "east": f"tnts:block/{name}_on",
            "south": f"tnts:block/{name}_on",
            "west": f"tnts:block/{name}_on"
        }
        model_on["elements"][0]["faces"] = {
            "down": {"uv": [0, 0, 16, 16], "texture": f"tnts:block/{name}_bottom"},
            "up": {"uv": [0, 0, 16, 16], "texture": f"tnts:block/{name}_top_on"},
            "north": {"uv": [0, 0, 16, 16], "texture": f"tnts:block/{name}_on"},
            "east": {"uv": [0, 0, 16, 16], "texture": f"tnts:block/{name}_on"},
            "south": {"uv": [0, 0, 16, 16], "texture": f"tnts:block/{name}_on"},
            "west": {"uv": [0, 0, 16, 16], "texture": f"tnts:block/{name}_on"}
        }
        with open(os.path.join(models_dir, f"{name}_on.json"), "w") as f:
            json.dump(model_on, f, indent=2)

    # 5. Generar modelo de item JSON
    item_models_dir = os.path.join(ASSETS, "models", "item")
    os.makedirs(item_models_dir, exist_ok=True)
    for name, *_ in NEW_TNTS:
        item_model = {"parent": f"tnts:block/{name}"}
        with open(os.path.join(item_models_dir, f"{name}.json"), "w") as f:
            json.dump(item_model, f, indent=2)

    # 6. Generar recetas JSON
    recipes_dir = os.path.join(DATA, "recipes")
    os.makedirs(recipes_dir, exist_ok=True)
    for name, effect, palette, radius, breaks, fire, recipe, count, *_ in NEW_TNTS:
        ingredients = []
        for item_id, mod_prefix in recipe:
            if mod_prefix:
                full_id = f"{mod_prefix}{item_id}"
            else:
                full_id = f"minecraft:{item_id}"
            ingredients.append({"item": full_id})
        # Agrupar ingredientes iguales
        from collections import Counter
        ingredient_counts = Counter()
        for ing in ingredients:
            ingredient_counts[ing["item"]] += 1
        shaped_ingredients = []
        for item_id, count in ingredient_counts.items():
            shaped_ingredients.append({"item": item_id})
        recipe_json = {
            "type": "minecraft:crafting_shaped",
            "pattern": ["TGG", "GGG", "   "][:min(3, max(1, len(shaped_ingredients)))],
            "key": {},
            "result": {"item": f"tnts:{name}", "count": count}
        }
        # Mapear ingredientes a letras
        letters = "TGIABCDEF"
        pattern_rows = []
        row = ""
        for i, ing in enumerate(shaped_ingredients):
            letter = letters[i]
            recipe_json["key"][letter] = ing
            row += letter * min(3, ing.get("count", 1))
            if len(row) >= 3:
                pattern_rows.append(row[:3])
                row = row[3:]
        if row:
            pattern_rows.append(row.ljust(3))
        while len(pattern_rows) < 3:
            pattern_rows.append("   ")
        recipe_json["pattern"] = pattern_rows[:3]
        with open(os.path.join(recipes_dir, f"{name}.json"), "w") as f:
            json.dump(recipe_json, f, indent=2)

    # 7. Actualizar NAMES en gen_resources.py
    gen_res_path = os.path.join(BASE, "tools", "gen_resources.py")
    with open(gen_res_path, "r", encoding="utf-8") as f:
        content = f.read()
    # Añadir nuevos nombres al final de NAMES
    new_names_str = ", ".join(f'"{name}"' for name, *_ in NEW_TNTS)
    # Buscar el final de NAMES
    import re
    names_match = re.search(r'NAMES\s*=\s*\[(.*?)\]', content, re.DOTALL)
    if names_match:
        old_names = names_match.group(1).strip()
        if NEW_TNTS[0][0] not in old_names:
            new_content = content[:names_match.end()-1] + f',\n         {new_names_str}]' + content[names_match.end():]
            with open(gen_res_path, "w", encoding="utf-8") as f:
                f.write(new_content)
            print(f"  OK NAMES actualizado en gen_resources.py")

    print(f"\n[gen_massive_tnts] OK {len(NEW_TNTS)} TNTs/dinamitas generadas!")
    print(f"  - Texturas: {len(NEW_TNTS) * 6} PNGs")
    print(f"  - Blockstates: {len(NEW_TNTS)} JSONs")
    print(f"  - Modelos: {len(NEW_TNTS) * 3} JSONs")
    print(f"  - Recetas: {len(NEW_TNTS)} JSONs")
    print("\nSiguiente paso: ejecutar gen_resources.py para lang y advancements")


if __name__ == "__main__":
    main()
