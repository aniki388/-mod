package com.chengcode.sgsmod.manager;

import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.nbt.NbtCompound;

public class HandInventory implements ImplementedInventory {
    private final ItemStack stack;
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(16, ItemStack.EMPTY);

    public HandInventory(ItemStack stack) {
        this.stack = stack;
        NbtCompound tag = stack.getSubNbt("HandItems");

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
        NbtCompound tag = stack.getOrCreateSubNbt("HandItems");
        Inventories.writeNbt(tag, items);
    }
}
