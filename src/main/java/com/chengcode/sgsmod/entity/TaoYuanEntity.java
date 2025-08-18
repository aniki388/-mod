package com.chengcode.sgsmod.entity;

import com.chengcode.sgsmod.item.ModItems;
import com.chengcode.sgsmod.manager.CardGameManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;

public class TaoYuanEntity extends TacticCardEntity{
    private static final Random rand = new Random();
    public TaoYuanEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.TAOYUAN;
    }

    @Override
    public void executeTacticEffect(PlayerEntity player) {
        super.executeTacticEffect(player);
        World world = this.getWorld();
        ArrayList<LivingEntity> targets = getTargets(player);
        spawnParticlesAroundEntity(player, rand, world);
        for (LivingEntity target : targets){
            spawnParticlesAroundEntity(target, rand, world);
            CardGameManager.recoverHealth(target, 5.0f);
        }
        if(world instanceof ServerWorld serverWorld){
            serverWorld.getPlayers().forEach(p -> p.sendMessage(Text.of("§d桃园结义！"), true));
        }
    }

    @Override
    public void executeTacticEffect(GeneralEntity general) {
        super.executeTacticEffect(general);
        World world = this.getWorld();
        ArrayList<LivingEntity> targets = getTargets(general);
        spawnParticlesAroundEntity(general, rand, world);
        for (LivingEntity target : targets){
            spawnParticlesAroundEntity(target, rand, world);
            CardGameManager.recoverHealth(target, 5.0f);
        }
        if(world instanceof ServerWorld serverWorld){
            serverWorld.getPlayers().forEach(p -> p.sendMessage(Text.of("§d桃园结义！"), true));
        }
    }

    private static void spawnParticlesAroundEntity(LivingEntity entity, Random rand, World world) {
        double centerX = entity.getX();
        double centerY = entity.getY() + 2.0; // 粒子生成在实体头顶2格高
        double centerZ = entity.getZ();

        int particleCount = 50; // 每个实体生成50个粒子

        for (int i = 0; i < particleCount; i++) {
            double offsetX = rand.nextDouble() * 4 - 2; // -2 ~ +2 范围
            double offsetY = rand.nextDouble() * 2;     // 0 ~ 2 高度随机
            double offsetZ = rand.nextDouble() * 4 - 2;

            double velocityX = (rand.nextDouble() - 0.5) * 0.02; // 左右轻微摆动
            double velocityY = -0.02 - rand.nextDouble() * 0.01;  // 向下缓慢
            double velocityZ = (rand.nextDouble() - 0.5) * 0.02;  // 前后轻微摆动

            // 粒子类型可换成自定义花瓣粒子
            world.addParticle(ParticleTypes.FALLING_HONEY,
                    centerX + offsetX,
                    centerY + offsetY,
                    centerZ + offsetZ,
                    velocityX, velocityY, velocityZ);
        }
    }
}
