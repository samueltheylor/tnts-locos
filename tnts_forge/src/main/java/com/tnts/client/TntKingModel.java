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
 * Modelo del Rey TNT (v2): un cubo gigante de TNT con MUCHOS detalles — corona
 * dorada con 5 puntas, brazos que se balancean, cejas 3D furiosas, mecha con
 * llama parpadeante y remaches. Con animaciones completas:
 * <ul>
 *   <li><b>Entrada:</b> al convocarse hace un "pop" elastico (escala 0 -> 1.12 -> 1)
 *       mientras la llama de la mecha se enciende grande.</li>
 *   <li><b>Idle:</b> late como una TNT a punto de explotar, llama que parpadea,
 *       brazos balanceandose.</li>
 *   <li><b>Furia (&lt;50% HP):</b> late mas rapido, cejas inclinadas, brazos arriba.</li>
 *   <li><b>Carga (embestida):</b> vibra con polvo rojo acumulandose.</li>
 *   <li><b>Dash:</b> se estira y se inclina hacia adelante.</li>
 *   <li><b>Aturdido:</b> brazos caidos, parpadea en dorado.</li>
 * </ul>
 */
public class TntKingModel extends EntityModel<TntKingEntity> {

    private final ModelPart body;
    private final ModelPart crown;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftBrow;
    private final ModelPart rightBrow;
    private final ModelPart fuse;
    private final ModelPart flame;

    /** Parpadeo blanco de la derrota (se lee en renderToBuffer). */
    private boolean deathFlash = false;

    public TntKingModel() {
        super(RenderType::entityCutoutNoCull);
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // cuerpo: cubo de TNT 16x16x16 (la textura lleva la cara enfadada)
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.ZERO);

