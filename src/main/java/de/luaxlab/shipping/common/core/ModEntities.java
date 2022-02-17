package de.luaxlab.shipping.common.core;

import de.luaxlab.shipping.common.entity.SpringEntity;
import de.luaxlab.shipping.common.entity.vehicle.barge.ChestBargeEntity;
import de.luaxlab.shipping.common.entity.vehicle.barge.FishingBargeEntity;
import de.luaxlab.shipping.common.entity.vehicle.tug.TugDummyHitboxEntity;
import de.luaxlab.shipping.common.entity.vehicle.tug.SteamTugEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModEntities {

    /* Tugs */
    public static final EntityType<SteamTugEntity> STEAM_TUG = Registry.register(
            Registry.ENTITY_TYPE,
            identifier("tug"),
            FabricEntityTypeBuilder.create(SpawnGroup.WATER_CREATURE, (EntityType.EntityFactory<SteamTugEntity>) SteamTugEntity::new).dimensions(EntityDimensions.fixed(0.75f, 0.75f)).build()
    );

    /* Barges */
    public static final EntityType<ChestBargeEntity> CHEST_BARGE = Registry.register(
            Registry.ENTITY_TYPE,
            identifier("barge"),
            FabricEntityTypeBuilder.create(SpawnGroup.WATER_CREATURE, (EntityType.EntityFactory<ChestBargeEntity>) ChestBargeEntity::new).dimensions(EntityDimensions.fixed(0.6f, 0.9f)).build()
    );
    public static final EntityType<FishingBargeEntity> FISHING_BARGE = Registry.register(
            Registry.ENTITY_TYPE,
            identifier("fishing_barge"),
            FabricEntityTypeBuilder.create(SpawnGroup.WATER_CREATURE, (EntityType.EntityFactory<FishingBargeEntity>) FishingBargeEntity::new).dimensions(EntityDimensions.fixed(0.6f, 0.9f)).build()
    );
    
    /* Misc */
    public static final EntityType<Entity> SPRING = Registry.register(
            Registry.ENTITY_TYPE,
            identifier("spring"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, SpringEntity::new).dimensions(EntityDimensions.fixed(0.05f, 0.2f)).build()
    );

    public static final EntityType<TugDummyHitboxEntity> DUMMY_TUG_HITBOX = Registry.register(
            Registry.ENTITY_TYPE,
            identifier("dummy_tug_hitbox"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, (EntityType.EntityFactory<TugDummyHitboxEntity>) TugDummyHitboxEntity::new).dimensions(EntityDimensions.fixed(0.75f, 0.75f)).build()
    );

    /**
     * Called by {@link ModCommon} to handle late-registering
     */
    /*default*/ static void register()
    {
        /* easy-registry */
        FabricDefaultAttributeRegistry.register(STEAM_TUG, SteamTugEntity.setCustomAttributes());
        FabricDefaultAttributeRegistry.register(CHEST_BARGE, ChestBargeEntity.setCustomAttributes());
        FabricDefaultAttributeRegistry.register(FISHING_BARGE, FishingBargeEntity.setCustomAttributes());


        /* space for compelx registry */
    }

    private static Identifier identifier(String path) { return ModCommon.identifier(path); }

}
