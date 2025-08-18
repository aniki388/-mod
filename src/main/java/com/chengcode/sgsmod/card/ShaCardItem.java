package com.chengcode.sgsmod.card;

import com.chengcode.sgsmod.entity.ShaEntity;
import com.chengcode.sgsmod.card.Card;
import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.skill.Skills;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import com.chengcode.sgsmod.sound.SkillSoundManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ShaCardItem extends Card {
    public ShaCardItem(Settings settings) {
        super(settings);
    }


    public ShaCardItem(Settings settings, int suit, int number, String baseId) {
        super(settings, suit, number, baseId);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        // 播放投掷音效
        world.playSound(
                null,
                user.getX(),
                user.getY(),
                user.getZ(),
                ModSoundEvents.SHA_ENTITY_THROW,
                SoundCategory.PLAYERS,
                1.0F,
                1.0F
        );
        if (!world.isClient) {
            ShaEntity shaEntity = new ShaEntity(user, world);
            shaEntity.setItem(itemStack.copy());
            shaEntity.setRedColor(this.getColor() == Card.RED);
            shaEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
            world.spawnEntity(shaEntity);
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!user.getAbilities().creativeMode) {
            CardGameManager.discard(itemStack.copy());
            itemStack.decrement(1);
        }



        if (this.getColor() == Card.RED && ShaEntity.hasSkill(user, Skills.jiang))
        {
            user.sendMessage(Text.of("发动技能【激昂】"),true);
            SkillSoundManager.playSkillSound("jiang", user);
            CardGameManager.giveCard(user,1);
        }

        user.getItemCooldownManager().set(this, 60);

        return TypedActionResult.success(itemStack, world.isClient());
    }
}
