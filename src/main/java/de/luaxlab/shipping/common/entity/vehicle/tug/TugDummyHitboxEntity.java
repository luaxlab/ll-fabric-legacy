package de.luaxlab.shipping.common.entity.vehicle.tug;

import de.luaxlab.shipping.common.core.ModCommon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TugDummyHitboxEntity extends Entity {
    private AbstractTugEntity tugEntity;
    public static final TrackedData<Integer> TUG_ID = DataTracker.registerData(TugDummyHitboxEntity.class, TrackedDataHandlerRegistry.INTEGER);


    public TugDummyHitboxEntity(EntityType<? extends TugDummyHitboxEntity> type, World world) {
        super(type, world);
    }


    public TugDummyHitboxEntity(AbstractTugEntity tugEntity) {
        this(ModCommon.ENTITY_DUMMY_TUG_HITBOX, tugEntity.world);
        this.tugEntity = tugEntity;
        this.setVelocity(Vec3d.ZERO);
        updatePosition();
        //TODO: Find out what this does....
        /*this.xo = getX();
        this.yo = getY();
        this.zo = getZ();*/
    }

    public AbstractTugEntity getTug(){
        return tugEntity;
    }

    @Override
    public void tick(){
        if(this.world.isClient && tugEntity == null){
            setTug();
        }
        if(!this.world.isClient) {
            if (tugEntity == null || !tugEntity.isAlive()) {
                this.kill();
            } else {
                TugDummyHitboxEntity p = tugEntity.getDummyHitbox();
                if (p != null && !p.equals(this)) {
                    this.kill();
                } else {
                    dataTracker.set(TUG_ID, tugEntity.getId());
                }
            }
        }
    }

    public void updatePosition(){
        double x = tugEntity.getX() + tugEntity.getMovementDirection().getOffsetX() * 0.7;
        double z = tugEntity.getZ() + tugEntity.getMovementDirection().getOffsetZ() * 0.7;
        double y = tugEntity.getY();
        this.updatePosition(x, y, z);
    }

    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public ActionResult interact(PlayerEntity p_184230_1_, Hand p_184230_2_) {
        return tugEntity.interact(p_184230_1_, p_184230_2_);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return tugEntity.damage(source,amount);
    }



    @Override
    protected void initDataTracker() {
        dataTracker.startTracking(TUG_ID, -1);
    }


    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        if(world.isClient) {
            if(TUG_ID.equals(data)) {
                setTug();
            }
        }
    }

    private void setTug() {
        Entity potential = world.getEntityById(dataTracker.get(TUG_ID));
        if(potential instanceof AbstractTugEntity){
            tugEntity = (AbstractTugEntity) potential;
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.remove(RemovalReason.DISCARDED);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this, tugEntity != null ? tugEntity.getId() : 0);
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        Entity entity = this.world.getEntityById(packet.getEntityData());
        if(entity instanceof AbstractTugEntity)
            tugEntity = (AbstractTugEntity) entity;
    }
}
