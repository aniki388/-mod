package com.chengcode.sgsmod.network;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * 客户端本地手牌缓存（SimpleInventory）
 * - 服务端 SYNC 包会写入这里
 * - GUI 可以通过 getHandInventory() 读取并渲染
 */
public class ClientHandCache {

    private static final int DEFAULT_HAND_SLOTS = 16; // 与服务端 MAX_HAND_SLOTS 保持一致或更大
    private static SimpleInventory handInv = new SimpleInventory(DEFAULT_HAND_SLOTS);

    /**
     * 将 ItemStack 数组写入客户端缓存（覆盖）
     */
    public static void setStacks(ItemStack[] stacks) {
        if (stacks == null) return;
        // 如果传入数组比缓存大，重新分配（防御式）
        if (stacks.length != handInv.size()) {
            handInv = new SimpleInventory(Math.max(stacks.length, handInv.size()));
        }
        for (int i = 0; i < handInv.size(); i++) {
            if (i < stacks.length) handInv.setStack(i, stacks[i].copy());
            else handInv.setStack(i, ItemStack.EMPTY);
        }
    }

    /**
     * 直接填充到当前位置（索引必须在范围内）
     */
    public static void setStack(int index, ItemStack stack) {
        if (index < 0) return;
        if (index >= handInv.size()) {
            SimpleInventory inv = new SimpleInventory(index + 1);
            for (int i = 0; i < inv.size(); i++) {
                inv.setStack(i, i < handInv.size() ? handInv.getStack(i) : ItemStack.EMPTY);
            }
            handInv = inv;
        }
        handInv.setStack(index, stack.copy());
    }

    /**
     * 返回客户端缓存（只读语义，调用者可复制需要的 ItemStack）
     */
    public static SimpleInventory getHandInventory() {
        return handInv;
    }

    /**
     * 将缓存序列化为 ItemStack 数组（用于发送 update packet）
     */
    public static ItemStack[] toItemStackArray() {
        ItemStack[] arr = new ItemStack[handInv.size()];
        for (int i = 0; i < handInv.size(); i++) arr[i] = handInv.getStack(i).copy();
        return arr;
    }

    /**
     * 可用于调试：标注某个槽为说明
     */
    public static void setLabel(int slot, String text) {
        if (slot < 0 || slot >= handInv.size()) return;
        ItemStack s = handInv.getStack(slot);
        if (s.isEmpty()) {
            // 放一个纸张表示
            s = new ItemStack(net.minecraft.item.Items.PAPER);
        }
        s.setCustomName(Text.of(text));
        handInv.setStack(slot, s);
    }
}
