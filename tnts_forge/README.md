# TNTs Locos — Mod para Minecraft 1.20.1 (Forge)

**27 bloques explosivos** (26 TNTs + mina trampa) + Detonador Remoto, Lanzador, Flecha y Granada de TNT. Se encienden con **mechero** (flint & steel) o carga de fuego, con **redstone** y en **cadena** si una explosión las alcanza.

## TNTs

| TNT | Efecto | Receta |
|---|---|---|
| Mega TNT | Explosión enorme (radio 12) | 2×2 TNT |
| Mini TNT | Explosión pequeña (×2) | TNT + pólvora |
| TNT de Lava | Incendia + deja charcos de lava | TNT + cubo de lava |
| TNT Instantánea | Mecha de 1 segundo | TNT + redstone |
| TNT de Hielo | Congela el agua + cubre de nieve | TNT + cubo de agua |
| TNT Saltarina | Lanza jugadores y mobs por los aires | TNT + slime |
| TNT Nuclear | Radio 20, incendia, lluvia de partículas | TNT + netherita + pólvora |
| TNT Limpia | No rompe bloques (solo daño) | TNT + pluma |
| TNT de Rayo | Invoca rayos | TNT + pararrayos |
| TNT Trampa | Mecha de 4s con pitidos de cuenta atrás | TNT + reloj |
| TNT de Oro | Suelta lingotes de oro | TNT + lingote de oro |
| TNT de Obsidiana | Crea un cráter blindado de obsidiana | TNT + obsidiana |
| TNT Criogénica | Congela a jugadores y mobs | TNT + hielo azul |
| TNT de Experiencia | Suelta orbes de XP | TNT + botella encantada |
| TNT de Agua | Inunda el cráter y apaga incendios | TNT + esponja |
| TNT de Arena | Avalancha de arena que cae sobre la zona | TNT + arena |
| TNT de Diamante | Suelta diamantes | TNT + diamante |
| TNT de Esmeralda | Suelta esmeraldas | TNT + esmeralda |
| TNT Agujero Negro | Succiona entidades e items hacia el cráter | TNT + obsidiana llorona |
| TNT de Viento | Empuja todo lejos | TNT + membrana de fantasma |
| TNT Inferno | Incendia una zona enorme | TNT + crema de magma |
| TNT de Setas | Esparce setas y micelio | TNT + seta roja |
| TNT de Miel | Lentitud pegajosa + bloques de miel | TNT + panal |
| TNT Curativa | Cura + regeneración | TNT + lágrima de ghast |
| TNT Teletransportadora | Manda a los seres vivos a sitios aleatorios | TNT + perla de ender |
| TNT de Confeti | Lluvia de partículas de colores | TNT + 3 tintes |
| Mina TNT | Explota al pisarla | TNT + placa de presión de piedra |

**Detonador Remoto:** enciende todas las TNTs en 24 bloques a la redonda (solo chunks cargados). Receta: hierro + redstone + TNT.

**Lanzador de TNT:** consume una TNT de tu inventario (cualquier variante) y la dispara con física. Receta: 2 hierro + TNT.

**Flecha de TNT:** proyectil que explota al impactar. Receta: TNT + 2 palos → 2 flechas.

**Granada de TNT:** se lanza con mecha de 1.5s, rebota y explota. Receta: TNT + cuerda → 2.

## Características

- 🧨 **TNTs con física real**: al encenderse son entidades — caen, rebotan, las empuja el agua y las lanzan las explosiones.
- 🔊 **Sonidos propios** sintetizados: mecha distinta por variante y explosiones con carácter (nuclear, hielo, rayo, oro, agua…).
- 🏆 **Advancements**: encender tu primera TNT, craftear cada una y el reto oculto de explotar las 16.
- 🔥 **Mecha animada**: textura de 6 frames con la llama parpadeando, humo, chispas y **flash blanco** en los últimos ticks antes de explotar.
- ⚙️ **Config editable**: `config/tnts-common.toml` — radio, mecha, fuego, romper bloques y efectos de **cada TNT**, con botón de config en el menú de mods.
- 🧪 **GameTests** automatizados (explosión, solo mechero, peto reactivo, pico explosivo, lanzador, granada, detonador masivo y registro del bunker) — se ejecutan con `./gradlew runGameTestServer`.
- 🏰 **Búnker de TNT abandonado** generado en el Overworld: TNTs escondidas en las paredes, minas en el suelo y un cofre con provisiones. `/locate structure tnts:tnts_bunker`.
- 🏷️ Recetas con **tags de Forge** (`forge:ingots/gold`, `forge:sand`, …) que aceptan variantes de otros mods.
- 🎨 Icono de mod propio, idiomas **español/inglés** y pestaña creativa *TNTs Locas*.

## Requisitos

- Minecraft **1.20.1** con **Forge 47.x** (probado con 47.2+)
- Java 17

## Instalar

1. Copia `build/libs/tnts-1.6.0.jar` (o el `.jar` que compile) a la carpeta `mods` de la instancia.
2. Abre el juego con el perfil Forge.
3. Pestaña creativa **TNTs Locas** o craftea las recetas.

## Config

La config se genera en `config/tnts-common.toml` al primer arranque. Por cada TNT:

| Clave | Qué hace | Ejemplo |
|---|---|---|
| `power` | Radio de la explosión | `power = 12.0` |
| `fuse` | Mecha en ticks (20 = 1s) | `fuse = 40` |
| `fire` | Incendia el terreno | `fire = true` |
| `breaksBlocks` | Destruye bloques | `breaksBlocks = true` |
| `effects` | Efectos especiales | `effects = ["LAVA"]` |

## Tests

```bash
./gradlew runGameTestServer   # ejecuta los GameTests y reporta
```

## Compilar

```bash
./gradlew build   # necesita Java 17
```

## Changelog

Ver `CHANGELOG.md` (raíz del repo) o `modrinth.md`.

## Licencia

MIT
