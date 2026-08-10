package com.tnts.entity;

import com.tnts.ModSounds;
import com.tnts.block.TntBlock;
import com.tnts.block.TntProperties;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Flecha de TNT: un proyectil que explota al impactar contra algo
 * (bloque o entidad) con el poder de su variante.
 */
public class TntArrowEntity extends ThrowableProjectile {

    private static final EntityDataAccessor<String> DATA_BLOCK =
            SynchedEntityData.defineId(TntArrowEntity.class, EntityDataSerializers.STRING);

    private String blockId = "tnts:mini_tnt";

    public TntArrowEntity(EntityType<? extends TntArrowEntity> type, Level level) {
        super(type, level);
    }

    public TntArrowEntity(Level level, LivingEntity shooter, String blockId) {
        super(TntsEntities.TNT_ARROW.get(), shooter, level);
        this.blockId = blockId;
        this.entityData.set(DATA_BLOCK, blockId);
    }

    /** Estado para renderizar (la variante sin encender). */
    public net.minecraft.world.level.block.state.BlockState getRenderBlockState() {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
        if (block != null) return block.defaultBlockState();
        return net.minecraft.world.level.block.Blocks.TNT.defaultBlockState();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_BLOCK, "tnts:mini_tnt");
    }

    @Override
    protected void onHit(HitResult result) {
        if (this.level().isClientSide) return;
        Level lvl = this.level();
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
        float power = 3.0f;
        boolean fire = false;
        boolean breaks = true;
        if (block instanceof TntBlock tntBlock) {
            TntProperties p = tntBlock.getTntProperties();
            power = Math.max(1.5f, p.power() * 0.6f);
            fire = p.fire();
            breaks = p.breaksBlocks();
        }
        double x = this.getX(), y = this.getY(), z = this.getZ();
        lvl.explode(this, x, y, z, power, fire,
                breaks ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE);
        lvl.playSound(null, x, y, z, ModSounds.explode(blockId.contains(":") ? blockId.substring(blockId.indexOf(':') + 1) : blockId),
                SoundSource.BLOCKS, 1.6F, 1.0F);
        this.discard();
    }
}
