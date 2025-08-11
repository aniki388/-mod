package com.chengcode.sgsmod.accessor;

import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Unique;

public interface PlayerEntityAccessor {
    @Unique
    NbtCompound sgsmod_1_20_1$getPersistentData();
}
