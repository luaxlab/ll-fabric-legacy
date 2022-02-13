package de.luaxlab.shipping.client;

import de.luaxlab.shipping.client.core.ClientEventHandlerImpl;
import de.luaxlab.shipping.client.entity.model.ChainModel;
import de.luaxlab.shipping.client.entity.model.ChestBargeModel;
import de.luaxlab.shipping.client.entity.model.SteamTugModel;
import de.luaxlab.shipping.client.entity.render.ChestBargeRenderer;
import de.luaxlab.shipping.client.entity.render.DummyEntityRenderer;
import de.luaxlab.shipping.client.entity.render.SteamTugRenderer;
import de.luaxlab.shipping.client.screen.SteamTugScreen;
import de.luaxlab.shipping.common.core.ModCommon;
import de.luaxlab.shipping.common.core.ModEntities;
import dev.architectury.event.events.client.ClientTextureStitchEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

public class ModClient implements ClientModInitializer {



    @Override
    public void onInitializeClient() {
        //EntityRenderRegistry
        EntityRendererRegistry.register(ModEntities.STEAM_TUG, SteamTugRenderer::new);
        EntityRendererRegistry.register(ModEntities.DUMMY_TUG_HITBOX, DummyEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.CHEST_BARGE, ChestBargeRenderer::new);
        EntityRendererRegistry.register(ModEntities.SPRING, DummyEntityRenderer::new);

        //EntityModelLayerRegistry
        EntityModelLayerRegistry.registerModelLayer(ChainModel.LAYER_LOCATION, ChainModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(SteamTugModel.LAYER_LOCATION, SteamTugModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ChestBargeModel.LAYER_LOCATION, ChestBargeModel::createBodyLayer);

        //Screens
        ScreenRegistry.register(ModCommon.CONTAINER_STEAM_TUG, SteamTugScreen::new);

        //Event registration
        ClientTextureStitchEvent.PRE.register(ClientEventHandlerImpl.INSTANCE);
        WorldRenderEvents.END.register(ClientEventHandlerImpl.INSTANCE);


    }
}
