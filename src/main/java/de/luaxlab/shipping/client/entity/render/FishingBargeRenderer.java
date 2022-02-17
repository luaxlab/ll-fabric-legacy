package de.luaxlab.shipping.client.entity.render;

import de.luaxlab.shipping.client.ModClient;
import de.luaxlab.shipping.client.entity.model.FishingBargeModel;
import de.luaxlab.shipping.common.core.ModCommon;
import de.luaxlab.shipping.common.entity.vehicle.barge.FishingBargeEntity;
import net.minecraft.client.render.entity.CreeperEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.util.Identifier;

public class FishingBargeRenderer extends VesselRenderer<FishingBargeEntity> {

    private static final Identifier BARGE_TEXTURE =
            ModCommon.identifier("textures/entity/fishing_barge.png");

    protected EntityModel<FishingBargeEntity> model;

    //private final EntityModel stashed = new FishingBargeModel();
    /*
    private final EntityModel transition = new FishingBargeTransitionModel();
    private final EntityModel deployed = new FishingBargeDeployedModel();
    */ //We don't use them any more

    public FishingBargeRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        model = new FishingBargeModel<FishingBargeEntity>(ctx.getPart(FishingBargeModel.LAYER_LOCATION));
    }

    @Override
    EntityModel getModel(FishingBargeEntity entity) {
        return model;
    }

    @Override
    public Identifier getTexture(FishingBargeEntity entity) {
        return BARGE_TEXTURE;
    }
}
