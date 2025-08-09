package com.chengcode.sgsmod.entity.goal;

import com.chengcode.sgsmod.entity.GeneralEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;

public class RescuerActiveTargetGoal extends ActiveTargetGoal<PlayerEntity> {

    public RescuerActiveTargetGoal(PathAwareEntity entity, Class<PlayerEntity> targetClass, boolean checkVisibility) {
        super(entity, targetClass, checkVisibility);
    }

    @Override
    public boolean canStart() {
        // 在检查目标之前，先检查rescuers列表中的玩家是否是目标
        if (this.mob instanceof GeneralEntity general) {
            // 获取最近的玩家
            PlayerEntity target = this.mob.getWorld().getClosestPlayer(
                    this.mob.getX(), this.mob.getY(), this.mob.getZ(), 16,
                    p -> p.isAlive() && !general.getRescuers().contains(p.getUuid())  // 排除rescuers中的玩家
            );

            // 如果有符合条件的玩家，才开始执行目标设置
            if (target != null) {
                this.target = target;
                return true;
            }
        }
        return false;
    }
}