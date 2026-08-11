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
 * Modelo del Rey TNT (v3): cubo gigante con cara enfadada, corona dorada
 * con 5 puntas, brazos, cejas furiosas, mecha con llama y remaches.
 *
 * NOTA: todos los offsets de hijos usan posiciones ABSOLUTAS desde root
 * (nada de offsets relativos a padres desplazados).
 */
public class TntKingModel extends EntityModel<TntKingEntity> {

    private final ModelPart body;
    private final ModelPart crownBase;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftBrow;
    private final ModelPart rightBrow;
    private final ModelPart fuse;
    private final ModelPart flame;
    private final ModelPart[] spikes = new ModelPart[5];

    /** Parpadeo blanco de la derrota (se lee en renderToBuffer). */
    private boolean deathFlash = false;

    public TntKingModel() {
        super(RenderType::entityCutoutNoCull);
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ===== CUERPO: cubo 16x16x16 =====
        // addBox(-8, -16, -8) → cubre desde y=-16 hasta y=0
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.ZERO);

        // ===== CORONA BASE: cubo 12x4x12 sobre el cuerpo =====
        // cubre desde y=-20 hasta y=-16 (justo encima del cuerpo)
        root.addOrReplaceChild("crownBase",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-6.0F, -20.0F, -6.0F, 12.0F, 4.0F, 12.0F),
                PartPose.ZERO);

        // ===== PUNTAS DE LA CORONA: hijos de root (no de crown) =====
        // crecen desde y=-16 (tope de la corona base) hacia arriba
        // Las 4 esquinas: 2x6x2
        float[][] spikePos = {{-4.5F, -4.5F}, {4.5F, -4.5F}, {-4.5F, 4.5F}, {4.5F, 4.5F}};
        int[] spikeTex = {48, 56, 64, 72};
        for (int i = 0; i < 4; i++) {
            root.addOrReplaceChild("spike" + (i + 1),
                    CubeListBuilder.create().texOffs(spikeTex[i], 32)
                            .addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                    PartPose.offset(spikePos[i][0], -16.0F, spikePos[i][1]));
        }
        // Punta central: más alta (2x8x2)
        root.addOrReplaceChild("spike5",
                CubeListBuilder.create().texOffs(80, 32)
                        .addBox(-1.0F, -8.0F, -1.0F, 2.0F, 8.0F, 2.0F),
                PartPose.offset(0.0F, -16.0F, 0.0F));

        // ===== BRAZOS: 4x12x4 colgando de los hombros =====
        // pivote en el hombro (y=-14, arriba del cuerpo) y cuelgan hasta y=-2
        root.addOrReplaceChild("leftArm",
                CubeListBuilder.create().texOffs(0, 48)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-10.0F, -14.0F, 0.0F));
        root.addOrReplaceChild("rightArm",
                CubeListBuilder.create().texOffs(16, 48)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(10.0F, -14.0F, 0.0F));

        // ===== CEJAS FURIOSAS: encima de los ojos, en la cara frontal (z-) =====
        root.addOrReplaceChild("leftBrow",
                CubeListBuilder.create().texOffs(52, 48)
                        .addBox(-3.0F, -1.0F, -0.5F, 6.0F, 2.0F, 1.0F),
                PartPose.offset(-3.5F, -10.0F, -8.5F));
        root.addOrReplaceChild("rightBrow",
                CubeListBuilder.create().texOffs(76, 48)
                        .addBox(-3.0F, -1.0F, -0.5F, 6.0F, 2.0F, 1.0F),
                PartPose.offset(3.5F, -10.0F, -8.5F));

        // ===== MECHA: 2x3x2 sobre el cuerpo =====
        root.addOrReplaceChild("fuse",
                CubeListBuilder.create().texOffs(32, 48)
                        .addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, -16.0F, 3.0F));

        // ===== LLAMA: 3x4x3 encima de la mecha =====
        root.addOrReplaceChild("flame",
                CubeListBuilder.create().texOffs(40, 48)
                        .addBox(-1.5F, -4.0F, -1.5F, 3.0F, 4.0F, 3.0F),
                PartPose.offset(0.0F, -18.5F, 3.0F));

        ModelPart baked = root.bake(128, 64);
        this.body = baked.getChild("body");
        this.crownBase = baked.getChild("crownBase");
        this.leftArm = baked.getChild("leftArm");
        this.rightArm = baked.getChild("rightArm");
        this.leftBrow = baked.getChild("leftBrow");
        this.rightBrow = baked.getChild("rightBrow");
        this.fuse = baked.getChild("fuse");
        this.flame = baked.getChild("flame");
        for (int i = 0; i < 5; i++) {
            this.spikes[i] = baked.getChild("spike" + (i + 1));
        }
    }

    /** Escala total del modelo: "pop" elástico al convocarse. */
    public float getModelScale(TntKingEntity entity, float partialTick) {
        int age = entity.tickCount;
        if (age < 24) {
            float t = Math.min(1.0F, (age + partialTick) / 16.0F);
            float ease = 1.0F + 2.5F * (t - 1.0F) * (t - 1.0F) * (t - 1.0F)
                    + 1.5F * (t - 1.0F) * (t - 1.0F);
            return Math.max(0.05F, ease);
        }
        return 1.0F;
    }

    @Override
    public void setupAnim(TntKingEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        boolean enraged = entity.isEnraged();
        boolean charging = entity.isCharging();
        boolean dashing = entity.isDashing();
        boolean stunned = entity.isStunned();
        boolean dying = entity.isDying();
        int age = entity.tickCount;

        float spawnScale = getModelScale(entity, 0.0F);
        float spawnBoost = Math.max(0.0F, 1.0F - spawnScale) * 1.8F;
        float flameBoost = 1.0F + (age < 24 ? spawnBoost : 0.0F);

        // pulsacion
        float pulseRate = enraged ? 0.55F : 0.22F;
        float pulse = 1.0F + (enraged ? 0.05F : 0.035F) * (float) Math.sin(ageInTicks * pulseRate);

        // orientacion
        float yaw = netHeadYaw * (float) (Math.PI / 180.0) * 0.45F;
        float pitch = headPitch * (float) (Math.PI / 180.0) * 0.2F;

        // ---- cuerpo ----
        if (dying) {
            this.deathFlash = entity.isDeathFlashOn();
            float shake = 0.14F + entity.deathTime * 0.008F;
            this.body.xRot = (float) Math.sin(age * 1.4F) * shake + 0.3F;
            this.body.zRot = (float) Math.cos(age * 1.1F) * shake * 0.7F;
            this.body.yRot = yaw;
            this.body.xScale = 1.0F;
            this.body.yScale = 1.0F;
            this.body.zScale = 1.0F;
        } else {
            this.deathFlash = false;
            this.body.xScale = pulse * spawnScale;
            this.body.yScale = (2.0F - pulse) * spawnScale * (dashing ? 0.88F : 1.0F);
            this.body.zScale = pulse * spawnScale * (dashing ? 1.18F : 1.0F);
            this.body.yRot = yaw;
            this.body.xRot = pitch + (dashing ? 0.35F : 0.0F);
            if (charging) {
                this.body.xRot += (float) Math.sin(age * 0.9F) * 0.06F;
                this.body.zRot = (float) Math.sin(age * 1.3F) * 0.05F;
            } else if (stunned) {
                this.body.zRot = (float) Math.sin(ageInTicks * 0.5F) * 0.04F;
            } else {
                this.body.zRot = 0;
            }
        }

        // ---- corona: sigue al cuerpo ----
        float crownScale = dying ? 1.0F : pulse * spawnScale;
        this.crownBase.xRot = this.body.xRot;
        this.crownBase.yRot = this.body.yRot;
        this.crownBase.xScale = crownScale;
        this.crownBase.yScale = dying ? 1.0F : spawnScale;
        this.crownBase.zScale = crownScale;

        // ---- puntas: siguen al cuerpo ----
        for (ModelPart spike : spikes) {
            spike.xRot = this.body.xRot;
            spike.yRot = this.body.yRot;
            spike.xScale = dying ? 1.0F : pulse * spawnScale;
            spike.yScale = dying ? 1.0F : spawnScale;
            spike.zScale = dying ? 1.0F : pulse * spawnScale;
        }

        // ---- brazos ----
        float armSwing = dying ? 0 : (float) Math.sin(ageInTicks * 0.16F) * 0.18F;
        this.leftArm.yRot = yaw + armSwing;
        this.rightArm.yRot = yaw - armSwing;
        this.leftArm.zRot = -0.12F;
        this.rightArm.zRot = 0.12F;
        if (dying) {
            this.leftArm.xRot = 1.4F + (float) Math.sin(age * 0.9F) * 0.2F;
            this.rightArm.xRot = 1.4F - (float) Math.sin(age * 0.9F) * 0.2F;
            this.leftArm.zRot = -0.3F;
            this.rightArm.zRot = 0.3F;
        } else if (enraged) {
            this.leftArm.xRot = -2.4F;
            this.rightArm.xRot = -2.4F;
            this.leftArm.zRot = -0.35F;
            this.rightArm.zRot = 0.35F;
        } else if (charging) {
            this.leftArm.xRot = -1.2F + (float) Math.sin(age * 0.8F) * 0.15F;
            this.rightArm.xRot = -1.2F - (float) Math.sin(age * 0.8F) * 0.15F;
        } else if (dashing) {
            this.leftArm.xRot = -1.8F;
            this.rightArm.xRot = -1.8F;
        } else if (stunned) {
            this.leftArm.xRot = 1.2F;
            this.rightArm.xRot = 1.2F;
        } else {
            this.leftArm.xRot = 0.12F;
            this.rightArm.xRot = 0.12F;
        }

        // ---- cejas ----
        float browTilt = enraged ? 0.5F : 0.12F;
        this.leftBrow.xRot = pitch;
        this.rightBrow.xRot = pitch;
        this.leftBrow.yRot = yaw;
        this.rightBrow.yRot = yaw;
        this.leftBrow.zRot = browTilt;
        this.rightBrow.zRot = -browTilt;
        this.leftBrow.y = -10.0F + (enraged ? 0.5F : 0.0F);
        this.rightBrow.y = -10.0F + (enraged ? 0.5F : 0.0F);

        // ---- mecha y llama ----
        this.fuse.xRot = dying ? 0.3F : this.body.xRot;
        this.fuse.yRot = yaw;
        float flicker = 0.8F + 0.4F * (float) Math.sin(ageInTicks * 0.9F)
                + 0.2F * (float) Math.sin(ageInTicks * 2.7F);
        this.flame.xRot = dying ? 0.3F : this.body.xRot;
        this.flame.yRot = yaw;
        this.flame.yScale = Math.max(0.4F, flicker) * flameBoost * (charging ? 1.5F : 1.0F);
        this.flame.xScale = 1.0F + 0.15F * (float) Math.sin(ageInTicks * 1.3F);
        this.flame.zScale = 1.0F + 0.15F * (float) Math.cos(ageInTicks * 1.1F);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        if (this.deathFlash) {
            red = 1.0F;
            green = 1.0F;
            blue = 1.0F;
        }
        this.body.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.crownBase.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        for (ModelPart spike : spikes) {
            spike.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
        this.leftArm.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.rightArm.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.leftBrow.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.rightBrow.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.fuse.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.flame.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
