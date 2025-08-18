package com.chengcode.sgsmod.item;

import com.chengcode.sgsmod.card.Card;
import net.minecraft.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CardStack {
    private static final Logger LOGGER = LoggerFactory.getLogger(CardStack.class);
    private final Deque<Card> stack = new ArrayDeque<>(); // 直接存储Card类型，避免转换问题
    private static String MODE = "test";

    // 每种牌的花色数量配置
    private static final Map<String, Map<Integer, Integer>> CARD_COUNTS = new HashMap<>();

    static {
        // 花色常量
        int SPADE = Card.SPADE;
        int HEART = Card.HEART;
        int CLUB = Card.CLUB;
        int DIAMOND = Card.DIAMOND;

        // 杀
        CARD_COUNTS.put("sha", Map.of(
                DIAMOND, 8,
                CLUB, 18,
                HEART, 6,
                SPADE, 12
        ));
        // 闪
        CARD_COUNTS.put("shan", Map.of(
                DIAMOND, 17,
                HEART, 7
        ));
        // 桃
        CARD_COUNTS.put("tao", Map.of(
                DIAMOND, 3,
                HEART, 9
        ));
//        // 无懈可击EX
//        CARD_COUNTS.put("wuxieEX", Map.of(
//                DIAMOND, 1,
//                CLUB, 2,
//                HEART, 2,
//                SPADE, 2
//        ));
        // 过河拆桥
        CARD_COUNTS.put("chaiqiao", Map.of(
                CLUB, 2,
                HEART, 1,
                SPADE, 3
        ));
        // 顺手牵羊
        CARD_COUNTS.put("shunshou", Map.of(
                DIAMOND, 2,
                SPADE, 3
        ));
        // 无中生有
        CARD_COUNTS.put("wuzhong", Map.of(
                HEART, 4
        ));
        // 南蛮入侵
        CARD_COUNTS.put("nanman", Map.of(
                CLUB, 1,
                SPADE, 2
        ));
        // 乐不思蜀
        CARD_COUNTS.put("lebusishu", Map.of(
                CLUB, 1,
                HEART, 1,
                SPADE, 1
        ));
        // 借刀杀人
        CARD_COUNTS.put("jiedao", Map.of(
                CLUB, 2
        ));
        // 无懈可击
        CARD_COUNTS.put("wuxie", Map.of(
                DIAMOND, 1,
                CLUB, 2
        ));
    }

    public CardStack() {
        reset(MODE);
    }

    public void reset(String mode) {
        stack.clear();
        List<Card> cards = new ArrayList<>(); // 直接使用Card泛型列表

        if (mode.equals("test") || mode.equals("standard")) {
            initializeCards(cards); // 只初始化支持的模式
            MODE = mode;
        } else {
            LOGGER.error("不支持的模式: {}", mode);
            return;
        }

        Collections.shuffle(cards);
        stack.addAll(cards);
        LOGGER.debug("牌堆重置完成，模式: {}, 卡牌数量: {}", mode, cards.size());
    }

    private void initializeCards(List<Card> cards) {
        for (Map.Entry<String, Map<Integer, Integer>> entry : CARD_COUNTS.entrySet()) {
            String baseId = entry.getKey();
            Map<Integer, Integer> suitCounts = entry.getValue();

            for (Map.Entry<Integer, Integer> suitEntry : suitCounts.entrySet()) {
                int suit = suitEntry.getKey();
                int count = suitEntry.getValue();
                boolean found = false;

                // 从ALL_CARDS中筛选出符合条件的Card
                for (Item item : ModItems.ALL_CARDS) {
                    // 严格检查是否为Card类型
                    if (item instanceof Card cardItem) {
                        if (cardItem.getBaseId().equals(baseId) && cardItem.getSuit() == suit) {
                            // 添加指定数量的卡牌
                            for (int i = 0; i < count; i++) {
                                cards.add(cardItem);
                            }
                            found = true;
                            break;
                        }
                    } else {
                        // 日志警告：非Card类型混入
                        LOGGER.warn("ALL_CARDS中包含非Card类型物品: {}", item.getClass().getSimpleName());
                    }
                }

                // 检查是否找到对应卡牌，避免漏配
                if (!found) {
                    LOGGER.error("未找到符合条件的卡牌: baseId={}, suit={}", baseId, suit);
                }
            }
        }
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    /**
     * 抽牌并自动重置空牌堆
     * @return 卡牌实例（非null）
     */
    public Card draw() {
        if (stack.isEmpty()) {
            LOGGER.debug("牌堆为空，自动重置...");
            reset(MODE);
        }
        // 此时stack必然非空，直接取出
        return stack.pollFirst();
    }

    public int size() {
        return stack.size();
    }

    /**
     * 获取牌堆顶部的x张牌
     * @param x 数量
     * @return 卡牌列表（数量可能小于x，若牌堆不足）
     */
    public List<Card> getTop(int x) {
        List<Card> topCards = new ArrayList<>();
        x = Math.max(0, x); // 避免负数

        for (int i = 0; i < x; i++) {
            if (isEmpty()) {
                reset(MODE); // 空牌堆时重置
                if (isEmpty()) break; // 仍为空则退出
            }
            topCards.add(stack.pollFirst());
        }
        return topCards;
    }
    /**
     * 向牌堆中添加一张指定的卡牌
     * @param baseId 卡牌基础ID
     * @param suit 花色
     * @param number 点数
     */
    public void addCard(String baseId, int suit, int number) {
        // 从ALL_CARDS中查找匹配的Card
        for (Item item : ModItems.ALL_CARDS) {
            if (item instanceof Card cardItem) {
                if (cardItem.getBaseId().equals(baseId) && cardItem.getSuit() == suit) {
                    stack.add(cardItem);
                    return;
                }
            }
        }
        LOGGER.warn("未找到匹配的卡牌: baseId={}, suit={}", baseId, suit);
    }

}
