# TNTs Locos — Changelog

## v1.10.11 — Pantalón de TNT, armadura completa, corona 3D en el jugador, fixes de capas y más advancements (solo Java)

- **👖 Pantalón de TNT:** la armadura de TNT por fin está COMPLETA (casco + peto + pantalón + botas). Receta: 7 TNT en forma de pantalón.
- **🛡️ SET BONUS de la armadura de TNT (4 piezas):** "Blindaje de TNT" — el daño de CUALQUIER explosión se reduce a la mitad (+50% de resistencia a explosiones).
- **👑 CORONA 3D en el jugador:** la Corona del Rey ya no se ve plana (ni invisible) — ahora se renderiza con un **modelo 3D propio**: banda dorada con gema roja y 5 puntas (4 en las esquinas + 1 central más alta), con su propia textura. ¡El rey de verdad la lleva!
- **🐛 FIX de las capas de armadura (el bug de verdad):** el generador dibujaba la cabeza de la armadura en `(0-7,0-7)` pero el modelo de armadura de Minecraft lee la cabeza en las caras `(0-31,8-15)` + top/bottom — por eso **la corona y el casco no se veían en el jugador**. Y las **botas se dibujaban en layer_2 cuando las botas usan layer_1** (solo los pantalones usan layer_2) — por eso no se veían las botas. Las dos capas (64x32) ahora usan el layout REAL de vanilla 1.20.1: layer_1 con cabeza/cuerpo/brazos/piernas (casco, peto y botas) y layer_2 con piernas + falda (pantalones).
- **🏰 El Rey de verdad en el búnker:** el búnker de TNT abandonado ahora tiene un **60% de probabilidades de estar habitado por el Rey TNT** (con su barra de jefe completa y botín completo), como dice su descripción — ya no solo el guardián del almacén.
- **🏆 6 advancements nuevos:** "¡Doble salto!" (botas), "Visión de TNT" (casco), "Poder del Rey" (set 2 piezas), "Escudo Real" (set 3), "Aura del Rey" (set 4) y "Se está agrietando..." (fases visuales del Rey) + "Guardia derrotada" (Rey guardián del almacén). Triggers nuevos `tnts:double_jumped`, `tnts:king_set`, `tnts:king_phase` y `tnts:king_guardian_defeated`.
- GameTests nuevos (`tnt_leggings_completes_armor_set`, `tnt_armor_full_set_halves_explosion_damage`, `king_phase_advancement_fires_at_level_2`) → **"All 31 required tests passed"**.

## v1.10.10 — Set del Rey, Botas + Casco de TNT, Rey guardián del almacén y 3 idiomas nuevos (solo Java)

- **👑 Set bonus del Rey TNT** (Corona + Peto + Espada + Escudo) con sinergias progresivas:
  - **2 piezas — "Poder del Rey":** la Espada del Rey enciende TNTs con el DOBLE de radio (12 bloques en vez de 6).
  - **3 piezas — "Escudo Real":** inmunidad al daño de las TNTs del propio mod (las tuyas ya no te hieren).
  - **4 piezas — "Aura del Rey":** cada 2s un aura enciende las TNTs del mod cercanas (radio 8) automáticamente, con destello dorado y pitido.
- **👢 Botas de TNT:** al agacharte en el aire haces un impulso de explosión con mini-explosión (partículas, sonido y desgaste de durabilidad) — el doble salto de TNT.
- **⛑️ Casco de TNT:** visión nocturna mientras lo llevas puesto.
- **👑 Rey guardián del almacén:** el Almacén de TNT saqueado tiene un ~35% de probabilidades de estar custodiado por un Rey TNT en modo guardián (menos vida, botín reducido, sin barra de jefe) — explorar el almacén ahora tiene más emoción.
- **🌍 3 idiomas nuevos:** portugués (pt_br), alemán (de_de) y francés (fr_fr) además de español e inglés.
- **📚 Documentación actualizada:** README y modrinth.md con los números reales (38 TNTs, 7 items especiales, 3 estructuras, jefe con fases).
- GameTests nuevos (`king_set_sword_double_radius`, `king_set_three_pieces_immune_to_tnt_blast`, `king_guardian_warehouse`) → **"All 28 required tests passed"**.

## v1.10.9 — Fases visuales del Rey TNT: se agrieta y suelta fragmentos (solo Java)

- **💥 3 texturas del Rey segun la vida** (el renderer elige la textura con `getCrackLevel()`):
  - **Sano** (>66% de vida): la textura original impecable.
  - **Agrietado** (33-66%): primeras grietas negras en las 4 caras del cubo, en la cara frontal (la vertical central entre los ojos), y en las puntas de la corona.
  - **Muy agrietado** (<33%): grietas más largas y gruesas en todo el cuerpo (incluido el top del cubo), la corona agrietada y los cantos del cubo oscurecidos — se ve a punto de reventar.
- **🧱 Fragmentos de bloque:** cuanto menos vida, más frecuente y denso suelta trozos de su propio bloque (partículas de la Mega TNT) + humo de la mecha cada vez más denso. Se siente el daño acumulado mientras peleas.
- En la **derrota** (muerte) siempre usa la textura más agrietada antes del boom final.
- GameTest nuevo (`king_cracks_at_low_health`): verifica que a vida completa está en nivel 0, al 50% en nivel 1, por debajo del 33% en nivel 2, y que el tick con partículas no crashea → **"All 24 required tests passed"**.

## v1.10.8 — Rey TNT completo: atlas de textura corregido + brazos en el hombro (solo Java)

- **🐛 EL BUG GRANDE: el atlas de la textura usaba formato de BLOQUE y el modelo es de ENTIDAD.** Minecraft lee los cubos de entidad con un layout distinto al de los bloques: el generador dibujaba las 4 caras laterales en la fila superior (donde Minecraft espera top/bottom) y dejaba VACÍAS las zonas donde Minecraft lee los costados. Por eso **el cuerpo del Rey no se veía por los lados** ("de los dos lados no se ve nada") y la corona/mecha/llama tampoco se veían bien.
- **🩹 Atlas reescrito desde cero en el formato estándar de cubo de entidad** (top/bottom arriba, caras laterales abajo, con la fórmula exacta de UV de Minecraft). Ahora TODAS las piezas del Rey se ven: cuerpo completo con su cara, corona con gemas, las 5 puntas, brazos, cejas, mecha y llama.
- **🦾 Brazos reposicionados:** antes colgaban desde y=-2 hacia abajo (saliendo por debajo del cuerpo) — ahora tienen el pivote en el hombro (y=-14) y cuelgan a lo largo del cuerpo, como debe ser.
- **😡 Cara arreglada:** los ojos y la boca se dibujaban DETRÁS de la franja blanca de la TNT (la franja se pintaba primero). Ahora los rasgos de la cara se dibujan encima — ojos amarillos con pupila y boca con dientes bien visibles.
- GameTests: **"All 23 required tests passed"**.

## v1.10.7 — Fix visual del Rey TNT + texturas mejoradas de espada y escudo (solo Java)

