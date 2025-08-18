package com.chengcode.sgsmod.compaign;

import com.chengcode.sgsmod.network.NetWorking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.List;

public final class DemoData {
    private DemoData() {}

    public static List<CampaignDef> sample() {
        return List.of(
                new CampaignDef(
                        "message",
                        "模式介绍",
                        "国家",
                        "年份",
                        "",
                        List.of("三国杀", "策略", "卡牌"),
                        List.of("历史", "多人对战"),
                        "在战役模式中，你将从一个初出茅庐的武将开始，逐步挑战各路英雄豪杰，使用杀、闪、桃、酒以及锦囊牌谋略取胜，甚至面对强大的 BOSS。支持单人或多人联机，体验沉浸式三国战场！",
                        "sgsmod:icon.png",
                        (player) -> {}
                ),
                new CampaignDef(
                        "hulao-guan",
                        "虎牢关之战",
                        "汉",
                        "初平元年",
                        "困难",
                        List.of("束发紫金冠*1", "技能『无双』*1"),
                        List.of("BOSS", "限时", "经典"),
                        "董卓率大军东进虎牢关，吕布、李儒、樊稠、张济随行，先锋三万驻防关前。\n   十八路诸侯分兵迎战，吕布神勇无比，联军屡败屡战。\n   此时，张飞、关羽、刘备三兄弟挺身而出，轮番围攻吕布...",
                        "sgsmod:textures/gui/battles/hulao.png",
                        (player) -> {
                            sendToClient(player, NetWorking.SPAWN_PACKET_ID, "lvbu");
                        }
                ),
                new CampaignDef(
                        "shenting-hanzhan",
                        "神亭酣战",
                        "吴",                                    // 历史上孙策是东吴
                        "建安二十二年",                             // 地点改为江东
                        "普通",
                        List.of("随机紫装", "武将碎片*20", "虎符*1"),
                        List.of("BOSS", "限时", "经典"),
                        "刘繇自领大军于神亭岭南下营，孙策率兵于岭北。\n    策夜梦光武庙，披挂上马与程普、黄盖、韩当等十三骑登庙祈愿：“若能于江东立业，必重修庙宇，四时祭祀。”\n    岭南，太史慈斗志昂扬，率先锋突出阵营，高呼：“有胆气者，都随我来！”小股将士紧随其后，战马嘶鸣，刀光闪烁。 ",
                        "sgsmod:textures/gui/battles/shenting.png",
                        (player) -> {
                            sendToClient(player, NetWorking.SPAWN_PACKET_ID, "sunce");
                        }
                ),
                new CampaignDef(
                        "ziwu-gu-if",
                        "子午谷奇谋",
                        "蜀",
                        "建兴四年",
                        "极难",
                        List.of("技能界·『烈弓』*1", "武将碎片*30", "虎符*1"),
                        List.of("BOSS", "分支", "策略"),
                        "魏延自请精兵五千，欲直取长安，兵出子午谷。夏侯楙军略怯，若操之得当，城可速取。然险象环生，行军途中可能遭遇伏兵或山谷突袭。突袭敌阵，方能完成奇谋，直取长安。",
                        "sgsmod:textures/gui/battles/ziwugu.png",
                        (player) -> {
                            sendToClient(player, NetWorking.SPAWN_PACKET_ID, "weiyan");
                        }
                ),
                new CampaignDef(
                        "jie-xusheng",
                        "界徐盛讨伐战",
                        "吴",
                        "建安二十四年",
                        "困难",
                        List.of("技能『破军』*1", "限定皮肤*1", "将魂*50"),
                        List.of("BOSS", "速战", "暴击"),
                        "濡须口之战，徐盛随吕蒙袭取南郡。魏军来犯，徐盛亲冒矢石，率部奋勇死战，断敌粮道，破军斩将，威震敌胆。\n   \"这长江天险后，便是江东铁壁！\"",
                        "sgsmod:textures/gui/battles/jiexusheng.png",
                        (player) -> {
                            sendToClient(player, NetWorking.SPAWN_PACKET_ID, "dabao");
                        }
                ),
                new CampaignDef(
                        "cao-chong",
                        "神童救库吏",
                        "魏",
                        "建安十三年",
                        "普通",
                        List.of("『称象』道具*1", "银两*2000", "经验卡*3"),
                        List.of("解谜", "剧情", "智斗"),
                        "曹操马鞍被鼠咬坏，库吏恐遭重罚。曹冲心生一计，先戳破自己衣物，装作忧戚，而后向曹操进言：『鼠咬衣物亦常事，何必苛责？』库吏遂免罪。\n   副本需玩家破解曹冲设计的连环谜题，展现过人智谋方可通关。",
                        "sgsmod:textures/gui/battles/caochong.png",
                        (player) -> {
                            sendToClient(player, NetWorking.SPAWN_PACKET_ID, "caochong");
                        }
                )
//                new CampaignDef(
//                        "changbanpo", "长坂坡突围", "蜀", "建安十三年", "普通",
//                        List.of("金币*800","蓝装","经验"),
//                        List.of("护送","剧情"),
//                        "赵云七进七出，掩护撤离。玩家需在有限时间内保全民众与辎重，途中刷新伏兵与陷阱。",
//                        "yourmod:textures/gui/battles/placeholder.png",
//                        (player) -> {}
//                )
//                new CampaignDef(
//                        "guandu", "官渡争锋", "魏", "建安五年", "地狱",
//                        List.of("红装图纸","专属称号","名将令*3"),
//                        List.of("攻坚","资源拉扯","经典"),
//                        "以少胜多的名战。你需要抢粮、断运、偷营，围绕补给线展开拉扯，最终一举破敌。",
//                        "yourmod:textures/gui/battles/placeholder.png"
//                )
        );
    }
    public static void sendToClient(PlayerEntity player, Identifier PacketID, String name)
    {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(name);
        buf.writeDouble(player.getX());
        buf.writeDouble(player.getY());
        buf.writeDouble(player.getZ());
        ClientPlayNetworking.send(PacketID, buf);
    }
}
