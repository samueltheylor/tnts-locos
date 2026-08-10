package com.tnts.item;

import com.tnts.ModSounds;
import com.tnts.entity.TntsPrimedTnt;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Granada de TNT: se lanza como una TNT encendida con mecha corta (1.5s)
 * que rebota y explota. Solo existe en Java (Bedrock no permite lanzar
 * entidades con mecha propia).
 */
public class TntGrenadeItem extends Item {

    public TntGrenadeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            TntsPrimedTnt grenade = new TntsPrimedTnt(level,
                    player.getX(), player.getEyeY() - 0.1, player.getZ(),
                    "tnts:mini_tnt", 30, player);
            grenade.setStationary(false); // lanzada: conserva la fisica de proyectil
            grenade.setDeltaMovement(player.getLookAngle().scale(1.2).add(0, 0.2, 0));
            level.addFreshEntity(grenade);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.LAUNCHER_SHOOT.get(), SoundSource.PLAYERS, 0.8F, 1.3F);
            player.getCooldowns().addCooldown(this, 8);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