- **👑 Modelo v3: fixes visuales críticos**
  - Las **5 puntas de la corona ya no flotan** — ahora son hijos de root con posiciones absolutas correctas (desde y=-16 hacia arriba), no relativas al padre desplazado.
  - Las **cejas** ahora están exactamente sobre los ojos en z=-8.5 (pegadas a la cara frontal) y más arriba (y=-10), encima de la franja blanca.
  - Los **brazos** ahora empiezan en y=-2 (junto al cuerpo) y cuelgan 12px, con la textura TNT mejorada con franja blanca visible.
  - La **mecha y llama** ahora están en z=3 (en la cara superior del cuerpo, no dentro del cubo).
- **🗡️ Espada del Rey mejorada:** filo de acero con sombreado, hoja ancha con T grabada en TNT, guarda dorada con gema roja, mango de madera con wrapping y pomo.
- **🛡️ Escudo de TNT mejorado:** borde metálico con sombreado de acero, franja TNT con T grabada, remaches dorados, y mecha encendida en la punta superior.
- **Ojos del Rey:** ahora son más grandes (3x2 píxeles) con pupila oscura, y la cara es más expresiva (cejas con punta dorada, boca con colmillos).
- GameTests: **"All 23 required tests passed"**.

## v1.10.6 — Animación de derrota del Rey TNT: parpadeo blanco, grietas y boom final (solo Java)

- **💀 Secuencia de derrota (2.2 segundos):** al matar al Rey ya no desaparece al instante — se queda quieto y se desmorona de verdad:
  1. **Parpadeo blanco:** el modelo entero brilla en blanco cada 3 ticks (como el flash de daño de vanilla, pero controlado por el mod).
  2. **Grietas:** suelta fragmentos de su propio bloque (partículas de bloque de la Mega TNT) cada vez más densos, con humo y chispas de la mecha.
  3. **Temblor:** se inclina y vibra cada vez más fuerte, con los brazos colgando — y temblor de cámara para los jugadores cercanos.
  4. **💥 BOOM final:** a los 2.2s explota de verdad (radio 5, rompe bloques) con flash, onda de luz y sonido de explosión — un final de jefe a la altura.
- El botín (corona, espada, escudo, TNTs y XP) se suelta al morir como siempre, y el boom final es el que lo hace volar.
- GameTest nuevo (`king_defeat_animation_no_crash`) que mata al Rey y verifica que completa la secuencia sin crashear → **"All 23 required tests passed"**.

## v1.10.5 — Rey TNT rediseñado: modelo detallado con animaciones completas (solo Java)

- **👑 Modelo v2:** el Rey ya no es un cubo pelado — ahora es una TNT gigante con **brazos** que se balancean, **corona dorada con 5 puntas** (la central más alta), **cejas 3D** que se inclinan, **mecha con llama parpadeante**, remaches en las esquinas y una **cara enfadada detallada** (ojos con pupila, cejas doradas, boca con dientes).
- **🎬 Animaciones completas:**
  - **Entrada:** al convocarse hace un **"pop" elástico** (escala 0 → 1.12 → 1 con rebote) mientras la llama de la mecha se enciende enorme.
  - **Idle:** late como una TNT a punto de explotar + llama que parpadea + brazos balanceándose.
  - **Furia (<50% HP):** late más rápido, cejas inclinadas hacia dentro (más enfadado) y brazos en alto.
  - **Carga (embestida):** vibra violentamente mientras acumula la carga de 3s.
  - **Dash:** se estira y se inclina hacia adelante con los brazos extendidos.
  - **Aturdido:** brazos caídos y balanceo tambaleante.
- **Textura nueva 128x64** dibujada a mano píxel a píxel (cuerpo TNT con cara, corona, puntas, brazos, cejas, mecha y llama en su propio rectángulo del atlas).

## v1.10.4 — Espectáculo extra con particleQuality=2 en las TNTs masivas (solo Java)

- **🌈 Anillos de colores expansivos:** al explotar una masiva (Terremoto, Meteorito, Tormenta, Colosal, Supernova, Nuclear o Agujero Negro) con **calidad de partículas en MÁXIMO** (config → `particleQuality = 2`), se lanza un abanico de **3 capas de anillos de colores** (rojo-naranja, amarillo, cian y púrpura) en radios crecientes (3/7/11) con chispas verticales que dan volumen.
- **📸 Destello de cámara:** flash blanco en el centro de la explosión + **temblor de pantalla** para todos los jugadores en un radio de 32 bloques (la animación de daño de vanilla, sin daño real — el mismo truco del Agujero Negro).
- **⚡ Sin pérdida de rendimiento:** todo con bucles acotados (~216 partículas por explosión, partículas de polvo ligeras) — el modo PC débil (0) y el normal (1) no cambian nada.
- GameTest nuevo (`massive_quality2_show_no_crash`) que ejercita el espectáculo en el servidor → **"All 22 required tests passed"**.

## v1.10.3 — 2 estructuras nuevas: Almacén de TNT saqueado y Campo de Pruebas (solo Java)

- **🏚️ Almacén de TNT saqueado** (`/locate structure tnts:tnts_warehouse`): gran depósito subterráneo (17x9x17) dividido en **4 celdas cerradas** alrededor de un pasillo en cruz, con **muros de ladrillo que esconden TNTs**, cajas de madera de la mercancía saqueada, **agujeros en el techo** por donde entró el ladrón, **minas** en los accesos y **3 cofres**: uno casi vacío (lo saquearon), uno con provisiones y uno del fondo que aún guarda botín (Mega TNT, TNT de Diamante, pólvora y el Manual). Un **spawner de zombies** guarda la celda noreste.
- **🎯 Campo de Pruebas de TNT** (`/locate structure tnts:tnts_test_range`): zona de pruebas al aire libre con **muros de observación con ventanas**, **4 blancos de piedra con una TNT encima** (apunta y dispara), **trampas de placa de presión sobre TNT Nuclear enterrada** (¡písalas con cuidado!), un **spawner de esqueletos** como maniquíes vivos en la plataforma central y un **cofre premio** con el Detonador Remoto, granadas, Mini TNTs y pólvora.
- Se generan en el Overworld cada ~28-30 chunks con separación propia. GameTest nuevo (`warehouse_structure_generates`) → **"All 21 required tests passed"**.

## v1.10.2 — Espada del Rey TNT + Escudo de TNT como botín raro del boss (solo Java)

- **⚔️ Espada del Rey TNT** (botín raro, 40%): hoja de TNT fundida (1561 de durabilidad, daño de diamante). **Al golpear a un enemigo enciende TODAS las TNTs del mod en un radio de 6 bloques** — convierte cualquier pelea en reacción en cadena. Se repara en el yunque con pólvora. Textura pixel-art: hoja roja con franja blanca, guarda dorada y gema en el pomo.
- **🛡️ Escudo de TNT** (botín raro, 40%): bloquea como un escudo normal y, al bloquear un golpe, **empuja al atacante lejos con un destello de llamas y una pequeña explosión**. Se repara en el yunque con pólvora (o con una Mini TNT). Textura pixel-art: escudo redondeado rojo con franja blanca, remaches dorados y mecha encendida.
- Ambos **raro (púrpura)**, en la pestaña creativa *TNTs Locas* y con nombres en español/inglés.
- GameTests nuevos (`king_sword_primes_tnts`, `tnt_shield_pushes_attacker`) → **"All 20 required tests passed"**.

