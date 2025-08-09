package com.chengcode.sgsmod.item;

import net.minecraft.item.Item;

import java.util.*;

public class CardStack {
    private final Deque<Item> stack = new ArrayDeque<>();
    private static final int REGULAR_CARD_COUNT = 32;  // 普通牌数量
    private static final int TACTIC_CARD_COUNT = 8;    // 锦囊牌数量

    public CardStack() {
        reset();
    }

    // 重置牌堆
    public void reset() {
        stack.clear();
        List<Item> cards = new ArrayList<>();

        // 初始化普通卡牌
        initializeRegularCards(cards);

        // 初始化锦囊卡牌
        initializeTacticCards(cards);

        // 洗牌
        Collections.shuffle(cards);
        stack.addAll(cards);
    }

    // 初始化普通卡牌（每种卡 32 张）
    private void initializeRegularCards(List<Item> cards) {
        for (int i = 0; i < REGULAR_CARD_COUNT; i++) {
            cards.add(ModItems.SHA);
            cards.add(ModItems.SHAN);
            cards.add(ModItems.TAO);
            cards.add(ModItems.JIU);
        }
    }

    // 初始化锦囊卡牌（每种锦囊牌 8 张）
    private void initializeTacticCards(List<Item> cards) {
        for (int i = 0; i < TACTIC_CARD_COUNT; i++) {
            cards.add(ModItems.WUXIE);
            cards.add(ModItems.WUZHONG);
            cards.add(ModItems.CHAIQIAO);
            cards.add(ModItems.SHUNSHOU);
        }
    }

    // 判断牌堆是否为空
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    // 抽取一张卡牌
    public Item draw() {
        if (stack.isEmpty()) {
            reset();  // 牌堆为空时，重新洗牌
        }
        return stack.pollFirst();  // 从牌堆顶部抽取一张卡牌
    }

    // 获取当前牌堆的大小
    public int size() {
        return stack.size();
    }
}
