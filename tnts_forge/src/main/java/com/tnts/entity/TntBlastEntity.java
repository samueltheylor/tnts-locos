package com.tnts.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/**
 * Efecto 3D generico de TODAS las TNTs: un cubo que reproduce la textura del
 * bloque real de la variante, crece, gira y se desvanece sobre el crater.
 * Cada TNT tiene su propio "fantasma 3D" con su textura y color de estallido.
 */
public class TntBlastEntity extends Entity {

    private static final EntityDataAccessor<String> DATA_BLOCK =
            SynchedEntityData.defineId(TntBlastEntity.class, EntityDataSerializers.STRING);

    /** Vida total en ticks (crece, se mantiene y se desvanece). */
    public static final int LIFETIME = 26;

    /** Color del estallido de la variante (para las particulas). */
    private Vector3f color = new Vector3f(1.0F, 0.3F, 0.1F);

    public TntBlastEntity(EntityType<? extends TntBlastEntity> type, Level level) {
        super(type, level);
    }

    public TntBlastEntity(ServerLevel level, double x, double y, double z,
                          String blockId, int[] rgb) {
        super(TntsEntities.TNT_BLAST.get(), level);
        this.setPos(x, y, z);
        this.entityData.set(DATA_BLOCK, blockId);
        this.color = new Vector3f(rgb[0] / 255f, rgb[1] / 255f, rgb[2] / 255f);
        this.setNoGravity(true);
    }

    public String getBlockId() {
        return this.entityData.get(DATA_BLOCK);
    }

    public Vector3f getColor() {
        return color;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_BLOCK, "tnts:mini_tnt");
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || this.isRemoved()) return;
        if (this.tickCount >= LIFETIME) {
            this.discard();
            return;
        }
        // sube lentamente y suelta particulas del color de la variante
        ServerLevel serverLevel = (ServerLevel) this.level();
        if (this.tickCount % 3 == 0) {
            serverLevel.sendParticles(new DustParticleOptions(color, 1.2F),
                    this.getX(), this.getY() + 0.3, this.getZ(),
                    4, 0.5, 0.4, 0.5, 0.02);
        }
        // destello al empezar
        if (this.tickCount == 1) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.FLASH,
                    this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("block")) {
            this.entityData.set(DATA_BLOCK, tag.getString("block"));
        }
        if (tag.contains("cr") && tag.contains("cg") && tag.contains("cb")) {
            this.color = new Vector3f(tag.getFloat("cr"), tag.getFloat("cg"), tag.getFloat("cb"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("block", this.getBlockId());
        tag.putFloat("cr", color.x());
        tag.putFloat("cg", color.y());
        tag.putFloat("cb", color.z());
    }
}
