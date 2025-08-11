package com.chengcode.sgsmod.manager;

import com.chengcode.sgsmod.entity.GeneralEntity;
import com.chengcode.sgsmod.card.Card;
import com.chengcode.sgsmod.item.CardStack;
import com.chengcode.sgsmod.item.ModItems;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.entity.EntityPose;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CardGameManager
 * ----------------
 * 负责管理纸牌游戏的核心逻辑，包括发牌、交换卡牌、弃牌等操作。
 *
 * <p>功能概要：</p>
 * <ul>
 *   <li>管理玩家的卡牌手牌</li>
 *   <li>支持从牌堆中发牌</li>
 *   <li>支持卡牌交换与偷牌逻辑</li>
 *   <li>支持弃牌和回收机制</li>
 * </ul>
 *
 *
 * @author  方溯_source
 * @version 1.0
 * @since   2025-08-10
 */
public class CardGameManager {

    /** 当前可操作的卡牌槽位索引（偷牌、拆牌等使用） */
    private static final List<Integer> cardIndices = new ArrayList<>();

    /** 随机数生成器 */
    private static final Random RANDOM = new Random();

    /** 濒死玩家列表（通过 UUID 标记） */
    private static final Set<UUID> DYING_PLAYERS = new HashSet<>();

    /** 牌堆对象 */
    private static final CardStack cardStack = new CardStack();

    /** 定时任务调度器（用于测试模式自动发牌） */
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /** 测试模式下的卡牌池 */
    private static final List<Item> TEST_CARDS = Arrays.asList(
            ModItems.SHA, ModItems.SHAN, ModItems.TAO, ModItems.JIU,
            ModItems.WUXIE, ModItems.WUZHONG, ModItems.CHAIQIAO, ModItems.SHUNSHOU
    );

    /* =========================================================
     * 测试模式管理
     * ========================================================= */

    /**
     * 启动测试模式：初始化牌堆、分发初始牌，并定时摸牌
     */
    public static void startTestMode(List<ServerPlayerEntity> players, List<GeneralEntity> generals) {
        System.out.println("进入测试模式：正在初始化牌堆与角色手牌...");
        cardStack.reset();

        // 发放初始牌
        players.forEach(CardGameManager::initHand);
        generals.forEach(CardGameManager::initHand);

        // 每 15 秒给每个角色摸 2 张牌
        scheduler.scheduleAtFixedRate(() -> {
            players.forEach(p -> { giveCard(p); giveCard(p); });
            generals.forEach(g -> { giveCard(g); giveCard(g); });
        }, 0, 15, TimeUnit.SECONDS);
    }

    /** 初始化玩家手牌（清空 → 发 4 张牌） */
    private static void initHand(PlayerEntity player) {
        clearHand(player);
        for (int i = 0; i < 4; i++) giveCard(player);
    }

    /** 初始化武将手牌（清空 → 发 4 张牌） */
    private static void initHand(GeneralEntity general) {
        clearHand(general);
        for (int i = 0; i < 4; i++) giveCard(general);
    }