## v1.10.1 — Pelea del Rey TNT mejorada: embestida, invocación de piglins y TNT Real (solo Java)

- **🏃 Embestida (modo furia):** cada ~10 segundos el Rey se queda quieto, suena un **gemido que sube de tono durante 3 segundos** (con polvo rojo acumulándose a su alrededor) y luego **sale disparado hacia ti**: 12 de daño + knockback a quien toque, con estela de humo y llamas. Aviso clarísimo para saltar a un lado.
- **👹 Grito de invocación:** cada ~20 segundos el Rey ruge y **trae piglins del Nether** (piglins brutos y piglins zombificados) con partículas de portal, que atacan a su objetivo. La guardia real del Rey.
- **💎 TNT REAL:** en modo furia, cada 3 ataques el Rey lanza **2 TNTs Reales** — mecha corta (1.75s), pitido de cuenta atrás urgente, radio **2.5x** e incendia. **No se puede aguantar**: la única forma de frenarlas es **desactivarlas con tijeras** (click derecho), y al hacerlo el Rey queda **aturdido 5 segundos** (inmóvil con estrellitas alrededor de la cabeza) — tu ventana de daño gratis. Así se gana la pelea: desactiva sus TNTs y machaca.
- **Sonido nuevo**: `entity.tnt_king.charge` (gemido de carga sintetizado que sube de tono).
- GameTests nuevos (`royal_tnt_defuse_stuns_king`, `king_force_summon`) → **"All 18 required tests passed"**.

## v1.10.0 — Rey TNT, aldeano, manual, disco, 3 TNTs nuevas, config y defuse (solo Java)

### 👑 EL REY TNT (jefe del mod)
- **Nuevo jefe**: vive en el bunker de TNT abandonado. Se convoca con el **Altar del Rey TNT** (bloque nuevo en el centro del bunker).
- **300 de vida, barra de jefe propia** (roja), lanza TNTs encendidas al jugador con arco (mini, mega, rápida, lava, rayo, oro) y **planta minas** en el suelo.
- **Modo FURIA** por debajo del 50%: lanza 3 TNTs de golpe, mucho más rápido, con rugido y anillo de polvo rojo.
- **Botín**: suelta la **Corona del Rey TNT** (siempre), 3-6 TNTs al azar y 200 XP. Al llevarla puesta: **Resistencia al Fuego** + tus TNTs explotan con **+50% de radio**.
- Modelo 3D propio (cubo gigante con corona dorada y cara enfadada), rugido sintetizado, huevo de invocación en la pestaña creativa y advancement "¡El Rey ha caído!".

### 🧑‍🌾 Aldeano Experto en TNTs
- Nueva profesión con su **Mesa de TNTs** (estación de trabajo / POI). Un aldeano desempleado que la encuentre se vuelve vendedor: **vende TNTs por esmeraldas** (mini, mega, rápida, rayo, lava, oro, diamante, nuclear y colosal según nivel) y **compra pólvora**.

### 📖 Manual de TNTs + 💿 Disco de música
- **Manual de TNTs** (item): abre un GUI con scroll con las 38 TNTs — icono, nombre, efecto y radio. Receta: libro + TNT. (También aparece en el cofre del bunker.)
- **Disco de TNTs Locas**: tema musical propio de 72s (marcha explosiva con batería sintetizada, bajo, melodía y acentos de explosión) tocable en la jukebox. Receta: 8 TNT + esmeralda.

### 🧨 3 TNTs nuevas (38 en total)
- **🌌 TNT del End**: teletransporta a los seres vivos a sitios aleatorios (¡a veces muy arriba!), invoca endermites. Receta: TNT + fruta de coro + piedra del End.
- **🫧 TNT Burbuja**: succiona seres e items hacia el cráter como un remolino y derrite el hielo. Receta: TNT + pez globo + alga.
- **☀️ TNT Solar**: hace día, despeja el tiempo e incendia una gran área. Receta: TNT + polvo de blaze + piedra luminosa.

### ⚙️ Config nueva + tijeras
- **Toggle por TNT en config**: `enabled = false` desactiva esa TNT del todo (no se enciende con mechero, redstone, detonador ni en cadena). Visible en la pantalla de config ("APAGADA").
- **Calidad de partículas** global (`particleQuality`: 0 = modo PC débil, 1 = normal, 2 = máximo) — escala todas las partículas del mod.
- **Desactivar con tijeras**: click derecho con shears sobre una TNT encendida la apaga y te **devuelve el bloque** (con sonido y humo). ¡Adiós reacciones en cadena no deseadas!

### ✅ Verificado
- **"All 16 required tests passed"** (3 GameTests nuevos: defuse con tijeras, altar→Rey TNT, TNTs nuevas 1.10.0).
- 38 TNTs, 81 sonidos (incluye rugido del Rey, defuse y el tema del disco).

## v1.9.8 — Items en espiral hacia el Agujero Negro (solo Java)

- **Estela blanca en espiral:** los items y orbes de XP que entran en la zona de captura (6 bloques) ya no desaparecen al instante — quedan **atrapados en una espiral que acelera** hacia el núcleo: giran cada vez más rápido y caen cada vez más fuerte cuanto más cerca están (velocidad tangencial + radial no lineal).
- **Efecto de estiramiento:** cada item suelta una **estela de partículas blancas brillantes** (END_ROD) que se hace más densa y rápida al acercarse al centro — parecen estirados mientras giran.
- **Consumido en el núcleo:** al llegar al centro (radio 0.9) desaparecen con destello, chispas blancas y el sonido de orbe.

## v1.9.7 — Agujero Negro con NUCLEO REAL (solo Java)

- **🕳️ Núcleo negro real:** ya no es una esfera de partículas — el modelo 3D ahora es un **disco negro plano** (el portal) con **anillo de acreción morado** exterior e **interior rosa/blanco**, inclinado 25° como un portal, brillo completo y girando en direcciones opuestas mientras pulsa.
- **✨ Disco de acreción en partículas:** las partículas **orbitan y caen al centro** (velocidad tangencial + radial) con **gradiente morado → rosa → blanco** según la distancia al núcleo.
- **🌀 Succión progresiva por fases:** 0-2s atracción leve → 2-5s jala fuerte → **5-7s modo CORRE** (succión 2.4→3.5) → 7-8s **colapso** (hasta 5.0). Fuerza **no lineal**: cuanto más cerca, mucho más fuerte.
- **👹 Mobs y jugador:** al acercarse aceleran; los **mobs pegados al núcleo son devorados** (desaparecen con destello y sonido), el **jugador recibe daño continuo** que escala con la fase.
- **🌫️ Niebla oscura y gravedad:** humo que cae al centro + niebla oscura estática alrededor del agujero; **temblor de cámara** cada 0.6s en fase CORRE y cada 0.4s en el colapso (antes cada 1.5s).
- **💥 Final: colapso real** — en vez de explotar normal, el anillo se comprime, triple destello blanco, onda de energía y **BOOM con cráter decente** (radio 11) con **lava en el fondo** y **anillo de fuego**.

