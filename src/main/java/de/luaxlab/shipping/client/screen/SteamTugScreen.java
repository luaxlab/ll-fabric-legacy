package de.luaxlab.shipping.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.luaxlab.shipping.common.container.SteamTugContainer;
import de.luaxlab.shipping.common.core.ModCommon;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.minecraft.client.gui.screen.ingame.FurnaceScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SteamTugScreen extends AbstractTugScreen<SteamTugContainer> {
    private static final Identifier GUI = ModCommon.identifier("textures/container/steam_tug.png");

    public SteamTugScreen(SteamTugContainer menu, PlayerInventory inventory, Text label) {
        super(menu, inventory, label);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        if(this.isPointWithinBounds(this.x+43,this.y+23+12, 14,14,mouseX,mouseY))
        {
            List<Text> list = List.of(new LiteralText(this.handler.getBurnProgress() +  "s"));
            this.renderTooltip(matrixStack, list, Optional.empty(),mouseX,mouseY);
        }

    }

    @Override
    protected void drawMouseoverTooltip(MatrixStack matrices, int x, int y) {
        super.drawMouseoverTooltip(matrices, x, y);
        //ModCommon.LOGGER.info("I:" + x + "|" + y);
        //ModCommon.LOGGER.info("S:" + (this.x+43) + "|" + (this.y+23+12));
        if(this.isMouseWithinBounds(x,y,this.x+43,this.y+23, 14,14))
        {
            //ModCommon.LOGGER.info("IN!");
            List<Text> list = List.of(new LiteralText(this.handler.getBurnTimeSeconds() +  "s"));
            this.renderTooltip(matrices, list, Optional.empty(),x,y);
        }
    }

    @Override
    protected void drawBackground(MatrixStack matrixStack, float partialTicks, int x, int y) {
        //RenderSystem.clearColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, GUI);
        //FurnaceScreen
        //this.client.getTextureManager().bindTexture(GUI);
        this.drawTexture(matrixStack, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        if(handler.isLit()) {
            int k = this.handler.getBurnProgress();
            this.drawTexture(matrixStack, this.x + 43, this.y + 23 + 12 - k, 176, 12 - k, 14, k + 1);
        }
    }


    /** Helper functions **/

    public boolean isMouseWithinBounds(int mouseX, int mouseY, int x, int y, int width, int height)
    {
        return isMouseWithinBoundsAbsolute(mouseX,mouseY,x,y,x+width,y+width);
    }

    public boolean isMouseWithinBoundsAbsolute(int mouseX, int mouseY, int x1, int y1, int x2, int y2)
    {
        return x1 <= mouseX && y1 <= mouseY && x2 >= mouseX && y2 >= mouseY;
    }
}
