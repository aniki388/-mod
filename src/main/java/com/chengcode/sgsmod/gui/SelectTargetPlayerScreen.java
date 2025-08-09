package com.chengcode.sgsmod.gui;

import com.chengcode.sgsmod.entity.GeneralEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class SelectTargetPlayerScreen extends Screen {

    private final PlayerEntity player;
    private ArrayList<LivingEntity> targets = new ArrayList<>();

    public SelectTargetPlayerScreen(PlayerEntity player) {
        super(Text.of("选择目标玩家"));
        this.player = player;
        this.targets = getTargets(player);
    }

    private ArrayList<LivingEntity> getTargets(PlayerEntity player) {
        ArrayList<LivingEntity> targetList = new ArrayList<>();
        Box box = player.getBoundingBox().expand(50);

        // 获取玩家实体
        List<ServerPlayerEntity> players = player.getWorld().getEntitiesByType(
                TypeFilter.instanceOf(ServerPlayerEntity.class),
                box,
                entity -> entity != player
        );
        targetList.addAll(players);

        // 添加 GeneralEntity
        List<GeneralEntity> generals = player.getWorld().getEntitiesByType(
                TypeFilter.instanceOf(GeneralEntity.class),
                box,
                entity -> true
        );
        targetList.addAll(generals);

        return targetList;
    }

    @Override
    protected void init() {
        super.init(); // 重要：调用父类初始化方法
        int buttonY = this.height / 2 - 100;  // 起始Y坐标
        int buttonHeight = 20;  // 每个按钮的高度
        int buttonSpacing = 5;  // 按钮之间的间距

        if (targets.size() == 0) {
            // 如果没有目标，显示一条提示信息
            this.addDrawableChild(ButtonWidget.builder(Text.of("没有可选目标！"), button -> {
                        if (this.client != null) {
                            this.client.setScreen(null);
                        }
                    })
                    .dimensions(this.width / 2 - 100, buttonY, 200, buttonHeight)
                    .build());
        } else {
            // 创建目标玩家选择按钮
            for (int i = 0; i < targets.size(); i++) {
                LivingEntity targetEntity = targets.get(i);
                this.addDrawableChild(ButtonWidget.builder(
                                Text.of(targetEntity.getName().getString()),
                                button -> {
                                    // 选择目标后打开选择牌区界面
                                    if (this.client != null) {
                                        this.client.setScreen(new SelectCardAreaScreen(player, targetEntity));
                                    }
                                })
                        .dimensions(this.width / 2 - 100, buttonY + i * (buttonHeight + buttonSpacing), 200, buttonHeight)
                        .build());
            }
        }
    }

    public ArrayList<LivingEntity> getTargets() {
        return targets;
    }
}
