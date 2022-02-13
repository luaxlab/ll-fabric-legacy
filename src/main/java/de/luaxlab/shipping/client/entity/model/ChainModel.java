package de.luaxlab.shipping.client.entity.model;// Made with Blockbench 4.0.5
// Exported for Minecraft version 1.15 - 1.16 with Mojang mappings
// Paste this class into your mod and generate all required imports

import de.luaxlab.shipping.common.core.ModCommon;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class ChainModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(ModCommon.identifier("chainmodel"), "main");
	private final ModelPart bb_main;

	public ChainModel(ModelPart root) {
		this.bb_main = root.getChild("bb_main");
	}

	public static TexturedModelData createBodyLayer() {
		ModelData meshdefinition = new ModelData();
		ModelPartData partdefinition = meshdefinition.getRoot();

		ModelPartData bb_main = partdefinition.addChild("bb_main", ModelPartBuilder.create(), ModelTransform.pivot(-13.0F, 24.0F, 0.0F));

		ModelPartData cube_r1 = bb_main.addChild("cube_r1", ModelPartBuilder.create(), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		ModelPartData bone2 = cube_r1.addChild("bone2", ModelPartBuilder.create().uv(0, 2).cuboid(1.0F, -25.0F, -11.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
				.uv(0, 0).cuboid(0.0F, -25.0F, -13.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
				.uv(3, 8).cuboid(0.0F, -26.0F, -13.0F, 2.0F, 1.0F, 4.0F, new Dilation(0.0F))
				.uv(4, 4).cuboid(0.0F, -24.0F, -13.0F, 2.0F, 1.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(-1.0F, 0.0F, 1.0F));

		return TexturedModelData.of(meshdefinition, 16, 16);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		bb_main.render(matrices, vertices, light, overlay);
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

	}

}