package com.chengcode.sgsmod.skill;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public interface Skill {
    Text getDisplayName();
    String getId();
}
