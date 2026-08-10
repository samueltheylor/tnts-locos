package com.tnts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tnts.entity.TntArrowEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderiza la Flecha de TNT como un mini bloque de TNT volando.
 */
public class TntArrowRenderer extends EntityRenderer<TntArrowEntity> {

    private final BlockRenderDispatcher blockRenderer;

    public TntArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.15F;
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(TntArrowEntity entity, float yRot, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight) {
        pose.pushPose();
        pose.translate(0.0D, 0.1D, 0.0D);
        pose.mulPose(Axis.YP.rotationDegrees(-entity.getYRot() + 90.0F));
        pose.scale(0.35F, 0.35F, 0.35F);
        this.blockRenderer.renderSingleBlock(entity.getRenderBlockState(), pose, buffer, packedLight, 0);
        pose.popPose();
        super.render(entity, yRot, partialTick, pose, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TntArrowEntity entity) {
        // sin textura propia: se renderiza un mini bloque de TNT
        return new ResourceLocation("tnts:textures/entity/tnt_arrow.png");
    }
}
