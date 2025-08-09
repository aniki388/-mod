package com.chengcode.sgsmod.card;

import com.chengcode.sgsmod.entity.ChaiqiaoEntity;
import com.chengcode.sgsmod.entity.ModEntities;
import com.chengcode.sgsmod.entity.TacticCardEntity;
import com.chengcode.sgsmod.manager.WuXieStack;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ChaiqiaoItem extends TacticCard{
    public ChaiqiaoItem(Settings settings) {
        super(settings);
    }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        ChaiqiaoEntity chaiqiaoEntity = new ChaiqiaoEntity(ModEntities.CHAIQIAO_ENTITY, world);
        chaiqiaoEntity.setOwner(user);
        boolean canResponse = true;
        chaiqiaoEntity.setResponse(canResponse);
        chaiqiaoEntity.setPosition(user.getX(), user.getY() + 1.0, user.getZ());
        chaiqiaoEntity.setVelocity(0, 0.05, 0);
        chaiqiaoEntity.setCardId(this.getCardId());
        user.playSound(ModSoundEvents.CHAIQIAO, SoundCategory.PLAYERS, 1.0f, 1.0f);
        world.spawnEntity(chaiqiaoEntity);
        WuXieStack.addWuXieStack(this.getCardId(), 0);
        // 将任务加入队列并启动定时检查
        WuXieItem.taskQueue.offer(TacticCardEntity.scheduleTacticCardEffect(chaiqiaoEntity));
        if (WuXieItem.taskQueue.size() == 1) {
            WuXieItem.processNextTask(); // 如果没有其他任务，开始处理队列中的第一个任务
        }
        // 消耗一张牌
        stack.decrement(1);
        return TypedActionResult.success(stack, world.isClient());
    }
}
