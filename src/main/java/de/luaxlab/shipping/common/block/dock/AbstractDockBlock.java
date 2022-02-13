package de.luaxlab.shipping.common.block.dock;

import de.luaxlab.shipping.common.blockentity.AbstractDockBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class AbstractDockBlock extends Block implements BlockEntityProvider {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    public AbstractDockBlock(Settings settings) {
        super(settings);
    }

    protected Optional<AbstractDockBlockEntity> getBlockEntity(World world, BlockPos pos){
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof AbstractDockBlockEntity)
            return Optional.of((AbstractDockBlockEntity) tileEntity);
        else
            return Optional.empty();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
        getBlockEntity(world, pos).flatMap(AbstractDockBlockEntity::getHopper).ifPresent(te -> {
            if (te.getPos().equals(pos.up())){
                world.setBlockState(te.getPos(), te.getWorld().getBlockState(te.getPos()).with(HopperBlock.FACING, state.get(FACING)));
            }
        });
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
        builder.add(FACING);
    }

    /*@Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context){
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());

    }*/

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
    }
}
