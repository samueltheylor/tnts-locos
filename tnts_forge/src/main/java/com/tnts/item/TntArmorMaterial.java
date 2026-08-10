package com.tnts.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Material de armadura del Peto de TNT: resistencia de hierro y se repara
 * con TNT. El nombre lleva namespace ("tnts:tnt") para que el renderer de
 * armadura busque la capa en tnts:textures/models/armor/tnt_layer_1.png
 * y no en el namespace de minecraft (si no, el peto no se ve en 3a persona).
 */
public class TntArmorMaterial implements ArmorMaterial {

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return 12;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return type == ArmorItem.Type.CHESTPLATE ? 6 : 3;
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_CHAIN;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(Items.TNT);
    }

    @Override
    public String getName() {
        return "tnts:tnt";
    }

    @Override
    public float getToughness() {
        return 1.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.0F;
    }
}
