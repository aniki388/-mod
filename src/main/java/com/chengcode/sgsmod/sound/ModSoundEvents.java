package com.chengcode.sgsmod.sound;

import com.chengcode.sgsmod.Sgsmod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSoundEvents {
    public static final SoundEvent SHA_ENTITY_HIT1 = register("sha_entity.hit1");
    public static final SoundEvent SHA_ENTITY_THROW = register("sha_entity.throw");
    public static final SoundEvent SHA_ENTITY_HIT2 = register("sha_entity.hit2");
    public static final SoundEvent SHAN = register("shan");
    public static final SoundEvent TAO_HEAL = register("tao_heal");
    public static final SoundEvent JIU_DRINK = register("jiu_drink");
    public static final SoundEvent MAD = register("mad");
    public static final SoundEvent WUSHUANG = register("wushuang");
    public static final SoundEvent WUXIE = register("wuxie");
    public static final SoundEvent WUZHONG = register("wuzhong");
    public static final SoundEvent LOSEHEALTH = register("losehealth");
    public static final SoundEvent SHUNSHOU = register("shunshou");
    public static final SoundEvent CHAIQIAO = register("chaiqiao");

    private static SoundEvent register(String name) {
        Identifier id = new Identifier(Sgsmod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void register() {
    }
}
