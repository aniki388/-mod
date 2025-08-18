package com.chengcode.sgsmod.entity;

import com.chengcode.sgsmod.item.ModItems;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class WanJianEntity extends TacticCardEntity{
    public WanJianEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.WANJIAN;
    }

    @Override
    public void executeTacticEffect(PlayerEntity player) {
        super.executeTacticEffect(player);
        World world = player.getWorld();

        // 确保只在服务端执行
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.getPlayers().forEach(p -> p.sendMessage(Text.of("§c§l万箭齐发！"), true));
            ArrayList<LivingEntity> targets = TacticCardEntity.getTargets(player);
            player.playSound(SoundEvents.EVENT_RAID_HORN.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
            serverWorld.getServer().execute(() -> {
                spawnArrowRain(serverWorld, player, targets);
            });
        }
    }

    @Override
    public void executeTacticEffect(GeneralEntity general) {
        super.executeTacticEffect(general);
        World world = general.getWorld();

        // 确保只在服务端执行
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.getPlayers().forEach(p -> p.sendMessage(Text.of("§c§l万箭齐发！"), true));
            ArrayList<LivingEntity> targets = TacticCardEntity.getTargets(general);
            general.playSound(SoundEvents.EVENT_RAID_HORN.value(), 1.0f, 1.0f);
            serverWorld.getServer().execute(() -> {
                spawnArrowRain(serverWorld, general, targets);
            });
        }
    }
    private static void spawnArrowRain(ServerWorld world, LivingEntity source, ArrayList<LivingEntity> targets) {
        for (LivingEntity target : targets) {
            Vec3d pos = target.getPos();
            target.playSound(ModSoundEvents.WANJIAN, 1.0f, 1.0f);
            double startY = pos.y + 8;

            for (int i = 0; i < 6; i++) {
                ArrowEntity arrow = new ArrowEntity(world, pos.x + (world.random.nextDouble() - 0.5) * 1.5, startY, pos.z + (world.random.nextDouble() - 0.5) * 1.5);
                arrow.setVelocity(0, -1.5, 0); // 下坠
                arrow.setDamage(1.0); // 伤害值
                arrow.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
                arrow.setOwner(source); // 伤害来源
                world.spawnEntity(arrow);

                // 箭粒子
                world.spawnParticles(ParticleTypes.CRIT, arrow.getX(), arrow.getY(), arrow.getZ(), 3, 0.05, 0.05, 0.05, 0.01);
                world.spawnParticles(ParticleTypes.ENCHANTED_HIT, arrow.getX(), arrow.getY(), arrow.getZ(), 2, 0.05, 0.05, 0.05, 0.01);

                // 箭飞声
                world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 0.7f, 1.2f);
            }

            // 命中前破空声（延迟一点时间）
            world.getServer().execute(() ->
                    world.playSound(null, target.getBlockPos(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.5f)
            );
        }
    }
}
