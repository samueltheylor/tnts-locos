package com.tnts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tnts.entity.BlackHoleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderiza la bola negra del Agujero Negro como una esfera 3D oscura
 * (un cubo con la textura de esfera) que flota y gira lentamente.
 */
public class BlackHoleRenderer extends EntityRenderer<BlackHoleEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("tnts", "textures/entity/black_hole.png");

    private final BlackHoleModel model;

    public BlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.model = new BlackHoleModel();
    }

    @Override
    public void render(BlackHoleEntity entity, float yRot, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight) {
        pose.pushPose();
        pose.translate(0.0D, 0.5D, 0.0D);
        float t = entity.tickCount + partialTick;
        pose.mulPose(Axis.YP.rotation(t * 0.12F));
        pose.mulPose(Axis.XP.rotation(t * 0.04F));
        pose.scale(1.9F, 1.9F, 1.9F);
        this.model.renderToBuffer(pose, buffer.getBuffer(this.model.renderType(TEXTURE)),
                0xF000F0, 0, 1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
        super.render(entity, yRot, partialTick, pose, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(BlackHoleEntity entity) {
        return TEXTURE;
    }
}
