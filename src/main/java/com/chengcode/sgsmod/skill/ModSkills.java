package com.chengcode.sgsmod.skill;

import com.chengcode.sgsmod.sound.SkillSoundManager;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ModSkills {
    private static final Map<UUID, List<Skill>> playerSkills = new ConcurrentHashMap<>();
    private static File skillsDataFolder;
    private static final Gson gson = new Gson();

    // 设置技能数据文件夹
    public static void initialize(MinecraftServer server) {
        if (server != null) {
            // 仅在服务器端初始化
            File dataDirectory = server.getRunDirectory();  // 获取服务器运行目录
            skillsDataFolder = new File(dataDirectory, "skills");

            if (!skillsDataFolder.exists()) {
                skillsDataFolder.mkdirs();  // 如果目录不存在则创建
            }
        }
    }

    // 保存玩家技能数据到文件
    public static void savePlayerSkills(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        List<Skill> skills = playerSkills.getOrDefault(playerId, new ArrayList<>());
        File file = new File(skillsDataFolder, playerId.toString() + ".json");

        SkillData skillData = new SkillData(skills);

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(skillData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 加载玩家技能数据
    public static void loadPlayerSkills(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        File file = new File(skillsDataFolder, playerId.toString() + ".json");

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                SkillData skillData = gson.fromJson(reader, SkillData.class);
                if (skillData != null) {
                    playerSkills.put(playerId, skillData.getSkills());
                }
            } catch (IOException | JsonIOException e) {
                e.printStackTrace();
            }
        }
    }


    // 添加技能
    public static void addSkill(ServerPlayerEntity player, Skill skill) {
        playerSkills.computeIfAbsent(player.getUuid(), k -> new ArrayList<>()).add(skill);
        SkillSoundManager.playSkillSound(skill.getName(), player);
    }

    // 移除技能
    public static void removeSkill(ServerPlayerEntity player, String skillName) {
        List<Skill> skills = playerSkills.get(player.getUuid());
        if (skills != null) {
            skills.removeIf(skill -> skill.getName().equals(skillName));
        }
    }


    // 判断玩家是否拥有某技能
    public static boolean hasSkill(ServerPlayerEntity player, String skillName) {
        List<Skill> skills = playerSkills.get(player.getUuid());
        if (skills == null) return false;
        return skills.stream().anyMatch(skill -> skill.getName().equals(skillName));
    }

    // 获取玩家技能列表
    public static List<Skill> getSkills(ServerPlayerEntity player) {
        return playerSkills.getOrDefault(player.getUuid(), Collections.emptyList());
    }

    // 清空玩家技能
    public static void clearSkills(ServerPlayerEntity player) {
        playerSkills.remove(player.getUuid());
    }

    // 提供静态访问的技能实例

    public static Skill getSkill(Skills skills) {
        switch (skills) {
            case Wushuang:
                return new WushuangSkill();
            case JieLiegong:
                return new JieLiegongSkill();
            case Kuanggu:
                return new KuangguSkill();
            default:
                return null;
        }
    }
}
