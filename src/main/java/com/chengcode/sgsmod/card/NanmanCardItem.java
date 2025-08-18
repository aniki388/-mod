package com.chengcode.sgsmod.card;

import com.chengcode.sgsmod.entity.ModEntities;
import com.chengcode.sgsmod.entity.NanmanCardEntity;
import com.chengcode.sgsmod.entity.TacticCardEntity;
import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.manager.WuXieStack;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class NanmanCardItem extends TacticCard{
    public NanmanCardItem(Settings settings) {
        super(settings);
    }



    public NanmanCardItem(Settings settings, int suit, int number, String baseId) {
        super(settings, suit, number,baseId);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        NanmanCardEntity nanmanCardEntity = new NanmanCardEntity(ModEntities.NANMAN_CARD_ENTITY, world);
        nanmanCardEntity.setOwner(user);
        boolean canResponse = true;
        nanmanCardEntity.setResponse(canResponse);
        nanmanCardEntity.setPosition(user.getX(), user.getY() + 1.0, user.getZ());
        nanmanCardEntity.setVelocity(0, 0.05, 0);
        nanmanCardEntity.setCardId(this.getCardId());
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                ModSoundEvents.NANMAN, SoundCategory.PLAYERS,
                1.0f, 1.0f);
        world.spawnEntity(nanmanCardEntity);
        WuXieStack.addWuXieStack(this.getCardId(), 0);
        WuXieItem.taskQueue.offer(TacticCardEntity.scheduleTacticCardEffect(nanmanCardEntity));
        if (WuXieItem.taskQueue.size() == 1) {
            WuXieItem.processNextTask();
        }
        CardGameManager.discard(stack.copy());
        stack.decrement(1);
        return super.use(world, user, hand);
    }
}
