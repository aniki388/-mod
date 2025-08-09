package com.chengcode.sgsmod.entity;

import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class ShunshouEntity extends TacticCardEntity {
    public ShunshouEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SHUNSHOU;
    }

    @Override
    public void executeTacticEffect(PlayerEntity player) {
        // 顺手牵羊锦囊效果
        double radius = 7.0;
        Box searchBox = new Box(
                player.getPos().subtract(radius, radius, radius),
                player.getPos().add(radius, radius, radius)
        );
        World world = player.getWorld();
        List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class, searchBox, player1 -> player1 != player);
        List<GeneralEntity> generals = world.getEntitiesByClass(GeneralEntity.class, searchBox, general -> true);

        if (!players.isEmpty()) {
            for (PlayerEntity targetPlayer : players) {
                CardGameManager.ObtainCard(player, targetPlayer);
            }
        }
        if (!generals.isEmpty()) {
            for (GeneralEntity general : generals) {
                CardGameManager.ObtainCard(player, general);
            }
        }
        this.kill();
    }

    @Override
    public void executeTacticEffect(GeneralEntity general) {
        // 顺手牵羊锦囊效果
        double radius = 5.0;
        Box searchBox = new Box(
                general.getPos().subtract(radius, radius, radius),
                general.getPos().add(radius, radius, radius)
        );
        this.kill();
        World world = general.getWorld();
        List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class, searchBox, player -> true);
        List<GeneralEntity> generals = world.getEntitiesByClass(GeneralEntity.class, searchBox, general1 -> general1 != general);
        if (!players.isEmpty()) {
            for (PlayerEntity player : players) {
                CardGameManager.ObtainCard(general, player);
            }
        }
        if (!generals.isEmpty()) {
            for (GeneralEntity general1 : generals) {
                CardGameManager.ObtainCard(general, general1);
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
