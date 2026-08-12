package com.tnts.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/**
 * Corona del Rey TNT: el botin del jefe. Es un casco de armadura; mientras
 * lo llevas puesto tienes Resistencia al Fuego y tus TNTs del mod explotan
 * con +50% de radio (ver TntBlock.prime y TntsEvents).
 * <p>
 * En el jugador se renderiza con un modelo 3D custom (banda dorada + 5
 * puntas) en vez de la capa plana de armadura, y usa su propia textura
 * (tnt_king_crown_layer_1.png).
 */
public class TntKingCrownItem extends ArmorItem {

    public TntKingCrownItem(ArmorMaterial material, ArmorItem.Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.model.HumanoidModel<?> getHumanoidArmorModel(
                    LivingEntity entity, ItemStack stack, EquipmentSlot slot,
                    net.minecraft.client.model.HumanoidModel<?> original) {
                return com.tnts.client.TntKingCrownModel.INSTANCE;
            }
        });
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "tnts:textures/models/armor/tnt_king_crown_layer_1.png";
    }
}
