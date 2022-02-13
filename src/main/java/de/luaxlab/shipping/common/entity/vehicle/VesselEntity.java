package de.luaxlab.shipping.common.entity.vehicle;

import com.mojang.datafixers.util.Pair;
import de.luaxlab.shipping.common.config.ShippingConfig;
import de.luaxlab.shipping.common.entity.SpringEntity;
import de.luaxlab.shipping.common.entity.SpringableEntity;
import de.luaxlab.shipping.common.util.Train;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

public abstract class VesselEntity extends WaterCreatureEntity implements SpringableEntity {
    protected VesselEntity(EntityType<? extends WaterCreatureEntity> type, World world) {
        super(type, world);
        stuckCounter = 0;
        resetSpeedAttributes();
        setSpeedAttributes(ShippingConfig.Common.TUG_BASE_SPEED);
    }

    // MOB STUFF
    private float invFriction;
    private int stuckCounter;
    private double waterLevel;
    private float landFriction;
    private BoatEntity.Location status;
    private BoatEntity.Location oldStatus;
    private double lastYd;
    private Optional<Pair<BlockPos, BlockState>> lastCornerGuideRail = Optional.empty();

    protected Optional<Pair<SpringableEntity, SpringEntity>> dominated = Optional.empty();
    protected Optional<Pair<SpringableEntity, SpringEntity>> dominant = Optional.empty();
    protected Train train;

    /*@Override
    public boolean isPickable(){
        return true;
    }*/
    //TODO: It is pickable


    public abstract boolean allowDockInterface();

    public boolean hasWaterOnSides(){
        return this.world.getBlockState(this.getLandingPos().offset(this.getMovementDirection().rotateYClockwise())).getBlock().equals(Blocks.WATER) &&
                this.world.getBlockState(this.getLandingPos().offset(this.getMovementDirection().rotateYCounterclockwise())).getBlock().equals(Blocks.WATER) &&
                this.world.getBlockState(this.getLandingPos().up().offset(this.getMovementDirection().rotateYClockwise())).getBlock().equals(Blocks.AIR) &&
                this.world.getBlockState(this.getLandingPos().up().offset(this.getMovementDirection().rotateYCounterclockwise())).getBlock().equals(Blocks.AIR);
    }

    @Override
    public void tick() {
        if(this.isAlive()) {
            tickSpringAliveCheck();
        }
        this.oldStatus = this.status;
        this.status = this.getStatus();

        this.floatBoat();
        this.unDrown();

        super.tick();
    }

    private void unDrown(){
        if(world.getBlockState(getLandingPos().up()).getBlock().equals(Blocks.WATER)){
            //TODO: Improvement, removed z velocity
            this.setVelocity(this.getVelocity().add(new Vec3d(0, 0.1, 0)));
        }

    }

