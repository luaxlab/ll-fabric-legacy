package de.luaxlab.shipping.common.entity.vehicle.barge;


import com.mojang.datafixers.util.Pair;
import de.luaxlab.shipping.common.entity.SpringEntity;
import de.luaxlab.shipping.common.entity.SpringableEntity;
import de.luaxlab.shipping.common.entity.vehicle.VesselEntity;
import de.luaxlab.shipping.common.util.Train;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class AbstractBargeEntity extends VesselEntity implements SpringableEntity {

    public AbstractBargeEntity(EntityType<? extends AbstractBargeEntity> type, World world) {
        super(type, world);
        this.intersectionChecked = true;
        this.train = new Train(this);
    }

    public AbstractBargeEntity(EntityType type, World worldIn, Vec3d position) {
        this(type, worldIn);
        this.setPosition(position);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    public abstract Item getDropItem();


    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!this.world.isClient) {
            doInteract(player);
        }
        // don't interact *and* use current item
        return ActionResult.CONSUME;
    }

    abstract protected void doInteract(PlayerEntity player);

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else if (!this.world.isClient && !this.isRemoved()) {
            this.dropItem(this.getDropItem());
            this.remove(RemovalReason.KILLED);
            return true;
        } else {
            return true;
        }
    }

    public boolean hasWaterOnSides(){
        return this.world.getBlockState(this.getLandingPos().offset(this.getMovementDirection().rotateYClockwise())).getBlock().equals(Blocks.WATER) &&
                this.world.getBlockState(this.getLandingPos().offset(this.getMovementDirection().rotateYCounterclockwise())).getBlock().equals(Blocks.WATER) &&
                this.world.getBlockState(this.getLandingPos().up().offset(this.getMovementDirection().rotateYClockwise())).getBlock().equals(Blocks.AIR) &&
                this.world.getBlockState(this.getLandingPos().up().offset(this.getMovementDirection().rotateYCounterclockwise())).getBlock().equals(Blocks.AIR);
    }

    @Override
    public void setDominated(SpringableEntity entity, SpringEntity spring) {
        this.dominated = Optional.of(new Pair<>(entity, spring));
    }

    @Override
    public void setDominant(SpringableEntity entity, SpringEntity spring) {
        this.setTrain(entity.getTrain());
        this.dominant = Optional.of(new Pair<>(entity, spring));
    }

    @Override
    public void removeDominated() {
        if(!this.isAlive()){
            return;
        }
        this.dominated = Optional.empty();
        this.train.setTail(this);
    }

    @Override
    public void removeDominant() {
        if(!this.isAlive()){
            return;
        }
        this.dominant = Optional.empty();
        this.setTrain(new Train(this));
    }

    @Override
    public void setTrain(Train train) {
        this.train = train;
        train.setTail(this);
        dominated.ifPresent(dominated -> {
            // avoid recursion loops
            if(!dominated.getFirst().getTrain().equals(train)){
                dominated.getFirst().setTrain(train);
            }
        });
    }

    @Override
    public void remove(RemovalReason reason) {
        handleSpringableKill();
        super.remove(reason);
    }

    // hack to disable hoppers
    public boolean isDockable() {
        return this.dominant.map(dom -> this.squaredDistanceTo((Entity) dom.getFirst()) < 1.1).orElse(true);
    }

    @Override
    public boolean allowDockInterface(){
        return isDockable();
    }
}
