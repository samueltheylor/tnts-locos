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
 * Cubo 3D generico con una textura de 64x32 (4 caras laterales). Se usa para
 * los efectos 3D del Meteorito, la Nube de Tormenta y la Supernova.
 */
public class CubeModel extends Model {

    private final ModelPart cube;

    public CubeModel(boolean translucent) {
        super(translucent ? RenderType::entityTranslucent : RenderType::entityCutout);
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("cube",
                CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.ZERO);
        this.cube = root.bake(64, 32).getChild("cube");
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.cube.render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
