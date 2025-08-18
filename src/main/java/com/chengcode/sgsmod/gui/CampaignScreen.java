package com.chengcode.sgsmod.gui;

import com.chengcode.sgsmod.Sgsmod;
import com.chengcode.sgsmod.compaign.CampaignDef;
import com.chengcode.sgsmod.compaign.DemoData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CampaignScreen extends Screen {
    private static final Identifier PANEL_BG_TEXTURE1 = new Identifier(Sgsmod.MOD_ID, "textures/gui/panel_bg1.png");
    private static final Identifier PANEL_BG_TEXTURE2 = new Identifier(Sgsmod.MOD_ID, "textures/gui/panel_bg2.png");


    private CampaignListPanel listPanel;     // 左侧列表
    private ScrollablePanel detailPanel;     // 右侧详情
    private TextFieldWidget search;

    private List<CampaignDef> all = new ArrayList<>();
    private List<CampaignDef> filtered = new ArrayList<>();
    private CampaignDef active;

    private int guiLeft, guiTop;
    private int guiW = 256, guiH = 240;
    private int leftW = 88;

    public CampaignScreen() {
        super(Text.translatable("screen.sgsmod.campaigns"));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 优先判断自定义按钮区域（与 init() 里按钮坐标保持一致）
        int startX = guiLeft + guiW - 54;
        int startY = guiTop + guiH - 20;
        int startW = 48, startH = 16;

        int previewX = guiLeft + guiW - 108;
        int previewY = startY;
        int previewW = 48, previewH = 16;

        // 注意 mouseX/mouseY 是 double，比较时保持 double/int 一致性即可
        if (mouseX >= startX && mouseX < startX + startW && mouseY >= startY && mouseY < startY + startH) {
            // 直接触发 start 按钮逻辑
            this.startCampaign();
            return true;
        }

        if (mouseX >= previewX && mouseX < previewX + previewW && mouseY >= previewY && mouseY < previewY + previewH) {
            this.previewCampaign();
            return true;
        }

        // 不是点击按钮，交给默认处理（会分发给 panels / textfield / 按钮等）
        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    protected void init() {
        guiLeft = (width - guiW) / 2;
        guiTop = (height - guiH) / 2;

        all = DemoData.sample();
        filtered = new ArrayList<>(all);
        active = filtered.isEmpty() ? null : filtered.get(0);

        // 搜索框
        search = new TextFieldWidget(textRenderer, guiLeft + 6, guiTop + 22, leftW - 12, 14, Text.literal(""));
        search.setPlaceholder(Text.literal("搜索..."));
        search.setChangedListener(s -> {
            String kw = s.trim().toLowerCase(Locale.ROOT);
            filtered = all.stream()
                    .filter(c -> kw.isEmpty() || (c.name() + "|" + c.faction() + "|" + c.era() + "|" + c.difficulty() + "|" + String.join(",", c.tags()))
                            .toLowerCase(Locale.ROOT).contains(kw))
                    .collect(Collectors.toList());
            listPanel.setData(filtered);
            if (!filtered.contains(active) && !filtered.isEmpty()) active = filtered.get(0);
            updateDetailContentHeight();
        });
        addDrawableChild(search);

        // 左侧滚动列表
        int listTop = guiTop + 38;
        int listH = guiH - 44;
        listPanel = new CampaignListPanel(guiLeft + 6, listTop, leftW - 12, listH, filtered, def -> {
            active = def;
            updateDetailContentHeight();
        });
        addSelectableChild(listPanel);

        // 右侧滚动详情
        int detailX = guiLeft + leftW + 4;
        int detailY = guiTop + 20;
        int detailW = guiW - leftW - 8;
        int detailH = guiH - 28;
        detailPanel = new ScrollablePanel(textRenderer, detailX, detailY, detailW, detailH, this::renderDetailContent);
        updateDetailContentHeight();
        addSelectableChild(detailPanel);

        // 按钮
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.sgsmod.start"),
                        b -> startCampaign())
                .dimensions(guiLeft + guiW - 54, guiTop + guiH - 20, 48, 16)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("预览"),
                        b -> previewCampaign())
                .dimensions(guiLeft + guiW - 108, guiTop + guiH - 20, 48, 16)
                .build());
    }

    private void updateDetailContentHeight() {
        detailPanel.setContentHeight(estimateDetailContentHeight(active));
    }

    private int estimateDetailContentHeight(CampaignDef def) {
        if (def == null) return 0;
        int baseHeight = 22 + 54 + 6; // cover图高度+间距
        int lines = textRenderer.wrapLines(Text.literal(def.summary()), guiW - leftW - 12 - 72 - 6).size();
        int rewards = def.reward().size();
        return baseHeight + lines * 9 + 16 + rewards * 9 + 10;
    }



    private void startCampaign() {
        if (active == null) {
            showMessage("请选择一个关卡再开始");
            return;
        }
        active.onStart().run(client.player);
        this.close();
    }

    private void previewCampaign() {
        if (active == null) {
            showMessage("请选择一个关卡再预览");
        }else showMessage("预览：" + active.name());
        this.close();
    }

    private void showMessage(String msg) {
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal(msg), false);
        }
    }


    @Override
    public void render(DrawContext g, int mouseX, int mouseY, float delta) {
        renderBackground(g);

        // 渲染背景面板
        fillPanel(g, guiLeft, guiTop, leftW, guiH, PANEL_BG_TEXTURE1);
        fillPanel(g, guiLeft + leftW + 4, guiTop, guiW - leftW - 4, guiH, PANEL_BG_TEXTURE2);

        // 左侧列表和右侧详情
        listPanel.render(g, mouseX, mouseY, delta);
        detailPanel.render(g, mouseX, mouseY, delta);

        // 标题
        g.drawText(textRenderer, title, guiLeft + 6, guiTop + 6, 0xFFECECEC, false);

        // 调用父类渲染按钮和文本框
        super.render(g, mouseX, mouseY, delta);
    }

    private void fillPanel(DrawContext g, int x, int y, int w, int h,Identifier TEXTURE) {
        // 1. 先填渐变底色
        int topColor = 0xFF2C2F36;
        int bottomColor = 0xFF525763;

        for (int i = 0; i < h; i++) {
            float ratio = i / (float) h;
            int r = (int) (((topColor >> 16 & 0xFF) * (1 - ratio)) + ((bottomColor >> 16 & 0xFF) * ratio));
            int gCol = (int) (((topColor >> 8 & 0xFF) * (1 - ratio)) + ((bottomColor >> 8 & 0xFF) * ratio));
            int b = (int) (((topColor & 0xFF) * (1 - ratio)) + ((bottomColor & 0xFF) * ratio));
            int color = (0xFF << 24) | (r << 16) | (gCol << 8) | b;
            g.fill(x, y + i, x + w, y + i + 1, color);
        }

        // 2. 叠加纹理贴图（设置部分透明度）
        g.setShaderColor(1f, 1f, 1f, 0.15f); // 透明度15%
        g.drawTexture(TEXTURE, x, y, 0, 0, w, h, w, h);
        g.setShaderColor(1f, 1f, 1f, 1f); // 重置透明度

        // 3. 叠加边框和光效
        g.fill(x, y, x + w, y + 1, 0x332A2E36);
        g.fill(x, y + h - 1, x + w, y + h, 0x332A2E36);
        g.fill(x, y, x + 1, y + h, 0x332A2E36);
        g.fill(x + w - 1, y, x + w, y + h, 0x332A2E36);

        // 4. 叠加顶端一条高光线（可选）
        g.fillGradient(x , y + 1, x + w - 1, y + 3, 0x80FFFFFF, 0x00FFFFFF);
    }

    private void renderDetailContent(DrawContext g) {
        if (active == null) return;

        int rx = 0;
        int ry = 0;
        int rw = guiW - leftW - 12;

        g.drawText(textRenderer, Text.literal(active.name()), rx, ry, 0xFFECECEC, false);
        g.drawText(textRenderer,
                Text.literal(active.faction() + " · " + active.era() + " · 难度：" + active.difficulty()),
                rx, ry + 10, 0xFF9AA3AF, false);

        int coverW = 72, coverH = 54;
        Identifier cover = active.coverPath() != null ? Identifier.tryParse(active.coverPath()) : null;
        if (cover != null) {
            g.drawTexture(cover, rx, ry + 22, 0, 0, coverW, coverH, coverW, coverH);
        } else {
            g.fill(rx, ry + 22, rx + coverW, ry + 22 + coverH, 0xFF1E2330);
        }

        int textX = rx + coverW + 6;
        int textY = ry + 22;
        int textW = rw - coverW - 6;

        for (var line : textRenderer.wrapLines(Text.literal(active.summary()), textW)) {
            g.drawText(textRenderer, line, textX, textY, 0xFFC9D1E1, false);
            textY += 9;
        }

        int rewardsY = Math.max(ry + 22 + coverH + 6, textY + 6);
        g.drawText(textRenderer, Text.literal("奖励："), rx, rewardsY, 0xFFECECEC, false);
        int i = 0;
        for (String r : active.reward()) {
            g.drawText(textRenderer, Text.literal("- " + r), rx + 20, rewardsY + 10 + i * 9, 0xFFC9D1E1, false);
            i++;
        }
    }
}