## v1.9.6 — Mas desastres: masivas buffeadas + Agujero Negro come MAS (solo Java)

- **🕳️ Agujero Negro come más:** radio 18 (antes 16), succion 1.0→3.0 (antes 0.8→2.2); come **hasta 22 bloques cada 6 ticks** (antes 8 cada 10) incluso obsidiana, con lava ocasional en el hueco; consume **items y orbes de XP desde más lejos** (radio 2.5); los **seres vivos pegados al núcleo son devorados** (daño continuo cada 5 ticks); explosión final de radio 9 (antes 6).
- **🌍 Terremoto:** cráter de radio 9 (antes 6) y más profundo, 12 grietas con lava hasta radio 16 (antes 8 hasta 12) + fuego en la superficie, lanza entidades a 4.5-7.5 bloques en radio 16, temblor en radio 18 y onda de choque 3D de radio 16.
- **☄️ Meteorito:** 8-12 meteoritos (antes 5-8) en área de 44 bloques, caen de más alto; cada impacto explota más fuerte (4.5-6.5), con cráter de lava de radio 3 + fuego.
- **⛈️ Tormenta:** 3 rayos inmediatos al explotar + nube durante 8 segundos (antes 6) en radio 20 (antes 16), rayos cada 14 ticks de 3-5 en racha (antes 20 ticks de 2-3), viento más fuerte.
- **🏔️ Colosal:** oleadas de radio 6/10/16 (antes 4/8/12) mucho más profundas, lanza entidades a 3-4 bloques, fuego en anillo 10-16, temblor de pantalla en cada oleada y ondas de choque 3D hasta radio 18.
- **⭐ Supernova:** 400-600 XP (antes 200-300), daño y curación en radio 14 (antes 10), 4 anillos de luz + fuego en el borde, esfera 3D que se expande más.
- **Power de explosión subido** en config: terremoto 18, meteorito 16, tormenta 12, colosal 20, supernova 17, agujero negro 10.

## v1.9.5 — Remolino del Agujero Negro con tornado REAL (CC0) (solo Java)

- **El remolino ya no es sintetizado:** la bola negra ahora suena con un **tornado real de estudio** (SSE Library: WIND, colección USC Cinema / Sunset Editorial, licencia CC0 dominio público) — viento continuo de 43s grabado con máquina de viento, bajado de tono ~0.78x para que sea un vórtice grave y amenazante.
- Se mantiene el **espiral de 5 Hz** (sensación de giro), el **sub-grave con vibrato** y la envolvente de bucle de 1.6s sin clics; el **pitch sigue subiendo con cada pulso** (0.7 + 0.15 × pulsos, en Java/Bedrock, sin tocar el código).
- Nuevo sample `tools/samples/vortex.wav` (CC0) con su crédito en `tools/samples/README.md`.

## v1.9.4 — Sonidos de estudio: samples reales CC0 + reverb + eco (solo Java)

- **Explosiones reales (dominio público / CC0):** las explosiones ahora usan **grabaciones reales de estudio** de la biblioteca "Red Library: Explosions" (colección USC Cinema / Sunset Editorial, archive.org, licencia **CC0 — sin atribución ni restricciones**, aún más permisiva que MIT) como capa de golpe y cuerpo bajo la síntesis. Cada TNT usa una muestra distinta (6 muestras en `tools/samples/` con su crédito).
- **Reverberación real (Schroeder, 4 comb + 2 allpass):** cada explosión suena en un espacio propio — las masivas retumban en una sala enorme (hasta 2.5s de cola), las pequeñas en un espacio cerrado. Las mechas también tienen una ligera sala.
- **Sub-grave descendente + transiente agudo:** golpe seco al inicio + peso grave que cae, como una explosión real.
- **Eco de cañón para las TNTs masivas:** repeticiones a 400/800/1300 ms que decaen (Terremoto, Meteorito, Tormenta, Colosal, Supernova, Nuclear…).
- **Mastering consistente:** todos los sonidos normalizados al mismo pico (0.72-0.84) con limitador suave — las masivas suenan grandes por duración/frecuencia/cola, no por recortar.
- **Mechas con chispas:** ~5 chispas/segundo (pops cortos de 9 ms) sobre el siseo, más sala ligera.
- Se regeneraron los 75 .ogg (Java + copia al RP de Bedrock, aunque Bedrock sigue congelado).
- Fix del pipeline: el `aecho` de ffmpeg atenuaba todo el sonido (out_gain global) — el eco ahora se calcula en Python solo sobre las repeticiones y se re-normaliza.

## v1.9.3 — Reacción en cadena + 3 TNTs nuevas + efectos 3D para TODAS las TNTs (solo Java)

- **Reacción en cadena:** al explotar una TNT, enciende automáticamente otras TNTs del mod cercanas (radio ~5) creando un efecto dominó espectacular.
- **☠️ TNT Tóxica:** nube verde venenosa que envenena y debilita a los seres vivos cercanos. No rompe bloques. Receta: TNT + ojo de araña + ojo de araña fermentado.
- **🎆 TNT de Fuegos Artificiales:** lanza 8-16 cohetes de colores aleatorios al cielo. No rompe bloques. Receta: TNT + fuego artificial.
- **🌌 TNT Gravitatoria:** aplasta a todas las entidades contra el suelo con una fuerza brutal, crea un cráter profundo. Receta: TNT + yunque.
- **✨ Efecto 3D genérico para TODAS las 35 TNTs:** ahora cada variante (incluyendo mini, oro, diamante, confeti, etc.) tiene un **fantasma 3D** que reproduce su propia textura de bloque, crece y gira sobre el cráter al explotar. Entidad genérica `TntBlastEntity` + renderer que usa `BlockRenderDispatcher` para dibujar el bloque real.
- 35 TNTs totales, 75 sonidos sintetizados.

## v1.9.2 — Efectos 3D para las TNTs masivas (solo Java)

- **☄️ Meteorito 3D:** ahora caen 5-8 **meteoritos visibles** (entidad propia: roca ardiente que gira y vibra mientras cae con estela de fuego y humo) y explotan al impactar dejando cráteres con lava.
- **⛈️ Nube de Tormenta 3D:** una **nube oscura visible** flota sobre la zona durante 6 segundos, invocando rayos cada 1s, empujando entidades con viento continuo y soltando lluvia + chispas eléctricas.
- **🌍 Terremoto:** **onda de choque 3D expansiva** (anillo de polvo marrón que recorre el suelo, 30 ticks) + temblor.
- **🏔️ Colosal:** **3 ondas de choque 3D** (una por oleada, colores rojo/amarillo) expandiéndose hasta radio 14.
- **⭐ Supernova:** **esfera dorada 3D pulsante** (flash → expansión → colapso en 2.5s) sobre el cráter.
- Nueva entidad genérica `CubeModel` (cubo 64x32 reutilizable) + 4 entidades + 4 renderers + 3 texturas nuevas.
- **GameTest nuevo** (`massive_3d_effects_spawn_entities`) → **"All 12 required tests passed"**.
- ⚠️ **Solo Java:** Bedrock se congela en la versión anterior (1.9.0) — se actualiza solo Java a partir de ahora.

