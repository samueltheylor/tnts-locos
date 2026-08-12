package com.tnts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tnts.entity.TntRocketEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

/**
 * Modelo del TNT COHETE: cubo de TNT con un propulsor de hierro en la parte
 * inferior y una llama que crece al volar.
 */
public class TntRocketModel extends EntityModel<TntRocketEntity> {

    private final ModelPart body;
    private final ModelPart nozzle;
    private final ModelPart flame;

    public TntRocketModel() {
        super(RenderType::entityCutoutNoCull);
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // cuerpo: cubo de TNT 16x16x16
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.ZERO);

        // propulsor: tubo de hierro debajo del cuerpo
        root.addOrReplaceChild("nozzle",
                CubeListBuilder.create().texOffs(64, 0)
                        .addBox(-3.0F, -4.0F, -3.0F, 6.0F, 4.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // llama del propulsor
        root.addOrReplaceChild("flame",
                CubeListBuilder.create().texOffs(64, 16)
                        .addBox(-2.5F, -6.0F, -2.5F, 5.0F, 6.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        ModelPart baked = root.bake(128, 64);
        this.body = baked.getChild("body");
        this.nozzle = baked.getChild("nozzle");
        this.flame = baked.getChild("flame");
    }

    @Override
    public void setupAnim(TntRocketEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        boolean flying = entity.isFlying();
        // la llama crece y parpadea al volar
        float flicker = 0.7F + 0.4F * (float) Math.sin(ageInTicks * 1.1F)
                + 0.2F * (float) Math.sin(ageInTicks * 3.1F);
        float flameScale = flying ? 1.0F : 0.35F;
        this.flame.yScale = Math.max(0.35F, flicker) * flameScale;
        this.flame.xScale = 1.0F + (flying ? 0.15F : 0.0F) * (float) Math.sin(ageInTicks * 1.7F);
        this.flame.zScale = 1.0F + (flying ? 0.15F : 0.0F) * (float) Math.cos(ageInTicks * 1.4F);
        // el cuerpo se inclina levemente al volar
        this.body.xRot = flying ? 0.12F : 0.0F;
        this.nozzle.xRot = this.body.xRot;
        this.flame.xRot = this.body.xRot;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.body.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.nozzle.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.flame.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
