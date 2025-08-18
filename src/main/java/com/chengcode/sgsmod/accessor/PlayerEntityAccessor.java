package com.chengcode.sgsmod.accessor;

import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Unique;

public interface PlayerEntityAccessor {
    NbtCompound sgs$getHandData();

    void sgs$setHandData(NbtCompound nbt);

    @Unique
    NbtCompound sgsmod_1_20_1$getPersistentData();
    public static NbtCompound write(DefaultedList<ItemStack> items) {
        NbtCompound hand = new NbtCompound();
        Inventories.writeNbt(hand, items);
        return hand;
    }

    public static void read(NbtCompound handNbt, DefaultedList<ItemStack> itemsOut) {
        itemsOut.clear();
        for (int i = 0; i < itemsOut.size(); i++) itemsOut.set(i, ItemStack.EMPTY);
        if (handNbt != null) {
            Inventories.readNbt(handNbt, itemsOut);
        }
    }
}
