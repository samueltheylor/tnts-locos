package com.tnts.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * Mesa de TNTs: la estacion de trabajo del aldeano "Experto en TNTs".
 * Es el POI (point of interest) que convierte a un aldeano desempleado en
 * un vendedor de TNTs (ver {@link com.tnts.ModVillagers}).
 */
public class TntTableBlock extends Block {

    public TntTableBlock(MapColor color) {
        super(Properties.of()
                .mapColor(color)
                .strength(2.5f, 6.0f)
                .sound(SoundType.WOOD));
    }
}
