package com.chengcode.sgsmod.entity;

import com.chengcode.sgsmod.card.TacticCard;
import com.chengcode.sgsmod.manager.WuXieStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class TacticCardEntity extends ThrownItemEntity {

    protected boolean canResponse = true;  // 是否可以响应无懈可击
    private String cardId;  // 唯一标识符
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public TacticCardEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected abstract Item getDefaultItem();

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);  // 保持物品悬浮
    }

    // 无懈可击反制的动画和音效
    public void triggerFailedEffect() {
        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 0.5F, 1.0F);
        this.getWorld().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.5, 0.5, 0.5);
        this.kill();  // 销毁物品实体
    }

    // 正常生效的动画和音效
    public void triggerSuccessEffect() {
        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
        this.getWorld().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY(), this.getZ(), 1.0, 1.0, 1.0);
        this.kill();
    }

    // 检查是否有无懈可击反制
    public void checkForWuXie() {
        // 检查是否有玩家实体
        if (this.getOwner() instanceof ServerPlayerEntity player) {

            if (!canResponse || WuXieStack.getWuxieCnt(cardId) % 2 == 0) {
                executeTacticEffect(player);
                triggerSuccessEffect();
            } else {
                player.sendMessage(Text.of("该锦囊牌被无懈可击反制！"), false);
                triggerFailedEffect();
            }
        }
        else if (this.getOwner() instanceof GeneralEntity general) {
            if (!canResponse || WuXieStack.getWuxieCnt(cardId) % 2 == 0) {
                executeTacticEffect(general);
                triggerSuccessEffect();
            }
            triggerFailedEffect();
        }
        TacticCard.USED = false;
    }

    // 设置无懈可击可响应的状态
    public void setResponse(boolean canResponse) {
        this.canResponse = canResponse;
    }

    /**
     * 用于创建一个延迟的定时任务来处理锦囊卡牌的反制逻辑
     */
    public static ScheduledFuture<?> scheduleTacticCardEffect(TacticCardEntity tacticCardEntity) {
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            tacticCardEntity.checkForWuXie();
        }, 3, TimeUnit.SECONDS);  // 延迟3秒执行
        return future;
    }

    /**
     * 用于取消定时任务，防止重复执行
     */
    public static void cancelTacticCardEffect(ScheduledFuture<?> future) {
        // 取消任务
        if (future != null && !future.isCancelled()) {
            future.cancel(false);  // 参数false表示不会中断正在执行的任务
        }
    }

    // 获取卡牌ID
    public String getCardId() {
        return cardId;
    }

    // 设置卡牌ID
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public void executeTacticEffect(PlayerEntity player) {
    }

    public void executeTacticEffect(GeneralEntity general) {
    }

    public static ArrayList<LivingEntity> getTargets(PlayerEntity player) {
        Box box = player.getBoundingBox().expand(
                30000000, 30000000, 30000000
        );

        // 获取玩家实体
        List<ServerPlayerEntity> players = player.getWorld().getEntitiesByType(
                TypeFilter.instanceOf(ServerPlayerEntity.class),
                box,
                entity -> entity != player
        );
        ArrayList<LivingEntity> targetList = new ArrayList<>(players);

        // 添加 GeneralEntity
        List<GeneralEntity> generals = player.getWorld().getEntitiesByType(
                TypeFilter.instanceOf(GeneralEntity.class),
                box,
                entity -> true
        );
        targetList.addAll(generals);

        return targetList;
    }
    public static ArrayList<LivingEntity> getTargets(GeneralEntity general) {
        Box box = general.getBoundingBox().expand(
                30000000, 30000000, 30000000
        );
        List<ServerPlayerEntity> players = general.getWorld().getEntitiesByType(
                TypeFilter.instanceOf(ServerPlayerEntity.class),
                box,
                entity -> true
        );
        ArrayList<LivingEntity> targetList = new ArrayList<>(players);
        List<GeneralEntity> generals = general.getWorld().getEntitiesByType(
                TypeFilter.instanceOf(GeneralEntity.class),
                box,
                entity -> entity!=  general
        );
        targetList.addAll(generals);
        return targetList;
    }
}
