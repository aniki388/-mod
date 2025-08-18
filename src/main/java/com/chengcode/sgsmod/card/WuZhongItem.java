package com.chengcode.sgsmod.card;

import com.chengcode.sgsmod.entity.ModEntities;
import com.chengcode.sgsmod.entity.TacticCardEntity;
import com.chengcode.sgsmod.entity.WuZhongEntity;
import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.manager.WuXieStack;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.concurrent.ScheduledFuture;


public class WuZhongItem extends TacticCard {

    public WuZhongItem(Settings settings) {
        super(settings);
    }


    public WuZhongItem(Settings settings, int suit, int number, String baseId) {
        super(settings, suit, number,baseId);
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        super.use(world, user, hand);

        ItemStack stack = user.getStackInHand(hand);

        WuZhongEntity wuZhongEntity = new WuZhongEntity(ModEntities.WUZHONG_ENTITY, user.getWorld());
        wuZhongEntity.setOwner(user);  // 设置拥有者

        boolean canResponse = true;
        wuZhongEntity.setResponse(canResponse);
        wuZhongEntity.setPosition(user.getX(), user.getY() + 1.0, user.getZ());
        wuZhongEntity.setVelocity(0, 0.05, 0);
        wuZhongEntity.setCardId(this.getCardId());
        world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSoundEvents.WUZHONG, SoundCategory.PLAYERS, 1.0f, 1.0f);
        user.getWorld().spawnEntity(wuZhongEntity);  // 将实体加入世界
        WuXieStack.addWuXieStack(this.getCardId(), 0);
        // 将任务加入队列并启动定时检查
        WuXieItem.taskQueue.offer(TacticCardEntity.scheduleTacticCardEffect(wuZhongEntity));
        if (WuXieItem.taskQueue.size() == 1) {
            WuXieItem.processNextTask(); // 如果没有其他任务，开始处理队列中的第一个任务
        }

        // 消耗一张无中生有牌
        CardGameManager.discard(stack.copy());
        stack.decrement(1);
        return TypedActionResult.success(stack, world.isClient());
    }

}
