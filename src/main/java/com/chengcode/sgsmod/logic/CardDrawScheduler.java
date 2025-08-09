package com.chengcode.sgsmod.logic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.*;

public class CardDrawScheduler {
    private static final Map<UUID, ScheduledDraw> scheduledDraws = new HashMap<>();

    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            Iterator<Map.Entry<UUID, ScheduledDraw>> iterator = scheduledDraws.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, ScheduledDraw> entry = iterator.next();
                ScheduledDraw draw = entry.getValue();
                draw.ticksRemaining--;

                if (draw.ticksRemaining <= 0) {
                    ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
                    if (player != null && player.isAlive()) {
                        for (int i = 0; i < 2 && !draw.deck.isEmpty(); i++) {
                            player.getInventory().insertStack(draw.deck.poll());
                        }
                        player.sendMessage(Text.of("你摸了两张牌"), false);
                        draw.ticksRemaining = 300; // 再次延迟 15 秒
                    } else {
                        iterator.remove(); // 玩家不在了，移除任务
                    }
                }
            }
        });
    }

    public static void schedule(ServerPlayerEntity player, Deque<ItemStack> deck) {
        scheduledDraws.put(player.getUuid(), new ScheduledDraw(deck, 300));
    }

    private static class ScheduledDraw {
        Deque<ItemStack> deck;
        int ticksRemaining;

        ScheduledDraw(Deque<ItemStack> deck, int ticks) {
            this.deck = deck;
            this.ticksRemaining = ticks;
        }
    }
}
