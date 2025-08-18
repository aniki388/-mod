package com.chengcode.sgsmod.entity.general;

import com.chengcode.sgsmod.entity.GeneralEntity;
import com.chengcode.sgsmod.entity.ModEntities;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class NanmanEntity extends PassiveEntity {
    public NanmanEntity(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
        this.setCustomName(Text.translatable("entity.sgsmod.nanman"));
        this.setCustomNameVisible( true);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0D, false)
                {
                    private static final double MAX_ATTACK_DIST = 2;
                    @Override
                    protected double getSquaredMaxAttackDistance(LivingEntity entity) {
                        return MAX_ATTACK_DIST * MAX_ATTACK_DIST;
                    }
                }
        );
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 0.8D));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, HostileEntity.class, true));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, GeneralEntity.class, true));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world,
                                 LocalDifficulty difficulty,
                                 SpawnReason spawnReason,
                                 @Nullable EntityData entityData,
                                 @Nullable NbtCompound entityNbt) {
        // 先调用父类的初始化逻辑
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);

        // 创建并生成大象
        ElephantEntity elephant = ModEntities.ELEPHANT.create(world.toServerWorld());
        if (elephant != null) {
            elephant.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
            world.spawnEntity(elephant);
            this.startRiding(elephant, true);
        }

        // 返回父类的 EntityData
        return data;
    }

}
