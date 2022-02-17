package de.luaxlab.shipping.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.luaxlab.shipping.common.container.FishingBargeContainer;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FishingBargeScreen extends HandledScreen<FishingBargeContainer> {
    private static final Identifier CONTAINER_BACKGROUND = new Identifier("textures/gui/container/generic_54.png");
    private final int containerRows;

    public FishingBargeScreen(FishingBargeContainer p_i51095_1_, PlayerInventory p_i51095_2_, Text p_i51095_3_) {
        super(p_i51095_1_, p_i51095_2_, p_i51095_3_);
        this.passEvents = false;
        this.containerRows = 1;
        this.backgroundHeight = 114 + this.containerRows * 18;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CONTAINER_BACKGROUND);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.containerRows * 18 + 17);
        this.drawTexture(matrices, i, j + this.containerRows * 18 + 17, 0, 126, this.backgroundWidth, 96);
    }
}
