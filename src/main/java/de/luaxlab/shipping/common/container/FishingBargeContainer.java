package de.luaxlab.shipping.common.container;

import de.luaxlab.shipping.common.core.ModCommon;
import de.luaxlab.shipping.common.entity.vehicle.barge.FishingBargeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

public class FishingBargeContainer extends AbstractItemHandlerContainer {
    private final FishingBargeEntity fishingBargeEntity;

    public FishingBargeContainer(int windowId, World world, int entityId,
                                 PlayerInventory playerInventory, PlayerEntity player) {
        super(ModCommon.CONTAINER_FISHING_BARGE, windowId, playerInventory, player);
        this.fishingBargeEntity = (FishingBargeEntity) world.getEntityById(entityId);
        layoutPlayerInventorySlots(8, 49);

        if(fishingBargeEntity != null) {
            /*fishingBargeEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
                for(int k = 0; k < 9; ++k) {
                    this.addSlot(new SlotItemHandler(h, k, 8 + k * 18, 18));
                }
            });*/ //No capability
            for(int k = 0; k < fishingBargeEntity.size(); ++k) {
                this.addSlot(new Slot(fishingBargeEntity, k, 8 + k * 18, 18));
            }
        }
    }

    @Override
    protected int getSlotNum() {
        return fishingBargeEntity.size();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return fishingBargeEntity.canPlayerUse(player);
    }
}
