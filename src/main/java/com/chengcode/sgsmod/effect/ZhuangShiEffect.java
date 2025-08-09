package com.chengcode.sgsmod.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.network.ServerPlayerEntity;

public class ZhuangShiEffect extends StatusEffect {
    public ZhuangShiEffect() {
        // 正面效果，红色粒子
        super(StatusEffectCategory.BENEFICIAL, 0xFF4444);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        // 每 tick 执行
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getWorld().isClient) return;

        if (entity instanceof ServerPlayerEntity player) {
            // 这里可以实时检查 & 更新效果
            // amplifier = 弃牌/失去体力的数量 - 1
            // 例如，amplifier=2 表示玩家有 3 次特殊攻击机会
            // 你可以在你的出牌逻辑里去读取这个次数并应用
        }
    }
}
