package com.tnts.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Pico de TNT: cada bloque que rompes explota (radio 2). ¡Cuidado con lo que
 * hay alrededor, y con las TNTs que puedas encender en cadena!
 */
public class TntPickaxeItem extends PickaxeItem {

    public TntPickaxeItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide && !state.isAir()) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;
            level.explode(null, x, y, z, 2.0F, false, Level.ExplosionInteraction.BLOCK);
        }
        return super.mineBlock(stack, level, state, pos, entity);
    }
}
