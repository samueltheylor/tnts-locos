package com.tnts.item;

import com.tnts.block.TntBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Espada del Rey TNT: botin raro del jefe. Hoja de TNT fundida — al golpear
 * a un enemigo enciende TODAS las TNTs del mod en un radio de 6 bloques
 * (perfecto para reacciones en cadena). Se repara en el yunque con polvora.
 */
public class TntKingSwordItem extends SwordItem {

    private static final Tier KING_TIER = new Tier() {
        @Override
        public int getUses() {
            return 1561;
        }

        @Override
        public float getSpeed() {
            return 8.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 3.5F;
        }

        @Override
        public int getLevel() {
            return 3;
        }

        @Override
        public int getEnchantmentValue() {
            return 12;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.GUNPOWDER);
        }
    };

    public TntKingSwordItem(Properties properties) {
        super(KING_TIER, 3, -2.4F, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = target.level();
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // enciende las TNTs del mod cercanas (radio 6) -> reaccion en cadena
            BlockPos center = target.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(
                    center.offset(-6, -3, -6), center.offset(6, 3, 6))) {
                BlockState state = serverLevel.getBlockState(p);
                if (state.getBlock() instanceof TntBlock tnt && !state.getValue(TntBlock.LIT)) {
                    tnt.prime(serverLevel, p, state);
                }
            }
            // chispas de la hoja
            serverLevel.sendParticles(ParticleTypes.SMALL_FLAME,
                    target.getX(), target.getY() + 1, target.getZ(),
                    14, 0.3, 0.4, 0.3, 0.03);
            serverLevel.sendParticles(ParticleTypes.LAVA,
                    target.getX(), target.getY() + 1, target.getZ(),
                    4, 0.2, 0.3, 0.2, 0.0);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
