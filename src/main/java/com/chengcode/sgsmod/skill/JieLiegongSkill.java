package com.chengcode.sgsmod.skill;

import net.minecraft.text.Text;

public class JieLiegongSkill implements Skill {
    @Override
    public String getId() {
        return "jieliegong";
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("item.sgsmod.skill.jieliegong");
    }
}
