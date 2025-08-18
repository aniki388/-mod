package com.chengcode.sgsmod.card;

import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.item.CombatState;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import static com.chengcode.sgsmod.manager.CardGameManager.isInDyingState;

public class JiuItem extends Card {
    public JiuItem(Settings settings) {
        super(settings);
    }

    public JiuItem(Settings settings, int suit, int number, String baseId) {
        super(settings, suit, number, baseId);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            CombatState.get(player).drinkJiu();
            if (isInDyingState(player)) {
                player.sendMessage(Text.of("你饮用了『酒』，脱离濒死状态）"), true);
                CardGameManager.exitDyingState(player);
            }else {
            player.sendMessage(Text.of("你饮用了『酒』，下次『杀』伤害+5（持续5秒）"), true);
            }
            world.playSound(null, player.getBlockPos(), ModSoundEvents.JIU_DRINK, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }


        ItemStack stack = player.getStackInHand(hand);
        CardGameManager.discard(stack.copy());
        stack.decrement(1);
        return TypedActionResult.success(stack, world.isClient());
    }
}
