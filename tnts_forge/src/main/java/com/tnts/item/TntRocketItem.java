package com.tnts.item;

import com.tnts.entity.TntRocketEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * TNT COHETE: al usarlo coloca la TNT montable frente al jugador (2 bloques
 * de distancia), para luego montarla y despegar.
 */
public class TntRocketItem extends Item {

    public TntRocketItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), true);
        // coloca la entidad a 2 bloques en la direccion de la mirada
        Vec3 look = player.getLookAngle();
        Vec3 pos = player.position().add(look.x * 2.0, 0.0, look.z * 2.0);
        TntRocketEntity rocket = new TntRocketEntity(
                com.tnts.entity.TntsEntities.TNT_ROCKET.get(), level);
        rocket.setPos(pos.x, player.getY(), pos.z);
        level.addFreshEntity(rocket);
        player.getItemInHand(hand).shrink(1);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), true);
    }
}
