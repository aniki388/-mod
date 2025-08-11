package com.chengcode.sgsmod.entity;

import com.chengcode.sgsmod.Sgsmod;
import com.chengcode.sgsmod.entity.LvBuEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<ShaEntity> SHA_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(Sgsmod.MOD_ID, "sha_entity"),
            FabricEntityTypeBuilder.<ShaEntity>create(SpawnGroup.MISC, ShaEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(32).trackedUpdateRate(10)
                    .build()
    );

    public static final EntityType<WuZhongEntity> WUZHONG_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(Sgsmod.MOD_ID, "wuzhong_entity"),
            FabricEntityTypeBuilder.<WuZhongEntity>create(SpawnGroup.MISC, WuZhongEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(32).trackedUpdateRate(10)
                    .build()
    );

    public static final EntityType<ShunshouEntity> SHUNSHOU_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(Sgsmod.MOD_ID, "shunshou_entity"),
            FabricEntityTypeBuilder.<ShunshouEntity>create(SpawnGroup.MISC, ShunshouEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(32).trackedUpdateRate(10)
                    .build()
    );

    public static final EntityType<ChaiqiaoEntity> CHAIQIAO_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(Sgsmod.MOD_ID, "chaiqiao_entity"),
            FabricEntityTypeBuilder.<ChaiqiaoEntity>create(SpawnGroup.MISC, ChaiqiaoEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(32).trackedUpdateRate(10)
                    .build()
    );

    public static final EntityType<LvBuEntity> LUBU = FabricEntityTypeBuilder.create(
                    SpawnGroup.MONSTER,
                    LvBuEntity::new
            ).dimensions(EntityDimensions.fixed(0.75F, 1.95F))
            .trackRangeBlocks(32)
            .build();

    public static final EntityType<WeiYanEntity> WEIYAN = FabricEntityTypeBuilder.create(
                    SpawnGroup.MONSTER,
                    WeiYanEntity::new
            ).dimensions(EntityDimensions.fixed(0.75F, 1.95F))
            .trackRangeBlocks(32)
            .build();

    public static void register() {
        Registry.register(Registries.ENTITY_TYPE, new Identifier(Sgsmod.MOD_ID, "lubu"), LUBU);
        Registry.register(Registries.ENTITY_TYPE, new Identifier(Sgsmod.MOD_ID, "weiyan"), WEIYAN);
        FabricDefaultAttributeRegistry.register(LUBU, GeneralEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(WEIYAN, WeiYanEntity.createMobAttributes());
        // Call this from main class on initialization
    }
}
