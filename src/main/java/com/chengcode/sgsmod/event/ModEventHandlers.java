package com.chengcode.sgsmod.event;

import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.item.ModItems;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

public class ModEventHandlers {
    public static void registerEvents() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!(entity instanceof PlayerEntity target)) return ActionResult.PASS;
            if (!CardGameManager.isInDyingState(target)) return ActionResult.PASS;

            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (stack.getItem() == ModItems.TAO) {
                    stack.decrement(1);
                    target.heal(5.0f);
                    CardGameManager.exitDyingState(target);
                    player.sendMessage(Text.of("你救了 " + target.getName().getString()), false);
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });
    }
}