## v1.9.1 — Fix crash (Java)

- **Arreglado el crash "Ticking entity / NullPointerException en Player.getDigSpeed"**: las TNTs masivas (Terremoto, Colosal) y el Agujero Negro usaban `getDestroyProgress(null, ...)` para comprobar si podían romper bloques — en Forge eso llama a `player.getDigSpeed(...)` y con jugador null crasheaba. Ahora usan `getDestroySpeed` (la dureza del bloque, sin jugador) y además respetan bedrock/obsidiana.
- **GameTest nuevo** (`massive_tnts_do_not_crash`) que explota Terremoto + Colosal y verifica que no crashean → **"All 11 required tests passed"**.

## v1.9.0 — Mod de Java (Forge 1.20.1) + v1.9.0 — Bedrock

### 🔧 Arreglos de la ronda anterior (5 TNTs masivas completas):
- **Versión real 1.9.0** en el jar (antes seguía diciendo 1.8.7).
- **Sonidos propios de las 5 masivas** (Terremoto, Meteorito, Tormenta, Colosal, Supernova): siseo de mecha + explosión con carácter (retumbo de terremoto, crepitar de rayos, shimmer de supernova…). 69 sonidos en total.
- **Bedrock con efectos reales de las 5 masivas**: el script ahora ejecuta terremoto (cráter + grietas con lava), lluvia de meteoritos, tormenta de rayos + viento, explosión en 3 oleadas sin lag, y supernova con XP masivo. Antes solo hacían la explosión genérica.
- **Flecha de TNT portada a Bedrock**: item propio con textura, receta (TNT + 2 palos → 2 flechas) y disparo rápido estilo flecha que explota al impacto.

### ⚖️ Presets de config (se aplican AL MOMENTO, sin reiniciar):
- Pantalla de config con 3 botones: **💥 Locura total** (poder ×3, mecha ×0.7), **⚖️ Equilibrado** (×1), **🕊️ Suave** (×0.5, mecha ×1.4). Ajustan las 32 TNTs de una vez y muestran el multiplicador activo. Singleplayer.

### 👁️ WTHIT/TOP (opcional):
- Al mirar una TNT del mod se muestran sus propiedades (radio, mecha, fuego, rompe bloques, nº de efectos).
- Al mirar una TNT ENCENDIDA se muestra el nombre y la **mecha restante en segundos**.
- Solo aparece si tienes WTHIT o TOP instalado (dependencia compileOnly, no obliga a nada).

### 🚀 Publicación:
- Repo git inicializado en `tnts_locos/` con `.gitignore` (excluye build/, run/, .gradle/ y el .mcaddon empaquetado).
- **CI en GitHub Actions** (`.github/workflows/build.yml`): compila el jar de Forge y empaqueta el addon de Bedrock en cada push, con verificación de estructura.
- `modrinth.md` listo para publicar en Modrinth/CurseForge y `README.md` raíz con la estructura del proyecto.

## v1.9.0 — Mod de Java (Forge 1.20.1) + v1.9.0 — Bedrock

### 🕳️ Agujero Negro MEGA-MEJORADO (1000% mejor):
- **Radio 16 bloques** (antes 12), **8 segundos** de duración (antes 5)
- **Vortex multicapa:** 3 anillos giratorios con colores diferentes (purpura brillante rápido, azul-violeta medio, negro-púrpura oscuro lento)
- **Rayos purpura:** cada 1.5s saltan 3-5 rayos aleatorios alrededor del vortex con destellos púrpura
- **Destrucción lenta de bloques:** come el borde del cráter (3-7 bloques cada 10 ticks), con particulas de bloque devorado
- **Succión creciente:** la fuerza escala del 0.8 al 2.2 con el tiempo (antes constante)
- **Explosión final:** al desaparecer explota una ultima vez (radio 6)
- **Sonido mejorado:** remolino grave + latidos de succión cada 1.5s
- **Temblor más fuerte:** knockback sutil + temblor cada 1.5s

### 💥 5 TNTs MASIVAS NUEVAS (destruyen mucho sin crash):
- **🌍 TNT Terremoto** (power 16): crater central + grietas con lava + lanza entidades alto + temblor fuerte
- **☄️ TNT Meteorito** (power 14): llama 5-8 meteoritos de obsidiana del cielo con estela de fuego, crateres con lava
- **⛈️ TNT Tormenta** (power 10): 12-18 rayos masivos + viento fuerte que empuja todo + lluvia de particulas
- **🏔️ TNT Colosal** (power 18): explosion en 3 oleadas progresivas (radio 4→8→12) sin lag, con fuego en bordes
- **⭐ TNT Supernova** (power 15): destello cegador + ondas de luz + 200-300 XP + daño + regeneracion + glowing

### 🖼️ Texturas mejoradas al 100% (Java + Bedrock):
- **Pico de TNT:** mango con veta, cabeza con sombreado, filo metálico, franja blanca
- **Peto de TNT:** hombreras reforzadas, correas con hebillas, logo TNT, sombreado
- **Granada:** forma esférica con sombreado, mecha detallada multi-color, chispa
- **Lanzador:** boca abierta con interior oscuro, botón con brillo, tornillos
- **Detonator:** LED verde, antena con brillo, botón grande
- **Flecha:** plumas, punta metálica, palo con veta
- **Bloques TNT:** sombreado lateral, letras TNT con brillo, remaches, gradientes
- **Bola negra 3D:** corona exterior, 3 tonos de anillo, gradiente de núcleo
- **Capa de armadura:** correas, hebillas, hombreras, sombreado

**32 TNTs totales** (27 originales + 5 masivas), todos los GameTests pasan.

## v1.8.7 — Mod de Java (Forge 1.20.1) + v1.8.6 — Bedrock

- **Remolino grave de la bola negra (sonido 59º, sintetizado):** mientras el Agujero Negro está activo suena un **retumbo grave modulado en espiral** (~5 Hz, sub-grave con vibrato, bucle de 1.6s sin clics) — se oye el vórtice girando. **Sube de tono con cada pulso** (hasta 5 pulsos), así que los últimos segundos son más intensos.
- Java: se reproduce solapado cada 10 ticks (`entity.black_hole.loop`, categoría ambient) con pitch creciente por pulso. Bedrock: mismo bucle en el script (`tnts.black_hole.loop`) con pitch creciente.
- **Tests endurecidos:** el test del lanzador falló una vez por lag del servidor ("Can't keep up! 167 ticks behind") — la entidad recién spawnada no aparecía en la comprobación síncrona del mismo tick. Ahora la munición se comprueba al instante y la entidad volando un tick después (lanzador y granada). **"All 10 required tests passed" en 2 rondas seguidas.**

## v1.8.6 — Mod de Java (Forge 1.20.1) + v1.8.5 — Bedrock

