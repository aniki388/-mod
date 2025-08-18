package com.chengcode.sgsmod.gui;

import com.chengcode.sgsmod.Sgsmod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModeSelectScreen extends Screen {
    public static final Identifier MODE_SELECT_PACKET_ID = new Identifier(Sgsmod.MOD_ID, "mode_select");

    public ModeSelectScreen() {
        super(Text.of("选择模式"));
    }

    @Override
    protected void init() {
        int y = this.height / 2 - 30;

        addDrawableChild(ButtonWidget.builder(Text.of("测试模式"), button -> {
            sendModeSelectToServer("test");
            this.client.setScreen(null);
        }).position(this.width / 2 - 50, y).size(100, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.of("标准模式"), button -> {
            sendModeSelectToServer("standard");
            this.client.setScreen(null);
        }).position(this.width / 2 - 50, y + 25).size(100, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.of("军争模式 (未实现)"), button -> {
            this.client.player.sendMessage(Text.of("军争模式暂未实现"), false);
//            sendModeSelectToServer("war");
        }).position(this.width / 2 - 50, y + 50).size(100, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.of("取消"), button -> this.client.setScreen(null))
                .position(this.width / 2 - 50, y + 80).size(100, 20).build());
    }

    private void sendModeSelectToServer(String mode) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(mode);
        ClientPlayNetworking.send(MODE_SELECT_PACKET_ID, buf);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);
        drawContext.drawTextWithShadow(this.textRenderer, this.title, this.width / 2 - this.textRenderer.getWidth(this.title) / 2, 50, 0xFFFFFF);
        super.render(drawContext, mouseX, mouseY, delta);
    }
}