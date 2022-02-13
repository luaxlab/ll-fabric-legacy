package de.luaxlab.shipping.client.entity.render;

import de.luaxlab.shipping.common.core.ModCommon;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

/**
 * A renderer that renders nothing. Use whenever you feel like it.
 */
public class DummyEntityRenderer extends EntityRenderer<Entity> {
    private static final Identifier TEXTURE = ModCommon.identifier("textures/entity/chain.png");

    public DummyEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        //Do nothing
    }

    @Override
    public Identifier getTexture(Entity entity) {
        return TEXTURE;
    }
}
