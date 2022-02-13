package de.luaxlab.shipping.common.core;

import de.luaxlab.shipping.common.block.guide_rail.CornerGuideRailBlock;
import de.luaxlab.shipping.common.block.guide_rail.TugGuideRailBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;

public class ModBlocks {

    private static final Map<Identifier, Block> SCHEDULED_BLOCKS = new HashMap<>();
    private static final Map<Identifier, Item> SCHEDULED_ITEMS = new HashMap<>();

    public static final List<Block> ALL_BLOCKS = new ArrayList<>();
    public static final AbstractBlock.Settings DEFAULT_BLOCK_SETTINGS = FabricBlockSettings.of(Material.METAL);

    /* Blocks */

    public static final Block BLOCK_GUIDE_CORNER = defferedRegister(identifier("guide_rail_corner"), new CornerGuideRailBlock(DEFAULT_BLOCK_SETTINGS));
    public static final Block BLOCK_GUIDE_TUG = defferedRegister(identifier("guide_rail_tug"), new TugGuideRailBlock(DEFAULT_BLOCK_SETTINGS));

    /* Code */

    static <T extends Block> T defferedRegister(Identifier identifier, T block, boolean item)
    {
        ALL_BLOCKS.add(block);
        SCHEDULED_BLOCKS.put(identifier, block);
        if(item)
            SCHEDULED_ITEMS.put(identifier, new BlockItem(block, ModItems.DEFAULT_ITEM_SETTINGS));
        return block;
    }

    static <T extends Block> T defferedRegister(Identifier identifier, T block)
    {
        return defferedRegister(identifier, block, true);
    }

    /**
     * Called by {@link ModCommon} to handle late-registering
     */
    /*default*/ static void register()
    {
        /* easy-registry */
            SCHEDULED_BLOCKS.forEach((identifier, block) -> Registry.register(Registry.BLOCK, identifier, block));
            SCHEDULED_BLOCKS.clear();
            SCHEDULED_ITEMS.forEach((identifier, item) -> Registry.register(Registry.ITEM, identifier, item));
            SCHEDULED_ITEMS.clear();
        /* space for compelx registry */
    }

    private static Identifier identifier(String path) { return ModCommon.identifier(path); }
}
