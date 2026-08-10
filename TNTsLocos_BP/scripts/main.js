// ============================================================
//  TNTs Locos - script principal (v1.7.0)
//  Requiere Minecraft Bedrock 1.21.90 o superior.
// ============================================================
import { world, system, ItemStack } from "@minecraft/server";

// ============================================================
//  CONFIGURACION: edita aqui los valores de cada TNT
//  power         -> radio de la explosion
//  fire          -> incendia bloques
//  breaksBlocks  -> false = no rompe bloques (solo daña)
//  fuse          -> mecha en ticks (20 ticks = 1 segundo)
//  efectos: ice, snow, lava, launch, nuclear, lightning, gold,
//           trap, obsidian, cryo, xp, water, sand
// ============================================================
const CONFIG = {
  "tnts:mega_tnt":      { power: 12,  fire: false, breaksBlocks: true,  fuse: 40, effects: [] },
  "tnts:mini_tnt":      { power: 2.5, fire: false, breaksBlocks: true,  fuse: 30, effects: [] },
  "tnts:lava_tnt":      { power: 6,   fire: true,  breaksBlocks: true,  fuse: 40, effects: ["lava"] },
  "tnts:rapida_tnt":    { power: 4,   fire: false, breaksBlocks: true,  fuse: 20, effects: [] },
  "tnts:hielo_tnt":     { power: 4,   fire: false, breaksBlocks: true,  fuse: 40, effects: ["ice", "snow"] },
  "tnts:saltarina_tnt": { power: 1.5, fire: false, breaksBlocks: true,  fuse: 30, effects: ["launch"] },
  "tnts:nuclear_tnt":   { power: 20,  fire: true,  breaksBlocks: true,  fuse: 50, effects: ["nuclear"] },
  "tnts:limpia_tnt":    { power: 5,   fire: false, breaksBlocks: false, fuse: 40, effects: [] },
  "tnts:rayo_tnt":      { power: 4,   fire: false, breaksBlocks: true,  fuse: 40, effects: ["lightning"] },
  "tnts:trampa_tnt":    { power: 5,   fire: false, breaksBlocks: true,  fuse: 80, effects: ["trap"] },
  "tnts:oro_tnt":       { power: 6,   fire: false, breaksBlocks: true,  fuse: 40, effects: ["gold"] },
  "tnts:obsidiana_tnt": { power: 8,   fire: false, breaksBlocks: true,  fuse: 40, effects: ["obsidian"] },
  "tnts:crio_tnt":      { power: 3,   fire: false, breaksBlocks: true,  fuse: 40, effects: ["cryo"] },
  "tnts:xp_tnt":        { power: 4,   fire: false, breaksBlocks: true,  fuse: 40, effects: ["xp"] },
  "tnts:agua_tnt":      { power: 5,   fire: false, breaksBlocks: true,  fuse: 40, effects: ["water"] },
  "tnts:arena_tnt":     { power: 4,   fire: false, breaksBlocks: true,  fuse: 40, effects: ["sand"] },
  "tnts:diamante_tnt":  { power: 6,   fire: false, breaksBlocks: true,  fuse: 40, effects: ["diamond"] },
  "tnts:esmeralda_tnt": { power: 6,   fire: false, breaksBlocks: true,  fuse: 40, effects: ["emerald"] },
  "tnts:negra_tnt":     { power: 8,   fire: false, breaksBlocks: true,  fuse: 40, effects: ["blackhole"] },
  "tnts:viento_tnt":    { power: 4,   fire: false, breaksBlocks: false, fuse: 30, effects: ["wind"] },
  "tnts:inferno_tnt":   { power: 7,   fire: true,  breaksBlocks: true,  fuse: 40, effects: ["inferno"] },
  "tnts:hongo_tnt":     { power: 4,   fire: false, breaksBlocks: true,  fuse: 40, effects: ["fungi"] },
  "tnts:miel_tnt":      { power: 4,   fire: false, breaksBlocks: true,  fuse: 40, effects: ["honey"] },
  "tnts:heal_tnt":      { power: 1.5, fire: false, breaksBlocks: false, fuse: 40, effects: ["heal"] },
  "tnts:teleport_tnt":  { power: 4,   fire: false, breaksBlocks: true,  fuse: 40, effects: ["teleport"] },
  "tnts:confeti_tnt":   { power: 3,   fire: false, breaksBlocks: true,  fuse: 30, effects: ["confetti"] },
  "tnts:mina_tnt":      { power: 3,   fire: false, breaksBlocks: true,  fuse: 10, effects: ["trap"] },
  "tnts:terremoto_tnt": { power: 16,  fire: false, breaksBlocks: true,  fuse: 60, effects: ["earthquake"] },
  "tnts:meteorito_tnt": { power: 14,  fire: true,  breaksBlocks: true,  fuse: 55, effects: ["meteor"] },
  "tnts:tormenta_tnt":  { power: 10,  fire: true,  breaksBlocks: true,  fuse: 50, effects: ["storm"] },
  "tnts:colosal_tnt":   { power: 18,  fire: true,  breaksBlocks: true,  fuse: 70, effects: ["colossal"] },
  "tnts:supernova_tnt": { power: 15,  fire: true,  breaksBlocks: true,  fuse: 65, effects: ["supernova"] }
};

const igniting = new Set(); // posiciones que ya estan encendidas

function keyOf(block) {
  const p = block.location;
  return block.dimension.id + ":" + Math.floor(p.x) + "," + Math.floor(p.y) + "," + Math.floor(p.z);
}

function hasEffect(config, name) {
  return config.effects && config.effects.indexOf(name) !== -1;
}

function variantOf(typeId) {
  return typeId.replace("tnts:", "");
}

function setLit(block, lit) {
  try {
    if (block.setPermutation && block.permutation && block.permutation.withState) {
      block.setPermutation(block.permutation.withState("tnts:lit", lit));
    }
  } catch (e) {}
}

// Solo se enciende con mechero o carga de fuego en la mano
function hasFlint(player) {
  try {
    if (!player) return false;
    const inv = player.getComponent("minecraft:inventory");
    const item = inv && inv.container ? inv.container.getItem(player.selectedSlotIndex) : undefined;
    return !!item && (item.typeId === "minecraft:flint_and_steel" || item.typeId === "minecraft:fire_charge");
  } catch (e) {
    return false;
  }
}

// Explosion con todos los efectos de una variante, en cualquier punto
function explodeAt(dimension, pos, typeId) {
  const config = CONFIG[typeId];
  if (!config) return;
  const variant = variantOf(typeId);
  try {
    dimension.createExplosion(pos, config.power, {
      causesFire: config.fire,
      breaksBlocks: config.breaksBlocks
    });
  } catch (e) {}
  variantFx(dimension, pos, typeId);
  try {
    dimension.playSound("tnts.explode." + variant, pos, { volume: 2.0, pitch: 1.0 });
  } catch (e) {}
  applyEffects(dimension, pos, config);
}

