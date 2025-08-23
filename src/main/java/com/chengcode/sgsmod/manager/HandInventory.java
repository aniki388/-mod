package com.chengcode.sgsmod.manager;

import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.nbt.NbtCompound;

import java.util.List;
import java.util.Random;

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
    public boolean addItem(ItemStack stack) {
        // 遍历空槽位，找到第一个空位插入（而非直接 add，避免列表扩容问题）
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, stack);
                markDirty();
                return true; // 添加成功
            }
        }
        return false; // 无空槽位，添加失败
    }

    public ItemStack getRandomCard(Random random) {
        List<ItemStack> nonEmpty = this.items.stream()
                .filter(stack -> !stack.isEmpty())
                .toList();

        if (nonEmpty.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return nonEmpty.get(random.nextInt(nonEmpty.size()));
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
