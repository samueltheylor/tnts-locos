package com.tnts.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Meteorito 3D de la TNT Meteorito: una roca ardiente visible que cae del
 * cielo con estela de fuego y humo, y al impactar con el suelo explota
 * dejando un crater con lava.
 */
public class MeteorEntity extends Entity {

    private static final double FALL_SPEED = 0.7;

    public MeteorEntity(EntityType<? extends MeteorEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
    }

    public MeteorEntity(Level level, double x, double y, double z) {
        this(TntsEntities.METEOR.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) this.level();

        // estela de fuego y humo
        serverLevel.sendParticles(ParticleTypes.FLAME,
                this.getX(), this.getY(), this.getZ(), 4, 0.3, 0.3, 0.3, 0.01);
        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                this.getX(), this.getY(), this.getZ(), 2, 0.4, 0.4, 0.4, 0.0);
        serverLevel.sendParticles(ParticleTypes.SMOKE,
                this.getX(), this.getY() + 0.5, this.getZ(), 1, 0.2, 0.4, 0.2, 0.0);

        // sonido de zumbido mientras cae
        if (this.tickCount % 10 == 0) {
            serverLevel.playSound(null, this.blockPosition(), SoundEvents.FIRE_AMBIENT,
                    SoundSource.WEATHER, 0.6F, 0.4F);
        }

        // cae (misma velocidad que un bloque cayendo, pero mas lento para verse bien)
        this.setDeltaMovement(0, -FALL_SPEED, 0);
        this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());

        // impacto con el suelo (o con un bloque solido bajo)
        if (this.onGround() || !this.level().getBlockState(this.blockPosition().below()).isAir()) {
            impact(serverLevel);
        }
        // limite de caida (nunca deberia llegar, pero por seguridad)
        if (this.getY() < this.level().getMinBuildHeight()) {
            impact(serverLevel);
        }
    }

    private void impact(ServerLevel serverLevel) {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        BlockPos center = this.blockPosition();

        // explosion + crater de lava
        serverLevel.explode(this, x, y + 1, z, 3.0f + serverLevel.random.nextFloat() * 2.0f,
                true, Level.ExplosionInteraction.BLOCK);
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 0, 2))) {
            if (serverLevel.isEmptyBlock(p) && serverLevel.getBlockState(p.below()).isSolid()
                    && serverLevel.random.nextInt(3) != 0) {
                serverLevel.setBlock(p, Blocks.LAVA.defaultBlockState(), 3);
            }
        }
        // destello de impacto
        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y + 1, z, 1, 0, 0, 0, 0);
        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 2, z, 8, 1, 1, 1, 0.1);
        serverLevel.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 2.5F, 0.8F);
        this.discard();
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

    @Override
    public boolean isPushable() {
        return false;
    }
}