// Explosion con personalidad propia de cada variante (espectaculo visual unico)
function variantFx(dimension, pos, typeId) {
  const variant = variantOf(typeId);
  const c = { x: pos.x + 0.5, y: pos.y + 0.5, z: pos.z + 0.5 };
  const rand = (n) => (Math.random() - 0.5) * n;
  const burst = (name, count, spread, dy) => {
    for (let i = 0; i < count; i++) {
      try {
        dimension.spawnParticle(name, {
          x: c.x + rand(spread),
          y: c.y + dy + Math.random() * spread * 0.5,
          z: c.z + rand(spread)
        });
      } catch (e) {}
    }
  };
  try {
    switch (variant) {
      case "mega_tnt":
        dimension.spawnParticle("minecraft:huge_explosion_emitter", c);
        burst("minecraft:explosion_particle", 40, 14, 0);
        break;
      case "nuclear_tnt":
        dimension.spawnParticle("minecraft:huge_explosion_emitter", c);
        burst("minecraft:basic_smoke_particle", 50, 4, 2);
        burst("minecraft:basic_flame_particle", 25, 10, 1);
        break;
      case "lava_tnt":
      case "inferno_tnt":
        burst("minecraft:lava_particle", 40, 10, 3);
        burst("minecraft:basic_flame_particle", 30, 8, 1);
        break;
      case "hielo_tnt":
      case "crio_tnt":
        burst("minecraft:snowflake_particle", 40, 8, 1);
        burst("minecraft:white_smoke_particle", 10, 6, 0);
        break;
      case "agua_tnt":
        burst("minecraft:water_splash_particle", 40, 8, 1);
        burst("minecraft:water_wake_particle", 20, 8, 0);
        break;
      case "rayo_tnt":
        burst("minecraft:electric_spark_particle", 35, 8, 1);
        burst("minecraft:basic_flame_particle", 10, 6, 0);
        break;
      case "oro_tnt":
        burst("minecraft:redstone_wire_dust_particle", 25, 6, 1);
        burst("minecraft:endrod_particle", 20, 6, 1);
        break;
      case "diamante_tnt":
        burst("minecraft:endrod_particle", 30, 7, 1);
        burst("minecraft:white_smoke_particle", 15, 6, 1);
        break;
      case "esmeralda_tnt":
      case "xp_tnt":
        burst("minecraft:happy_villager_particle", 30, 7, 1);
        burst("minecraft:endrod_particle", 15, 6, 1);
        break;
      case "negra_tnt":
        burst("minecraft:basic_smoke_particle", 40, 9, 1);
        burst("minecraft:portal_particle", 30, 5, 1);
        break;
      case "viento_tnt":
        burst("minecraft:basic_smoke_particle", 40, 16, 0);
        burst("minecraft:white_smoke_particle", 20, 12, 0);
        break;
      case "heal_tnt":
        burst("minecraft:heart_particle", 25, 6, 1);
        burst("minecraft:totem_of_undying_particle", 20, 6, 1);
        break;
      case "teleport_tnt":
        burst("minecraft:portal_particle", 50, 6, 1);
        break;
      case "arena_tnt":
        burst("minecraft:basic_smoke_particle", 40, 8, 2);
        burst("minecraft:white_smoke_particle", 15, 8, 0);
        break;
      case "saltarina_tnt":
        burst("minecraft:explosion_particle", 30, 5, 0);
        break;
      case "hongo_tnt":
        burst("minecraft:redstone_wire_dust_particle", 20, 7, 1);
        burst("minecraft:basic_smoke_particle", 20, 7, 1);
        break;
      case "miel_tnt":
        burst("minecraft:endrod_particle", 30, 7, 1);
        burst("minecraft:redstone_wire_dust_particle", 15, 5, 1);
        break;
      case "obsidiana_tnt":
        burst("minecraft:redstone_wire_dust_particle", 25, 8, 1);
        burst("minecraft:basic_smoke_particle", 20, 6, 1);
        break;
      case "rapida_tnt":
        burst("minecraft:white_smoke_particle", 30, 6, 0);
        burst("minecraft:explosion_particle", 15, 4, 0);
        break;
      case "confeti_tnt":
        burst("minecraft:happy_villager_particle", 40, 8, 2);
        burst("minecraft:endrod_particle", 25, 8, 2);
        break;
      case "terremoto_tnt":
        dimension.spawnParticle("minecraft:huge_explosion_emitter", c);
        burst("minecraft:block_destruct_particle", 60, 12, 1);
        burst("minecraft:basic_smoke_particle", 30, 12, 1);
        break;
      case "meteorito_tnt":
        burst("minecraft:basic_flame_particle", 50, 12, 2);
        burst("minecraft:lava_particle", 30, 8, 1);
        burst("minecraft:basic_smoke_particle", 40, 10, 2);
        break;
      case "tormenta_tnt":
        dimension.spawnParticle("minecraft:huge_explosion_emitter", c);
        burst("minecraft:electric_spark_particle", 50, 14, 1);
        burst("minecraft:basic_flame_particle", 20, 12, 1);
        break;
      case "colosal_tnt":
        dimension.spawnParticle("minecraft:huge_explosion_emitter", c);
        burst("minecraft:explosion_particle", 70, 18, 0);
        burst("minecraft:basic_smoke_particle", 50, 16, 1);
        break;
      case "supernova_tnt":
        burst("minecraft:endrod_particle", 60, 14, 2);
        burst("minecraft:happy_villager_particle", 40, 12, 1);
        burst("minecraft:white_smoke_particle", 30, 10, 1);
        break;
      default:
        burst("minecraft:explosion_particle", 15, 4, 0);
    }
  } catch (e) {}
}

// Efectos especiales de cada variante (compartidos por bloques y proyectiles)
function applyEffects(dimension, pos, config) {
  if (hasEffect(config, "ice")) freezeWater(dimension, pos, 7);
  if (hasEffect(config, "snow")) snowCover(dimension, pos, 7);
  if (hasEffect(config, "lava")) lavaPools(dimension, pos, 4);
  if (hasEffect(config, "launch")) launchPlayers(dimension, pos, 9);
  if (hasEffect(config, "nuclear")) nuclearShow(dimension, pos);
  if (hasEffect(config, "lightning")) strikeLightning(dimension, pos);
  if (hasEffect(config, "gold")) dropGold(dimension, pos);
  if (hasEffect(config, "obsidian")) obsidianCrater(dimension, pos, 4);
  if (hasEffect(config, "cryo")) cryoFreeze(dimension, pos, 8);
  if (hasEffect(config, "xp")) xpDrop(dimension, pos);
  if (hasEffect(config, "water")) waterFlood(dimension, pos, 4);
  if (hasEffect(config, "sand")) sandFall(dimension, pos, 5);
  if (hasEffect(config, "diamond")) dropLoot(dimension, pos, "minecraft:diamond", 3 + Math.floor(Math.random() * 3));
  if (hasEffect(config, "emerald")) dropLoot(dimension, pos, "minecraft:emerald", 8 + Math.floor(Math.random() * 8));
  if (hasEffect(config, "blackhole")) blackholeSuck(dimension, pos, 12);
  if (hasEffect(config, "wind")) windPush(dimension, pos, 9);
  if (hasEffect(config, "inferno")) inferno(dimension, pos, 8);
  if (hasEffect(config, "fungi")) fungiSpread(dimension, pos, 6);
  if (hasEffect(config, "honey")) honeyGoo(dimension, pos, 8);
  if (hasEffect(config, "heal")) healBurst(dimension, pos, 10);
  if (hasEffect(config, "teleport")) teleportScramble(dimension, pos, 12);
  if (hasEffect(config, "confetti")) confetti(dimension, pos);
  if (hasEffect(config, "earthquake")) earthquake(dimension, pos);
  if (hasEffect(config, "meteor")) meteorShower(dimension, pos);
  if (hasEffect(config, "storm")) massiveStorm(dimension, pos);
  if (hasEffect(config, "colossal")) colossalExplosion(dimension, pos);
  if (hasEffect(config, "supernova")) supernova(dimension, pos);
}

