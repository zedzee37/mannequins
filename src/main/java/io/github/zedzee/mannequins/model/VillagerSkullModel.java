package io.github.zedzee.mannequins.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.jetbrains.annotations.NotNull;

public class VillagerSkullModel extends SkullModelBase {
    private final ModelPart head;
    private final ModelPart nose;

    public VillagerSkullModel(ModelPart modelPart) {
        this.head = modelPart.getChild("head");
        this.nose = head.getChild("nose");
    }

    public static MeshDefinition createHeadModel() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition head = partDefinition.addOrReplaceChild(
                "head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F,
                        -10.0F,
                        -4.0F,
                        8.0F,
                        10.0F,
                        8.0F), PartPose.ZERO
        );
        head.addOrReplaceChild(
                "nose", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F,
                        -1.0F,
                        -6.0F,
                        2.0F,
                        4.0F,
                        2.0F), PartPose.offset(0.0F, -2.0F, 0.0F)
        );

        return meshDefinition;
    }

    @Override
    public void setupAnim(float mouthAnimation, float yRot, float xRot) {
        this.head.yRot = yRot * (float) (Math.PI / 180.0);
        this.head.xRot = xRot * (float) (Math.PI / 180.0);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack,
                               @NotNull VertexConsumer buffer,
                               int packedLight,
                               int packedOverlay,
                               int color) {
        head.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
