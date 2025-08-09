package com.chengcode.sgsmod.effect;

import com.chengcode.sgsmod.Sgsmod;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEffects {
    public static final StatusEffect ZHUANGSHI = registerEffects("zhuangshi", new ZhuangShiEffect());

    public static StatusEffect registerEffects(String id, StatusEffect effect) {
        return Registry.register(Registries.STATUS_EFFECT, new Identifier(Sgsmod.MOD_ID, id), effect);
    }

    public static void registerEffects() {
    }
}
