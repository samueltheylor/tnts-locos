package com.tnts.entity;

import com.tnts.ModSounds;
import com.tnts.ModTriggers;
import com.tnts.block.TntBlock;
import com.tnts.block.TntEffect;
import com.tnts.block.TntProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * TNT encendida: es una entidad {@link PrimedTnt} que al terminar la mecha
 * explota con las propiedades y efectos de su variante (config editable).
 * <p>
 * Por defecto queda CLAVADA en su sitio al encenderse (no cae, no la empuja
 * el agua ni las explosiones) para que no se mueva de donde la pusiste.
 * Solo las TNT lanzadas (lanzador, granada, flecha) conservan la fisica.
 */
public class TntsPrimedTnt extends PrimedTnt {

    private static final EntityDataAccessor<String> DATA_BLOCK =
            SynchedEntityData.defineId(TntsPrimedTnt.class, EntityDataSerializers.STRING);

    private String blockId = "tnts:mini_tnt";

    /** Jugador que la encendio (para los advancements). */
    private UUID ownerUuid;

    /**
     * true = TNT colocada y encendida en su sitio (no se mueve de lugar);
     * false = TNT lanzada como proyectil (lanzador, granada: conserva fisica).
     */
    private boolean stationary = true;

    /** Marca esta TNT como estatica (no se mueve de su sitio al encenderse). */
    public void setStationary(boolean stationary) {
        this.stationary = stationary;
    }

    public TntsPrimedTnt(EntityType<? extends TntsPrimedTnt> type, Level level) {
        super(type, level);
    }

    public TntsPrimedTnt(Level level, double x, double y, double z, String blockId,
                         int fuse, @Nullable LivingEntity owner) {
        super(TntsEntities.PRIMED_TNT.get(), level);
        this.setPos(x, y, z);
        this.blockId = blockId;
        this.entityData.set(DATA_BLOCK, blockId);
        this.setFuse(fuse);
        this.ownerUuid = owner != null ? owner.getUUID() : null;
    }

    /** Nombre de la variante (ej: "mega_tnt"). */
    public String getVariantName() {
        return blockId.contains(":") ? blockId.substring(blockId.indexOf(':') + 1) : blockId;
    }

    /** Estado para renderizar (la variante encendida, con su textura animada). */
    public BlockState getRenderBlockState() {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
        if (block instanceof TntBlock tntBlock) {
            return tntBlock.defaultBlockState().setValue(TntBlock.LIT, true);
        }
        return Blocks.TNT.defaultBlockState();
    }

