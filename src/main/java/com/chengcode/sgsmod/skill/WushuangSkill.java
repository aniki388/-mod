package com.chengcode.sgsmod.skill;

import com.chengcode.sgsmod.item.ModItems;
import com.chengcode.sgsmod.network.ServerReceiver;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class WushuangSkill implements Skill {
    @Override
    public String getId() {
        return "Wushuang"; // 用于保存到 NBT
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("item.sgsmod.skill.wushuang"); // 显示
    }
}

