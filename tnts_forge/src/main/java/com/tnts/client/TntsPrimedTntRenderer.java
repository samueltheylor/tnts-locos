package com.tnts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tnts.entity.TntsPrimedTnt;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Renderiza la TNT encendida con el estado "encendido" de su variante
 * (textura animada) y el pulso/blanco final de vanilla.
 */
public class TntsPrimedTntRenderer extends EntityRenderer<TntsPrimedTnt> {

    private final BlockRenderDispatcher blockRenderer;

    public TntsPrimedTntRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(TntsPrimedTnt entity, float yRot, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight) {
        pose.pushPose();
        pose.translate(0.0F, 0.5F, 0.0F);

        // pulso de crecimiento en los ultimos ticks (como la TNT de vanilla)
        int fuse = entity.getFuse();
        if ((float) fuse - partialTick + 1.0F < 10.0F) {
            float f = 1.0F - ((float) fuse - partialTick + 1.0F) / 10.0F;
            f = Mth.clamp(f, 0.0F, 1.0F);
            f *= f;
            f *= f;
            float scale = 1.0F + f * 0.3F;
            pose.scale(scale, scale, scale);
        }

        pose.mulPose(Axis.YP.rotationDegrees(-90.0F));
        pose.translate(-0.5F, -0.5F, 0.5F);

        // el flash blanco final se ve con la particula FLASH + el pulso de arriba
        BlockState state = entity.getRenderBlockState();
        this.blockRenderer.renderSingleBlock(state, pose, buffer, packedLight, 0);

        pose.popPose();
        super.render(entity, yRot, partialTick, pose, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TntsPrimedTnt entity) {
        // sin textura propia: se renderiza el estado del bloque
        return new ResourceLocation("tnts:textures/entity/primed_tnt.png");
    }
}
