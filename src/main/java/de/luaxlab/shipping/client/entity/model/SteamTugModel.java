package de.luaxlab.shipping.client.entity.model;// Made with Blockbench 4.1.4
// Exported for Minecraft version 1.17 with Mojang mappings
// Paste this class into your mod and generate all required imports


import de.luaxlab.shipping.common.core.ModCommon;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class SteamTugModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(ModCommon.identifier("steamtugmodel"), "main");
	private final ModelPart bb_main;

	public SteamTugModel(ModelPart root) {
		this.bb_main = root.getChild("bb_main");
	}

	public static TexturedModelData createBodyLayer() {
		ModelData meshdefinition = new ModelData();
		ModelPartData partdefinition = meshdefinition.getRoot();

		ModelPartData bb_main = partdefinition.addChild("bb_main", ModelPartBuilder.create().uv(0, 0).cuboid(-7.0F, -10.0F, -19.0F, 14.0F, 6.0F, 24.0F, new Dilation(0.0F))
		.uv(0, 50).cuboid(-9.0F, -12.0F, -19.0F, 2.0F, 4.0F, 24.0F, new Dilation(0.0F))
		.uv(44, 30).cuboid(7.0F, -12.0F, -19.0F, 2.0F, 4.0F, 24.0F, new Dilation(0.0F))
		.uv(60, 66).cuboid(-7.0F, -12.0F, 5.0F, 14.0F, 4.0F, 2.0F, new Dilation(0.0F))
		.uv(28, 66).cuboid(-7.0F, -12.0F, -21.0F, 14.0F, 4.0F, 2.0F, new Dilation(0.0F))
		.uv(53, 0).cuboid(-6.0F, -18.0F, -13.0F, 12.0F, 8.0F, 14.0F, new Dilation(0.0F))
		.uv(0, 30).cuboid(-8.0F, -20.0F, -15.0F, 16.0F, 2.0F, 18.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-2.0F, -26.0F, -5.0F, 4.0F, 6.0F, 4.0F, new Dilation(0.0F))
		.uv(0, 10).cuboid(-2.0F, -25.25F, -5.0F, 4.0F, 2.0F, 4.0F, new Dilation(0.5F))
		.uv(28, 58).cuboid(-9.0F, -11.0F, -21.0F, 18.0F, 2.0F, 6.0F, new Dilation(0.25F))
		.uv(0, 0).cuboid(-2.0F, -26.0F, -11.0F, 4.0F, 6.0F, 4.0F, new Dilation(0.0F))
		.uv(0, 10).cuboid(-2.0F, -25.25F, -11.0F, 4.0F, 2.0F, 4.0F, new Dilation(0.5F))
		.uv(6, 5).cuboid(-1.0F, -14.0F, 7.0F, 2.0F, 3.0F, 1.0F, new Dilation(0.0F))
		.uv(2, 5).cuboid(-1.0F, -14.0F, 6.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		return TexturedModelData.of(meshdefinition, 128, 128);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		bb_main.render(matrices, vertices, light, overlay);
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

	}
}