package com.chengcode.sgsmod;

import com.chengcode.sgsmod.entity.ModEntities;
import com.chengcode.sgsmod.entity.LvBuEntityRenderer;
import com.chengcode.sgsmod.entity.WeiYanEntityRenderer;
import com.chengcode.sgsmod.gui.ModeSelectScreen;
import com.chengcode.sgsmod.gui.SelectTargetPlayerScreen;
import com.chengcode.sgsmod.gui.ShanPromptScreen;
import com.chengcode.sgsmod.skill.ModSkills;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import com.chengcode.sgsmod.network.NetWorking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;


public class SgsmodClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.SHA_ENTITY, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.WUZHONG_ENTITY, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SHUNSHOU_ENTITY, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.CHAIQIAO_ENTITY, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.LUBU, LvBuEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.WEIYAN, WeiYanEntityRenderer::new);
        // 监听服务端提示出闪包，打开客户端UI（示意，需自行实现UI）
        ClientPlayNetworking.registerGlobalReceiver(NetWorking.SHAN_PROMPT_PACKET, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                MinecraftClient.getInstance().setScreen(new ShanPromptScreen());
            });
        });

        // 监听客户端回复包，不需要客户端读取数据（客户端发送该包给服务器）
        ClientPlayNetworking.registerGlobalReceiver(NetWorking.SHAN_RESPONSE_PACKET, (client, handler, buf, responseSender) -> {
            // 理论上客户端不会收到该包（只有服务端接收客户端发来的）
            // 如果有需要也可加处理
        });

        ClientPlayNetworking.registerGlobalReceiver(NetWorking.OPEN_MODE_MENU_PACKET_ID, (client, handler, buf, responseSender) -> {
            client.execute(() -> client.setScreen(new ModeSelectScreen()));
        });

        ClientPlayNetworking.registerGlobalReceiver(NetWorking.OPEN_SELECT_TARGET_PACKET_ID, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                // 打开选择目标玩家的GUI界面
                client.setScreen(new SelectTargetPlayerScreen(client.player));
            });
        });

        // 玩家加入服务器事件
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            ModSkills.loadPlayerSkills(player);  // 玩家连接时加载技能数据
        });

        // 服务器停止时保存数据
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ModSkills.savePlayerSkills(player);
            }
        });
    }
}
