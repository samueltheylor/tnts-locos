package com.tnts;

import com.tnts.client.ClientSetup;
import com.tnts.config.TntsConfig;
import com.tnts.entity.TntsEntities;
import com.tnts.worldgen.TntsStructures;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TntsMod.MODID)
public class TntsMod {
    public static final String MODID = "tnts";

    public TntsMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        // Config (config/tnts-common.toml)
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TntsConfig.SPEC);

        // Invalida la cache de calidad de particulas al cargar/recargar la config
        bus.addListener((net.minecraftforge.fml.event.config.ModConfigEvent.Loading e) ->
                TntsConfig.invalidateCache());
        bus.addListener((net.minecraftforge.fml.event.config.ModConfigEvent.Reloading e) ->
                TntsConfig.invalidateCache());

        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModCreativeTabs.TABS.register(bus);
        TntsEntities.ENTITIES.register(bus);
        ModSounds.SOUNDS.register(bus);

        // Aldeano Experto en TNTs (POI + profesion)
        ModVillagers.POI_TYPES.register(bus);
        ModVillagers.PROFESSIONS.register(bus);

        // Atributos del Rey TNT y del Golem de TNT
        bus.addListener(com.tnts.entity.TntKingEntity::onAttributeCreate);
        bus.addListener(com.tnts.entity.TntGolemEntity::onAttributeCreate);

        // Estructuras (bunker de TNT)
        TntsStructures.STRUCTURE_TYPES.register(bus);
        TntsStructures.PIECE_TYPES.register(bus);

        // Triggers personalizados para advancements
        ModTriggers.register();

        // Eventos (peto de TNT reactivo)
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(TntsEvents.class);

        // Comando /tnts (give + list)
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
                com.tnts.command.TntsCommands::register);

        // Registro solo cliente (config screen + renderers)
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientSetup::registerClient);
    }
}
