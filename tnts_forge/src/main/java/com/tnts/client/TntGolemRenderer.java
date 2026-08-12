package com.tnts.client;

import com.tnts.entity.TntGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderiza al GOLEM de TNT: cubo aliado con cara amigable, brazos, piernas
 * y llama en la mecha. Escala 1.1x (un poco mas grande que una TNT normal).
 */
public class TntGolemRenderer extends MobRenderer<TntGolemEntity, TntGolemModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("tnts", "textures/entity/tnt_golem.png");

    public TntGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new TntGolemModel(), 0.8F);
    }

    @Override
    public ResourceLocation getTextureLocation(TntGolemEntity entity) {
        return TEXTURE;
    }
}
