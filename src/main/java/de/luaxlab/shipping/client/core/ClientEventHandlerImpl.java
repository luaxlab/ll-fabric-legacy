package de.luaxlab.shipping.client.core;

import de.luaxlab.shipping.common.config.ShippingConfig;
import de.luaxlab.shipping.common.container.SteamTugContainer;
import de.luaxlab.shipping.common.core.ModCommon;
import de.luaxlab.shipping.common.item.TugRouteItem;
import dev.architectury.event.events.client.ClientTextureStitchEvent;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.function.Consumer;

public class ClientEventHandlerImpl implements ClientTextureStitchEvent.Pre, WorldRenderEvents.End {

    private final Identifier ROUTE_BEACON_TEXTURE = ModCommon.identifier("textures/entity/beacon_beam.png");

    public static final ClientEventHandlerImpl INSTANCE = new ClientEventHandlerImpl();

    @Override
    public void stitch(SpriteAtlasTexture atlas, Consumer<Identifier> spriteAdder) {
        //Manually add our placeholder items to stitching
        if(atlas.getId() != SteamTugContainer.EMPTY_ATLAS_LOC) return;
        spriteAdder.accept(SteamTugContainer.EMPTY_TUG_ROUTE);
        spriteAdder.accept(SteamTugContainer.EMPTY_ENERGY);
    }

    @Override
    public void onEnd(WorldRenderContext context) {
        if(ShippingConfig.Client.DISABLE_TUG_ROUTE_BEACONS)
            return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
        if (stack.getItem().equals(ModCommon.ITEM_TUG_ROUTE)){
            Vec3d vector3d = MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos();
            double x = vector3d.x;
            double v = vector3d.y;
            double z = vector3d.z;

            VertexConsumerProvider.Immediate renderTypeBuffer = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            List<Vector2f> route = TugRouteItem.getRoute(stack);
            for (int i = 0, routeSize = route.size(); i < routeSize; i++) {
                Vector2f node = route.get(i);
                MatrixStack matrixStack = context.matrixStack();

                matrixStack.push();
                matrixStack.translate(node.getX() - x, 1 - v, node.getY() - z);

                BeaconBlockEntityRenderer.renderBeam(matrixStack, renderTypeBuffer, ROUTE_BEACON_TEXTURE, context.tickDelta(),
                        1F, context.world().getTime(), 0, 1024,
                        DyeColor.RED.getColorComponents(), 0.2F, 0.25F);
                matrixStack.pop();
                matrixStack.push();

                matrixStack.translate(node.getX() - x , player.getY() + 2 - v, node.getY() - z );
                matrixStack.scale(-0.025F, -0.025F, -0.025F);

                matrixStack.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());

                Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();

                TextRenderer fontRenderer = MinecraftClient.getInstance().textRenderer;
                String text =  "Node " + i; //node.getDisplayName(i);
                float width = (-fontRenderer.getWidth(text) / (float) 2);
                fontRenderer.draw(text, width, 0.0F, -1, true, matrix4f, renderTypeBuffer, true, 0, 15728880);
                matrixStack.pop();
            }
            renderTypeBuffer.drawCurrentLayer();
        }
    }
}
