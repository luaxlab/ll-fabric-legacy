package de.luaxlab.shipping.common.navigation;

import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.ai.pathing.SwimNavigation;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;

public class TugPathNavigator extends SwimNavigation {
    public TugPathNavigator(MobEntity p_i45873_1_, World p_i45873_2_) {
        super(p_i45873_1_, p_i45873_2_);
        setRangeMultiplier(5);
    }

    @Override
    protected PathNodeNavigator createPathNodeNavigator(int range) {
        this.nodeMaker = new TugNodeProcessor();
        return new PathNodeNavigator(this.nodeMaker, range);
    }

    @Override
    public boolean startMovingTo(double x, double y, double z, double speed) {
        return this.startMovingAlong(this.findPathTo(x,y,z,0), speed);
    }
}
