package com.chengcode.sgsmod.entity;

import com.chengcode.sgsmod.gui.SelectTargetPlayerScreen;
import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.item.ModItems;
import com.chengcode.sgsmod.network.NetWorking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class ChaiqiaoEntity extends TacticCardEntity{
    public ChaiqiaoEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.CHAIQIAO;
    }

    @Override
    public void executeTacticEffect(PlayerEntity player) {
        if (player.getWorld().isClient) {
            // 在客户端调用 GUI
            MinecraftClient.getInstance().setScreen(new SelectTargetPlayerScreen(player));
        } else {
            // 在服务器端发送包通知客户端打开GUI
            if (player instanceof ServerPlayerEntity serverPlayer) {
                ServerPlayNetworking.send(serverPlayer, NetWorking.OPEN_SELECT_TARGET_PACKET_ID, PacketByteBufs.empty());
            }
        }
    }

    @Override
    public void executeTacticEffect(GeneralEntity general) {
        LivingEntity target = general.getTarget();
        if (target instanceof ServerPlayerEntity) {
            // 优先处理装备（如果有的话）
            if(!CardGameManager.removeRandomCard(target, "ZB"))
            {
                CardGameManager.removeRandomCard(target, "SP");
            }
        }
        this.kill();
    }

    @Override
    public void checkForWuXie() {
        // 针对无中生有的特殊逻辑，调用父类的反制逻辑
        super.checkForWuXie();
    }
}
