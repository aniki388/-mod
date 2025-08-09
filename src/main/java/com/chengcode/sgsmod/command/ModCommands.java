package com.chengcode.sgsmod.command;

import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.skill.ModSkills;
import com.chengcode.sgsmod.skill.Skill;
import com.chengcode.sgsmod.skill.WushuangSkill;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    literal("sgsmod")
                            .requires(source -> source.hasPermissionLevel(2)) // 仅OP可用
                            .then(literal("testmode")
                                    .then(literal("off")
                                            .executes(context -> {
                                                CardGameManager.clearAll();
                                                ServerCommandSource source = context.getSource();
                                                source.sendFeedback(() -> Text.of("§c测试模式已关闭，停止发牌任务。"), false);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )

                            // 添加技能
                            .then(literal("addskill")
                                    .then(argument("skill", StringArgumentType.word())
                                            .executes(context -> {
                                                ServerPlayerEntity player = context.getSource().getPlayer();
                                                String skillName = StringArgumentType.getString(context, "skill");

                                                Skill skill = resolveSkillByName(skillName);
                                                if (skill == null) {
                                                    player.sendMessage(Text.of("未知技能: " + skillName), false);
                                                    return 0;
                                                }

                                                ModSkills.addSkill(player, skill);
                                                player.sendMessage(Text.of("已添加技能: " + skill.getName()), false);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )

                            // 移除技能
                            .then(literal("removeskill")
                                    .then(argument("skill", StringArgumentType.word())
                                            .executes(context -> {
                                                ServerPlayerEntity player = context.getSource().getPlayer();
                                                String skillName = StringArgumentType.getString(context, "skill");

                                                ModSkills.removeSkill(player, skillName);
                                                player.sendMessage(Text.of("已移除技能: " + skillName), false);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )

                            // 列出技能
                            .then(literal("listskills")
                                    .executes(context -> {
                                        ServerPlayerEntity player = context.getSource().getPlayer();
                                        var skills = ModSkills.getSkills(player);
                                        if (skills.isEmpty()) {
                                            player.sendMessage(Text.of("你没有任何技能"), false);
                                        } else {
                                            player.sendMessage(Text.of("当前技能: "), false);
                                            for (Skill s : skills) {
                                                player.sendMessage(Text.of(" - " + s.getName()), false);
                                            }
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )

                            // 清空技能
                            .then(literal("clearskills")
                                    .executes(context -> {
                                        ServerPlayerEntity player = context.getSource().getPlayer();
                                        ModSkills.clearSkills(player);
                                        player.sendMessage(Text.of("已清除所有技能"), false);
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
            );
        });
    }

    // 简单的技能名称映射函数
    private static Skill resolveSkillByName(String name) {
        return switch (name.toLowerCase()) {
            case "wushuang", "无双" -> new WushuangSkill();
            default -> null;
        };
    }
}
