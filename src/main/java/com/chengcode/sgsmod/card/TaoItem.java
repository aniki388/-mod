package com.chengcode.sgsmod.card;

import com.chengcode.sgsmod.manager.CardGameManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import static com.chengcode.sgsmod.manager.CardGameManager.isInDyingState;

public class TaoItem extends Card {
    public TaoItem(Settings settings) {
        super(settings);
    }


    public TaoItem(Settings settings, int suit, int number, String baseId) {
        super(settings, suit, number, baseId);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            CardGameManager.recoverHealth(player, 5);
            player.sendMessage(Text.of("你使用了『桃』，恢复了5点生命值"), true);
            if (isInDyingState(player)) {
                CardGameManager.exitDyingState(player);
            }
        }
        ItemStack stack = player.getStackInHand(hand);
        stack.decrement(1);
        return TypedActionResult.success(stack, world.isClient());
    }
}