function igniteTnt(block) {
  const config = CONFIG[block.typeId];
  if (!config) return;

  const key = keyOf(block);
  if (igniting.has(key)) return;
  igniting.add(key);

  const { x, y, z } = block.location;
  const dimension = block.dimension;
  const pos = { x, y, z };
  const typeId = block.typeId; // guardar antes de convertirla en aire
  const variant = variantOf(typeId);

  try {
    dimension.playSound("tnts.fuse." + variant, pos, { volume: 1.0, pitch: 1.0 });
  } catch (e) {}
  setLit(block, true); // textura encendida animada (flipbook)

  let remaining = config.fuse;
  const loop = system.runInterval(() => {
    remaining--;
    if (remaining <= 0) {
      system.clearRun(loop);
      igniting.delete(key);
      try {
        block.setType("minecraft:air");
      } catch (e) {}
      explodeAt(dimension, pos, typeId);
      return;
    }

    // mecha: chispas + humo DENSOS en la cara superior (centro del bloque)
    try {
      dimension.spawnParticle("minecraft:basic_smoke_particle", {
        x: x + 0.5 + (Math.random() - 0.5) * 0.5,
        y: y + 0.95,
        z: z + 0.5 + (Math.random() - 0.5) * 0.5
      });
      dimension.spawnParticle("minecraft:basic_smoke_particle", {
        x: x + 0.5 + (Math.random() - 0.5) * 0.5,
        y: y + 0.9 + Math.random() * 0.2,
        z: z + 0.5 + (Math.random() - 0.5) * 0.5
      });
      dimension.spawnParticle("minecraft:small_flame_particle", {
        x: x + 0.5 + (Math.random() - 0.4) * 0.4,
        y: y + 0.95,
        z: z + 0.5 + (Math.random() - 0.4) * 0.4
      });
      // punto de luz rojo que parpadea sobre la cara superior
      if (Math.floor(remaining / 4) % 2 === 0) {
        dimension.spawnParticle("minecraft:redstone_wire_dust_particle", {
          x: x + 0.5, y: y + 1.05, z: z + 0.5
        });
      }
    } catch (e) {}

    // flash blanco justo antes de explotar
    if (remaining <= 8) {
      try {
        dimension.spawnParticle("minecraft:white_smoke_particle", pos);
      } catch (e) {}
    }

    // pitidos de cuenta atras que se aceleran (TNT Trampa y Mina)
    if (hasEffect(config, "trap") && remaining <= 20 && remaining % Math.max(2, Math.floor(remaining / 4)) === 0) {
      try {
        dimension.playSound("tnts.beep", pos, { volume: 1.0, pitch: 1 + (20 - remaining) * 0.06 });
      } catch (e) {}
    }
  }, 1);
}

// TNT de Hielo: congela el agua cercana
function freezeWater(dimension, pos, radius) {
  for (let dx = -radius; dx <= radius; dx++) {
    for (let dy = -radius; dy <= radius; dy++) {
      for (let dz = -radius; dz <= radius; dz++) {
        try {
          const b = dimension.getBlock({ x: pos.x + dx, y: pos.y + dy, z: pos.z + dz });
          if (b && (b.typeId === "minecraft:water" || b.typeId === "minecraft:flowing_water")) {
            b.setType("minecraft:ice");
          }
        } catch (e) {}
      }
    }
  }
}

// TNT de Hielo: cubre la superficie con nieve
function snowCover(dimension, pos, radius) {
  for (let dx = -radius; dx <= radius; dx++) {
    for (let dz = -radius; dz <= radius; dz++) {
      for (let dy = -1; dy <= 1; dy++) {
        try {
          const p = dimension.getBlock({ x: pos.x + dx, y: pos.y + dy, z: pos.z + dz });
          const below = dimension.getBlock({ x: pos.x + dx, y: pos.y + dy - 1, z: pos.z + dz });
          if (p && below && p.typeId === "minecraft:air" && below.isSolid) {
            p.setType("minecraft:snow_layer");
          }
        } catch (e) {}
      }
    }
  }
}

// TNT de Lava: deja charcos de lava en el crater
function lavaPools(dimension, pos, radius) {
  for (let dx = -radius; dx <= radius; dx++) {
    for (let dy = -2; dy <= 2; dy++) {
      for (let dz = -radius; dz <= radius; dz++) {
        try {
          const p = dimension.getBlock({ x: pos.x + dx, y: pos.y + dy, z: pos.z + dz });
          const below = dimension.getBlock({ x: pos.x + dx, y: pos.y + dy - 1, z: pos.z + dz });
          if (p && below && p.typeId === "minecraft:air" && below.isSolid && Math.random() < 0.75) {
            p.setType("minecraft:lava");
          }
        } catch (e) {}
      }
    }
  }
}

// TNT de Obsidiana: convierte el crater en obsidiana
function obsidianCrater(dimension, pos, radius) {
  for (let dx = -radius; dx <= radius; dx++) {
    for (let dy = -2; dy <= 1; dy++) {
      for (let dz = -radius; dz <= radius; dz++) {
        try {
          const p = dimension.getBlock({ x: pos.x + dx, y: pos.y + dy, z: pos.z + dz });
          const below = dimension.getBlock({ x: pos.x + dx, y: pos.y + dy - 1, z: pos.z + dz });
          if (p && below && p.typeId === "minecraft:air" && below.isSolid) {
            p.setType("minecraft:obsidian");
          }
        } catch (e) {}
      }
    }
  }
}

// TNT de Agua: inunda el crater y apaga el fuego
function waterFlood(dimension, pos, radius) {
  for (let dx = -radius; dx <= radius; dx++) {
    for (let dy = -3; dy <= 1; dy++) {
      for (let dz = -radius; dz <= radius; dz++) {
        try {
          const p = dimension.getBlock({ x: pos.x + dx, y: pos.y + dy, z: pos.z + dz });
          if (!p) continue;
          if (p.typeId === "minecraft:fire") {
            p.setType("minecraft:air");
          } else if (p.typeId === "minecraft:air") {
            p.setType("minecraft:water");
          }
        } catch (e) {}
      }
    }
  }
}

// TNT de Arena: avalancha de arena que cae sobre la zona
function sandFall(dimension, pos, radius) {
  try {
    for (let i = 0; i < 14; i++) {
      const ox = pos.x + Math.floor(Math.random() * (radius * 2 + 1)) - radius;
      const oz = pos.z + Math.floor(Math.random() * (radius * 2 + 1)) - radius;
      const oy = pos.y + 10 + Math.floor(Math.random() * 8);
      dimension.spawnEntity("minecraft:falling_block", { x: ox, y: oy, z: oz });
    }
  } catch (e) {}
}

// TNT Saltarina: lanza a los seres vivos por los aires
function launchPlayers(dimension, pos, radius) {
  try {
    for (const player of dimension.getPlayers()) {
      const p = player.location;
      const dx = p.x - pos.x;
      const dz = p.z - pos.z;
      const dist = Math.sqrt(dx * dx + dz * dz);
      if (dist < 0.001 || dist > radius) continue;
      player.applyKnockback(dx / dist, dz / dist, 3.0, 2.6);
    }
    for (const entity of dimension.getEntities({ maxDistance: radius, location: pos })) {
      if (entity.typeId === "minecraft:player") continue;
      const p = entity.location;
      const dx = p.x - pos.x;
      const dz = p.z - pos.z;
      const dist = Math.sqrt(dx * dx + dz * dz);
      if (dist < 0.001 || dist > radius) continue;
      entity.applyKnockback(dx / dist, dz / dist, 3.0, 2.6);
    }
  } catch (e) {}
}

// TNT Criogenica: congela a jugadores y mobs
function cryoFreeze(dimension, pos, radius) {
  try {
    const targets = dimension.getPlayers().concat(
      dimension.getEntities({ maxDistance: radius, location: pos })
    );
    for (const entity of targets) {
      try {
        entity.addEffect("minecraft:slowness", 200, { amplifier: 2 });
        entity.addEffect("minecraft:frost", 200, { amplifier: 1 });
      } catch (e) {}
    }
  } catch (e) {}
}

