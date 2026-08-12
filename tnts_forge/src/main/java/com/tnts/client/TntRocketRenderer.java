package com.tnts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tnts.entity.TntRocketEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderiza el AVIÓN de TNT montable. El modelo tiene la base en y=0 y la
 * nariz mirando +Z; se rota con el yaw de la entidad (que el tick pone en la
 * dirección de la mirada del piloto).
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
        // rotacion segun el yaw de la entidad (la direccion de la mirada)
        pose.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        // pequeño rebote en vuelo
        float bob = entity.isFlying()
                ? (float) Math.sin((entity.tickCount + partialTick) * 0.3F) * 0.03F : 0.0F;
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
