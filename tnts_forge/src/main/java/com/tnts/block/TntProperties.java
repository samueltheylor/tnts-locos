package com.tnts.block;

import java.util.EnumSet;

/**
 * Comportamiento configurable de cada TNT.
 *
 * @param power        radio de la explosion (mas grande = mas boom)
 * @param fire         true incendia los bloques cercanos
 * @param breaksBlocks false = no rompe bloques (solo daña entidades)
 * @param fuse         duracion de la mecha en ticks (20 ticks = 1 segundo)
 * @param effects      efectos especiales (ver {@link TntEffect})
 */
public record TntProperties(
        float power,
        boolean fire,
        boolean breaksBlocks,
        int fuse,
        EnumSet<TntEffect> effects) {

    public TntProperties {
        effects = EnumSet.copyOf(effects);
    }

    public boolean has(TntEffect effect) {
        return effects.contains(effect);
    }
}
