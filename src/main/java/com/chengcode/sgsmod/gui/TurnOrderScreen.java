package com.chengcode.sgsmod.gui;

import com.chengcode.sgsmod.entity.GeneralEntity;
import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.network.NetWorking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TurnOrderScreen extends Screen {
    private final List<PlayerEntity> players;

    // 武将ID与名字列表
    private final List<Integer> generalsIds;
    private final List<String> generalsNames;

    // 顺序池：保存玩家或武将
    // "P:<uuid>" 表示玩家, "G:<id>" 表示武将
    private final List<String> order = new ArrayList<>();

    public TurnOrderScreen(List<PlayerEntity> players, List<GeneralEntity> generals) {
        super(Text.literal("选择回合顺序"));
        this.players = players;
        this.generalsIds = new ArrayList<>();
        this.generalsNames = new ArrayList<>();
        for (GeneralEntity g : generals) {
            if (g == null) continue;
            this.generalsIds.add(g.getId());
            this.generalsNames.add(Objects.requireNonNullElse(g.getCustomName(), Text.literal("未命名")).getString());
        }
    }

    @Override
    protected void init() {
        int y = 40;

        // 玩家按钮
        for (PlayerEntity player : players) {
            UUID uuid = player.getUuid();
            this.addDrawableChild(ButtonWidget.builder(
                            Text.literal("玩家: " + player.getGameProfile().getName()),
                            b -> addToOrder("P:" + uuid.toString()))
                    .dimensions(20, y, 160, 20).build());
            y += 25;
        }

        y += 20; // 空隙

        // 武将按钮
        for (int i = 0; i < generalsIds.size(); i++) {
            int id = generalsIds.get(i);
            String name = generalsNames.get(i);
            this.addDrawableChild(ButtonWidget.builder(
                            Text.literal("武将: " + name),
                            b -> addToOrder("G:" + id))
                    .dimensions(20, y, 160, 20).build());
            y += 25;
        }

        // 确认按钮
        this.addDrawableChild(ButtonWidget.builder(Text.literal("确认顺序"), b -> confirmOrder())
                .dimensions(this.width - 120, this.height - 40, 100, 20).build());

        // 清空按钮
        this.addDrawableChild(ButtonWidget.builder(Text.literal("清空"), b -> order.clear())
                .dimensions(this.width - 120, this.height - 70, 100, 20).build());
    }

    private void addToOrder(String id) {
        if (!order.contains(id)) order.add(id);
    }

    private void confirmOrder() {
        var buf = PacketByteBufs.create();
        buf.writeInt(order.size());
        for (String id : order) buf.writeString(id);
        ClientPlayNetworking.send(NetWorking.TURN_ORDER_SELECT_PACKET_ID, buf);
        this.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int y = 40;
        context.drawText(this.textRenderer, "已选顺序:", this.width - 180, 20, 0xFFFFFF, false);

        for (String id : order) {
            String display = "未知";
            if (id.startsWith("P:")) {
                UUID uuid = UUID.fromString(id.substring(2));
                display = players.stream()
                        .filter(p -> p.getUuid().equals(uuid))
                        .map(p -> "玩家: " + p.getGameProfile().getName())
                        .findFirst().orElse("玩家(未知)");
            } else if (id.startsWith("G:")) {
                int gid = Integer.parseInt(id.substring(2));
                int index = generalsIds.indexOf(gid);
                if (index != -1) display = "武将: " + generalsNames.get(index);
                else display = "武将(未知)";
            }
            context.drawText(this.textRenderer, (order.indexOf(id) + 1) + ". " + display,
                    this.width - 180, y, 0x00FF00, false);
            y += 15;
        }
    }

}