- **La bola negra pulsa:** cada segundo lanza un **anillo de energía púrpura expansivo** (polvo púrpura + portal) que crece desde el núcleo hasta el borde del radio durante ~15 ticks, con hasta 2-3 anillos activos a la vez.
- **Temblor de pantalla:** al lanzar cada pulso, los jugadores dentro del radio sienten un **leve temblor de cámara** (la animación de golpe de vanilla vía `animateHurt`, sin daño ni overlay rojo — verificado que Gui no lee `hurtTime` en 1.20.1). Solo Java: Bedrock no expone temblor de cámara por Script API (el anillo púrpura sí está en ambos).
- GameTests: **"All 10 required tests passed"** (la bola negra completa los 5s con pulsos y temblor sin errores).

## v1.8.5 — Mod de Java (Forge 1.20.1) + v1.8.4 — Bedrock

- **Bola negra 3D de verdad (Agujero Negro):** al explotar, una **entidad visible** con forma de esfera oscura (anillo púrpura, núcleo negro) flota sobre el cráter **5 segundos** atrayendo entidades e items — con vórtice en espiral, humo tragado al centro y los items que caen al núcleo consumidos con un destello. En Java es una entidad propia con renderer y modelo 3D (cubo con textura de esfera, gira lentamente); en Bedrock una entidad personalizada (`tnts:black_hole`) con geometría y textura propias en el RP.
- **TNT Inferno invoca mobs del Nether:** al explotar aparecen de 3 a 6 mobs hostiles (blaze, cubo de magma, piglin zombificado y esqueleto wither) alrededor del cráter, con partículas de invocación, que **atacan al jugador más cercano**. ¡Te van a dar susto de verdad! 😄
- GameTests: **"All 10 required tests passed"** (el Agujero Negro con la entidad nueva completa los 5s de succión sin errores).

## v1.8.4 — Mod de Java (Forge 1.20.1) + v1.8.3 — Bedrock

- **Agujero Negro con cráter persistente:** ya no es un empujón de un instante — durante **3 segundos** (60 ticks) el cráter **succiona entidades E items** hacia el centro con fuerza creciente, con un **vórtice en espiral** de partículas de portal girando, humo tragado hacia el núcleo y un núcleo oscuro pulsante.
- **Los items que caen al núcleo son consumidos** con un destello (adiós a lo que se acerque demasiado).
- Java: tarea programada tick a tick (misma técnica de siempre, ahora 60 ticks + espiral + consumo). Bedrock: `blackholeSuck` ahora usa un `runInterval` de 60 ticks con el mismo vórtice y consumo.
- GameTest nuevo: **"All 10 required tests passed"** (el Agujero Negro explota y completa los 3s de succión sin errores).

## v1.8.3 — Mod de Java (Forge 1.20.1) + v1.8.2 — Bedrock

- **Explosiones con personalidad propia:** cada TNT explota con un **estallido de polvo de su color** (ninguna se ve igual) + partículas características:
  - **Mega:** ondas de choque concéntricas + flash gigante. **Nuclear:** hongo de seta (columna de humo + copa de nubes + resplandor verde radiactivo).
  - **Lava e Inferno:** lluvia de lava + llamas. **Hielo/Criogénica:** flash blanco + nevada. **Agua:** salpicaduras y burbujas. **Rayo:** chispas eléctricas. **Oro/Diamante/Esmeralda:** lluvia de partículas del propio item. **XP:** brillos verdes.
  - **Agujero Negro:** implosión (humo + vórtice púrpura). **Viento:** ráfagas de nubes. **Curativa:** corazones + tótem. **Teletransportadora:** vórtice de portal. **Arena:** polvo de arena cayendo. **Saltarina:** pompas. **Setas:** esporas rojas/marrones. **Miel:** goteo de miel. **Obsidiana:** púrpura oscuro. **Instantánea:** flash seco. **Confeti:** ya tenía su lluvia de colores.
- **Comportamiento diferenciado:** la **TNT de Viento ya no rompe bloques** (solo empuja) y la **Curativa es un estallido suave** (radio pequeño, no rompe, cura a todos) — dos estilos que antes eran idénticos a las demás.
- Bedrock: la misma variedad por script (`variantFx` con partículas propias por variante) + config sincronizada (viento sin romper, cura suave).
- GameTests: **"All 9 required tests passed"**.

## v1.8.2 — Mod de Java (Forge 1.20.1) + v1.8.1 — Bedrock

- **Mecha viva:** las TNT encendidas ahora sueltan **humo denso** (3 nubes por tick) y **chispas** (llamas pequeñas + llama grande) concentradas en la cara superior, en el centro del bloque — se ve arder desde lejos.
- **Punto de luz rojo parpadeante** sobre la cara superior (4 ticks encendido / 4 apagado): en Java con partícula de polvo rojo (`DustParticleOptions`), en Bedrock con `redstone_wire_dust_particle`.
- GameTests: **"All 9 required tests passed"**.

## v1.8.1 — Mod de Java (Forge 1.20.1) + v1.8.0 — Bedrock

- **Las TNT ya NO se mueven al encenderse:** quedan clavadas en su sitio (sin gravedad, sin empujes de agua ni de explosiones) — puedes encender una hilera y no se descolocan. Solo las lanzadas (lanzador, granada, flecha) conservan la física de proyectil. Nuevo GameTest que lo verifica: **"All 9 required tests passed"**.
- **Texturas y modelos estilo TNT de vanilla:** modelos `cube_bottom_top` (antes `cube_all`) con **cara superior con las letras TNT**, **cara inferior lisa** y lateral con la franja clásica. Las encendidas animan la llama en el lateral Y en la cara superior.
- **Peto de TNT arreglado en 3ª persona:** la capa de armadura ahora se busca en `tnts:textures/...` (el nombre del material lleva namespace) y está dibujada en las **regiones correctas** del lienzo 64x32 (cuerpo y brazos, no las piernas).
- **Granada con textura:** la granada de Java no tenía textura ni modelo (se veía como textura faltante) — añadida su textura pixel-art (bola TNT con mecha encendida) y su modelo de item.
- **Bedrock:** mismas caras superior/inferior (material_instances `up`/`down` en los 27 bloques), flipbook de la cara superior encendida, y la capa de armadura del peto con el nombre correcto para Bedrock (`tnt_chestplate_layer_1.png`).

## v1.8.0 — Mod de Java (Forge 1.20.1)

- **Bunker de TNT abandonado** (estructura generada en el mundo): sala subterránea de ladrillo de piedra con **TNTs escondidas en las paredes** (cada 3 bloques), **3 minas** en el suelo, antorchas y un **cofre con provisiones** (TNTs, pólvora e hierro). Se genera en el Overworld cada ~24 chunks (structure set `tnts:tnts_bunkers`). ¡Búscalo con `/locate structure tnts:tnts_bunker`!
- **8 GameTests** (antes 2): peto reactivo (empuja al atacante), pico explosivo (el bloque minado explota), lanzador (consume TNT y dispara), granada (se lanza y consume), detonador (enciende 10+ de golpe) y registro del bunker. **"All 8 required tests passed"** — y de paso cazó un bug real del test del lanzador (el slot de la mano ocupaba el slot 0).

