package com.tnts.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/**
 * Supernova 3D: una esfera de luz dorada que aparece en el crater, pulsa y
 * se expande en 3 fases (flash -> expansion -> colapso) durante 2.5 segundos
 * (50 ticks), con ondas de particulas y destellos de estrella.
 */
public class SupernovaEntity extends Entity {

    private static final int LIFETIME = 50;
    private static final int PHASE_FLASH = 8;
    private static final int PHASE_EXPAND = 30;

    private int age = 0;

    public SupernovaEntity(EntityType<? extends SupernovaEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public SupernovaEntity(Level level, double x, double y, double z) {
        this(TntsEntities.SUPERNOVA.get(), level);
        this.setPos(x, y + 1.5, z);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (++age > LIFETIME) {
            this.discard();
            return;
        }
        ServerLevel serverLevel = (ServerLevel) this.level();
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        double progress = age / (double) LIFETIME;

        // Fase 1: flash inicial (destello blanco)
        if (age <= PHASE_FLASH) {
            serverLevel.sendParticles(ParticleTypes.FLASH, x, y, z, 2, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 10, 2, 2, 2, 0.1);
        }

        // Fase 2: expansion (anillos dorados creciendo)
        if (age > PHASE_FLASH && age <= PHASE_EXPAND) {
            double r = (age - PHASE_FLASH) * 0.7;
            int count = (int) (16 + progress * 16);
            for (int i = 0; i < count; i++) {
                double a = i / (double) count * Math.PI * 2;
                serverLevel.sendParticles(new DustParticleOptions(
                                new Vector3f(1.0f, 0.9f, 0.4f), 1.2F),
                        x + Math.cos(a) * r, y, z + Math.sin(a) * r, 1, 0, 0, 0, 0);
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        x + Math.cos(a) * r, y + (serverLevel.random.nextDouble() - 0.5), z + Math.sin(a) * r,
                        1, 0, 0, 0, 0);
            }
            // corona de luz en 3D (esferas de particulas)
            for (int i = 0; i < 8; i++) {
                double phi = serverLevel.random.nextDouble() * Math.PI;
                double theta = serverLevel.random.nextDouble() * Math.PI * 2;
                double rr = r * 0.6;
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        x + Math.sin(phi) * Math.cos(theta) * rr,
                        y + Math.cos(phi) * rr,
                        z + Math.sin(phi) * Math.sin(theta) * rr,
                        1, 0, 0, 0, 0);
            }
        }

        // Fase 3: colapso (las particulas vuelven al centro)
        if (age > PHASE_EXPAND) {
            double r = (LIFETIME - age) * 0.4;
            int count = 12;
            for (int i = 0; i < count; i++) {
                double a = i / (double) count * Math.PI * 2;
                serverLevel.sendParticles(new DustParticleOptions(
                                new Vector3f(1.0f, 0.85f, 0.3f), 1.0F),
                        x + Math.cos(a) * r, y, z + Math.sin(a) * r, 1, 0, 0, 0, 0);
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        x + Math.cos(a) * r, y + 0.5, z + Math.sin(a) * r, 1, 0, 0, 0, 0);
            }
        }

        // destello final
        if (age == LIFETIME - 2) {
            serverLevel.sendParticles(ParticleTypes.FLASH, x, y, z, 1, 0, 0, 0, 0);
            serverLevel.playSound(null, x, y, z, SoundEvents.END_PORTAL_SPAWN,
                    SoundSource.BLOCKS, 2.0F, 0.6F);
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

    @Override
    public boolean isPickable() {
        return false;
    }
}
