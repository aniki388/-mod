package com.chengcode.sgsmod.combat;

import com.chengcode.sgsmod.sound.ModSoundEvents;
import net.minecraft.sound.SoundEvent;

public enum AttackLevel {
    LEVEL1(new SoundEvent[]{ModSoundEvents.SHA_ENTITY_HIT1}),
    LEVEL2(new SoundEvent[]{ModSoundEvents.SHA_ENTITY_HIT1}),
    LEVEL3(new SoundEvent[]{ModSoundEvents.SHA_ENTITY_HIT2}),
    LEVEL4(new SoundEvent[]{ModSoundEvents.SHA_ENTITY_HIT2, ModSoundEvents.MAD}),
    LEVEL5(new SoundEvent[]{ModSoundEvents.SHA_ENTITY_HIT2, ModSoundEvents.WUSHUANG});

    private final SoundEvent[] sounds;

    AttackLevel(SoundEvent[] sounds) {
        this.sounds = sounds;
    }

    public SoundEvent[] getSounds() {
        return sounds;
    }

    public static AttackLevel fromDamage(int damage) {
        if (damage >= 20) return LEVEL5;
        else if (damage >= 15) return LEVEL4;
        else if (damage >= 10) return LEVEL3;
        else if (damage >= 5) return LEVEL2;
        else return LEVEL1;
    }
}