// TNT Nuclear: espectaculo de particulas
function nuclearShow(dimension, pos) {
  try {
    dimension.spawnParticle("minecraft:huge_explosion_emitter", pos);
    for (let i = 0; i < 12; i++) {
      dimension.spawnParticle("minecraft:basic_flame_particle", {
        x: pos.x + (Math.random() - 0.5) * 10,
        y: pos.y + Math.random() * 8,
        z: pos.z + (Math.random() - 0.5) * 10
      });
    }
  } catch (e) {}
}

// TNT de Rayo: invoca rayos sobre la zona
function strikeLightning(dimension, pos) {
  try {
    for (let i = 0; i < 3; i++) {
      dimension.spawnEntity("minecraft:lightning_bolt", {
        x: pos.x + (Math.random() - 0.5) * 8,
        y: pos.y,
        z: pos.z + (Math.random() - 0.5) * 8
      });
    }
  } catch (e) {}
}

// Suelta items (oro, diamantes, esmeraldas...)
function dropLoot(dimension, pos, id, total) {
  try {
    while (total > 0) {
      const count = Math.min(total, 16);
      total -= count;
      dimension.spawnItem(new ItemStack(id, count), {
        x: pos.x, y: pos.y + 0.5, z: pos.z
      });
    }
  } catch (e) {}
}

// TNT de Oro: suelta lingotes de oro
function dropGold(dimension, pos) {
  dropLoot(dimension, pos, "minecraft:gold_ingot", 8 + Math.floor(Math.random() * 5));
}

// TNT Agujero Negro: bola negra 3D (entidad tnts:black_hole) que flota sobre
// el crater durante 5 segundos (100 ticks) atrayendo entidades e items.
// Los items que caen al nucleo son consumidos con un destello.
function blackholeSuck(dimension, pos, radius) {
  try {
    // la bola negra 3D visible (la elimina el script al terminar)
    let ball = null;
    try {
      ball = dimension.spawnEntity("tnts:black_hole", {
        x: pos.x + 0.5, y: pos.y + 0.8, z: pos.z + 0.5
      });
    } catch (err) {}

    let remaining = 100; // 5 segundos
    const rings = []; // anillos purpura expansivos (uno por segundo)
    const loop = system.runInterval(() => {
      remaining--;
      if (remaining <= 0) {
        system.clearRun(loop);
        try {
          if (ball) ball.remove();
        } catch (err) {}
        return;
      }
      // pulso cada segundo: lanzar un anillo que crece ~1.3 bloques/tick
      if (remaining % 20 === 0) {
        rings.push({ age: 0 });
      }
      for (const r of rings) r.age++;
      for (let i = rings.length - 1; i >= 0; i--) {
        if (rings[i].age > 15) {
          rings.splice(i, 1);
          continue;
        }
        const rr = Math.min(radius, 1 + rings[i].age * 1.3);
        for (let k = 0; k < 24; k++) {
          const a = (k / 24) * Math.PI * 2;
          try {
            dimension.spawnParticle("minecraft:portal_particle", {
              x: pos.x + Math.cos(a) * rr,
              y: pos.y + 0.4,
              z: pos.z + Math.sin(a) * rr
            });
          } catch (err) {}
        }
      }
      const targets = dimension.getPlayers().concat(
        dimension.getEntities({ maxDistance: radius, location: pos })
      );
      for (const e of targets) {
        const dx = pos.x - e.location.x;
        const dz = pos.z - e.location.z;
        const dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 0.001 || dist > radius) continue;
        // item que cae al nucleo: consumido con un destello
        if (e.typeId === "minecraft:item" && dist < 1 && e.location.y < pos.y + 1) {
          try {
            e.remove();
            dimension.spawnParticle("minecraft:portal_particle", e.location);
          } catch (err) {}
          continue;
        }
        e.applyKnockback(dx / dist, dz / dist, 0.9, 0.1);
      }
      // vortice en espiral girando alrededor del centro
      const angle = (Date.now() / 50) % (Math.PI * 2);
      for (let i = 0; i < 8; i++) {
        const a = angle + (i * Math.PI) / 4;
        const r = 1 + Math.random() * 3;
        try {
          dimension.spawnParticle("minecraft:portal_particle", {
            x: pos.x + Math.cos(a) * r,
            y: pos.y + 0.3 + Math.random() * 1.5,
            z: pos.z + Math.sin(a) * r
          });
        } catch (err) {}
      }
      // humo tragado hacia el centro
      try {
        dimension.spawnParticle("minecraft:basic_smoke_particle", {
          x: pos.x + (Math.random() - 0.5) * 8,
          y: pos.y + 0.5 + Math.random() * 2,
          z: pos.z + (Math.random() - 0.5) * 8
        });
      } catch (err) {}
      // remolino grave continuo (se solapa cada 10 ticks); sube de tono con cada pulso
      if (remaining % 10 === 0) {
        const pulses = Math.min(Math.floor((100 - remaining) / 20), 5);
        try {
          dimension.playSound("tnts.black_hole.loop", {
            x: pos.x + 0.5, y: pos.y + 0.5, z: pos.z + 0.5
          }, {
            volume: 0.9,
            pitch: 1 + pulses * 0.12
          });
        } catch (err) {}
      }
    }, 1);
  } catch (e) {}
}

// TNT Inferno: invoca mobs del Nether hostiles alrededor del crater
function spawnNetherMobs(dimension, pos, radius) {
  try {
    const mobs = [
      "minecraft:blaze",
      "minecraft:magma_cube",
      "minecraft:zombified_piglin",
      "minecraft:wither_skeleton"
    ];
    const count = 3 + Math.floor(Math.random() * 4);
    for (let i = 0; i < count; i++) {
      const a = Math.random() * Math.PI * 2;
      const r = 3 + Math.random() * radius;
      const p = {
        x: pos.x + Math.cos(a) * r,
        y: pos.y + 0.5,
        z: pos.z + Math.sin(a) * r
      };
      try {
        dimension.spawnEntity(mobs[Math.floor(Math.random() * mobs.length)], p);
        dimension.spawnParticle("minecraft:portal_particle", p);
      } catch (err) {}
    }
  } catch (e) {}
}

// TNT de Viento: empuja todo lejos
function windPush(dimension, pos, radius) {
  try {
    const targets = dimension.getPlayers().concat(
      dimension.getEntities({ maxDistance: radius, location: pos })
    );
    for (const e of targets) {
      const dx = e.location.x - pos.x;
      const dz = e.location.z - pos.z;
      const dist = Math.sqrt(dx * dx + dz * dz);
      if (dist < 0.001) continue;
      e.applyKnockback(dx / dist, dz / dist, 2.5, 1.2);
    }
    for (let i = 0; i < 15; i++) {
      dimension.spawnParticle("minecraft:basic_smoke_particle", {
        x: pos.x + (Math.random() - 0.5) * radius * 2,
        y: pos.y + Math.random() * 2,
        z: pos.z + (Math.random() - 0.5) * radius * 2
      });
    }
  } catch (e) {}
}

// TNT Inferno: incendia una gran area e invoca mobs del Nether
function inferno(dimension, pos, radius) {
  try {
    for (let dx = -radius; dx <= radius; dx++) {
      for (let dy = -2; dy <= 4; dy++) {
        for (let dz = -radius; dz <= radius; dz++) {
          const p = dimension.getBlock({ x: pos.x + dx, y: pos.y + dy, z: pos.z + dz });
          const below = dimension.getBlock({ x: pos.x + dx, y: pos.y + dy - 1, z: pos.z + dz });
          if (p && below && p.typeId === "minecraft:air" && below.isSolid && Math.random() < 0.7) {
            p.setType("minecraft:fire");
          }
        }
      }
    }
    spawnNetherMobs(dimension, pos, radius);
  } catch (e) {}
}

