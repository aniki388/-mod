package com.chengcode.sgsmod.item;

import com.chengcode.sgsmod.gui.CampaignScreen;
import com.chengcode.sgsmod.gui.SelectTargetPlayerScreen;
import com.chengcode.sgsmod.network.NetWorking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class CampaignItem extends Item {
    public CampaignItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // 发送一个自定义的网络包给客户端，让客户端打开GUI
        if (user.getWorld().isClient) {
            // 在客户端调用 GUI
            MinecraftClient.getInstance().setScreen(new CampaignScreen());
        } else {
            // 在服务器端发送包通知客户端打开GUI
            if (user instanceof ServerPlayerEntity serverPlayer) {
                ServerPlayNetworking.send(serverPlayer, NetWorking.OPEN_CAMPAIGNS_PACKET_ID, PacketByteBufs.empty());
            }
        }
         return super.use(world, user, hand);
    }
}
