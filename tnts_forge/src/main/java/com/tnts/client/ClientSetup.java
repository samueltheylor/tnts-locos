package com.tnts.client;

import com.tnts.entity.TntsEntities;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Registro SOLO cliente (cargado via DistExecutor desde TntsMod): el boton de
 * config en el menu de mods y los renderers de las entidades. Mantener las
 * clases de cliente fuera de las clases comunes evita que un servidor
 * dedicado intente cargar Screen y falle.
 */
public class ClientSetup {

    public static void registerClient() {
        // boton de config en el menu de mods
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> new TntsConfigScreen(parent)));

        // renderers de las entidades
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::registerRenderers);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TntsEntities.PRIMED_TNT.get(), TntsPrimedTntRenderer::new);
        event.registerEntityRenderer(TntsEntities.TNT_ARROW.get(), TntArrowRenderer::new);
        event.registerEntityRenderer(TntsEntities.BLACK_HOLE.get(), BlackHoleRenderer::new);
        event.registerEntityRenderer(TntsEntities.METEOR.get(), MeteorRenderer::new);
        event.registerEntityRenderer(TntsEntities.STORM_CLOUD.get(), StormCloudRenderer::new);
        event.registerEntityRenderer(TntsEntities.SHOCKWAVE.get(), ShockwaveRenderer::new);
        event.registerEntityRenderer(TntsEntities.SUPERNOVA.get(), SupernovaRenderer::new);
        event.registerEntityRenderer(TntsEntities.TNT_BLAST.get(), TntBlastRenderer::new);
        event.registerEntityRenderer(TntsEntities.TNT_KING.get(), TntKingRenderer::new);
        event.registerEntityRenderer(TntsEntities.TNT_GOLEM.get(), TntGolemRenderer::new);
        event.registerEntityRenderer(TntsEntities.TNT_ROCKET.get(), TntRocketRenderer::new);
    }
}
