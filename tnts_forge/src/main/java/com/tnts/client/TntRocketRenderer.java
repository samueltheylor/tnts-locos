package com.tnts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tnts.entity.TntRocketEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderiza el TNT COHETE montable.
 */
public class TntRocketRenderer extends EntityRenderer<TntRocketEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("tnts", "textures/entity/tnt_rocket.png");

    private final TntRocketModel model = new TntRocketModel();

    public TntRocketRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TntRocketEntity entity, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight) {
        pose.pushPose();
        // centra el cubo: el cuerpo va de y=-16..0, lo bajamos para que el
        // suelo del cubo toque y=0 (el propulsor queda debajo)
        pose.translate(0.0F, -0.5F, 0.0F);
        float bob = (float) Math.sin((entity.tickCount + partialTick) * 0.15F) * 0.02F;
        pose.translate(0.0F, bob, 0.0F);
        this.model.setupAnim(entity, 0, 0, entity.tickCount + partialTick, 0, 0);
        this.model.renderToBuffer(pose, buffer.getBuffer(this.model.renderType(TEXTURE)),
                packedLight, 0, 1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TntRocketEntity entity) {
        return TEXTURE;
    }
}
