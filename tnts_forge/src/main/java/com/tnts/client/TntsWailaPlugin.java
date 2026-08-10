package com.tnts.client;

import com.tnts.block.TntBlock;
import com.tnts.block.TntProperties;
import com.tnts.entity.TntsPrimedTnt;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

/**
 * Integracion con WTHIT/TOP (opcional): al mirar una TNT del mod muestra
 * sus propiedades (radio, mecha, fuego, rompe bloques) y al mirar una TNT
 * ENCENDIDA muestra la mecha restante en segundos.
 *
 * Solo se carga si el jugador tiene WTHIT o TOP instalado; si no, esta
 * clase simplemente no se ejecuta (la dependencia es compileOnly).
 */
public class TntsWailaPlugin implements IWailaPlugin {

    private static final IBlockComponentProvider BLOCK_PROVIDER = new IBlockComponentProvider() {
        @Override
        public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            Block block = accessor.getBlock();
            if (!(block instanceof TntBlock tnt)) return;
            TntProperties p = tnt.getTntProperties();
            tooltip.addLine(Component.literal("💥 Radio: " + p.power()
                    + "  |  Mecha: " + String.format("%.1fs", p.fuse() / 20.0)
                    + "  |  Fuego: " + (p.fire() ? "sí" : "no")
                    + "  |  Rompe: " + (p.breaksBlocks() ? "sí" : "no")));
            if (!p.effects().isEmpty()) {
                tooltip.addLine(Component.literal("✨ Efectos: " + p.effects().size()));
            }
        }
    };

    private static final IEntityComponentProvider ENTITY_PROVIDER = new IEntityComponentProvider() {
        @Override
        public void appendBody(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
            if (!(accessor.getEntity() instanceof TntsPrimedTnt tnt)) return;
            int fuse = tnt.getFuse();
            String name = tnt.getVariantName().replace("_tnt", "").replace("_", " ");
            tooltip.addLine(Component.literal("🧨 " + name));
            tooltip.addLine(Component.literal("⏱️ Mecha: " + String.format("%.1fs", fuse / 20.0)));
        }
    };

    @Override
    public void register(IRegistrar registrar) {
        registrar.addComponent(BLOCK_PROVIDER, TooltipPosition.BODY, TntBlock.class);
        registrar.addComponent(ENTITY_PROVIDER, TooltipPosition.BODY, TntsPrimedTnt.class);
    }
}
