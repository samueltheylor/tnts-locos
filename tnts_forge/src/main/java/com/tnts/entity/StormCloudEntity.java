package com.tnts.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Nube de tormenta 3D de la TNT Tormenta: una nube oscura visible que flota
 * sobre la zona durante 6 segundos (120 ticks) invocando rayos a su alrededor,
 * empujando entidades con viento y soltando lluvia de particulas.
 */
public class StormCloudEntity extends Entity {

    private static final int LIFETIME = 120; // 6 segundos
    private static final double RADIUS = 16.0;

    private int age = 0;

    public StormCloudEntity(EntityType<? extends StormCloudEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public StormCloudEntity(Level level, double x, double y, double z) {
        this(TntsEntities.STORM_CLOUD.get(), level);
        this.setPos(x, y + 14, z);
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

        // rayos: cada 20 ticks, 2-3 rayos
        if (age % 20 == 0) {
            int numBolts = 2 + serverLevel.random.nextInt(2);
            for (int i = 0; i < numBolts; i++) {
                double bx = x + (serverLevel.random.nextDouble() - 0.5) * RADIUS;
                double bz = z + (serverLevel.random.nextDouble() - 0.5) * RADIUS;
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (bolt != null) {
                    bolt.moveTo(bx, y - 2, bz);
                    serverLevel.addFreshEntity(bolt);
                }
                serverLevel.sendParticles(ParticleTypes.FLASH, bx, y - 4, bz, 1, 0, 0, 0, 0);
            }
            serverLevel.playSound(null, x, y, z, SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.WEATHER, 3.0F, 0.8F);
        }

        // viento: empuja entidades lejos (continuo, mas suave que el estallido)
        AABB box = new AABB(x - RADIUS, y - 10, z - RADIUS, x + RADIUS, y + 6, z + RADIUS);
        for (Entity e : serverLevel.getEntitiesOfClass(Entity.class, box,
                e -> !(e instanceof StormCloudEntity) && !(e instanceof TntsPrimedTnt))) {
            Vec3 p = e.position();
            Vec3 dir = new Vec3(p.x - x, 0.2, p.z - z);
            double dist = dir.horizontalDistance();
            if (dist < 0.001) continue;
            double strength = 0.12 * (1 - Math.min(1, dist / RADIUS)) + 0.02;
            e.setDeltaMovement(e.getDeltaMovement().add(dir.normalize().scale(strength)));
            e.hurtMarked = true;
        }

        // lluvia: particulas cayendo desde la nube
        for (int i = 0; i < 8; i++) {
            double rx = x + (serverLevel.random.nextDouble() - 0.5) * RADIUS;
            double rz = z + (serverLevel.random.nextDouble() - 0.5) * RADIUS;
            serverLevel.sendParticles(ParticleTypes.CLOUD, rx, y - 2, rz, 1, 0, -1.5, 0, 0);
            serverLevel.sendParticles(ParticleTypes.RAIN, rx, y - 2, rz, 1, 0, -2.0, 0, 0);
        }
        // chispas electricas dentro de la nube
        for (int i = 0; i < 4; i++) {
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    x + (serverLevel.random.nextDouble() - 0.5) * 3,
                    y + (serverLevel.random.nextDouble() - 0.5) * 2,
                    z + (serverLevel.random.nextDouble() - 0.5) * 3,
                    1, 0, 0, 0, 0);
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
