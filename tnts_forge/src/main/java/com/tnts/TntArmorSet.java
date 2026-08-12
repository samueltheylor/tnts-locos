package com.tnts;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Armadura de TNT: Casco + Peto + Pantalon + Botas.
 * <p>
 * Sinergia: con las 4 piezas puestas ("Blindaje de TNT") el daño de las
 * explosiones se reduce a la mitad (+50% de resistencia a explosiones).
 */
public final class TntArmorSet {

    /** Cuenta cuantas piezas de la armadura de TNT lleva puestas (0 a 4). */
    public static int countPieces(LivingEntity entity) {
        int count = 0;
        if (entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.TNT_HELMET.get())) count++;
        if (entity.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.TNT_CHESTPLATE.get())) count++;
        if (entity.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.TNT_LEGGINGS.get())) count++;
        if (entity.getItemBySlot(EquipmentSlot.FEET).is(ModItems.TNT_BOOTS.get())) count++;
        return count;
    }

    /** True si lleva las 4 piezas (set bonus activo). */
    public static boolean isComplete(LivingEntity entity) {
        return countPieces(entity) == 4;
    }

    private TntArmorSet() {
    }
}
