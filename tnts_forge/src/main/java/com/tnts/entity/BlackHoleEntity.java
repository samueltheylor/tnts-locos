package com.tnts.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Agujero Negro con NUCLEO REAL (disco negro + anillos de acrecion 3D en el
 * cliente) y succion por fases en el servidor:
 *   0-2s   atraccion leve
 *   2-5s   empieza a jalar fuerte
 *   5-7s   modo CORRE (succion maxima)
 *   7-8s   colapso: comprime todo, destello y BOOM final con crater
 * Las particulas del disco de acrecion orbitan y caen al centro con gradiente
 * morado -> rosa -> blanco; los seres vivos pegados al nucleo son devorados.
 */
public class BlackHoleEntity extends Entity {

    public static final int LIFETIME = 160; // 8 segundos
    private static final double RADIUS = 18.0;

    private int age = 0;

    public BlackHoleEntity(EntityType<? extends BlackHoleEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public BlackHoleEntity(Level level, double x, double y, double z) {
        this(TntsEntities.BLACK_HOLE.get(), level);
        this.setPos(x, y + 0.3, z);
    }

    /** Fuerza de succion por fases: leve -> fuerte -> CORRE -> colapso. */
    private double phaseSuction(int age) {
        if (age < 40) return 0.35 + age / 40.0 * 0.5;                 // 0-2s: leve
        if (age < 100) return 0.8 + (age - 40) / 60.0 * 1.2;          // 2-5s: fuerte
        if (age < 140) return 2.4 + (age - 100) / 40.0 * 1.1;         // 5-7s: CORRE
        return 3.5 + (age - 140) / 20.0 * 1.5;                        // 7-8s: colapso
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) this.level();
        if (++age > LIFETIME) {
            collapse(serverLevel);
            this.discard();
            return;
        }
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        double progress = age / (double) LIFETIME;
        double suction = phaseSuction(age);
        double effectiveRadius = RADIUS * (0.6 + 0.4 * progress);
        boolean collapsing = age >= 140;

        // === SUCCION DE ENTIDADES (no lineal: mas cerca = mucho mas fuerte) ===
        AABB box = new AABB(x - effectiveRadius, y - 5, z - effectiveRadius,
                x + effectiveRadius, y + 5, z + effectiveRadius);
        for (Entity e : serverLevel.getEntitiesOfClass(Entity.class, box,
                e -> !(e instanceof BlackHoleEntity))) {
            Vec3 p = e.position();
            Vec3 dir = new Vec3(x - p.x, y - p.y, z - p.z);
            double dist = dir.length();
            if (dist > effectiveRadius) continue;

            double hd = Math.sqrt((p.x - x) * (p.x - x) + (p.z - z) * (p.z - z));

            // === ITEMS/ORBES DE XP CAPTURADOS: espiral acelerada + estela blanca ===
            if ((e instanceof ItemEntity || e instanceof ExperienceOrb) && hd < 6.0 && p.y < y + 4.0) {
                // devorado al llegar al nucleo
                if (hd < 0.9) {
                    e.discard();
                    serverLevel.sendParticles(ParticleTypes.FLASH, p.x, p.y + 0.3, p.z, 4, 0, 0, 0, 0);
                    serverLevel.sendParticles(ParticleTypes.END_ROD, p.x, p.y + 0.3, p.z, 6, 0.2, 0.2, 0.2, 0);
                    serverLevel.playSound(null, p.x, p.y, p.z, SoundEvents.EXPERIENCE_ORB_PICKUP,
                            SoundSource.AMBIENT, 0.7F, 2.0F);
                } else {
                    // 0 lejos, 1 en el nucleo: la espiral ACELERA al acercarse
                    double capture = 1.0 - Math.min(1.0, hd / 6.0);
                    double orbit = 0.14 + capture * 0.5;   // giro cada vez mas rapido
                    double inward = 0.08 + capture * 0.45;  // caida cada vez mas fuerte
                    // tangencial (orbita) + radial (hacia el centro) en el plano del disco
                    double tx = -(p.z - z) / hd;
                    double tz = (p.x - x) / hd;
                    double rx = dir.x / dist;
                    double rz = dir.z / dist;
                    double vy = (y - p.y) * 0.12;
                    e.setDeltaMovement(tx * orbit + rx * inward, vy, tz * orbit + rz * inward);
                    e.hurtMarked = true;
                    // estela blanca: mas densa cuanto mas cerca (parece estirado)
                    int trail = 1 + (int) (capture * 3);
                    Vec3 mv = e.getDeltaMovement();
                    for (int ti = 0; ti < trail; ti++) {
                        serverLevel.sendParticles(ParticleTypes.END_ROD,
                                p.x + (serverLevel.random.nextDouble() - 0.5) * 0.3,
                                p.y + 0.1 + (serverLevel.random.nextDouble() - 0.5) * 0.2,
                                p.z + (serverLevel.random.nextDouble() - 0.5) * 0.3,
                                1, -mv.x * 0.25, -mv.y * 0.25, -mv.z * 0.25, 0);
                    }
                }
                continue;
            }

            // Ser vivo pegado al nucleo: los mobs son DEVORADOS (desaparecen);
            // el jugador recibe dano continuo
            if (e instanceof LivingEntity le && hd < 1.6 && le.isAlive()) {
                if (le instanceof Player) {
                    if (age % 5 == 0) {
                        le.hurt(le.damageSources().wither(), 4.0f + (float) (suction * 0.6));
                    }
                } else if (serverLevel.random.nextInt(4) == 0) {
                    le.discard();
                    serverLevel.sendParticles(ParticleTypes.FLASH, p.x, p.y + 0.3, p.z, 5, 0.2, 0.2, 0.2, 0);
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.STONE.defaultBlockState()),
                            p.x, p.y + 0.5, p.z, 8, 0.3, 0.3, 0.3, 0);
                    serverLevel.playSound(null, p.x, p.y, p.z, SoundEvents.ENDERMAN_TELEPORT,
                            SoundSource.AMBIENT, 0.8F, 0.4F);
                    continue;
                }
            }

