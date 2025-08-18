package com.chengcode.sgsmod.item;

import com.chengcode.sgsmod.Sgsmod;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final RegistryKey<ItemGroup> SGSMOD_GROUP1 = register("sgsmod_group1");
    public static final RegistryKey<ItemGroup> SGSMOD_GROUP2 = register("sgsmod_group2");

    private static RegistryKey<ItemGroup> register(String id) {
        return RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(Sgsmod.MOD_ID,id));
    }

//    public static final ItemGroup SGSMOD_GROUP2 = Registry.register(
//            Registries.ITEM_GROUP,
//            new Identifier(Sgsmod.MOD_ID, "sgsmod_group2"),
//            ItemGroup.create(null, -1)
//                    .displayName(Text.translatable("itemGroup.sgsmod_group2"))
//                    .icon(() -> new ItemStack(ModItems.ICE_ETHER))
//                    .entries((displaycontext, entries) -> {
//                        entries.add(ModItems.CARDBOARD);
//                        entries.add(Items.DIAMOND);
//                    })
//                    .build()
//    );

    public static void registerItemGroups() {
        Registry.register(
                Registries.ITEM_GROUP,
                SGSMOD_GROUP1,
                ItemGroup.create(ItemGroup.Row.TOP, 7)
                        .displayName(Text.translatable("itemGroup.sgsmod_group1"))
                        .icon(() -> new ItemStack(ModItems.SHA))
                        .entries((displaycontext, entries) -> {
                            entries.add(ModItems.SHA);
                            entries.add(ModItems.SHAN);
                            entries.add(ModItems.LEISHA);
                            entries.add(ModItems.HUOSHA);
                            entries.add(ModItems.JIU);
                            entries.add(ModItems.TAO);
                            entries.add(ModItems.WUXIE);
                            entries.add(ModItems.WUZHONG);
                            entries.add(ModItems.SHUNSHOU);
                            entries.add(ModItems.CHAIQIAO);
                            entries.add(ModItems.CARD_STACK);
                            entries.add(ModItems.WUSHUANG_ITEM);
                            entries.add(ModItems.LIEGONG_ITEM);
                            entries.add(ModItems.KUROU_ITEM);
                            entries.add(ModItems.NANMAN);
                            entries.add(ModItems.WANJIAN);
                            entries.add(ModItems.TAOYUAN);
                            entries.add(ModItems.CAMPAIGN_ITEM);
                            entries.add(ModItems.ZHUGELIANNU);
                            entries.add(ModItems.CROSSBOWARM);
                            entries.add(ModItems.CROSSBOWMAGAZINE);
                            entries.add(ModItems.LVBUHAT);
//                            entries.add(ModItems.ICE_ETHER);
//                            entries.add(ModItems.RAW_ICE_ETHER);
                        })
                        .build()
        );
        Registry.register(
                Registries.ITEM_GROUP,
                SGSMOD_GROUP2,
                ItemGroup.create(ItemGroup.Row.TOP, 7)
                        .displayName(Text.translatable("itemGroup.sgsmod_group2"))
                        .icon(() -> new ItemStack(ModItems.SHA))
                        .entries((displaycontext, entries) -> {
                            ModItems.ALL_CARDS.forEach(entries::add);
                        })
                        .build()
        );
    }
}
