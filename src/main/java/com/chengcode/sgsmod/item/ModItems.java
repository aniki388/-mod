package com.chengcode.sgsmod.item;

import com.chengcode.sgsmod.Sgsmod;
import com.chengcode.sgsmod.card.*;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class ModItems {
//    public static final Item ICE_ETHER = registerItems("ice_ether", new Item(new Item.Settings()));
//    public static final Item RAW_ICE_ETHER = registerItems("raw_ice_ether", new Item(new Item.Settings()));
//    public static final Item CARDBOARD = registerItems("material/cardboard", new Item(new Item.Settings()));
    public static final Item SHA = registerItems("card/sha", new ShaCardItem(new Item.Settings()));
    public static final Item SHAN = registerItems("card/shan", new Card(new Item.Settings()));
    public static final Item LEISHA = registerItems("card/leisha", new Item(new Item.Settings()));
    public static final Item HUOSHA = registerItems("card/huosha", new Item(new Item.Settings()));
    public static final Item JIU = registerItems("card/jiu", new JiuItem(new Item.Settings()));
    public static final Item TAO = registerItems("card/tao", new TaoItem(new Item.Settings()));
    public static final Item SHUNSHOU = registerItems("card/shunshou", new ShunshouItem(new Item.Settings()));
    public static final Item CARD_STACK = registerItems("card/card_stack", new CardStackItem (new Item.Settings()));
    public static final Item WUSHUANG_ITEM = registerItems("skill/wushuang_item", new WushuangItem(new Item.Settings().maxCount(1)));
    public static final Item LIEGONG_ITEM = registerItems("skill/liegong_item", new LiegongItem(new Item.Settings().maxCount(1)));
    public static final Item KUROU_ITEM = registerItems("skill/kurou_item", new KurouItem(new Item.Settings().maxCount(1)));
    public static final Item WUZHONG = registerItems("card/wuzhong", new WuZhongItem(new Item.Settings()));
    public static final Item WUXIE = registerItems("card/wuxie", new WuXieItem(new Item.Settings()));
    public static final Item CHAIQIAO = registerItems("card/chaiqiao", new ChaiqiaoItem(new Item.Settings()));
    public static Item registerItems(String id, Item item) {
        return Registry.register(Registries.ITEM, RegistryKey.of(Registries.ITEM.getKey(), new Identifier(Sgsmod.MOD_ID,id)), item);
    }



   public static void registerItems() {
//        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(ModItems::addItemToItemGroup);
//        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(ModItems::addItemToItemGroup2);
   }

//    private static void addItemToItemGroup(FabricItemGroupEntries entries) {
//        entries.add(ICE_ETHER);
//        entries.add(RAW_ICE_ETHER);
//    }
//    private static void addItemToItemGroup2(FabricItemGroupEntries entries) {
//        entries.add(CARDBOARD);
//    }
}
