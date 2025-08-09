package com.chengcode.sgsmod.network;

import com.chengcode.sgsmod.gui.ShanPromptScreen;
import net.minecraft.client.MinecraftClient;

public class ClientReceiver {
    public static void openShanPromptUI() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            client.setScreen(new ShanPromptScreen());
        });
    }
}
