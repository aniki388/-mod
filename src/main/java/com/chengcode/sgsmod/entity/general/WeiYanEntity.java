package com.chengcode.sgsmod.entity.general;

import com.chengcode.sgsmod.entity.GeneralEntity;
import com.chengcode.sgsmod.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class WeiYanEntity extends GeneralEntity {
    public WeiYanEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.setCustomName(Text.translatable("entity.sgsmod.weiyan"));
        this.setCustomNameVisible(true);
        setKuangguEnabled( true);
        setZhuangshiEnabled( true);
    }


    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void drop(DamageSource source) {
        super.drop(source);
        this.dropItem(ModItems.LIEGONG_ITEM);
    }
}
