package com.tnts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

/**
 * Agujero Negro con NUCLEO REAL: un disco negro plano (el portal) con dos
 * anillos de acrecion formados por barras — el exterior morado y el interior
 * rosa/blanco — que giran en direcciones opuestas. Durante el colapso final
 * los anillos se comprimen contra el disco y desaparecen.
 */
public class BlackHoleModel extends Model {

    private final ModelPart core;
    private final ModelPart ring;
    private final ModelPart ring2;

    public BlackHoleModel() {
        super(RenderType::entityTranslucent);
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Disco negro (nucleo / portal): 14x1x14, plano
        root.addOrReplaceChild("core",
                CubeListBuilder.create().texOffs(0, 16).addBox(-7.0F, -0.5F, -7.0F, 14.0F, 1.0F, 14.0F),
                PartPose.ZERO);

        // Anillo de acrecion exterior (morado): marco cuadrado de 4 barras
        CubeListBuilder ringBars = CubeListBuilder.create()
                .texOffs(0, 0).addBox(-9.0F, -1.0F, 8.0F, 18.0F, 2.0F, 2.0F)
                .texOffs(0, 0).addBox(-9.0F, -1.0F, -10.0F, 18.0F, 2.0F, 2.0F)
                .texOffs(0, 0).addBox(-10.0F, -1.0F, -9.0F, 2.0F, 2.0F, 18.0F)
                .texOffs(0, 0).addBox(8.0F, -1.0F, -9.0F, 2.0F, 2.0F, 18.0F);
        root.addOrReplaceChild("ring", ringBars, PartPose.ZERO);

        // Anillo interior (rosa/blanco, cerca del nucleo): marco mas pequeno
        CubeListBuilder ring2Bars = CubeListBuilder.create()
                .texOffs(32, 0).addBox(-5.5F, -0.7F, 4.9F, 11.0F, 1.4F, 1.2F)
                .texOffs(32, 0).addBox(-5.5F, -0.7F, -6.1F, 11.0F, 1.4F, 1.2F)
                .texOffs(32, 0).addBox(-6.1F, -0.7F, -5.5F, 1.2F, 1.4F, 11.0F)
                .texOffs(32, 0).addBox(4.9F, -0.7F, -5.5F, 1.2F, 1.4F, 11.0F);
        root.addOrReplaceChild("ring2", ring2Bars, PartPose.ZERO);

        ModelPart baked = root.bake(64, 32);
        this.core = baked.getChild("core");
        this.ring = baked.getChild("ring");
        this.ring2 = baked.getChild("ring2");
    }

    /** Anima el agujero: pulso, rotaciones opuestas y compresion final. */
    public void animate(float age, float partialTick, int lifetime) {
        float t = age + partialTick;
        float progress = Math.min(1.0F, t / lifetime);
        // colapso en los ultimos 1/8 de vida: 0..1
        float collapse = Math.max(0.0F, progress - 0.875F) * 8.0F;

        // pulso global
        float pulse = 1.0F + 0.06F * (float) Math.sin(t * 0.22F);

        // nucleo: rota lento, crece con el tiempo y se encoge en el colapso
        core.yRot = t * 0.08F;
        float coreScale = (0.9F + progress * 0.7F) * pulse;
        if (collapse > 0.0F) coreScale *= 1.0F - collapse * 0.35F;
        core.xScale = coreScale;
        core.zScale = coreScale;
        core.yScale = 0.3F;

        // anillo exterior: rota rapido en direccion opuesta, crece y se comprime
        ring.yRot = -t * 0.45F;
        float ringScale = (0.8F + progress * 0.8F) * pulse;
        if (collapse > 0.0F) ringScale *= 1.0F - collapse * 0.85F;
        ring.xScale = ringScale;
        ring.zScale = ringScale;
        ring.yScale = 1.0F - collapse * 0.85F;   // se aplasta contra el disco

        // anillo interior: rota mas rapido, misma compresion
        ring2.yRot = t * 0.65F;
        float ring2Scale = (0.9F + progress * 0.6F) * pulse;
        if (collapse > 0.0F) ring2Scale *= 1.0F - collapse * 0.8F;
        ring2.xScale = ring2Scale;
        ring2.zScale = ring2Scale;
        ring2.yScale = 1.0F - collapse * 0.8F;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.core.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.ring.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.ring2.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
