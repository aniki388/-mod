package com.chengcode.sgsmod.entity;

import com.chengcode.sgsmod.entity.general.ElephantEntity;
import com.chengcode.sgsmod.entity.general.NanmanEntity;
import com.chengcode.sgsmod.item.ModItems;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;

public class NanmanCardEntity extends TacticCardEntity{
    private final Random RANDOM = new Random();
    public NanmanCardEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.NANMAN;
    }

    @Override
    public void executeTacticEffect(PlayerEntity player) {
        super.executeTacticEffect(player);
        ArrayList<LivingEntity> targets = TacticCardEntity.getTargets(player);
        World world = player.getWorld();
        if (world instanceof ServerWorld serverWorld)
        {
            serverWorld.getPlayers().forEach(p -> {
                p.sendMessage(Text.of("§c§l南蛮入侵！"), true);
                p.playSound(ModSoundEvents.NANMAN, 1.0f, 1.0f);
                p.playSound(RANDOM.nextBoolean() ? ModSoundEvents.ELEPHENT1 : ModSoundEvents.ELEPHENT2, 1.0f, 1.0f);
            });
        }
        for (LivingEntity target : targets) {
            if (!world.isClient) { // 只在服务端生成
                NanmanEntity nanman = ModEntities.NANMAN.create(world);
                ElephantEntity elephant = ModEntities.ELEPHANT.create(world);

                if (nanman != null && elephant != null) {
                    Random random = new Random();

                    // 随机偏移
                    double offsetX = random.nextInt(5) - 5;
                    double offsetZ = random.nextInt(5) - 5;

                    elephant.refreshPositionAndAngles(target.getX() + offsetX, target.getY(), target.getZ() + offsetZ, 0, 0);
                    nanman.refreshPositionAndAngles(target.getX() + offsetX, target.getY(), target.getZ() + offsetZ, 0, 0);

                    // 服务端生成
                    world.spawnEntity(elephant);
                    world.spawnEntity(nanman);

                    // 让 nanman 骑上 elephant
                    nanman.startRiding(elephant, true);

                    // 设置目标
                    nanman.setTarget(target);
                    elephant.setTarget(target);
                }
            }

        }
    }

    @Override
    public void executeTacticEffect(GeneralEntity general) {
        super.executeTacticEffect(general);
        ArrayList<LivingEntity> targets = TacticCardEntity.getTargets(general);
        World world = general.getWorld();
        if (world instanceof ServerWorld serverWorld)
        {
            serverWorld.getPlayers().forEach(p -> {
                p.sendMessage(Text.of("§c§l南蛮入侵！"), true);
                p.playSound(ModSoundEvents.NANMAN, 1.0f, 1.0f);
                p.playSound(RANDOM.nextBoolean() ? ModSoundEvents.ELEPHENT1 : ModSoundEvents.ELEPHENT2, 1.0f, 1.0f);
            });
        }
        for (LivingEntity target : targets) {
            // 在目标附近生成南蛮
            if (!world.isClient) { // 只在服务端生成
                NanmanEntity nanman = ModEntities.NANMAN.create(world);
                ElephantEntity elephant = ModEntities.ELEPHANT.create(world);

                if (nanman != null && elephant != null) {
                    Random random = new Random();

                    // 随机偏移
                    double offsetX = random.nextInt(5) - 5;
                    double offsetZ = random.nextInt(5) - 5;

                    elephant.refreshPositionAndAngles(target.getX() + offsetX, target.getY(), target.getZ() + offsetZ, 0, 0);
                    nanman.refreshPositionAndAngles(target.getX() + offsetX, target.getY(), target.getZ() + offsetZ, 0, 0);

                    // 服务端生成
                    world.spawnEntity(elephant);
                    world.spawnEntity(nanman);

                    // 让 nanman 骑上 elephant
                    nanman.startRiding(elephant, true);

                    // 设置目标
                    nanman.setTarget(target);
                    elephant.setTarget(target);
                }
            }

        }
    }
}
