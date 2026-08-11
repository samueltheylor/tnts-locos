package com.tnts.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;

/**
 * Escudo de TNT: botin raro del Rey TNT. Bloquea como un escudo normal y,
 * ademas, al bloquear un golpe empuja al atacante lejos (efecto en
 * TntsEvents). Se repara en el yunque con polvora.
 */
public class TntShieldItem extends ShieldItem {

    public TntShieldItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        // se repara con polvora (o con cualquier TNT del mod como "material")
        if (repairCandidate.is(Items.GUNPOWDER)) return true;
        return repairCandidate.getItem() instanceof net.minecraft.world.item.BlockItem
                && repairCandidate.is(com.tnts.ModItems.MINI_TNT.get());
    }
}
