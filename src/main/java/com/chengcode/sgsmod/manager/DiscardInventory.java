package com.chengcode.sgsmod.manager;

import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;

public class DiscardInventory implements ImplementedInventory {
    private final ItemStack stack;
    // 容量匹配MAX_DISCARD_SLOTS=6
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(7, ItemStack.EMPTY);

    public DiscardInventory(ItemStack stack) {
        this.stack = stack;
        NbtCompound tag = stack.getSubNbt("DiscardItems");
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
        NbtCompound tag = stack.getOrCreateSubNbt("DiscardItems");
        Inventories.writeNbt(tag, items);
    }
}