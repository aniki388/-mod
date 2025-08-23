package com.chengcode.sgsmod.network;

import com.chengcode.sgsmod.Sgsmod;
import net.minecraft.util.Identifier;

public class NetWorking {
    public static final Identifier SHAN_PROMPT_PACKET = new Identifier(Sgsmod.MOD_ID, "shan_prompt");       // 服务端通知客户端可以出闪
    public static final Identifier SHAN_RESPONSE_PACKET = new Identifier(Sgsmod.MOD_ID, "shan_response");   // 客户端回应是否出闪
    public static final Identifier SHA_RESPONSE_PACKET = new Identifier(Sgsmod.MOD_ID, "sha_response");   // 客户端回应是否出闪
    public static final Identifier OPEN_MODE_MENU_PACKET_ID = new Identifier(Sgsmod.MOD_ID, "open_mode_menu");
    public static final Identifier MODE_SELECT_PACKET_ID = new Identifier(Sgsmod.MOD_ID, "mode_select");
    public static final Identifier OPEN_SELECT_TARGET_PACKET_ID = new Identifier(Sgsmod.MOD_ID, "open_select_target");
    public static final Identifier OPEN_CAMPAIGNS_PACKET_ID = new Identifier(Sgsmod.MOD_ID, "open_campaigns");
    public static final Identifier SPAWN_PACKET_ID = new Identifier(Sgsmod.MOD_ID, "spawn");
    public static final Identifier JIEDAO_ATTACK_PACKET = new Identifier(Sgsmod.MOD_ID, "jiedao_attack");
    public static final Identifier REQUEST_ID = new Identifier(Sgsmod.MOD_ID, "request_hand");
    public static final Identifier SYNC_ID    = new Identifier(Sgsmod.MOD_ID, "sync_hand");
    public static final Identifier UPDATE_ID  = new Identifier(Sgsmod.MOD_ID, "update_hand");
    public static final Identifier TURN_ORDER_SELECT_PACKET_ID = new Identifier(Sgsmod.MOD_ID, "turn_order_select");  // 客户端 → 服务端，确认选择的顺序
    public static final Identifier TURN_ORDER_SYNC_PACKET_ID   = new Identifier(Sgsmod.MOD_ID, "turn_order_sync");    // 服务端 → 客户端，同步轮次顺序
    public static final Identifier OPEN_TURN_ORDER_PACKET_ID = new Identifier(Sgsmod.MOD_ID, "open_turn_order");
    public static final Identifier PK_OPEN_PINPOINT = new Identifier("sgsmod","open_pinpoint"); // server->client: 打开 GUI
    public static final Identifier PK_CLIENT_CONFIRM = new Identifier("sgsmod","client_confirm"); // client->server: 点击确认
    public static final Identifier PK_SERVER_REVEAL = new Identifier("sgsmod","server_reveal"); // server->client: 揭示两张卡和胜负
    public static final Identifier PK_CANCEL = new Identifier("sgsmod","pinpoint_cancel"); // 取消/超时
}
