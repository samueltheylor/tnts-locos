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
 * Bola negra del Agujero Negro: un cubo 16x16x16 con la textura de esfera
 * oscura pintada en las 4 caras laterales (misma textura 64x32 que Bedrock).
 */
public class BlackHoleModel extends Model {

    private final ModelPart ball;

    public BlackHoleModel() {
        super(RenderType::entityTranslucent);
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("ball",
                CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.ZERO);
        this.ball = root.bake(64, 32).getChild("ball");
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.ball.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
