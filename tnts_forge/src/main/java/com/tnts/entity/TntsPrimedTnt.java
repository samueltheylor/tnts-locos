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
import net.minecraft.world.item.ItemStack;
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

    /** Multiplicador de radio (Corona del Rey TNT: 1.5x). */
    private float powerMul = 1.0f;

    /**
     * TNT REAL del Rey TNT: mecha corta, radio enorme, pitido de aviso, y
     * solo se frena desactivandola con tijeras (eso aturde al Rey).
     */
    private boolean royal = false;

    /** Marca esta TNT como estatica (no se mueve de su sitio al encenderse). */
    public void setStationary(boolean stationary) {
        this.stationary = stationary;
    }

    /** Ajusta el multiplicador de radio de la explosion. */
    public void setPowerMul(float powerMul) {
        this.powerMul = powerMul;
    }

    /** Marca esta TNT como REAL (la que lanza el Rey TNT en modo furia). */
    public void setRoyal(boolean royal) {
        this.royal = royal;
    }

    public boolean isRoyal() {
        return this.royal;
    }

    /**
     * Desactivar la TNT con tijeras: click derecho con shears sobre la TNT
     * encendida la apaga y te devuelve el bloque. Evita la reaccion en cadena.
     */
    @Override
    public net.minecraft.world.InteractionResult interact(
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        if (this.level().isClientSide) return net.minecraft.world.InteractionResult.sidedSuccess(true);
        net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
        if (stack.is(net.minecraft.world.item.Items.SHEARS) && !this.isRemoved()) {
            // apagar: desaparece y se devuelve el bloque
            this.discard();
            if (!royal) {
                // TNT normal: se devuelve el bloque
                ItemStack blockStack = new ItemStack(
                        net.minecraft.world.item.Item.byBlock(
                                ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId))));
                if (!blockStack.isEmpty()) {
                    ItemEntity drop = new ItemEntity(this.level(),
                            this.getX(), this.getY() + 0.2, this.getZ(), blockStack);
                    drop.setDeltaMovement(0, 0.2, 0);
                    this.level().addFreshEntity(drop);
                }
            } else {
                // TNT REAL del Rey: no se devuelve nada, pero desactivarla
                // ATURDE al Rey 5 segundos -> ventana de dano gratis
                AABB box = new AABB(this.blockPosition()).inflate(24);
                for (TntKingEntity king : this.level().getEntitiesOfClass(
                        TntKingEntity.class, box)) {
                    king.setStunned(100);
                }
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.FLASH,
                            this.getX(), this.getY() + 0.5, this.getZ(), 1, 0, 0, 0, 0);
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            this.getX(), this.getY() + 0.5, this.getZ(),
                            20, 0.4, 0.4, 0.4, 0.05);
                }
            }
            stack.hurtAndBreak(1, player, (living) -> living.broadcastBreakEvent(hand));
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.POOF,
                        this.getX(), this.getY() + 0.5, this.getZ(), 8, 0.2, 0.2, 0.2, 0.02);
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        this.getX(), this.getY() + 0.9, this.getZ(), 4, 0.1, 0.05, 0.1, 0.0);
                serverLevel.playSound(null, this.blockPosition(), ModSounds.DEFUSE.get(),
                        SoundSource.BLOCKS, 0.8F, 1.0F);
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(true);
        }
        return super.interact(player, hand);
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
        // (cantidad segun la calidad de particulas de la config). Se emiten
        // cada 2 ticks: con reacciones en cadena de 20-40 TNTs la mitad del
        // trafico de red sin cambio visual perceptible.
        double fx = this.getX();
        double fy = this.getY() + 0.95;
        double fz = this.getZ();
        if ((fuse & 1) == 0) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, fx, fy, fz,
                    com.tnts.config.TntsConfig.particles(3), 0.12, 0.05, 0.12, 0.0);
            serverLevel.sendParticles(ParticleTypes.SMALL_FLAME, fx, fy, fz,
                    com.tnts.config.TntsConfig.particles(2), 0.08, 0.03, 0.08, 0.01);
            serverLevel.sendParticles(ParticleTypes.FLAME, fx, fy, fz,
                    com.tnts.config.TntsConfig.particles(1), 0.04, 0.0, 0.04, 0.0);
        }
        // punto de luz rojo que parpadea sobre la cara superior (4 ticks encendido / 4
        // apagado). Se basa en la mecha (no en tickCount): las TNTs estaticas no
        // incrementan tickCount, asi que antes el punto nunca parpadeaba en ellas.
        if ((fuse / 4) % 2 == 0) {
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
        // pitido de aviso de la TNT REAL del Rey (cuentas atras urgente)
        if (royal && fuse <= 25 && fuse % 5 == 0) {
            float pitch = 1.2f + (25 - fuse) * 0.04f;
            serverLevel.playSound(null, this.blockPosition(), ModSounds.BEEP.get(),
                    SoundSource.HOSTILE, 2.0F, pitch);
            serverLevel.sendParticles(new DustParticleOptions(
                            new org.joml.Vector3f(1.0F, 0.2F, 0.5F), 1.3F),
                    fx, fy, fz, 3, 0.1, 0.05, 0.1, 0.02);
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

        // si la TNT esta desactivada en config, no explota (red de seguridad)
        if (!com.tnts.config.TntsConfig.isEnabled(getVariantName())) return;

        // TNT REAL del Rey: radio 2.5x y siempre incendia (no se puede
        // aguantar; hay que desactivarla con tijeras)
        float boomPower = royal ? p.power() * 2.5f : p.power() * powerMul;
        lvl.explode(this, x, y, z, boomPower, royal || p.fire(),
                p.breaksBlocks() ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE);

        if (lvl.isClientSide) return; // el resto es solo servidor

        ServerLevel sl = (ServerLevel) lvl;
        explosionFx(sl, x, y, z, p);

        // particleQuality=2: espectaculo extra en las TNTs masivas — anillos de
        // colores expansivos + destello de camara, con bucles acotados
        if (com.tnts.config.TntsConfig.maxQuality() && isMassive(p)) {
            massiveQualityShow(sl, x, y, z);
        }

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
        // === NUEVAS 1.9.3 ===
        if (p.has(TntEffect.TOXIC)) toxicCloud(lvl, x, y, z);
        if (p.has(TntEffect.FIREWORKS)) fireworkShow(lvl, x, y, z);
        if (p.has(TntEffect.GRAVITY)) gravityCrush(lvl, x, y, z, center);
        // === NUEVAS 1.10.0 ===
        if (p.has(TntEffect.ENDER)) enderBlast(lvl, x, y, z);
        if (p.has(TntEffect.BUBBLE)) bubblePull(lvl, x, y, z);
        if (p.has(TntEffect.SOLAR)) solarBlast(lvl, x, y, z);
        // === NUEVAS 1.10.12 ===
        if (p.has(TntEffect.HOUSE)) buildHouse(lvl, x, y, z);
        if (p.has(TntEffect.MANSION)) buildMansion(lvl, x, y, z);

        // REACCION EN CADENA: enciende TNTs del mod cercanas
        chainReaction(lvl, x, y, z, center);

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
        // firma de la TNT REAL del Rey: anillo purpura + rojo
        if (royal) {
            for (int i = 0; i < 40; i++) {
                double a = i / 40.0 * Math.PI * 2;
                lvl.sendParticles(new DustParticleOptions(
                                new org.joml.Vector3f(0.9F, 0.15F, 0.5F), 1.5F),
                        x + Math.cos(a) * 2.5, y + 0.4, z + Math.sin(a) * 2.5,
                        2, 0.1, 0.1, 0.1, 0.02);
                lvl.sendParticles(ParticleTypes.FLASH,
                        x + Math.cos(a) * 1.5, y + 0.8, z + Math.sin(a) * 1.5,
                        1, 0, 0, 0, 0);
            }
        }
        float radius = Math.min(4.5f, Math.max(1.2f, p.power() * 0.4f));
        // estallido de polvo del color propio de la TNT (esfera)
        // (cantidad segun la calidad de particulas de la config)
        for (int i = 0; i < com.tnts.config.TntsConfig.particles(60); i++) {
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
            case "ender_tnt" -> enderBurst(lvl, x, y, z);
            case "bubble_tnt" -> bubbleBurst(lvl, x, y, z);
            case "solar_tnt" -> solarBurst(lvl, x, y, z);
            default -> { /* mini, limpia, trampa, confeti, mina: solo el estallido de color */ }
        }
        // Efecto 3D generico para TODAS las TNTs: un cubo con la textura
        // real del bloque que crece y gira sobre el crater
        lvl.addFreshEntity(new TntBlastEntity(lvl, x, y, z, blockId, color));
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

    // ---------- EFECTOS NUEVOS (1.9.3) ----------

    /**
     * TNT Toxica: nube verde que envenena y debilita a los seres vivos.
     */
    private void toxicCloud(Level lvl, double x, double y, double z) {
        if (!(lvl instanceof ServerLevel sl)) return;
        // nube verde de particulas
        for (int i = 0; i < 60; i++) {
            double a = lvl.random.nextDouble() * Math.PI * 2;
            double r = 2 + lvl.random.nextDouble() * 5;
            sl.sendParticles(new DustParticleOptions(new org.joml.Vector3f(0.3f, 0.8f, 0.1f), 1.5F),
                    x + Math.cos(a) * r, y + 0.5 + lvl.random.nextDouble() * 3,
                    z + Math.sin(a) * r, 2, 0.2, 0.3, 0.2, 0.02);
            sl.sendParticles(ParticleTypes.WITCH,
                    x + (lvl.random.nextDouble() - 0.5) * 8,
                    y + 1 + lvl.random.nextDouble() * 3,
                    z + (lvl.random.nextDouble() - 0.5) * 8,
                    2, 0.2, 0.2, 0.2, 0.01);
        }
        // aplicar veneno y debilidad a entidades cercanas
        AABB box = new AABB(x - 6, y - 3, z - 6, x + 6, y + 4, z + 6);
        for (LivingEntity e : lvl.getEntitiesOfClass(LivingEntity.class, box)) {
            e.addEffect(new MobEffectInstance(MobEffects.POISON, 140, 1));
            e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
        }
    }

    /**
     * TNT de Fuegos Artificiales: lanza cohetes de colores al cielo.
     */
    private void fireworkShow(Level lvl, double x, double y, double z) {
        if (!(lvl instanceof ServerLevel sl)) return;
        int rockets = 8 + lvl.random.nextInt(8);
        for (int i = 0; i < rockets; i++) {
            double fx = x + (lvl.random.nextDouble() - 0.5) * 6;
            double fz = z + (lvl.random.nextDouble() - 0.5) * 6;
            double fy = y + lvl.random.nextDouble() * 2;
            // cohete de fuegos artificiales con colores aleatorios
            net.minecraft.world.item.ItemStack rocket = new net.minecraft.world.item.ItemStack(
                    net.minecraft.world.item.Items.FIREWORK_ROCKET);
            net.minecraft.nbt.CompoundTag fireworks = new net.minecraft.nbt.CompoundTag();
            net.minecraft.nbt.ListTag explosions = new net.minecraft.nbt.ListTag();
            net.minecraft.nbt.CompoundTag explosion = new net.minecraft.nbt.CompoundTag();
            explosion.putIntArray("Colors", new int[]{
                    lvl.random.nextInt(0x1000000) | 0xFF000000
            });
            explosion.putIntArray("FadeColors", new int[]{
                    lvl.random.nextInt(0x1000000) | 0xFF000000
            });
            explosion.putByte("Type", (byte) lvl.random.nextInt(4));
            explosions.add(explosion);
            fireworks.put("Explosions", explosions);
            rocket.addTagElement("Fireworks", fireworks);
            net.minecraft.world.entity.projectile.FireworkRocketEntity fw =
                    new net.minecraft.world.entity.projectile.FireworkRocketEntity(
                            lvl, fx, fy, fz, rocket);
            sl.addFreshEntity(fw);
        }
        sl.sendParticles(ParticleTypes.FIREWORK, x, y + 1, z, 20, 4, 2, 4, 0.1);
    }

    /**
     * TNT Gravitatoria: aplasta a todo contra el suelo con fuerza brutal.
     */
    private void gravityCrush(Level lvl, double x, double y, double z, BlockPos center) {
        if (!(lvl instanceof ServerLevel sl)) return;
        // aplastar entidades hacia abajo
        AABB box = new AABB(x - 10, y - 5, z - 10, x + 10, y + 8, z + 10);
        for (net.minecraft.world.entity.Entity e : lvl.getEntitiesOfClass(
                net.minecraft.world.entity.Entity.class, box,
                e -> !(e instanceof TntsPrimedTnt))) {
            e.setDeltaMovement(e.getDeltaMovement().add(0, -3.5, 0));
            e.hurtMarked = true;
        }
        // particulas purpuras de gravedad
        for (int i = 0; i < 40; i++) {
            double a = lvl.random.nextDouble() * Math.PI * 2;
            double r = 1 + lvl.random.nextDouble() * 6;
            sl.sendParticles(new DustParticleOptions(new org.joml.Vector3f(0.55f, 0.36f, 0.96f), 1.3F),
                    x + Math.cos(a) * r, y + 0.5 + lvl.random.nextDouble() * 4,
                    z + Math.sin(a) * r, 2, 0.1, 0.1, 0.1, 0.0);
            sl.sendParticles(ParticleTypes.ITEM_SLIME,
                    x + (lvl.random.nextDouble() - 0.5) * 7,
                    y + 4 + lvl.random.nextDouble() * 3,
                    z + (lvl.random.nextDouble() - 0.5) * 7,
                    1, 0.1, 0.3, 0.1, 0.0);
        }
        // crater profundo
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-5, -6, -5), center.offset(5, 0, 5))) {
            double dist = Math.sqrt(p.distSqr(center.offset(0, -2, 0)));
            if (dist < 5 && lvl.random.nextInt(3) != 0) {
                BlockState s = lvl.getBlockState(p);
                if (!s.isAir() && canDestroy(s, lvl, p)) {
                    lvl.destroyBlock(p, true);
                }
            }
        }
    }

    // ---------- EFECTOS NUEVOS (1.10.0) ----------

    /**
     * TNT del End: teletransporta a los seres vivos a sitios aleatorios
     * (a veces muy alto), invoca endermites y suelta particulas de portal.
     */
    private void enderBlast(Level lvl, double x, double y, double z) {
        if (!(lvl instanceof ServerLevel sl)) return;
        // teletransportar a los seres vivos cercanos
        AABB box = new AABB(x - 14, y - 6, z - 14, x + 14, y + 8, z + 14);
        for (net.minecraft.world.entity.LivingEntity e : sl.getEntitiesOfClass(
                net.minecraft.world.entity.LivingEntity.class, box)) {
            double nx = x + (sl.random.nextDouble() - 0.5) * 48;
            double nz = z + (sl.random.nextDouble() - 0.5) * 48;
            // a veces aterrizan muy arriba (chiste del End: "cuidado con la caida")
            double ny = sl.random.nextInt(6) == 0
                    ? e.getY() + 30 + sl.random.nextInt(20)
                    : e.getY();
            if (e.randomTeleport(nx, ny, nz, true)) {
                sl.sendParticles(ParticleTypes.PORTAL,
                        e.getX(), e.getY() + 1, e.getZ(),
                        com.tnts.config.TntsConfig.particles(14), 0.3, 0.5, 0.3, 0.1);
            }
        }
        // invocar endermites
        int mites = 2 + sl.random.nextInt(3);
        for (int i = 0; i < mites; i++) {
            net.minecraft.world.entity.monster.Endermite mite =
                    net.minecraft.world.entity.EntityType.ENDERMITE.create(sl);
            if (mite != null) {
                mite.moveTo(x + (sl.random.nextDouble() - 0.5) * 8,
                        y + 0.5, z + (sl.random.nextDouble() - 0.5) * 8);
                sl.addFreshEntity(mite);
            }
        }
        // particulas de portal en espiral
        for (int i = 0; i < com.tnts.config.TntsConfig.particles(50); i++) {
            double a = sl.random.nextDouble() * Math.PI * 2;
            double r = 1 + sl.random.nextDouble() * 7;
            sl.sendParticles(ParticleTypes.PORTAL,
                    x + Math.cos(a) * r, y + 0.5 + sl.random.nextDouble() * 5,
                    z + Math.sin(a) * r,
                    2, 0.1, 0.2, 0.1, 0.0);
        }
    }

    /**
     * TNT Burbuja: succiona a los seres e items hacia el crater (corriente
     * bajo el agua), convierte el hielo en agua y suelta burbujas.
     */
    private void bubblePull(Level lvl, double x, double y, double z) {
        if (!(lvl instanceof ServerLevel sl)) return;
        // succionar entidades e items hacia el centro (como un remolino de agua)
        AABB box = new AABB(x - 9, y - 6, z - 9, x + 9, y + 6, z + 9);
        for (net.minecraft.world.entity.Entity e : sl.getEntitiesOfClass(
                net.minecraft.world.entity.Entity.class, box,
                e -> !(e instanceof TntsPrimedTnt))) {
            Vec3 p = e.position();
            Vec3 to = new Vec3(x - p.x, y + 1 - p.y, z - p.z);
            double dist = to.length();
            if (dist < 0.01) continue;
            double strength = 1.9 * (1 - Math.min(1, dist / 9.0)) + 0.3;
            e.setDeltaMovement(e.getDeltaMovement().add(to.normalize().scale(strength)));
            e.hurtMarked = true;
        }
        // hielo -> agua
        for (BlockPos p : BlockPos.betweenClosed(
                new BlockPos((int) x, (int) y, (int) z).offset(-5, -3, -5),
                new BlockPos((int) x, (int) y, (int) z).offset(5, 3, 5))) {
            BlockState s = sl.getBlockState(p);
            if (s.is(Blocks.ICE) || s.is(Blocks.PACKED_ICE) || s.is(Blocks.FROSTED_ICE)) {
                sl.setBlock(p, Blocks.WATER.defaultBlockState(), 3);
            }
        }
        // burbujas por todos lados
        for (int i = 0; i < com.tnts.config.TntsConfig.particles(70); i++) {
            sl.sendParticles(ParticleTypes.BUBBLE,
                    x + (sl.random.nextDouble() - 0.5) * 14,
                    y + sl.random.nextDouble() * 6,
                    z + (sl.random.nextDouble() - 0.5) * 14,
                    2, 0.3, 0.3, 0.3, 0.1);
            sl.sendParticles(ParticleTypes.SPLASH,
                    x + (sl.random.nextDouble() - 0.5) * 10,
                    y + sl.random.nextDouble() * 4,
                    z + (sl.random.nextDouble() - 0.5) * 10,
                    1, 0.2, 0.3, 0.2, 0.0);
        }
    }

    /**
     * TNT Solar: despeja el tiempo y hace dia, incendia un area enorme,
     * quema a los seres vivos y suelta particulas de calor.
     */
    private void solarBlast(Level lvl, double x, double y, double z) {
        if (!(lvl instanceof ServerLevel sl)) return;
        // hacer dia y despejar el tiempo (solo en el Overworld)
        if (sl.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
            sl.setDayTime((sl.getDayTime() / 24000L) * 24000L + 6000L);
            sl.setWeatherParameters(6000, 0, false, false);
        }
        // incendiar una gran area
        BlockPos center = BlockPos.containing(x, y, z);
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-10, -2, -10), center.offset(10, 5, 10))) {
            if (sl.isEmptyBlock(p) && sl.getBlockState(p.below()).isSolid()
                    && sl.random.nextInt(3) != 0) {
                sl.setBlock(p, Blocks.FIRE.defaultBlockState(), 3);
            }
        }
        // quemar a los seres vivos
        AABB box = new AABB(x - 10, y - 4, z - 10, x + 10, y + 6, z + 10);
        for (net.minecraft.world.entity.LivingEntity e : sl.getEntitiesOfClass(
                net.minecraft.world.entity.LivingEntity.class, box)) {
            if (e.fireImmune()) continue;
            e.setRemainingFireTicks(Math.max(e.getRemainingFireTicks(), 140));
        }
        // destello cegador + particulas de calor
        sl.sendParticles(ParticleTypes.FLASH, x, y + 1, z, 2, 0, 0, 0, 0);
        for (int i = 0; i < com.tnts.config.TntsConfig.particles(80); i++) {
            double a = sl.random.nextDouble() * Math.PI * 2;
            double r = 1 + sl.random.nextDouble() * 12;
            sl.sendParticles(ParticleTypes.FLAME,
                    x + Math.cos(a) * r, y + 0.5 + sl.random.nextDouble() * 5,
                    z + Math.sin(a) * r,
                    2, 0.2, 0.2, 0.2, 0.02);
            sl.sendParticles(new DustParticleOptions(
                            new org.joml.Vector3f(1.0f, 0.75f, 0.15f), 1.2F),
                    x + (sl.random.nextDouble() - 0.5) * 16,
                    y + 1 + sl.random.nextDouble() * 4,
                    z + (sl.random.nextDouble() - 0.5) * 16,
                    1, 0, 0, 0, 0);
        }
    }

    // ---------- TNT Casa y TNT Mansión (1.10.12) ----------

    /** Coloca un bloque si la posicion esta dentro del mundo y es reemplazable. */
    private void buildSet(ServerLevel lvl, BlockPos p, net.minecraft.world.level.block.state.BlockState state) {
        if (lvl.isOutsideBuildHeight(p)) return;
        lvl.setBlock(p, state, 3);
    }

    /**
     * TNT Casa: construye una casita acogedora mejorada (9x9, interior 7x7)
     * con techo de abeto, chimenea de piedra con humo, porche, faroles,
     * macetas, jardin con flores, puerta, ventanas, cama, mesa de crafteo,
     * horno, cofre y antorchas. El suelo se nivela bajo el piso y el jardin.
     */
    private void buildHouse(Level lvl, double x, double y, double z) {
        if (!(lvl instanceof ServerLevel sl)) return;
        BlockPos base = BlockPos.containing(x, y, z);
        int bx = base.getX();
        int by = base.getY();
        int bz = base.getZ();

        net.minecraft.world.level.block.state.BlockState plank = Blocks.OAK_PLANKS.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState log = Blocks.OAK_LOG.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState roofPlank = Blocks.SPRUCE_PLANKS.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState stairDown = Blocks.SPRUCE_STAIRS.defaultBlockState()
                .setValue(net.minecraft.world.level.block.StairBlock.FACING,
                        net.minecraft.core.Direction.SOUTH)
                .setValue(net.minecraft.world.level.block.StairBlock.HALF,
                        net.minecraft.world.level.block.state.properties.Half.TOP);
        net.minecraft.world.level.block.state.BlockState stairDownN = stairDown
                .setValue(net.minecraft.world.level.block.StairBlock.FACING,
                        net.minecraft.core.Direction.NORTH);
        net.minecraft.world.level.block.state.BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState torch = Blocks.TORCH.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState lantern = Blocks.LANTERN.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState stone = Blocks.STONE_BRICKS.defaultBlockState();

        // nivelar el suelo: debajo de la casa + porche y jardin delante
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -6; dz <= 4; dz++) {
                for (int dy = by - 3; dy < by; dy++) {
                    BlockPos p = new BlockPos(bx + dx, dy, bz + dz);
                    if (!sl.getBlockState(p).isSolid()
                            || sl.getBlockState(p).getBlock() == Blocks.BEDROCK) {
                        buildSet(sl, p, Blocks.DIRT.defaultBlockState());
                    }
                }
            }
        }

        // piso
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                buildSet(sl, new BlockPos(bx + dx, by, bz + dz), plank);
            }
        }
        // porche delante de la puerta (a nivel del piso interior)
        buildSet(sl, new BlockPos(bx - 1, by, bz - 5), plank);
        buildSet(sl, new BlockPos(bx, by, bz - 5), plank);

        // paredes (perimetro de 9x9, de by+1 a by+3)
        for (int dy = 1; dy <= 3; dy++) {
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    boolean wall = Math.abs(dx) == 4 || Math.abs(dz) == 4;
                    if (!wall) continue;
                    boolean corner = (Math.abs(dx) == 4 && Math.abs(dz) == 4);
                    BlockPos p = new BlockPos(bx + dx, by + dy, bz + dz);
                    buildSet(sl, p, corner ? log : plank);
                }
            }
        }

        // puerta en el frente (z = bz-4, centro), ventanas a los lados
        buildSet(sl, new BlockPos(bx, by + 1, bz - 4),
                Blocks.OAK_DOOR.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.DoorBlock.FACING,
                                net.minecraft.core.Direction.SOUTH)
                        .setValue(net.minecraft.world.level.block.DoorBlock.HALF,
                                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER));
        buildSet(sl, new BlockPos(bx, by + 2, bz - 4),
                Blocks.OAK_DOOR.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.DoorBlock.FACING,
                                net.minecraft.core.Direction.SOUTH)
                        .setValue(net.minecraft.world.level.block.DoorBlock.HALF,
                                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
        // ventanas frontales
        for (int wx : new int[]{-2, 2}) {
            buildSet(sl, new BlockPos(bx + wx, by + 2, bz - 4), glass);
        }
        // ventanas laterales y trasera
        for (int wz : new int[]{-1, 1}) {
            buildSet(sl, new BlockPos(bx + 4, by + 2, bz + wz), glass);
            buildSet(sl, new BlockPos(bx - 4, by + 2, bz + wz), glass);
        }
        buildSet(sl, new BlockPos(bx, by + 2, bz + 4), glass);

        // techo de ABETO a dos aguas: fila de escalones invertidos + cumbrera
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (Math.abs(dx) == 4) {
                    buildSet(sl, new BlockPos(bx + dx, by + 4, bz + dz),
                            dx < 0 ? stairDownN : stairDown);
                } else if (Math.abs(dx) <= 3) {
                    buildSet(sl, new BlockPos(bx + dx, by + 4, bz + dz), roofPlank);
                }
            }
        }
        // cumbrera superior
        for (int dz = -3; dz <= 3; dz++) {
            buildSet(sl, new BlockPos(bx, by + 5, bz + dz), roofPlank);
        }

        // chimenea de piedra sobre el horno (atraviesa el techo)
        for (int dy = 4; dy <= 6; dy++) {
            buildSet(sl, new BlockPos(bx + 3, by + dy, bz + 3), stone);
        }

        // mobiliario interior
        buildSet(sl, new BlockPos(bx - 3, by + 1, bz - 3),
                Blocks.RED_BED.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.BedBlock.FACING,
                                net.minecraft.core.Direction.EAST)
                        .setValue(net.minecraft.world.level.block.BedBlock.PART,
                                net.minecraft.world.level.block.state.properties.BedPart.HEAD));
        buildSet(sl, new BlockPos(bx - 2, by + 1, bz - 3),
                Blocks.RED_BED.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.BedBlock.FACING,
                                net.minecraft.core.Direction.EAST)
                        .setValue(net.minecraft.world.level.block.BedBlock.PART,
                                net.minecraft.world.level.block.state.properties.BedPart.FOOT));
        buildSet(sl, new BlockPos(bx + 3, by + 1, bz - 3), Blocks.CRAFTING_TABLE.defaultBlockState());
        buildSet(sl, new BlockPos(bx + 3, by + 1, bz + 3), Blocks.FURNACE.defaultBlockState());
        buildSet(sl, new BlockPos(bx - 3, by + 1, bz + 3), Blocks.CHEST.defaultBlockState());
        buildSet(sl, new BlockPos(bx, by + 1, bz + 3), torch);
        buildSet(sl, new BlockPos(bx, by + 1, bz - 3), torch);

        // faroles colgando a los lados de la puerta + macetas
        buildSet(sl, new BlockPos(bx - 1, by + 2, bz - 4), lantern);
        buildSet(sl, new BlockPos(bx + 1, by + 2, bz - 4), lantern);
        buildSet(sl, new BlockPos(bx - 1, by + 1, bz - 4), Blocks.POTTED_POPPY.defaultBlockState());
        buildSet(sl, new BlockPos(bx + 1, by + 1, bz - 4), Blocks.POTTED_DANDELION.defaultBlockState());

        // jardin delante: flores sobre la tierra nivelada
        buildSet(sl, new BlockPos(bx - 3, by - 1, bz - 5), Blocks.POPPY.defaultBlockState());
        buildSet(sl, new BlockPos(bx - 2, by - 1, bz - 6), Blocks.DANDELION.defaultBlockState());
        buildSet(sl, new BlockPos(bx, by - 1, bz - 6), Blocks.POPPY.defaultBlockState());
        buildSet(sl, new BlockPos(bx + 2, by - 1, bz - 6), Blocks.AZURE_BLUET.defaultBlockState());
        buildSet(sl, new BlockPos(bx + 3, by - 1, bz - 5), Blocks.DANDELION.defaultBlockState());

        // humo de la chimenea + chispas de construccion
        sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                bx + 3.5, by + 6.5, bz + 3.5, 4, 0.15, 0.25, 0.15, 0.01);
        for (int i = 0; i < 30; i++) {
            sl.sendParticles(ParticleTypes.CLOUD,
                    bx + (sl.random.nextDouble() - 0.5) * 9,
                    by + 1 + sl.random.nextDouble() * 5,
                    bz + (sl.random.nextDouble() - 0.5) * 9,
                    2, 0.3, 0.2, 0.3, 0.0);
        }
        sl.sendParticles(ParticleTypes.END_ROD,
                bx, by + 2, bz, 24, 4, 3, 4, 0.1);
        sl.playSound(null, new BlockPos(bx, by, bz),
                net.minecraft.sounds.SoundEvents.WOOD_PLACE,
                SoundSource.BLOCKS, 1.2F, 0.8F);
    }

    /**
     * TNT Mansión: construye una mansion de CUARZO estilo YouTube (15x15)
     * de dos plantas con suelo intermedio y escalera de mano, 4 torres con
     * remate de oro, tejado escalonado de cuarzo con corona dorada, doble
     * puerta, ventanales, fuente con agua delante, alfombra roja, biblioteca,
     * cama de lujo, cofre y lamparas. Muy cara de craftear.
     */
    private void buildMansion(Level lvl, double x, double y, double z) {
        if (!(lvl instanceof ServerLevel sl)) return;
        BlockPos base = BlockPos.containing(x, y, z);
        int bx = base.getX();
        int by = base.getY();
        int bz = base.getZ();

        // ---- materiales: cuarzo blanco + detalles de ORO (look YouTube) ----
        net.minecraft.world.level.block.state.BlockState quartz = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState quartzBricks = Blocks.QUARTZ_BRICKS.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState pillar = Blocks.QUARTZ_PILLAR.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState gold = Blocks.GOLD_BLOCK.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState lantern = Blocks.LANTERN.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState carpet = Blocks.RED_CARPET.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState water = Blocks.WATER.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState doorLower = Blocks.DARK_OAK_DOOR.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.FACING,
                        net.minecraft.core.Direction.SOUTH)
                .setValue(net.minecraft.world.level.block.DoorBlock.HALF,
                        net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER)
                .setValue(net.minecraft.world.level.block.DoorBlock.OPEN, true);
        net.minecraft.world.level.block.state.BlockState doorUpper = doorLower
                .setValue(net.minecraft.world.level.block.DoorBlock.HALF,
                        net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER);

        // nivelar el suelo: 15x15 de la mansion + zona de la fuente delante
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -10; dz <= 7; dz++) {
                for (int dy = by - 4; dy < by; dy++) {
                    BlockPos p = new BlockPos(bx + dx, dy, bz + dz);
                    if (!sl.getBlockState(p).isSolid()
                            || sl.getBlockState(p).getBlock() == Blocks.BEDROCK) {
                        buildSet(sl, p, Blocks.DIRT.defaultBlockState());
                    }
                }
            }
        }

        // piso: borde de ORO, interior de cuarzo liso
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                boolean edge = Math.abs(dx) == 7 || Math.abs(dz) == 7;
                buildSet(sl, new BlockPos(bx + dx, by, bz + dz), edge ? gold : quartz);
            }
        }

        // paredes de 2 plantas (baja by+1..3, alta by+5..6) con suelo intermedio
        // en by+4; esquinas y pilares de fachada en QUARTZ_PILLAR
        for (int dy = 1; dy <= 6; dy++) {
            for (int dx = -7; dx <= 7; dx++) {
                for (int dz = -7; dz <= 7; dz++) {
                    boolean wall = Math.abs(dx) == 7 || Math.abs(dz) == 7;
                    if (!wall) continue;
                    boolean corner = Math.abs(dx) == 7 && Math.abs(dz) == 7;
                    boolean facadePillar = dz == -7 && Math.abs(dx) == 4;
                    boolean sidePillar = Math.abs(dx) == 7 && Math.abs(dz) == 3;
                    boolean backPillar = dz == 7 && Math.abs(dx) == 3;
                    BlockPos p = new BlockPos(bx + dx, by + dy, bz + dz);
                    if (corner || facadePillar || sidePillar || backPillar) {
                        buildSet(sl, p, pillar);
                    } else {
                        buildSet(sl, p, quartz);
                    }
                }
            }
        }
        // suelo intermedio (by+4) de la 2a planta, con hueco de escalera atras
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                if (dx == 0 && dz == 6) continue; // hueco de la escalera de mano
                buildSet(sl, new BlockPos(bx + dx, by + 4, bz + dz), quartz);
            }
        }
        // escalera de mano en el hueco (sobre la pared trasera)
        for (int dy = 1; dy <= 3; dy++) {
            buildSet(sl, new BlockPos(bx, by + dy, bz + 6),
                    Blocks.LADDER.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.LadderBlock.FACING,
                                    net.minecraft.core.Direction.SOUTH));
        }

        // puertas dobles abiertas en el frente (z = bz-7)
        for (int dx : new int[]{-1, 0}) {
            buildSet(sl, new BlockPos(bx + dx, by + 1, bz - 7), doorLower);
            buildSet(sl, new BlockPos(bx + dx, by + 2, bz - 7), doorUpper);
        }
        // columnas de ORO flanqueando la entrada
        for (int dy = 1; dy <= 2; dy++) {
            buildSet(sl, new BlockPos(bx - 3, by + dy, bz - 7), gold);
            buildSet(sl, new BlockPos(bx + 3, by + dy, bz - 7), gold);
        }
        // ventanales grandes en la fachada (2x2 en cada planta)
        for (int wx : new int[]{-6, -5, 5, 6}) {
            for (int wy : new int[]{2, 3, 5, 6}) {
                buildSet(sl, new BlockPos(bx + wx, by + wy, bz - 7), glass);
            }
        }
        // ventanas laterales (2x2 en cada planta)
        for (int wy : new int[]{2, 3, 5, 6}) {
            for (int wz : new int[]{-5, -4, 4, 5}) {
                buildSet(sl, new BlockPos(bx + 7, by + wy, bz + wz), glass);
                buildSet(sl, new BlockPos(bx - 7, by + wy, bz + wz), glass);
            }
        }
        // ventana trasera central (2x2 en cada planta)
        for (int wy : new int[]{2, 3, 5, 6}) {
            for (int wx : new int[]{-1, 0}) {
                buildSet(sl, new BlockPos(bx + wx, by + wy, bz + 7), glass);
            }
        }

        // techo plano de cuarzo (by+7) con esquinas de ORO
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                boolean corner = Math.abs(dx) == 7 && Math.abs(dz) == 7;
                buildSet(sl, new BlockPos(bx + dx, by + 7, bz + dz), corner ? gold : quartz);
            }
        }
        // corona escalonada de cuarzo (piramide) con centro de ORO
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                if (Math.abs(dx) == 5 || Math.abs(dz) == 5) {
                    buildSet(sl, new BlockPos(bx + dx, by + 8, bz + dz), quartzBricks);
                }
            }
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) == 3 || Math.abs(dz) == 3) {
                    buildSet(sl, new BlockPos(bx + dx, by + 9, bz + dz), quartz);
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                buildSet(sl, new BlockPos(bx + dx, by + 10, bz + dz), quartz);
            }
        }
        buildSet(sl, new BlockPos(bx, by + 11, bz), gold);

        // torres de cuarzo en las 4 esquinas con remate de ORO
        for (int tx : new int[]{-7, 7}) {
            for (int tz : new int[]{-7, 7}) {
                for (int dy = 7; dy <= 9; dy++) {
                    buildSet(sl, new BlockPos(bx + tx, by + dy, bz + tz), pillar);
                }
                buildSet(sl, new BlockPos(bx + tx, by + 10, bz + tz), gold);
            }
        }

        // FUENTE delante de la entrada: primero el anillo de ORO (contencion)
        // y DESPUES el agua — si el agua se coloca antes que su anillo,
        // fluye al instante y el estanque se queda vacio
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -8; dz <= -10; dz++) {
                boolean ring = Math.abs(dx) == 2 || dz == -8 || dz == -10;
                if (ring) {
                    buildSet(sl, new BlockPos(bx + dx, by, bz + dz), gold);
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            buildSet(sl, new BlockPos(bx + dx, by, bz - 9), water);
        }
        // chorro central de la fuente
        buildSet(sl, new BlockPos(bx, by + 1, bz - 9), pillar);
        buildSet(sl, new BlockPos(bx, by + 2, bz - 9), lantern);

        // interior planta baja: alfombra roja de la entrada al centro
        for (int dx = -1; dx <= 0; dx++) {
            for (int dz = -6; dz <= -1; dz++) {
                buildSet(sl, new BlockPos(bx + dx, by + 1, bz + dz), carpet);
            }
        }
        // lampara colgante central (del suelo intermedio)
        buildSet(sl, new BlockPos(bx, by + 3, bz), lantern);
        // librerias contra las paredes laterales
        for (int dy = 1; dy <= 2; dy++) {
            buildSet(sl, new BlockPos(bx - 6, by + dy, bz - 5), Blocks.BOOKSHELF.defaultBlockState());
            buildSet(sl, new BlockPos(bx - 6, by + dy, bz + 5), Blocks.BOOKSHELF.defaultBlockState());
            buildSet(sl, new BlockPos(bx + 6, by + dy, bz - 5), Blocks.BOOKSHELF.defaultBlockState());
            buildSet(sl, new BlockPos(bx + 6, by + dy, bz + 5), Blocks.BOOKSHELF.defaultBlockState());
        }
        buildSet(sl, new BlockPos(bx - 5, by + 1, bz - 4), Blocks.CHEST.defaultBlockState());
        buildSet(sl, new BlockPos(bx + 5, by + 1, bz - 4), Blocks.CRAFTING_TABLE.defaultBlockState());
        buildSet(sl, new BlockPos(bx + 5, by + 1, bz + 4), Blocks.FURNACE.defaultBlockState());

        // planta alta: cama de lujo + librerias + lampara
        buildSet(sl, new BlockPos(bx - 1, by + 5, bz - 5),
                Blocks.RED_BED.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.BedBlock.FACING,
                                net.minecraft.core.Direction.SOUTH)
                        .setValue(net.minecraft.world.level.block.BedBlock.PART,
                                net.minecraft.world.level.block.state.properties.BedPart.HEAD));
        buildSet(sl, new BlockPos(bx, by + 5, bz - 5),
                Blocks.RED_BED.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.BedBlock.FACING,
                                net.minecraft.core.Direction.SOUTH)
                        .setValue(net.minecraft.world.level.block.BedBlock.PART,
                                net.minecraft.world.level.block.state.properties.BedPart.FOOT));
        for (int dy = 5; dy <= 6; dy++) {
            buildSet(sl, new BlockPos(bx - 6, by + dy, bz + 4), Blocks.BOOKSHELF.defaultBlockState());
            buildSet(sl, new BlockPos(bx + 6, by + dy, bz + 4), Blocks.BOOKSHELF.defaultBlockState());
        }
        buildSet(sl, new BlockPos(bx, by + 6, bz), lantern);

        // destello de construccion + musica de riqueza
        for (int i = 0; i < 60; i++) {
            sl.sendParticles(ParticleTypes.CLOUD,
                    bx + (sl.random.nextDouble() - 0.5) * 15,
                    by + 1 + sl.random.nextDouble() * 8,
                    bz + (sl.random.nextDouble() - 0.5) * 15,
                    2, 0.4, 0.3, 0.4, 0.0);
        }
        for (int i = 0; i < 24; i++) {
            sl.sendParticles(new DustParticleOptions(
                            new org.joml.Vector3f(0.95F, 0.9F, 0.7F), 1.2F),
                    bx + (sl.random.nextDouble() - 0.5) * 16,
                    by + 1 + sl.random.nextDouble() * 9,
                    bz + (sl.random.nextDouble() - 0.5) * 16,
                    1, 0, 0, 0, 0);
        }
        sl.sendParticles(ParticleTypes.END_ROD,
                bx, by + 2, bz, 40, 7, 5, 7, 0.1);
        sl.playSound(null, new BlockPos(bx, by, bz),
                net.minecraft.sounds.SoundEvents.STONE_PLACE,
                SoundSource.BLOCKS, 1.4F, 0.7F);
    }

    private void enderBurst(ServerLevel lvl, double x, double y, double z) {
        lvl.sendParticles(ParticleTypes.FLASH, x, y, z, 1, 0, 0, 0, 0);
        for (int i = 0; i < com.tnts.config.TntsConfig.particles(45); i++) {
            double a = lvl.random.nextDouble() * Math.PI * 2;
            double r = 0.5 + lvl.random.nextDouble() * 6;
            lvl.sendParticles(ParticleTypes.PORTAL,
                    x + Math.cos(a) * r, y + 0.5 + lvl.random.nextDouble() * 4, z + Math.sin(a) * r,
                    2, 0.1, 0.2, 0.1, 0.0);
            lvl.sendParticles(ParticleTypes.END_ROD,
                    x + (lvl.random.nextDouble() - 0.5) * 7,
                    y + 0.5 + lvl.random.nextDouble() * 4,
                    z + (lvl.random.nextDouble() - 0.5) * 7,
                    1, 0.15, 0.25, 0.15, 0.03);
        }
    }

    private void bubbleBurst(ServerLevel lvl, double x, double y, double z) {
        for (int i = 0; i < com.tnts.config.TntsConfig.particles(55); i++) {
            lvl.sendParticles(ParticleTypes.BUBBLE,
                    x + (lvl.random.nextDouble() - 0.5) * 9,
                    y + 0.5 + lvl.random.nextDouble() * 4,
                    z + (lvl.random.nextDouble() - 0.5) * 9,
                    2, 0.3, 0.3, 0.3, 0.05);
            lvl.sendParticles(ParticleTypes.SPLASH,
                    x + (lvl.random.nextDouble() - 0.5) * 8,
                    y + 0.5 + lvl.random.nextDouble() * 3,
                    z + (lvl.random.nextDouble() - 0.5) * 8,
                    1, 0.2, 0.3, 0.2, 0.0);
        }
    }

    private void solarBurst(ServerLevel lvl, double x, double y, double z) {
        lvl.sendParticles(ParticleTypes.FLASH, x, y, z, 1, 0, 0, 0, 0);
        for (int i = 0; i < com.tnts.config.TntsConfig.particles(50); i++) {
            lvl.sendParticles(new DustParticleOptions(new org.joml.Vector3f(1.0f, 0.8f, 0.2f), 1.3F),
                    x + (lvl.random.nextDouble() - 0.5) * 12,
                    y + 0.5 + lvl.random.nextDouble() * 4,
                    z + (lvl.random.nextDouble() - 0.5) * 12,
                    2, 0.1, 0.1, 0.1, 0.0);
            lvl.sendParticles(ParticleTypes.FLAME,
                    x + (lvl.random.nextDouble() - 0.5) * 10,
                    y + 0.5 + lvl.random.nextDouble() * 3,
                    z + (lvl.random.nextDouble() - 0.5) * 10,
                    1, 0.1, 0.1, 0.1, 0.0);
        }
    }

    // ---------- REACCION EN CADENA ----------

    /**
     * Reaccion en cadena: busca bloques TNT del mod cercanos no encendidos
     * y los activa, creando un efecto domino espectacular.
     */
    private void chainReaction(Level lvl, double x, double y, double z, BlockPos center) {
        if (!(lvl instanceof ServerLevel sl)) return;
        int radius = 5;
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-radius, -1, -radius),
                center.offset(radius, 1, radius))) {
            BlockState state = sl.getBlockState(p);
            // si el bloque sigue siendo una TNT del mod, no esta encendida
            // y no esta desactivada en config -> se enciende (efecto domino)
            if (state.getBlock() instanceof com.tnts.block.TntBlock tnt
                    && !state.getValue(com.tnts.block.TntBlock.LIT)
                    && com.tnts.config.TntsConfig.isEnabled(
                            ForgeRegistries.BLOCKS.getKey(tnt).getPath())) {
                tnt.prime(sl, p, state, null);
            }
        }
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
        // Ola 1: nucleo del crater (radio 5) al instante — el crater entero
        // de golpe eran ~2.600 destroyBlock en un tick (freeze del servidor),
        // asi que el anillo exterior (radio 5-9) se reparte unos ticks despues
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-9, -7, -9), center.offset(9, 3, 9))) {
            double dist = Math.sqrt(p.distSqr(center.offset(0, 2, 0)));
            if (dist < 5 && lvl.random.nextInt(3) != 0) {
                BlockState state = lvl.getBlockState(p);
                if (!state.isAir() && canDestroy(state, lvl, p)) {
                    lvl.destroyBlock(p, true);
                }
            }
        }
        // Ola 2: anillo exterior del crater (radio 5-9) pocos ticks despues
        serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                serverLevel.getServer().getTickCount() + 8, () -> {
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-9, -7, -9), center.offset(9, 3, 9))) {
                double dist = Math.sqrt(p.distSqr(center.offset(0, 2, 0)));
                if (dist >= 5 && dist < 9 && lvl.random.nextInt(3) != 0) {
                    BlockState state = lvl.getBlockState(p);
                    if (!state.isAir() && canDestroy(state, lvl, p)) {
                        lvl.destroyBlock(p, true);
                    }
                }
            }
        }));
        // Ola 3: grietas con lava (12 lineas hasta radio 16, mas lava)
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI / 6;
            for (int d = 4; d <= 16; d++) {
                int bx = (int)(x + Math.cos(angle) * d);
                int bz = (int)(z + Math.sin(angle) * d);
                BlockPos fissure = new BlockPos(bx, center.getY() - 1, bz);
                if (lvl.random.nextInt(2) == 0) {
                    lvl.setBlock(fissure, Blocks.LAVA.defaultBlockState(), 3);
                } else if (lvl.random.nextInt(2) == 0) {
                    lvl.destroyBlock(fissure.above(), true);
                }
                // fuego en la superficie de la grieta
                BlockPos surface = fissure.above();
                if (lvl.isEmptyBlock(surface) && lvl.random.nextInt(5) == 0) {
                    lvl.setBlock(surface, Blocks.FIRE.defaultBlockState(), 3);
                }
            }
        }
        // Lanzar entidades MUY alto (radio 16)
        AABB box = new AABB(x - 16, y - 4, z - 16, x + 16, y + 4, z + 16);
        for (LivingEntity e : lvl.getEntitiesOfClass(LivingEntity.class, box)) {
            e.push(0, 4.5 + lvl.random.nextDouble() * 3, 0);
            e.hurtMarked = true;
        }
        // Particulas de terremoto (mas densas)
        for (int i = 0; i < 140; i++) {
            double a = lvl.random.nextDouble() * Math.PI * 2;
            double r = lvl.random.nextDouble() * 14;
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.DIRT.defaultBlockState()),
                    x + Math.cos(a) * r, y + 0.5 + lvl.random.nextDouble() * 2,
                    z + Math.sin(a) * r, 3, 0.3, 0.3, 0.3, 0);
            serverLevel.sendParticles(new DustParticleOptions(
                            new org.joml.Vector3f(0.6f, 0.5f, 0.3f), 1.0F),
                    x + Math.cos(a) * r, y + 0.5, z + Math.sin(a) * r,
                    1, 0, 0, 0, 0);
        }
        // Temblor fuerte (radio 18)
        for (ServerPlayer sp : serverLevel.players()) {
            if (sp.distanceToSqr(x, y, z) <= 18 * 18) {
                sp.animateHurt(sp.getYRot());
            }
        }
        // onda de choque 3D expansiva (marron terremoto, radio 16)
        serverLevel.addFreshEntity(new ShockwaveEntity(serverLevel, x, y + 0.2, z,
                40, 16.0, new org.joml.Vector3f(0.6f, 0.45f, 0.25f)));
    }    /**
     * TNT Meteorito: invoca 5-8 meteoritos 3D que caen del cielo con estela
     * de fuego y explotan al impactar (entidad visible, como la bola negra).
     */
    private void meteorShower(Level lvl, double x, double y, double z) {
        if (!(lvl instanceof ServerLevel serverLevel)) return;
        // MAS meteoritos (8-12) en un area mas amplia
        int numMeteors = 8 + lvl.random.nextInt(5);
        for (int i = 0; i < numMeteors; i++) {
            double mx = x + (lvl.random.nextDouble() - 0.5) * 44;
            double mz = z + (lvl.random.nextDouble() - 0.5) * 44;
            int my = serverLevel.getHeight(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                    (int) mx, (int) mz);
            // el meteorito 3D cae desde 40 bloques arriba y explota al impactar
            serverLevel.addFreshEntity(new MeteorEntity(serverLevel, mx + 0.5, my + 40, mz + 0.5));
        }
        lvl.playSound(null, x, y, z, ModSounds.explode("meteorito_tnt"), SoundSource.BLOCKS, 3.0F, 0.6F);
    }

    /**
     * TNT Tormenta: invoca una nube 3D que flota sobre la zona 6 segundos,
     * invocando rayos, empujando con viento y soltando lluvia.
     */
    private void massiveStorm(Level lvl, double x, double y, double z) {
        if (!(lvl instanceof ServerLevel serverLevel)) return;
        serverLevel.addFreshEntity(new StormCloudEntity(serverLevel, x, y, z));
        // 3 rayos inmediatos alrededor (la nube sigue invocando durante 8s)
        for (int i = 0; i < 3; i++) {
            double bx = x + (lvl.random.nextDouble() - 0.5) * 24;
            double bz = z + (lvl.random.nextDouble() - 0.5) * 24;
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (bolt != null) {
                bolt.moveTo(bx, y + 14, bz);
                serverLevel.addFreshEntity(bolt);
            }
            serverLevel.sendParticles(ParticleTypes.FLASH, bx, y + 8, bz, 1, 0, 0, 0, 0);
        }
        lvl.playSound(null, x, y, z, ModSounds.explode("rayo_tnt"), SoundSource.BLOCKS, 4.0F, 0.5F);
    }

    /**
     * TNT Colosal: explosion en 3 oleadas progresivas (cada 8 ticks)
     * que destruye bloques sin lag.
     */
    private void colossalExplosion(Level lvl, double x, double y, double z, BlockPos center) {
        if (!(lvl instanceof ServerLevel serverLevel)) return;
        // Ola 1: crater (radio 6)
        serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                serverLevel.getServer().getTickCount() + 10, () -> {
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-6, -5, -6), center.offset(6, 3, 6))) {
                double dist = Math.sqrt(p.distSqr(center.offset(0, 1, 0)));
                if (dist < 6 && lvl.random.nextInt(2) == 0) {
                    BlockState state = lvl.getBlockState(p);
                    if (!state.isAir() && canDestroy(state, lvl, p)) {
                        lvl.destroyBlock(p, true);
                    }
                }
            }
            for (ServerPlayer sp : serverLevel.players()) {
                if (sp.distanceToSqr(x, y, z) <= 10 * 10) sp.animateHurt(sp.getYRot());
            }
            lvl.playSound(null, x, y, z, ModSounds.explode("colosal_tnt"), SoundSource.BLOCKS, 2.5F, 0.8F);
        }));
        // Ola 2: crater mediano (radio 10)
        serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                serverLevel.getServer().getTickCount() + 20, () -> {
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-10, -6, -10), center.offset(10, 4, 10))) {
                double dist = Math.sqrt(p.distSqr(center.offset(0, 1, 0)));
                if (dist < 10 && dist >= 4 && lvl.random.nextInt(2) == 0) {
                    BlockState state = lvl.getBlockState(p);
                    if (!state.isAir() && canDestroy(state, lvl, p)) {
                        lvl.destroyBlock(p, true);
                    }
                }
            }
            // Lanzar entidades MAS alto
            AABB box = new AABB(x - 14, y - 3, z - 14, x + 14, y + 5, z + 14);
            for (LivingEntity e : lvl.getEntitiesOfClass(LivingEntity.class, box)) {
                e.push(0, 3.0 + lvl.random.nextDouble(), 0);
                e.hurtMarked = true;
            }
            for (ServerPlayer sp : serverLevel.players()) {
                if (sp.distanceToSqr(x, y, z) <= 14 * 14) sp.animateHurt(sp.getYRot());
            }
            lvl.playSound(null, x, y, z, ModSounds.explode("colosal_tnt"), SoundSource.BLOCKS, 3.0F, 0.7F);
        }));
        // Ola 3: crater grande (radio 16)
        serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                serverLevel.getServer().getTickCount() + 30, () -> {
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-16, -8, -16), center.offset(16, 5, 16))) {
                double dist = Math.sqrt(p.distSqr(center.offset(0, 1, 0)));
                if (dist < 16 && dist >= 8 && lvl.random.nextInt(3) == 0) {
                    BlockState state = lvl.getBlockState(p);
                    if (!state.isAir() && canDestroy(state, lvl, p)) {
                        lvl.destroyBlock(p, true);
                    }
                }
            }
            // Fuego en los bordes (anillo mas amplio)
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-16, -1, -16), center.offset(16, 3, 16))) {
                double dist = Math.sqrt(p.distSqr(center.offset(0, 0, 0)));
                if (dist > 10 && dist < 16 && lvl.isEmptyBlock(p) && lvl.random.nextInt(3) == 0) {
                    lvl.setBlock(p, Blocks.FIRE.defaultBlockState(), 3);
                }
            }
            for (ServerPlayer sp : serverLevel.players()) {
                if (sp.distanceToSqr(x, y, z) <= 20 * 20) sp.animateHurt(sp.getYRot());
            }
            lvl.playSound(null, x, y, z, ModSounds.explode("colosal_tnt"), SoundSource.BLOCKS, 4.0F, 0.5F);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 3, 2, 1, 2, 0);
        }));
        // Particulas inmediatas + ondas de choque 3D (rojo/amarillo)
        for (int i = 0; i < 60; i++) {
            double a = lvl.random.nextDouble() * Math.PI * 2;
            double r = lvl.random.nextDouble() * 8;
            serverLevel.sendParticles(new DustParticleOptions(
                            new org.joml.Vector3f(1.0f, 0.6f, 0.1f), 1.2F),
                    x + Math.cos(a) * r, y + 0.5, z + Math.sin(a) * r,
                    2, 0.2, 0.2, 0.2, 0);
        }
        // 3 ondas expansivas MAS grandes (una por oleada, colores distintos)
        serverLevel.addFreshEntity(new ShockwaveEntity(serverLevel, x, y + 0.2, z,
                25, 8.0, new org.joml.Vector3f(1.0f, 0.8f, 0.3f)));
        serverLevel.addFreshEntity(new ShockwaveEntity(serverLevel, x, y + 0.2, z,
                35, 13.0, new org.joml.Vector3f(1.0f, 0.5f, 0.1f)));
        serverLevel.addFreshEntity(new ShockwaveEntity(serverLevel, x, y + 0.2, z,
                45, 18.0, new org.joml.Vector3f(0.8f, 0.3f, 0.1f)));
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
                x, y + 1, z, 120, 10, 5, 10, 0.2);
        // Ondas expansivas de luz (4 anillos mas amplios)
        for (int ring = 0; ring < 4; ring++) {
            for (int i = 0; i < 48; i++) {
                double a = i / 48.0 * Math.PI * 2;
                double r = 2 + ring * 4;
                serverLevel.sendParticles(new DustParticleOptions(
                                new org.joml.Vector3f(1.0f, 0.95f, 0.6f), 1.3F),
                        x + Math.cos(a) * r, y + 0.5 + ring * 0.5, z + Math.sin(a) * r,
                        2, 0.1, 0.1, 0.1, 0);
            }
        }
        // Fuego en el borde de la zona
        for (BlockPos p : BlockPos.betweenClosed(
                new BlockPos((int) x - 12, (int) y, (int) z - 12),
                new BlockPos((int) x + 12, (int) y + 1, (int) z + 12))) {
            double dist = Math.sqrt(p.distSqr(new BlockPos((int) x, (int) y, (int) z)));
            if (dist > 8 && dist < 12 && lvl.isEmptyBlock(p) && lvl.random.nextInt(3) == 0) {
                lvl.setBlock(p, Blocks.FIRE.defaultBlockState(), 3);
            }
        }
        // Massive XP drop (400-600)
        int totalXp = 400 + lvl.random.nextInt(200);
        while (totalXp > 0) {
            int value = Math.min(totalXp, 50);
            totalXp -= value;
            lvl.addFreshEntity(new ExperienceOrb(lvl, x, y + 1, z, value));
        }
        // Entidades cercanas reciben daño + regeneracion (radio 14)
        AABB box = new AABB(x - 14, y - 4, z - 14, x + 14, y + 8, z + 14);
        for (LivingEntity e : lvl.getEntitiesOfClass(LivingEntity.class, box)) {
            e.hurt(e.damageSources().magic(), 6.0f);
            e.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2));
            e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 400, 0));
        }
        // esfera de supernova 3D (flash -> expansion -> colapso)
        serverLevel.addFreshEntity(new SupernovaEntity(serverLevel, x, y, z));
        // Sonido de supernova
        lvl.playSound(null, x, y, z, ModSounds.explode("supernova_tnt"), SoundSource.BLOCKS, 4.0F, 0.4F);
        lvl.playSound(null, x, y, z, ModSounds.explode("negra_tnt"), SoundSource.BLOCKS, 3.0F, 0.5F);
    }

    /** Es una TNT masiva? (las que merecen el espectaculo de quality=2). */
    private static boolean isMassive(TntProperties p) {
        return p.has(TntEffect.EARTHQUAKE) || p.has(TntEffect.METEOR) || p.has(TntEffect.STORM)
                || p.has(TntEffect.COLOSSAL) || p.has(TntEffect.SUPERNOVA)
                || p.has(TntEffect.NUCLEAR) || p.has(TntEffect.BLACKHOLE);
    }

    /**
     * Espectaculo extra (particleQuality=2) en las masivas: un abanico de
     * anillos de colores que se expande desde el centro + destello de camara
     * para los jugadores cercanos. Todo con bucles acotados (3 anillos x 24
     * puntos x 3 capas = ~216 particulas) para no cargar el PC.
     */
    public static void massiveQualityShow(ServerLevel sl, double x, double y, double z) {
        // 3 capas de anillos de colores que crecen (rojo/amarillo, cian, purpura)
        org.joml.Vector3f[] palette = {
                new org.joml.Vector3f(1.0f, 0.35f, 0.2f),  // rojo-naranja
                new org.joml.Vector3f(1.0f, 0.85f, 0.2f),  // amarillo
                new org.joml.Vector3f(0.35f, 0.9f, 1.0f),  // cian
                new org.joml.Vector3f(0.7f, 0.35f, 1.0f),  // purpura
        };
        for (int layer = 0; layer < 3; layer++) {
            for (int i = 0; i < 24; i++) {
                double a = i / 24.0 * Math.PI * 2;
                double r = 3 + layer * 4; // anillos en radio 3, 7 y 11
                sl.sendParticles(new DustParticleOptions(palette[(layer + i) % 4], 1.4F),
                        x + Math.cos(a) * r, y + 0.4 + layer * 0.6, z + Math.sin(a) * r,
                        1, 0.05, 0.05, 0.05, 0.0);
                // chispa vertical arriba para dar volumen
                sl.sendParticles(new DustParticleOptions(palette[(layer + i + 1) % 4], 1.1F),
                        x + Math.cos(a) * r * 0.6, y + 1.2 + layer, z + Math.sin(a) * r * 0.6,
                        1, 0.04, 0.04, 0.04, 0.0);
            }
        }
        // destello de camara: flash en el centro + temblor en los jugadores cerca
        sl.sendParticles(ParticleTypes.FLASH, x, y + 1, z, 3, 0, 0, 0, 0);
        AABB camBox = new AABB(x - 32, y - 16, z - 32, x + 32, y + 16, z + 32);
        for (ServerPlayer sp : sl.getEntitiesOfClass(ServerPlayer.class, camBox)) {
            sp.animateHurt(sp.getYRot());
        }
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
