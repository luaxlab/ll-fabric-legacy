package de.luaxlab.shipping.client.entity.render;

import de.luaxlab.shipping.client.entity.model.SteamTugModel;
import de.luaxlab.shipping.common.core.ModCommon;
import de.luaxlab.shipping.common.entity.vehicle.tug.SteamTugEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.util.Identifier;

public class SteamTugRenderer extends VesselRenderer<SteamTugEntity> {
    private static final Identifier TEXTURE =
            new Identifier(ModCommon.MODID, "textures/entity/tug.png");


    private final EntityModel model;

    public SteamTugRenderer(EntityRendererFactory.Context context) {
        super(context);
        model = new SteamTugModel(context.getPart(SteamTugModel.LAYER_LOCATION));
    }

    @Override
    public Identifier getTexture(SteamTugEntity entity) {
        return TEXTURE;
    }

    @Override
    EntityModel getModel(SteamTugEntity entity) {
        return model;
    }

    @Override
    protected double getModelYoffset() {
        return 1.55D;
    }

    @Override
    protected float getModelYrot() {
        return 0;
    }

}
