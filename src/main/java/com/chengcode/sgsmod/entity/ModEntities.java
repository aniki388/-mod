package com.chengcode.sgsmod.entity;

import com.chengcode.sgsmod.Sgsmod;
import com.chengcode.sgsmod.entity.general.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<ShaEntity> SHA_ENTITY = register(
            "sha_entity",
            FabricEntityTypeBuilder.<ShaEntity>create(SpawnGroup.MISC, ShaEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(16).trackedUpdateRate(10)
                    .build()
    );

    public static final EntityType<WuZhongEntity> WUZHONG_ENTITY = register(
            "wuzhong_entity",
            FabricEntityTypeBuilder.<WuZhongEntity>create(SpawnGroup.MISC, WuZhongEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(16).trackedUpdateRate(10)
                    .build()
    );

    public static final EntityType<ShunshouEntity> SHUNSHOU_ENTITY = register(
            "shunshou_entity",
            FabricEntityTypeBuilder.<ShunshouEntity>create(SpawnGroup.MISC, ShunshouEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(16).trackedUpdateRate(10)
                    .build()
    );

    public static final EntityType<ChaiqiaoEntity> CHAIQIAO_ENTITY = register(
            "chaiqiao_entity",
            FabricEntityTypeBuilder.<ChaiqiaoEntity>create(SpawnGroup.MISC, ChaiqiaoEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(16).trackedUpdateRate(10)
                    .build()
    );

    public static final EntityType<NanmanCardEntity> NANMAN_CARD_ENTITY = register(
            "nanman_card_entity",
            FabricEntityTypeBuilder.<NanmanCardEntity>create(SpawnGroup.MISC, NanmanCardEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(16).trackedUpdateRate(10)
                    .build()
    );
    public static final EntityType<TaoYuanEntity> TAOYUAN_CARD_ENTITY = register(
            "taoyuan_card_entity",
            FabricEntityTypeBuilder.<TaoYuanEntity>create(SpawnGroup.MISC, TaoYuanEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(16).trackedUpdateRate(10)
                    .build()
    );

    public static final EntityType<WanJianEntity> WANJIAN = register(
            "wanjian",
            FabricEntityTypeBuilder.<WanJianEntity>create(SpawnGroup.MISC, WanJianEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(16).trackedUpdateRate(10)
                    .build()
    );

    public static final EntityType<JiedaoEntity> JIEDAO_CARD_ENTITY = register(
            "jiedao_card_entity",
            FabricEntityTypeBuilder.<JiedaoEntity>create(SpawnGroup.MISC, JiedaoEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(16).trackedUpdateRate(10)
                    .build()
    );

    public static final EntityType<LvBuEntity> LUBU = register(
            "lubu",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, LvBuEntity::new)
                    .dimensions(EntityDimensions.fixed(0.75F, 1.95F))
                    .trackRangeBlocks(32)
                    .build()
    );

    public static final EntityType<WeiYanEntity> WEIYAN = register(
            "weiyan",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, WeiYanEntity::new)
                    .dimensions(EntityDimensions.fixed(0.75F, 1.95F))
                    .trackRangeBlocks(32)
                    .build()
    );

    public static final EntityType<SunCeEntity> SUNCE = register(
            "sunce",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, SunCeEntity::new)
                    .dimensions(EntityDimensions.fixed(0.75F, 1.95F))
                    .trackRangeBlocks(32)
                    .build()
    );

    public static final EntityType<JieXushengEntity> JIE_XUSHENG = register(
            "jiexusheng",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, JieXushengEntity::new)
                    .dimensions(EntityDimensions.fixed(0.75F, 1.95F))
                    .trackRangeBlocks(32)
                    .build()
    );

    public static final EntityType<CaochongEntity> CAOCHONG = register(
            "caochong",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, CaochongEntity::new)
                    .dimensions(EntityDimensions.fixed(0.75F, 1.95F))
                    .trackRangeBlocks(32)
                    .build()
    );

    public static final EntityType<ElephantEntity> ELEPHANT = register(
            "elephant",
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, ElephantEntity::new)
                    .dimensions(EntityDimensions.fixed(1.4F, 1.4F))
                    .trackRangeBlocks(32)
                    .build()
    );

    public static final EntityType<NanmanEntity> NANMAN = register(
            "nanman",
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, NanmanEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.95F))
                    .trackRangeBlocks(32)
                    .build()
    );


    public static void registerEntities() {
        FabricDefaultAttributeRegistry.register(LUBU, GeneralEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(WEIYAN, WeiYanEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(JIE_XUSHENG, JieXushengEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(ELEPHANT, ElephantEntity.createElephantAttributes());
        FabricDefaultAttributeRegistry.register(SUNCE, SunCeEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(CAOCHONG, CaochongEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(NANMAN, NanmanEntity.createAttributes());

    }

    // 通用注册方法
    public static <T extends Entity> EntityType<T> register(String name, EntityType<T> entityType) {
        return Registry.register(Registries.ENTITY_TYPE, new Identifier(Sgsmod.MOD_ID, name), entityType);
    }
}
