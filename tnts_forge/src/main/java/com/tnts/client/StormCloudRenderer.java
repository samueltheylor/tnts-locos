package com.tnts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tnts.entity.StormCloudEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderiza la nube de tormenta 3D: un cubo gris oscuro grande que flota
 * sobre la zona, con un leve balanceo y parpadeo de las chispas electricas.
 */
public class StormCloudRenderer extends EntityRenderer<StormCloudEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("tnts", "textures/entity/storm_cloud.png");

    private final CubeModel model;

    public StormCloudRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.model = new CubeModel(false);
    }

    @Override
    public void render(StormCloudEntity entity, float yRot, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight) {
        pose.pushPose();
        pose.translate(0.0D, 0.0D, 0.0D);
        float t = entity.tickCount + partialTick;
        pose.mulPose(Axis.YP.rotation((float) Math.sin(t * 0.05F) * 0.1F)); // balanceo lento
        pose.scale(4.0F, 1.4F, 4.0F); // nube ancha y baja
        this.model.renderToBuffer(pose, buffer.getBuffer(this.model.renderType(TEXTURE)),
                0xF000F0, 0, 1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
        super.render(entity, yRot, partialTick, pose, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(StormCloudEntity entity) {
        return TEXTURE;
    }
}
