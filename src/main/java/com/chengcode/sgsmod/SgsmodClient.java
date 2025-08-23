package com.chengcode.sgsmod;

import com.chengcode.sgsmod.entity.*;
import com.chengcode.sgsmod.entity.general.LvBuEntity;
import com.chengcode.sgsmod.entity.general.SunCeEntity;
import com.chengcode.sgsmod.entity.general.WeiYanEntity;
import com.chengcode.sgsmod.gui.*;
import com.chengcode.sgsmod.item.ModItems;
import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.manager.HandInventory;
import com.chengcode.sgsmod.manager.HandManagementScreenHandler;
import com.chengcode.sgsmod.model.ElephantModel;
import com.chengcode.sgsmod.network.ClientHandCache;
import com.chengcode.sgsmod.render.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import com.chengcode.sgsmod.network.NetWorking;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.*;

// 常量类：提取硬编码字符串，提高可维护性
//class ClientConstants {
//    public static final String ENTITY_SUNCE = "sunce";
//    public static final String ENTITY_LVBU = "lvbu";
//    public static final String ENTITY_WEIYAN = "weiyan";
//}

public class SgsmodClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 1. 注册实体渲染器（提取为单独方法，简化结构）
        registerEntityRenderers();

        // 2. 注册实体模型层
        registerEntityModelLayers();

        // 3. 注册客户端网络接收器（仅处理客户端需要响应的数据包）
        registerClientNetworkHandlers();

        ScreenRegistry.register(ModItems.HAND_MANAGEMENT_SCREEN_HANDLER, HandManagementScreen::new);
    }




    /**
     * 注册实体渲染器（整理重复代码）
     */
    private void registerEntityRenderers() {
        // 物品飞行实体渲染器
        EntityRendererRegistry.register(ModEntities.SHA_ENTITY, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.WUZHONG_ENTITY, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SHUNSHOU_ENTITY, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.CHAIQIAO_ENTITY, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.NANMAN_CARD_ENTITY, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.TAOYUAN_CARD_ENTITY, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.JIEDAO_CARD_ENTITY, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.WANJIAN, FlyingItemEntityRenderer::new);

        // 武将实体渲染器
        EntityRendererRegistry.register(ModEntities.LUBU, LvBuEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.WEIYAN, WeiYanEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.ELEPHANT, ElephantEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SUNCE, SunCeEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.NANMAN, NanmanRender::new);
        EntityRendererRegistry.register(ModEntities.JIE_XUSHENG, JieXushengRenderer::new);
        EntityRendererRegistry.register(ModEntities.CAOCHONG, CaochongEntityRenderer::new);


    }

    /**
     * 注册实体模型层
     */
    private void registerEntityModelLayers() {
        EntityModelLayerRegistry.registerModelLayer(ModEntityModelLayers.ELEPHANT_LAYER, ElephantModel::getTexturedModelData);
    }

    /**
     * 注册客户端网络处理器（仅处理客户端逻辑）
     */
    private void registerClientNetworkHandlers() {
        // 监听"出闪"提示包，打开客户端UI
        ClientPlayNetworking.registerGlobalReceiver(NetWorking.SHAN_PROMPT_PACKET, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                MinecraftClient.getInstance().setScreen(new ShanPromptScreen());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(NetWorking.SYNC_ID, (client, handler, buf, responseSender) -> {
            // 从包中解析 ItemStack 列表并写入客户端缓存
            client.execute(() -> {
                int size = buf.readInt();
                ItemStack[] stacks = new ItemStack[size];
                for (int i = 0; i < size; i++) stacks[i] = buf.readItemStack();
                // 写入本地缓存
                ClientHandCache.setStacks(stacks);

                // 如果当前打开的界面有能力直接消费这些数据（例如你的 Screen 实现有方法），
                // 你可以在这里把数据写回 GUI；下面是兼容逻辑：如果 current screen
                // 提供了一个名为 "onHandDataSynced(ItemStack[])" 的公开方法，你可以调用之。
                // 但为避免强依赖（避免 NoClassDef），这里只保留保守方式：让 GUI 自行从 ClientHandCache 拉数据。
            });
        });

        // 监听"借刀杀人"攻击包，处理目标选择UI
        ClientPlayNetworking.registerGlobalReceiver(NetWorking.JIEDAO_ATTACK_PACKET, (client, handler, buf, responseSender) -> {
            // 网络线程读取数据（必须在client.execute外完成，避免线程安全问题）
            int knifeOwnerId = buf.readInt();
            int targetId = buf.readInt();

            client.execute(() -> {
                World world = MinecraftClient.getInstance().world;
                if (world == null) return;

                LivingEntity knifeOwner = (LivingEntity) world.getEntityById(knifeOwnerId);
                LivingEntity target = (LivingEntity) world.getEntityById(targetId);

                if (target instanceof PlayerEntity) {
                    MinecraftClient.getInstance().setScreen(new ShaPromptScreen(knifeOwner));
                } else if (target instanceof GeneralEntity general) {
                    general.setTarget(knifeOwner);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(NetWorking.TURN_ORDER_SYNC_PACKET_ID, (client, handler, buf, responseSender) -> {
            // 在网络线程读取数据，存到列表
            List<Integer> turnIndexes = new ArrayList<>();
            List<Boolean> isPlayers = new ArrayList<>();
            List<UUID> playerUUIDs = new ArrayList<>();
            List<Integer> entityIds = new ArrayList<>();

            while (buf.isReadable()) {
                boolean isPlayer = buf.readBoolean();
                int turnIndex = buf.readInt();
                turnIndexes.add(turnIndex);
                isPlayers.add(isPlayer);

                if (isPlayer) {
                    playerUUIDs.add(buf.readUuid());
                    entityIds.add(null);
                } else {
                    entityIds.add(buf.readInt());
                    playerUUIDs.add(null);
                }
            }

            // 在主线程处理显示
            client.execute(() -> {
                World world = MinecraftClient.getInstance().world;
                if (world == null) return;

                Map<Integer, LivingEntity> turns = new HashMap<>();
                for (int i = 0; i < turnIndexes.size(); i++) {
                    int index = turnIndexes.get(i);
                    if (isPlayers.get(i)) {
                        UUID uuid = playerUUIDs.get(i);
                        PlayerEntity player = world.getPlayerByUuid(uuid);
                        if (player != null) turns.put(index, player);
                    } else {
                        int eid = entityIds.get(i);
                        Entity entity = world.getEntityById(eid);
                        if (entity instanceof GeneralEntity general) turns.put(index, general);
                    }
                }
                CardGameManager.ClientTurnManager.updateTurnOrder(turns);
                // 构建显示文本
                StringBuilder sb = new StringBuilder("当前轮次：");
                for (int i = 1; i <= turns.size(); i++) {
                    LivingEntity entity = turns.get(i);
                    if (entity instanceof PlayerEntity player) {
                        sb.append("第").append(i).append("轮玩家：").append(player.getEntityName()).append(" ");
                    } else if (entity instanceof GeneralEntity general) {
                        sb.append("第").append(i).append("轮武将：")
                                .append(Objects.requireNonNullElse(general.getCustomName(), Text.literal("未命名")).getString())
                                .append(" ");
                    }
                }

                // 只发送给本地玩家
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(Text.of(sb.toString()), false);
                }
            });
        });


        ClientPlayNetworking.registerGlobalReceiver(NetWorking.OPEN_TURN_ORDER_PACKET_ID,
                (client, handler, buf, responseSender) -> {
                    List<UUID> playerIds = new ArrayList<>();
                    List<Integer> generalIds = new ArrayList<>();

                    int Playsize = buf.readInt();
                    for (int i = 0; i < Playsize; i++) {
                        playerIds.add(buf.readUuid());
                    }
                    int Generalsize = buf.readInt();
                    for (int i = 0; i < Generalsize; i++) {
                        generalIds.add(buf.readInt());
                    }

                    client.execute(() -> {
                        List<PlayerEntity> players = new ArrayList<>();
                        List<GeneralEntity> generals = new ArrayList<>();

                        for (UUID playerId : playerIds) {
                            if (client.world != null) {
                                players.add(client.world.getPlayerByUuid(playerId));
                            }
                        }

                        for (Integer generalId : generalIds) {
                            if (client.world != null) {
                                generals.add((GeneralEntity) client.world.getEntityById(generalId));
                            }
                        }

                        // 打开 GUI
                        client.setScreen(new TurnOrderScreen(players, generals));
                    });
                });


        // 监听"杀"响应包
        ClientPlayNetworking.registerGlobalReceiver(NetWorking.SHA_RESPONSE_PACKET, (client, handler, buf, responseSender) -> {
            int targetId = buf.readInt();
            boolean usedSha = buf.readBoolean();

            client.execute(() -> {
                if (usedSha && client.player != null) {
                    LivingEntity target = (LivingEntity) handler.getWorld().getEntityById(targetId);
                    if (target != null) {
                        // 注意：CardGameManager若包含服务器逻辑，此处应仅做客户端视觉效果
                        // 实际攻击逻辑应在服务器端处理
                        CardGameManager.throwShaAt(client.player, target);
                    }
                }
            });
        });

        // 打开模式选择菜单
        ClientPlayNetworking.registerGlobalReceiver(NetWorking.OPEN_MODE_MENU_PACKET_ID, (client, handler, buf, responseSender) -> {
            client.execute(() -> client.setScreen(new ModeSelectScreen()));
        });


        // 打开目标选择界面
        ClientPlayNetworking.registerGlobalReceiver(NetWorking.OPEN_SELECT_TARGET_PACKET_ID, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                if (client.player != null) {
                    client.setScreen(new SelectTargetPlayerScreen(client.player));
                }
            });
        });

        // 打开战役界面
        ClientPlayNetworking.registerGlobalReceiver(NetWorking.OPEN_CAMPAIGNS_PACKET_ID, (client, handler, buf, responseSender) -> {
            client.execute(() -> client.setScreen(new CampaignScreen()));
        });



    }
}
