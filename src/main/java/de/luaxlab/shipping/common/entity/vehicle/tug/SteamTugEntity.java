package de.luaxlab.shipping.common.entity.vehicle.tug;

import de.luaxlab.shipping.common.config.ShippingConfig;
import de.luaxlab.shipping.common.container.SteamTugContainer;
import de.luaxlab.shipping.common.core.ModCommon;
import de.luaxlab.shipping.common.entity.accessor.SteamTugDataAccessor;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;


public class SteamTugEntity extends AbstractTugEntity {
    private static final int FURNACE_FUEL_MULTIPLIER= ShippingConfig.Common.STEAM_TUG_FUEL_MULTIPLIER;

    protected int burnTime = 0;
    protected int burnCapacity = 0;

    public SteamTugEntity(EntityType<? extends WaterCreatureEntity> type, World world) {
        super(type, world);
    }

    public SteamTugEntity(World worldIn, Vec3d position) {
        super(ModCommon.ENTITY_STEAM_TUG, worldIn, position);
    }

    @Override
    protected int getNonRouteItemSlots() {
        return 1; // 1 extra slot for fuel
    }

    @Override
    protected NamedScreenHandlerFactory createContainerProvider() {
        return new ExtendedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return new TranslatableText("screen.littlelogistics.tug");
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                //return new SteamTugContainer(i, world, getDataAccessor(), playerInventory, playerEntity);
                return new SteamTugContainer(syncId, getDataAccessor(), playerInventory);
            }

            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                getDataAccessor().write(buf);
            }
        };
    }

    public int getBurnProgress() {
        int i = burnCapacity;
        if (i == 0) {
            i = 200;
        }

        return burnTime * 13 / i;
    }

    public int getBurnTimeSeconds() {
        return burnTime / 20;
    }

    // CONTAINER STUFF
    public boolean isLit() {
        return burnTime > 0;
    }

    @Override
    public SteamTugDataAccessor getDataAccessor() {
        return new SteamTugDataAccessor.Builder(this.getId())
                .withBurnProgress(this::getBurnProgress)
                .withLit(this::isLit)
                .withBurnTime(this::getBurnTimeSeconds)
                .build();
    }

    @Override
    protected boolean isTugSlotItemValid(int slot, @Nonnull ItemStack stack){
        return slot == 1 && FuelRegistry.INSTANCE.get(stack.getItem()) != null;
    }

    @Override
    protected int getTugSlotLimit(int slot){
        return slot == 1 ? 64 : 0;
    }

    @Override
    protected boolean tickFuel() {
        if (burnTime > 0) {
            burnTime--;
            return true;
        } else {
            ItemStack stack = itemHandler.getStack(1);
            if (!stack.isEmpty()) {
                //TODO: Burn hooks
                burnCapacity = ((FuelRegistryImpl)FuelRegistryImpl.INSTANCE).getFuelTimes().getOrDefault(stack.getItem(),0);
                burnTime = burnCapacity - 1;
                stack.decrement(1);
                return true;
            } else {
                burnCapacity = 0;
                burnTime = 0;
                return false;
            }
        }
    }

    public Item getDropItem() {
        return ModCommon.ITEM_STEAM_TUG;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if(!player.world.isClient)
        {
            ItemStack stack = player.getStackInHand(hand).copy();
            if(stack.isOf(ModCommon.ITEM_TUG_ROUTE))
            {
                stack.setCount(1);
                this.setStack(SteamTugEntity.TUG_ROUTE_ITEM_SLOT, stack);
                contentsChanged = true;
                player.sendMessage(new LiteralText("Set route"), true);
            }
            else if(((FuelRegistryImpl)FuelRegistryImpl.INSTANCE).getFuelTimes().containsKey(stack.getItem()))
            {
                //stack.setCount(1);
                this.setStack(1, stack);
                contentsChanged = true;
            }
        }
        return super.interactMob(player, hand);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("burn", burnTime);
        nbt.putInt("burn_capacity", burnCapacity);
        super.writeCustomDataToNbt(nbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        burnTime = nbt.contains("burn") ? nbt.getInt("burn") : 0;
        burnCapacity = nbt.contains("burn_capacity") ? nbt.getInt("burn_capacity") : 0;
        super.readCustomDataFromNbt(nbt);
    }

    // Have to implement IInventory to work with hoppers
    @Override
    public boolean isEmpty() {
        return itemHandler.getStack(1).isEmpty();
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        //TODO: Limit to fuel slot
        if (!this.itemHandler.isValid(slot, stack)){
            return;
        }
        this.itemHandler.setStack(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }
    }
}
