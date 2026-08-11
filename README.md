# 💥 TNTs Locos

Mod de Minecraft con **38 TNTs con personalidad propia** para **Java (Forge 1.20.1)**.

Cada TNT tiene su propia textura, sonido, explosión y comportamiento. Desde la Mini TNT hasta la Colosal (power 20) o el Agujero Negro (cráter de 8s que succiona todo).

## 👑 Contenido destacado

- **38 TNTs** con efectos únicos (5 masivas con espectáculos 3D)
- **Rey TNT**: jefe con 300 de vida, embestida, invocación de piglins, TNTs Reales desactivables con tijeras, fases visuales de grietas y animación de derrota
- **Set del Rey**: Corona + Peto + Espada + Escudo con **set bonus** progresivo (2/3/4 piezas)
- **Armadura de TNT completa**: Peto, Botas (doble salto), Casco (visión nocturna) + Pico explosivo
- **3 estructuras**: búnker abandonado, almacén saqueado (con Rey guardián) y campo de pruebas
- **Items**: Detonador Remoto, Lanzador, Granada, Flecha, Manual de TNTs, disco de música, Aldeano Experto
- **81+ sonidos** de estudio (CC0) con reverb y eco
- **5 idiomas**: ES, EN, PT-BR, DE, FR
- **Config** editable por TNT (poder, fuego, mecha, activada/desactivada, calidad de partículas)

## 📁 Estructura

```
tnts_locos/
├── tnts_forge/           # Mod de Java (Forge 1.20.1)
│   ├── src/main/java/    # Código
│   ├── src/main/resources/ # Texturas, modelos, recetas, lang, sonidos
│   └── tools/            # Generadores (texturas + sonidos)
├── CHANGELOG.md
└── modrinth.md           # Descripción para publicación
```

## 🔨 Compilar (Java)

```bash
cd tnts_forge
python tools/gen_resources.py   # regenerar texturas/modelos/recetas/lang
python tools/gen_sounds.py      # sintetizar sonidos (necesita ffmpeg)
./gradlew build                 # jar en build/libs/
```

## ✅ Tests

```bash
cd tnts_forge && ./gradlew runGameTestServer  # 24 GameTests
```

Detalles en [modrinth.md](modrinth.md) y [CHANGELOG.md](CHANGELOG.md).
