package com.chengcode.sgsmod.gui;

import com.chengcode.sgsmod.compaign.CampaignDef;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.Selectable;

import java.util.List;
import java.util.function.Consumer;

public class CampaignListPanel implements Drawable, Element, Selectable {

    private static final int ITEM_HEIGHT = 18;
    private static final int PADDING_X = 4;
    private static final int SCROLLBAR_W = 3;
    private static final int SCROLL_SPEED = 12;

    private final int x, y, width, height;
    private List<CampaignDef> data;
    private final Consumer<CampaignDef> onSelect;

    private double scrollOffset = 0.0;
    private int selectedIndex = -1;
    private int hoveredIndex = -1;

    public CampaignListPanel(int x, int y, int width, int height,
                             List<CampaignDef> data, Consumer<CampaignDef> onSelect) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.data = data;
        this.onSelect = onSelect;
    }

    // 可选：给 Screen 使用
    public void setData(List<CampaignDef> data) {
        this.data = data;
        // 保持更稳妥的选中状态
        if (selectedIndex >= data.size()) selectedIndex = -1;
        int maxScroll = Math.max(0, data.size() * ITEM_HEIGHT - height);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public CampaignDef getSelected() {
        return (selectedIndex >= 0 && selectedIndex < data.size()) ? data.get(selectedIndex) : null;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        var tr = MinecraftClient.getInstance().textRenderer;

        // 裁剪区域
        ctx.enableScissor(x, y, x + width, y + height);

        // 计算首/尾可见项，避免渲染整表
        int contentHeight = data.size() * ITEM_HEIGHT;
        int firstIndex = Math.max(0, (int) Math.floor(scrollOffset / ITEM_HEIGHT));
        int visibleCount = (height / ITEM_HEIGHT) + 2;
        int lastIndex = Math.min(data.size() - 1, firstIndex + visibleCount);

        int startY = y - (int) (scrollOffset % ITEM_HEIGHT);
        hoveredIndex = -1;

        for (int i = firstIndex, drawY = startY; i <= lastIndex; i++, drawY += ITEM_HEIGHT) {
            boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= drawY && mouseY <= drawY + ITEM_HEIGHT;

            // 背景：选中/悬停
            int bg = (i == selectedIndex) ? 0xFF5050A0 : (hovered ? 0xFF404040 : 0x00000000);
            if ((bg >>> 24) != 0) ctx.fill(x, drawY, x + width, drawY + ITEM_HEIGHT, bg);

            // 文本
            ctx.drawText(tr, data.get(i).name(), x + PADDING_X, drawY + 4, 0xFFECECEC, false);

            if (hovered) hoveredIndex = i;
        }

        ctx.disableScissor();

        // 滚动条
        int maxScroll = Math.max(0, contentHeight - height);
        if (maxScroll > 0) {
            int barHeight = Math.max(10, height * height / contentHeight);
            int barY = y + (int) (scrollOffset * (height - barHeight) / maxScroll);
            int barX = x + width - SCROLLBAR_W - 1;
            ctx.fill(barX, y, barX + SCROLLBAR_W, y + height, 0x55000000);
            ctx.fill(barX, barY, barX + SCROLLBAR_W, barY + barHeight, 0xFF888888);
        }
    }

    // 1.20+ 签名（3 参数）
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!isInside(mouseX, mouseY)) return false;
        int contentHeight = data.size() * ITEM_HEIGHT;
        int maxScroll = Math.max(0, contentHeight - height);
        scrollOffset = clamp(scrollOffset - amount * SCROLL_SPEED, 0, maxScroll);
        return true;
    }

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isInside(mouseX, mouseY)) return false;
        int index = (int) ((mouseY - y + scrollOffset) / ITEM_HEIGHT);
        if (index >= 0 && index < data.size()) {
            selectedIndex = index;
            onSelect.accept(data.get(index));
            return false;
        }
        return false;
    }

    private boolean isInside(double mx, double my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    private static double clamp(double v, double min, double max) {
        return v < min ? min : (v > max ? max : v);
    }

    // ---- Selectable 接口（为了 addSelectableChild 使用）----
    @Override
    public SelectionType getType() {
        return hoveredIndex >= 0 ? SelectionType.HOVERED : (selectedIndex >= 0 ? SelectionType.FOCUSED : SelectionType.NONE);
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        // 可按需添加无障碍提示，这里留空
    }
}
