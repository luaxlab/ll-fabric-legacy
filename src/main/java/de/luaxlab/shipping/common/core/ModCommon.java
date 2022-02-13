package de.luaxlab.shipping.common.core;

import de.luaxlab.shipping.common.config.ShippingConfig;
import de.luaxlab.shipping.common.container.SteamTugContainer;
import de.luaxlab.shipping.common.entity.SpringableEntity;
import de.luaxlab.shipping.common.entity.accessor.SteamTugDataAccessor;
import de.luaxlab.shipping.common.entity.vehicle.tug.SteamTugEntity;
import de.luaxlab.shipping.common.entity.vehicle.tug.TugDummyHitboxEntity;
import de.luaxlab.shipping.common.item.SpringItem;
import de.luaxlab.shipping.common.util.EntitySpringAPI;
import dev.architectury.event.events.common.InteractionEvent;
import me.lortseam.completeconfig.data.Config;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModCommon implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final FabricItemSettings DEFAULT_ITEM_SETTINGS = new FabricItemSettings().group(ItemGroup.TRANSPORTATION);
	public static final String MODID = "littlelogistics";
	public static final Logger LOGGER = LoggerFactory.getLogger("Little Logistics");
	public static final Config CONFIG = new Config(MODID, new ShippingConfig());

	@Deprecated
	public static final Block BLOCK_GUIDE_CORNER = ModBlocks.BLOCK_GUIDE_CORNER;
	@Deprecated
	public static final Block BLOCK_GUIDE_TUG = ModBlocks.BLOCK_GUIDE_TUG;
	@Deprecated
	public static final Item ITEM_TUG_ROUTE = ModItems.TUG_ROUTE;
	@Deprecated
	public static final Item ITEM_SPRING = ModItems.SPRING;
	@Deprecated
	public static final Item ITEM_STEAM_TUG = ModItems.STEAM_TUG;
	@Deprecated
	public static final EntityType<SteamTugEntity> ENTITY_STEAM_TUG = ModEntities.STEAM_TUG;
	@Deprecated
	public static final EntityType<Entity> ENTITY_SPRING = ModEntities.SPRING;
	@Deprecated
	public static final EntityType<TugDummyHitboxEntity> ENTITY_DUMMY_TUG_HITBOX = ModEntities.DUMMY_TUG_HITBOX;

	public static final ScreenHandlerType<SteamTugContainer> CONTAINER_STEAM_TUG = ScreenHandlerRegistry.registerExtended(identifier("steam_tug"),
			(syncId, inventory, buf) -> new SteamTugContainer(syncId, new SteamTugDataAccessor(makeIntArray(buf)), inventory));

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		/* Registration */

		ModBlocks.register();
		ModItems.register();
		ModEntities.register();

		/* Events */

		UseEntityCallback.EVENT.register(new UseEntityCallback() {
			@Override
			public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
				Item item = player.getStackInHand(hand).getItem();
				if(item instanceof SpringItem springItem) {
					if(EntitySpringAPI.isValidTarget(entity)) {
						springItem.onUsedOnEntity(player.getStackInHand(hand), player,world,entity);
						return ActionResult.SUCCESS;
					}
				}
				else if(item instanceof ShearsItem) {
					if(entity instanceof SpringableEntity springable) {
						springable.getDominant().ifPresent(pair -> pair.getSecond().kill());
						return ActionResult.SUCCESS;
					}
				}
				return ActionResult.PASS;
			}
		});

		//Entities

		//FabricDefaultAttributeRegistry.register(ENTITY_DUMMY_TUG_HITBOX, TugDummyHitboxEntity.setCustomAttributes());


		LOGGER.info("Hello Fabric world!");
	}

	public static Identifier identifier(String path)
	{
		return new Identifier(MODID, path);
	}

	private static PropertyDelegate makeIntArray(PacketByteBuf buffer) {
		int size = (buffer.readableBytes() + 1) / 4;
		PropertyDelegate arr = new ArrayPropertyDelegate(size);
		for (int i = 0; i < size; i++) {
			arr.set(i, buffer.readInt());
		}
		return arr;
	}
}
