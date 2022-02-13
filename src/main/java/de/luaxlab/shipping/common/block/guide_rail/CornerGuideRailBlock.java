package de.luaxlab.shipping.common.block.guide_rail;

import com.mojang.datafixers.util.Pair;
import de.luaxlab.shipping.common.entity.vehicle.VesselEntity;
import de.luaxlab.shipping.common.util.BoxUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class CornerGuideRailBlock extends Block {
    protected static final VoxelShape SHAPE = BoxUtil.block(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);

    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty INVERTED = Properties.INVERTED;


    public CornerGuideRailBlock(Settings settings) {
        super(settings);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(player.getPose().equals(EntityPose.CROUCHING)){
            world.setBlockState(pos, state.with(INVERTED, !state.get(INVERTED)));
            return ActionResult.SUCCESS;
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, INVERTED);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(FACING, ctx.getPlayerFacing().getOpposite())
                .with(INVERTED, false);
    }

    public static Direction getArrowsDirection(BlockState state){
        Direction facing = state.get(CornerGuideRailBlock.FACING);
        return state.get(CornerGuideRailBlock.INVERTED) ? facing.rotateYClockwise() : facing.rotateYCounterclockwise();
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        Direction facing = state.get(CornerGuideRailBlock.FACING);
        if(!entity.getMovementDirection().equals(facing.getOpposite()) || !(entity instanceof VesselEntity)){
            return;
        }

        Direction arrows = getArrowsDirection(state);
        ((VesselEntity) entity).setLastCornerGuideRail(new Pair<>(pos, state));
        entity.setVelocity(entity.getVelocity().add(
                new Vec3d(
                        (facing.getOpposite().getOffsetX() + arrows.getOffsetX()) * 0.1,
                        0,
                        (facing.getOpposite().getOffsetZ() + arrows.getOffsetZ()) * 0.1)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
}
