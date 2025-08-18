package com.chengcode.sgsmod.card;

import com.chengcode.sgsmod.entity.ModEntities;
import com.chengcode.sgsmod.entity.TacticCardEntity;
import com.chengcode.sgsmod.entity.TaoYuanEntity;
import com.chengcode.sgsmod.item.ModItems;
import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.manager.WuXieStack;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class TaoYuanItem extends TacticCard{
    public TaoYuanItem(Settings settings) {
        super(settings);
    }



    public TaoYuanItem(Settings settings, int suit, int number, String baseId) {
        super(settings, suit, number,baseId);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        TaoYuanEntity taoYuanEntity = new TaoYuanEntity(ModEntities.TAOYUAN_CARD_ENTITY, world);
        taoYuanEntity.setOwner( user);
        boolean canResponse = true;
        taoYuanEntity.setResponse(canResponse);
        taoYuanEntity.setPosition(user.getX(), user.getY() + 1.0, user.getZ());
        taoYuanEntity.setVelocity(0.0F, 0.05, 0);
        taoYuanEntity.setCardId(this.getCardId());
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                ModSoundEvents.TAOYUAN, SoundCategory.PLAYERS,
                1.0f, 1.0f);
        world.spawnEntity(taoYuanEntity);
        WuXieStack.addWuXieStack(this.getCardId(), 0);
        WuXieItem.taskQueue.offer(TacticCardEntity.scheduleTacticCardEffect(taoYuanEntity));
        if (WuXieItem.taskQueue.size() == 1) {
            WuXieItem.processNextTask();
        }
        CardGameManager.discard(itemStack.copy());
        itemStack.decrement(1);
        return super.use(world, user, hand);
    }
}
