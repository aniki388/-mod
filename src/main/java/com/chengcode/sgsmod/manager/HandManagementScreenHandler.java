package com.chengcode.sgsmod.manager;

import com.chengcode.sgsmod.accessor.PlayerEntityAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class HandManagementScreenHandler extends ScreenHandler {

    private static final int HAND_SLOTS_PER_ROW = 8;
    private static final int HAND_ROWS = 2;
    private static final int MAX_HAND_SLOTS = HAND_SLOTS_PER_ROW * HAND_ROWS;
    private static final int MAX_EQUIPMENT_SLOTS = 5;
    private static final int MAX_JUDGMENT_SLOTS = 2;
    private static final int MAX_DISCARD_SLOTS = 6;

    private final PropertyDelegate propertyDelegate;
    public HandManagementScreenHandler(int syncId, PlayerInventory inventory, HandInventory handInv) {
        this(syncId, inventory, handInv, new SimpleInventory(18), new SimpleInventory(5), new SimpleInventory(6), 164);
    }

    public HandManagementScreenHandler(int i, PlayerInventory playerInventory) {
        this(i, playerInventory, new HandInventory(playerInventory.player.getStackInHand(playerInventory.player.getActiveHand())));
    }

    public HandManagementScreenHandler(int syncId,
                                       PlayerInventory playerInventory,
                                       Inventory handInv,
                                       Inventory equipmentInv,
                                       Inventory judgmentInv,
                                       Inventory discardInv,
                                       int initialCapacity) {
        super(ScreenHandlerType.GENERIC_9X6, syncId);

        this.propertyDelegate = new PropertyDelegate() {
            private int autoHand = 1;
            private int handCapacity = initialCapacity;
            private int discardCountdown = 0;

            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> autoHand;
                    case 1 -> handCapacity;
                    case 2 -> discardCountdown;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> autoHand = value;
                    case 1 -> handCapacity = value;
                    case 2 -> discardCountdown = value;
                }
            }

            @Override
            public int size() {
                return 3;
            }
        };

        int startX = 8;
        int startY = 18;

        // --- 手牌区 ---
        for (int row = 0; row < HAND_ROWS; row++) {
            for (int col = 0; col < HAND_SLOTS_PER_ROW; col++) {
                final int index = row * HAND_SLOTS_PER_ROW + col;
                addSlot(new Slot(handInv, index, startX + col * 18, startY + row * 18));
            }
            addLabelSlot(Items.PAPER, "§e手牌区 第" + (row + 1) + "行", startX + HAND_SLOTS_PER_ROW * 18, startY + row * 18);
        }

        startY += HAND_ROWS * 18;
        addLabelSlotFullRow(Items.GRAY_STAINED_GLASS_PANE, "§7手牌区分界线", startX, startY);

        // --- 装备 + 判定区 ---
        startY += 18;
        for (int i = 0; i < MAX_EQUIPMENT_SLOTS; i++) {
            addSlot(new Slot(equipmentInv, i, startX + i * 18, startY));
        }
        addLabelSlot(Items.PAPER, "§a装备区", startX + MAX_EQUIPMENT_SLOTS * 18, startY);

        for (int i = 0; i < MAX_JUDGMENT_SLOTS; i++) {
            addSlot(new Slot(judgmentInv, i, startX + (MAX_EQUIPMENT_SLOTS + i) * 18, startY) {
                @Override public boolean canInsert(ItemStack stack) { return false; }
            });
        }
        addLabelSlot(Items.PAPER, "§b判定区", startX + (MAX_EQUIPMENT_SLOTS + MAX_JUDGMENT_SLOTS) * 18, startY);

        startY += 18;
        addLabelSlotFullRow(Items.GRAY_STAINED_GLASS_PANE, "§7装备/判定区分界线", startX, startY);

        // --- 弃牌区 ---
        startY += 18;
        for (int i = 0; i < MAX_DISCARD_SLOTS; i++) {
            addSlot(new Slot(discardInv, i, startX + i * 18, startY) {
                @Override public boolean canInsert(ItemStack stack) { return true; }
                @Override public boolean canTakeItems(PlayerEntity player) { return false; }
            });
        }

        ItemStack toggleAutoHand = new ItemStack(Items.LEVER);
        toggleAutoHand.setCustomName(Text.of("§6自动手牌开关"));
        addSlot(new Slot(new SimpleInventory(1), 0, startX + (MAX_DISCARD_SLOTS - 2) * 18, startY) {
            @Override public boolean canInsert(ItemStack stack) { return false; }
            @Override public ItemStack getStack() { return toggleAutoHand; }
            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                if (!player.getWorld().isClient && player instanceof ServerPlayerEntity) {
                    int cur = propertyDelegate.get(0);
                    propertyDelegate.set(0, cur == 0 ? 1 : 0);
                }
            }
        });

        addLabelSlot(Items.PAPER, "§c弃牌区", startX + (MAX_DISCARD_SLOTS - 1) * 18, startY);
    }


    private void addLabelSlot(Item item, String displayName, int x, int y) {
        final ItemStack stack = new ItemStack(item);
        if (displayName != null && !displayName.isEmpty()) stack.setCustomName(Text.of(displayName));
        addSlot(new Slot(new SimpleInventory(1), 0, x, y) {
            @Override public boolean canInsert(ItemStack stack) { return false; }
            @Override public ItemStack getStack() { return stack; }
        });
    }

    private void addLabelSlotFullRow(Item item, String displayName, int startX, int y) {
        for (int i = 0; i < 9; i++) addLabelSlot(item, displayName, startX + i * 18, y);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override public boolean canUse(PlayerEntity player) { return true; }
}
