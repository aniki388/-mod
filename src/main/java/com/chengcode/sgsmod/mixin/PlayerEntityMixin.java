package com.chengcode.sgsmod.mixin;

import com.chengcode.sgsmod.accessor.PlayerEntityAccessor;
import com.chengcode.sgsmod.manager.CardGameManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements PlayerEntityAccessor {
    @Unique
    private NbtCompound persistentData = new NbtCompound();

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.put("sgsmod_persistent", persistentData);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("sgsmod_persistent", NbtElement.COMPOUND_TYPE)) {
            persistentData = nbt.getCompound("sgsmod_persistent");
        }
    }

    @Override
    public NbtCompound sgsmod_1_20_1$getPersistentData() {
        return persistentData;
    }
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

