package com.chengcode.sgsmod.gui;

import com.chengcode.sgsmod.manager.CardGameManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class SelectCardAreaScreen extends Screen {

    private final PlayerEntity player;
    private final LivingEntity targetPlayer;

    public SelectCardAreaScreen(PlayerEntity player, LivingEntity targetPlayer) {
        super(Text.of("选择牌区"));
        this.player = player;
        this.targetPlayer = targetPlayer;
    }

    @Override
    protected void init() {
        int buttonY = this.height / 2 - 50;  // 按钮位置

        // 创建选择“判定区”按钮
        this.addDrawableChild(ButtonWidget.builder(Text.of("判定区（没做完）"), button -> {
            executeCardEffect("PD");
        }).dimensions(this.width / 2 - 100, buttonY, 200, 20).build());

        // 创建选择“手牌区”按钮
        this.addDrawableChild(ButtonWidget.builder(Text.of("手牌区"), button -> {
            executeCardEffect("SP");
        }).dimensions(this.width / 2 - 100, buttonY + 30, 200, 20).build());

        // 创建选择“装备区”按钮
        this.addDrawableChild(ButtonWidget.builder(Text.of("装备区"), button -> {
            executeCardEffect("ZB");
        }).dimensions(this.width / 2 - 100, buttonY + 60, 200, 20).build());

        // 创建选择“物品栏”按钮
        this.addDrawableChild(ButtonWidget.builder(Text.of("物品栏"), button -> {
            executeCardEffect("WP");
        }).dimensions(this.width / 2 - 100, buttonY + 90, 200, 20).build());
    }

    private void executeCardEffect(String area) {
        // 处理拆卡逻辑
        CardGameManager.removeRandomCard(targetPlayer, area);

        // 关闭当前界面
        client.setScreen(null);

        // 播放效果和反馈信息
        player.sendMessage(Text.of("“过河拆桥”生效！"), false);
        targetPlayer.sendMessage(Text.of("被" + player.getName().getString() + "的“过河拆桥”效果命中！"));
    }
}
