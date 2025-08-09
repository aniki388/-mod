package com.chengcode.sgsmod.gui;
import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import com.chengcode.sgsmod.network.NetWorking;

public class ShanPromptScreen extends Screen{
    public ShanPromptScreen() {
        super(Text.of("是否使用『闪』？"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.of("使用『闪』"), btn -> {
            sendResponseToServer(true);
            this.close();
        }).dimensions(centerX - 80, centerY - 10, 70, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("不出"), btn -> {
            sendResponseToServer(false);
            this.close();
        }).dimensions(centerX + 10, centerY - 10, 70, 20).build());
    }

    private void sendResponseToServer(boolean usedShan) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(usedShan);
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(new net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket(NetWorking.SHAN_RESPONSE_PACKET, buf));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
