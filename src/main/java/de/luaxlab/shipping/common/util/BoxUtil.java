package de.luaxlab.shipping.common.util;

import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class BoxUtil {

    public static VoxelShape block(double sx, double sy, double sz, double ex, double ey, double ez)
    {
        return VoxelShapes.cuboid(sx/16,sy/16,sz/16,ex/16,ey/16,ez/16);
    }

    public static final Box INFINITE_EXTENT_BOX = new Box(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
}
