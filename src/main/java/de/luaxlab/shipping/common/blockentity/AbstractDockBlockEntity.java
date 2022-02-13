package de.luaxlab.shipping.common.blockentity;

import de.luaxlab.shipping.common.entity.vehicle.VesselEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Optional;


public abstract class AbstractDockBlockEntity extends BlockEntity {

    public AbstractDockBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract boolean holdVessel(VesselEntity vessel, Direction direction);

    public Optional<HopperBlockEntity> getHopper(){
        BlockEntity mayBeHopper = this.world.getBlockEntity(this.getTargetBlockPos());
        if (mayBeHopper instanceof HopperBlockEntity) {
            return Optional.of((HopperBlockEntity) mayBeHopper);
        }
        else return Optional.empty();
    }

    public Optional<IVesselLoader> getVesselLoader(){
        BlockEntity canidate = this.world.getBlockEntity(this.getTargetBlockPos());
        if (canidate instanceof IVesselLoader) {
            return Optional.of((IVesselLoader) canidate);
        }
        else return Optional.empty();
    }

    protected abstract BlockPos getTargetBlockPos();

    protected BlockState getBlockState()
    {
        return world.getBlockState(pos);
    }

}
