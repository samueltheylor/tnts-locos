package com.tnts.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

/**
 * Corona del Rey TNT: el botin del jefe. Es un casco de armadura; mientras
 * lo llevas puesto tienes Resistencia al Fuego y tus TNTs del mod explotan
 * con +50% de radio (ver TntBlock.prime y TntsEvents).
 */
public class TntKingCrownItem extends ArmorItem {

    public TntKingCrownItem(ArmorMaterial material, ArmorItem.Type type, Properties properties) {
        super(material, type, properties);
    }
}
