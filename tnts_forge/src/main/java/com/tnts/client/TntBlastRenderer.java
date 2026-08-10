package com.tnts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tnts.entity.TntBlastEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Renderiza el efecto 3D generico de TODAS las TNTs: dibuja el BLOQUE REAL de
 * la variante (su textura) escalando, girando y desvaneciendose sobre el
 * crater. Asi cada TNT tiene su propio fantasma 3D con su textura propia.
 */
public class TntBlastRenderer extends EntityRenderer<TntBlastEntity> {

    public TntBlastRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(TntBlastEntity entity, float yRot, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight) {
        BlockState state = ForgeRegistries.BLOCKS.getValue(
                new ResourceLocation(entity.getBlockId())).defaultBlockState();
        float t = entity.tickCount + partialTick;
        float life = Math.min(1.0F, t / TntBlastEntity.LIFETIME);

        pose.pushPose();
        pose.translate(0.0D, 0.4D, 0.0D);
        // crece al principio y se encoge al final (desvanece)
        float scale = 0.5F + 1.1F * (float) Math.sin(life * Math.PI);
        pose.scale(scale, scale, scale);
        // rota suavemente
        pose.mulPose(Axis.YP.rotation(t * 0.4F));
        pose.mulPose(Axis.XP.rotation((float) Math.sin(t * 0.13F) * 0.3F));
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                state, pose, buffer, 0xF000F0, 0);
        pose.popPose();

        super.render(entity, yRot, partialTick, pose, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TntBlastEntity entity) {
        return null;
    }
}