// TNT de Setas: esparce setas y micelio
function fungiSpread(dimension, pos, radius) {
  try {
    for (let dx = -radius; dx <= radius; dx++) {
      for (let dy = -2; dy <= 2; dy++) {
        for (let dz = -radius; dz <= radius; dz++) {
          const p = dimension.getBlock({ x: pos.x + dx, y: pos.y + dy, z: pos.z + dz });
          const below = dimension.getBlock({ x: pos.x + dx, y: pos.y + dy - 1, z: pos.z + dz });
          if (p && below && p.typeId === "minecraft:air" && below.isSolid) {
            p.setType(Math.random() < 0.5 ? "minecraft:red_mushroom" : "minecraft:brown_mushroom");
          }
          if (p && (p.typeId === "minecraft:grass_block" || p.typeId === "minecraft:dirt") && Math.random() < 0.3) {
            p.setType("minecraft:mycelium");
          }
        }
      }
    }
  } catch (e) {}
}

// TNT de Miel: lentitud pegajosa + bloques de miel
function honeyGoo(dimension, pos, radius) {
  try {
    for (const e of dimension.getPlayers().concat(
      dimension.getEntities({ maxDistance: radius, location: pos })
    )) {
      e.addEffect("minecraft:slowness", 300, { amplifier: 3 });
    }
    for (let dx = -3; dx <= 3; dx++) {
      for (let dy = -2; dy <= 1; dy++) {
        for (let dz = -3; dz <= 3; dz++) {
          const p = dimension.getBlock({ x: pos.x + dx, y: pos.y + dy, z: pos.z + dz });
          const below = dimension.getBlock({ x: pos.x + dx, y: pos.y + dy - 1, z: pos.z + dz });
          if (p && below && p.typeId === "minecraft:air" && below.isSolid && Math.random() < 0.7) {
            p.setType("minecraft:honey_block");
          }
        }
      }
    }
  } catch (e) {}
}

// TNT Curativa: cura y da regeneracion
function healBurst(dimension, pos, radius) {
  try {
    for (const e of dimension.getPlayers().concat(
      dimension.getEntities({ maxDistance: radius, location: pos })
    )) {
      e.addEffect("minecraft:regeneration", 120, { amplifier: 1 });
      try {
        const h = e.getComponent("minecraft:health");
        if (h) h.setCurrent(h.current + 10);
      } catch (e2) {}
    }
  } catch (e) {}
}

// TNT Teletransportadora: manda a los seres vivos a sitios aleatorios
function teleportScramble(dimension, pos, radius) {
  try {
    for (const e of dimension.getPlayers().concat(
      dimension.getEntities({ maxDistance: radius, location: pos })
    )) {
      e.teleport({
        x: pos.x + (Math.random() - 0.5) * 40,
        y: e.location.y,
        z: pos.z + (Math.random() - 0.5) * 40
      });
    }
  } catch (e) {}
}

// TNT de Confeti: lluvia de particulas de colores
function confetti(dimension, pos) {
  try {
    for (let i = 0; i < 40; i++) {
      dimension.spawnParticle("minecraft:happy_villager_particle", {
        x: pos.x + (Math.random() - 0.5) * 14,
        y: pos.y + Math.random() * 6,
        z: pos.z + (Math.random() - 0.5) * 14
      });
      dimension.spawnParticle("minecraft:basic_smoke_particle", {
        x: pos.x + (Math.random() - 0.5) * 14,
        y: pos.y + Math.random() * 6,
        z: pos.z + (Math.random() - 0.5) * 14
      });
    }
  } catch (e) {}
}

// TNT Terremoto: crater + grietas con lava + lanza entidades alto
function earthquake(dimension, pos) {
  try {
    // crater central (radio 6)
    for (let dx = -6; dx <= 6; dx++) {
      for (let dy = -4; dy <= 2; dy++) {
        for (let dz = -6; dz <= 6; dz++) {
          if (Math.sqrt(dx * dx + dy * dy + dz * dz) < 6 && Math.random() < 0.66) {
            try {
              const p = { x: pos.x + dx, y: pos.y + dy, z: pos.z + dz };
              const b = dimension.getBlock(p);
              if (b && b.typeId !== "minecraft:air") b.setType("minecraft:air");
            } catch (e) {}
          }
        }
      }
    }
    // grietas con lava (8 lineas desde el centro)
    for (let i = 0; i < 8; i++) {
      const angle = i * Math.PI / 4;
      for (let d = 4; d <= 12; d++) {
        const bx = Math.floor(pos.x + Math.cos(angle) * d);
        const bz = Math.floor(pos.z + Math.sin(angle) * d);
        try {
          const p = { x: bx, y: pos.y - 1, z: bz };
          if (Math.random() < 0.33) {
            const b = dimension.getBlock(p);
            if (b) b.setType("minecraft:lava");
          } else if (Math.random() < 0.5) {
            const above = dimension.getBlock({ x: bx, y: pos.y, z: bz });
            if (above && above.typeId !== "minecraft:air") above.setType("minecraft:air");
          }
        } catch (e) {}
      }
    }
    // lanzar entidades alto
    for (const e of dimension.getPlayers().concat(
      dimension.getEntities({ maxDistance: 12, location: pos })
    )) {
      try { e.applyImpulse({ x: 0, y: 3.5 + Math.random() * 2, z: 0 }); } catch (e2) {}
    }
  } catch (e) {}
}

// TNT Meteorito: llama meteoritos del cielo con estela de fuego
function meteorShower(dimension, pos) {
  try {
    const num = 5 + Math.floor(Math.random() * 4);
    for (let i = 0; i < num; i++) {
      const mx = pos.x + (Math.random() - 0.5) * 30;
      const mz = pos.z + (Math.random() - 0.5) * 30;
      // estela de fuego cayendo
      for (let j = 0; j < 20; j++) {
        const ty = pos.y + 30 - j * 1.5;
        dimension.spawnParticle("minecraft:basic_flame_particle", {
          x: mx + (Math.random() - 0.5) * 0.5,
          y: ty,
          z: mz + (Math.random() - 0.5) * 0.5
        });
        dimension.spawnParticle("minecraft:basic_smoke_particle", {
          x: mx + (Math.random() - 0.5) * 0.3,
          y: ty,
          z: mz + (Math.random() - 0.5) * 0.3
        });
      }
      // impacto: explosion pequena + lava
      const impact = { x: Math.floor(mx), y: Math.floor(pos.y + 1), z: Math.floor(mz) };
      try { dimension.createExplosion(impact, 3 + Math.random() * 2, { causesFire: true, breaksBlocks: true }); } catch (e) {}
      for (let dx = -2; dx <= 2; dx++) {
        for (let dz = -2; dz <= 2; dz++) {
          if (Math.random() < 0.33) {
            try {
              const b = dimension.getBlock({ x: impact.x + dx, y: impact.y - 1, z: impact.z + dz });
              if (b) b.setType("minecraft:lava");
            } catch (e) {}
          }
        }
      }
      try {
        dimension.spawnParticle("minecraft:huge_explosion_emitter", impact);
        dimension.spawnParticle("minecraft:large_explosion_particle", {
          x: impact.x, y: impact.y + 2, z: impact.z
        });
      } catch (e) {}
    }
  } catch (e) {}
}

// TNT Tormenta: lluvia de rayos + viento fuerte
function massiveStorm(dimension, pos) {
  try {
    const num = 12 + Math.floor(Math.random() * 7);
    for (let i = 0; i < num; i++) {
      try {
        const lx = pos.x + (Math.random() - 0.5) * 20;
        const lz = pos.z + (Math.random() - 0.5) * 20;
        dimension.spawnEntity("minecraft:lightning_bolt", {
          x: lx, y: pos.y, z: lz
        });
      } catch (e) {}
    }
    // viento fuerte: empuja todo lejos
    for (const e of dimension.getPlayers().concat(
      dimension.getEntities({ maxDistance: 16, location: pos })
    )) {
      try {
        const dx = e.location.x - pos.x;
        const dz = e.location.z - pos.z;
        const dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > 0.1) {
          const strength = 3.0 * (1 - Math.min(1, dist / 16)) + 0.5;
          e.applyImpulse({ x: dx / dist * strength, y: 0.3, z: dz / dist * strength });
        }
      } catch (e2) {}
    }
    // nubes oscuras
    for (let i = 0; i < 40; i++) {
      dimension.spawnParticle("minecraft:white_smoke_particle", {
        x: pos.x + (Math.random() - 0.5) * 18,
        y: pos.y + 8,
        z: pos.z + (Math.random() - 0.5) * 18
      });
    }
  } catch (e) {}
}