            // Succion no lineal: cerca del nucleo la fuerza explota
            double closeness = Math.pow(1.0 - Math.min(1.0, dist / effectiveRadius), 0.6);
            double strength = suction * closeness + 0.25;
            e.setDeltaMovement(e.getDeltaMovement().add(dir.normalize().scale(strength)));
            e.hurtMarked = true;
        }

        // === DESTRUCCION DE BLOQUES (come hasta el colapso) ===
        if (age % 6 == 0 && progress > 0.1 && !collapsing) {
            int blocksToBreak = (int) (8 + progress * 14);
            for (int i = 0; i < blocksToBreak; i++) {
                double angle = serverLevel.random.nextDouble() * Math.PI * 2;
                double dist = 1.2 + serverLevel.random.nextDouble() * (effectiveRadius - 1.2);
                int bx = (int) (x + Math.cos(angle) * dist);
                int bz = (int) (z + Math.sin(angle) * dist);
                int by = (int) (y + (serverLevel.random.nextDouble() - 0.5) * 6);
                BlockPos bpos = new BlockPos(bx, by, bz);
                BlockState state = serverLevel.getBlockState(bpos);
                if (state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA)) continue;
                float hardness = state.getDestroySpeed(serverLevel, bpos);
                if (hardness >= 0.0f && hardness < 60.0f) {  // come incluso obsidiana
                    serverLevel.destroyBlock(bpos, true);
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.STONE.defaultBlockState()),
                            bx + 0.5, by + 0.5, bz + 0.5, 3, 0.2, 0.2, 0.2, 0);
                    if (serverLevel.random.nextInt(6) == 0) {
                        serverLevel.setBlock(bpos, Blocks.LAVA.defaultBlockState(), 3);
                    }
                }
            }
        }

        // === DISCO DE ACRECION: particulas que ORBITAN y CAEN al centro ===
        int accretionCount = 6 + (int) (progress * 12);
        double baseAngle = serverLevel.getGameTime() * 0.5;
        for (int i = 0; i < accretionCount; i++) {
            double r = 0.8 + serverLevel.random.nextDouble() * (effectiveRadius * 0.85);
            double a = baseAngle + i * (Math.PI * 2 / accretionCount);
            double px = x + Math.cos(a) * r;
            double pz = z + Math.sin(a) * r;
            double py = y + (serverLevel.random.nextDouble() - 0.5) * 1.2;
            // velocidad: tangencial (orbita) + radial hacia el centro (caida)
            double orbit = 0.22 + progress * 0.18;
            double fall = 0.10 * (1.0 + progress * 2.0);
            double vx = -Math.sin(a) * orbit - Math.cos(a) * fall;
            double vz = Math.cos(a) * orbit - Math.sin(a) * fall;
            double vy = (serverLevel.random.nextDouble() - 0.5) * 0.06;
            // gradiente morado -> rosa -> blanco segun la distancia al nucleo
            Vector3f color;
            if (r > effectiveRadius * 0.55) color = new Vector3f(0.65f, 0.20f, 1.0f);
            else if (r > effectiveRadius * 0.28) color = new Vector3f(1.0f, 0.40f, 0.95f);
            else color = new Vector3f(1.0f, 0.92f, 1.0f);
            float size = 0.8F + (float) (progress * 0.9);
            serverLevel.sendParticles(new DustParticleOptions(color, size),
                    px, py, pz, 1, vx, vy, vz, 0);
        }
        // halo morado en el plano del disco (mantiene el look de portal)
        for (int i = 0; i < 20; i++) {
            double a = baseAngle * 0.6 + i * Math.PI * 2 / 20;
            double rr = effectiveRadius * 0.92;
            serverLevel.sendParticles(new DustParticleOptions(
                            new Vector3f(0.7f, 0.3f, 1.0f), 1.3F),
                    x + Math.cos(a) * rr, y + (serverLevel.random.nextDouble() - 0.5) * 0.6,
                    z + Math.sin(a) * rr, 1,
                    -Math.sin(a) * 0.15, 0, Math.cos(a) * 0.15, 0);
        }

        // === RAYOS PURPURA (cada 40 ticks, 3-5 rayos aleatorios) ===
        if (age % 40 == 0 && age > 20) {
            int numArcs = 3 + serverLevel.random.nextInt(3);
            for (int i = 0; i < numArcs; i++) {
                double angle = serverLevel.random.nextDouble() * Math.PI * 2;
                double dist = 3 + serverLevel.random.nextDouble() * (effectiveRadius - 4);
                double tx = x + Math.cos(angle) * dist;
                double tz = z + Math.sin(angle) * dist;
                for (int j = 0; j < 8; j++) {
                    double px = x + (tx - x) * j / 8.0 + (serverLevel.random.nextDouble() - 0.5) * 0.5;
                    double py = y + 0.5 + serverLevel.random.nextDouble() * 3;
                    double pz = z + (tz - z) * j / 8.0 + (serverLevel.random.nextDouble() - 0.5) * 0.5;
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            px, py, pz, 2, 0, 0, 0, 0);
                }
                serverLevel.sendParticles(new DustParticleOptions(
                                new Vector3f(0.7f, 0.2f, 1.0f), 1.5F),
                        tx, y + 1, tz, 5, 0.3, 0.3, 0.3, 0);
            }
        }

        // === HUMO Y NIEBLA OSCURA alrededor (efecto de gravedad) ===
        for (int i = 0; i < 5; i++) {
            double a = serverLevel.random.nextDouble() * Math.PI * 2;
            double r = effectiveRadius * (0.5 + serverLevel.random.nextDouble() * 0.7);
            double sx = x + Math.cos(a) * r;
            double sz = z + Math.sin(a) * r;
            // humo que cae al centro
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    sx, y + 0.5 + serverLevel.random.nextDouble() * 3, sz, 1,
                    (x - sx) * 0.15, -0.05, (z - sz) * 0.15, 0);
            // niebla oscura estatica
            serverLevel.sendParticles(new DustParticleOptions(
                            new Vector3f(0.08f, 0.03f, 0.18f), 2.4F),
                    sx, y + (serverLevel.random.nextDouble() - 0.5) * 2.5, sz, 1, 0, -0.02, 0, 0);
        }

        // === TEMBLOR DE CAMARA (mas frecuente en CORRE y colapso) ===
        int shakeEvery = collapsing ? 8 : (progress > 0.625 ? 12 : 20);
        if (age % shakeEvery == 0) {
            double kb = 0.15 + progress * 0.2;
            for (ServerPlayer sp : serverLevel.players()) {
                if (sp.distanceToSqr(x, y, z) <= (effectiveRadius + 2) * (effectiveRadius + 2)) {
                    sp.animateHurt(sp.getYRot());
                    Vec3 dir = sp.position().subtract(x, y, z);
                    if (dir.length() > 0.5) {
                        sp.push(dir.normalize().x * kb, 0.05, dir.normalize().z * kb);
                        sp.hurtMarked = true;
                    }
                }
            }
        }

        // === SONIDO DE REMOLINO (se solapa cada 8 ticks, sube de tono) ===
        if (age % 8 == 0) {
            int pulses = Math.min(age / 30, 5);
            float pitch = 0.7F + 0.15F * pulses;
            float volume = (float) (1.0F + 0.3F * progress);
            serverLevel.playSound(null, x, y, z,
                    com.tnts.ModSounds.BLACK_HOLE_LOOP.get(),
                    SoundSource.AMBIENT, volume, pitch);
        }

        // === SONIDO DE SUCCION (cada 20 ticks) ===
        if (age % 20 == 0) {
            serverLevel.playSound(null, x, y, z,
                    SoundEvents.ENDER_DRAGON_FLAP,
                    SoundSource.AMBIENT, 0.4F, 0.3F);
        }
    }

    /** Colapso final: el anillo se comprime, destello, BOOM y crater decente. */
    private void collapse(ServerLevel serverLevel) {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        BlockPos center = this.blockPosition();

        // destello blanco triple + onda de energia
        serverLevel.sendParticles(ParticleTypes.FLASH, x, y + 1, z, 8, 0, 0, 0, 0);
        serverLevel.sendParticles(ParticleTypes.END_ROD, x, y + 1, z, 160, 8, 6, 8, 0.3);
        serverLevel.sendParticles(new DustParticleOptions(
                        new Vector3f(1.0f, 0.6f, 1.0f), 2.5F),
                x, y + 1, z, 40, 6, 4, 6, 0);

        // BOOM grande
        serverLevel.explode(this, x, y + 1, z, 10.0f, true, Level.ExplosionInteraction.BLOCK);

        // crater decente garantizado (radio 11) + lava + fuego.
        // El crater entero de golpe eran ~1.000 destroyBlock en un tick
        // (freeze del servidor): el nucleo (radio 6) al instante y el anillo
        // exterior (radio 6-11) unos ticks despues.
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-11, -6, -11), center.offset(11, 3, 11))) {
            double dist = Math.sqrt(p.distSqr(center.offset(0, 0, 0)));
            if (dist < 6 && serverLevel.random.nextInt(2) == 0) {
                BlockState state = serverLevel.getBlockState(p);
                if (!state.isAir()) {
                    float hardness = state.getDestroySpeed(serverLevel, p);
                    if (hardness >= 0.0f && hardness < 60.0f) {
                        serverLevel.destroyBlock(p, true);
                    }
                }
            }
        }
        serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                serverLevel.getServer().getTickCount() + 8, () -> {
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-11, -6, -11), center.offset(11, 3, 11))) {
                double dist = Math.sqrt(p.distSqr(center.offset(0, 0, 0)));
                if (dist >= 6 && dist < 11 && serverLevel.random.nextInt(2) == 0) {
                    BlockState state = serverLevel.getBlockState(p);
                    if (!state.isAir()) {
                        float hardness = state.getDestroySpeed(serverLevel, p);
                        if (hardness >= 0.0f && hardness < 60.0f) {
                            serverLevel.destroyBlock(p, true);
                        }
                    }
                }
            }
        }));
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-8, -2, -8), center.offset(8, -1, 8))) {
            double dist = Math.sqrt(p.distSqr(center.offset(0, 0, 0)));
            if (dist < 8 && serverLevel.isEmptyBlock(p)
                    && serverLevel.getBlockState(p.below()).isSolid()
                    && serverLevel.random.nextInt(3) == 0) {
                serverLevel.setBlock(p, Blocks.LAVA.defaultBlockState(), 3);
            }
        }
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-12, 0, -12), center.offset(12, 1, 12))) {
            double dist = Math.sqrt(p.distSqr(center.offset(0, 0, 0)));
            if (dist > 9 && dist < 12 && serverLevel.isEmptyBlock(p)
                    && serverLevel.random.nextInt(3) == 0) {
                serverLevel.setBlock(p, Blocks.FIRE.defaultBlockState(), 3);
            }
        }
        serverLevel.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, 0.4F);
        for (ServerPlayer sp : serverLevel.players()) {
            if (sp.distanceToSqr(x, y, z) <= 16 * 16) {
                sp.animateHurt(sp.getYRot());
            }
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
