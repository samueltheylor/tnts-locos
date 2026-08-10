package com.tnts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tnts.entity.SupernovaEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderiza la supernova 3D: una esfera dorada translucida que pulsa (crece
 * y se encoge) sobre el crater, girando lentamente.
 */
public class SupernovaRenderer extends EntityRenderer<SupernovaEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("tnts", "textures/entity/supernova.png");

    private final CubeModel model;

    public SupernovaRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.model = new CubeModel(true);
    }

    @Override
    public void render(SupernovaEntity entity, float yRot, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight) {
        pose.pushPose();
        float t = entity.tickCount + partialTick;
        // pulso: escala que late (0.8 + 0.4*|sen|)
        float pulse = 1.0F + 0.35F * (float) Math.abs(Math.sin(t * 0.18F));
        pose.scale(pulse, pulse, pulse);
        pose.mulPose(Axis.YP.rotation(t * 0.15F));
        pose.mulPose(Axis.XP.rotation(t * 0.05F));
        this.model.renderToBuffer(pose, buffer.getBuffer(this.model.renderType(TEXTURE)),
                0xF000F0, 0, 1.0F, 1.0F, 1.0F, 0.9F);
        pose.popPose();
        super.render(entity, yRot, partialTick, pose, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SupernovaEntity entity) {
        return TEXTURE;
    }
}
