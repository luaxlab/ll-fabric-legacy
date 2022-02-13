package de.luaxlab.shipping.common.entity.vehicle.barge;

import de.luaxlab.shipping.common.core.ModEntities;
import de.luaxlab.shipping.common.core.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class ChestBargeEntity extends AbstractBargeEntity implements Inventory, NamedScreenHandlerFactory, SidedInventory {
    protected final DefaultedList<ItemStack> itemStacks = createItemStacks();

    public ChestBargeEntity(EntityType<? extends ChestBargeEntity> type, World world) {
        super(type, world);
    }

    public ChestBargeEntity(World worldIn, Vec3d spawnPos) {
        super(ModEntities.CHEST_BARGE, worldIn, spawnPos);
    }

    protected DefaultedList<ItemStack> createItemStacks(){
        return DefaultedList.ofSize(36, ItemStack.EMPTY);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else if (!this.world.isClient && !this.isRemoved()) {
            //TODO: Improvement: Drop no item if creative player
            if(!source.isOutOfWorld())
            {
                if(!source.isSourceCreativePlayer())
                    this.dropItem(this.getDropItem());
                ItemScatterer.spawn(this.world, this, this);
            }

            this.remove(RemovalReason.KILLED);
            return true;
        } else {
            return true;
        }
    }

    @Override
    public Item getDropItem() {
        return ModItems.CHEST_BARGE;
    }

    protected void doInteract(PlayerEntity player) {
        player.openHandledScreen(this);
    }

    @Override
    public int size() {
        return 27;
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack itemstack : this.itemStacks) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.itemStacks.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.itemStacks, slot, amount);

    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack itemstack = this.itemStacks.get(slot);
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.itemStacks.set(slot, ItemStack.EMPTY);
            return itemstack;
        }
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.itemStacks.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }
    }

    @Override
    public void markDirty() {
        //Nothing here on purpose
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (this.isRemoved()) {
            return false;
        } else {
            return !(player.squaredDistanceTo(this) > 64.0D);
        }
    }

    @Override
    public void clear() {
        this.itemStacks.clear();
    }

//here
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        Inventories.writeNbt(nbt,this.itemStacks);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        Inventories.readNbt(nbt,this.itemStacks);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return IntStream.range(0, size()).toArray();
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @org.jetbrains.annotations.Nullable Direction dir) {
        return isDockable();
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return isDockable();
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        if(player.isSpectator())
            return null;
        return GenericContainerScreenHandler.createGeneric9x3(syncId,inv,this);
    }
}
