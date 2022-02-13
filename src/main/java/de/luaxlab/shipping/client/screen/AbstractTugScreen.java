package de.luaxlab.shipping.client.screen;

import de.luaxlab.shipping.common.container.AbstractItemHandlerContainer;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public abstract class AbstractTugScreen<T extends AbstractItemHandlerContainer> extends HandledScreen<T> {
    public AbstractTugScreen(T menu, PlayerInventory inventory, Text text) {
        super(menu, inventory, text);
    }

    protected static boolean inBounds(int mouseX, int mouseY, int x1, int y1, int x2, int y2) {
        return (mouseX >= x1) && (mouseX < x2) && (mouseY >= y1) && (mouseY < y2);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.drawMouseoverTooltip(matrixStack, mouseX, mouseY);
    }
}