    /** 清空玩家的手牌（仅清理属于牌的物品） */
    public static void clearHand(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (isCard(stack.getItem())) {
                player.getInventory().setStack(i, ItemStack.EMPTY);
            }
        }
    }

    /** 清空武将的手牌（仅清理属于牌的物品） */
    private static void clearHand(GeneralEntity general) {
        for (int i = 0; i < general.getInventory().size(); i++) {
            ItemStack stack = general.getInventory().getStack(i);
            if (isCard(stack.getItem())) {
                general.getInventory().setStack(i, ItemStack.EMPTY);
            }
        }
    }

    /** 判断物品是否是测试卡牌 */
    static boolean isCard(Item item) {
        return TEST_CARDS.contains(item);
    }

    /* =========================================================
     * 发牌逻辑
     * ========================================================= */

    /** 判断玩家背包是否有空位 */
    private static boolean hasInventorySpace(PlayerEntity player) {
        return player.getInventory().getEmptySlot() != -1;
    }

    /** 给玩家发 1 张牌 */
    public static void giveCard(PlayerEntity player) {
        if (!hasInventorySpace(player)) {
            player.sendMessage(Text.of("你的背包满了，无法获得牌"), false);
            return;
        }

        Item cardItem = cardStack.draw();
        if (cardItem == null) {
            player.sendMessage(Text.of("牌堆已空，重新洗牌"), false);
            return;
        }

        ItemStack newCardStack = new ItemStack(cardItem);

        // 动态设置卡牌属性
        if (cardItem instanceof Card card) {
            setCardAttributes(card);
        }

        if (player.getInventory().insertStack(newCardStack)) {
            player.sendMessage(cardName(cardItem), false);
        } else {
            player.sendMessage(Text.of("你的背包满了，无法获得牌"), false);
        }
    }

    /** 给武将发 1 张牌 */
    public static void giveCard(GeneralEntity general) {
        Item cardItem = cardStack.draw();
        if (cardItem == null) return;

        ItemStack newCardStack = new ItemStack(cardItem);
        if (general.getInventory().addStack(newCardStack).isEmpty()) {
            general.say(cardName(cardItem).getString());
        }
    }

    /** 获取卡牌的显示名称 */
    private static Text cardName(Item item) {
        if (item == ModItems.SHA) return Text.of("摸到：杀");
        if (item == ModItems.SHAN) return Text.of("摸到：闪");
        if (item == ModItems.TAO) return Text.of("摸到：桃");
        if (item == ModItems.JIU) return Text.of("摸到：酒");
        if (item == ModItems.WUXIE) return Text.of("摸到：无懈可击");
        if (item == ModItems.WUZHONG) return Text.of("摸到：无中生有");
        if (item == ModItems.CHAIQIAO) return Text.of("摸到：过河拆桥");
        if (item == ModItems.SHUNSHOU) return Text.of("摸到：顺手牵羊");
        return Text.of("未知牌");
    }

    /** 停止所有调度任务并清理 */
    public static void clearAll() {
        scheduler.shutdownNow();
    }

    /* =========================================================
     * 濒死状态管理
     * ========================================================= */

    /** 进入濒死状态 */
    public static void enterDyingState(PlayerEntity player) {
        DYING_PLAYERS.add(player.getUuid());
        player.setHealth(1.0f);
        player.setVelocity(0, 0, 0);
        player.setNoGravity(true);
        player.setBodyYaw(player.getBodyYaw() + 90.0f);

        player.sendMessage(Text.of("你已濒死，等待救援..."), false);
    }

    /** 退出濒死状态 */
    public static void exitDyingState(PlayerEntity player) {
        DYING_PLAYERS.remove(player.getUuid());
        player.setNoGravity(false);
        player.setBodyYaw(player.getBodyYaw() - 90.0f);
        player.setPose(EntityPose.STANDING);
        player.sendMessage(Text.of("你被救活了！"), false);
    }


    /** 判断玩家是否濒死 */
    public static boolean isInDyingState(PlayerEntity player) {
        return DYING_PLAYERS.contains(player.getUuid());
    }

    /* =========================================================
     * 卡牌属性与体力管理
     * ========================================================= */

    /** 随机设置卡牌属性（花色、点数、颜色） */
    private static void setCardAttributes(Card card) {
        int suit = RANDOM.nextInt(4);   // 0黑桃, 1红桃, 2梅花, 3方片
        int number = RANDOM.nextInt(13); // 0A, 1~9, 10J, 11Q, 12K
        int color = (suit == 1 || suit == 3) ? 0 : 1; // 0红, 1黑

        card.setSuit(suit);
        card.setNumber(number);
        card.setColor(color);
    }

    /** 失去体力值（播放音效） */
    public static void loseHealth(LivingEntity entity, float amount) {
        ServerWorld world = (ServerWorld) entity.getWorld();
        world.playSound(null, entity.getBlockPos(),
                ModSoundEvents.LOSEHEALTH, SoundCategory.PLAYERS, 1.0F, 1.0F);
        entity.heal(-amount);
    }

    /** 恢复体力值（播放音效） */
    public static void recoverHealth(LivingEntity entity, float amount) {
        ServerWorld world = (ServerWorld) entity.getWorld();
        world.playSound(null, entity.getBlockPos(),
                ModSoundEvents.TAO_HEAL, SoundCategory.PLAYERS, 1.0F, 1.0F);
        entity.heal(amount);
    }

    /* =========================================================
     * 偷牌 / 拆牌逻辑
     * ========================================================= */

    // ...（此处可继续添加偷牌、拆牌方法并加注释）
    // 获取一个玩家的牌
    public static void ObtainCard(LivingEntity e1, LivingEntity e2) {

        if (e1 instanceof ServerPlayerEntity player) {
            // 检查背包是否有空格
            if (player.getInventory().getEmptySlot() == -1) {
                player.sendMessage(Text.of("你的背包已满，无法获得卡牌！"), false);
                return;
            }

            // 检查目标是否为玩家
            if (e2 instanceof ServerPlayerEntity targetPlayer) {
                // 更新目标玩家的卡牌索引
                updateCardIndices(targetPlayer);
                if (cardIndices.isEmpty()) {
                    player.sendMessage(Text.of("目标玩家没有卡牌！"), false);
                    return;
                }

                // 随机选择一张卡牌
                int randomIndex = RANDOM.nextInt(cardIndices.size());
                ItemStack stolenCard = getRandomCardOptimized(targetPlayer, randomIndex);
                if (stolenCard == null) {
                    player.sendMessage(Text.of("目标玩家没有卡牌！"), false);
                } else {
                    // 移除目标玩家的卡牌
                    targetPlayer.getInventory().removeStack(cardIndices.get(randomIndex));

                    // 将卡牌添加到偷取者的背包
                    String name = stolenCard.getName().getString();
                    boolean success = player.getInventory().insertStack(stolenCard);
                    if (success) {
                        player.sendMessage(Text.of("你成功偷取了" + name + "！"), false);
                        targetPlayer.sendMessage(Text.of("你的卡牌被偷走了！"), false);
                    } else {
                        // 如果背包满了，归还卡牌
                        targetPlayer.getInventory().insertStack(stolenCard);
                        player.sendMessage(Text.of("你的背包已满，无法获得卡牌！"), false);
                    }
                }
            } else if (e2 instanceof GeneralEntity targetGeneral) {
                if (!targetGeneral.getInventory().isEmpty()) {
                    updateCardIndices(targetGeneral);
                    if (cardIndices.isEmpty()) {
                        player.sendMessage(Text.of("目标武将没有卡牌！"), false);
                        return;
                    }
                    // 随机选择一张卡牌
                    int randomIndex = RANDOM.nextInt(cardIndices.size());
                    ItemStack stolenCard = getRandomCardOptimized(targetGeneral, randomIndex);
                    if (stolenCard == null) {
                        player.sendMessage(Text.of("目标武将没有卡牌！"), false);
                    } else {
                        targetGeneral.getInventory().removeStack(cardIndices.get(randomIndex));
                        // 将卡牌添加到偷取者的背包
                        String name = stolenCard.getName().getString();
                        boolean success = player.getInventory().insertStack(stolenCard);
                        if (!success) {
                            player.sendMessage(Text.of("你的背包已满，无法获得卡牌！"), false);
                        } else {
                            player.sendMessage(Text.of("你成功偷取了" + name + "！"), false);
                        }
                    }
                }
            }
        }else if (e1 instanceof GeneralEntity general) {
            // 检查目标是否为玩家
            if (e2 instanceof ServerPlayerEntity targetPlayer) {
                // 更新目标玩家的卡牌索引
                updateCardIndices(targetPlayer);
                if (cardIndices.isEmpty()) {
                    general.say("目标玩家没有卡牌！");
                    return;
                }

                // 随机选择一张卡牌
                int randomIndex = RANDOM.nextInt(cardIndices.size());
                ItemStack stolenCard = getRandomCardOptimized(targetPlayer, randomIndex);
                if (stolenCard == null) {
                    general.say("目标玩家没有卡牌！");
                } else {
                    // 移除目标玩家的卡牌
                    targetPlayer.getInventory().removeStack(cardIndices.get(randomIndex));

                    // 将卡牌添加到武将的背包
                    String name = stolenCard.getName().getString();
                    ItemStack remainder = general.getInventory().addStack(stolenCard);
                    boolean success = remainder.isEmpty();
                    if (success) {
                        general.say(("成功偷取了" + name + "！"));
                        targetPlayer.sendMessage(Text.of("你的卡牌被偷走了！"), false);
                    }
                }
            } else if (e2 instanceof GeneralEntity targetGeneral) {
                // 更新目标武将的卡牌索引
                updateCardIndices(targetGeneral);
                if (cardIndices.isEmpty()) {
                    general.say(targetGeneral.getName().getString() + "没有卡牌！");
                    return;
                }

                // 随机选择一张卡牌
                int randomIndex = RANDOM.nextInt(cardIndices.size());
                ItemStack stolenCard = getRandomCardOptimized(targetGeneral, randomIndex);
                if (stolenCard == null) {
                    general.say(targetGeneral.getName().getString() + "没有卡牌！");
                } else {
                    // 移除目标武将的卡牌
                    targetGeneral.getInventory().removeStack(cardIndices.get(randomIndex));
                    // 将卡牌添加到偷取者的背包
                    String name = stolenCard.getName().getString();
                    ItemStack remainder = general.getInventory().addStack(stolenCard);
                    boolean success = remainder.isEmpty();
                    if (!success) {
                        general.say("背包已满，无法获得卡牌！");
                    } else {
                        general.say("成功偷取了一张" + name + "！");
                    }
                }
            }
        }

    }



    // 随机获得物品栏的牌，返回索引
    public static ItemStack getRandomCardOptimized(LivingEntity entity,int randomIndex) {
        if (!cardIndices.isEmpty()) {
            int selectedIndex = cardIndices.get(randomIndex);
            if (entity instanceof ServerPlayerEntity  player) {
                return player.getInventory().getStack(selectedIndex);
            }else if (entity instanceof GeneralEntity general) {
                return general.getInventory().getStack(selectedIndex);
            }
        }
        return null;
    }


    // 当添加或移除物品时更新cardIndices
    private static void updateCardIndices(PlayerEntity player) {
        cardIndices.clear();
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack itemStack = player.getInventory().getStack(i);
            if (itemStack.getItem() instanceof Card) {
                cardIndices.add(i);
            }
        }
    }

    private static void updateCardIndices(GeneralEntity general){
        cardIndices.clear();
        for (int i = 0; i < general.getInventory().size(); i++) {
            ItemStack itemStack = general.getInventory().getStack(i);
            if (itemStack.getItem() instanceof Card) {
                cardIndices.add(i);
            }
        }
    }

    public static boolean removeRandomCard(LivingEntity targetPlayer, String area) {
        if (targetPlayer instanceof ServerPlayerEntity player) {
            switch (area) {
                case "PD":
                    //TODO: 添加处理判定区的逻辑
                    break;
                case "SP":
                    // 随机选择一张卡牌
                    updateCardIndices(player);
                    if (!cardIndices.isEmpty()) {
                        int randomIndex = RANDOM.nextInt(cardIndices.size());
                        ItemStack stolenCard = getRandomCardOptimized(targetPlayer, randomIndex);
                        if (stolenCard != null) {
                            player.getInventory().removeStack(cardIndices.get(randomIndex)).decrement(1);
                            return true;
                        }else {
                            return false;
                        }
                    }
                    else{
                        return false;
                    }
                case "ZB":
                    // 随机选择一个装备（已装备）
                    List<Integer> equipmentSlots = new ArrayList<>();

                    // 检查各个装备槽位是否有装备
                    for (int i = 0; i < 6; i++) { // 0-3是盔甲槽位，4是副手，5是主手
                        ItemStack equipment = player.getInventory().getStack(i);
                        if (!equipment.isEmpty() && !(equipment.getItem() instanceof Card)) {
                            equipmentSlots.add(i);
                        }
                    }

                    // 如果有装备，随机移除一个
                    if (!equipmentSlots.isEmpty()) {
                        int randomSlot = equipmentSlots.get(RANDOM.nextInt(equipmentSlots.size()));
                        ItemStack removedEquipment = player.getInventory().removeStack(randomSlot);
                        // 可以选择将装备丢到地上或者销毁
                        // 这里是直接移除（销毁）
                        player.sendMessage(Text.of("你的" + removedEquipment.getName().getString() + "被移除了！"), false);
                        removedEquipment.decrement(1);
                        return true;
                    } else {
                        player.sendMessage(Text.of("你没有装备任何物品！"), false);
                        return false;
                    }
                case "WP":
                    // 掉物品栏
                    if (!player.getInventory().isEmpty()) {
                        // 随机移除
                        ItemStack removedEquipment = player.getInventory().removeStack(RANDOM.nextInt(player.getInventory().size()-1));
                        player.sendMessage(Text.of("你被拆了" + removedEquipment.getName().getString()), false);
                    }
            }
        } else if (targetPlayer instanceof GeneralEntity general) {
            switch ( area)
            {
                case "PD":
                    //TODO: 添加处理判定区的逻辑
                    break;
                case "SP":
                    // 随机选择一张卡牌
                    updateCardIndices(general);
                    if (!cardIndices.isEmpty()) {
                        int randomIndex = RANDOM.nextInt(cardIndices.size());
                        ItemStack stolenCard = getRandomCardOptimized(targetPlayer, randomIndex);
                        if (stolenCard != null) {
                            general.say("我的" + stolenCard.getName().getString() + "被拆了！");
                            general.getInventory().removeStack(cardIndices.get(randomIndex)).decrement(1);
                            return true;
                        }else {
                            return false;
                        }
                    }
                    break;
                case "ZB":
                    // TODO: 添加处理装备区的逻辑
                    break;
            }
        }
        return false;
    }
}
