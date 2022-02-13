package de.luaxlab.shipping.common.blockentity;

import de.luaxlab.shipping.common.entity.vehicle.VesselEntity;

public interface IVesselLoader {
    enum Mode {
        EXPORT,
        IMPORT
    }

    /*static <T> Optional<T> getEntityCapability(BlockPos pos, Capability<T> capability, World level){
        List<Entity> fluidEntities = level.getEntities((Entity) null,
                getSearchBox(pos),
                (e -> entityPredicate(e, pos, capability))
        );

        if(fluidEntities.isEmpty()){
            return Optional.empty();
        } else {
            Entity entity = fluidEntities.get(0);
            return entity.getCapability(capability).resolve();
        }
    }

    static boolean entityPredicate(Entity entity, BlockPos pos, Capability<?> capability) {
        return entity.getCapability(capability).resolve().map(cap -> {
            if (entity instanceof VesselEntity){
                VesselEntity vessel = (VesselEntity) entity;
                return vessel.allowDockInterface() && (vessel.getBlockPos().getX() == pos.getX() && vessel.getBlockPos().getZ() == pos.getZ());
            } else {
                return true;
            }
        }).orElse(false);
    }

    static AxisAlignedBB getSearchBox(BlockPos pos) {
        return new AxisAlignedBB(
                pos.getX() - 0.5D,
                pos.getY() - 0.5D,
                pos.getZ() - 0.5D,
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D);
    }*/

    boolean holdVessel(VesselEntity vessel, Mode mode);
}
