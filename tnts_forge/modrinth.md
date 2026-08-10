# TNTs Locos — Forge 1.20.1

**16 TNTs personalizadas** con efectos únicos, Detonador Remoto, config editable y mechas animadas. Se encienden con **mechero**, con **redstone** o en **cadena** si una explosión las alcanza.

## Las TNTs

| TNT | Efecto | Receta |
|---|---|---|
| Mega TNT | Explosión enorme (radio 12) | 2×2 TNT |
| Mini TNT | Explosión pequeña | TNT + pólvora (×2) |
| TNT de Lava | Incendia + deja charcos de lava reales | TNT + cubo de lava |
| TNT Instantánea | Mecha de 1 segundo | TNT + redstone |
| TNT de Hielo | Congela el agua + cubre la zona de nieve | TNT + cubo de agua |
| TNT Saltarina | Lanza jugadores y mobs por los aires | TNT + slime |
| TNT Nuclear | Radio 20, incendia y lluvia de partículas | TNT + netherita + pólvora |
| TNT Limpia | No rompe bloques, solo daño | TNT + pluma |
| TNT de Rayo | Invoca rayos | TNT + pararrayos |
| TNT Trampa | Mecha de 4s con pitidos que se aceleran | TNT + reloj |
| TNT de Oro | Suelta lingotes de oro | TNT + lingote de oro |
| TNT de Obsidiana | Crea un cráter blindado de obsidiana | TNT + obsidiana |
| TNT Criogénica | Congela a jugadores y mobs | TNT + hielo azul |
| TNT de Experiencia | Suelta orbes de XP | TNT + botella encantada |
| TNT de Agua | Inunda el cráter y apaga incendios | TNT + esponja |
| TNT de Arena | Avalancha de arena | TNT + arena |

## Detonador Remoto

Enciende **todas las TNTs en 24 bloques** (chunks cargados) con un click. Receta: hierro + redstone + TNT.

## Características

- 🔥 **Mecha animada**: textura de 6 frames con la llama parpadeando, humo, chispas y **flash blanco** antes de explotar.
- ⚙️ **Config editable**: `config/tnts-common.toml` — radio, mecha, fuego, romper bloques y efectos de cada TNT, sin recompilar (se recarga con `/reload`).
- 🧪 **GameTests**: verificación automática (explosión + solo mechero).
- 🏷️ Recetas con **tags de Forge** (acepta variantes de otros mods).

## Requisitos

- Minecraft **1.20.1** · **Forge 47.x** · Java 17

## Instalar

1. Copia el `.jar` a la carpeta `mods` de la instancia.
2. Abre el juego con el perfil Forge.
3. Las TNTs están en la pestaña creativa **TNTs Locas** (o craftea las recetas).

## Config

| Clave | Qué hace |
|---|---|
| `power` | Radio de la explosión (bloques) |
| `fuse` | Duración de la mecha (ticks; 20 = 1s) |
| `fire` | Si incendia el terreno |
| `breaksBlocks` | Si destruye bloques |
| `effects` | Efectos especiales (lava, nieve, rayos, XP…) |

## Changelog

- **1.5.0**: 5 TNTs nuevas (Obsidiana, Criogénica, XP, Agua, Arena), animación de 6 frames, GameTests, icono del mod, perf del detonador.
- **1.4.0**: config en juego, lava/nieve reales, saltarina para todos, detonador, flash+pitos, recetas con tags, verificación en runtime.
- **1.3.0**: solo mechero, 3 TNTs nuevas (Rayo, Trampa, Oro), animaciones, mechas largas.
- **1.2.1**: fix crash "Registry frozen" (registro perezoso).
- **1.2.0**: versión Java inicial (8 TNTs).

## Compilar

```bash
cd tnts_forge
./gradlew build   # necesita Java 17
```

## Licencia

MIT
