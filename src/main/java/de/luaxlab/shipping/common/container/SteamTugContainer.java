package de.luaxlab.shipping.common.container;

import com.mojang.datafixers.util.Pair;
import de.luaxlab.shipping.common.core.ModCommon;
import de.luaxlab.shipping.common.entity.accessor.SteamTugDataAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;


public class SteamTugContainer extends AbstractTugContainer<SteamTugDataAccessor> {

    public static final Identifier EMPTY_TUG_ROUTE = ModCommon.identifier("item/empty_tug_route");
    public static final Identifier EMPTY_ENERGY = ModCommon.identifier( "item/empty_energy");
    public static final Identifier EMPTY_ATLAS_LOC = PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    
    public SteamTugContainer(int windowId, SteamTugDataAccessor data,
                             PlayerInventory playerInventory) {
        super(ModCommon.CONTAINER_STEAM_TUG, windowId, playerInventory.player.world, data, playerInventory, playerInventory.player);

        /*if(tugEntity != null) {
            tugEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
                addSlot(new SlotItemHandler(h, 0, 116, 35)
                        .setBackground(ModClientEventHandler.EMPTY_ATLAS_LOC, ModClientEventHandler.EMPTY_TUG_ROUTE));
                addSlot(new SlotItemHandler(h, 1, 42, 40));
            });
        }*/
        addSlot(new SingleSmartItemSlot(tugEntity, 0, 116, 35, new Pair<>(EMPTY_ATLAS_LOC, EMPTY_TUG_ROUTE))
        {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModCommon.ITEM_TUG_ROUTE);
            }
        });
        addSlot(new Slot(tugEntity, 1, 42, 40));

        this.addProperties(data.getRawData());
    }


    public int getBurnProgress(){
        return data.getBurnProgress();
    }

    public int getBurnTimeSeconds(){
        return data.getBurnTime();
    }

    public boolean isLit(){
        return data.isLit();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        /*return tugEntity.canPlayerUse(player);*/
        //TODO: Fix canPlayerUse
        return true;
    }

    @Override
    protected int getSlotNum() {
        return 2;
    }

    public static class SingleSmartItemSlot extends Slot {

        private final Pair<Identifier, Identifier> bgSprite;

        public SingleSmartItemSlot(Inventory inventory, int index, int x, int y, @Nullable Pair<Identifier, Identifier> bg) {
            super(inventory, index, x, y);
            bgSprite = bg != null ? bg : super.getBackgroundSprite();
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }

        @Nullable
        @Override
        public Pair<Identifier, Identifier> getBackgroundSprite() {
            return bgSprite;
        }
    }
}
