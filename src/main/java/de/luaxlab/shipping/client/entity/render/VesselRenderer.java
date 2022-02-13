package de.luaxlab.shipping.client.entity.render;

import de.luaxlab.shipping.client.entity.model.ChainModel;
import de.luaxlab.shipping.common.core.ModCommon;
import de.luaxlab.shipping.common.entity.vehicle.VesselEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.LightType;

public abstract class VesselRenderer<T extends VesselEntity> extends EntityRenderer<T> {


    private static final Identifier CHAIN_TEXTURE =
            new Identifier(ModCommon.MODID, "textures/entity/chain.png");

    private final ChainModel chainModel;




    public VesselRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.chainModel = new ChainModel(context.getPart(ChainModel.LAYER_LOCATION));
    }

    @Override
    public void render(T vesselEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider buffer, int light) {
        matrixStack.push();
        matrixStack.translate(0.0D, getModelYoffset(), 0.0D);
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0F - yaw));
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(getModelYrot()));
        renderModel(vesselEntity, matrixStack, buffer, light);
        getAndRenderChain(vesselEntity, matrixStack, buffer, light);
        matrixStack.pop();

        getAndRenderLeash(vesselEntity, yaw, tickDelta, matrixStack, buffer, light);

    }

    private void renderModel(T vesselEntity, MatrixStack matrixStack, VertexConsumerProvider buffer, int light) {
        VertexConsumer ivertexbuilder = buffer.getBuffer(getModel(vesselEntity).getLayer(this.getTexture(vesselEntity)));
        getModel(vesselEntity).render(matrixStack, ivertexbuilder, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    protected double getModelYoffset() {
        return 0.375D;
    }

    protected float getModelYrot() {
        return 90.0F;
    }

    private void getAndRenderChain(T bargeEntity, MatrixStack matrixStack, VertexConsumerProvider buffer, int light) {
        if(bargeEntity.getDominant().isPresent()) {
            double dist = ((Entity) bargeEntity.getDominant().get().getFirst()).distanceTo(bargeEntity);
            VertexConsumer vertexConsumer = buffer.getBuffer(chainModel.getLayer(CHAIN_TEXTURE));
            int segments = (int) Math.ceil(dist * 4);
            matrixStack.push();
            for (int i = 0; i < segments; i++) {
                matrixStack.push();
                matrixStack.translate(i / 4.0, 0, 0);
                chainModel.render(matrixStack, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
                matrixStack.pop();
            }
            matrixStack.pop();
        }
    }

    private void getAndRenderLeash(T bargeEntity, float yaw, float deltaTicks, MatrixStack matrixStack, VertexConsumerProvider buffer, int light) {
        matrixStack.push();
        Entity entity = bargeEntity.getHoldingEntity();
        super.render(bargeEntity, yaw, deltaTicks, matrixStack, buffer, light);
        if (entity != null) {
            matrixStack.push();
            this.renderLeash(bargeEntity, deltaTicks, matrixStack, buffer, entity);
            matrixStack.pop();
        }
        matrixStack.pop();
    }

    @Override
    public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z) {
        if(entity.getDominant().isPresent()){
            if(((Entity) entity.getDominant().get().getFirst()).shouldRender(x, y, z)){
                return true;
            }
            if(entity.getDominant().get().getSecond().shouldRender(x, y, z)){
                return true;
            }
        }
        return super.shouldRender(entity, frustum, x, y, z);
    }

    abstract EntityModel getModel(T entity);

    private <E extends Entity> void renderLeash(T entity, float deltaTicks, MatrixStack matrixStack, VertexConsumerProvider buffer, E holder) {
        matrixStack.push();
        Vec3d vector3d = holder.getLeashPos(deltaTicks);
        double d0 = (double)(MathHelper.lerp(deltaTicks, entity.prevBodyYaw, entity.bodyYaw) * ((float)Math.PI / 180F)) + (Math.PI / 2D);
        Vec3d vector3d1 = entity.getLeashOffset();
        double d1 = Math.cos(d0) * vector3d1.z + Math.sin(d0) * vector3d1.x;
        double d2 = Math.sin(d0) * vector3d1.z - Math.cos(d0) * vector3d1.x;
        double d3 = MathHelper.lerp(deltaTicks, entity.prevX, entity.getX()) + d1;
        double d4 = MathHelper.lerp(deltaTicks, entity.prevY, entity.getY()) + vector3d1.y;
        double d5 = MathHelper.lerp(deltaTicks, entity.prevZ, entity.getZ()) + d2;
        matrixStack.translate(d1, vector3d1.y, d2);
        float f = (float)(vector3d.x - d3);
        float f1 = (float)(vector3d.y - d4);
        float f2 = (float)(vector3d.z - d5);
        VertexConsumer ivertexbuilder = buffer.getBuffer(RenderLayer.getLeash());
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
        float f4 = MathHelper.fastInverseSqrt(f * f + f2 * f2) * 0.025F / 2.0F;
        float f5 = f2 * f4;
        float f6 = f * f4;
        BlockPos blockpos = new BlockPos(entity.getCameraPosVec(deltaTicks));
        BlockPos blockpos1 = new BlockPos(holder.getCameraPosVec(deltaTicks));
        int i = this.getBlockLight(entity, blockpos);
        int k = entity.world.getLightLevel(LightType.SKY, blockpos);
        int l = entity.world.getLightLevel(LightType.SKY, blockpos1);

        renderSide(ivertexbuilder, matrix4f, f, f1, f2, i, i, k, l, 0.025F, 0.025F, f5, f6);
        renderSide(ivertexbuilder, matrix4f, f, f1, f2, i, i, k, l, 0.025F, 0.0F, f5, f6);
        matrixStack.pop();
    }

    /** Stolen from MobEntityRenderer, I really didn't feel like using access widener **/

    public static void renderSide(VertexConsumer p_229119_0_, Matrix4f p_229119_1_, float p_229119_2_, float p_229119_3_, float p_229119_4_, int p_229119_5_, int p_229119_6_, int p_229119_7_, int p_229119_8_, float p_229119_9_, float p_229119_10_, float p_229119_11_, float p_229119_12_) {
        int i = 24;

        for(int j = 0; j < 24; ++j) {
            float f = (float)j / 23.0F;
            int k = (int)MathHelper.lerp(f, (float)p_229119_5_, (float)p_229119_6_);
            int l = (int)MathHelper.lerp(f, (float)p_229119_7_, (float)p_229119_8_);
            int i1 = net.minecraft.client.render.LightmapTextureManager.pack(k, l);
            addVertexPair(p_229119_0_, p_229119_1_, i1, p_229119_2_, p_229119_3_, p_229119_4_, p_229119_9_, p_229119_10_, 24, j, false, p_229119_11_, p_229119_12_);
            addVertexPair(p_229119_0_, p_229119_1_, i1, p_229119_2_, p_229119_3_, p_229119_4_, p_229119_9_, p_229119_10_, 24, j + 1, true, p_229119_11_, p_229119_12_);
        }

    }

    public static void addVertexPair(VertexConsumer p_229120_0_, Matrix4f p_229120_1_, int p_229120_2_, float p_229120_3_, float p_229120_4_, float p_229120_5_, float p_229120_6_, float p_229120_7_, int p_229120_8_, int p_229120_9_, boolean p_229120_10_, float p_229120_11_, float p_229120_12_) {
        float f = 0.5F;
        float f1 = 0.4F;
        float f2 = 0.3F;
        if (p_229120_9_ % 2 == 0) {
            f *= 0.7F;
            f1 *= 0.7F;
            f2 *= 0.7F;
        }

        float f3 = (float)p_229120_9_ / (float)p_229120_8_;
        float f4 = p_229120_3_ * f3;
        float f5 = p_229120_4_ > 0.0F ? p_229120_4_ * f3 * f3 : p_229120_4_ - p_229120_4_ * (1.0F - f3) * (1.0F - f3);
        float f6 = p_229120_5_ * f3;
        if (!p_229120_10_) {
            p_229120_0_.vertex(p_229120_1_, f4 + p_229120_11_, f5 + p_229120_6_ - p_229120_7_, f6 - p_229120_12_).color(f, f1, f2, 1.0F).light(p_229120_2_).next();
        }

        p_229120_0_.vertex(p_229120_1_, f4 - p_229120_11_, f5 + p_229120_7_, f6 + p_229120_12_).color(f, f1, f2, 1.0F).light(p_229120_2_).next();
        if (p_229120_10_) {
            p_229120_0_.vertex(p_229120_1_, f4 + p_229120_11_, f5 + p_229120_6_ - p_229120_7_, f6 - p_229120_12_).color(f, f1, f2, 1.0F).light(p_229120_2_).next();
        }

    }


}
