package com.chengcode.sgsmod.card;

import com.chengcode.sgsmod.card.Card;
import com.chengcode.sgsmod.entity.TacticCardEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class TacticCard extends Card {
    private boolean canResponse = true;
    public static boolean USED = false;
    public TacticCard(Settings settings) {
        super(settings);
    }

    // 可以在这个方法中加入锦囊牌的特殊行为，比如能否被无懈可击反制
    // 设置无懈可击可响应的状态
    public void setResponse(boolean canResponse) {
        this.canResponse = canResponse;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        USED = true;
        return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
    }


    // 锦囊牌被反制后的动画效果
    private void triggerBrokenAnimation(PlayerEntity player) {
        // 执行反制时的动画，例如销毁物品、播放特效等
        player.getWorld().addParticle(ParticleTypes.EXPLOSION, player.getX(), player.getY(), player.getZ(), 0, 0, 0);
    }

}
