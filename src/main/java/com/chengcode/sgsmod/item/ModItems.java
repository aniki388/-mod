package com.chengcode.sgsmod.item;

import com.chengcode.sgsmod.Sgsmod;
import com.chengcode.sgsmod.card.*;
import com.chengcode.sgsmod.manager.HandManagementScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ModItems {
    // 在类最上方定义花色常量数组（方便循环）
    private static final int SPADE = Card.SPADE;
    private static final int HEART = Card.HEART;
    private static final int CLUB = Card.CLUB;
    private static final int DIAMOND = Card.DIAMOND;

    // 保存所有注册的卡牌（方便以后管理）
    public static final List<Item> ALL_CARDS = new ArrayList<>();


    // 在静态代码块批量注册
    static {
        Object[][] cardData = {
                {"sha", SPADE, 7}, {"sha", SPADE, 8}, {"sha", SPADE, 9}, {"sha", SPADE, 10},
                {"wuxie", SPADE, 10}, {"nanman", SPADE, 7}, {"chaiqiao", SPADE, 3},
                {"wuzhong", SPADE, 6}, {"juedou", SPADE, 0},
                {"sha", HEART, 10}, {"shan", HEART, 2}, {"shan", HEART, 4}, {"tao", HEART, 3},
                {"taoyuan", HEART, 0}, {"wanjian", HEART, 0}, {"wuzhong", HEART, 7},
                {"wuzhong", HEART, 8}, {"wuzhong", HEART, 9},
                {"sha", CLUB, 2}, {"sha", CLUB, 3}, {"sha", CLUB, 4}, {"sha", CLUB, 5}, {"sha", CLUB, 6},
                {"sha", CLUB, 7}, {"sha", CLUB, 8}, {"sha", CLUB, 9}, {"sha", CLUB, 10},
                {"chaiqiao", CLUB, 3}, {"shunshou", CLUB, 4}, {"wuxie", CLUB, 11}, {"jiedao", CLUB, 12},
                {"sha", DIAMOND, 6}, {"sha", DIAMOND, 7}, {"sha", DIAMOND, 8}, {"sha", DIAMOND, 9}, {"sha", DIAMOND, 10},
                {"shan", DIAMOND, 2}, {"shan", DIAMOND, 4}, {"shunshou", DIAMOND, 3}, {"shunshou", DIAMOND, 4},
                {"tao", DIAMOND, 11}, {"jiedao", DIAMOND, 12}, {"juedou", DIAMOND, 0}
        };

        for (int i = 0; i < cardData.length; i++) {
            Object[] data = cardData[i];
            String baseId = (String) data[0];
            int suit = (int) data[1];
            int number = (int) data[2];

            // 每张牌生成唯一注册 ID，但贴图共用 baseId
            String registryId = "card/" + baseId + "_" + i; // 避免重复注册
            Item cardItem = switch (baseId) {
                case "sha" -> new ShaCardItem(new Item.Settings(), suit, number, baseId);
                case "shan" -> new Card(new Item.Settings(), suit, number,baseId);
                case "jiu" -> new JiuItem(new Item.Settings(), suit, number,baseId);
                case "wuxie" -> new WuXieItem(new Item.Settings(), suit, number,baseId);
                case "nanman" -> new NanmanCardItem(new Item.Settings(), suit, number,baseId);
                case "wuzhong" -> new WuZhongItem(new Item.Settings(), suit, number,baseId);
                case "juedou" -> new Card(new Item.Settings(), suit, number,baseId);
                case "chaiqiao" -> new ChaiqiaoItem(new Item.Settings(), suit, number,baseId);
//                case "jiedao" -> new JiedaoItem(new Item.Settings(), suit, number,baseId);
                case "wanjian" -> new WanJianItem(new Item.Settings(), suit, number,baseId);
                case "taoyuan" -> new TaoYuanItem(new Item.Settings(), suit, number,baseId);
                case "shunshou" -> new ShunshouItem(new Item.Settings(), suit, number,baseId);
                case "tao" -> new TaoItem(new Item.Settings(), suit, number,baseId);
                default -> new Card(new Item.Settings(), suit, number,baseId);
            };

            ALL_CARDS.add(registerItems(registryId, cardItem));
        }
    }





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
    public static final Item LVBUHAT = registerItems("lvbu_hat", new LvbuHatItem(new Item.Settings()));
    public static final Item CHAIQIAO = registerItems("card/chaiqiao", new ChaiqiaoItem(new Item.Settings()));
    public static final Item NANMAN = registerItems("card/nanman", new NanmanCardItem(new Item.Settings()));
    public static final Item WANJIAN = registerItems("card/wanjian", new WanJianItem(new Item.Settings()));
    public static final Item CAMPAIGN_ITEM = registerItems("campaign_item", new CampaignItem(new Item.Settings()));
    public static final Item ZHUGELIANNU= registerItems("equipment/zhugeliannu", new ZhugeCrossbowItem(new Item.Settings()));
    public static final Item CROSSBOWARM = registerItems("material/crossbowarm", new Item(new Item.Settings()));
    public static final Item CROSSBOWMAGAZINE = registerItems("material/crossbowmagazine", new Item(new Item.Settings()));
    public static final Item TAOYUAN = registerItems("card/taoyuan", new TaoYuanItem(new Item.Settings()));
    public static final Item JIEDAO = registerItems("card/jiedao", new JiedaoItem(new Item.Settings()));
    public static final Item HANDMANAGER = registerItems("handmanager", new HandManagerItem(new Item.Settings()));
    public static final ScreenHandlerType<HandManagementScreenHandler> HAND_MANAGEMENT_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(Sgsmod.MOD_ID, "hand_management"), HandManagementScreenHandler::new);
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
