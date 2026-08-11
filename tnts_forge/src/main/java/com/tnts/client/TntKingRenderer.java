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

    /** Fases visuales: textura segun el nivel de grietas (0 sano, 1 agrietado, 2 muy agrietado). */
    private static final ResourceLocation[] TEXTURES = {
            new ResourceLocation("tnts", "textures/entity/tnt_king.png"),
            new ResourceLocation("tnts", "textures/entity/tnt_king_damaged.png"),
            new ResourceLocation("tnts", "textures/entity/tnt_king_damaged2.png"),
    };

    public TntKingRenderer(EntityRendererProvider.Context context) {
        super(context, new TntKingModel(), 1.0F);
    }

    @Override
    protected void scale(TntKingEntity entity, PoseStack pose, float partialTick) {
        // 1.3x para imponer sobre los bloques de TNT + animacion de entrada
        // (el modelo crece con un "pop" elastico al ser convocado)
        float s = 1.3F * this.model.getModelScale(entity, partialTick);
        pose.scale(s, s, s);
        super.scale(entity, pose, partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(TntKingEntity entity) {
        return TEXTURES[Math.min(2, Math.max(0, entity.getCrackLevel()))];
    }
}