    /** Propiedades actuales de la variante (config editable). */
    public TntProperties getTntProperties() {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
        if (block instanceof TntBlock tntBlock) {
            return tntBlock.getTntProperties();
        }
        return new TntProperties(4.0f, false, true, 40, java.util.EnumSet.noneOf(TntEffect.class));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BLOCK, "tnts:mini_tnt");
    }

    @Override
    public void tick() {
        if (stationary) {
            tickStationary();
        } else {
            super.tick();
        }
        if (this.level().isClientSide || this.isRemoved()) return;
        int fuse = this.getFuse();
        if (fuse <= 0) return;

        ServerLevel serverLevel = (ServerLevel) this.level();
        double px = this.getX();
        double py = this.getY() + 0.3;
        double pz = this.getZ();

        // mecha: chispas + humo DENSOS en la cara superior (centro del bloque)
        double fx = this.getX();
        double fy = this.getY() + 0.95;
        double fz = this.getZ();
        serverLevel.sendParticles(ParticleTypes.SMOKE, fx, fy, fz, 3, 0.12, 0.05, 0.12, 0.0);
        serverLevel.sendParticles(ParticleTypes.SMALL_FLAME, fx, fy, fz, 2, 0.08, 0.03, 0.08, 0.01);
        serverLevel.sendParticles(ParticleTypes.FLAME, fx, fy, fz, 1, 0.04, 0.0, 0.04, 0.0);
        // punto de luz rojo que parpadea sobre la cara superior (4 ticks encendido / 4 apagado)
        if ((this.tickCount / 4) % 2 == 0) {
            serverLevel.sendParticles(new DustParticleOptions(
                            new org.joml.Vector3f(1.0F, 0.15F, 0.05F), 1.4F),
                    fx, this.getY() + 1.06, fz, 1, 0, 0, 0, 0);
        }
        // flash blanco justo antes de explotar
        if (fuse <= 8) {
            serverLevel.sendParticles(ParticleTypes.FLASH, px, py, pz, 1, 0, 0, 0, 0);
        }
        // pitidos de cuenta atras que se aceleran (TNT Trampa y Mina)
        if (getTntProperties().has(TntEffect.TRAP) && fuse <= 20
                && fuse % Math.max(2, fuse / 4) == 0) {
            float pitch = 1.0f + (20 - fuse) * 0.06f;
            serverLevel.playSound(null, this.blockPosition(), ModSounds.BEEP.get(),
                    SoundSource.BLOCKS, 1.0F, pitch);
            serverLevel.sendParticles(ParticleTypes.NOTE, px, py + 0.8, pz,
                    1, 0, 0, 0, 10.0);
        }
    }

    /**
     * Version estatica del tick de la TNT: decrementa la mecha y explota,
     * pero NUNCA aplica movimiento (ni gravedad, ni agua, ni empujes de
     * explosiones) para que la TNT encendida no se mueva de su sitio.
     */
    private void tickStationary() {
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        this.setDeltaMovement(Vec3.ZERO);
        int i = this.getFuse() - 1;
        this.setFuse(i);
        if (i <= 0) {
            this.discard();
            if (!this.level().isClientSide) {
                this.explode();
            }
        } else if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.SMOKE,
                    this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void explode() {
        Level lvl = this.level();
        TntProperties p = getTntProperties();
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        lvl.explode(this, x, y, z, p.power(), p.fire(),
                p.breaksBlocks() ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE);

        if (lvl.isClientSide) return; // el resto es solo servidor

        ServerLevel sl = (ServerLevel) lvl;
        explosionFx(sl, x, y, z, p);

        BlockPos center = this.blockPosition();
        if (p.has(TntEffect.FREEZES)) freezeWater(lvl, center, 7);
        if (p.has(TntEffect.SNOW)) snowCover(lvl, center, 7);
        if (p.has(TntEffect.LAVA)) lavaPools(lvl, center, 4);
        if (p.has(TntEffect.OBSIDIAN)) obsidianCrater(lvl, center, 4);
        if (p.has(TntEffect.WATER)) waterFlood(lvl, center, 4);
        if (p.has(TntEffect.SAND)) sandFall(lvl, center, 5);
        if (p.has(TntEffect.LAUNCH)) launchEntities(lvl, x, y, z, 9);
        if (p.has(TntEffect.CRYO)) cryoFreeze(lvl, x, y, z, 8);
        if (p.has(TntEffect.NUCLEAR)) nuclearFx(lvl, x, y, z);
        if (p.has(TntEffect.LIGHTNING)) strikeLightning(lvl, x, y, z);
        if (p.has(TntEffect.GOLD)) dropItems(lvl, x, y, z, Items.GOLD_INGOT, 8 + lvl.random.nextInt(5));
        if (p.has(TntEffect.XP)) xpDrop(lvl, x, y, z);
        if (p.has(TntEffect.DIAMOND)) dropItems(lvl, x, y, z, Items.DIAMOND, 3 + lvl.random.nextInt(3));
        if (p.has(TntEffect.EMERALD)) dropItems(lvl, x, y, z, Items.EMERALD, 8 + lvl.random.nextInt(8));
        if (p.has(TntEffect.BLACKHOLE)) spawnBlackHole(lvl, x, y, z);
        if (p.has(TntEffect.WIND)) windPush(lvl, x, y, z, 9);
        if (p.has(TntEffect.INFERNO)) {
            inferno(lvl, center, 8);
            spawnNetherMobs(lvl, center);
        }
        if (p.has(TntEffect.FUNGI)) fungiSpread(lvl, center, 6);
        if (p.has(TntEffect.HONEY)) honeyGoo(lvl, x, y, z, 8);
        if (p.has(TntEffect.HEAL)) healBurst(lvl, x, y, z, 10);
        if (p.has(TntEffect.TELEPORT)) teleportScramble(lvl, x, y, z, 12);
        if (p.has(TntEffect.CONFETTI)) confetti(lvl, x, y, z);
        if (p.has(TntEffect.EARTHQUAKE)) earthquake(lvl, x, y, z, center);
        if (p.has(TntEffect.METEOR)) meteorShower(lvl, x, y, z);
        if (p.has(TntEffect.STORM)) massiveStorm(lvl, x, y, z);
        if (p.has(TntEffect.COLOSSAL)) colossalExplosion(lvl, x, y, z, center);
        if (p.has(TntEffect.SUPERNOVA)) supernova(lvl, x, y, z);

        // sonido propio de la explosion
        lvl.playSound(null, x, y, z, ModSounds.explode(getVariantName()), SoundSource.BLOCKS, 2.0F, 1.0F);

        // advancement: "explota una TNT de cada tipo"
        if (ownerUuid != null && lvl instanceof ServerLevel serverLevel) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(ownerUuid);
            if (player != null) {
                ModTriggers.EXPLODED.trigger(player, new ResourceLocation(blockId));
            }
        }
    }

    // ---------- efectos especiales (portados del antiguo BlockEntity) ----------

    private void freezeWater(Level lvl, BlockPos center, int radius) {
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {
            if (lvl.getBlockState(p).getBlock() == Blocks.WATER) {
                lvl.setBlock(p, Blocks.ICE.defaultBlockState(), 3);
            }
        }
    }

    private void snowCover(Level lvl, BlockPos center, int radius) {
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-radius, -1, -radius),
                center.offset(radius, 1, radius))) {
            if (lvl.isEmptyBlock(p) && lvl.getBlockState(p.below()).isSolid()) {
                int layers = 1 + lvl.random.nextInt(3);
                lvl.setBlock(p, Blocks.SNOW.defaultBlockState()
                        .setValue(SnowLayerBlock.LAYERS, layers), 3);
            }
        }
    }

    private void lavaPools(Level lvl, BlockPos center, int radius) {
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-radius, -2, -radius),
                center.offset(radius, 2, radius))) {
            if (lvl.isEmptyBlock(p)
                    && lvl.getBlockState(p.below()).isSolid()
                    && lvl.random.nextInt(4) != 0) {
                lvl.setBlock(p, Blocks.LAVA.defaultBlockState(), 3);
            }
        }
    }

    private void obsidianCrater(Level lvl, BlockPos center, int radius) {
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-radius, -2, -radius),
                center.offset(radius, 1, radius))) {
            if (lvl.isEmptyBlock(p) && lvl.getBlockState(p.below()).isSolid()) {
                lvl.setBlock(p, Blocks.OBSIDIAN.defaultBlockState(), 3);
            }
        }
    }

    private void waterFlood(Level lvl, BlockPos center, int radius) {
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-radius, -3, -radius),
                center.offset(radius, 1, radius))) {
            BlockState s = lvl.getBlockState(p);
            if (s.getBlock() == Blocks.FIRE) {
                lvl.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            } else if (lvl.isEmptyBlock(p)) {
                lvl.setBlock(p, Blocks.WATER.defaultBlockState(), 3);
            }
        }
    }

    private void sandFall(Level lvl, BlockPos center, int radius) {
        if (!(lvl instanceof ServerLevel serverLevel)) return;
        for (int i = 0; i < 14; i++) {
            int ox = center.getX() + lvl.random.nextInt(radius * 2 + 1) - radius;
            int oz = center.getZ() + lvl.random.nextInt(radius * 2 + 1) - radius;
            int oy = center.getY() + 10 + lvl.random.nextInt(8);
            FallingBlockEntity.fall(serverLevel, new BlockPos(ox, oy, oz), Blocks.SAND.defaultBlockState());
        }
    }

    private void launchEntities(Level lvl, double x, double y, double z, double radius) {
        AABB box = new AABB(x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius);
        for (LivingEntity entity : lvl.getEntitiesOfClass(LivingEntity.class, box)) {
            Vec3 p = entity.position();
            double dx = p.x - x;
            double dz = p.z - z;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < 0.001) continue;
            entity.push(dx / dist * 3.0, 2.6, dz / dist * 3.0);
            entity.hurtMarked = true;
        }
    }

    private void cryoFreeze(Level lvl, double x, double y, double z, double radius) {
        AABB box = new AABB(x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius);
        for (LivingEntity entity : lvl.getEntitiesOfClass(LivingEntity.class, box)) {
            entity.setTicksFrozen(Math.min(300, entity.getTicksRequiredToFreeze() + 200));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2));
        }
    }

    private void nuclearFx(Level lvl, double x, double y, double z) {
        if (lvl instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0, 0, 0, 0);
            for (int i = 0; i < 20; i++) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        x + (lvl.random.nextDouble() - 0.5) * 10,
                        y + lvl.random.nextDouble() * 8,
                        z + (lvl.random.nextDouble() - 0.5) * 10,
                        2, 0.1, 0.1, 0.1, 0.0);
            }
        }
    }

    private void strikeLightning(Level lvl, double x, double y, double z) {
        if (lvl instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 3; i++) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (bolt == null) continue;
                bolt.moveTo(
                        x + (lvl.random.nextDouble() - 0.5) * 8,
                        y,
                        z + (lvl.random.nextDouble() - 0.5) * 8);
                serverLevel.addFreshEntity(bolt);
            }
        }
    }

    /** Deja caer items (Oro, Diamante, Esmeralda...). */
    private void dropItems(Level lvl, double x, double y, double z, net.minecraft.world.item.Item item, int total) {
        while (total > 0) {
            int count = Math.min(total, 16);
            total -= count;
            ItemEntity drop = new ItemEntity(lvl, x, y + 0.5, z, new ItemStack(item, count));
            drop.setDeltaMovement(
                    (lvl.random.nextDouble() - 0.5) * 0.5,
                    0.4,
                    (lvl.random.nextDouble() - 0.5) * 0.5);
            lvl.addFreshEntity(drop);
        }
    }

    /**
     * TNT Agujero Negro: spawna la bola negra 3D (entidad) que flota sobre
     * el crater atrayendo entidades e items durante 5 segundos.
     */
    private void spawnBlackHole(Level lvl, double x, double y, double z) {
        if (!(lvl instanceof ServerLevel serverLevel)) return;
        serverLevel.addFreshEntity(new BlackHoleEntity(serverLevel, x, y, z));
    }

    /**
     * TNT Inferno: invoca mobs del Nether hostiles alrededor del crater,
     * que atacan al jugador mas cercano (con particulas de invocacion).
     */
    private void spawnNetherMobs(Level lvl, BlockPos center) {
        if (!(lvl instanceof ServerLevel serverLevel)) return;
        net.minecraft.world.entity.player.Player nearest = null;
        double best = Double.MAX_VALUE;
        for (ServerPlayer sp : serverLevel.players()) {
            double d = sp.distanceToSqr(center.getX(), center.getY(), center.getZ());
            if (d < best) {
                best = d;
                nearest = sp;
            }
        }
        int count = 3 + lvl.random.nextInt(4);
        for (int i = 0; i < count; i++) {
            double a = lvl.random.nextDouble() * Math.PI * 2;
            double r = 3 + lvl.random.nextDouble() * 4;
            BlockPos spawn = serverLevel.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                    new BlockPos((int) (center.getX() + Math.cos(a) * r),
                            center.getY(),
                            (int) (center.getZ() + Math.sin(a) * r)));
            net.minecraft.world.entity.Mob mob = switch (lvl.random.nextInt(4)) {
                case 0 -> new net.minecraft.world.entity.monster.Blaze(EntityType.BLAZE, lvl);
                case 1 -> new net.minecraft.world.entity.monster.MagmaCube(EntityType.MAGMA_CUBE, lvl);
                case 2 -> new net.minecraft.world.entity.monster.ZombifiedPiglin(EntityType.ZOMBIFIED_PIGLIN, lvl);
                default -> new net.minecraft.world.entity.monster.WitherSkeleton(EntityType.WITHER_SKELETON, lvl);
            };
            mob.setPos(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
            serverLevel.addFreshEntity(mob);
            if (nearest != null) {
                mob.setTarget(nearest);
            }
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5,
                    20, 0.4, 0.6, 0.4, 0.1);
        }
    }

    /** TNT de Viento: empuja a todo (seres e items) lejos del crater. */
    private void windPush(Level lvl, double x, double y, double z, double radius) {
        AABB box = new AABB(x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius);
        for (Entity e : lvl.getEntitiesOfClass(Entity.class, box,
                e -> !(e instanceof TntsPrimedTnt))) {
            Vec3 p = e.position();
            Vec3 dir = new Vec3(p.x - x, (p.y - y) * 0.3 + 0.5, p.z - z);
            double dist = dir.horizontalDistance();
            if (dist < 0.001) continue;
            double strength = 2.6 * (1 - Math.min(1, dist / radius)) + 0.5;
            e.setDeltaMovement(e.getDeltaMovement().add(dir.normalize().scale(strength)));
            e.hurtMarked = true;
        }
        if (lvl instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 30; i++) {
                double a = serverLevel.random.nextDouble() * Math.PI * 2;
                double r = serverLevel.random.nextDouble() * radius;
                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        x + Math.cos(a) * r, y + 1 + serverLevel.random.nextDouble(), z + Math.sin(a) * r,
                        1, 0, 0, 0, 0);
            }
        }
    }

    /** TNT Inferno: incendia una gran area. */
    private void inferno(Level lvl, BlockPos center, int radius) {
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-radius, -2, -radius),
                center.offset(radius, 4, radius))) {
            if (lvl.isEmptyBlock(p) && lvl.getBlockState(p.below()).isSolid()
                    && lvl.random.nextInt(3) != 0) {
                lvl.setBlock(p, Blocks.FIRE.defaultBlockState(), 3);
            }
        }
    }

    /** TNT de Setas: esparce setas y micelio. */
    private void fungiSpread(Level lvl, BlockPos center, int radius) {
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-radius, -2, -radius),
                center.offset(radius, 2, radius))) {
            if (lvl.isEmptyBlock(p) && lvl.getBlockState(p.below()).isSolid()) {
                lvl.setBlock(p, (lvl.random.nextBoolean() ? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM)
                        .defaultBlockState(), 3);
            }
            BlockState s = lvl.getBlockState(p);
            if ((s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT)) && lvl.random.nextInt(3) == 0) {
                lvl.setBlock(p, Blocks.MYCELIUM.defaultBlockState(), 3);
            }
        }
    }

    /** TNT de Miel: lentitud pegajosa + bloques de miel en el crater. */
    private void honeyGoo(Level lvl, double x, double y, double z, double radius) {
        AABB box = new AABB(x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius);
        for (LivingEntity e : lvl.getEntitiesOfClass(LivingEntity.class, box)) {
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 3));
        }
        BlockPos center = BlockPos.containing(x, y, z);
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-3, -2, -3), center.offset(3, 1, 3))) {
            if (lvl.isEmptyBlock(p) && lvl.getBlockState(p.below()).isSolid()
                    && lvl.random.nextInt(3) != 0) {
                lvl.setBlock(p, Blocks.HONEY_BLOCK.defaultBlockState(), 3);
            }
        }
    }

    /** TNT Curativa: cura y da regeneracion a los seres vivos cercanos. */
    private void healBurst(Level lvl, double x, double y, double z, double radius) {
        AABB box = new AABB(x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius);
        for (LivingEntity e : lvl.getEntitiesOfClass(LivingEntity.class, box)) {
            if (!e.isAlive()) continue;
            e.heal(10.0F);
            e.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));
            if (lvl instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HEART,
                        e.getX(), e.getY() + 1, e.getZ(), 3, 0.2, 0.2, 0.2, 0);
            }
        }
    }

    /** TNT Teletransportadora: manda a los seres vivos a sitios aleatorios. */
    private void teleportScramble(Level lvl, double x, double y, double z, double radius) {
        if (!(lvl instanceof ServerLevel serverLevel)) return;
        AABB box = new AABB(x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius);
        for (LivingEntity e : serverLevel.getEntitiesOfClass(LivingEntity.class, box)) {
            double nx = x + (serverLevel.random.nextDouble() - 0.5) * 40;
            double nz = z + (serverLevel.random.nextDouble() - 0.5) * 40;
            if (e.randomTeleport(nx, e.getY(), nz, true)) {
                serverLevel.sendParticles(ParticleTypes.PORTAL,
                        e.getX(), e.getY() + 1, e.getZ(), 10, 0.3, 0.5, 0.3, 0.1);
            }
        }
    }

    /** TNT de Confeti: lluvia de particulas de colores. */
    private void confetti(Level lvl, double x, double y, double z) {
        if (!(lvl instanceof ServerLevel serverLevel)) return;
        for (int i = 0; i < 60; i++) {
            serverLevel.sendParticles(
                    new DustParticleOptions(
                            new org.joml.Vector3f(
                                    serverLevel.random.nextFloat(),
                                    serverLevel.random.nextFloat(),
                                    serverLevel.random.nextFloat()),
                            1.0F),
                    x + (serverLevel.random.nextDouble() - 0.5) * 14,
                    y + serverLevel.random.nextDouble() * 6,
                    z + (serverLevel.random.nextDouble() - 0.5) * 14,
                    2, 0.1, 0.1, 0.1, 0.02);
        }
        serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 40, 6, 3, 6, 0.1);
    }

    private void xpDrop(Level lvl, double x, double y, double z) {
        int total = 80 + lvl.random.nextInt(70);
        while (total > 0) {
            int value = Math.min(total, 50);
            total -= value;
            lvl.addFreshEntity(new ExperienceOrb(lvl, x, y + 0.5, z, value));
        }
    }

    // ---------- explosiones con personalidad propia ----------

    /**
     * Cada TNT explota con su propio espectaculo: un estallido de polvo del
     * color de la variante (ninguna se ve igual) + particulas caracteristicas
     * (hongo nuclear, lluvia de lava, nieve, chispas, items...).
     */
    private void explosionFx(ServerLevel lvl, double x, double y, double z, TntProperties p) {
        String variant = getVariantName();
        int[] color = TntVfx.colorOf(variant);
        float radius = Math.min(4.5f, Math.max(1.2f, p.power() * 0.4f));
        // estallido de polvo del color propio de la TNT (esfera)
        for (int i = 0; i < 60; i++) {
            double a = lvl.random.nextDouble() * Math.PI * 2;
            double b = (lvl.random.nextDouble() - 0.5) * Math.PI;
            double r = radius * (0.25 + 0.75 * lvl.random.nextDouble());
            lvl.sendParticles(new DustParticleOptions(
                            new org.joml.Vector3f(color[0] / 255f, color[1] / 255f, color[2] / 255f), 1.0F),
                    x + Math.cos(a) * Math.cos(b) * r,
                    y + 0.5 + Math.sin(b) * r,
                    z + Math.sin(a) * Math.cos(b) * r,
                    1, 0, 0, 0, 0.0);
        }
        // firma visual de cada variante
        switch (variant) {
            case "mega_tnt" -> megaShockwave(lvl, x, y, z);
            case "nuclear_tnt" -> nuclearCloud(lvl, x, y, z);
            case "lava_tnt", "inferno_tnt" -> lavaRain(lvl, x, y, z);
            case "hielo_tnt", "crio_tnt" -> iceBurst(lvl, x, y, z);
            case "agua_tnt" -> waterBurst(lvl, x, y, z);
            case "rayo_tnt" -> sparkBurst(lvl, x, y, z);
            case "oro_tnt" -> itemBurst(lvl, x, y, z, Items.GOLD_INGOT);
            case "diamante_tnt" -> itemBurst(lvl, x, y, z, Items.DIAMOND);
            case "esmeralda_tnt" -> itemBurst(lvl, x, y, z, Items.EMERALD);
            case "xp_tnt" -> xpBurst(lvl, x, y, z);
            case "negra_tnt" -> implosionFx(lvl, x, y, z);
            case "viento_tnt" -> windBurst(lvl, x, y, z);
            case "heal_tnt" -> healFx(lvl, x, y, z);
            case "teleport_tnt" -> portalBurst(lvl, x, y, z);
            case "arena_tnt" -> sandBurst(lvl, x, y, z);
            case "saltarina_tnt" -> puffBurst(lvl, x, y, z);
            case "hongo_tnt" -> fungiBurst(lvl, x, y, z);
            case "miel_tnt" -> honeyBurst(lvl, x, y, z);
            case "obsidiana_tnt" -> obsidianBurst(lvl, x, y, z);
            case "rapida_tnt" -> quickFlash(lvl, x, y, z);
            default -> { /* mini, limpia, trampa, confeti, mina: solo el estallido de color */ }
        }
    }

    private void megaShockwave(ServerLevel lvl, double x, double y, double z) {
        lvl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0, 0, 0, 0);
        for (int ring = 2; ring <= 8; ring += 2) {
            for (int i = 0; i < 32; i++) {
                double a = i / 32.0 * Math.PI * 2;
                lvl.sendParticles(new DustParticleOptions(new org.joml.Vector3f(0.92f, 0.92f, 0.92f), 1.2F),
                        x + Math.cos(a) * ring, y + 0.3, z + Math.sin(a) * ring, 1, 0, 0, 0, 0.0);
                lvl.sendParticles(ParticleTypes.SMOKE,
                        x + Math.cos(a) * ring, y + 0.3, z + Math.sin(a) * ring, 1, 0, 0, 0, 0.0);
            }
        }
    }

    private void nuclearCloud(ServerLevel lvl, double x, double y, double z) {
        lvl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0, 0, 0, 0);
        // columna de humo
        for (int i = 0; i < 40; i++) {
            lvl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    x + (lvl.random.nextDouble() - 0.5) * 2.5,
                    y + 1 + lvl.random.nextDouble() * 7,
                    z + (lvl.random.nextDouble() - 0.5) * 2.5,
                    1, 0.05, 0.1, 0.05, 0.02);
        }
        // copa de la seta
        for (int i = 0; i < 50; i++) {
            double a = lvl.random.nextDouble() * Math.PI * 2;
            double r = 3 + lvl.random.nextDouble() * 4;
            lvl.sendParticles(ParticleTypes.CLOUD,
                    x + Math.cos(a) * r, y + 7 + lvl.random.nextDouble() * 2, z + Math.sin(a) * r,
                    1, 0.1, 0.1, 0.1, 0.0);
        }
        // resplandor verde radiactivo
        for (int i = 0; i < 25; i++) {
            lvl.sendParticles(new DustParticleOptions(new org.joml.Vector3f(0.5f, 1.0f, 0.3f), 1.6F),
                    x + (lvl.random.nextDouble() - 0.5) * 10,
                    y + 0.5 + lvl.random.nextDouble() * 4,
                    z + (lvl.random.nextDouble() - 0.5) * 10,
                    1, 0, 0, 0, 0.0);
        }
    }

    private void lavaRain(ServerLevel lvl, double x, double y, double z) {
        for (int i = 0; i < 40; i++) {
            lvl.sendParticles(ParticleTypes.LAVA,
                    x + (lvl.random.nextDouble() - 0.5) * 10,
                    y + 4 + lvl.random.nextDouble() * 6,
                    z + (lvl.random.nextDouble() - 0.5) * 10,
                    1, 0, 0, 0, 0.0);
            lvl.sendParticles(ParticleTypes.FLAME,
                    x + (lvl.random.nextDouble() - 0.5) * 8,
                    y + 0.5 + lvl.random.nextDouble() * 3,
                    z + (lvl.random.nextDouble() - 0.5) * 8,
                    1, 0.1, 0.1, 0.1, 0.0);
        }
    }

    private void iceBurst(ServerLevel lvl, double x, double y, double z) {
        lvl.sendParticles(ParticleTypes.FLASH, x, y, z, 1, 0, 0, 0, 0);
        for (int i = 0; i < 30; i++) {
            lvl.sendParticles(ParticleTypes.SNOWFLAKE,
                    x + (lvl.random.nextDouble() - 0.5) * 8,
                    y + 0.5 + lvl.random.nextDouble() * 3,
                    z + (lvl.random.nextDouble() - 0.5) * 8,
                    2, 0.3, 0.2, 0.3, 0.0);
        }
    }

    private void waterBurst(ServerLevel lvl, double x, double y, double z) {
        for (int i = 0; i < 50; i++) {
            lvl.sendParticles(ParticleTypes.SPLASH,
                    x + (lvl.random.nextDouble() - 0.5) * 8,
                    y + 0.5 + lvl.random.nextDouble() * 3,
                    z + (lvl.random.nextDouble() - 0.5) * 8,
                    1, 0.2, 0.3, 0.2, 0.0);
            lvl.sendParticles(ParticleTypes.BUBBLE,
                    x + (lvl.random.nextDouble() - 0.5) * 8,
                    y + 1 + lvl.random.nextDouble() * 3,
                    z + (lvl.random.nextDouble() - 0.5) * 8,
                    1, 0, 0.1, 0, 0.0);
        }
    }

    private void sparkBurst(ServerLevel lvl, double x, double y, double z) {
        lvl.sendParticles(ParticleTypes.FLASH, x, y, z, 1, 0, 0, 0, 0);
        for (int i = 0; i < 30; i++) {
            lvl.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    x + (lvl.random.nextDouble() - 0.5) * 8,
                    y + 0.5 + lvl.random.nextDouble() * 4,
                    z + (lvl.random.nextDouble() - 0.5) * 8,
                    1, 0.2, 0.2, 0.2, 0.0);
        }
    }

    /** Lluvia de particulas del item (oro, diamante, esmeralda). */
    private void itemBurst(ServerLevel lvl, double x, double y, double z, net.minecraft.world.item.Item item) {
        for (int i = 0; i < 24; i++) {
            lvl.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(item)),
                    x + (lvl.random.nextDouble() - 0.5) * 6,
                    y + 0.5 + lvl.random.nextDouble() * 3,
                    z + (lvl.random.nextDouble() - 0.5) * 6,
                    1, 0.2, 0.4, 0.2, 0.1);
        }
    }

    private void xpBurst(ServerLevel lvl, double x, double y, double z) {
        for (int i = 0; i < 40; i++) {
            lvl.sendParticles(ParticleTypes.END_ROD,
                    x + (lvl.random.nextDouble() - 0.5) * 7,
                    y + 0.5 + lvl.random.nextDouble() * 4,
                    z + (lvl.random.nextDouble() - 0.5) * 7,
                    1, 0.15, 0.25, 0.15, 0.05);
        }
    }

    private void implosionFx(ServerLevel lvl, double x, double y, double z) {
        lvl.sendParticles(ParticleTypes.FLASH, x, y, z, 1, 0, 0, 0, 0);
        for (int i = 0; i < 50; i++) {
            double a = lvl.random.nextDouble() * Math.PI * 2;
            double r = 3 + lvl.random.nextDouble() * 5;
            lvl.sendParticles(ParticleTypes.SMOKE,
                    x + Math.cos(a) * r, y + 0.5 + lvl.random.nextDouble() * 3, z + Math.sin(a) * r,
                    1, 0, 0, 0, 0.0);
        }
        for (int i = 0; i < 30; i++) {
            lvl.sendParticles(ParticleTypes.PORTAL,
                    x + (lvl.random.nextDouble() - 0.5) * 5,
                    y + 0.5 + lvl.random.nextDouble() * 3,
                    z + (lvl.random.nextDouble() - 0.5) * 5,
                    1, 0, 0, 0, 0.0);
        }
    }

    private void windBurst(ServerLevel lvl, double x, double y, double z) {
        for (int i = 0; i < 60; i++) {
            double a = lvl.random.nextDouble() * Math.PI * 2;
            double r = 1 + lvl.random.nextDouble() * 8;
            lvl.sendParticles(ParticleTypes.CLOUD,
                    x + Math.cos(a) * r, y + 0.5 + lvl.random.nextDouble() * 2, z + Math.sin(a) * r,
                    1, 0, 0, 0, 0.0);
        }
    }

    private void healFx(ServerLevel lvl, double x, double y, double z) {
        for (int i = 0; i < 25; i++) {
            lvl.sendParticles(ParticleTypes.HEART,
                    x + (lvl.random.nextDouble() - 0.5) * 6,
                    y + 0.5 + lvl.random.nextDouble() * 3,
                    z + (lvl.random.nextDouble() - 0.5) * 6,
                    1, 0.2, 0.3, 0.2, 0.0);
        }
        for (int i = 0; i < 30; i++) {
            lvl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    x + (lvl.random.nextDouble() - 0.5) * 6,
                    y + 0.5 + lvl.random.nextDouble() * 4,
                    z + (lvl.random.nextDouble() - 0.5) * 6,
                    1, 0.1, 0.2, 0.1, 0.0);
        }
    }

    private void portalBurst(ServerLevel lvl, double x, double y, double z) {
        for (int i = 0; i < 60; i++) {
            double a = lvl.random.nextDouble() * Math.PI * 2;
            double r = 0.5 + lvl.random.nextDouble() * 6;
            lvl.sendParticles(ParticleTypes.PORTAL,
                    x + Math.cos(a) * r, y + 0.5 + lvl.random.nextDouble() * 4, z + Math.sin(a) * r,
                    1, 0.1, 0.2, 0.1, 0.0);
        }
    }

    private void sandBurst(ServerLevel lvl, double x, double y, double z) {
        for (int i = 0; i < 50; i++) {
            lvl.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                    x + (lvl.random.nextDouble() - 0.5) * 8,
                    y + 4 + lvl.random.nextDouble() * 5,
                    z + (lvl.random.nextDouble() - 0.5) * 8,
                    1, 0, 0, 0, 0.0);
            lvl.sendParticles(new DustParticleOptions(new org.joml.Vector3f(0.9f, 0.8f, 0.5f), 1.0F),
                    x + (lvl.random.nextDouble() - 0.5) * 7,
                    y + 0.5 + lvl.random.nextDouble() * 3,
                    z + (lvl.random.nextDouble() - 0.5) * 7,
                    1, 0, 0, 0, 0.0);
        }
    }

    private void puffBurst(ServerLevel lvl, double x, double y, double z) {
        for (int i = 0; i < 40; i++) {
            lvl.sendParticles(ParticleTypes.POOF,
                    x + (lvl.random.nextDouble() - 0.5) * 5,
                    y + 0.5 + lvl.random.nextDouble() * 2,
                    z + (lvl.random.nextDouble() - 0.5) * 5,
                    1, 0.2, 0.5, 0.2, 0.0);
        }
    }

    private void fungiBurst(ServerLevel lvl, double x, double y, double z) {
        for (int i = 0; i < 40; i++) {
            boolean red = lvl.random.nextBoolean();
            lvl.sendParticles(new DustParticleOptions(
                            new org.joml.Vector3f(red ? 0.85f : 0.45f, red ? 0.2f : 0.25f, red ? 0.2f : 0.1f), 1.2F),
                    x + (lvl.random.nextDouble() - 0.5) * 7,
                    y + 0.5 + lvl.random.nextDouble() * 3,
                    z + (lvl.random.nextDouble() - 0.5) * 7,
                    1, 0, 0, 0, 0.0);
        }
        lvl.sendParticles(ParticleTypes.MYCELIUM, x, y + 0.5, z, 20, 3, 2, 3, 0.0);
    }

    private void honeyBurst(ServerLevel lvl, double x, double y, double z) {
        for (int i = 0; i < 40; i++) {
            lvl.sendParticles(new DustParticleOptions(new org.joml.Vector3f(1.0f, 0.75f, 0.1f), 1.2F),
                    x + (lvl.random.nextDouble() - 0.5) * 7,
                    y + 0.5 + lvl.random.nextDouble() * 3,
                    z + (lvl.random.nextDouble() - 0.5) * 7,
                    1, 0, 0, 0, 0.0);
        }
        lvl.sendParticles(ParticleTypes.DRIPPING_HONEY, x, y + 2, z, 20, 3, 1, 3, 0.0);
    }

    private void obsidianBurst(ServerLevel lvl, double x, double y, double z) {
        for (int i = 0; i < 40; i++) {
            lvl.sendParticles(new DustParticleOptions(new org.joml.Vector3f(0.55f, 0.36f, 0.96f), 1.1F),
                    x + (lvl.random.nextDouble() - 0.5) * 8,
                    y + 0.5 + lvl.random.nextDouble() * 4,
                    z + (lvl.random.nextDouble() - 0.5) * 8,
                    1, 0, 0, 0, 0.0);
            lvl.sendParticles(ParticleTypes.SMOKE,
                    x + (lvl.random.nextDouble() - 0.5) * 6,
                    y + 0.5 + lvl.random.nextDouble() * 3,
                    z + (lvl.random.nextDouble() - 0.5) * 6,
                    1, 0.1, 0.2, 0.1, 0.0);
        }
    }

    private void quickFlash(ServerLevel lvl, double x, double y, double z) {
        lvl.sendParticles(ParticleTypes.FLASH, x, y, z, 1, 0, 0, 0, 0);
        lvl.sendParticles(ParticleTypes.CRIT, x, y, z, 30, 3, 2, 3, 0.1);
    }

    // ========== TNTs MASIVAS NUEVAS ==========

    /**
     * Puede este bloque ser destruido por una TNT masiva?
     * Usa la dureza del bloque (getDestroySpeed, sin jugador — evita el NPE
     * de getDestroyProgress(null,...) en Forge). Bedrock tiene dureza -1
     * y no se destruye nunca; la obsidiana (50) tampoco.
     */
    private static boolean canDestroy(BlockState state, Level lvl, BlockPos p) {
        float hardness = state.getDestroySpeed(lvl, p);
        return hardness >= 0.0f && hardness < 40.0f;
    }

    /**
     * TNT Terremoto: destruye bloques en oleadas concentricas, lanza entidades
     * alto, crea grietas con lava/piedra, temblor fuerte.
     */
    private void earthquake(Level lvl, double x, double y, double z, BlockPos center) {
        if (!(lvl instanceof ServerLevel serverLevel)) return;
        // Ola 1: crater central (radio 6)
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-6, -4, -6), center.offset(6, 2, 6))) {
            double dist = Math.sqrt(p.distSqr(center.offset(0, 2, 0)));
            if (dist < 6 && lvl.random.nextInt(3) != 0) {
                BlockState state = lvl.getBlockState(p);
                if (!state.isAir() && canDestroy(state, lvl, p)) {
                    lvl.destroyBlock(p, true);
                }
            }
        }
        // Ola 2: grietas con lava (lineas desde el centro)
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            for (int d = 4; d <= 12; d++) {
                int bx = (int)(x + Math.cos(angle) * d);
                int bz = (int)(z + Math.sin(angle) * d);
                BlockPos fissure = new BlockPos(bx, center.getY() - 1, bz);
                if (lvl.random.nextInt(3) == 0) {
                    lvl.setBlock(fissure, Blocks.LAVA.defaultBlockState(), 3);
                } else if (lvl.random.nextInt(2) == 0) {
                    lvl.destroyBlock(fissure.above(), true);
                }
            }
        }
        // Lanzar entidades alto
        AABB box = new AABB(x - 12, y - 4, z - 12, x + 12, y + 4, z + 12);
        for (LivingEntity e : lvl.getEntitiesOfClass(LivingEntity.class, box)) {
            e.push(0, 3.5 + lvl.random.nextDouble() * 2, 0);
            e.hurtMarked = true;
        }
        // Particulas de terremoto
        for (int i = 0; i < 80; i++) {
            double a = lvl.random.nextDouble() * Math.PI * 2;
            double r = lvl.random.nextDouble() * 10;
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.DIRT.defaultBlockState()),
                    x + Math.cos(a) * r, y + 0.5 + lvl.random.nextDouble() * 2,
                    z + Math.sin(a) * r, 3, 0.3, 0.3, 0.3, 0);
            serverLevel.sendParticles(new DustParticleOptions(
                            new org.joml.Vector3f(0.6f, 0.5f, 0.3f), 1.0F),
                    x + Math.cos(a) * r, y + 0.5, z + Math.sin(a) * r,
                    1, 0, 0, 0, 0);
        }
        // Temblor fuerte
        for (ServerPlayer sp : serverLevel.players()) {
            if (sp.distanceToSqr(x, y, z) <= 14 * 14) {
                sp.animateHurt(sp.getYRot());
            }
        }
    }

    /**
     * TNT Meteorito: llama 5-8 meteoritos de obsidiana del cielo que caen
     * con estela de fuego y crean crateres pequeños al impactar.
     */
    private void meteorShower(Level lvl, double x, double y, double z) {
        if (!(lvl instanceof ServerLevel serverLevel)) return;
        int numMeteors = 5 + lvl.random.nextInt(4);
        for (int i = 0; i < numMeteors; i++) {
            // Posicion de caida aleatoria
            double mx = x + (lvl.random.nextDouble() - 0.5) * 30;
            double mz = z + (lvl.random.nextDouble() - 0.5) * 30;
            int my = serverLevel.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, (int) mx, (int) mz);
            // Estela de fuego cayendo
            for (int j = 0; j < 20; j++) {
                double ty = my + 30 - j * 1.5;
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        mx + (lvl.random.nextDouble() - 0.5) * 0.5,
                        ty,
                        mz + (lvl.random.nextDouble() - 0.5) * 0.5,
                        3, 0.1, -0.5, 0.1, 0.02);
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        mx + (lvl.random.nextDouble() - 0.5) * 0.3,
                        ty,
                        mz + (lvl.random.nextDouble() - 0.5) * 0.3,
                        2, 0.05, -0.2, 0.05, 0);
            }
            // Impacto: explosion pequena + crater de fuego
            BlockPos impact = new BlockPos((int) mx, my, (int) mz);
            lvl.explode(null, mx, my + 1, mz, 3.0f + lvl.random.nextFloat() * 2, true,
                    Level.ExplosionInteraction.BLOCK);
            // Lava en el crater
            for (BlockPos p : BlockPos.betweenClosed(impact.offset(-2, -1, -2), impact.offset(2, 0, 2))) {
                if (lvl.isEmptyBlock(p) && lvl.getBlockState(p.below()).isSolid()
                        && lvl.random.nextInt(3) != 0) {
                    lvl.setBlock(p, Blocks.LAVA.defaultBlockState(), 3);
                }
            }
            // Destello de impacto
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    mx, my + 1, mz, 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    mx, my + 2, mz, 8, 1, 1, 1, 0.1);
        }
        // Sonido de impacto            lvl.playSound(null, x, y, z, ModSounds.explode("mini_tnt"), SoundSource.BLOCKS, 3.0F, 0.6F);
    }

    /**
     * TNT Tormenta: lluvia de 12-18 rayos masivos, viento fuerte,
     * lluvia intensa y cielo oscuro.
     */
    private void massiveStorm(Level lvl, double x, double y, double z) {
        if (!(lvl instanceof ServerLevel serverLevel)) return;
        int numBolts = 12 + lvl.random.nextInt(7);
        for (int i = 0; i < numBolts; i++) {
            double bx = x + (lvl.random.nextDouble() - 0.5) * 20;
            double bz = z + (lvl.random.nextDouble() - 0.5) * 20;
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (bolt != null) {
                bolt.moveTo(bx, y, bz);
                serverLevel.addFreshEntity(bolt);
            }
            // Destello extra
            serverLevel.sendParticles(ParticleTypes.FLASH, bx, y + 2, bz, 1, 0, 0, 0, 0);
        }
        // Viento fuerte (empuja todo lejos)
        AABB box = new AABB(x - 16, y - 4, z - 16, x + 16, y + 8, z + 16);
        for (Entity e : lvl.getEntitiesOfClass(Entity.class, box,
                e -> !(e instanceof TntsPrimedTnt))) {
            Vec3 p = e.position();
            Vec3 dir = new Vec3(p.x - x, 0.3, p.z - z);
            double dist = dir.horizontalDistance();
            if (dist < 0.001) continue;
            double strength = 3.0 * (1 - Math.min(1, dist / 16)) + 0.5;
            e.setDeltaMovement(e.getDeltaMovement().add(dir.normalize().scale(strength)));
            e.hurtMarked = true;
        }
        // Lluvia de particulas
        for (int i = 0; i < 50; i++) {
            double rx = x + (lvl.random.nextDouble() - 0.5) * 16;
            double rz = z + (lvl.random.nextDouble() - 0.5) * 16;
            serverLevel.sendParticles(ParticleTypes.CLOUD, rx, y + 8, rz, 1, 0, -1, 0, 0);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    rx + (lvl.random.nextDouble() - 0.5) * 2,
                    y + lvl.random.nextDouble() * 6,
                    rz + (lvl.random.nextDouble() - 0.5) * 2,
                    2, 0, 0, 0, 0);
        }
        // Sonido de tormenta
        lvl.playSound(null, x, y, z, ModSounds.explode("rayo_tnt"), SoundSource.BLOCKS, 4.0F, 0.5F);
    }

    /**
     * TNT Colosal: explosion en 3 oleadas progresivas (cada 8 ticks)
     * que destruye bloques sin lag.
     */
    private void colossalExplosion(Level lvl, double x, double y, double z, BlockPos center) {
        if (!(lvl instanceof ServerLevel serverLevel)) return;
        // Ola 1: crater pequeno (radio 4)
        serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                serverLevel.getServer().getTickCount() + 8, () -> {
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-4, -3, -4), center.offset(4, 2, 4))) {
                double dist = Math.sqrt(p.distSqr(center.offset(0, 1, 0)));
                if (dist < 4 && lvl.random.nextInt(2) == 0) {
                    BlockState state = lvl.getBlockState(p);
                    if (!state.isAir() && canDestroy(state, lvl, p)) {
                        lvl.destroyBlock(p, true);
                    }
                }
            }
            lvl.playSound(null, x, y, z, ModSounds.explode("mini_tnt"), SoundSource.BLOCKS, 2.5F, 0.8F);
        }));
        // Ola 2: crater mediano (radio 8)
        serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                serverLevel.getServer().getTickCount() + 16, () -> {
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-8, -4, -8), center.offset(8, 2, 8))) {
                double dist = Math.sqrt(p.distSqr(center.offset(0, 1, 0)));
                if (dist < 8 && dist >= 3 && lvl.random.nextInt(2) == 0) {
                    BlockState state = lvl.getBlockState(p);
                    if (!state.isAir() && canDestroy(state, lvl, p)) {
                        lvl.destroyBlock(p, true);
                    }
                }
            }
            // Lanzar entidades
            AABB box = new AABB(x - 10, y - 3, z - 10, x + 10, y + 5, z + 10);
            for (LivingEntity e : lvl.getEntitiesOfClass(LivingEntity.class, box)) {
                e.push(0, 2.5, 0);
                e.hurtMarked = true;
            }
            lvl.playSound(null, x, y, z, ModSounds.explode("mini_tnt"), SoundSource.BLOCKS, 3.0F, 0.7F);
        }));
        // Ola 3: crater grande (radio 12)
        serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                serverLevel.getServer().getTickCount() + 24, () -> {
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-12, -5, -12), center.offset(12, 3, 12))) {
                double dist = Math.sqrt(p.distSqr(center.offset(0, 1, 0)));
                if (dist < 12 && dist >= 6 && lvl.random.nextInt(3) == 0) {
                    BlockState state = lvl.getBlockState(p);
                    if (!state.isAir() && canDestroy(state, lvl, p)) {
                        lvl.destroyBlock(p, true);
                    }
                }
            }
            // Fuego en los bordes
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-12, -1, -12), center.offset(12, 2, 12))) {
                double dist = Math.sqrt(p.distSqr(center.offset(0, 0, 0)));
                if (dist > 8 && dist < 12 && lvl.isEmptyBlock(p) && lvl.random.nextInt(4) == 0) {
                    lvl.setBlock(p, Blocks.FIRE.defaultBlockState(), 3);
                }
            }
            lvl.playSound(null, x, y, z, ModSounds.explode("mega_tnt"), SoundSource.BLOCKS, 4.0F, 0.5F);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 3, 2, 1, 2, 0);
        }));
        // Particulas inmediatas
        for (int i = 0; i < 40; i++) {
            double a = lvl.random.nextDouble() * Math.PI * 2;
            double r = lvl.random.nextDouble() * 6;
            serverLevel.sendParticles(new DustParticleOptions(
                            new org.joml.Vector3f(1.0f, 0.6f, 0.1f), 1.2F),
                    x + Math.cos(a) * r, y + 0.5, z + Math.sin(a) * r,
                    2, 0.2, 0.2, 0.2, 0);
        }
    }

    /**
     * TNT Supernova: expansion de luz seguida de colapso, massive XP drop,
     * destello cegador y onda de choque purpura.
     */
    private void supernova(Level lvl, double x, double y, double z) {
        if (!(lvl instanceof ServerLevel serverLevel)) return;
        // Destello cegador inicial
        serverLevel.sendParticles(ParticleTypes.FLASH, x, y + 1, z, 5, 0, 0, 0, 0);
        serverLevel.sendParticles(ParticleTypes.END_ROD,
                x, y + 1, z, 80, 8, 4, 8, 0.2);
        // Ondas expansivas de luz (3 anillos)
        for (int ring = 0; ring < 3; ring++) {
            for (int i = 0; i < 40; i++) {
                double a = i / 40.0 * Math.PI * 2;
                double r = 2 + ring * 4;
                serverLevel.sendParticles(new DustParticleOptions(
                                new org.joml.Vector3f(1.0f, 0.95f, 0.6f), 1.3F),
                        x + Math.cos(a) * r, y + 0.5 + ring * 0.5, z + Math.sin(a) * r,
                        2, 0.1, 0.1, 0.1, 0);
            }
        }
        // Massive XP drop
        int totalXp = 200 + lvl.random.nextInt(100);
        while (totalXp > 0) {
            int value = Math.min(totalXp, 50);
            totalXp -= value;
            lvl.addFreshEntity(new ExperienceOrb(lvl, x, y + 1, z, value));
        }
        // Entidades cercanas reciben daño leve + regeneracion
        AABB box = new AABB(x - 10, y - 4, z - 10, x + 10, y + 6, z + 10);
        for (LivingEntity e : lvl.getEntitiesOfClass(LivingEntity.class, box)) {
            e.hurt(e.damageSources().magic(), 4.0f);
            e.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2));
            e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 400, 0));
        }
        // Sonido de supernova
        lvl.playSound(null, x, y, z, ModSounds.explode("supernova_tnt"), SoundSource.BLOCKS, 4.0F, 0.4F);
        lvl.playSound(null, x, y, z, ModSounds.explode("negra_tnt"), SoundSource.BLOCKS, 3.0F, 0.5F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("block", blockId);
        if (ownerUuid != null) {
            tag.putUUID("owner", ownerUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.blockId = tag.getString("block");
        if (this.blockId.isEmpty()) this.blockId = "tnts:mini_tnt";
        this.entityData.set(DATA_BLOCK, this.blockId);
        if (tag.hasUUID("owner")) {
            this.ownerUuid = tag.getUUID("owner");
        }
    }
}
