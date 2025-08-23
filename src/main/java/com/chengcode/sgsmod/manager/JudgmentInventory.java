package com.chengcode.sgsmod.manager;

import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;

public class JudgmentInventory implements ImplementedInventory {
    private final ItemStack stack;
    // 容量匹配MAX_JUDGMENT_SLOTS=2
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(3, ItemStack.EMPTY);

    public JudgmentInventory(ItemStack stack) {
        this.stack = stack;
        NbtCompound tag = stack.getSubNbt("JudgmentItems");
        if (tag != null) {
            Inventories.readNbt(tag, items);
        }
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public void markDirty() {
        NbtCompound tag = stack.getOrCreateSubNbt("JudgmentItems");
        Inventories.writeNbt(tag, items);
    }
}