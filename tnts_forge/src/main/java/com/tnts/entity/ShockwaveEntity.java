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
 * Onda de choque 3D de Terremoto y Colosal: un anillo de polvo de color que
 * se expande desde el crater por el suelo (radio creciente) durante unos
 * ticks, dando la sensacion de la onda expansiva recorriendo el terreno.
 */
public class ShockwaveEntity extends Entity {

    private static final int DEFAULT_LIFETIME = 30;
    private static final double DEFAULT_MAX_RADIUS = 12.0;

    private int age = 0;
    private int lifetime = DEFAULT_LIFETIME;
    private double maxRadius = DEFAULT_MAX_RADIUS;
    private Vector3f color = new Vector3f(1.0f, 0.85f, 0.5f);

    public ShockwaveEntity(EntityType<? extends ShockwaveEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public ShockwaveEntity(Level level, double x, double y, double z,
                           int lifetime, double maxRadius, Vector3f color) {
        this(TntsEntities.SHOCKWAVE.get(), level);
        this.setPos(x, y, z);
        this.lifetime = lifetime;
        this.maxRadius = maxRadius;
        this.color = color;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (++age > lifetime) {
            this.discard();
            return;
        }
        ServerLevel serverLevel = (ServerLevel) this.level();
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        // radio actual: crece de forma suave (ease-out)
        double progress = age / (double) lifetime;
        double r = maxRadius * (1 - (1 - progress) * (1 - progress));

        // anillo de particulas (2 capas: polvo de color + polvo de bloque)
        int count = (int) (20 + progress * 20);
        for (int i = 0; i < count; i++) {
            double a = i / (double) count * Math.PI * 2;
            double jitter = (serverLevel.random.nextDouble() - 0.5) * 0.6;
            serverLevel.sendParticles(new DustParticleOptions(color, 1.1F),
                    x + Math.cos(a) * (r + jitter), y + 0.3, z + Math.sin(a) * (r + jitter),
                    1, 0, 0, 0, 0);
            if (i % 2 == 0) {
                serverLevel.sendParticles(ParticleTypes.POOF,
                        x + Math.cos(a) * (r + jitter), y + 0.2, z + Math.sin(a) * (r + jitter),
                        1, 0, 0.1, 0, 0);
            }
        }
        // retumbo grave mientras se expande
        if (age % 8 == 0) {
            serverLevel.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE,
                    SoundSource.BLOCKS, 1.2F, 0.5F + (float) progress * 0.4F);
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
