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
 * Modelo del GOLEM de TNT: cubo de TNT con cara amigable, bracitos, piernas
 * cortas, mecha con llama parpadeante. Las piezas usan posiciones ABSOLUTAS
 * desde root (como el Rey TNT).
 */
public class TntGolemModel extends EntityModel<TntGolemEntity> {

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

        // cuerpo: cubo de TNT 16x16x16 (de y=-16 a y=0)
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.ZERO);

        // brazos: 4x10x4 colgando de los hombros
        root.addOrReplaceChild("leftArm",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F),
                PartPose.offset(-10.0F, -14.0F, 0.0F));
        root.addOrReplaceChild("rightArm",
                CubeListBuilder.create().texOffs(16, 32)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F),
                PartPose.offset(10.0F, -14.0F, 0.0F));

        // piernas: 4x6x4
        root.addOrReplaceChild("leftLeg",
                CubeListBuilder.create().texOffs(32, 32)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offset(-4.0F, -6.0F, 0.0F));
        root.addOrReplaceChild("rightLeg",
                CubeListBuilder.create().texOffs(48, 32)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offset(4.0F, -6.0F, 0.0F));

        // mecha y llama sobre el cuerpo
        root.addOrReplaceChild("fuse",
                CubeListBuilder.create().texOffs(64, 32)
                        .addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, -16.0F, 3.0F));
        root.addOrReplaceChild("flame",
                CubeListBuilder.create().texOffs(72, 32)
                        .addBox(-1.5F, -4.0F, -1.5F, 3.0F, 4.0F, 3.0F),
                PartPose.offset(0.0F, -18.5F, 3.0F));

        ModelPart baked = root.bake(128, 64);
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
        // cuerpo late suavemente como una TNT a punto de explotar
        float pulse = 1.0F + 0.03F * (float) Math.sin(ageInTicks * 0.28F);
        this.body.xScale = pulse;
        this.body.yScale = 2.0F - pulse;
        this.body.zScale = pulse;
        this.body.yRot = netHeadYaw * (float) (Math.PI / 180.0) * 0.25F;

        // brazos: balanceo al caminar
        this.leftArm.xRot = (float) Math.cos(limbSwing * 0.9F) * 0.8F * walk;
        this.rightArm.xRot = (float) Math.cos(limbSwing * 0.9F + (float) Math.PI) * 0.8F * walk;
        this.leftArm.zRot = -0.1F;
        this.rightArm.zRot = 0.1F;

        // piernas
        this.leftLeg.xRot = (float) Math.cos(limbSwing * 0.9F + (float) Math.PI) * 0.9F * walk;
        this.rightLeg.xRot = (float) Math.cos(limbSwing * 0.9F) * 0.9F * walk;

        // llama parpadeante
        float flicker = 0.75F + 0.4F * (float) Math.sin(ageInTicks * 0.9F)
                + 0.2F * (float) Math.sin(ageInTicks * 2.7F);
        this.flame.yScale = Math.max(0.4F, flicker);
        this.flame.xScale = 1.0F + 0.12F * (float) Math.sin(ageInTicks * 1.3F);
        this.flame.zScale = 1.0F + 0.12F * (float) Math.cos(ageInTicks * 1.1F);
        this.fuse.yRot = this.body.yRot;
        this.flame.yRot = this.body.yRot;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.body.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.leftArm.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.rightArm.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.leftLeg.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.rightLeg.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.fuse.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.flame.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
