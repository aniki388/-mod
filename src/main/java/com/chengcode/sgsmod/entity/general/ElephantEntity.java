package com.chengcode.sgsmod.entity.general;

import com.chengcode.sgsmod.entity.GeneralEntity;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class ElephantEntity extends AnimalEntity {
    private final Random RANDOM = new Random();
    private int attackAnimationTick = 0;
    private static final int ATTACK_ANIMATION_DURATION = 20;
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public ElephantEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
        this.setCustomName(Text.translatable("entity.sgsmod.elephent"));
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    public static DefaultAttributeContainer.Builder createElephantAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.15D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0D)
                .add(EntityAttributes.GENERIC_ARMOR, 4.0D);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0D, false) {
            private static final double MAX_ATTACK_DIST = 2; // 攻击距离，单位方块
            @Override
            public boolean canStart() {
                LivingEntity target = ElephantEntity.this.getTarget();
                return target != null && ElephantEntity.this.squaredDistanceTo(target) <= MAX_ATTACK_DIST * MAX_ATTACK_DIST;
            }

            @Override
            public boolean shouldContinue() {
                LivingEntity target = ElephantEntity.this.getTarget();
                return target != null && ElephantEntity.this.squaredDistanceTo(target) <= MAX_ATTACK_DIST * MAX_ATTACK_DIST;
            }
        });

        this.goalSelector.add(2, new WanderAroundFarGoal(this, 0.7D));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(4, new LookAroundGoal(this));

        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, HostileEntity.class, true));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, GeneralEntity.class, true));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        // 行走动画（客户端/服务端都可以）
        if (this.isOnGround() && this.getVelocity().horizontalLength() > 0.01D) {
            if (!walkAnimationState.isRunning()) {
                walkAnimationState.start(this.age);
            }
        } else {
            walkAnimationState.stop();
        }

        // 只在服务端执行攻击
        if (!getWorld().isClient) {
            LivingEntity target = this.getTarget();
            if (target != null && squaredDistanceTo(target) <= 8 && this.age % 100 == 0) {
                if (!attackAnimationState.isRunning()) {
                    attackAnimationState.start(this.age);
                    attackAnimationTick = 0;
                    this.tryAttack(target); // 只在服务端执行
                }

                attackAnimationState.run(state -> {
                    attackAnimationTick++;
                    if (attackAnimationTick >= ATTACK_ANIMATION_DURATION) {
                        state.stop();
                    }
                });
            } else {
                attackAnimationState.stop();
            }
        }
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        super.updatePassengerPosition(passenger, positionUpdater);
        if (passenger != null) {
            double yOffset = this.getHeight() * 0.75D;
            double xOffset = 0.0D;
            double zOffset = 0.0D;

            // 直接更新乘客位置
            passenger.setPos(this.getX() + xOffset, this.getY() + yOffset, this.getZ() + zOffset);

            // 可选：让骑乘者跟随大象旋转
            passenger.setYaw(this.getYaw());
            passenger.setPitch(this.getPitch());
        }
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return RANDOM.nextBoolean() ? ModSoundEvents.ELEPHENT2 : ModSoundEvents.ELEPHENT1 ;
    }

}
