# 💥 TNTs Locos

Mod de Minecraft con **32 TNTs con personalidad propia** para **Java (Forge 1.20.1)** y **Bedrock (addon)**.

Cada TNT tiene su propia textura, sonido, explosión y comportamiento. Desde la Mini TNT hasta la Colosal (power 18) o el Agujero Negro (cráter de 8s que succiona todo).

## 📁 Estructura

```
tnts_locos/
├── tnts_forge/           # Mod de Java (Forge 1.20.1)
│   ├── src/main/java/    # Código
│   ├── src/main/resources/ # Texturas, modelos, recetas, lang, sonidos
│   └── tools/            # Generadores (texturas + sonidos sintetizados)
├── TNTsLocos_BP/         # Behavior Pack de Bedrock (bloques, items, scripts)
├── TNTsLocos_RP/         # Resource Pack de Bedrock (texturas, modelos, lang)
├── tools/build_addon.py  # Genera y empaqueta TNTsLocos.mcaddon
├── CHANGELOG.md
└── modrinth.md           # Descripción para publicación
```

## 🔨 Compilar (Java)

```bash
cd tnts_forge
python tools/gen_resources.py   # regenerar texturas/modelos/recetas
python tools/gen_sounds.py      # sintetizar sonidos (necesita ffmpeg)
./gradlew build                 # jar en build/libs/
```

## 📦 Empaquetar (Bedrock)

```bash
cd tnts_forge && python tools/gen_sounds.py   # copia .ogg al RP
cd .. && python tools/build_addon.py          # genera TNTsLocos.mcaddon
```

## ✅ Tests

```bash
cd tnts_forge && ./gradlew runGameTestServer  # 10 GameTests
```

Detalles en [modrinth.md](modrinth.md) y [CHANGELOG.md](CHANGELOG.md).
