package de.luaxlab.shipping.client.entity.model;// Made with Blockbench 4.1.3
// Exported for Minecraft version 1.17 with Mojang mappings
// Paste this class into your mod and generate all required imports


import de.luaxlab.shipping.common.core.ModCommon;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class FishingBargeModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(ModCommon.identifier("fishingbargemodel"), "main");
	private final ModelPart root, left, right, leftBasket, rightBasket;

	public FishingBargeModel(ModelPart root) {
		this.root = root.getChild("root");
		this.left = root.getChild("left");
		this.leftBasket = left.getChild("leftBasket");
		this.right = root.getChild("right");
		this.rightBasket = right.getChild("rightBasket");
	}

	public static TexturedModelData createBodyLayer() {
		ModelData meshdefinition = new ModelData();
		ModelPartData partdefinition = meshdefinition.getRoot();

		ModelPartData root = partdefinition.addChild("root", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, -28.0F, -7.0F, 12.0F, 5.0F, 14.0F, new Dilation(0.0F))
		.uv(18, 23).cuboid(-8.0F, -30.0F, -7.0F, 2.0F, 4.0F, 14.0F, new Dilation(0.0F))
		.uv(0, 19).cuboid(6.0F, -30.0F, -7.0F, 2.0F, 4.0F, 14.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData left = partdefinition.addChild("left", ModelPartBuilder.create().uv(6, 0).cuboid(-6.0F, -8.0F, -1.0F, 1.0F, 9.0F, 2.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(5.0F, -8.0F, -1.0F, 1.0F, 9.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -5.0F, -4.0F));

		ModelPartData leftBasket = left.addChild("leftBasket", ModelPartBuilder.create().uv(36, 19).cuboid(-5.0F, 1.0F, -4.0F, 10.0F, 4.0F, 7.0F, new Dilation(0.0F))
		.uv(38, 8).cuboid(-5.0F, 1.0F, -1.0F, 1.0F, 4.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -7.0F, 0.0F));

		ModelPartData cube_r1 = leftBasket.addChild("cube_r1", ModelPartBuilder.create().uv(38, 8).cuboid(-0.5F, -2.0F, -1.0F, 1.0F, 4.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(4.5F, 3.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		ModelPartData right = partdefinition.addChild("right", ModelPartBuilder.create().uv(6, 0).cuboid(-6.0F, -8.0F, -1.0F, 1.0F, 9.0F, 2.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(5.0F, -8.0F, -1.0F, 1.0F, 9.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -5.0F, 4.0F));

		ModelPartData rightBasket = right.addChild("rightBasket", ModelPartBuilder.create().uv(36, 19).cuboid(-5.0F, 1.0F, -3.0F, 10.0F, 4.0F, 7.0F, new Dilation(0.0F))
		.uv(38, 8).cuboid(-5.0F, 1.0F, -1.0F, 1.0F, 4.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -7.0F, 0.0F));

		ModelPartData cube_r2 = rightBasket.addChild("cube_r2", ModelPartBuilder.create().uv(38, 8).cuboid(-0.5F, -2.0F, -1.0F, 1.0F, 4.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(4.5F, 3.0F, 0.0F, 0.0F, -3.1416F, 0.0F));

		return TexturedModelData.of(meshdefinition, 128, 128);
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		//TODO
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		root.render(matrices, vertices, light, overlay);
		left.render(matrices, vertices, light, overlay);
		right.render(matrices, vertices, light, overlay);
	}
}