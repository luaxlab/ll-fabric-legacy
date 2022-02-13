package de.luaxlab.shipping.common.navigation;

import de.luaxlab.shipping.common.core.ModCommon;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;

public class TugNodeProcessor extends WaterPathNodeMaker {
    public TugNodeProcessor() {
        super(false);
    }

    @Override
    public int getSuccessors(PathNode[] successors, PathNode node) {
        int i = 0;

        for(Direction direction : Arrays.asList(Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH)) {
            PathNode PathNode = this.getWaterNode(node.x + direction.getOffsetX(), node.y + direction.getOffsetY(), node.z + direction.getOffsetZ());
            if (PathNode != null && !PathNode.visited) {
                successors[i++] = PathNode;
            }
        }

        return i;
    }

    private PathNode getNodeSimple(int x, int y, int z) {
        return this.pathNodeCache.computeIfAbsent(PathNode.hash(x, y, z), (p_215743_3_) -> {
            return new PathNode(x, y, z);
        });
    }

    @Override
    public TargetPathNode getNode(double x, double y, double z) {
        return new TargetPathNode(getNodeSimple(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z)));
    }

    @Override
    protected PathNode getNode(int p_176159_1_, int p_176159_2_, int p_176159_3_) {
        PathNode PathNode = super.getNode(p_176159_1_, p_176159_2_, p_176159_3_);
        if (PathNode != null) {
            BlockPos pos = PathNode.getBlockPos();
            float penalty = 0;
            for (BlockPos surr : Arrays.asList(
                    pos.east(),
                    pos.west(),
                    pos.south(),
                    pos.north(),
                    pos.north().west(),
                    pos.north().east(),
                    pos.south().east(),
                    pos.south().west(),
                    pos.north().west().north().west(),
                    pos.north().east().north().east(),
                    pos.south().west().south().west(),
                    pos.south().east().south().east()
            )
            ){
                // if the point's neighbour has land, penalty is 5 unless there is a dock
                if(!cachedWorld.getBlockState(surr).isOf(Blocks.WATER)){
                    penalty = 5f;
                }
                if(
                        cachedWorld.getBlockState(surr).isOf(ModCommon.BLOCK_GUIDE_CORNER)
                                //cachedWorld.getBlockState(surr).isOf(ModBlocks.BARGE_DOCK.get()) || //TODO: Add barge
                                //cachedWorld.getBlockState(surr).isOf(ModBlocks.TUG_DOCK.get())

                ){
                    penalty = 0;
                    break;
                }
            }
            PathNode.penalty += penalty;
        }


        return PathNode;
    }

    private PathNode getWaterNode(int p_186328_1_, int p_186328_2_, int p_186328_3_) {
        PathNodeType pathnodetype = this.isFree(p_186328_1_, p_186328_2_, p_186328_3_);
        return  pathnodetype != PathNodeType.WATER ? null : this.getNode(p_186328_1_, p_186328_2_, p_186328_3_);
    }

    private PathNodeType isFree(int p_186327_1_, int p_186327_2_, int p_186327_3_) {
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for(int i = p_186327_1_; i < p_186327_1_ + this.entityBlockXSize; ++i) {
            for(int j = p_186327_2_; j < p_186327_2_ + this.entityBlockYSize; ++j) {
                for(int k = p_186327_3_; k < p_186327_3_ + this.entityBlockZSize; ++k) {
                    FluidState fluidstate = this.cachedWorld.getFluidState(blockpos$mutable.set(i, j, k));
                    BlockState blockstate = this.cachedWorld.getBlockState(blockpos$mutable.set(i, j, k));
                    if (fluidstate.isEmpty() && blockstate.canPathfindThrough(this.cachedWorld, blockpos$mutable.down(), NavigationType.WATER) && blockstate.isAir()) {
                        return PathNodeType.BREACH;
                    }

                    if (!fluidstate.isOf(Fluids.WATER)) {
                        return PathNodeType.BLOCKED;
                    }
                }
            }
        }

        BlockState blockstate1 = this.cachedWorld.getBlockState(blockpos$mutable);
        return blockstate1.canPathfindThrough(this.cachedWorld, blockpos$mutable, NavigationType.WATER) ? PathNodeType.WATER : PathNodeType.BLOCKED;
    }

}
