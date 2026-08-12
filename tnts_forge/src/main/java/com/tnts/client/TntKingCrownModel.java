package com.tnts.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

/**
 * Modelo 3D de la Corona del Rey TNT para el jugador: una banda dorada con
 * gema roja y 5 puntas (4 en las esquinas + 1 central mas alta). Se monta
 * sobre la cabeza del jugador sustituyendo al casco de armadura.
 * <p>
 * UVs: banda 10x2x10 @ texOffs(0,0), puntas de esquina 2x4x2 @ texOffs(40,0)
 * y punta central 2x6x2 @ texOffs(48,0) — coincide con
 * tnt_king_crown_layer_1.png (64x32).
 */
public class TntKingCrownModel extends HumanoidModel<LivingEntity> {

    /** Instancia unica (el modelo es stateless). */
    public static final TntKingCrownModel INSTANCE = new TntKingCrownModel(createMesh());

    private TntKingCrownModel(ModelPart root) {
        super(root);
        // solo se renderiza la corona; el resto de partes queda oculto
        this.body.visible = false;
        this.rightArm.visible = false;
        this.leftArm.visible = false;
        this.rightLeg.visible = false;
        this.leftLeg.visible = false;
        this.hat.visible = false;
    }

    /** Construye el ModelPart de la corona (64x32), estilo 1.20.1. */
    private static ModelPart createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // HumanoidModel requiere TODOS estos parts en el root (su constructor
        // hace root.getChild("hat"), ...). Los demas quedan vacios y ocultos
        // en el constructor — solo se renderiza la corona sobre la cabeza.
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        // banda: 10x2x10 alrededor de la cabeza (la cabeza mide 8x8x8)
        head.addOrReplaceChild("band",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-5.0F, -8.0F, -5.0F, 10.0F, 2.0F, 10.0F),
                PartPose.ZERO);

        // 4 puntas de las esquinas: 2x4x2 @ (40,0)
        float[][] corners = {{-4.0F, -4.0F}, {4.0F, -4.0F}, {-4.0F, 4.0F}, {4.0F, 4.0F}};
        for (int i = 0; i < corners.length; i++) {
            head.addOrReplaceChild("spike" + (i + 1),
                    CubeListBuilder.create().texOffs(40, 0)
                            .addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                    PartPose.offset(corners[i][0], -8.0F, corners[i][1]));
        }

        // punta central mas alta: 2x6x2 @ (48,0)
        head.addOrReplaceChild("spike5",
                CubeListBuilder.create().texOffs(48, 0)
                        .addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offset(0.0F, -8.0F, 0.0F));

        return root.bake(64, 32);
    }
}
