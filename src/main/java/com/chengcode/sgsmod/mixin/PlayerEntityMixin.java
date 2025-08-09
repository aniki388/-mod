package com.chengcode.sgsmod.mixin;

import com.chengcode.sgsmod.manager.CardGameManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (player.getHealth() - amount <= 0 && !player.getWorld().isClient()) {
            if (!CardGameManager.isInDyingState(player)) {
                CardGameManager.enterDyingState(player);
                cir.setReturnValue(false); // 阻止默认死亡行为
            }
        }
    }
}
