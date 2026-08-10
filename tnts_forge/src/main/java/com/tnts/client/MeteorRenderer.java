package com.tnts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tnts.entity.MeteorEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderiza el meteorito 3D: una roca ardiente (cubo con textura de piedra
 * y brasas) que gira y vibra mientras cae del cielo.
 */
public class MeteorRenderer extends EntityRenderer<MeteorEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("tnts", "textures/entity/meteor.png");

    private final CubeModel model;

    public MeteorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.model = new CubeModel(false);
    }

    @Override
    public void render(MeteorEntity entity, float yRot, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight) {
        pose.pushPose();
        pose.translate(0.0D, -0.5D, 0.0D);
        float t = entity.tickCount + partialTick;
        pose.mulPose(Axis.YP.rotation(t * 0.5F));
        pose.mulPose(Axis.XP.rotation(t * 0.35F));
        pose.mulPose(Axis.ZP.rotation((float) Math.sin(t * 0.7F) * 0.15F)); // vibracion
        pose.scale(0.9F, 0.9F, 0.9F);
        this.model.renderToBuffer(pose, buffer.getBuffer(this.model.renderType(TEXTURE)),
                0xF000F0, 0, 1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
        super.render(entity, yRot, partialTick, pose, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MeteorEntity entity) {
        return TEXTURE;
    }
}
