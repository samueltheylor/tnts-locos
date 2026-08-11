package com.tnts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tnts.entity.TntKingEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

/**
 * Modelo del Rey TNT: un cubo gigante de TNT con su corona dorada en la
 * cabeza. Late suavemente y se inclina hacia su objetivo mientras pelea.
 */
public class TntKingModel extends EntityModel<TntKingEntity> {

    private final ModelPart body;
    private final ModelPart crown;

    public TntKingModel() {
        super(RenderType::entityCutoutNoCull);
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("crown",
                CubeListBuilder.create().texOffs(0, 32).addBox(-6.0F, -20.0F, -6.0F, 12.0F, 4.0F, 12.0F),
                PartPose.ZERO);
        ModelPart baked = root.bake(64, 64);
        this.body = baked.getChild("body");
        this.crown = baked.getChild("crown");
    }

    @Override
    public void setupAnim(TntKingEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // late suavemente como un corazon de TNT
        float pulse = 1.0F + 0.035F * (float) Math.sin(ageInTicks * 0.22F);
        this.body.xScale = pulse;
        this.body.zScale = pulse;
        // se gira hacia su objetivo
        this.body.yRot = netHeadYaw * (float) (Math.PI / 180.0) * 0.45F;
        this.body.xRot = headPitch * (float) (Math.PI / 180.0) * 0.2F;
        // la corona sigue al cuerpo (misma pulsacion, sin rotacion extra)
        this.crown.xRot = this.body.xRot;
        this.crown.yRot = this.body.yRot;
        this.crown.xScale = pulse;
        this.crown.zScale = pulse;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.body.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.crown.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
