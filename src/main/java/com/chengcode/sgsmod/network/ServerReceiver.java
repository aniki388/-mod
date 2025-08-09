package com.chengcode.sgsmod.network;

import com.chengcode.sgsmod.entity.GeneralEntity;
import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.item.ModItems;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerReceiver {
    private static final Map<UUID, Integer> pendingDamage = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> remainingShanNeeded = new ConcurrentHashMap<>();
    private static final Map<UUID, ScheduledFuture<?>> pendingShanTasks = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static boolean tryConsumeShanFromInventory(ServerPlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            var stack = player.getInventory().getStack(i);
            if (stack.getItem() == ModItems.SHAN) {
                stack.decrement(1);
                if (stack.isEmpty()) {
                    player.getInventory().removeStack(i);
                }
                return true;
            }
        }
        return false;
    }

    public static void register() {
        // 接收【闪】的响应
        ServerPlayNetworking.registerGlobalReceiver(NetWorking.SHAN_RESPONSE_PACKET, (server, player, handler, buf, responseSender) -> {
            boolean usedShan = buf.readBoolean();
            UUID playerId = player.getUuid();

            server.execute(() -> {
                int realDamage = pendingDamage.getOrDefault(playerId, 5);

                // 普通闪：无需考虑双闪
                if (!remainingShanNeeded.containsKey(playerId)) {
                    if (usedShan && tryConsumeShanFromInventory(player)) {
                        player.sendMessage(Text.of("你使用了『闪』！成功抵挡了『杀』"), false);
                        player.getWorld().playSound(null, player.getBlockPos(), ModSoundEvents.SHAN, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    } else {
                        player.sendMessage(Text.of("你未出『闪』，受到 " + realDamage + " 点伤害"), false);
                        player.damage(player.getWorld().getDamageSources().generic(), realDamage);
                    }
                    pendingDamage.remove(playerId);
                    ScheduledFuture<?> task = pendingShanTasks.remove(playerId);
                    if (task != null) task.cancel(false);
                    return;
                }

                // 无双：需要出2张闪
                if (usedShan && tryConsumeShanFromInventory(player)) {
                    int remaining = remainingShanNeeded.get(playerId) - 1;
                    if (remaining <= 0) {
                        // 完成所有闪
                        remainingShanNeeded.remove(playerId);
                        pendingDamage.remove(playerId);
                        ScheduledFuture<?> future = pendingShanTasks.remove(playerId);
                        if (future != null) future.cancel(false);

                        player.sendMessage(Text.of("你成功出了两张『闪』！抵挡了『杀』"), false);
                        player.getWorld().playSound(null, player.getBlockPos(), ModSoundEvents.SHAN, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    } else {
                        remainingShanNeeded.put(playerId, remaining);
                        player.sendMessage(Text.of("你出了第 " + (2 - remaining) + " 张『闪』，还需出 " + remaining + " 张"), false);
                        player.getWorld().playSound(null, player.getBlockPos(), ModSoundEvents.SHAN, SoundCategory.PLAYERS, 1.0F, 1.0F);

                        // 重新计时3秒
                        ScheduledFuture<?> prev = pendingShanTasks.remove(playerId);
                        if (prev != null) prev.cancel(false);

                        // 这里创建新的定时任务
                        ScheduledFuture<?> newTask = scheduler.schedule(() -> {
                            player.getServer().execute(() -> {
                                if (pendingShanTasks.remove(playerId) != null) {
                                    remainingShanNeeded.remove(playerId);
                                    pendingDamage.remove(playerId);
                                    player.sendMessage(Text.of("你未能连续出两张『闪』，受到 " + realDamage + " 点伤害"), false);
                                    player.damage(player.getWorld().getDamageSources().generic(), realDamage);
                                }
                            });
                        }, 3, TimeUnit.SECONDS);

                        // 更新任务队列
                        pendingShanTasks.put(playerId, newTask);

                        // 发送提示消息，告诉玩家还需要出一张闪卡
                        ServerPlayNetworking.send(player, NetWorking.SHAN_PROMPT_PACKET, PacketByteBufs.create());

                        // 更新闪卡需求为1（还需一张闪卡）
                        remainingShanNeeded.put(playerId, 1);
                    }

                } else {
                    // 没出闪，直接受伤
                    remainingShanNeeded.remove(playerId);
                    pendingDamage.remove(playerId);
                    ScheduledFuture<?> future = pendingShanTasks.remove(playerId);
                    if (future != null) future.cancel(false);

                    player.sendMessage(Text.of("你未出『闪』，受到 " + realDamage + " 点伤害"), false);
                    player.damage(player.getWorld().getDamageSources().generic(), realDamage);
                }
            });
        });

        // 进入测试模式
        ServerPlayNetworking.registerGlobalReceiver(NetWorking.MODE_SELECT_PACKET_ID, (server, player, handler, buf, sender) -> {
            String selectedMode = buf.readString();
            server.execute(() -> {
                if ("test".equals(selectedMode)) {
                    player.sendMessage(Text.of("进入测试模式"), false);
                    double radius = 100.0;
                    // 获得全部的在线玩家
                    List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
                    List<GeneralEntity> generals = server.getOverworld().getEntitiesByType(
                            TypeFilter.instanceOf(GeneralEntity.class),
                            new Box(player.getPos().subtract(radius, radius, radius), player.getPos().add(radius, radius, radius)),
                            general -> true
                    );
                    CardGameManager.startTestMode(players,generals );
                } else {
                    player.sendMessage(Text.of("该模式尚未实现: " + selectedMode), false);
                }
            });
        });
    }

    public static boolean promptShan(ServerPlayerEntity target, int damage) {
        AtomicBoolean hasShan = new AtomicBoolean(true);
        UUID playerId = target.getUuid();
        target.sendMessage(Text.of("你可以出『闪』！3秒内有效"), false);
        ServerPlayNetworking.send(target, NetWorking.SHAN_PROMPT_PACKET, PacketByteBufs.create());

        pendingDamage.put(playerId, damage);

        // 创建一个定时任务，每3秒检查是否出闪
        ScheduledFuture<?> task = scheduler.schedule(() -> {
            target.getServer().execute(() -> {
                if (pendingShanTasks.remove(playerId) != null) {
                    pendingDamage.remove(playerId);
                    target.sendMessage(Text.of("你未出『闪』，受到 " + damage + " 点伤害"), false);
                    target.damage(target.getWorld().getDamageSources().generic(), damage);
                    hasShan.set(false);
                }
            });
        }, 3, TimeUnit.SECONDS);

        // 将定时任务保存在任务队列中
        pendingShanTasks.put(playerId, task);
        return hasShan.get();
    }

    public static boolean promptDoubleShan(ServerPlayerEntity target, int damage) {
        AtomicBoolean hasShan = new AtomicBoolean(true);
        UUID playerId = target.getUuid();

        // 发送初次提示
        target.sendMessage(Text.of("『无双』你需要连续出两张『闪』！每张3秒内有效"), false);
        ServerPlayNetworking.send(target, NetWorking.SHAN_PROMPT_PACKET, PacketByteBufs.create());

        // 保存玩家当前的伤害和闪的需求
        pendingDamage.put(playerId, damage);
        remainingShanNeeded.put(playerId, 2);  // 需要两张闪

        // 创建一个定时任务，3秒后检查是否完成出闪任务
        ScheduledFuture<?> task = scheduler.schedule(() -> {
            target.getServer().execute(() -> {
                if (pendingShanTasks.remove(playerId) != null) {
                    remainingShanNeeded.remove(playerId);
                    pendingDamage.remove(playerId);

                    // 如果任务被移除，且未成功出两张闪，则造成伤害
                    target.sendMessage(Text.of("你未能出够两张『闪』，受到 " + damage + " 点伤害"), false);
                    target.damage(target.getWorld().getDamageSources().generic(), damage);
                    hasShan.set(false);
                }
            });
        }, 3, TimeUnit.SECONDS);

        // 保存定时任务，用于后续取消
        pendingShanTasks.put(playerId, task);
        return hasShan.get();
    }
}
