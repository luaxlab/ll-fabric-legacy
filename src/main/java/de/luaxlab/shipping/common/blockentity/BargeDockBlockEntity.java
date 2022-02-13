package de.luaxlab.shipping.common.blockentity;

import de.luaxlab.shipping.common.block.dock.BargeDockBlock;
import de.luaxlab.shipping.common.entity.vehicle.VesselEntity;
import de.luaxlab.shipping.common.entity.vehicle.barge.AbstractBargeEntity;
import de.luaxlab.shipping.common.util.InventoryUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BargeDockBlockEntity extends AbstractDockBlockEntity {
    public BargeDockBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /*public BargeDockBlockEntity(TileEntityType<?> p_i48289_1_) {
        super(p_i48289_1_);
    }

    public BargeDockBlockEntity() {
        super(ModTileEntitiesTypes.BARGE_DOCK.get());
    }*/

    protected BlockPos getTargetBlockPos(){
        if (isExtract()) {
            return this.pos.down()
                    .offset(this.getBlockState().get(BargeDockBlock.FACING));
        } else return this.pos.up();
    }


    private boolean handleItemHopper(VesselEntity bargeEntity, HopperBlockEntity hopper){
        if(!(bargeEntity instanceof Inventory)){
            return false;
        }
        if (isExtract()) {
            return InventoryUtils.mayMoveIntoInventory(hopper, (Inventory) bargeEntity);
        } else {
            return InventoryUtils.mayMoveIntoInventory((Inventory) bargeEntity, hopper);
        }
    }

    private Boolean isExtract() {
        return getBlockState().get(BargeDockBlock.EXTRACT_MODE);
    }


    @Override
    public boolean holdVessel(VesselEntity vessel, Direction direction) {
        if (!(vessel instanceof AbstractBargeEntity)
                || !getBlockState().get(BargeDockBlock.FACING).getOpposite().equals(direction))
        {
            return false;
        }

        return getHopper().map(h -> handleItemHopper(vessel, h))
                .orElse(getVesselLoader().map(l -> l.holdVessel(vessel, isExtract() ? IVesselLoader.Mode.IMPORT : IVesselLoader.Mode.EXPORT))
                        .orElse(false));
    }
}
