package com.chengcode.sgsmod.manager;

import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;

public class EquipmentInventory implements ImplementedInventory {
    private final ItemStack stack;
    // 容量匹配MAX_EQUIPMENT_SLOTS=5
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(5, ItemStack.EMPTY);

    public EquipmentInventory(ItemStack stack) {
        this.stack = stack;
        // 读取装备区NBT（键名：EquipmentItems）
        NbtCompound tag = stack.getSubNbt("EquipmentItems");
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
        // 写入装备区NBT
        NbtCompound tag = stack.getOrCreateSubNbt("EquipmentItems");
        Inventories.writeNbt(tag, items);
    }
}