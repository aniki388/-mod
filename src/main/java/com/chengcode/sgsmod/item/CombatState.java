package com.chengcode.sgsmod.item;

import com.chengcode.sgsmod.entity.GeneralEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatState {
    private static final Map<UUID, CombatState> stateMap = new HashMap<>();

    private long jiuEndTime = 0;


    public static CombatState get(PlayerEntity player) {
        return stateMap.computeIfAbsent(player.getUuid(), id -> new CombatState());
    }

    // 支持武将（或未来支持更多实体）
    public static CombatState get(GeneralEntity general) {
        return get(general.getUuid());
    }

    // 统一的核心方法
    public static CombatState get(UUID id) {
        return stateMap.computeIfAbsent(id, k -> new CombatState());
    }


    /**
     * 尝试饮酒，如果已经在效果中则返回 false
     */
    public boolean drinkJiu() {
        if (isJiuActive()) return false;  // 不允许叠加
        this.jiuEndTime = System.currentTimeMillis() + 5000; // 5 秒生效时间
        return true;
    }

    /**
     * 是否处于酒状态
     */
    public boolean isJiuActive() {
        return System.currentTimeMillis() <= jiuEndTime;
    }

    /**
     * 获取当前加成伤害（如果有）
     */
    public int getBonusDamage() {
        return isJiuActive() ? 5 : 0;
    }

    /**
     * 使用完后清除状态
     */
    public void clear() {
        jiuEndTime = 0;
    }
}
