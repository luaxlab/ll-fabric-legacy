package de.luaxlab.shipping.common.util;

import de.luaxlab.shipping.common.entity.vehicle.tug.AbstractTugEntity;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class InventoryUtils {

    public static boolean mayMoveIntoInventory(Inventory target, Inventory source) {
        if (source.isEmpty()){
            return false;
        }

        HashMap<Item, List<ItemStack>> map = new HashMap<>();
        List<Integer> airList = new ArrayList<>();
        int init = target instanceof AbstractTugEntity ? 1 : 0;
        for (int i = init; i < target.size(); i++) {
            ItemStack stack = target.getStack(i);
            if((stack.isEmpty() || stack.getItem().equals(Items.AIR)) && target.isValid(i, stack)){
                airList.add(i);
            }
            else if (stack.getMaxCount() != stack.getCount() && target.isValid(i, stack)) {
                if (map.containsKey(stack.getItem())) {
                    map.get(stack.getItem()).add(stack);
                } else {
                    map.put(stack.getItem(), new ArrayList<>(Collections.singleton(stack)));
                }
            }
        }

        for (int i = 0; i < source.size(); i++) {
            ItemStack stack = source.getStack(i);
            if (!stack.isEmpty() && map.containsKey(stack.getItem())) {
                for (ItemStack targetStack : map.get(stack.getItem())){
                    if (canMergeItems(targetStack, stack))
                        return true;
                }
            } else if (!airList.isEmpty() && target instanceof Entity){
                Entity e = (Entity) target;
                //TODO: Capability?
                /*boolean validSlot = e.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                        .map(itemHandler -> airList.stream()
                                .map(j -> itemHandler.isItemValid(j, stack))
                                .reduce(false, Boolean::logicalOr)).orElse(true);*/
                for (int j = 0; j < target.size(); j++) {
                    if(target.isValid(j,stack)) {
                        return true;
                    }
                }
            } else if (!airList.isEmpty()){
                return true;
            }
        }
        return false;
    }

    public static int findSlotFotItem(Inventory target, ItemStack itemStack) {
        for (int i = 0; i < target.size(); i++) {
            ItemStack stack = target.getStack(i);
            if(stack.isEmpty() || stack.getItem().equals(Items.AIR)){
                return i;
            }
            else if (canMergeItems(stack, itemStack)) {
                return i;
            }
        }

        return -1;
    }

    public static boolean canMergeItems(ItemStack p_145894_0_, ItemStack p_145894_1_) {
        if (p_145894_0_.getItem() != p_145894_1_.getItem()) {
            return false;
        } else if (p_145894_0_.getDamage() != p_145894_1_.getDamage()) {
            return false;
        } else if (p_145894_0_.getCount() > p_145894_0_.getMaxCount()) {
            return false;
        } else {
            return ItemStack.areNbtEqual(p_145894_0_, p_145894_1_);
        }
    }
}
