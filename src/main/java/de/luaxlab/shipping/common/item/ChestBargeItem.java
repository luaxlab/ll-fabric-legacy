package de.luaxlab.shipping.common.item;

import de.luaxlab.shipping.common.entity.vehicle.barge.ChestBargeEntity;
import de.luaxlab.shipping.common.entity.vehicle.tug.SteamTugEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ChestBargeItem extends AbstractEntityAddItem {
    public ChestBargeItem(Settings settings) {
        super(settings);
    }

    protected Entity getEntity(World world, Vec3d vec3d) {
        return new ChestBargeEntity(world, vec3d);
    }

}
