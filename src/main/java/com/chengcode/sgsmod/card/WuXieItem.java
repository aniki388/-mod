package com.chengcode.sgsmod.card;

import com.chengcode.sgsmod.entity.TacticCardEntity;
import com.chengcode.sgsmod.entity.WuZhongEntity;
import com.chengcode.sgsmod.manager.WuXieStack;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WuXieItem extends TacticCard {
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    public static final Queue<ScheduledFuture<?>> taskQueue = new LinkedList<>();

    public WuXieItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            // 使用最小堆来存储附近的锦囊卡牌
            PriorityQueue<NearbyCard> nearbyCards = new PriorityQueue<>((a, b) -> Double.compare(a.distance, b.distance));

            // 获取当前玩家的所有实体（附近的实体）
            List<Entity> nearbyEntities = world.getEntitiesByClass(Entity.class, player.getBoundingBox().expand(5.0), entity -> entity instanceof TacticCardEntity);

            // 遍历玩家附近的所有实体，计算并加入堆
            for (Entity entity : nearbyEntities) {
                if (entity instanceof TacticCardEntity tacticCard) {
                    double distance = player.squaredDistanceTo(entity);
                    nearbyCards.offer(new NearbyCard(tacticCard, distance));  // 将实体和距离加入堆
                }
            }

            // 获取堆顶元素，即距离最近的锦囊卡牌
            NearbyCard nearestCard = nearbyCards.poll();
            if (nearestCard != null) {
                String targetCardId = nearestCard.card.getCardId();  // 获取最近锦囊的cardId
                WuXieStack.plusWuxieCnt(targetCardId);
                player.sendMessage(Text.literal("已触发【无懈可击】效果，请等待锦囊卡牌处理完毕！"));
                player.playSound(ModSoundEvents.WUXIE, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    // 定时任务检查任务是否完成
    public static void processNextTask() {
        if (!taskQueue.isEmpty()) {
            ScheduledFuture<?> future = taskQueue.peek();  // 获取当前任务

            if (future != null) {
                executorService.schedule(() -> {
                    if (future.isDone()) {
                        taskQueue.poll();  // 移除已完成任务
                        processNextTask();  // 继续处理下一个任务
                    } else {
                        // 如果任务未完成，继续检查
                        processNextTask();
                    }
                }, 3, TimeUnit.SECONDS);  // 每 3 秒检查一次任务状态
            }
        }
    }

    // 定义一个类来存储卡牌和距离信息，用于堆排序
    public static class NearbyCard {
        public TacticCardEntity card;
        public double distance;

        public NearbyCard(TacticCardEntity card, double distance) {
            this.card = card;
            this.distance = distance;
        }
    }
}
