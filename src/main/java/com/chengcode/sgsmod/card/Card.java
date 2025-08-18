package com.chengcode.sgsmod.card;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class Card extends Item {

    private static final Random RANDOM = new Random();
    private int suit;  // 花色
    private int number;  // 点数
    private int color;  // 颜色
    private String cardId;  // 卡牌的唯一标识符
    private String baseId;  // 卡牌类型，例如 "sha", "shan" 等

    // 花色常量
    public static final int SPADE = 0;   // 黑桃
    public static final int HEART = 1;   // 红桃
    public static final int CLUB = 2;    // 草花
    public static final int DIAMOND = 3; // 方片
    public static final int NONE = -1;   // 无色

    // 点数常量
    public static final int ACE = 0;   // A
    public static final int TWO = 1;   // 2
    public static final int THREE = 2; // 3
    public static final int KING = 12; // K

    // 颜色常量
    public static final int RED = 0;
    public static final int BLACK = 1;

    public Card(Settings settings) {
        super(settings);
        this.suit = NONE;
        this.number = ACE;
        this.color = BLACK;  // 红色：红桃、方片，黑色：黑桃、草花
        this.setCardId("Card-" + System.currentTimeMillis());  // 使用当前时间戳生成唯一ID
    }

    public Card(Settings settings, int suit, int number, String baseId) {
        super(settings);
        this.suit = suit;
        this.number = number;
        this.baseId = baseId;
        this.color = (suit == HEART || suit == DIAMOND) ? RED : BLACK;
        this.setCardId("Card-" + System.currentTimeMillis());
    }


    public void setSuit(int suit) {
        this.suit = suit;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getSuit() {
        return suit;
    }

    public int getNumber() {
        return number;
    }

    public int getColor() {
        return color;
    }

    // 获取卡牌ID
    public String getCardId() {
        return cardId;
    }

    // 设置卡牌ID
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }
    // 在物品描述栏显示卡牌的点数、花色和颜色
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        // 获取花色、点数和颜色
        String suitName = getSuitName();
        String numberName = getNumberName();
        String colorName = getColorName();

        // 添加花色、点数、颜色到描述栏
        tooltip.add(Text.translatable("item.sgsmod.card.suit", suitName));  // 花色
        tooltip.add(Text.translatable("item.sgsmod.card.number", numberName)); // 点数
        tooltip.add(Text.translatable("item.sgsmod.card.color", colorName));  // 颜色
    }
    // 返回花色的名字
    private String getSuitName() {
        switch (suit) {
            case SPADE: return "黑桃";
            case HEART: return "红桃";
            case CLUB: return "梅花";
            case DIAMOND: return "方片";
            default: return "无色";
        }
    }

    // 返回点数的名字
    private String getNumberName() {
        switch (number) {
            case ACE: return "A";
            case TWO: return "2";
            case THREE: return "3";
            case KING: return "K";
            default: return String.valueOf(number + 1);  // 对应 4 - 10
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return super.use(world, user, hand);
    }

    // 返回颜色的名字
    private String getColorName() {
        return color == RED ? "红色" : "黑色";
    }


    public String getBaseId() {
        return baseId;
    }
}
