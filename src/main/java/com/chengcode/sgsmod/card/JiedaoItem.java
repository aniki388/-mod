package com.chengcode.sgsmod.card;

import com.chengcode.sgsmod.entity.JiedaoEntity;
import com.chengcode.sgsmod.entity.ModEntities;
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

public class JiedaoItem extends TacticCard{

    public JiedaoItem(Settings settings) {
        super(settings);
    }

    public JiedaoItem(Settings settings, int suit, int number, String baseId) {
        super(settings, suit, number,baseId);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        JiedaoEntity jiedaoEntity = new JiedaoEntity(ModEntities.JIEDAO_CARD_ENTITY, world);
        jiedaoEntity.setOwner( user);
        jiedaoEntity.setResponse( true);
        jiedaoEntity.setPosition(user.getX(), user.getY() + 1.0, user.getZ());
        jiedaoEntity.setVelocity(0, 0.05, 0);
        jiedaoEntity.setCardId(this.getCardId());
        world.playSound(null ,user.getX(), user.getY(), user.getZ(), ModSoundEvents.JIEDAO, SoundCategory.PLAYERS, 1.0f, 1.0f);
        world.spawnEntity(jiedaoEntity);
        WuXieStack.addWuXieStack(this.getCardId(), 0);
        // 将任务加入队列并启动定时检查
        WuXieItem.taskQueue.offer(TacticCardEntity.scheduleTacticCardEffect(jiedaoEntity));
        if (WuXieItem.taskQueue.size() == 1) {
            WuXieItem.processNextTask(); // 如果没有其他任务，开始处理队列中的第一个任务
        }
        CardGameManager.discard(stack.copy());
        stack.decrement(1);
        return super.use(world, user, hand);
    }
}
