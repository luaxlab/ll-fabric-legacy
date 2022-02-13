package de.luaxlab.shipping.common.container;

import de.luaxlab.shipping.common.entity.accessor.DataAccessor;
import de.luaxlab.shipping.common.entity.vehicle.tug.AbstractTugEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class AbstractTugContainer<T extends DataAccessor> extends AbstractItemHandlerContainer {
    protected T data;
    protected AbstractTugEntity tugEntity;

    public AbstractTugContainer(@Nullable ScreenHandlerType<?> containerType, int windowId, World world, T data,
                                PlayerInventory playerInventory, PlayerEntity player) {
        super(containerType, windowId, playerInventory, player);
        this.tugEntity = (AbstractTugEntity) world.getEntityById(data.getEntityUUID());
        this.data = data;
        layoutPlayerInventorySlots(8, 84);
        this.addProperties(data);
    }

    public AbstractTugContainer(@Nullable ScreenHandlerType<?> containerType, int windowId, T data,
                                PlayerInventory playerInventory) {
        this(containerType,windowId, playerInventory.player.world, data, playerInventory, playerInventory.player);

    }
}
