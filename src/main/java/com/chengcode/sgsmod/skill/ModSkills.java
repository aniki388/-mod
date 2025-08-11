package com.chengcode.sgsmod.skill;

import com.chengcode.sgsmod.accessor.PlayerEntityAccessor;
import com.chengcode.sgsmod.sound.SkillSoundManager;
import net.minecraft.nbt.*;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ModSkills {
    private static final Map<UUID, List<Skill>> playerSkills = new ConcurrentHashMap<>();
    private static final String NBT_KEY = "sgsmod_skills";

    public static void savePlayerSkills(ServerPlayerEntity player) {
        List<Skill> skills = playerSkills.getOrDefault(player.getUuid(), new ArrayList<>());
        NbtList skillList = new NbtList();

        for (Skill skill : skills) {
            NbtCompound skillTag = new NbtCompound();
            skillTag.putString("Id", skill.getId()); // 用枚举名存储
            skillList.add(skillTag);
        }

        NbtCompound playerData = ((PlayerEntityAccessor) player).sgsmod_1_20_1$getPersistentData();
        playerData.put(NBT_KEY, skillList);
    }

    public static void loadPlayerSkills(ServerPlayerEntity player) {
        NbtCompound playerData = ((PlayerEntityAccessor) player).sgsmod_1_20_1$getPersistentData();
        System.out.println("Loading skills from NBT: " + playerData);

        if (playerData.contains(NBT_KEY, NbtElement.LIST_TYPE)) {
            NbtList skillList = playerData.getList(NBT_KEY, NbtElement.COMPOUND_TYPE);
            System.out.println("Found skill list with size: " + skillList.size());

            List<Skill> skills = new ArrayList<>();
            for (int i = 0; i < skillList.size(); i++) {
                NbtCompound skillTag = skillList.getCompound(i);
                String id = skillTag.getString("Id");
                System.out.println("Loading skill: " + id);

                Skill skill = getSkillById(id);
                if (skill != null) skills.add(skill);
            }

            playerSkills.put(player.getUuid(), skills);
            System.out.println("Loaded skills: " + skills);
        } else {
            System.out.println("No skills found in NBT");
        }
    }

    public static void addSkill(ServerPlayerEntity player, Skill skill) {
        playerSkills.computeIfAbsent(player.getUuid(), k -> new ArrayList<>()).add(skill);
        SkillSoundManager.playSkillSound(skill.getId(), player);
        savePlayerSkills(player);
    }

    public static void removeSkill(ServerPlayerEntity player, String id) {
        List<Skill> skills = playerSkills.get(player.getUuid());
        if (skills != null) {
            skills.removeIf(skill -> skill.getId().equals(id));
            savePlayerSkills(player);
        }
    }

    public static boolean hasSkill(ServerPlayerEntity player, String id) {
        List<Skill> skills = playerSkills.get(player.getUuid());
        if (skills == null) return false;
        return skills.stream().anyMatch(skill -> skill.getId().equalsIgnoreCase(id));
    }

    public static List<Skill> getSkills(ServerPlayerEntity player) {
        return playerSkills.getOrDefault(player.getUuid(), Collections.emptyList());
    }

    public static void clearSkills(ServerPlayerEntity player) {
        playerSkills.remove(player.getUuid());
        savePlayerSkills(player);
    }

    public static Skill getSkillById(String id) {
        for (Skills s : Skills.values()) {
            if (s.name().equalsIgnoreCase(id)) {
                return getSkill(s);
            }
        }
        return null;
    }


    public static Skill getSkill(Skills skills) {
        switch (skills) {
            case wushuang:   return new WushuangSkill();
            case jieliegong: return new JieLiegongSkill();
            case kuanggu:    return new KuangguSkill();
            default:         return null;
        }
    }
}
