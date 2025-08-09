package com.chengcode.sgsmod.card;

import com.chengcode.sgsmod.entity.ModEntities;
import com.chengcode.sgsmod.entity.ShunshouEntity;
import com.chengcode.sgsmod.entity.TacticCardEntity;
import com.chengcode.sgsmod.manager.WuXieStack;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ShunshouItem extends TacticCard{
    public ShunshouItem(Settings settings) {
        super(settings);
    }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        ShunshouEntity shunshouEntity = new ShunshouEntity(ModEntities.SHUNSHOU_ENTITY, world);
        shunshouEntity.setOwner( user);

        boolean canResponse = true;
        shunshouEntity.setResponse(canResponse);
        shunshouEntity.setPosition(user.getX(), user.getY() + 1.0, user.getZ());
        shunshouEntity.setVelocity(0, 0.05, 0);
        shunshouEntity.setCardId(this.getCardId());
        user.playSound(ModSoundEvents.SHUNSHOU, SoundCategory.PLAYERS, 1.0f, 1.0f);
        world.spawnEntity(shunshouEntity);
        WuXieStack.addWuXieStack(this.getCardId(), 0);
        // 将任务加入队列并启动定时检查
        WuXieItem.taskQueue.offer(TacticCardEntity.scheduleTacticCardEffect(shunshouEntity));
        if (WuXieItem.taskQueue.size() == 1) {
            WuXieItem.processNextTask(); // 如果没有其他任务，开始处理队列中的第一个任务
        }
        // 消耗一张牌
        stack.decrement(1);
        return TypedActionResult.success(stack, world.isClient());
    }

}
