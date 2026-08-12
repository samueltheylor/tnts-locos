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
 * Modelo del AVIÓN de TNT: un avión hecho de bloques de TNT — fuselaje largo
 * con franja blanca, ala principal, cola con timón vertical y horizontal,
 * hélice delantera y llama del propulsor trasera que crece al volar.
 * El jugador se monta encima del fuselaje.
 *
 * Orientacion: la NARIZ mira hacia +Z (adelante), las ALAS van en X.
 * Las piezas usan posiciones ABSOLUTAS desde root.
 */
public class TntRocketModel extends EntityModel<TntRocketEntity> {

    private final ModelPart fuselage;
    private final ModelPart nose;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart tailFin;
    private final ModelPart tailWing;
    private final ModelPart propeller;
    private final ModelPart flame;

    public TntRocketModel() {
        super(RenderType::entityCutoutNoCull);
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ===== FUSELAJE: cuerpo alargado 12x8x24 (de y=-8 a 0, z=-12 a +12) =====
        root.addOrReplaceChild("fuselage",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-6.0F, -8.0F, -12.0F, 12.0F, 8.0F, 24.0F),
                PartPose.ZERO);

        // ===== NARIZ: cuña que apunta a +Z (z=12..18) =====
        root.addOrReplaceChild("nose",
                CubeListBuilder.create().texOffs(72, 0)
                        .addBox(-4.0F, -6.0F, 12.0F, 8.0F, 5.0F, 6.0F),
                PartPose.ZERO);

        // ===== ALAS: 14x2x10 a cada lado (x=-20..-6 y +6..+20) =====
        root.addOrReplaceChild("leftWing",
                CubeListBuilder.create().texOffs(72, 16)
                        .addBox(-20.0F, -6.0F, -2.0F, 14.0F, 2.0F, 10.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("rightWing",
                CubeListBuilder.create().texOffs(0, 48)
                        .addBox(6.0F, -6.0F, -2.0F, 14.0F, 2.0F, 10.0F),
                PartPose.ZERO);

        // ===== COLA: timon vertical (z=-14..-10) + plano horizontal =====
        root.addOrReplaceChild("tailFin",
                CubeListBuilder.create().texOffs(60, 48)
                        .addBox(-1.0F, -12.0F, -14.0F, 2.0F, 10.0F, 4.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("tailWing",
                CubeListBuilder.create().texOffs(72, 48)
                        .addBox(-8.0F, -5.0F, -14.0F, 16.0F, 2.0F, 5.0F),
                PartPose.ZERO);

        // ===== HELICE: barra giratoria en la nariz (z=16..18) =====
        root.addOrReplaceChild("propeller",
                CubeListBuilder.create().texOffs(96, 56)
                        .addBox(-5.0F, -10.0F, 16.0F, 10.0F, 1.0F, 2.0F),
                PartPose.ZERO);

        // ===== LLAMA del propulsor: detras de la cola (z=-20..-14) =====
        root.addOrReplaceChild("flame",
                CubeListBuilder.create().texOffs(56, 32)
                        .addBox(-3.0F, -9.0F, -20.0F, 6.0F, 6.0F, 6.0F),
                PartPose.ZERO);

        ModelPart baked = root.bake(128, 64);
        this.fuselage = baked.getChild("fuselage");
        this.nose = baked.getChild("nose");
        this.leftWing = baked.getChild("leftWing");
        this.rightWing = baked.getChild("rightWing");
        this.tailFin = baked.getChild("tailFin");
        this.tailWing = baked.getChild("tailWing");
        this.propeller = baked.getChild("propeller");
        this.flame = baked.getChild("flame");
    }

    @Override
    public void setupAnim(TntRocketEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        boolean flying = entity.isFlying();
        // la llama crece y parpadea al volar
        float flicker = 0.7F + 0.4F * (float) Math.sin(ageInTicks * 1.1F)
                + 0.2F * (float) Math.sin(ageInTicks * 3.1F);
        float flameScale = flying ? 1.2F : 0.4F;
        this.flame.yScale = Math.max(0.35F, flicker) * flameScale;
        this.flame.xScale = 1.0F + (flying ? 0.15F : 0.0F) * (float) Math.sin(ageInTicks * 1.7F);
        this.flame.zScale = 1.0F + (flying ? 0.15F : 0.0F) * (float) Math.cos(ageInTicks * 1.4F);

        // la helice gira rapido al volar
        float propSpin = (float) (Math.PI * 2.0 * (flying ? 2.0 : 0.3)) * (ageInTicks / 20.0F);
        this.propeller.yRot = propSpin;

        // balanceo suave en vuelo (alas que "respiran")
        float rock = flying ? (float) Math.sin(ageInTicks * 0.35F) * 0.03F : 0.0F;
        this.fuselage.zRot = rock;
        this.nose.zRot = rock;
        this.leftWing.zRot = rock;
        this.rightWing.zRot = rock;
        this.tailFin.zRot = rock;
        this.tailWing.zRot = rock;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.fuselage.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.nose.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.leftWing.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.rightWing.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.tailFin.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.tailWing.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.propeller.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.flame.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
