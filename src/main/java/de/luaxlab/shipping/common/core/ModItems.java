package de.luaxlab.shipping.common.core;

import de.luaxlab.shipping.common.item.ChestBargeItem;
import de.luaxlab.shipping.common.item.SpringItem;
import de.luaxlab.shipping.common.item.SteamTugItem;
import de.luaxlab.shipping.common.item.TugRouteItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModItems {

    private static final Map<Identifier, Item> SCHEDULED_ITEMS = new HashMap<>();

    public static final List<Item> ALL_ITEMS = new ArrayList<>();
    public static final Item.Settings DEFAULT_ITEM_SETTINGS = new FabricItemSettings().group(ItemGroup.TRANSPORTATION);

    /* Items */

    public static final Item TUG_ROUTE = defferedRegister(identifier("tug_route"),new TugRouteItem(DEFAULT_ITEM_SETTINGS));
    public static final Item SPRING = defferedRegister(identifier("spring"), new SpringItem(DEFAULT_ITEM_SETTINGS));
    /* Items: Spawn Eggs */
    public static final Item STEAM_TUG = defferedRegister(identifier("tug"), new SteamTugItem(DEFAULT_ITEM_SETTINGS));
    public static final Item CHEST_BARGE = defferedRegister(identifier("barge"), new ChestBargeItem(DEFAULT_ITEM_SETTINGS));

    /* Code */

    static <T extends Item> T defferedRegister(Identifier identifier, T item)
    {
        ALL_ITEMS.add(item);
        SCHEDULED_ITEMS.put(identifier, item);
        return item;
    }


    /**
     * Called by {@link ModCommon} to handle late-registering
     */
    /*default*/ static void register()
    {
        /* easy-registry */
            SCHEDULED_ITEMS.forEach((identifier, item) -> Registry.register(Registry.ITEM, identifier, item));
            SCHEDULED_ITEMS.clear();
        /* space for compelx registry */
    }

    private static Identifier identifier(String path) { return ModCommon.identifier(path); }
}
