package com.tnts.item;

import com.tnts.ModSounds;
import com.tnts.block.TntBlock;
import com.tnts.entity.TntsPrimedTnt;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Lanzador de TNT: consume una TNT del inventario (cualquier variante) y la
 * dispara con fisica real. Sin municion en el inventario no hace nada.
 */
public class TntLauncherItem extends Item {

    public TntLauncherItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack launcher = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(launcher);

        // buscar una TNT del mod en el inventario y consumirla
        String blockId = null;
        int fuse = 40;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof TntBlock tntBlock) {
                blockId = ForgeRegistries.BLOCKS.getKey(tntBlock).toString();
                fuse = tntBlock.getTntProperties().fuse();
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                break;
            }
        }

        if (blockId == null) {
            // sin municion: click seco
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.DETONATOR_CLICK.get(), SoundSource.PLAYERS, 0.6F, 0.5F);
            return InteractionResultHolder.fail(launcher);
        }

        TntsPrimedTnt tnt = new TntsPrimedTnt(level,
                player.getX(), player.getEyeY() - 0.1, player.getZ(), blockId, fuse, player);
        tnt.setStationary(false); // lanzada: conserva la fisica de proyectil
        tnt.setDeltaMovement(player.getLookAngle().scale(1.6));
        level.addFreshEntity(tnt);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.LAUNCHER_SHOOT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.getCooldowns().addCooldown(this, 10);
        return InteractionResultHolder.success(launcher);
    }
}
