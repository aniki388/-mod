package com.chengcode.sgsmod.command;

import com.chengcode.sgsmod.entity.GeneralEntity;
import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.skill.*;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    literal("sgsmod")
                            .requires(source -> source.hasPermissionLevel(2)) // OP 权限

                            // 测试模式关闭
                            .then(literal("testmode")
                                    .then(literal("off")
                                            .executes(context -> {
                                                CardGameManager.clearAll();
                                                ServerCommandSource source = context.getSource();
                                                source.sendFeedback(() -> Text.literal("§c测试模式已关闭，停止发牌任务。"), false);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )

                            .then(literal("cleargeneral")
                                    .requires(source -> source.hasPermissionLevel(2)) // 仅OP可用
                                    .executes(context -> {
                                        ServerCommandSource source = context.getSource();
                                        Box worldBounds = new Box(
                                                -30000000, -30000000, -30000000,
                                                30000000, 30000000, 30000000
                                        );
                                        var world = source.getWorld();
                                        var generals = world.getEntitiesByClass(GeneralEntity.class, worldBounds, e -> true);
                                        for (GeneralEntity ge : generals) {
                                            ge.remove(Entity.RemovalReason.KILLED);
                                        }
                                        source.sendFeedback(() -> net.minecraft.text.Text.literal("移除GeneralEntity数量：" + generals.size()), true);
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )

                            // 添加技能
                            .then(literal("addskill")
                                    .then(argument("skill", StringArgumentType.word())
                                            .executes(context -> {
                                                ServerPlayerEntity player = context.getSource().getPlayer();
                                                String skillName = StringArgumentType.getString(context, "skill");

                                                Skill skill = resolveSkillByName(skillName);
                                                if (skill == null) {
                                                    player.sendMessage(Text.literal("未知技能: " + skillName), false);
                                                    return 0;
                                                }

                                                ModSkills.addSkill(player, skill);
                                                player.sendMessage(Text.literal("你获得了技能「").append(skill.getDisplayName()).append("」！"), false);
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

                                                Skill skill = resolveSkillByName(skillName);
                                                if (skill == null) {
                                                    player.sendMessage(Text.literal("未知技能: " + skillName), false);
                                                    return 0;
                                                }

                                                ModSkills.removeSkill(player, skill.getId());
                                                player.sendMessage(Text.literal("已移除技能: ").append(skill.getDisplayName()), false);
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
                                            player.sendMessage(Text.literal("你没有任何技能"), false);
                                        } else {
                                            player.sendMessage(Text.literal("当前技能: "), false);
                                            for (Skill s : skills) {
                                                player.sendMessage(Text.literal(" - ").append(s.getDisplayName()), false);
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
                                        player.sendMessage(Text.literal("已清除所有技能"), false);
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(literal("pindian")
                                    // 指定目标玩家
                                    .then(argument("target", StringArgumentType.word())
                                            .executes(context -> {
                                                ServerPlayerEntity player = context.getSource().getPlayer();
                                                String targetName = StringArgumentType.getString(context, "target");
                                                ServerPlayerEntity target = context.getSource().getServer().getPlayerManager().getPlayer(targetName);

                                                if (target == null) {
                                                    player.sendMessage(Text.literal("§c找不到玩家: " + targetName), false);
                                                    return 0;
                                                }

                                                player.sendMessage(Text.literal("§e你向 " + target.getName().getString() + " 发起了拼点！"), false);
                                                target.sendMessage(Text.literal("§e" + player.getName().getString() + " 向你发起了拼点！"), false);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )

                                    // 不指定目标（默认自己）
                                    .executes(context -> {
                                        ServerPlayerEntity player = context.getSource().getPlayer();

//                                        PinManager.startPinDian(context.getSource().getServer(), player, target);
                                        player.sendMessage(Text.literal("§e你和自己发起了拼点（测试用）。"), false);

                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
            );
        });
    }

    // 技能名称解析（支持英文枚举名和中文名）
    private static Skill resolveSkillByName(String name) {
        return switch (name.toLowerCase()) {
            case "wushuang", "无双" -> new WushuangSkill();
            case "jieliegong", "烈弓" -> new JieLiegongSkill();
            case "kuanggu", "狂骨" -> new KuangguSkill();
            default -> null;
        };
    }
}