        // corona: base dorada + 5 puntas
        PartDefinition crownDef = root.addOrReplaceChild("crown",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-6.0F, -20.0F, -6.0F, 12.0F, 4.0F, 12.0F),
                PartPose.ZERO);
        crownDef.addOrReplaceChild("spike1",
                CubeListBuilder.create().texOffs(48, 32).addBox(-1.0F, -25.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offset(-4.5F, -20.0F, -4.5F));
        crownDef.addOrReplaceChild("spike2",
                CubeListBuilder.create().texOffs(56, 32).addBox(-1.0F, -25.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offset(4.5F, -20.0F, -4.5F));
        crownDef.addOrReplaceChild("spike3",
                CubeListBuilder.create().texOffs(64, 32).addBox(-1.0F, -25.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offset(-4.5F, -20.0F, 4.5F));
        crownDef.addOrReplaceChild("spike4",
                CubeListBuilder.create().texOffs(72, 32).addBox(-1.0F, -25.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offset(4.5F, -20.0F, 4.5F));
        crownDef.addOrReplaceChild("spike5",
                CubeListBuilder.create().texOffs(80, 32).addBox(-1.0F, -27.0F, -1.0F, 2.0F, 8.0F, 2.0F),
                PartPose.offset(0.0F, -20.0F, 0.0F));

        // brazos: cuelgan a los lados del cubo
        root.addOrReplaceChild("leftArm",
                CubeListBuilder.create().texOffs(0, 48)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-10.0F, -14.0F, 0.0F));
        root.addOrReplaceChild("rightArm",
                CubeListBuilder.create().texOffs(16, 48)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(10.0F, -14.0F, 0.0F));

        // cejas 3D furiosas (se inclinan en modo furia)
        root.addOrReplaceChild("leftBrow",
                CubeListBuilder.create().texOffs(52, 48)
                        .addBox(-3.0F, -1.0F, -0.5F, 6.0F, 2.0F, 1.0F),
                PartPose.offset(-3.5F, -8.2F, -8.2F));
        root.addOrReplaceChild("rightBrow",
                CubeListBuilder.create().texOffs(76, 48)
                        .addBox(-3.0F, -1.0F, -0.5F, 6.0F, 2.0F, 1.0F),
                PartPose.offset(3.5F, -8.2F, -8.2F));

        // mecha con llama en la parte superior
        root.addOrReplaceChild("fuse",
                CubeListBuilder.create().texOffs(32, 48)
                        .addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, -16.0F, 0.0F));
        root.addOrReplaceChild("flame",
                CubeListBuilder.create().texOffs(40, 48)
                        .addBox(-1.5F, -4.0F, -1.5F, 3.0F, 4.0F, 3.0F),
                PartPose.offset(0.0F, -18.5F, 0.0F));

        ModelPart baked = root.bake(128, 64);
        this.body = baked.getChild("body");
        this.crown = baked.getChild("crown");
        this.leftArm = baked.getChild("leftArm");
        this.rightArm = baked.getChild("rightArm");
        this.leftBrow = baked.getChild("leftBrow");
        this.rightBrow = baked.getChild("rightBrow");
        this.fuse = baked.getChild("fuse");
        this.flame = baked.getChild("flame");
    }

    /** Escala total del modelo (1.0 = tamaño vanilla; el renderer la multiplica). */
    public float getModelScale(TntKingEntity entity, float partialTick) {
        // animacion de entrada: "pop" elastico al ser convocado (primeros ~1.2s)
        int age = entity.tickCount;
        if (age < 24) {
            float t = Math.min(1.0F, (age + partialTick) / 16.0F);
            float ease = 1.0F + 2.5F * (t - 1.0F) * (t - 1.0F) * (t - 1.0F)
                    + 1.5F * (t - 1.0F) * (t - 1.0F); // overshoot elastico
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

        // ---- entrada: "pop" + llama grande al aparecer ----
        float spawnScale = getModelScale(entity, 0.0F);
        float spawnBoost = Math.max(0.0F, 1.0F - spawnScale) * 1.8F; // llama enorme al entrar
        float flameBoost = 1.0F + (age < 24 ? spawnBoost : 0.0F);

        // ---- pulsacion: normal lenta, furia rapida ----
        float pulseRate = enraged ? 0.55F : 0.22F;
        float pulse = 1.0F + (enraged ? 0.05F : 0.035F) * (float) Math.sin(ageInTicks * pulseRate);

        // ---- orientacion hacia el objetivo ----
        float yaw = netHeadYaw * (float) (Math.PI / 180.0) * 0.45F;
        float pitch = headPitch * (float) (Math.PI / 180.0) * 0.2F;

        // ---- cuerpo ----
        this.body.xScale = pulse * spawnScale;
        this.body.yScale = (2.0F - pulse) * spawnScale * (dashing ? 0.88F : 1.0F);
        this.body.zScale = pulse * spawnScale * (dashing ? 1.18F : 1.0F);
        this.body.yRot = yaw;
        this.body.xRot = pitch + (dashing ? 0.35F : 0.0F);
        if (dying) {
            // derrota: se agrieta — temblor violento + inclinacion cada vez mayor
            this.deathFlash = entity.isDeathFlashOn();
            float shake = 0.14F + entity.deathTime * 0.008F;
            this.body.xRot = (float) Math.sin(age * 1.4F) * shake + 0.3F;
            this.body.zRot = (float) Math.cos(age * 1.1F) * shake * 0.7F;
            this.body.yRot = yaw;
        } else {
            this.deathFlash = false;
            if (charging) {
                // vibra violentamente mientras carga la embestida
                this.body.xRot += (float) Math.sin(age * 0.9F) * 0.06F;
                this.body.zRot = (float) Math.sin(age * 1.3F) * 0.05F;
            }
            if (stunned) {
                this.body.zRot = (float) Math.sin(ageInTicks * 0.5F) * 0.04F;
            }
        }

        // ---- corona: sigue al cuerpo, late con el (sin rotacion extra) ----
        this.crown.xRot = this.body.xRot;
        this.crown.yRot = this.body.yRot;
        this.crown.xScale = pulse * spawnScale;
        this.crown.yScale = spawnScale;
        this.crown.zScale = pulse * spawnScale;

        // ---- brazos: balanceo idle; furia = arriba; stun = caidos ----
        float armSwing = (float) Math.sin(ageInTicks * 0.16F) * 0.18F;
        this.leftArm.yRot = yaw + armSwing;
        this.rightArm.yRot = yaw - armSwing;
        this.leftArm.zRot = -0.12F;
        this.rightArm.zRot = 0.12F;
        if (dying) {
            // derrota: brazos caidos colgando
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

        // ---- cejas: furia = inclinadas hacia adentro (mas enfadado) ----
        float browTilt = enraged ? 0.5F : 0.12F;
        this.leftBrow.xRot = pitch;
        this.rightBrow.xRot = pitch;
        this.leftBrow.yRot = yaw;
        this.rightBrow.yRot = yaw;
        this.leftBrow.zRot = browTilt;
        this.rightBrow.zRot = -browTilt;
        this.leftBrow.y = -8.2F + (enraged ? 0.3F : 0.0F);
        this.rightBrow.y = -8.2F + (enraged ? 0.3F : 0.0F);

        // ---- mecha y llama parpadeante ----
        this.fuse.xRot = this.body.xRot;
        this.fuse.yRot = this.body.yRot;
        float flicker = 0.8F + 0.4F * (float) Math.sin(ageInTicks * 0.9F)
                + 0.2F * (float) Math.sin(ageInTicks * 2.7F);
        this.flame.xRot = this.body.xRot;
        this.flame.yRot = this.body.yRot;
        this.flame.yScale = Math.max(0.4F, flicker) * flameBoost * (charging ? 1.5F : 1.0F);
        this.flame.xScale = 1.0F + 0.15F * (float) Math.sin(ageInTicks * 1.3F);
        this.flame.zScale = 1.0F + 0.15F * (float) Math.cos(ageInTicks * 1.1F);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        // parpadeo de la derrota: todo el modelo se pinta blanco
        if (this.deathFlash) {
            red = 1.0F;
            green = 1.0F;
            blue = 1.0F;
        }
        this.body.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.crown.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.leftArm.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.rightArm.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.leftBrow.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.rightBrow.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.fuse.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.flame.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
