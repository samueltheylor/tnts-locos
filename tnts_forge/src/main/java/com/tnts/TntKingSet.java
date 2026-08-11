package com.tnts;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Set del Rey TNT: Corona + Peto + Espada + Escudo.
 * <p>
 * Sinergias progresivas segun cuantas piezas llevas puestas:
 * <ul>
 *   <li><b>2 piezas</b> — "Poder del Rey": la Espada del Rey enciende TNTs
 *       con el DOBLE de radio (12 bloques en vez de 6).</li>
 *   <li><b>3 piezas</b> — "Escudo Real": inmunidad al daño de las TNTs del
 *       propio mod (las tuyas ya no te hieren).</li>
 *   <li><b>4 piezas</b> — "Aura del Rey": cada 2s un aura enciende las TNTs
 *       del mod cercanas (radio 8) automaticamente, con destello dorado.</li>
 * </ul>
 */
public final class TntKingSet {

    /** Cuenta cuantas piezas del set del Rey lleva puestas (0 a 4). */
    public static int countPieces(LivingEntity entity) {
        int count = 0;
        if (entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.TNT_KING_CROWN.get())) count++;
        if (entity.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.TNT_CHESTPLATE.get())) count++;
        ItemStack main = entity.getMainHandItem();
        ItemStack off = entity.getOffhandItem();
        if (main.is(ModItems.TNT_KING_SWORD.get()) || off.is(ModItems.TNT_KING_SWORD.get())) count++;
        if (main.is(ModItems.TNT_SHIELD.get()) || off.is(ModItems.TNT_SHIELD.get())) count++;
        return count;
    }

    private TntKingSet() {
    }
}
