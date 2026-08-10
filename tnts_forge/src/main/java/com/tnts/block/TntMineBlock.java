package com.tnts.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Mina TNT: como una TNT normal pero explota en cuanto un ser vivo la pisa.
 * (Tambien se puede encender con mechero, redstone o en cadena.)
 */
public class TntMineBlock extends TntBlock {

    public TntMineBlock(String name, TntProperties defaultProps, MapColor color) {
        super(name, defaultProps, color);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!level.isClientSide && entity instanceof LivingEntity) {
            prime(level, pos, state, null);
        }
    }
}
