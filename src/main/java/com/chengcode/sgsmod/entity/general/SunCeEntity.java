package com.chengcode.sgsmod.entity.general;

import com.chengcode.sgsmod.entity.GeneralEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SunCeEntity extends GeneralEntity {
    public SunCeEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        setCustomName(Text.translatable("entity.sgsmod.sunce"));
        setJiangEnabled( true);
        setHunZiEnabled( true);
    }
    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 250.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D);
    }

    @Override
    public boolean isAwakend() {
        return !isHunZiEnabled();
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (isAwakend()) {
            // 觉醒时增加移动速度
            setVelocity(getVelocity().multiply(1.1D, 1.0D, 1.1D));
        }
        super.travel(movementInput);
    }

}
