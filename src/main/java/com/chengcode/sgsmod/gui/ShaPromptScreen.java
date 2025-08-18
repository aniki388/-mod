package com.chengcode.sgsmod.gui;

import com.chengcode.sgsmod.network.NetWorking;
import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public class ShaPromptScreen extends Screen {
    private LivingEntity target;
    public ShaPromptScreen(LivingEntity target) {
        super(Text.of("是否使用『杀』？"));
        this.target = target;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 使用『杀』按钮
        this.addDrawableChild(ButtonWidget.builder(Text.of("是否对" + target.getName().getString() + "使用『杀』"), btn -> {
            sendResponseToServer(true);
            this.close();
        }).dimensions(centerX - 80, centerY - 10, 70, 20).build());

        // 不出按钮
        this.addDrawableChild(ButtonWidget.builder(Text.of("不出"), btn -> {
            sendResponseToServer(false);
            this.close();
        }).dimensions(centerX + 10, centerY - 10, 70, 20).build());
    }

    /**
     * 将玩家是否出杀的选择发送给服务器
     */
    private void sendResponseToServer(boolean usedSha) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(usedSha);
        buf.writeInt(target.getId());

        MinecraftClient.getInstance().getNetworkHandler().sendPacket(
                new net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket(
                        NetWorking.SHA_RESPONSE_PACKET, buf
                )
        );
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}