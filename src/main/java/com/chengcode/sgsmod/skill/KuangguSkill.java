package com.chengcode.sgsmod.skill;

import net.minecraft.text.Text;

public class KuangguSkill implements Skill{
    @Override
    public Text getDisplayName() {
        return Text.translatable("item.sgsmod.skill.kuanggu");
    }

    @Override
    public String getId() {
        return "kuanggu";
    }

}
