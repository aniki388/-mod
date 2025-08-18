package com.chengcode.sgsmod.gui;

import com.chengcode.sgsmod.compaign.CampaignDef;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;

import java.util.List;
import java.util.function.Consumer;

public class CampaignListWidget extends EntryListWidget<CampaignListWidget.Entry> {
    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    public void setX(int i) {
        this.left = i;
    }

    public static class Entry extends EntryListWidget.Entry<Entry> {
        private final CampaignDef def;
        private final Consumer<CampaignDef> onSelect;

        public Entry(CampaignDef def, Consumer<CampaignDef> onSelect) {
            this.def = def;
            this.onSelect = onSelect;
        }

        @Override
        public void render(DrawContext ctx, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float delta) {
            var tr = MinecraftClient.getInstance().textRenderer;

            // 背板（hover 高亮）
            int bg = hovered ? 0x33232832 : 0x2215181f;
            ctx.fill(x, y, x + entryWidth, y + entryHeight, bg);

            // 标题
            ctx.drawText(tr, def.name(), x + 8, y + 4, 0xFFECECEC, false);

            // 副标题
            String meta = def.faction() + " · " + def.era();
            ctx.drawText(tr, meta, x + 8, y + 16, 0xFF9AA3AF, false);

            // 难度徽章（右侧）
            String badge = def.difficulty();
            int bw = tr.getWidth(badge) + 10;
            int bx0 = x + entryWidth - bw - 8;
            ctx.fill(bx0, y + 5, bx0 + bw, y + 5 + 14, 0x332A2E36);
            ctx.drawText(tr, badge, bx0 + 4, y + 7, 0xFFC9D1E1, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            onSelect.accept(def);
            return true;
        }
    }

    public CampaignListWidget(MinecraftClient mc, int width, int height, int top, int bottom, int itemHeight) {
        super(mc, width, height, top, bottom, itemHeight);
        setRenderBackground(false);
        setRenderHeader(false, 0);
    }

    public void setData(List<CampaignDef> data, Consumer<CampaignDef> onSelect) {
        clearEntries();
        data.forEach(d -> addEntry(new Entry(d, onSelect)));
    }
}
