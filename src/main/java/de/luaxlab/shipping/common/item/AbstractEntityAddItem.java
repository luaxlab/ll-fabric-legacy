package de.luaxlab.shipping.common.item;

import de.luaxlab.shipping.common.core.ModCommon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.*;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractEntityAddItem extends Item {

    private static final Predicate<Entity> RIDERS = EntityPredicates.EXCEPT_SPECTATOR.and(Entity::collides);


    public AbstractEntityAddItem(Settings settings) {
        super(settings);
    }

    /*@Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BoatItem
        //ModCommon.LOGGER.info("Hit on: {}", context.getHitPos());
        //ModCommon.LOGGER.info("Hit on: {}", context.getHitPos());
        Vec3d direction = Vec3d.of(Direction.getEntityFacingOrder(context.getPlayer())[0].getVector());
        ModCommon.LOGGER.info("Facing: {}", direction);
        Entity entity = getEntity(context.getWorld(), context.getHitPos().add(direction));
        entity.setYaw(context.getPlayerYaw());
        if (!context.getWorld().canCollide(entity, entity.getBoundingBox().expand(-0.1D))) {
            return ActionResult.FAIL;
        } else {
            if (!context.getWorld().isClient) {
                context.getWorld().spawnEntity(entity);
                if (!context.getPlayer().getAbilities().creativeMode) {
                    context.getStack().decrement(1);
                }
            }

            context.getPlayer().increaseStat(Stats.USED.getOrCreateStat(context.getStack().getItem()),1);
            return ActionResult.SUCCESS;
        }
    }*/

    /* Copied from BoatItem, but altered to fit our needs */

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        BlockHitResult hitResult = BoatItem.raycast(world, user, RaycastContext.FluidHandling.ANY);
        if (((HitResult)hitResult).getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(itemStack);
        }
        Vec3d vec3d = user.getRotationVec(1.0f);
        double d = 5.0;
        List<Entity> list = world.getOtherEntities(user, user.getBoundingBox().stretch(vec3d.multiply(5.0)).expand(1.0), RIDERS);
        if (!list.isEmpty()) {
            Vec3d vec3d2 = user.getEyePos();
            for (Entity entity : list) {
                Box box = entity.getBoundingBox().expand(entity.getTargetingMargin());
                if (!box.contains(vec3d2)) continue;
                return TypedActionResult.pass(itemStack);
            }
        }
        if (((HitResult)hitResult).getType() == HitResult.Type.BLOCK) {
            Entity entity = getEntity(world, hitResult.getPos());
            entity.setYaw(user.getYaw());
            if (!world.isSpaceEmpty(entity, entity.getBoundingBox())) {
                return TypedActionResult.fail(itemStack);
            }
            if (!world.isClient) {
                world.spawnEntity(entity);
                world.emitGameEvent((Entity)user, GameEvent.ENTITY_PLACE, new BlockPos(hitResult.getPos()));
                if (!user.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }
            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            return TypedActionResult.success(itemStack, world.isClient());
        }
        return TypedActionResult.pass(itemStack);
    }

    protected abstract Entity getEntity(World world, Vec3d position);
}
