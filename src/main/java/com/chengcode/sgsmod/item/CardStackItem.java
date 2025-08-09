package com.chengcode.sgsmod.item;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import com.chengcode.sgsmod.network.NetWorking;

public class CardStackItem extends Item {
    public CardStackItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {
            // 这里发送一个自定义的网络包给客户端，让客户端打开GUI
            ServerPlayNetworking.send(serverPlayer, NetWorking.OPEN_MODE_MENU_PACKET_ID, PacketByteBufs.empty());
        }
        return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
    }
}