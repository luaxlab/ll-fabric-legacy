package de.luaxlab.shipping.common.config;

import me.lortseam.completeconfig.api.ConfigContainer;
import me.lortseam.completeconfig.api.ConfigEntries;
import me.lortseam.completeconfig.api.ConfigEntry;
import me.lortseam.completeconfig.api.ConfigGroup;

public class ShippingConfig implements ConfigContainer {

    @ConfigEntries
    @Transitive
    public static class Client implements ConfigGroup
    {

        public static double FISHING_TREASURE_CHANCE_MODIFIER = 0.02;
        public static String FISHING_LOOT_TABLE = "minecraft:gameplay/fishing/fish";

        @ConfigEntry.BoundedDouble(min = 0, max= 1)
        public static double TUG_SMOKE_MODIFIER = 0.4;

        public static boolean DISABLE_TUG_ROUTE_BEACONS = false;
    }

    @ConfigEntries
    @Transitive
    public static class Common implements ConfigGroup
    {

        public static double FISHING_TREASURE_CHANCE_MODIFIER = 0.02;
        public static String FISHING_LOOT_TABLE = "minecraft:gameplay/fishing/fish";

        @ConfigEntry.BoundedDouble(min = 0.1d, max= 10d)
        public static double TUG_BASE_SPEED = 2.4;

        @ConfigEntry.BoundedInteger(min=1)
        public static int STEAM_TUG_FUEL_MULTIPLIER = 4;

        @ConfigEntry.BoundedInteger(min=1)
        public static int ENERGY_TUG_BASE_CAPACITY = 10_000;

        @ConfigEntry.BoundedInteger(min=1)
        public static int ENERGY_TUG_BASE_ENERGY_USAGE = 1;

        @ConfigEntry.BoundedInteger(min=1)
        public static int ENERGY_TUG_BASE_MAX_CHARGE_RATE = 100;

        @ConfigEntry.BoundedInteger(min=1)
        public static int VESSEL_CHARGER_BASE_CAPACITY=10_000;

        @ConfigEntry.BoundedInteger(min=1)
        public static int VESSEL_CHARGER_BASE_MAX_TRANSFER=100;
    }

}
