package com.tnts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tnts.entity.TntKingEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderiza al Rey TNT: el cubo gigante con corona, escalado 1.3x para que
 * imponga sobre los bloques de TNT normales.
 */
public class TntKingRenderer extends MobRenderer<TntKingEntity, TntKingModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("tnts", "textures/entity/tnt_king.png");

    public TntKingRenderer(EntityRendererProvider.Context context) {
        super(context, new TntKingModel(), 1.0F);
    }

    @Override
    protected void scale(TntKingEntity entity, PoseStack pose, float partialTick) {
        pose.scale(1.3F, 1.3F, 1.3F);
        super.scale(entity, pose, partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(TntKingEntity entity) {
        return TEXTURE;
    }
}
