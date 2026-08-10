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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Bola negra 3D MEGA-MEJORADA del Agujero Negro: entidad visible que flota
 * sobre el crater durante 8 segundos (160 ticks) con:
 * - Vortex multicapa (3 anillos giratorios con colores diferentes)
 * - Rayos purpura que saltan alrededor
 * - Destruccion lenta de bloques (come el borde del crater)
 * - Sccion creciente que escala con el tiempo
 * - Temblor de pantalla fuerte para quien este dentro
 * - Sonido de remolino grave que sube de intensidad
 */
public class BlackHoleEntity extends Entity {

    private static final int LIFETIME = 160; // 8 segundos
    private static final double RADIUS = 18.0;
    private static final double SUCTION_BASE = 1.0;
    private static final double SUCTION_MAX = 3.0;

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

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (++age > LIFETIME) {
            // Explosion final al desaparecer (mas grande)
            if (!this.level().isClientSide) {
                this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                        9.0f, true, Level.ExplosionInteraction.BLOCK);
            }
            this.discard();
            return;
        }
        ServerLevel serverLevel = (ServerLevel) this.level();
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        // Progreso (0.0 a 1.0)
        double progress = age / (double) LIFETIME;

        // Fuerza de succion creciente
        double suctionStrength = SUCTION_BASE + (SUCTION_MAX - SUCTION_BASE) * progress;

        // Radio efectivo (crece un poco con el tiempo)
        double effectiveRadius = RADIUS * (0.7 + 0.3 * progress);

        // === SUCCION DE ENTIDADES ===
        AABB box = new AABB(x - effectiveRadius, y - 5, z - effectiveRadius,
                x + effectiveRadius, y + 5, z + effectiveRadius);
        for (Entity e : serverLevel.getEntitiesOfClass(Entity.class, box,
                e -> !(e instanceof BlackHoleEntity))) {
            Vec3 p = e.position();
            Vec3 dir = new Vec3(x - p.x, y - p.y, z - p.z);
            double dist = dir.length();
            if (dist > effectiveRadius) continue;

            double hd = Math.sqrt((p.x - x) * (p.x - x) + (p.z - z) * (p.z - z));

            // Item u orb de XP que cae al nucleo: consumido con destello (come MAS)
            if ((e instanceof ItemEntity || e instanceof ExperienceOrb) && hd < 2.5 && p.y < y + 3.0) {
                e.discard();
                serverLevel.sendParticles(ParticleTypes.FLASH, p.x, p.y + 0.3, p.z, 3, 0, 0, 0, 0);
                serverLevel.sendParticles(ParticleTypes.END_ROD, p.x, p.y + 0.3, p.z, 4, 0.2, 0.2, 0.2, 0);
                serverLevel.playSound(null, p.x, p.y, p.z, SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.AMBIENT, 0.7F, 2.0F);
                continue;
            }

            // Ser vivo pegado al nucleo: devorado (dano continuo cada 5 ticks)
            if (e instanceof LivingEntity le && hd < 1.5 && age % 5 == 0 && le.isAlive()) {
                le.hurt(le.damageSources().wither(), 4.0f);
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.STONE.defaultBlockState()),
                        p.x, p.y + 0.5, p.z, 6, 0.2, 0.2, 0.2, 0);
            }

            // Sccion con fuerza creciente
            double strength = suctionStrength * (1 - dist / effectiveRadius) + 0.3;
            Vec3 force = dir.normalize().scale(strength);
            e.setDeltaMovement(e.getDeltaMovement().add(force));
            e.hurtMarked = true;
        }

        // === DESTRUCCION DE BLOQUES (come MAS: cada 6 ticks, hasta 22 bloques) ===
        if (age % 6 == 0 && progress > 0.1) {
            int blocksToBreak = (int)(8 + progress * 14);
            for (int i = 0; i < blocksToBreak; i++) {
                double angle = serverLevel.random.nextDouble() * Math.PI * 2;
                double dist = 1.2 + serverLevel.random.nextDouble() * (effectiveRadius - 1.2);
                int bx = (int)(x + Math.cos(angle) * dist);
                int bz = (int)(z + Math.sin(angle) * dist);
                int by = (int)(y + (serverLevel.random.nextDouble() - 0.5) * 6);
                BlockPos bpos = new BlockPos(bx, by, bz);
                BlockState state = serverLevel.getBlockState(bpos);
                if (state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA)) continue;
                float hardness = state.getDestroySpeed(serverLevel, bpos);
                if (hardness >= 0.0f && hardness < 60.0f) {  // come incluso obsidiana
                    serverLevel.destroyBlock(bpos, true);
                    // Particula de bloque siendo devorado
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.STONE.defaultBlockState()),
                            bx + 0.5, by + 0.5, bz + 0.5, 3, 0.2, 0.2, 0.2, 0);
                    // De vez en cuando el abismo deja lava en el hueco
                    if (serverLevel.random.nextInt(6) == 0) {
                        serverLevel.setBlock(bpos, Blocks.LAVA.defaultBlockState(), 3);
                    }
                }
            }
        }

        // === VORTEX MULTICAPA (3 anillos giratorios) ===
        double baseAngle = (serverLevel.getGameTime() % 720) * 0.3;
        // Anillo 1: purpura brillante (rapido, pequeno)
        for (int i = 0; i < 16; i++) {
            double a = baseAngle + i * Math.PI * 8 / 16;
            double r = 0.6 + progress * 2.5 + serverLevel.random.nextDouble() * 0.8;
            serverLevel.sendParticles(new DustParticleOptions(
                            new Vector3f(0.8f, 0.3f, 1.0f), 1.0F),
                    x + Math.cos(a) * r, y + 0.2 + serverLevel.random.nextDouble() * 0.5,
                    z + Math.sin(a) * r, 1, 0, 0, 0, 0);
        }
        // Anillo 2: azul-violeta (medio)
        for (int i = 0; i < 12; i++) {
            double a = -baseAngle * 0.7 + i * Math.PI * 6 / 12;
            double r = 1.5 + progress * 4 + serverLevel.random.nextDouble() * 1.5;
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    x + Math.cos(a) * r, y + 0.3 + serverLevel.random.nextDouble(),
                    z + Math.sin(a) * r, 1, 0, 0, 0, 0);
        }
        // Anillo 3: negro-purpura oscuro (largo, lento)
        for (int i = 0; i < 20; i++) {
            double a = baseAngle * 0.4 + i * Math.PI * 10 / 20;
            double r = 2 + progress * 6 + serverLevel.random.nextDouble() * 2;
            serverLevel.sendParticles(new DustParticleOptions(
                            new Vector3f(0.15f, 0.05f, 0.3f), 1.4F),
                    x + Math.cos(a) * r, y + 0.4 + serverLevel.random.nextDouble() * 1.5,
                    z + Math.sin(a) * r, 1, 0, 0, 0, 0);
        }

        // === PULSO CADA 1.5 SEGUNDOS: anillo expansivo ===
        for (int start = 30; start <= age; start += 30) {
            int ringAge = age - start;
            if (ringAge > 20) continue;
            double rr = Math.min(RADIUS, 1.5 + ringAge * 1.0);
            for (int i = 0; i < 32; i++) {
                double a = i / 32.0 * Math.PI * 2;
                // Anillo doble (purpura + blanco)
                serverLevel.sendParticles(new DustParticleOptions(
                                new Vector3f(0.85f, 0.4f, 1.0f), 1.2F),
                        x + Math.cos(a) * rr, y + 0.3, z + Math.sin(a) * rr,
                        1, 0, 0, 0, 0);
                if (i % 2 == 0) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            x + Math.cos(a) * rr, y + 0.5, z + Math.sin(a) * rr,
                            1, 0, 0, 0, 0);
                }
            }
        }

        // === RAYOS PURPURA (cada 40 ticks, 3-5 rayos aleatorios) ===
        if (age % 40 == 0 && age > 20) {
            int numArcs = 3 + serverLevel.random.nextInt(3);
            for (int i = 0; i < numArcs; i++) {
                double angle = serverLevel.random.nextDouble() * Math.PI * 2;
                double dist = 3 + serverLevel.random.nextDouble() * (effectiveRadius - 4);
                double tx = x + Math.cos(angle) * dist;
                double tz = z + Math.sin(angle) * dist;
                // Rayo puro con particulas
                for (int j = 0; j < 8; j++) {
                    double px = x + (tx - x) * j / 8.0 + (serverLevel.random.nextDouble() - 0.5) * 0.5;
                    double py = y + 0.5 + serverLevel.random.nextDouble() * 3;
                    double pz = z + (tz - z) * j / 8.0 + (serverLevel.random.nextDouble() - 0.5) * 0.5;
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            px, py, pz, 2, 0, 0, 0, 0);
                }
                // Destello en el punto de impacto
                serverLevel.sendParticles(new DustParticleOptions(
                                new Vector3f(0.7f, 0.2f, 1.0f), 1.5F),
                        tx, y + 1, tz, 5, 0.3, 0.3, 0.3, 0);
            }
        }

        // === HUMO TRAGADO AL CENTRO ===
        for (int i = 0; i < 4; i++) {
            double a = serverLevel.random.nextDouble() * Math.PI * 2;
            double r = 3 + serverLevel.random.nextDouble() * (effectiveRadius - 3);
            double startX = x + Math.cos(a) * r;
            double startZ = z + Math.sin(a) * r;
            double startY = y + 0.5 + serverLevel.random.nextDouble() * 3;
            // Movimiento hacia el centro
            double dx = (x - startX) * 0.15;
            double dz = (z - startZ) * 0.15;
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    startX, startY, startZ, 1, dx, -0.05, dz, 0);
        }

        // === NUCLEO OSCURO PULSANTE (mas grande y dramtico) ===
        float coreScale = 2.5f + (float)(Math.sin(age * 0.15) * 0.8);
        serverLevel.sendParticles(new DustParticleOptions(
                        new Vector3f(0.08f, 0.02f, 0.18f), coreScale),
                x, y + 0.1, z, 3, 0.3, 0.1, 0.3, 0);
        // Centro absoluto (negro puro)
        serverLevel.sendParticles(new DustParticleOptions(
                        new Vector3f(0.0f, 0.0f, 0.0f), 3.0F),
                x, y + 0.15, z, 1, 0, 0, 0, 0);

        // === TEMBLOR DE PANTALLA (cada pulso, mas fuerte con el tiempo) ===
        if (age % 30 == 0) {
            for (ServerPlayer sp : serverLevel.players()) {
                if (sp.distanceToSqr(x, y, z) <= effectiveRadius * effectiveRadius) {
                    sp.animateHurt(sp.getYRot());
                    // Knockback sutil
                    Vec3 dir = sp.position().subtract(x, y, z);
                    if (dir.length() > 0.5) {
                        sp.push(dir.normalize().x * 0.15, 0.05, dir.normalize().z * 0.15);
                        sp.hurtMarked = true;
                    }
                }
            }
        }

        // === SONIDO DE REMOLINO (se solapa cada 8 ticks, sube de tono) ===
        if (age % 8 == 0) {
            int pulses = Math.min(age / 30, 5);
            float pitch = 0.7F + 0.15F * pulses;
            float volume = (float)(1.0F + 0.3F * progress);
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
