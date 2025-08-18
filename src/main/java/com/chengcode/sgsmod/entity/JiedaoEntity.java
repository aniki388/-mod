package com.chengcode.sgsmod.entity;

import com.chengcode.sgsmod.card.JiedaoItem;
import com.chengcode.sgsmod.gui.SelectTargetPlayerScreen;
import com.chengcode.sgsmod.gui.ShaPromptScreen;
import com.chengcode.sgsmod.item.ModItems;
import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.network.NetWorking;
import com.chengcode.sgsmod.network.ServerReceiver;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class JiedaoEntity extends TacticCardEntity{
    public JiedaoEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.JIEDAO;
    }

    @Override
    public void executeTacticEffect(PlayerEntity player) {
        super.executeTacticEffect(player);
        // 进入第一次选择界面，选择借刀者
        MinecraftClient.getInstance().setScreen(new SelectTargetPlayerScreen(player, entity -> {
            if (!(entity instanceof PlayerEntity p)) return false;

            // 只有持有武器的玩家才能成为借刀对象
            for (ItemStack itemStack : p.getInventory().main) {
                if (itemStack.getItem() instanceof SwordItem ||
                        itemStack.getItem() instanceof AxeItem ||
                        itemStack.getItem() instanceof TridentItem ||
                        itemStack.getItem() instanceof BowItem ||
                        itemStack.getItem() instanceof CrossbowItem) {
                    return true;  // 是武器持有者，允许选择
                }
            }
            return false;  // 不是武器持有者，不能选择
        }) {
            @Override
            protected void onTargetSelected(LivingEntity target) {
                super.onTargetSelected(target);

                // 进入第二次选择界面，选择被借刀的目标
                MinecraftClient.getInstance().setScreen(new SelectTargetPlayerScreen((PlayerEntity) target) {
                    @Override
                    protected void onTargetSelected(LivingEntity secondTarget) {
                        super.onTargetSelected(secondTarget);

                        // 发送数据到服务器进行处理
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeInt(target.getId());  // 借刀者
                        buf.writeInt(secondTarget.getId());  // 被借刀的目标
                        ServerPlayNetworking.send((ServerPlayerEntity) target, NetWorking.JIEDAO_ATTACK_PACKET, buf);

                        // 关闭当前界面
                        MinecraftClient.getInstance().setScreen(null);
                    }
                });
            }
        });
    }

    @Override
    public void executeTacticEffect(GeneralEntity general) {
        super.executeTacticEffect(general);
        LivingEntity target = general.getTarget();
        if (target instanceof ServerPlayerEntity serverPlayer) {
            boolean hasWeapon = false;
            // 是否有武器
            PlayerInventory  inventory = serverPlayer.getInventory();
            for (ItemStack itemStack : inventory.main) {
                if (itemStack == null || itemStack.isEmpty()) continue;

                if (itemStack.getItem() instanceof SwordItem ||
                        itemStack.getItem() instanceof AxeItem ||
                        itemStack.getItem() instanceof TridentItem ||
                        itemStack.getItem() instanceof BowItem ||
                        itemStack.getItem() instanceof CrossbowItem ||
                        itemStack.getItem() instanceof RangedWeaponItem) { // 自定义远程武器
                    hasWeapon = true;
                    break;
                }
            }

            if (!hasWeapon) {
                serverPlayer.sendMessage(Text.of("你没有武器，无法被借刀。"), false);
                return;
            }
            LivingEntity target2 = CardGameManager.getNearestLivingEntity(serverPlayer, 20);
            if (target2 == null) {
                serverPlayer.sendMessage(Text.of("没有目标，无法被借刀。"), false);
                return;
            }
            if (target.getWorld().isClient) {
                // 在客户端调用 GUI
                MinecraftClient.getInstance().setScreen(new ShaPromptScreen(target2));
            } else {
                // 在服务器端发送包通知客户端打开GUI
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeInt(target2.getId());
                if (target instanceof ServerPlayerEntity servertarget) {
                    ServerPlayNetworking.send(servertarget, NetWorking.SHA_RESPONSE_PACKET, PacketByteBufs.empty());
                }
            }
        }


    }

}
