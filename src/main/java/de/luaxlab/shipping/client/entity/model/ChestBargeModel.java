package de.luaxlab.shipping.client.entity.model;// Made with Blockbench 4.1.4
// Exported for Minecraft version 1.17 with Mojang mappings
// Paste this class into your mod and generate all required imports


import de.luaxlab.shipping.common.core.ModCommon;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.CreeperEntityRenderer;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class ChestBargeModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(ModCommon.identifier("chestbargemodel"), "main");
	private final ModelPart bb_main;

	public ChestBargeModel(ModelPart root) {
		this.bb_main = root.getChild("bb_main");
	}

	public static TexturedModelData createBodyLayer() {
		ModelData meshdefinition = new ModelData();
		ModelPartData ModelPartData = meshdefinition.getRoot();

		ModelPartData bb_main = ModelPartData.addChild("bb_main", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, -27.0F, -7.0F, 12.0F, 5.0F, 14.0F, new Dilation(0.0F))
		.uv(38, 5).cuboid(-8.0F, -29.0F, -7.0F, 2.0F, 4.0F, 14.0F, new Dilation(0.0F))
		.uv(28, 43).cuboid(-6.0F, -29.0F, -9.0F, 12.0F, 4.0F, 2.0F, new Dilation(0.0F))
		.uv(26, 25).cuboid(6.0F, -29.0F, -7.0F, 2.0F, 4.0F, 14.0F, new Dilation(0.0F))
		.uv(0, 41).cuboid(-6.0F, -29.0F, 7.0F, 12.0F, 4.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 23.0F, 0.0F));

		ModelPartData cube_r1 = bb_main.addChild("cube_r1", ModelPartBuilder.create().uv(0, 19).cuboid(-5.0F, -35.0F, -5.0F, 10.0F, 10.0F, 10.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		return TexturedModelData.of(meshdefinition, 128, 128);
	}

	@Override
	public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bb_main.render(matrices, vertices, packedLight, packedOverlay);
	}
}