## v1.7.0 — Bedrock

- **Bunker de TNT** generado por script: una vez por mundo, a 250-400 bloques del spawn se construye la misma sala subterránea (TNTs en las paredes, 3 minas, antorchas y cofre con loot). Persistente con dynamic property (no se duplica).

## v1.6.3 — Bedrock

- **Lanzador de TNT:** consume una TNT del mod de tu inventario (¡cualquier variante!) y la dispara con física propia (gravedad, rebotes y estela de partículas) por script. Sin munición → click seco. Receta: 2 hierro + TNT (igual que Java).
- **Granada de TNT:** se lanza con mecha de 1.5s, rebota y explota como la Mini TNT. Receta: TNT + cuerda → 2.
- Refactor: los efectos especiales viven ahora en `applyEffects`/`explodeAt` compartidos por bloques y proyectiles (una sola fuente de verdad).
- Texturas propias del lanzador (tubo de hierro con botón rojo) y de la granada (bola TNT con mecha encendida), idiomas español/inglés.

## v1.6.2 — Bedrock

- **Peto de TNT portado:** componente `wearable` (slot pecho) + `armor` (6 de protección) + reparación con TNT; por script, al recibir daño **empuja al atacante** con destello de llamas y pitido (cooldown de 30 ticks).
- **Pico de TNT portado:** componente `digger` (velocidad de diamante) + `durability` (800); por script, **cada bloque roto explota** (radio 2, rompe bloques).
- Texturas propias (item + capa de armadura), recetas (8 TNT → peto; 3 TNT + 2 palos → pico) e idiomas español/inglés.

## v1.7.2 — Mod de Java (Forge 1.20.1)

- **Peto de TNT** (armadura reactiva): al recibir daño, empuja al atacante lejos con un destello de llamas y un pitido. Resistencia de hierro, se repara con TNT, textura de capa propia (torso rojo con franja blanca). Receta: 8 TNT en forma de peto.
- **Pico de TNT**: cada bloque que rompes explota (radio 2) — ¡cuidado con lo que hay alrededor y con encadenar TNTs! Receta: 3 TNT + 2 palos.
- Ambos con textura, modelo, receta e idiomas español/inglés; están en la pestaña creativa *TNTs Locas*.
- GameTests siguen pasando ("All 2 required tests passed").

## v1.7.1 — Mod de Java (Forge 1.20.1)

- **Título + contador en pantalla** al detonar 10+ TNTs de golpe con el Detonador: "¡Detonación masiva!" en grande con subtítulo "N TNTs encendidas de golpe" (paquetes de título en 1.20.1).
- **Cuenta atrás sonora para la Mina TNT:** la mina ahora pita con urgencia (beeps cada 2 ticks, tono ascendente) antes de explotar. Los pitidos de la TNT Trampa también se aceleran de forma gradual (intervalo adaptativo).

## v1.6.1 — Bedrock

- Mismo feedback: título "¡Detonación masiva!" con contador (`onScreenDisplay.setTitle`) al detonar 10+ y beeps de cuenta atrás en la mina (efecto `trap` + intervalo adaptativo).

## v1.7.0 — Mod de Java (Forge 1.20.1)

- **10 TNTs nuevas (26 en total):** de Diamante (suelta diamantes), de Esmeralda (suelta esmeraldas), Agujero Negro (succiona entidades e items hacia el cráter durante 1s), de Viento (empuja todo lejos), Inferno (incendia una zona enorme), de Setas (esparce setas y micelio), de Miel (lentitud pegajosa + bloques de miel), Curativa (cura + regeneración), Teletransportadora (manda a los seres vivos a sitios aleatorios) y de Confeti (lluvia de partículas de colores).
- **Mina TNT** (bloque nº27): explota en cuanto un ser vivo la pisa (también con mechero/redstone/cadena).
- **Granada de TNT** (item): se lanza con mecha de 1.5s, rebota y explota. Receta: TNT + cuerda → 2.
- **Advancement nuevo:** "¡Detonación masiva!" (trigger `tnts:mass_detonated`) al detonar 10+ de golpe con el detonador. El reto "Colección explosiva" ahora cubre las 27.
- 10 efectos nuevos (DIAMOND, EMERALD, BLACKHOLE, WIND, INFERNO, FUNGI, HONEY, HEAL, TELEPORT, CONFETTI) + 22 sonidos nuevos (58 en total), incluido el glissando "warp" de la teletransportadora.
- GameTests siguen pasando: "All 2 required tests passed".

## v1.6.0 — Bedrock

- **Las mismas 10 TNTs nuevas + Mina TNT** (27 bloques): diamante, esmeralda, agujero negro, viento, inferno, setas, miel, curativa, teletransportadora y confeti, con sus recetas y efectos en script (la mina explota si un ser vivo está encima, por sondeo).
- **Bug arreglado:** la textura de la TNT Limpia estaba dentro de un comentario del generador y nunca se generaba → ahora sí tiene textura.
- Sonidos nuevos incluidos (58 .ogg) y terrain_texture regenerado para las 27.

## v1.6.1 — Mod de Java (Forge 1.20.1)

- **Feedback del Detonador Remoto:** cada TNT encendida suelta un destello de partículas (FLASH + varitas), y al detonar **10 o más** de golpe suena una **ráfaga propia** (triple click + boom) con un **anillo de polvo rojo** alrededor del jugador.
- Sonido nuevo sintetizado: `item.detonator.burst` (36 .ogg en total).

## v1.5.1 — Bedrock

- **Mismo feedback en el Detonador:** destello (humo + llama) en cada TNT encendida y ráfaga `tnts.detonator.burst` al detonar 10+ de golpe.

## v1.6.0 — Mod de Java (Forge 1.20.1)

- **TNTs con física real:** al encenderse ya no son bloques estáticos, ahora son **entidades** (como la TNT de vanilla): caen por gravedad, rebotan, las empuja el agua y las lanzan las explosiones. Todos los efectos (lava, nieve, rayos, oro, XP, obsidiana…) se conservan.
- **Sonidos propios sintetizados** (35 .ogg generados con Python + ffmpeg): siseo de mecha distinto por variante, explosiones con carácter (retumbo nuclear, cristal de hielo, ding de oro, trueno de rayo, chapoteo de agua…), pitido de cuenta atrás de la Trampa, click del detonador y disparo del lanzador.
- **Advancements:** árbol completo — "¡TNTs Locas!" (raíz), "¡A la mecha!" (encender la primera), 16 logros de crafteo (uno por TNT) y el reto oculto "Colección explosiva" (explotar una de cada tipo). Triggers personalizados `tnts:ignited` y `tnts:exploded`.
- **Lanzador de TNT** (item): consume una TNT de tu inventario (cualquier variante) y la dispara con física. Receta: 2 hierro + TNT.
- **Flecha de TNT** (item): proyectil que explota al impactar. Receta: TNT + 2 palos → 2 flechas.
- **Pantalla de config en el menú de mods**: muestra los valores actuales de cada TNT y botón para abrir la carpeta de config.
- GameTests actualizados a entidades: **"All 2 required tests passed"** en servidor dedicado.
- Bug cazado en runtime: el lambda del config screen cargaba `Screen` en el servidor dedicado → movido a `ClientSetup` vía `DistExecutor`.

