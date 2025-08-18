package com.chengcode.sgsmod.entity.general;

import com.chengcode.sgsmod.entity.GeneralEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class JieXushengEntity extends GeneralEntity {
    public JieXushengEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        setPojunEnabled( true);
        setCustomName( Text.translatable("entity.sgsmod.jiexusheng"));
        setCustomNameVisible( true);
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D);
    }
}
