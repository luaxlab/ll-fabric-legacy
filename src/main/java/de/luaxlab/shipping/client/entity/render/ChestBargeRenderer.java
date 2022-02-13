package de.luaxlab.shipping.client.entity.render;

import de.luaxlab.shipping.client.entity.model.ChestBargeModel;
import de.luaxlab.shipping.common.core.ModCommon;
import de.luaxlab.shipping.common.entity.vehicle.barge.ChestBargeEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.util.Identifier;

public class ChestBargeRenderer extends VesselRenderer<ChestBargeEntity> {
    private static final Identifier BARGE_TEXTURE = ModCommon.identifier( "textures/entity/barge.png");
    private static final Identifier BARGE_TEXTURE_CC = ModCommon.identifier( "textures/entity/barge_cc.png");

    private final EntityModel model;

    public ChestBargeRenderer(EntityRendererFactory.Context context) {
        super(context);
        model = new ChestBargeModel(context.getPart(ChestBargeModel.LAYER_LOCATION));
    }

    @Override
    public Identifier getTexture(ChestBargeEntity entity) {
        //Don't tell anyone
        return entity.hasCustomName() && entity.getCustomName().asString().equalsIgnoreCase("i'm different") ? BARGE_TEXTURE_CC : BARGE_TEXTURE;
    }

    @Override
    public EntityModel getModel(ChestBargeEntity entity) {
        return model;
    }
}