    public static DefaultAttributeContainer.Builder setCustomAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0D);
        //TODO: Swim speed
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        // override speed attributes on load from previous versions
        resetSpeedAttributes();
    }

    // reset speed to 1
    private void resetSpeedAttributes() {
        this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
        //this.getAttribute(ForgeMod.SWIM_SPEED.get()).setBaseValue(0);
        //TODO: Swim speed
    }

    private void setSpeedAttributes(double speed) {
        this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                .addTemporaryModifier(
                        new EntityAttributeModifier("movementspeed_mult", speed, EntityAttributeModifier.Operation.ADDITION));
        /*this.getAttribute(ForgeMod.SWIM_SPEED.get())
                .addTransientModifier(
                        new AttributeModifier("swimspeed_mult", speed, AttributeModifier.Operation.ADDITION));*/
        //TODO: Swim speed
    }

    @Override
    protected void tickWaterBreathingAir(int air) {
        this.setAir(300);
    }

    public abstract Item getDropItem();

    /*
    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return new ItemStack(this.getDropItem());
    }*/
    //TODO: Forge



    @Override
    public Optional<Pair<SpringableEntity, SpringEntity>> getDominated() {
        return this.dominated;
    }

    @Override
    public Optional<Pair<SpringableEntity, SpringEntity>> getDominant() {
        return this.dominant;
    }

    @Override
    public Train getTrain() {
        return this.train;
    }

    @Override
    public void checkDespawn() {

    }


    private void floatBoat() {
        double d0 = (double) -0.04F;
        double d1 = this.hasNoGravity() ? 0.0D : (double) -0.04F;
        double d2 = 0.0D;
        this.invFriction = 0.05F;
        if (this.oldStatus == BoatEntity.Location.IN_AIR && this.status != BoatEntity.Location.IN_AIR && this.status != BoatEntity.Location.ON_LAND) {
            this.waterLevel = this.getBodyY(1.0D);
            this.setPos(this.getX(), (double) (this.getWaterLevelAbove() - this.getHeight()) + 0.101D, this.getZ());
            this.setVelocity(this.getVelocity().multiply(1.0D, 0.0D, 1.0D));
            this.lastYd = 0.0D;
            this.status = BoatEntity.Location.IN_WATER;
        } else {
            if (this.status == BoatEntity.Location.IN_WATER) {
                d2 = (this.waterLevel - this.getY()) / (double) this.getHeight();
                this.invFriction = 0.9F;
            } else if (this.status == BoatEntity.Location.UNDER_FLOWING_WATER) {
                d1 = -7.0E-4D;
                this.invFriction = 0.9F;
            } else if (this.status == BoatEntity.Location.UNDER_WATER) {
                d2 = (double) 0.01F;
                this.invFriction = 0.45F;
            } else if (this.status == BoatEntity.Location.IN_AIR) {
                this.invFriction = 0.9F;
            } else if (this.status == BoatEntity.Location.ON_LAND) {
                this.invFriction = this.landFriction;
                if (this.getVehicle() instanceof PlayerEntity) {
                    this.landFriction /= 2.0F;
                }
            }

            Vec3d vector3d = this.getVelocity();
            this.setVelocity(vector3d.x * (double) this.invFriction, vector3d.y + d1, vector3d.z * (double) this.invFriction);
            if (d2 > 0.0D) {
                Vec3d vector3d1 = this.getVelocity();
                this.setVelocity(vector3d1.x, (vector3d1.y + d2 * 0.06153846016296973D) * 0.75D, vector3d1.z);
            }
        }

    }


    public float getWaterLevelAbove() {
        Box axisalignedbb = this.getBoundingBox();
        int i = MathHelper.floor(axisalignedbb.minX);
        int j = MathHelper.ceil(axisalignedbb.maxX);
        int k = MathHelper.floor(axisalignedbb.maxY);
        int l = MathHelper.ceil(axisalignedbb.maxY - this.lastYd);
        int i1 = MathHelper.floor(axisalignedbb.minZ);
        int j1 = MathHelper.ceil(axisalignedbb.maxZ);
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        label39:
        for (int k1 = k; k1 < l; ++k1) {
            float f = 0.0F;

            for (int l1 = i; l1 < j; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    blockpos$mutable.set(l1, k1, i2);
                    FluidState fluidstate = this.world.getFluidState(blockpos$mutable);
                    if (fluidstate.isOf(Fluids.WATER)) {
                        f = Math.max(f, fluidstate.getHeight(this.world, blockpos$mutable));
                    }

                    if (f >= 1.0F) {
                        continue label39;
                    }
                }
            }

            if (f < 1.0F) {
                return (float) blockpos$mutable.getY() + f;
            }
        }

        return (float) (l + 1);
    }

    private BoatEntity.Location getStatus() {
        BoatEntity.Location boatentity$status = this.isUnderwater();
        if (boatentity$status != null) {
            this.waterLevel = this.getBoundingBox().maxY;
            return boatentity$status;
        } else if (this.checkInWater()) {
            return BoatEntity.Location.IN_WATER;
        } else {
            float f = this.getGroundFriction();
            if (f > 0.0F) {
                this.landFriction = f;
                return BoatEntity.Location.ON_LAND;
            } else {
                return BoatEntity.Location.IN_AIR;
            }
        }
    }

    public float getGroundFriction() {
        Box axisalignedbb = this.getBoundingBox();
        Box axisalignedbb1 = new Box(axisalignedbb.minX, axisalignedbb.minY - 0.001D, axisalignedbb.minZ, axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ);
        int i = MathHelper.floor(axisalignedbb1.minX) - 1;
        int j = MathHelper.ceil(axisalignedbb1.maxX) + 1;
        int k = MathHelper.floor(axisalignedbb1.minY) - 1;
        int l = MathHelper.ceil(axisalignedbb1.maxY) + 1;
        int i1 = MathHelper.floor(axisalignedbb1.minZ) - 1;
        int j1 = MathHelper.ceil(axisalignedbb1.maxZ) + 1;
        VoxelShape voxelshape = VoxelShapes.cuboid(axisalignedbb1);
        float f = 0.0F;
        int k1 = 0;
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for (int l1 = i; l1 < j; ++l1) {
            for (int i2 = i1; i2 < j1; ++i2) {
                int j2 = (l1 != i && l1 != j - 1 ? 0 : 1) + (i2 != i1 && i2 != j1 - 1 ? 0 : 1);
                if (j2 != 2) {
                    for (int k2 = k; k2 < l; ++k2) {
                        if (j2 <= 0 || k2 != k && k2 != l - 1) {
                            blockpos$mutable.set(l1, k2, i2);
                            BlockState blockstate = this.world.getBlockState(blockpos$mutable);
                            if (!(blockstate.getBlock() instanceof LilyPadBlock) && VoxelShapes.matchesAnywhere(blockstate.getCollisionShape(this.world, blockpos$mutable).offset((double) l1, (double) k2, (double) i2), voxelshape, BooleanBiFunction.AND)) {
                                f += blockstate.getBlock().getSlipperiness();
                                ++k1;
                            }
                        }
                    }
                }
            }
        }

        return f / (float) k1;
    }

    private boolean checkInWater() {
        Box axisalignedbb = this.getBoundingBox();
        int i = MathHelper.floor(axisalignedbb.minX);
        int j = MathHelper.ceil(axisalignedbb.maxX);
        int k = MathHelper.floor(axisalignedbb.minY);
        int l = MathHelper.ceil(axisalignedbb.minY + 0.001D);
        int i1 = MathHelper.floor(axisalignedbb.minZ);
        int j1 = MathHelper.ceil(axisalignedbb.maxZ);
        boolean flag = false;
        this.waterLevel = Double.MIN_VALUE;
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    blockpos$mutable.set(k1, l1, i2);
                    FluidState fluidstate = this.world.getFluidState(blockpos$mutable);
                    if (fluidstate.isOf(Fluids.WATER)) {
                        float f = (float) l1 + fluidstate.getHeight(this.world, blockpos$mutable);
                        this.waterLevel = Math.max((double) f, this.waterLevel);
                        flag |= axisalignedbb.minY < (double) f;
                    }
                }
            }
        }

        return flag;
    }

    @Nullable
    private BoatEntity.Location isUnderwater() {
        Box axisalignedbb = this.getBoundingBox();
        double d0 = axisalignedbb.maxY + 0.001D;
        int i = MathHelper.floor(axisalignedbb.minX);
        int j = MathHelper.ceil(axisalignedbb.maxX);
        int k = MathHelper.floor(axisalignedbb.maxY);
        int l = MathHelper.ceil(d0);
        int i1 = MathHelper.floor(axisalignedbb.minZ);
        int j1 = MathHelper.ceil(axisalignedbb.maxZ);
        boolean flag = false;
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    blockpos$mutable.set(k1, l1, i2);
                    FluidState fluidstate = this.world.getFluidState(blockpos$mutable);
                    if (fluidstate.isOf(Fluids.WATER) && d0 < (double) ((float) blockpos$mutable.getY() + fluidstate.getHeight(this.world, blockpos$mutable))) {
                        if (!fluidstate.isStill()) {
                            return BoatEntity.Location.UNDER_FLOWING_WATER;
                        }

                        flag = true;
                    }
                }
            }
        }

        return flag ? BoatEntity.Location.UNDER_WATER : null;
    }

    /*@Override
    protected void jumpInLiquid(ITag<Fluid> p_180466_1_) {
        if (this.getNavigation().canFloat()) {
            super.jumpInLiquid(p_180466_1_);
        } else {
            this.setVelocity(this.getVelocity().add(0.0D, 0.3D, 0.0D));
        }
    }*/
    //TODO


    @Override
    public boolean isInvulnerableTo(DamageSource p_180431_1_) {
        return p_180431_1_.equals(DamageSource.IN_WALL) || super.isInvulnerableTo(p_180431_1_);
    }

    // LivingEntity override, to avoid jumping out of water
    @Override
    public void travel(Vec3d movementInput) {
        if (this.canMoveVoluntarily() || this.isLogicalSideForUpdatingMovement()) {
            double d0 = 0.08D;
            //TODO: Forge modifiable gravity
            boolean flag = this.getVelocity().y <= 0.0D;

            FluidState fluidstate = this.world.getFluidState(this.getBlockPos());
            if (this.isTouchingWater() && this.shouldSwimInFluids() && !this.canWalkOnFluid(fluidstate.getFluid())) {
                double d8 = this.getY();
                float f5 = this.isSprinting() ? 0.9F : this.getBaseMovementSpeedMultiplier();
                float f6 = 0.02F;
                float f7 = 0;
                if (f7 > 3.0F) {
                    f7 = 3.0F;
                }

                if (!this.onGround) {
                    f7 *= 0.5F;
                }

                if (f7 > 0.0F) {
                    f5 += (0.54600006F - f5) * f7 / 3.0F;
                    f6 += (this.getMovementSpeed() - f6) * f7 / 3.0F;
                }

                if (this.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
                    f5 = 0.96F;
                }

                //TODO: Forge swim speed
                //f6 *= (float) this.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()).getValue();
                this.updateVelocity(f6, movementInput);
                this.move(MovementType.SELF, this.getVelocity());
                Vec3d vector3d6 = this.getVelocity();
                if (this.horizontalCollision && this.isClimbing()) {
                    vector3d6 = new Vec3d(vector3d6.x, 0.2D, vector3d6.z);
                }

                this.setVelocity(vector3d6.multiply((double) f5, (double) 0.8F, (double) f5));
                // getFluidFallingAdjustedMovement
                Vec3d vector3d2 = this.method_26317(d0, flag, this.getVelocity());
                this.setVelocity(vector3d2);
                if (this.horizontalCollision) {
                    if (stuckCounter > 10) {
                        // destroy lilypads
                        Direction direction = getMovementDirection();
                        BlockPos front = getBlockPos().offset(direction).up();
                        BlockPos left = front.offset(direction.rotateYClockwise());
                        BlockPos right = front.offset(direction.rotateYCounterclockwise());
                        for (BlockPos pos : Arrays.asList(front, left, right)){
                            BlockState state = this.world.getBlockState(pos);
                            if (state.isOf(Blocks.LILY_PAD)){
                                this.world.removeBlock(pos, true);
                            }
                        }
                        stuckCounter = 0;
                    } else {
                        stuckCounter++;
                    }
                } else {
//                    stuckCounter = 0;
                }
            } else if (this.isInLava() && this.shouldSwimInFluids() && !this.canWalkOnFluid(fluidstate.getFluid())) {
                double d7 = this.getY();
                this.updateVelocity(0.02F, movementInput);
                this.move(MovementType.SELF, this.getVelocity());
                if (this.getFluidHeight(FluidTags.LAVA) <= this.getSwimHeight()) {
                    this.setVelocity(this.getVelocity().multiply(0.5D, (double) 0.8F, 0.5D));
                    //getFluidFallingAdjustedMovement => method_26317
                    Vec3d vector3d3 = this.method_26317(d0, flag, this.getVelocity());
                    this.setVelocity(vector3d3);
                } else {
                    this.setVelocity(this.getVelocity().multiply(0.5D));
                }

                if (!this.hasNoGravity()) {
                    this.setVelocity(this.getVelocity().add(0.0D, -d0 / 4.0D, 0.0D));
                }

                Vec3d vector3d4 = this.getVelocity();
                if (this.horizontalCollision && this.doesNotCollide(vector3d4.x, vector3d4.y + (double) 0.6F - this.getY() + d7, vector3d4.z)) {
                    this.setVelocity(vector3d4.x, (double) 0.3F, vector3d4.z);
                }
            } else if (this.isFallFlying()) {
                Vec3d vector3d = this.getVelocity();
                if (vector3d.y > -0.5D) {
                    this.fallDistance = 1.0F;
                }

                Vec3d vector3d1 = this.getRotationVector();
                float f = this.getPitch() * ((float) Math.PI / 180F);
                double d1 = Math.sqrt(vector3d1.x * vector3d1.x + vector3d1.z * vector3d1.z);
                double d3 = vector3d.horizontalLength();

                double d4 = vector3d1.length();
                float f1 = MathHelper.cos(f);
                f1 = (float) ((double) f1 * (double) f1 * Math.min(1.0D, d4 / 0.4D));
                vector3d = this.getVelocity().add(0.0D, d0 * (-1.0D + (double) f1 * 0.75D), 0.0D);
                if (vector3d.y < 0.0D && d1 > 0.0D) {
                    double d5 = vector3d.y * -0.1D * (double) f1;
                    vector3d = vector3d.add(vector3d1.x * d5 / d1, d5, vector3d1.z * d5 / d1);
                }

                if (f < 0.0F && d1 > 0.0D) {
                    double d9 = d3 * (double) (-MathHelper.sin(f)) * 0.04D;
                    vector3d = vector3d.add(-vector3d1.x * d9 / d1, d9 * 3.2D, -vector3d1.z * d9 / d1);
                }

                if (d1 > 0.0D) {
                    vector3d = vector3d.add((vector3d1.x / d1 * d3 - vector3d.x) * 0.1D, 0.0D, (vector3d1.z / d1 * d3 - vector3d.z) * 0.1D);
                }

                this.setVelocity(vector3d.multiply((double) 0.99F, (double) 0.98F, (double) 0.99F));
                this.move(MovementType.SELF, this.getVelocity());
                if (this.horizontalCollision && !this.world.isClient) {
                    double d10 = this.getVelocity().horizontalLength();
                    double d6 = d3 - d10;
                    float f2 = (float) (d6 * 10.0D - 3.0D);
                    if (f2 > 0.0F) {
                        //TODO access widener with getFallSound
                        this.playSound(f2 > 4 ? this.getFallSounds().big() : this.getFallSounds().small(), 1.0F, 1.0F);
                        this.damage(DamageSource.FLY_INTO_WALL, f2);
                    }
                }

                if (this.onGround && !this.world.isClient) {
                    this.setFlag(7, false);
                }
            } else {
                BlockPos blockpos = this.getVelocityAffectingPos();
                float f3 = this.world.getBlockState(blockpos).getBlock().getSlipperiness();
                float f4 = this.onGround ? f3 * 0.91F : 0.91F;
                Vec3d vector3d5 = this.applyMovementInput(movementInput, f3);
                double d2 = vector3d5.y;
                if (this.hasStatusEffect(StatusEffects.LEVITATION)) {
                    d2 += (0.05D * (double) (this.getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1) - vector3d5.y) * 0.2D;
                    this.fallDistance = 0.0F;
                } else if (this.world.isClient && !this.world.isChunkLoaded(blockpos)) {
                    if (this.getY() > 0.0D) {
                        d2 = -0.1D;
                    } else {
                        d2 = 0.0D;
                    }
                } else if (!this.hasNoGravity()) {
                    d2 -= d0;
                }

                this.setVelocity(vector3d5.x * (double) f4, d2 * (double) 0.98F, vector3d5.z * (double) f4);
            }
        }

        this.updateLimbs(this, false);
    }

    public Optional<Pair<BlockPos, BlockState>> getLastCornerGuideRail() {
        return lastCornerGuideRail;
    }

    public void setLastCornerGuideRail(Pair<BlockPos, BlockState> lastCornerGuideRail) {
        this.lastCornerGuideRail = Optional.of(lastCornerGuideRail);
    }
}
