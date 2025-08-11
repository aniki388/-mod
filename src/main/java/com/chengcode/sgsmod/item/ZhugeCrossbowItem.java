package com.chengcode.sgsmod.item;

import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;

import java.util.function.Predicate;

public class ZhugeCrossbowItem extends RangedWeaponItem {
    private static final int RANGE = 15;

    public ZhugeCrossbowItem(Settings settings) {
        super(settings);
    }

    @Override
    public Predicate<ItemStack> getProjectiles() {
        return stack -> stack.isOf(ModItems.SHA);
    }

    @Override
    public int getRange() {
        return RANGE;
    }
}
