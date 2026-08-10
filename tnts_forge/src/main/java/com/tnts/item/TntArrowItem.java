package com.tnts.item;

import com.tnts.entity.TntArrowEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Flecha de TNT: se lanza como un proyectil y explota al impactar.
 */
public class TntArrowItem extends Item {

    public TntArrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            TntArrowEntity arrow = new TntArrowEntity(level, player, "tnts:mini_tnt");
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(arrow);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5F,
                    0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            player.getCooldowns().addCooldown(this, 6);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
