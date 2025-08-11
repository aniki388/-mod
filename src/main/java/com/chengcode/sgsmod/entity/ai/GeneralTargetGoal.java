package com.chengcode.sgsmod.entity.ai;

import com.chengcode.sgsmod.entity.GeneralEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;

public class GeneralTargetGoal extends ActiveTargetGoal<PlayerEntity> {

    private final GeneralEntity general;

    public GeneralTargetGoal(GeneralEntity mob) {
        super(mob, PlayerEntity.class, true);
        this.general = mob;
    }

    @Override
    public boolean canStart() {
        // 先调用父类判断
        if (!super.canStart()) {
            return false;
        }
        // 额外判断：目标是否在enemies且不在rescuers集合中
        LivingEntity target = this.mob.getTarget();
        if (target == null) return false;

        return general.getEnemies().contains(target.getUuid())
                && !general.getRescuers().contains(target.getUuid());
    }

    @Override
    protected boolean canTrack(LivingEntity entity, net.minecraft.entity.ai.TargetPredicate targetPredicate) {
        // 判断玩家是否在敌人列表，且不在救援者列表
        if (!(entity instanceof PlayerEntity player)) return false;

        if (!general.getEnemies().contains(player.getUuid())) return false;
        if (general.getRescuers().contains(player.getUuid())) return false;

        return super.canTrack(entity, targetPredicate);
    }
}
