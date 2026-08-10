package com.tnts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tnts.entity.ShockwaveEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer de la onda de choque: no dibuja modelo (el efecto 3D se ve por
 * las particulas del anillo expansivo), solo evita el cubo por defecto.
 */
public class ShockwaveRenderer extends EntityRenderer<ShockwaveEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("tnts", "textures/entity/meteor.png");

    public ShockwaveRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(ShockwaveEntity entity, float yRot, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight) {
        // sin modelo: solo particulas (ver ShockwaveEntity.tick)
    }

    @Override
    public ResourceLocation getTextureLocation(ShockwaveEntity entity) {
        return TEXTURE;
    }
}
