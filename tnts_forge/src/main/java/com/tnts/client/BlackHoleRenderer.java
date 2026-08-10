package com.tnts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tnts.entity.BlackHoleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderiza el Agujero Negro como un disco negro con anillos de acrecion:
 * inclinado ~25 grados (look de portal), con brillo completo y los anillos
 * girando en direcciones opuestas mientras el conjunto pulsa y, al final,
 * se comprime en el colapso.
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
        pose.translate(0.0D, 0.35D, 0.0D);
        // inclinacion tipo portal (se ve como un circulo desde el costado)
        pose.mulPose(Axis.XP.rotationDegrees(25.0F));
        this.model.animate(entity.tickCount, partialTick, BlackHoleEntity.LIFETIME);
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
