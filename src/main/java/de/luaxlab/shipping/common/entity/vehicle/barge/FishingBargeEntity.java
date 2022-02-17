package de.luaxlab.shipping.common.entity.vehicle.barge;

import com.mojang.datafixers.util.Pair;
import de.luaxlab.shipping.common.config.ShippingConfig;
import de.luaxlab.shipping.common.container.FishingBargeContainer;
import de.luaxlab.shipping.common.core.ModEntities;
import de.luaxlab.shipping.common.core.ModItems;
import de.luaxlab.shipping.common.entity.SpringableEntity;
import de.luaxlab.shipping.common.util.InventoryUtils;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.IntStream;

public class FishingBargeEntity extends AbstractBargeEntity implements Inventory, SidedInventory {
    protected final SimpleInventory itemHandler = createHandler();
    //protected final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    protected boolean contentsChanged = false;
    private int ticksDeployable = 0;
    private int fishCooldown = 0;
    private final Set<Pair<Integer, Integer>> overFishedCoords = new HashSet<>();
    private final Queue<Pair<Integer, Integer>> overFishedQueue = new LinkedList<>();

    private static final Identifier fishingLootTable =
            new Identifier(ShippingConfig.Common.FISHING_LOOT_TABLE);


    public FishingBargeEntity(EntityType<? extends FishingBargeEntity> type, World world) {
        super(type, world);
    }
    public FishingBargeEntity(World worldIn, Vec3d position) {
        super(ModEntities.FISHING_BARGE, worldIn,position);
    }


    @Override
    protected void doInteract(PlayerEntity player) {
        //NetworkHooks.openGui((ServerPlayerEntity) player, createContainerProvider(), buffer -> buffer.writeInt(this.getId()));
        //TODO
        NamedScreenHandlerFactory factory = createContainerProvider();
        if(factory != null)
            player.openHandledScreen(factory);
    }

    protected NamedScreenHandlerFactory createContainerProvider() {
        return new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                buf.writeInt(getId());
            }

            @Override
            public Text getDisplayName() {
                return new TranslatableText("screen.littlelogistics.fishing_barge");
            }

            @Nullable
            @Override
            public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                if(playerEntity.isSpectator())
                    return null;
                return new FishingBargeContainer(i, world, getId(), playerInventory, playerEntity);
            }
        };
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason == RemovalReason.KILLED && !this.world.isClient) {
            ItemScatterer.spawn(this.world, this, this);
        }
        super.remove(reason);
    }

    private SimpleInventory createHandler() {
        return new SimpleInventory(9);
    }


    @Override
    public void tick(){
        super.tick();
        tickWaterOnSidesCheck();
        if(!this.world.isClient && this.getStatus() == Status.DEPLOYED){
            if(fishCooldown < 0) {
                tickFish();
                fishCooldown = 20;
            }  else {
                fishCooldown--;
            }
        }

    }

    private void tickWaterOnSidesCheck(){
        if(hasWaterOnSides()){
            ticksDeployable++;
        }else {
            ticksDeployable = 0;
        }
    }

    private double computeDepthPenalty(){
        int count = 0;
        for (BlockPos pos = this.getBlockPos();  this.world.getBlockState(pos).isOf(Blocks.WATER); pos = pos.down()){
            count ++;
        }
        count = Math.min(count, 20);
        return ((double) count) / 20.0;
    }

    private void tickFish(){
        double overFishPenalty = isOverFished() ? 0.05 : 1;
        double shallowPenalty = computeDepthPenalty();
        double chance = 0.25 * overFishPenalty * shallowPenalty;
        double treasure_chance = shallowPenalty > 0.4 ? chance * (shallowPenalty / 2)
                * ShippingConfig.Common.FISHING_TREASURE_CHANCE_MODIFIER : 0;
        double r = Math.random();
        if(r < chance){
            LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld)this.world))
                    .optionalParameter(LootContextParameters.ORIGIN, this.getPos())
                    .optionalParameter(LootContextParameters.THIS_ENTITY, this)
                    .optionalParameter(LootContextParameters.TOOL, new ItemStack(Items.FISHING_ROD))
                    .random(this.random);

            //lootcontext$builder.optionalParameter(LootContextParameters.KILLER_ENTITY, this).optionalParameter(LootContextParameters.THIS_ENTITY, this);
            LootTable loottable = this.world.getServer().getLootManager().getTable(r < treasure_chance ? LootTables.FISHING_TREASURE_GAMEPLAY : fishingLootTable);
            List<ItemStack> list = loottable.generateLoot(lootcontext$builder.build(LootContextTypes.FISHING));
            for (ItemStack stack : list) {
                int slot = InventoryUtils.findSlotFotItem(this, stack);
                if (slot != -1) {
                    itemHandler.addStack(stack);
                    //TODO
                }
                if(!isOverFished()) {
                    addOverFish();
                }
            }
        }
    }

    private String overFishedString(){
        return overFishedQueue.stream().map(t -> t.getFirst() + ":" + t.getSecond()).reduce("", (acc, curr) -> String.join(",", acc, curr));
    }

    private void populateOverfish(String string){
        Arrays.stream(string.split(","))
                .filter(s -> !s.isEmpty())
                .map(s -> s.split(":"))
                .map(arr -> new Pair(Integer.parseInt(arr[0]), Integer.parseInt(arr[1])))
                .forEach(overFishedQueue::add);
        overFishedCoords.addAll(overFishedQueue);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        InventoryUtils.readNbt(nbt.getCompound("inv"), this.itemHandler);
        populateOverfish(nbt.getString("overfish"));
        super.readCustomDataFromNbt(nbt);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        InventoryUtils.writeNbt(nbt.getCompound("inv"), this.itemHandler, false);
        nbt.putString("overfish", overFishedString());
        super.writeCustomDataToNbt(nbt);
    }


    private void addOverFish(){
        int x = (int) Math.floor(this.getX());
        int z = (int) Math.floor(this.getZ());
        overFishedCoords.add(new Pair<>(x, z));
        overFishedQueue.add(new Pair<>(x, z));
        if(overFishedQueue.size() > 30){
            overFishedCoords.remove(overFishedQueue.poll());
        }
    }

    private boolean isOverFished(){
        int x = (int) Math.floor(this.getX());
        int z = (int) Math.floor(this.getZ());
        return overFishedCoords.contains(new Pair<>(x, z));
    }

    @Override
    public Item getDropItem() {
        return ModItems.FISHING_BARGE;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return IntStream.range(0, size()).toArray();
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @org.jetbrains.annotations.Nullable Direction dir) {
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return isDockable();
    }

    @Override
    public int size() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        return itemHandler.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return itemHandler.getStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return itemHandler.removeStack(slot,amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return itemHandler.removeStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        itemHandler.setStack(slot, stack);
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
    public void markDirty() {
        contentsChanged = true;
    }

    public Status getStatus(){
        return hasWaterOnSides() ? getNonStashedStatus() : Status.STASHED;
    }

    private Status getNonStashedStatus(){
        if (ticksDeployable < 40){
            return Status.TRANSITION;
        } else {
            return this.applyWithDominant(SpringableEntity::hasWaterOnSides)
                    .reduce(true, Boolean::logicalAnd)
                    ? Status.DEPLOYED : Status.TRANSITION;
        }
    }

    /*@Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }

        return super.getCapability(cap, side);
    }*/

    @Override
    public void clear() {
        //Empty on purpose
    }

    public enum Status {
        STASHED,
        DEPLOYED,
        TRANSITION
    }
}
