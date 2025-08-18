package com.chengcode.sgsmod.item;

import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.sound.SkillSoundManager;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class KurouItem extends Item {
    public KurouItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.PASS; // 禁止在方块上使用，防止误触
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            if (player.getHealth() > 5) {
                CardGameManager.loseHealth(player, 5.0f);
                CardGameManager.giveCard(player,2);
                SkillSoundManager.playSkillSound("kurou", player);
            }else {
                player.sendMessage(Text.of("再苦下去就要没了..."), false);
            }
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.sgsmod.skill.kurou_description", ""));
    }
}