## v1.5.0 — Bedrock

- **Sonidos propios** (los mismos 35 .ogg sintetizados que Java): mecha por variante, explosiones con carácter, pitido de trampa, click del detonador. Añadidos `sound_definitions.json` y llamadas `playSound` en el script.

## v1.5.0 — Mod de Java (Forge 1.20.1)

- **5 TNTs nuevas (16 en total):** TNT de Obsidiana (cráter blindado), TNT Criogénica (congela jugadores y mobs), TNT de Experiencia (suelta orbes de XP), TNT de Agua (inunda y apaga incendios) y TNT de Arena (avalancha de arena).
- **Refactor a `EnumSet<TntEffect>`** para los efectos: más limpio y fácil de ampliar.
- **Animación de 6 frames** (antes 4) con la llama parpadeando de forma irregular + **flash blanco** en los últimos ticks.
- **Detonador Remoto optimizado:** radio 24 con rango vertical acotado y solo chunks cargados (antes escaneaba ~140.000 bloques; ahora ~40.000).
- **GameTests automatizados:** `./gradlew runGameTestServer` verifica que la TNT explota al pasar la mecha y que solo se enciende con mechero. **Ambos tests pasan.**
- **Icono del mod** (`logoFile` en mods.toml) + **modrinth.md** para publicar en Modrinth.
- Verificado en runtime: estructura NBT gzip correcta (bug cazado y arreglado) y coordenadas absolutas en los tests (bug cazado y arreglado).

## v1.4.0 — Bedrock

- **Paridad con Java:** 5 TNTs nuevas (Obsidiana, Criogénica, XP, Agua, Arena) → 16 en total.
- **Detonador Remoto** (item): enciende todas las TNTs en 24 bloques con un click. Receta: hierro + redstone + TNT.
- **Flipbook animado:** la mecha encendida es una tira de 6 frames que parpadea (antes frame estático).
- Efectos nuevos: obsidiana, congelación, XP, agua y avalancha de arena.

## v1.4.0 — Mod de Java (Forge 1.20.1)

- **Config en juego:** `config/tnts-common.toml` con radio, mecha, fuego, romper bloques y efectos por TNT (se aplica al recargar).
- **TNT de Lava** ahora deja charcos de lava reales en el cráter; **TNT de Hielo** cubre la zona de nieve; **TNT Saltarina** lanza también mobs.
- **Detonador Remoto** (item): enciende todas las TNTs en 32 bloques. Receta: hierro + redstone + TNT.
- **Flash blanco** justo antes de explotar y **pitidos acelerados** de la TNT Trampa.
- **Recetas con tags** de Forge (hierro/oro/redstone/slime/pólvora/netherita aceptan variantes de otros mods) + README.
- **Verificado en runtime:** servidor Forge headless arrancó y cargó el mod sin errores (esto cazó y arregló un bug de la config antes de entregarlo).

## v1.3.0 — Bedrock

- **Paridad con Java:** 3 TNTs nuevas (Rayo, Trampa, Oro) → 11 en total.
- Solo se encienden con **mechero** o carga de fuego (adiós a encenderlas al romperlas).
- TNT de Lava deja lava real, TNT de Hielo cubre de nieve, TNT de Oro suelta lingotes, TNT de Rayo invoca rayos, TNT Trampa pita.
- Mechas más largas (1 a 4 segundos).

## v1.3.0 — Mod de Java (Forge 1.20.1)

- **Arreglo de texturas:** los PNG se generaban con 3 bytes por píxel pero declarados RGBA → Minecraft los rechazaba. Ahora son RGBA válidos.
- **Solo mechero:** las TNTs solo se encienden con mechero (flint & steel) o carga de fuego; ya no explotan al romperlas (se rompen normal y sueltan el item).
- **3 TNTs nuevas (11 en total):** TNT de Rayo (invoca rayos), TNT Trampa (mecha de 4s), TNT de Oro (suelta lingotes de oro).
- **Animaciones:** la textura encendida es una animación de 4 frames con la llama parpadeando, más humo denso y chispas mientras arde.
- **Mecha mas larga:** 1 a 4 segundos segun la TNT (antes 0.2-0.8s).
- Recetas nuevas: Rayo (TNT + pararrayos), Trampa (TNT + reloj), Oro (TNT + lingote de oro).

## v1.2.1 — Mod de Java (Forge 1.20.1)

- **Arreglo del crash "Registry is already frozen"**: los bloques ahora se crean de forma perezosa dentro del `DeferredRegister` (antes se instanciaban al cargar la clase, antes de que el registro estuviera abierto).
- Recompilado e instalado en `mods/` como `tnts-1.2.1.jar`.

## v1.2.0 — Mod de Java (Forge 1.20.1)

- **Nuevo:** versión para Minecraft **Java 1.20.1 con Forge** (mod completo compilado en `tnts_forge/build/libs/tnts-1.2.0.jar`).
- Mismas 8 TNTs que en Bedrock, con pestaña creativa propia, recetas y español/inglés.
- En Java: se encienden al hacer click derecho, con redstone, al romperlas y en cadena.
- La TNT de Hielo congela el agua, la Saltarina lanza jugadores, la Nuclear hace lluvia de partículas y la Limpia no rompe bloques.
- Requiere Forge 47.x (1.20.1) y Java 17. Recompilar: `./gradlew build`.

## v1.2.1 — Bedrock

- Arreglo de texturas (PNG RGBA válidos) — mismo bug que el mod de Java.

## v1.2.0 — Bedrock
- **Arreglo importante:** los bloques ahora usan el sistema moderno de bloques de Bedrock (componentes personalizados `tnts:ignite` + formato 1.21.90). El sistema viejo (`minecraft:on_interact`) se eliminó en Minecraft 1.21.x, por eso no cargaban los bloques.
- Requiere **Minecraft Bedrock 1.21.90 o superior** y Script API 2.0.0.
- Nueva TNT: **TNT Limpia** (no rompe bloques, solo daña entidades). Ya son 8.
- **Mecha visible:** las TNTs cambian a textura encendida con llamas mientras arde la mecha.
- **Redstone:** se encienden con señal de redstone (componente `minecraft:redstone_consumer` + sondeo de respaldo).
- **`/function tnts:give_all`:** te da las 8 TNTs de golpe.
- **Configuración editable:** radio, fuego, romper bloques, mecha y efectos especiales por TNT en `scripts/main.js` (sección CONFIG).
- Texturas mejoradas estilo vanilla (franja clara, letras TNT, polvora) + variantes encendidas.

## v1.1.0
- Baja el requisito de motor a 1.20.0 y Script API 1.4.0 para intentar compatibilidad.
- Añade TNT de Hielo, TNT Saltarina y TNT Nuclear (7 TNTs en total).

## v1.0.0
- Primera versión: Mega TNT, Mini TNT, TNT de Lava y TNT Instantánea (4 TNTs).
