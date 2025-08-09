package com.chengcode.sgsmod.entity;

import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class WuZhongEntity extends TacticCardEntity {

    public WuZhongEntity(EntityType<? extends TacticCardEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.WUZHONG;
    }

    @Override
    public void executeTacticEffect(PlayerEntity player) {
        // 无中生有锦囊效果
        CardGameManager.giveCard(player);
        CardGameManager.giveCard(player);
        this.kill();
    }

    @Override
    public void executeTacticEffect(GeneralEntity general) {
        // 无中生有锦囊效果
        CardGameManager.giveCard(general);
        CardGameManager.giveCard(general);
        this.kill();
    }

    @Override
    public void checkForWuXie() {
        // 针对无中生有的特殊逻辑，调用父类的反制逻辑
        super.checkForWuXie();
    }
}