// TNT Colosal: explosion en 3 oleadas (radio 4 -> 8 -> 12) sin lag
function colossalExplosion(dimension, pos) {
  const crater = (radius) => {
    for (let dx = -radius; dx <= radius; dx++) {
      for (let dy = -5; dy <= 3; dy++) {
        for (let dz = -radius; dz <= radius; dz++) {
          const dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
          if (dist < radius && Math.random() < 0.6) {
            try {
              const p = { x: pos.x + dx, y: pos.y + dy, z: pos.z + dz };
              const b = dimension.getBlock(p);
              if (b && b.typeId !== "minecraft:air") b.setType("minecraft:air");
            } catch (e) {}
          }
        }
      }
    }
  };
  try {
    crater(4);
    system.runTimeout(() => {
      try {
        crater(8);
        system.runTimeout(() => {
          try {
            crater(12);
            // fuego en los bordes
            for (let dx = -12; dx <= 12; dx++) {
              for (let dz = -12; dz <= 12; dz++) {
                const dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 8 && dist < 12 && Math.random() < 0.25) {
                  try {
                    const p = { x: pos.x + dx, y: pos.y, z: pos.z + dz };
                    const b = dimension.getBlock(p);
                    if (b && b.typeId === "minecraft:air") b.setType("minecraft:fire");
                  } catch (e) {}
                }
              }
            }
            dimension.spawnParticle("minecraft:huge_explosion_emitter", {
              x: pos.x + 0.5, y: pos.y + 0.5, z: pos.z + 0.5
            });
          } catch (e) {}
        }, 10);
      } catch (e) {}
    }, 10);
  } catch (e) {}
}

// TNT Supernova: destello + ondas de luz + XP masivo
function supernova(dimension, pos) {
  try {
    const c = { x: pos.x + 0.5, y: pos.y + 1, z: pos.z + 0.5 };
    dimension.spawnParticle("minecraft:huge_explosion_emitter", c);
    for (let i = 0; i < 60; i++) {
      dimension.spawnParticle("minecraft:endrod_particle", {
        x: c.x + (Math.random() - 0.5) * 16,
        y: c.y + (Math.random() - 0.5) * 8,
        z: c.z + (Math.random() - 0.5) * 16
      });
    }
    // XP masivo
    let total = 200 + Math.floor(Math.random() * 100);
    while (total > 0) {
      const value = Math.min(total, 50);
      total -= value;
      dimension.spawnEntity("minecraft:xp_orb", {
        x: c.x + (Math.random() - 0.5),
        y: c.y,
        z: c.z + (Math.random() - 0.5)
      });
    }
    // regeneracion + glowing a entidades cercanas
    for (const e of dimension.getPlayers().concat(
      dimension.getEntities({ maxDistance: 10, location: pos })
    )) {
      try {
        e.addEffect("minecraft:regeneration", 200, { amplifier: 2 });
        e.addEffect("minecraft:glowing", 400, { amplifier: 0 });
      } catch (e2) {}
    }
  } catch (e) {}
}

// TNT de Experiencia: suelta orbes de experiencia
function xpDrop(dimension, pos) {
  try {
    let total = 80 + Math.floor(Math.random() * 70);
    while (total > 0) {
      const value = Math.min(total, 50);
      total -= value;
      dimension.spawnEntity("minecraft:xp_orb", {
        x: pos.x + (Math.random() - 0.5),
        y: pos.y + 0.5,
        z: pos.z + (Math.random() - 0.5)
      });
    }
  } catch (e) {}
}

// ------------------------------------------------------------
//  DETONADOR REMOTO: enciende todas las TNTs en 24 bloques
// ------------------------------------------------------------
const DETONATOR_RADIUS = 24;

function useDetonator(player) {
  try {
    const dimension = player.dimension;
    const center = player.location;
    let count = 0;
    for (let dx = -DETONATOR_RADIUS; dx <= DETONATOR_RADIUS; dx++) {
      for (let dy = -8; dy <= 8; dy++) {
        for (let dz = -DETONATOR_RADIUS; dz <= DETONATOR_RADIUS; dz++) {
          try {
            const b = dimension.getBlock({
              x: Math.floor(center.x) + dx,
              y: Math.floor(center.y) + dy,
              z: Math.floor(center.z) + dz
            });
            if (b && CONFIG[b.typeId]) {
              igniteTnt(b);
              count++;
              // destello en cada TNT encendida
              try {
                dimension.spawnParticle("minecraft:white_smoke_particle", b.location);
                dimension.spawnParticle("minecraft:small_flame_particle", {
                  x: b.location.x + 0.5, y: b.location.y + 0.5, z: b.location.z + 0.5
                });
              } catch (e2) {}
            }
          } catch (e) {}
        }
      }
    }
    try {
      if (count >= 10) {
        dimension.playSound("tnts.detonator.burst", center, { volume: 1.2, pitch: 1.0 });
        try {
          player.onScreenDisplay.setTitle("¡Detonación masiva!", {
            subtitle: count + " TNTs encendidas de golpe",
            fadeInDuration: 5, stayDuration: 40, fadeOutDuration: 10
          });
        } catch (e2) {}
      } else {
        dimension.playSound("tnts.detonator.click", center, { volume: 1.0, pitch: count > 0 ? 1.0 : 0.5 });
      }
    } catch (e) {}
    if (count > 0) {
      try {
        const inv = player.getComponent("minecraft:inventory");
        const item = inv && inv.container ? inv.container.getItem(player.selectedSlotIndex) : undefined;
        if (item && item.typeId === "tnts:detonator") {
          item.setLore ? item.setLore([]) : null;
        }
      } catch (e) {}
    }
  } catch (e) {}
}

// ------------------------------------------------------------
//  LANZADOR DE TNT y GRANADA: proyectiles con fisica propia
// ------------------------------------------------------------
const itemCooldown = new Map(); // playerId -> tick

function tryCooldown(player, ticks) {
  const now = system.currentTick;
  const last = itemCooldown.get(player.id) || 0;
  if (now - last < ticks) return false;
  itemCooldown.set(player.id, now);
  return true;
}

function isCreative(player) {
  try {
    return player.getGameMode && player.getGameMode() === "creative";
  } catch (e) {
    return false;
  }
}

// Busca una TNT del mod en el inventario (cualquier variante)
function findTntInInventory(player) {
  try {
    const inv = player.getComponent("minecraft:inventory");
    const container = inv && inv.container;
    if (!container) return null;
    for (let i = 0; i < container.size; i++) {
      const item = container.getItem(i);
      if (item && CONFIG[item.typeId]) return { slot: i, item: item, container: container };
    }
  } catch (e) {}
  return null;
}

function useLauncher(player) {
  try {
    if (!tryCooldown(player, 10)) return;
    const found = findTntInInventory(player);
    if (!found) {
      // sin municion: click seco
      player.dimension.playSound("tnts.detonator.click", player.location, { volume: 0.6, pitch: 0.5 });
      return;
    }
    if (!isCreative(player)) {
      if (found.item.amount <= 1) found.container.setItem(found.slot, undefined);
      else found.container.setItem(found.slot, new ItemStack(found.item.typeId, found.item.amount - 1));
    }
    const look = player.getViewDirection();
    const origin = player.getHeadLocation();
    const fuse = CONFIG[found.item.typeId].fuse || 40;
    spawnFlyingTnt(player.dimension, origin, look, 1.6, found.item.typeId, fuse, { bounce: 0.4 });
    player.dimension.playSound("tnts.launcher.shoot", player.location, { volume: 1.0, pitch: 1.0 });
  } catch (e) {}
}

