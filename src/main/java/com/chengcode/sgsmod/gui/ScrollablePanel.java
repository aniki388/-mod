package com.chengcode.sgsmod.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.client.gui.widget.AbstractTextWidget;

import java.util.function.Consumer;

public class ScrollablePanel extends AbstractTextWidget {
    private int contentHeight;
    int scrollAmount = 0;
    private int maxScroll;

    private final Consumer<DrawContext> contentRenderer;

    public ScrollablePanel(TextRenderer textRenderer, int x, int y, int width, int height, Consumer<DrawContext> contentRenderer) {
        super( x, y, width, height, Text.empty(),textRenderer);
        this.contentRenderer = contentRenderer;
        this.contentHeight = height;
        this.maxScroll = 0;
    }

    public void setContentHeight(int contentHeight) {
        this.contentHeight = contentHeight;
        maxScroll = Math.max(0, contentHeight - height);
        if (scrollAmount > maxScroll) scrollAmount = maxScroll;
        if (scrollAmount < 0) scrollAmount = 0;
    }

    public void scroll(int delta) {
        scrollAmount = Math.min(maxScroll, Math.max(0, scrollAmount + delta));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.isMouseOver(mouseX, mouseY)) {
            scroll((int) (-amount * 10));
            return true;
        }
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.enableScissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());
        context.getMatrices().push();
        context.getMatrices().translate(getX(), getY() - scrollAmount, 0);

        contentRenderer.accept(context);

        context.getMatrices().pop();
        context.disableScissor();

        if (maxScroll > 0) {
            int barHeight = Math.max(10, (int)((float) getHeight() * getHeight() / contentHeight));
            int barY = getY() + (int)((float) scrollAmount * (getHeight() - barHeight) / maxScroll);
            int barX = getX() + getWidth() - 6;
            context.fill(barX, getY(), barX + 4, getY() + getHeight(), 0xFF333333);
            context.fill(barX, barY, barX + 4, barY + barHeight, 0xFF888888);
        }
    }


    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        // 不用画按钮部分
    }

}
