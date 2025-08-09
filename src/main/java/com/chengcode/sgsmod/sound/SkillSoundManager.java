package com.chengcode.sgsmod.sound;

import com.chengcode.sgsmod.Sgsmod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import java.util.*;

public class SkillSoundManager {
    private static final Map<String, List<SoundEvent>> skillSounds = new HashMap<>();
    private static final Random RANDOM = new Random();


    public static void init() {
        registerSkill("wushuang", 2);
        registerSkill("jieliegong", 2);
        registerSkill("kuanggu", 2);
        registerSkill("kurou",2);
        registerSkill("zhuangshi",2);
        // registerSkill("qice", 2);
    }

    public static void registerSkill(String skillId, int clipCount) {
        List<SoundEvent> soundList = new ArrayList<>();
        for (int i = 1; i <= clipCount; i++) {
            Identifier id = new Identifier(Sgsmod.MOD_ID, skillId + "_" + i);
            soundList.add(Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id)));
        }
        skillSounds.put(skillId, soundList);
    }

    public static List<SoundEvent> getSkillSounds(String skillId) {
        return skillSounds.get(skillId);
    }


    public static void playSkillSound(String skillId, LivingEntity entity) {

        List<SoundEvent> sounds = skillSounds.get(skillId);
        if (sounds == null || sounds.isEmpty()) return;

        SoundEvent selected = sounds.get(RANDOM.nextInt(sounds.size()));
        ServerWorld world = (ServerWorld) entity.getWorld();

        world.playSound(null, entity.getBlockPos(), selected, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }


}
