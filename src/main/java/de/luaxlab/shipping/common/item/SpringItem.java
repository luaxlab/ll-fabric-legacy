package de.luaxlab.shipping.common.item;

/*
MIT License

Copyright (c) 2018 Xavier "jglrxavpok" Niochaut

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


import de.luaxlab.shipping.common.entity.SpringEntity;
import de.luaxlab.shipping.common.entity.SpringableEntity;
import de.luaxlab.shipping.common.entity.vehicle.VesselEntity;
import de.luaxlab.shipping.common.entity.vehicle.tug.TugDummyHitboxEntity;
import de.luaxlab.shipping.common.util.Train;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class SpringItem extends Item {

    private Text springInfo = new TranslatableText("item.littlelogistics.spring.description").formatted(Formatting.GRAY);

    public SpringItem(Settings settings) {
        super(settings);

//        addProperty(new ResourceLocation("first_selected"), (stack, a, b) -> getState(stack) == State.WAITING_NEXT ? 1f : 0f);
    }

    // because 'itemInteractionForEntity' is only for Living entities
    public void onUsedOnEntity(ItemStack stack, PlayerEntity player, World world, Entity target) {
        if(target instanceof TugDummyHitboxEntity){
            target = ((TugDummyHitboxEntity) target).getTug();
        }
        if(world.isClient)
            return;
        switch(getState(stack)) {
            case WAITING_NEXT: {
                createSpringHelper(stack, player, world, target);
            }
            break;

            default: {
                setDominant(world, stack, target);
            }
            break;
        }
    }

    private void createSpringHelper(ItemStack stack, PlayerEntity player, World world, Entity target) {
        Entity dominant = getDominant(world, stack);
        if(dominant == null)
            return;
        if(dominant == target) {
            player.sendMessage(new TranslatableText("item.littlelogistics.spring.notToSelf"), true);
        } else if(dominant instanceof SpringableEntity) {
            Train firstTrain =  ((SpringableEntity) dominant).getTrain();
            Train secondTrain = ((SpringableEntity) target).getTrain();
            if (dominant.distanceTo(target) > 15){
                player.sendMessage(new TranslatableText("item.littlelogistics.spring.tooFar"), true);
            } else if (firstTrain.getTug().isPresent() && secondTrain.getTug().isPresent()) {
                player.sendMessage(new TranslatableText("item.littlelogistics.spring.noTwoTugs"), true);
            } else if (secondTrain.equals(firstTrain)){
                player.sendMessage(new TranslatableText("item.littlelogistics.spring.noLoops"), true);
            } else if (firstTrain.getTug().isPresent()) {
                SpringEntity.createSpring((VesselEntity) firstTrain.getTail(), (VesselEntity) secondTrain.getHead());
            } else {
                SpringEntity.createSpring((VesselEntity) secondTrain.getTail(), (VesselEntity) firstTrain.getHead());
            }
            // First entity clicked is the dominant
            if(!player.isCreative())
                stack.decrement(1);
        }
        resetLinked(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(springInfo);
    }

    private void setDominant(World worldIn, ItemStack stack, Entity entity) {
        nbt(stack).putInt("linked", entity.getId());
    }

    private Entity getDominant(World worldIn, ItemStack stack) {
        int id = nbt(stack).getInt("linked");
        return worldIn.getEntityById(id);
    }

    private static NbtCompound nbt(ItemStack stack)  {
        if(stack.getNbt() == null) {
            stack.setNbt(new NbtCompound());
        }
        return stack.getNbt();
    }

    private void resetLinked(ItemStack itemstack) {
        nbt(itemstack).remove("linked");
    }

    /*@Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        resetLinked(playerIn.getItemInHand(handIn));
        return super.use(worldIn, playerIn, handIn);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        return super.useOnEntity(stack, user, entity, hand);
    }*/
    //TODO: Add right click actions

    public static State getState(ItemStack stack) {
        if(nbt(stack).contains("linked"))
            return State.WAITING_NEXT;
        return State.READY;
    }

    public enum State {
        WAITING_NEXT,
        READY
    }
}