function throwGrenade(player) {
  try {
    if (!tryCooldown(player, 8)) return;
    if (!isCreative(player)) {
      const inv = player.getComponent("minecraft:inventory");
      const container = inv && inv.container;
      if (!container) return;
      const slot = player.selectedSlotIndex;
      const item = container.getItem(slot);
      if (!item || item.typeId !== "tnts:tnt_grenade") return;
      if (item.amount <= 1) container.setItem(slot, undefined);
      else container.setItem(slot, new ItemStack(item.typeId, item.amount - 1));
    }
    const look = player.getViewDirection();
    const origin = player.getHeadLocation();
    spawnFlyingTnt(player.dimension, origin, look, 1.2, "tnts:mini_tnt", 30, { bounce: 0.5, lift: 0.2 });
    player.dimension.playSound("tnts.launcher.shoot", player.location, { volume: 0.8, pitch: 1.3 });
  } catch (e) {}
}

// Flecha de TNT: proyectil rapido (estilo flecha) que explota al impacto
function shootTntArrow(player) {
  try {
    if (!tryCooldown(player, 6)) return;
    if (!isCreative(player)) {
      const inv = player.getComponent("minecraft:inventory");
      const container = inv && inv.container;
      if (!container) return;
      const slot = player.selectedSlotIndex;
      const item = container.getItem(slot);
      if (!item || item.typeId !== "tnts:tnt_arrow") return;
      if (item.amount <= 1) container.setItem(slot, undefined);
      else container.setItem(slot, new ItemStack(item.typeId, item.amount - 1));
    }
    const look = player.getViewDirection();
    const origin = player.getHeadLocation();
    spawnFlyingTnt(player.dimension, origin, look, 2.6, "tnts:mini_tnt", 60, { bounce: 0.1, lift: 0.05 });
    player.dimension.playSound("tnts.launcher.shoot", player.location, { volume: 1.0, pitch: 1.8 });
  } catch (e) {}
}

const flying = []; // proyectiles activos de lanzador/granada

function spawnFlyingTnt(dimension, origin, look, speed, typeId, fuse, opts) {
  opts = opts || {};
  flying.push({
    dimension: dimension,
    pos: { x: origin.x, y: origin.y, z: origin.z },
    vel: {
      x: look.x * speed,
      y: look.y * speed + (opts.lift || 0),
      z: look.z * speed
    },
    typeId: typeId,
    fuse: fuse,
    bounce: opts.bounce !== undefined ? opts.bounce : 0.45
  });
}

// Fisica de los proyectiles: gravedad, rebotes y explosion al final
system.runInterval(() => {
  for (let i = flying.length - 1; i >= 0; i--) {
    const p = flying[i];
    try {
      p.fuse--;
      p.vel.y -= 0.06; // gravedad
      if (p.vel.y < -1.6) p.vel.y = -1.6; // velocidad terminal

      const nx = p.pos.x + p.vel.x;
      const ny = p.pos.y + p.vel.y;
      const nz = p.pos.z + p.vel.z;
      let nb = null;
      try {
        nb = p.dimension.getBlock({ x: Math.floor(nx), y: Math.floor(ny), z: Math.floor(nz) });
      } catch (e) {}
      const solid = nb && !nb.isAir && nb.typeId !== "minecraft:water" && nb.typeId !== "minecraft:flowing_water" &&
                    nb.typeId !== "minecraft:lava" && nb.typeId !== "minecraft:flowing_lava";

      const speed = Math.abs(p.vel.x) + Math.abs(p.vel.y) + Math.abs(p.vel.z);
      if (solid) {
        if (p.fuse > 0 && speed > 0.15 && p.vel.y < -0.15 && p.bounce > 0) {
          // rebote en el suelo
          p.pos.y = Math.floor(ny) - 0.01;
          p.vel.y = -p.vel.y * p.bounce;
          p.vel.x *= 0.6;
          p.vel.z *= 0.6;
          try { p.dimension.playSound("tnts.beep", p.pos, { volume: 0.4, pitch: 0.5 }); } catch (e2) {}
          continue;
        }
        if (p.fuse > 0 && speed > 0.4 && p.bounce > 0) {
          // rebote en pared: invierte el eje horizontal dominante
          if (Math.abs(p.vel.x) > Math.abs(p.vel.z)) p.vel.x = -p.vel.x * p.bounce;
          else p.vel.z = -p.vel.z * p.bounce;
          p.vel.y *= 0.6;
          continue;
        }
        // impacto duro: explota ya
        explodeAt(p.dimension, { x: p.pos.x, y: p.pos.y, z: p.pos.z }, p.typeId);
        flying.splice(i, 1);
        continue;
      }

      p.pos.x = nx;
      p.pos.y = ny;
      p.pos.z = nz;

      // estela de partículas
      try {
        p.dimension.spawnParticle("minecraft:basic_smoke_particle", p.pos);
        if (p.fuse % 3 === 0) {
          p.dimension.spawnParticle("minecraft:small_flame_particle", { x: p.pos.x, y: p.pos.y + 0.4, z: p.pos.z });
        }
      } catch (e) {}

      if (p.fuse <= 0) {
        explodeAt(p.dimension, { x: p.pos.x, y: p.pos.y, z: p.pos.z }, p.typeId);
        flying.splice(i, 1);
      }
    } catch (e) {
      flying.splice(i, 1);
    }
  }
}, 1);

const events = world.events || world.afterEvents;
if (events && events.itemUse && events.itemUse.subscribe) {
  events.itemUse.subscribe((event) => {
    if (!event || !event.item || !event.source) return;
    const id = event.item.typeId;
    if (id === "tnts:detonator") {
      useDetonator(event.source);
    } else if (id === "tnts:tnt_launcher") {
      useLauncher(event.source);
    } else if (id === "tnts:tnt_grenade") {
      throwGrenade(event.source);
    } else if (id === "tnts:tnt_arrow") {
      shootTntArrow(event.source);
    }
  });
}

// ------------------------------------------------------------
//  Forma moderna (1.21.90+): componente personalizado "tnts:ignite"
// ------------------------------------------------------------
const igniteComponent = {
  onPlayerInteract(event) {
    if (event && event.block && hasFlint(event.player)) igniteTnt(event.block);
  },
  onRedstoneUpdate(event) {
    if (event && event.block) igniteTnt(event.block);
  }
};

try {
  system.beforeEvents.startup.subscribe(({ blockComponentRegistry }) => {
    blockComponentRegistry.registerCustomComponent("tnts:ignite", igniteComponent);
  });
} catch (e) {}

// ------------------------------------------------------------
//  Respaldo: eventos globales
// ------------------------------------------------------------
if (events) {
  // Al hacer click con mechero
  if (events.blockInteract && events.blockInteract.subscribe) {
    events.blockInteract.subscribe((event) => {
      if (event && event.block && hasFlint(event.player)) igniteTnt(event.block);
    });
  }
  // Reaccion en cadena: si una explosion destruye otra TNT loca, explota tambien
  if (events.blockExplode && events.blockExplode.subscribe) {
    events.blockExplode.subscribe((event) => {
      if (event && event.block) igniteTnt(event.block);
    });
  }
}

// ------------------------------------------------------------
//  Peto de TNT reactivo: empuja al atacante con destello y pitido
// ------------------------------------------------------------
const armorCooldown = new Map(); // playerId -> tick

