package de.luaxlab.shipping.common.entity.vehicle.tug;

import com.mojang.datafixers.util.Pair;
import de.luaxlab.shipping.common.block.guide_rail.TugGuideRailBlock;
import de.luaxlab.shipping.common.blockentity.TugDockBlockEntity;
import de.luaxlab.shipping.common.config.ShippingConfig;
import de.luaxlab.shipping.common.core.ModBlocks;
import de.luaxlab.shipping.common.core.ModCommon;
import de.luaxlab.shipping.common.entity.SpringEntity;
import de.luaxlab.shipping.common.entity.SpringableEntity;
import de.luaxlab.shipping.common.entity.accessor.DataAccessor;
import de.luaxlab.shipping.common.entity.vehicle.VesselEntity;
import de.luaxlab.shipping.common.item.TugRouteItem;
import de.luaxlab.shipping.common.navigation.TugPathNavigator;
import de.luaxlab.shipping.common.util.SmartInternalInventory;
import de.luaxlab.shipping.common.util.Train;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public abstract class AbstractTugEntity extends VesselEntity implements SpringableEntity, Inventory, SidedInventory {

    public static final int TUG_ROUTE_ITEM_SLOT = 0;

    // CONTAINER STUFF
    protected final SmartInternalInventory itemHandler = createHandler();
    protected boolean contentsChanged = false;
    protected boolean docked = false;
    private int dockCheckCooldown = 0;
    private boolean independentMotion = false;
    private static final TrackedData<Boolean> INDEPENDENT_MOTION = DataTracker.registerData(AbstractTugEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public boolean allowDockInterface(){
        return isDocked();
    }

    private TugDummyHitboxEntity extraHitbox = null;

    private List<Vector2f> path;
    private int nextStop;


    public AbstractTugEntity(EntityType<? extends WaterCreatureEntity> type, World world) {
        super(type, world);
        this.intersectionChecked = true;
        this.train = new Train(this);
        this.path = new ArrayList<>();
    }

    public AbstractTugEntity(EntityType type, World worldIn, Vec3d position) {
        this(type, worldIn);
        this.setPosition(position);
        /*this.xo = x;
        this.yo = y;
        this.zo = z;*/ //Whatever this does
    }

    // CONTAINER STUFF
    @Override
    public void detachLeash(boolean sendPacket, boolean dropItem) {
        navigation.recalculatePath();
        super.detachLeash(sendPacket, dropItem);
    }

    public abstract DataAccessor getDataAccessor();

    /*@Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }

        return super.getCapability(cap, side);
    }*/ // No capability system in fabric

    @Override
    public boolean isPushedByFluids() {
        return true;
    }

    private SmartInternalInventory createHandler() {
        return new SmartInternalInventory(1 + getNonRouteItemSlots()) {

            @Override
            public void markDirty() {
                contentsChanged = true;
            }

            @Override
            public boolean isValid(int slot, ItemStack stack) {
                switch (slot) {
                    case 0: // route
                        return stack.getItem() == ModCommon.ITEM_TUG_ROUTE;
                    default: // up to children
                        return isTugSlotItemValid(slot, stack);
                }
            }

            @Override
            public int getMaxCountPerStack(int slot) {
                switch (slot) {
                    case 0: // route
                        return 1;
                    default: // up to children
                        return getTugSlotLimit(slot);
                }
            }

        };
    }

    protected abstract int getNonRouteItemSlots();

    protected boolean isTugSlotItemValid(int slot, @Nonnull ItemStack stack){
        return false;
    }

    protected int getTugSlotLimit(int slot){
        return 0;
    }

    public TugDummyHitboxEntity getDummyHitbox(){
        return extraHitbox;
    }

    protected abstract NamedScreenHandlerFactory createContainerProvider();


    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.put("inv", itemHandler.toNbtList());
        nbt.putInt("next_stop", nextStop);
        super.writeCustomDataToNbt(nbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        itemHandler.readNbt(nbt.getCompound("inventory"));
        nextStop = nbt.contains("next_stop") ? nbt.getInt("next_stop") : 0;
        contentsChanged = true;
        extraHitbox = null;
        super.readCustomDataFromNbt(nbt);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.put("inventory", itemHandler.writeNbt(new NbtCompound()));
        nbt.putInt("next_stop", nextStop);
        return super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        itemHandler.readNbtList(nbt.getList("inv", 0));
        nextStop = nbt.contains("next_stop") ? nbt.getInt("next_stop") : 0;
        contentsChanged = true;
        extraHitbox = null;
        super.readNbt(nbt);
    }

    private void tickRouteCheck() {
        if (contentsChanged) {
            //TODO: Log
            LOGGER.info("Tug route changed");
            ItemStack stack = itemHandler.getStack(TUG_ROUTE_ITEM_SLOT);
            this.setPath(TugRouteItem.getRoute(stack));
        }
    }

    protected abstract boolean tickFuel();

    public static DefaultAttributeContainer.Builder setCustomAttributes() {
        return VesselEntity.setCustomAttributes()
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 200);
    }


    // MOB STUFF

    private List<Direction> getSideDirections() {
        return this.getMovementDirection() == Direction.NORTH || this.getMovementDirection() == Direction.SOUTH ?
                Arrays.asList(Direction.EAST, Direction.WEST) :
                Arrays.asList(Direction.NORTH, Direction.SOUTH);
    }


    private void tickCheckDock() {
        int x = (int) Math.floor(this.getX());
        int y = (int) Math.floor(this.getY());
        int z = (int) Math.floor(this.getZ());

        if (this.docked && dockCheckCooldown > 0){
            dockCheckCooldown--;
            this.setVelocity(Vec3d.ZERO);
            this.updatePosition(x + 0.5 ,getY(),z + 0.5);

            return;
        }

        // Check docks
        this.docked = this.getSideDirections()
                .stream()
                .map((curr) ->
                    Optional.ofNullable(world.getBlockEntity(new BlockPos(x + curr.getOffsetX(), y, z + curr.getOffsetY())))
                            .filter(entity -> entity instanceof TugDockBlockEntity)
                            .map(entity -> (TugDockBlockEntity) entity)
                            .map(dock -> dock.holdVessel(this, curr))
                            .orElse(false))
                .reduce(false, (acc, curr) -> acc || curr);

        if(this.docked) {
            dockCheckCooldown = 20; // todo: magic number
            this.setVelocity(Vec3d.ZERO);
            this.updatePosition(x + 0.5 ,getY(),z + 0.5);
        } else {
            dockCheckCooldown = 0;
        }
    }

    @Override
    public boolean canBreatheInWater() {
        return true;
    }

    protected void makeSmoke() {
        //TODO: Client server breach?
        //TODO: Optimizable in forge edition?
        if (world != null) {
            BlockPos blockpos = this.getBlockPos().up(2);
            Random random = world.random;
            if (random.nextFloat() < ShippingConfig.Client.TUG_SMOKE_MODIFIER) {
                for(int i = 0; i < random.nextInt(2) + 2; ++i) {
                    makeParticles(world, blockpos, true, false);
                }
            }
        }
    }

    public static void makeParticles(World p_220098_0_, BlockPos p_220098_1_, boolean p_220098_2_, boolean p_220098_3_) {
        Random random = p_220098_0_.getRandom();
        Supplier<Boolean> h = () -> random.nextDouble() < 0.5;
        ParticleEffect basicparticletype = p_220098_2_ ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE;
        double xdrift = (h.get() ? 1 : -1) * random.nextDouble() * 2;
        double zdrift = (h.get() ? 1 : -1) * random.nextDouble() * 2;

        p_220098_0_.addImportantParticle(basicparticletype, true, (double)p_220098_1_.getX() + 0.5D + random.nextDouble() / 3.0D * (double)(random.nextBoolean() ? 1 : -1), (double)p_220098_1_.getY() + random.nextDouble() + random.nextDouble(), (double)p_220098_1_.getZ() + 0.5D + random.nextDouble() / 3.0D * (double)(random.nextBoolean() ? 1 : -1), 0.007D * xdrift, 0.05D, 0.007D * zdrift);
    }

    @Override
    protected EntityNavigation createNavigation(World p_175447_1_) {
        return new TugPathNavigator(this, p_175447_1_);
    }


    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!player.world.isClient()) {
            //TODO: Open GUI
            //NetworkHooks.openGui((ServerPlayerEntity) player, createContainerProvider(), getDataAccessor()::write);

            NamedScreenHandlerFactory factory = createContainerProvider();
            if(factory != null)
                player.openHandledScreen(factory);

            for (int i = 0; i < size(); i++) {
                //player.sendMessage(new LiteralText(i + ": " + getStack(i).toString()), false);
            }
        }
        // don't open GUI *and* use item in hand
        return ActionResult.CONSUME;
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        if(world.isClient) {
            if(INDEPENDENT_MOTION.equals(data)) {
                independentMotion = dataTracker.get(INDEPENDENT_MOTION);
            }
        }
    }


    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new MovementGoal());
    }


    class MovementGoal extends Goal {
        @Override
        public boolean canStart() {
            return AbstractTugEntity.this.path != null;
        }

        public void tick() {
            if(!AbstractTugEntity.this.world.isClient) {
                tickRouteCheck();
                tickCheckDock();
                followPath();
                followGuideRail();
                contentsChanged = false;
            }

        }
    }

    public void tick() {
        if(!world.isClient){
            if (extraHitbox == null || !extraHitbox.isAlive()){
                this.extraHitbox = new TugDummyHitboxEntity(this);
                world.spawnEntity(this.extraHitbox);
            }
            extraHitbox.updatePosition();
        }

        if(this.world.isClient
                && independentMotion){
            makeSmoke();
        }

        super.tick();

    }

    private void followGuideRail(){
        List<BlockState> belowList = Arrays.asList(this.world.getBlockState(getBlockPos().down()),
                this.world.getBlockState(getBlockPos().down(2)));
        BlockState water = this.world.getBlockState(getBlockPos());

        for (BlockState below : belowList) {
            if (below.getBlock()== ModBlocks.BLOCK_GUIDE_TUG && water.getBlock() == Blocks.WATER) {
                Direction arrows = TugGuideRailBlock.getArrowsDirection(below);
                this.setYaw(arrows.asRotation());
                double modifier = 0.03;
                this.setVelocity(this.getVelocity().add(
                        new Vec3d(arrows.getOffsetX() * modifier, 0, arrows.getOffsetZ() * modifier)));
            }
        }
    }

    // todo: someone said you could prevent mobs from getting stuck on blocks by override this
    /*@Override
    protected void customServerAiStep() {
        super.customServerAiStep();
    }*/

    private void followPath() {
        if (!this.path.isEmpty() && !this.docked && tickFuel()) {
            //LOGGER.info("We move!");
            Vector2f stop = path.get(nextStop);
            //TODO: Serious bug here??
            //navigation.moveTo(stop.x, this.getY(), stop.y, 10);
            navigation.startMovingTo(stop.getX(), this.getY(), stop.getY(), 10);
            double distance = Math.abs(Math.hypot(this.getX() - (stop.getX() + 0.5), this.getZ() - (stop.getY() + 0.5)));
            independentMotion = true;
            dataTracker.set(INDEPENDENT_MOTION, true);

            if (distance < 0.6) {
                incrementStop();
            }

        } else{
            dataTracker.set(INDEPENDENT_MOTION, false);

            if (this.path.isEmpty()){
                this.nextStop = 0;
            }
        }
    }
    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(INDEPENDENT_MOTION, false);
    }

    public void setPath(List<Vector2f> path) {
        this.path = path;
    }

    private void incrementStop() {
        if (this.path.size() == 1) {
            nextStop = 0;
        } else if (!this.path.isEmpty()) {
            nextStop = (nextStop + 1) % (this.path.size());
        }
    }

    @Override
    public void setDominated(SpringableEntity entity, SpringEntity spring) {
        this.dominated = Optional.of(new Pair<>(entity, spring));
    }

    @Override
    public void setDominant(SpringableEntity entity, SpringEntity spring) {

    }

    @Override
    public void removeDominated() {
        this.dominated = Optional.empty();
        this.train.setTail(this);
    }

    @Override
    public void removeDominant() {

    }

    @Override
    public void setTrain(Train train) {
        this.train = train;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else if (!this.world.isClient && !this.isRemoved()) {
            //TODO: Improvement: Drop no item if creative player
            if(!source.isSourceCreativePlayer() || source.isOutOfWorld())
                this.dropItem(this.getDropItem());
            this.remove(RemovalReason.KILLED);
            return true;
        } else {
            return true;
        }

    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.world.isClient) {
            ItemScatterer.spawn(this.world, this, this);
        }
        handleSpringableKill();
        super.remove(reason);
    }

    // Have to implement IInventory to work with hoppers


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

    public boolean canPlaceItem(int p_94041_1_, ItemStack p_94041_2_) {
        return true;
    }

    @Override
    public void markDirty() {
        contentsChanged = true;
    }


    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        //TODO: Forge version optimization
        return !isRemoved() && player.squaredDistanceTo(this) > 64.0D;
    }

    @Override
    public int size() {
        return 1 + getNonRouteItemSlots();
    }

    @Override
    public Item getDropItem() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return itemHandler.isEmpty();
    }


    @Override
    public void setStack(int slot, ItemStack stack) {
        itemHandler.setStack(slot, stack);
    }

    @Override
    public void clear() {
        //Nothing here on purpose
    }

    //Sided inventory
    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return IntStream.range(1, size()).toArray();
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @org.jetbrains.annotations.Nullable Direction dir) {
        return true;
    }

    public boolean isDocked(){
        return docked;
    }

    @Override
    public boolean canBeLeashedBy(PlayerEntity player) {
        return true;
    }


}
