package de.luaxlab.shipping.common.blockentity;

import com.mojang.datafixers.util.Pair;
import de.luaxlab.shipping.common.block.dock.TugDockBlock;
import de.luaxlab.shipping.common.entity.vehicle.VesselEntity;
import de.luaxlab.shipping.common.entity.vehicle.barge.AbstractBargeEntity;
import de.luaxlab.shipping.common.entity.vehicle.tug.AbstractTugEntity;
import de.luaxlab.shipping.common.util.InventoryUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TugDockBlockEntity extends AbstractDockBlockEntity {


    public TugDockBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**public TugDockBlockEntity(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }
    public TugDockBlockEntity() {
        super(ModTileEntitiesTypes.TUG_DOCK.get());
    }**/

    private boolean handleItemHopper(VesselEntity tugEntity, HopperBlockEntity hopper){
        if(!(tugEntity instanceof Inventory)){
            return false;
        }
        return InventoryUtils.mayMoveIntoInventory((Inventory) tugEntity, hopper);
    }




    public boolean holdVessel(VesselEntity tug, Direction direction){
        BlockState state = world.getBlockState(pos);
        if (!(tug instanceof AbstractTugEntity)
                || !state.get(TugDockBlock.FACING).getOpposite().equals(direction)
                || tug.getMovementDirection().equals(getRowDirection(state.get(TugDockBlock.FACING)))
        ){
            return false;
        }

        // force tug to be docked when powered
        // todo: add UI for inverted mode toggle?
        if (state.get(TugDockBlock.POWERED)) {
            return true;
        }

        if(getHopper().map(hopper -> handleItemHopper(tug, hopper))
                .orElse(getVesselLoader().map(l -> l.holdVessel(tug, IVesselLoader.Mode.EXPORT)).orElse(false))){
            return true;
        }


        List<Pair<AbstractBargeEntity, BargeDockBlockEntity>> barges = getBargeDockPairs((AbstractTugEntity) tug);


        if (barges.stream().map(pair -> pair.getSecond().holdVessel(pair.getFirst(), direction)).reduce(false, Boolean::logicalOr)){
            return true;
        }

        return false;
    }

    @Override
    protected BlockPos getTargetBlockPos() {
        return this.pos.up();
    }

    private List<Pair<AbstractBargeEntity, BargeDockBlockEntity>> getBargeDockPairs(AbstractTugEntity tug){
        List<AbstractBargeEntity> barges = tug.getTrain().getBarges();
        List<BargeDockBlockEntity> docks = getBargeDocks();
        return IntStream.range(0, Math.min(barges.size(), docks.size()))
                .mapToObj(i -> new Pair<>(barges.get(i), docks.get(i)))
                .collect(Collectors.toList());
    }

    private List<BargeDockBlockEntity> getBargeDocks(){
        Direction facing = this.getBlockState().get(TugDockBlock.FACING);
        Direction rowDirection = getRowDirection(facing);
        List<BargeDockBlockEntity> docks = new ArrayList<>();
        for (Optional<BargeDockBlockEntity> dock = getNextBargeDock(rowDirection, this.pos);
             //TODO: Possible bug here
             dock.isPresent();
             dock = getNextBargeDock(rowDirection, dock.get().getPos())) {
             docks.add(dock.get());
        }
        return docks;
    }

    private Direction getRowDirection(Direction facing) {
        return this.world.getBlockState(pos).get(TugDockBlock.INVERTED) ? facing.rotateYClockwise() : facing.rotateYCounterclockwise();
    }

    private Optional<BargeDockBlockEntity> getNextBargeDock(Direction rowDirection, BlockPos pos) {
        BlockPos next = pos.offset(rowDirection);
        return Optional.ofNullable(this.world.getBlockEntity(next))
                .filter(e -> e instanceof BargeDockBlockEntity)
                .map(e -> (BargeDockBlockEntity) e);
    }

    //Removed the nbt method as it did not save anything
}