function getChestSlot(player) {
  try {
    const armor = player.getComponent("minecraft:armor_containers");
    if (armor && armor.container) return armor.container.getSlot(1);
  } catch (e) {}
  try {
    const inv = player.getComponent("minecraft:inventory");
    if (inv && inv.container) return inv.container.getSlot(37); // legado: pechera
  } catch (e) {}
  return undefined;
}

if (events && events.entityHurt && events.entityHurt.subscribe) {
  events.entityHurt.subscribe((event) => {
    try {
      const player = event.hurtEntity;
      if (!player || player.typeId !== "minecraft:player") return;
      const slot = getChestSlot(player);
      if (!slot || !slot.getItem || !slot.getItem()) return;
      if (slot.getItem().typeId !== "tnts:tnt_chestplate") return;
      const attacker = event.damageSource && event.damageSource.damagingEntity;
      if (!attacker || !attacker.location) return;
      const now = system.currentTick;
      const last = armorCooldown.get(player.id) || 0;
      if (now - last < 30) return;
      armorCooldown.set(player.id, now);
      const dx = attacker.location.x - player.location.x;
      const dz = attacker.location.z - player.location.z;
      if (Math.abs(dx) < 0.001 && Math.abs(dz) < 0.001) return;
      attacker.applyKnockback(dx, dz, 1.6, 0.8);
      const dim = player.dimension;
      dim.spawnParticle("minecraft:flame_particle", {
        x: attacker.location.x, y: attacker.location.y + 1, z: attacker.location.z
      });
      dim.playSound("tnts.beep", attacker.location, { volume: 0.5, pitch: 0.7 });
    } catch (e) {}
  });
}

// ------------------------------------------------------------
//  Pico de TNT: cada bloque roto explota (radio 2)
// ------------------------------------------------------------
if (events && events.playerBreakBlock && events.playerBreakBlock.subscribe) {
  events.playerBreakBlock.subscribe((event) => {
    try {
      let isPick = false;
      if (event.itemStackBeforeBreak) {
        isPick = event.itemStackBeforeBreak.typeId === "tnts:tnt_pickaxe";
      }
      if (!isPick && event.player) {
        const inv = event.player.getComponent("minecraft:inventory");
        const item = inv && inv.container ? inv.container.getItem(event.player.selectedSlotIndex) : undefined;
        isPick = !!(item && item.typeId === "tnts:tnt_pickaxe");
      }
      if (!isPick) return;
      event.block.dimension.createExplosion(event.block.location, 2, {
        breaksBlocks: true,
        causesFire: false
      });
    } catch (e) {}
  });
}

// ------------------------------------------------------------
//  BUNKER DE TNT: se genera una vez por mundo cerca del spawn
// ------------------------------------------------------------
const BUNKER_PROP = "tnts:bunker_generated";

function findSurfaceY(dimension, x, z) {
  for (let y = 120; y > 20; y--) {
    try {
      const b = dimension.getBlock({ x: x, y: y, z: z });
      if (b && !b.isAir) return y + 1;
    } catch (e) {}
  }
  return 64;
}

function buildBunker(dimension, cx, cy, cz) {
  const set = (x, y, z, id) => {
    try { dimension.setBlockType({ x: x, y: y, z: z }, id); } catch (e) {}
  };
  const stoneBricks = "minecraft:stone_bricks";
  const cracked = "minecraft:cracked_stone_bricks";
  const hiddenTnt = "tnts:mega_tnt";
  const mine = "tnts:mina_tnt";
  const torch = "minecraft:wall_torch";
  for (let x = cx - 5; x <= cx + 5; x++) {
    for (let y = cy - 3; y <= cy + 4; y++) {
      for (let z = cz - 5; z <= cz + 5; z++) {
        const dx = x - cx;
        const dy = y - cy;
        const dz = z - cz;
        const wall = dx === -5 || dx === 5 || dz === -5 || dz === 5;
        if (dy === -3) {
          set(x, y, z, (dx + dz) % 4 === 0 ? cracked : stoneBricks); // suelo
        } else if (dy === 4) {
          set(x, y, z, stoneBricks); // techo
        } else if (wall) {
          // paredes con TNT escondida cada 3 bloques
          if (dy >= -1 && dy <= 1 && (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) % 3 === 0) {
            set(x, y, z, hiddenTnt);
          } else {
            set(x, y, z, stoneBricks);
          }
        } else if (dy === -2 && ((dx === -3 && dz === -3) || (dx === 3 && dz === 3) || (dx === 0 && dz === 3))) {
          set(x, y, z, mine); // minas en el suelo
        } else if (dx === 0 && dy === -2 && dz === 0) {
          set(x, y, z, "minecraft:chest"); // cofre central
        } else {
          set(x, y, z, "minecraft:air");
        }
      }
    }
  }
  // antorchas en las paredes
  set(cx - 5, cy + 1, cz, torch);
  set(cx + 5, cy + 1, cz, torch);
  set(cx, cy + 1, cz - 5, torch);
  set(cx, cy + 1, cz + 5, torch);
  // loot del cofre
  try {
    dimension.runCommand(`loot spawn ${cx} ${cy - 2} ${cz} loot "chests/simple_dungeon"`);
  } catch (e) {}
}

function spawnBunkerIfNeeded() {
  try {
    if (world.getDynamicProperty(BUNKER_PROP)) return;
    const dimension = world.getDimension("minecraft:overworld");
    const spawn = world.getDefaultSpawnLocation();
    const angle = Math.random() * Math.PI * 2;
    const dist = 250 + Math.floor(Math.random() * 150);
    const bx = Math.floor(spawn.x + Math.cos(angle) * dist);
    const bz = Math.floor(spawn.z + Math.sin(angle) * dist);
    const surfaceY = findSurfaceY(dimension, bx, bz);
    buildBunker(dimension, bx, surfaceY - 8, bz);
    world.setDynamicProperty(BUNKER_PROP, true);
    console.warn("[TNTs Locos] Bunker de TNT generado en " + bx + ", " + (surfaceY - 8) + ", " + bz);
  } catch (e) {}
}

if (events && events.worldLoad && events.worldLoad.subscribe) {
  events.worldLoad.subscribe(spawnBunkerIfNeeded);
} else {
  system.runTimeout(spawnBunkerIfNeeded, 100);
}

// ------------------------------------------------------------
//  Respaldo de redstone: sondeo de las TNT colocadas
// ------------------------------------------------------------
const placed = new Set();
if (events && events.blockPlace && events.blockPlace.subscribe) {
  events.blockPlace.subscribe((event) => {
    if (event && event.block && CONFIG[event.block.typeId]) {
      placed.add(keyOf(event.block));
    }
  });
}
system.runInterval(() => {
  for (const key of placed) {
    try {
      const parts = key.split(":");
      const dimId = parts.shift();
      const [cx, cy, cz] = parts[0].split(",").map(Number);
      const dimension = world.getDimension(dimId);
      const block = dimension.getBlock({ x: cx, y: cy, z: cz });
      if (!block || !CONFIG[block.typeId]) {
        placed.delete(key);
        continue;
      }
      if (block.getRedstonePower && block.getRedstonePower() > 0) {
        placed.delete(key);
        igniteTnt(block);
        continue;
      }
      // mina: explota si un ser vivo esta encima
      if (block.typeId === "tnts:mina_tnt") {
        const sobre = dimension.getEntities({
          location: { x: cx + 0.5, y: cy + 1, z: cz + 0.5 },
          maxDistance: 1.2
        });
        if (sobre.length > 0) {
          placed.delete(key);
          igniteTnt(block);
        }
      }
    } catch (e) {
      placed.delete(key);
    }
  }
}, 4);

try {
  console.warn("[TNTs Locos] Script cargado correctamente (v1.7.0)");
} catch (e) {}
