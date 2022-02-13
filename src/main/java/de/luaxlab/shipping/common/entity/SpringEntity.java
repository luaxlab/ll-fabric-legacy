package de.luaxlab.shipping.common.entity;

/*
MIT License

Copyright (c) 2018 Xavier "jglrxavpok" Niochaut
F
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

import de.luaxlab.shipping.common.core.ModCommon;
import de.luaxlab.shipping.common.entity.vehicle.VesselEntity;
import de.luaxlab.shipping.common.entity.vehicle.tug.AbstractTugEntity;
import de.luaxlab.shipping.common.util.BoxUtil;
import de.luaxlab.shipping.common.util.EntitySpringAPI;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SpringEntity extends Entity {

    public static final TrackedData<Integer> DOMINANT_ID = DataTracker.registerData(SpringEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> DOMINATED_ID = DataTracker.registerData(SpringEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private @Nullable
    NbtCompound dominantNBT;
    private @Nullable NbtCompound dominatedNBT;
    @Nullable
    private VesselEntity dominant;
    @Nullable
    private VesselEntity dominated;

    public VesselEntity getDominant(){
        return dominant;
    }

    public void setDominant(VesselEntity dominant){
        if(dominated != null && dominant != null){
            dominant.setDominated(dominated, this);
            dominated.setDominant(dominant, this);
        }
        this.dominant = dominant;
    }

    public void setDominated(VesselEntity dominated){
        if(dominated != null && dominant != null){
            dominant.setDominated(dominated, this);
            dominated.setDominant(dominant, this);
        }
        this.dominated = dominated;
    }

    public Entity getDominated(){
        return dominant;
    }

    public SpringEntity(EntityType<? extends Entity> type, World worldIn) {
        super(type, worldIn);
        setNoGravity(true);
        noClip = true;
    }

    public SpringEntity(@Nonnull VesselEntity dominant, @Nonnull VesselEntity dominatedEntity) {
        super(ModCommon.ENTITY_SPRING, dominant.getWorld());
        setDominant(dominant);
        setDominated(dominatedEntity);
        setPos((dominant.getX() + dominated.getX())/2, (dominant.getY() + dominated.getY())/2, (dominant.getZ() + dominated.getZ())/2);
    }

    @Override
    protected void initDataTracker() {
        dataTracker.startTracking(DOMINANT_ID, -1);
        dataTracker.startTracking(DOMINATED_ID, -1);
    }

    public static Vec3d calculateAnchorPosition(VesselEntity entity, SpringSide side) {
        return EntitySpringAPI.calculateAnchorPosition(entity, side);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        if(world.isClient) {
            if(DOMINANT_ID.equals(data)) {
                VesselEntity potential = (VesselEntity) world.getEntityById(dataTracker.get(DOMINANT_ID));
                if(potential != null) {
                    setDominant(potential);
                }
            }
            if(DOMINATED_ID.equals(data)) {
                VesselEntity potential = (VesselEntity) world.getEntityById(dataTracker.get(DOMINATED_ID));
                if(potential != null) {
                    setDominated(potential);
                }
            }
        }
    }

    @Override
    public Direction getHorizontalFacing() {
        return this.dominated.getHorizontalFacing();
    }

    @Override
    public void baseTick() {
        setVelocity(0, 0, 0);
        super.baseTick();

        if(dominant != null && dominated != null) {
            if(dominated.distanceTo(dominant) > 20 && !this.world.isClient){
                dominated.removeDominant();
                kill();
                return;
            }
            if( ! dominant.isAlive() || ! dominated.isAlive()) {
                kill();
                return;
            }
            setPos((dominant.getX() + dominated.getX())/2, (dominant.getY() + dominated.getY())/2, (dominant.getZ() + dominated.getZ())/2);


            double distSq = dominant.squaredDistanceTo(dominated);
            double maxDstSq = ((SpringableEntity) dominant).getTrain().getTug().map(tug -> tug.isDocked() ? 1 : 1.2).orElse(1.2);
            if(distSq > maxDstSq) {
                Vec3d frontAnchor = calculateAnchorPosition(dominant, SpringSide.DOMINATED);
                Vec3d backAnchor = calculateAnchorPosition(dominated, SpringSide.DOMINANT);
                double dist = Math.sqrt(distSq);
                double dx = (frontAnchor.x - backAnchor.x) / dist;
                double dy = (frontAnchor.y - backAnchor.y) / dist;
                double dz = (frontAnchor.z - backAnchor.z) / dist;
                final double alpha = 0.5;

                float targetYaw = computeTargetYaw(dominated.getYaw(), frontAnchor, backAnchor);
                dominated.setYaw((float) ((alpha * dominated.getYaw() + targetYaw * (1f-alpha)) % 360));
                this.setYaw(dominated.getYaw());
                double k = dominant instanceof AbstractTugEntity ? 0.2 : 0.13;
                double l0 = maxDstSq;
                dominated.setVelocity(k*(dist-l0)*dx, k*(dist-l0)*dy, k*(dist-l0)*dz);
                if(!this.world.isClient) {
                    dominated.getLastCornerGuideRail().ifPresent(pair -> {
                        if (dominated.collidesWithStateAtPos(pair.getFirst(), pair.getSecond())) {
                            ModCommon.BLOCK_GUIDE_CORNER.onEntityCollision(pair.getSecond(), this.world, pair.getFirst(), dominated);
                        }
                    });
                }
            }

            if(!world.isClient) { // send update every tick to ensure client has infos
                dataTracker.set(DOMINANT_ID, dominant.getId());
                dataTracker.set(DOMINATED_ID, dominated.getId());
            } else if (dominant == null){
                onTrackedDataSet(DOMINANT_ID);
            } else if (dominated == null) {
                onTrackedDataSet(DOMINATED_ID);
            }
        } else { // front and back entities have not been loaded yet
            if(dominantNBT != null && dominatedNBT != null) {
                tryToLoadFromNBT(dominantNBT).ifPresent(e -> {
                    setDominant(e);
                    dataTracker.set(DOMINANT_ID, e.getId());
                });
                tryToLoadFromNBT(dominatedNBT).ifPresent(e -> {
                    setDominated(e);
                    dataTracker.set(DOMINATED_ID, e.getId());
                });
            }
            updateClient();
        }
    }

    private void updateClient(){
        if(this.world.isClient) {
            if(this.dominant == null) {
                Entity potential = world.getEntityById(dataTracker.get(DOMINANT_ID));
                if (potential != null) {
                    setDominant((VesselEntity) potential);
                }
            }

            if(this.dominated == null) {
                Entity potential_dominated = world.getEntityById(dataTracker.get(DOMINATED_ID));
                if (potential_dominated != null) {
                    setDominated((VesselEntity) potential_dominated);
                }
            }
        }

    }

    private float computeTargetYaw(Float currentYaw, Vec3d anchorPos, Vec3d otherAnchorPos) {
        float idealYaw = (float) (Math.atan2(otherAnchorPos.x - anchorPos.x, -(otherAnchorPos.z - anchorPos.z)) * (180f/Math.PI));
        float closestDistance = Float.POSITIVE_INFINITY;
        float closest = idealYaw;
        for(int sign : Arrays.asList(-1, 0, 1)) {
            float potentialYaw = idealYaw + sign * 360f;
            float distance = Math.abs(potentialYaw - currentYaw);
            if(distance < closestDistance) {
                closestDistance = distance;
                closest = potentialYaw;
            }
        }
        return closest;
    }

    private Optional<VesselEntity> tryToLoadFromNBT(NbtCompound compound) {
        try {
            BlockPos.Mutable pos = new BlockPos.Mutable();
            pos.set(compound.getInt("X"), compound.getInt("Y"), compound.getInt("Z"));
            String uuid = compound.getString("UUID");
            Box searchBox = new Box(
                    pos.getX() - 2,
                    pos.getY() - 2,
                    pos.getZ() - 2,
                    pos.getX() + 2,
                    pos.getY() + 2,
                    pos.getZ() + 2
            );
            List<Entity> entities = world.getOtherEntities(this, searchBox, e -> e.getUuidAsString().equals(uuid));
            return entities.stream().findFirst().map(e -> (VesselEntity) e);
        } catch (Exception e){
            return Optional.empty();
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if(dominant != null && dominated != null) {
            writeNBT(SpringSide.DOMINANT, dominant, nbt);
            writeNBT(SpringSide.DOMINATED, dominated, nbt);
        } else {
            if(dominantNBT != null)
                nbt.put(SpringSide.DOMINANT.name(), dominantNBT);
            if(dominatedNBT != null)
                nbt.put(SpringSide.DOMINATED.name(), dominatedNBT);
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        dominantNBT = nbt.getCompound(SpringSide.DOMINANT.name());
        dominatedNBT = nbt.getCompound(SpringSide.DOMINATED.name());
    }

    private void writeNBT(SpringSide side, Entity entity, NbtCompound globalCompound) {
        NbtCompound compound = new NbtCompound();
        compound.putInt("X", (int)Math.floor(entity.getX()));
        compound.putInt("Y", (int)Math.floor(entity.getY()));
        compound.putInt("Z", (int)Math.floor(entity.getZ()));

        compound.putString("UUID", entity.getUuid().toString());

        globalCompound.put(side.name(), compound);
    }

    /*@Override
    public void writeSpawnData(PacketByteBuf buffer) {
        if(dominated != null && dominant != null) {
            buffer.writeBoolean(true);
            buffer.writeInt(dominant.getId());
            buffer.writeInt(dominated.getId());

            NbtCompound dominatedNBT = new NbtCompound();
            writeNBT(SpringSide.DOMINATED, dominated, dominatedNBT);

            NbtCompound dominantNBT = new NbtCompound();
            writeNBT(SpringSide.DOMINANT, dominant, dominantNBT);

            buffer.writeNbt(dominantNBT);
            buffer.writeNbt(dominatedNBT);
        } else {
            buffer.writeBoolean(false);
        }
    }

    @Override
    public void readSpawnData(PacketByteBuf additionalData) {
        if(additionalData.readBoolean()) { // has both entities
            int frontID = additionalData.readInt();
            int backID = additionalData.readInt();
            setDominant((VesselEntity) world.getEntityById(frontID));
            setDominated((VesselEntity) world.getEntityById(backID));

            dominantNBT = additionalData.readNbt();
            dominatedNBT = additionalData.readNbt();
        }
    }*/

    public static void createSpring(VesselEntity dominantEntity, VesselEntity dominatedEntity) {
        SpringEntity link = new SpringEntity(dominantEntity, dominatedEntity);
        World world = dominantEntity.world;
        world.spawnEntity(link);
    }

    @Override
    public Box getVisibilityBoundingBox() {
        return BoxUtil.INFINITE_EXTENT_BOX;
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this, this.getType(),0,this.getBlockPos());
    }

    public void kill() {
        super.remove(RemovalReason.KILLED);
        if(dominant != null){
            dominant.removeDominated();
        }
        if(!world.isClient)
            ItemScatterer.spawn(world, getX(), getY(), getZ(), new ItemStack(ModCommon.ITEM_SPRING));
    }

    public enum SpringSide {
        DOMINANT, DOMINATED
    }
}
