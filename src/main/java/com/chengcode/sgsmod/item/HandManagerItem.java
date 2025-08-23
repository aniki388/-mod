package com.chengcode.sgsmod.item;

import com.chengcode.sgsmod.Sgsmod;
import com.chengcode.sgsmod.manager.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class HandManagerItem extends Item {
    private static final int HAND_CAPACITY = 16; // 同步手牌区容量

    public HandManagerItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {
            ItemStack stack = user.getStackInHand(hand);
            user.openHandledScreen(createScreenHandlerFactory(stack));
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    private NamedScreenHandlerFactory createScreenHandlerFactory(ItemStack stack) {
        return new SimpleNamedScreenHandlerFactory((syncId, playerInventory, player) -> {
            // 获取服务端持久化的手牌区
            HandInventory handInv = new HandInventory(stack);
            EquipmentInventory equipInv = new EquipmentInventory(stack);
            JudgmentInventory judgeInv = new JudgmentInventory(stack);
            DiscardInventory discardInv = new DiscardInventory(stack);

            return new HandManagementScreenHandler(
                    syncId,
                    playerInventory,
                    handInv,
                    equipInv,
                    judgeInv,
                    discardInv,
                    164
            );
        }, Text.translatable(Sgsmod.MOD_ID,"gui.hand_manager"));
    }
}