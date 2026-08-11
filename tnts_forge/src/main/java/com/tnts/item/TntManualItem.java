package com.tnts.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * Manual de TNTs: al usarlo abre un GUI con todas las TNTs del mod, su
 * nombre, su efecto y su radio. La apertura de la pantalla vive en
 * {@link com.tnts.client.TntManualClient} (solo cliente) para que el
 * servidor dedicado nunca cargue las clases de pantalla.
 */
public class TntManualItem extends Item {

    public TntManualItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> com.tnts.client.TntManualClient::open);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
