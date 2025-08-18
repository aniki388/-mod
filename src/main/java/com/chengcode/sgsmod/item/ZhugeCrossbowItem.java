package com.chengcode.sgsmod.item;

import com.chengcode.sgsmod.card.ShaCardItem;
import com.chengcode.sgsmod.entity.ShaEntity;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class ZhugeCrossbowItem extends RangedWeaponItem {
    private static final int RANGE = 15;

    public ZhugeCrossbowItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // 检查是否满足使用条件
        PlayerInventory inventory = user.getInventory();
        ItemStack shaStack = findShaInInventory(inventory);

        if (!shaStack.isEmpty()) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSoundEvents.SHA_ENTITY_THROW, SoundCategory.PLAYERS, 1.0f, 1.0f);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSoundEvents.LIANNU, SoundCategory.PLAYERS, 1.0f, 1.0f);

            if (!world.isClient) {
                ShaEntity shaEntity = new ShaEntity(user, world);
                shaEntity.setItem(shaStack.copy());
                shaEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
                world.spawnEntity(shaEntity);

                // 消耗一张杀牌
                if (!user.getAbilities().creativeMode) shaStack.decrement(1);
            }

            // 触发使用动画
            user.setCurrentHand(hand);
            return TypedActionResult.success(stack, world.isClient());
        } else {
            return TypedActionResult.fail(stack);
        }
    }

    // 辅助方法：在物品栏中查找杀牌
    private ItemStack findShaInInventory(PlayerInventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() instanceof ShaCardItem && stack.getCount() > 0) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Predicate<ItemStack> getProjectiles() {
        return stack -> stack.isOf(ModItems.SHA);
    }

    @Override
    public int getRange() {
        return RANGE;
    }
}
