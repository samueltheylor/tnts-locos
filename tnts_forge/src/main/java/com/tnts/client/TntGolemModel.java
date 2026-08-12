package com.tnts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tnts.entity.TntGolemEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

/**
 * Modelo del GOLEM de TNT (v2): un golem de verdad — cabeza grande con cara
 * amigable, cuerpo ancho de TNT, brazos gruesos que cuelgan hasta la cadera
 * y piernas cortas y anchas. Mecha con llama parpadeante sobre la cabeza.
 * Las piezas usan posiciones ABSOLUTAS desde root (como el Rey TNT).
 */
public class TntGolemModel extends EntityModel<TntGolemEntity> {

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart fuse;
    private final ModelPart flame;

    public TntGolemModel() {
        super(RenderType::entityCutoutNoCull);
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ===== CABEZA: cubo 10x8x10 con la cara (de y=-24 a y=-16) =====
        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(56, 0)
                        .addBox(-5.0F, -8.0F, -5.0F, 10.0F, 8.0F, 10.0F),
                PartPose.offset(0.0F, -16.0F, 0.0F));

        // ===== CUERPO: cubo 14x16x14 de TNT (de y=-16 a y=0) =====
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-7.0F, -16.0F, -7.0F, 14.0F, 16.0F, 14.0F),
                PartPose.ZERO);

        // ===== BRAZOS GRUESOS: 6x14x6 colgando de los hombros =====
        // pivote en el hombro (y=-16) y cuelgan hasta y=-2
        root.addOrReplaceChild("leftArm",
                CubeListBuilder.create().texOffs(96, 0)
                        .addBox(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F),
                PartPose.offset(-11.0F, -16.0F, 0.0F));
        root.addOrReplaceChild("rightArm",
                CubeListBuilder.create().texOffs(96, 20)
                        .addBox(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F),
                PartPose.offset(11.0F, -16.0F, 0.0F));

        // ===== PIERNAS CORTAS Y ANCHAS: 6x8x6 (de y=-8 a y=0) =====
        root.addOrReplaceChild("leftLeg",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-3.0F, 0.0F, -3.0F, 6.0F, 8.0F, 6.0F),
                PartPose.offset(-4.5F, -8.0F, 0.0F));
        root.addOrReplaceChild("rightLeg",
                CubeListBuilder.create().texOffs(24, 32)
                        .addBox(-3.0F, 0.0F, -3.0F, 6.0F, 8.0F, 6.0F),
                PartPose.offset(4.5F, -8.0F, 0.0F));

        // ===== MECHA sobre la cabeza + LLAMA =====
        root.addOrReplaceChild("fuse",
                CubeListBuilder.create().texOffs(120, 0)
                        .addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, -24.0F, 3.0F));
        root.addOrReplaceChild("flame",
                CubeListBuilder.create().texOffs(100, 48)
                        .addBox(-1.5F, -4.0F, -1.5F, 3.0F, 4.0F, 3.0F),
                PartPose.offset(0.0F, -26.5F, 3.0F));

        ModelPart baked = root.bake(128, 64);
        this.head = baked.getChild("head");
        this.body = baked.getChild("body");
        this.leftArm = baked.getChild("leftArm");
        this.rightArm = baked.getChild("rightArm");
        this.leftLeg = baked.getChild("leftLeg");
        this.rightLeg = baked.getChild("rightLeg");
        this.fuse = baked.getChild("fuse");
        this.flame = baked.getChild("flame");
    }

    @Override
    public void setupAnim(TntGolemEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float walk = Math.min(1.0F, limbSwingAmount * 2.0F);
        // el cuerpo late suavemente como una TNT a punto de explotar
        float pulse = 1.0F + 0.03F * (float) Math.sin(ageInTicks * 0.28F);
        this.body.xScale = pulse;
        this.body.yScale = 2.0F - pulse;
        this.body.zScale = pulse;
        this.body.yRot = netHeadYaw * (float) (Math.PI / 180.0) * 0.18F;

        // la cabeza sigue el giro del cuerpo y el pitch de la mirada
        this.head.yRot = this.body.yRot;
        this.head.xRot = headPitch * (float) (Math.PI / 180.0) * 0.3F;

        // brazos: se balancean al caminar como un golem pesado
        this.leftArm.xRot = (float) Math.cos(limbSwing * 0.7F) * 0.6F * walk;
        this.rightArm.xRot = (float) Math.cos(limbSwing * 0.7F + (float) Math.PI) * 0.6F * walk;
        this.leftArm.zRot = -0.08F;
        this.rightArm.zRot = 0.08F;

        // piernas: paso corto y pesado
        this.leftLeg.xRot = (float) Math.cos(limbSwing * 0.7F + (float) Math.PI) * 0.7F * walk;
        this.rightLeg.xRot = (float) Math.cos(limbSwing * 0.7F) * 0.7F * walk;

        // llama parpadeante
        float flicker = 0.75F + 0.4F * (float) Math.sin(ageInTicks * 0.9F)
                + 0.2F * (float) Math.sin(ageInTicks * 2.7F);
        this.flame.yScale = Math.max(0.4F, flicker);
        this.flame.xScale = 1.0F + 0.12F * (float) Math.sin(ageInTicks * 1.3F);
        this.flame.zScale = 1.0F + 0.12F * (float) Math.cos(ageInTicks * 1.1F);
        this.fuse.yRot = this.head.yRot;
        this.flame.yRot = this.head.yRot;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.head.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.body.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.leftArm.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.rightArm.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.leftLeg.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.rightLeg.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.fuse.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.flame.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
