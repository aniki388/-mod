package com.chengcode.sgsmod.manager;

import com.chengcode.sgsmod.accessor.PlayerEntityAccessor;
import com.chengcode.sgsmod.card.Card;
import com.chengcode.sgsmod.item.HandManagerItem;
import com.chengcode.sgsmod.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
    private static final int MAX_JUDGMENT_SLOTS = 3;
    private static final int MAX_DISCARD_SLOTS = 7;

//    private final PropertyDelegate propertyDelegate;

//    private static PropertyDelegate createPropertyDelegate(int initialCapacity) {
//        return new PropertyDelegate() {
//            private int autoHand = 1;
//            private int handCapacity = initialCapacity;
//            private int discardCountdown = 0;
//
//            @Override
//            public int get(int index) {
//                if (index < 0 || index >= size()) return 0;
//                return switch (index) {
//                    case 0 -> autoHand;
//                    case 1 -> handCapacity;
//                    case 2 -> discardCountdown;
//                    default -> 0;
//                };
//            }
//
//            @Override
//            public void set(int index, int value) {
//                if (index < 0 || index >= size()) return;
//                switch (index) {
//                    case 0 -> autoHand = value;
//                    case 1 -> handCapacity = value;
//                    case 2 -> discardCountdown = value;
//                }
//            }
//
//            @Override
//            public int size() {
//                return 3;
//            }
//        };
//    }

    // 修正：第一个构造方法应使用正确的自定义Inventory类型
    public HandManagementScreenHandler(int syncId, PlayerInventory playerInventory, HandInventory handInv) {
        this(syncId, playerInventory, handInv,
                new EquipmentInventory(playerInventory.player.getStackInHand(playerInventory.player.getActiveHand())),
                new JudgmentInventory(playerInventory.player.getStackInHand(playerInventory.player.getActiveHand())),
                new DiscardInventory(playerInventory.player.getStackInHand(playerInventory.player.getActiveHand())),
                164);
    }

    public HandManagementScreenHandler(int i, PlayerInventory playerInventory) {
        this(i, playerInventory, new HandInventory(playerInventory.player.getStackInHand(playerInventory.player.getActiveHand())));
    }

    // 主构造方法保持参数类型为自定义Inventory
    public HandManagementScreenHandler(int syncId,
                                       PlayerInventory playerInventory,
                                       HandInventory handInv,
                                       EquipmentInventory equipmentInv,
                                       JudgmentInventory judgmentInv,
                                       DiscardInventory discardInv,
                                       int initialCapacity) {
        super(ScreenHandlerType.GENERIC_9X6, syncId);
        /*this.propertyDelegate = createPropertyDelegate(initialCapacity);
        addProperties(this.propertyDelegate); // 添加这行以注册属性代理*/
        int startX = 8;
        int startY = 18;
        int capacity = CardGameManager.getHandCapacity(playerInventory.player);
        // --- 手牌区 ---
        for (int row = 0; row < HAND_ROWS; row++) {
            for (int col = 0; col < HAND_SLOTS_PER_ROW; col++) {
                final int index = row * HAND_SLOTS_PER_ROW + col;
                addSlot(new Slot(handInv, index, startX + col * 18, startY + row * 18) {
                    // 允许插入任何物品进行测试，后续可根据需要限制
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return stack.getItem() instanceof Card;
                    }
                });
            }
            addLabelSlot(Items.PAPER, "§e手牌区 第" + (row + 1) + "行 手牌上限：" + capacity, startX + HAND_SLOTS_PER_ROW * 18, startY + row * 18);
        }

        startY += HAND_ROWS * 18;
        addLabelSlotFullRow(Items.GRAY_STAINED_GLASS_PANE, "§7手牌区分界线", startX, startY);

        // --- 装备 + 判定区 ---
        startY += 18;
        for (int i = 0; i < MAX_EQUIPMENT_SLOTS; i++) {
            addSlot(new Slot(equipmentInv, i, startX + i * 18, startY){
                @Override
                public boolean canInsert(ItemStack stack) {
                    return stack.getItem() instanceof Card;
                }
            });
        }
        addLabelSlot(Items.PAPER, "§a装备区/§b判定区", startX + MAX_EQUIPMENT_SLOTS * 18, startY);

        for (int i = 0; i < MAX_JUDGMENT_SLOTS; i++) {
            addSlot(new Slot(judgmentInv, i, startX + (MAX_EQUIPMENT_SLOTS + i) * 18, startY) {
                @Override public boolean canInsert(ItemStack stack) { return false; }
            });
        }

        startY += 18;
        addLabelSlotFullRow(Items.GRAY_STAINED_GLASS_PANE, "§7装备/判定区分界线", startX, startY);

        // --- 弃牌区 ---
        startY += 18;
        for (int i = 0; i < MAX_DISCARD_SLOTS; i++) {
            addSlot(new Slot(discardInv, i, startX + i * 18, startY) {
                @Override public boolean canInsert(ItemStack stack) {
                    int currentHand = 0;
                    for(ItemStack s : handInv.getItems()){
                        if(!s.isEmpty()) currentHand++;
                    }
                    int maxHand = CardGameManager.getHandCapacity(playerInventory.player);
                    return currentHand > maxHand;
                }
                @Override public boolean canTakeItems(PlayerEntity player) { return false; }
            });
        }

        ItemStack toggleAutoHand = new ItemStack(Items.LEVER);

        String turnOn = "§a开";
        String turnOff = "§c关";
        boolean initState = CardGameManager.getAutoHand(playerInventory.player);
        toggleAutoHand.setCustomName(Text.of("§6自动手牌开关 当前状态：" + (initState ? turnOn : turnOff)));
        addSlot(new Slot(new SimpleInventory(1), 0, startX + (MAX_DISCARD_SLOTS - 2) * 18, startY) {
            @Override public boolean canInsert(ItemStack stack) { return false; }
            @Override public ItemStack getStack() { return toggleAutoHand; }
            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                if (!player.getWorld().isClient && player instanceof ServerPlayerEntity) {
                    boolean newState = CardGameManager.toggleAutoHand(player);
                    toggleAutoHand.setCustomName(Text.of("§6自动手牌开关 当前状态：" + (newState ? turnOn : turnOff)));
                }
            }

            @Override
            public boolean canTakeItems(PlayerEntity player) {
                // 拦截点击
                if (!player.getWorld().isClient && player instanceof ServerPlayerEntity) {
                    boolean newState = CardGameManager.toggleAutoHand(player);
                    this.getStack().setCustomName(Text.of("§6自动手牌开关 当前状态：" + (newState ? "§a开" : "§c关")));
                }
                return false; // 禁止真正拿走物品
            }
        });

        int currentHand = 0;
        for(ItemStack s : handInv.getItems()){
            if(!s.isEmpty()) currentHand+=s.getCount();
        }
        int maxHand = CardGameManager.getHandCapacity(playerInventory.player);
        for(int i = 0;i<playerInventory.player.getInventory().size();i++) {
            ItemStack stack = playerInventory.player.getInventory().getStack(i);
            if(!stack.isEmpty() && stack.getItem() instanceof Card) currentHand+=stack.getCount();
        }
        int discard = currentHand - maxHand;
        addLabelSlot(Items.PAPER, "§c弃牌区" + (discard > 0 ? "§6(" + discard + ")" : ""), startX + (MAX_DISCARD_SLOTS - 1) * 18, startY);

        // 添加玩家背包槽位，使玩家可以从背包移动物品到手牌区
        int playerInventoryStartY = startY + 50;
        // 玩家背包主槽位（3行）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int x = startX + col * 18;
                int y = playerInventoryStartY + row * 18;
                addSlot(new Slot(playerInventory, col + row * 9 + 9, x, y));
            }
        }
        // 玩家快捷栏（1行）
        for (int col = 0; col < 9; col++) {
            int x = startX + col * 18;
            int y = playerInventoryStartY + 3 * 18 + 4;
            addSlot(new Slot(playerInventory, col, x, y));
        }
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
        ItemStack movedStack = ItemStack.EMPTY;
        Slot slotObject = this.slots.get(slot);

        if (slotObject.hasStack()) {
            ItemStack originalStack = slotObject.getStack();
            movedStack = originalStack.copy();

            // 实现简单的快速移动逻辑
            if (slot < MAX_HAND_SLOTS + MAX_EQUIPMENT_SLOTS + MAX_JUDGMENT_SLOTS + MAX_DISCARD_SLOTS) {
                // 从手牌/装备/判定/弃牌区移动到玩家背包
                if (!this.insertItem(originalStack, MAX_HAND_SLOTS + MAX_EQUIPMENT_SLOTS + MAX_JUDGMENT_SLOTS + MAX_DISCARD_SLOTS + 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 从玩家背包移动到手牌区
                if (!this.insertItem(originalStack, 0, MAX_HAND_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slotObject.setStack(ItemStack.EMPTY);
            } else {
                slotObject.markDirty();
            }
        }

        return movedStack;
    }

    @Override public boolean canUse(PlayerEntity player) {
        // 确保只有持有手牌管理器的玩家才能使用
        return true;
    }
}
