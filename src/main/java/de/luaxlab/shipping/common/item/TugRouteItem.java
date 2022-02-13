package de.luaxlab.shipping.common.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TugRouteItem extends Item {
    private static final String ROUTE_NBT = "route";
    public TugRouteItem(Settings settings) {
        super(settings);
    }

    /*public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if(!player.level.isClientSide){
            int x = (int) Math.floor(player.getX());
            int z = (int) Math.floor(player.getZ());
            if (!tryRemoveSpecific(itemstack, x, z)) {
                player.displayClientMessage(new TranslationTextComponent("item.littlelogistics.tug_route.added", x, z), false);
                pushRoute(itemstack, x, z);
            } else {
                player.displayClientMessage(new TranslationTextComponent("item.littlelogistics.tug_route.removed", x, z), false);
            }

        }

        return ActionResult.pass(itemstack);
    }*/

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        /*ItemStack itemstack = context.getPlayer().getStackInHand(context.getHand());
        if(!context.getWorld().isClient){
            int x = (int) Math.floor(context.getPlayer().getX());
            int z = (int) Math.floor(context.getPlayer().getZ());
            if (!tryRemoveSpecific(itemstack, x, z)) {
                context.getPlayer().sendMessage(new TranslatableText("item.littlelogistics.tug_route.added", x, z), false);
                pushRoute(itemstack, x, z);
            } else {
                context.getPlayer().sendMessage(new TranslatableText("item.littlelogistics.tug_route.removed", x, z), false);
            }
        }*/
        return ActionResult.PASS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemstack = user.getStackInHand(hand);
        if(!world.isClient){
            int x = (int) Math.floor(user.getX());
            int z = (int) Math.floor(user.getZ());
            if (!tryRemoveSpecific(itemstack, x, z)) {
                user.sendMessage(new TranslatableText("item.littlelogistics.tug_route.added", x, z), false);
                pushRoute(itemstack, x, z);
            } else {
                user.sendMessage(new TranslatableText("item.littlelogistics.tug_route.removed", x, z), false);
            }
        }
        return TypedActionResult.pass(itemstack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        //TODO: Add formatting on Forge version
        super.appendTooltip(stack, world, tooltip, context);
        //I genuinely hate this
        //tooltip.add(new TranslatableText("item.littlelogistics.tug_route.description").formatted(Formatting.GRAY));
        //tooltip.add(new LiteralText(formatRoute(getRoute(stack))).formatted(Formatting.GRAY));
        tooltip.add(new LiteralText("Stations: " + getRoute(stack).size()));
    }

    public static List<Vector2f> getRoute(ItemStack itemStack){
        NbtCompound nbt = nbt(itemStack);
        if(!nbt.contains(ROUTE_NBT)){
            nbt.putString(ROUTE_NBT, "");
        }

        return parseRoute(nbt.getString(ROUTE_NBT));
    }

    public static boolean popRoute(ItemStack itemStack){
        List<Vector2f> route = getRoute(itemStack);
        if(route.size() == 0) {
            return false;
        }
        route.remove(route.size() - 1);
        saveRoute(route, itemStack);
        return true;
    }

    public static boolean tryRemoveSpecific(ItemStack itemStack, int x, int z){
        List<Vector2f> route = getRoute(itemStack);
        if(route.size() == 0) {
            return false;
        }
        boolean removed = route.removeIf(v -> v.getX() == x && v.getY() == z);
        saveRoute(route, itemStack);
        return removed;
    }

    public static void pushRoute(ItemStack itemStack, int x, int y){
        List<Vector2f> route = getRoute(itemStack);
        route.add(new Vector2f(x, y));
        saveRoute(route, itemStack);
    }

    private static void saveRoute(List<Vector2f> route, ItemStack itemStack){
        NbtCompound nbt = nbt(itemStack);
        nbt.putString(ROUTE_NBT, serialiseRoute(route));
    }

    private static List<Vector2f> parseRoute(String route){
        if(route.equals("")){
            return new ArrayList<>();
        }

        return Arrays.stream(route.split(","))
                .map(string -> string.split(":"))
                .map(arr -> new Vector2f(Float.parseFloat(arr[0]), Float.parseFloat(arr[1])))
                .collect(Collectors.toList());

    }

    private static String serialiseRoute(List<Vector2f> route){
        return route
                .stream()
                .map(vector -> vector.getX() + ":" + vector.getY())
                .reduce((acc, curr) -> acc + "," + curr)
                .orElse("");
    }

    private static String formatRoute(List<Vector2f> route){
        AtomicInteger index = new AtomicInteger();
        return route
                .stream()
                .map(vector -> String.format("%s %d. X:%d, Y:%d",
                        I18n.translate("item.littlelogistics.tug_route.node"),
                        index.getAndIncrement(), (int) Math.floor(vector.getX()), (int) Math.floor(vector.getY())))
                .reduce((acc, curr) -> acc + "\n" + curr)
                .orElse("");
    }

    private static NbtCompound nbt(ItemStack stack)  {
        if(stack.getNbt() == null) {
            stack.setNbt(new NbtCompound());
        }
        return stack.getNbt();
    }

}
