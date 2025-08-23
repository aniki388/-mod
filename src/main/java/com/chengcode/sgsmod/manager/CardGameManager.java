package com.chengcode.sgsmod.manager;

import com.chengcode.sgsmod.card.Card;
import com.chengcode.sgsmod.entity.GeneralEntity;
import com.chengcode.sgsmod.entity.ShaEntity;
import com.chengcode.sgsmod.gui.TurnOrderScreen;
import com.chengcode.sgsmod.item.CardStack;
import com.chengcode.sgsmod.item.HandManagerItem;
import com.chengcode.sgsmod.item.ModItems;
import com.chengcode.sgsmod.network.NetWorking;
import com.chengcode.sgsmod.skill.Skills;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import com.chengcode.sgsmod.sound.SkillSoundManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.chengcode.sgsmod.manager.CardGameManager.ClientTurnManager.getClientTurnOrder;

/**
 * CardGameManager
 * ----------------
 * 负责管理纸牌游戏的核心逻辑，包括发牌、交换卡牌、弃牌等操作。
 * 新增功能：弃牌堆（支持卡牌丢弃、重洗补充牌堆）
 *
 * <p>功能概要：</p>
 * <ul>
 *   <li>管理玩家的卡牌手牌</li>
 *   <li>支持从牌堆中发牌</li>
 *   <li>支持卡牌交换与偷牌逻辑</li>
 *   <li>支持弃牌和回收机制（新增弃牌堆）</li>
 * </ul>
 *
 * @author  方溯_source
 * @version 1.3 新增弃牌堆版
 * @since   2025-08-17
 */
public class CardGameManager {
    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final Logger LOGGER = LoggerFactory.getLogger(CardGameManager.class);

    /** 扣在武将上的牌（存储物品+原槽位） */
    private static final ConcurrentHashMap<UUID, CoveredItems> cardsBesideHero = new ConcurrentHashMap<>();

    /** 随机数生成器 */
    private static final Random RANDOM = new Random();

    /** 玩家摸牌Map，使用UUID作为键确保线程安全 */
    private static final ConcurrentHashMap<UUID, Integer> DrawCardMap = new ConcurrentHashMap<>();

    /** 濒死玩家列表（通过UUID标记） */
    private static final ConcurrentHashMap<UUID, Integer> DYING_PLAYERS = new ConcurrentHashMap<>();

    /** 玩家是否自动发牌到手牌区 */
    private static final ConcurrentHashMap<UUID, Boolean> AUTO_DRAW_HAND = new ConcurrentHashMap<>();

    /** 缓存濒死玩家原始状态（解决移动限制恢复问题） */
    private static final ConcurrentHashMap<UUID, Boolean> ORIGINAL_FLYING = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Float> ORIGINAL_SPEED = new ConcurrentHashMap<>();

    /** 牌堆对象 */
    private static final CardStack cardStack = new CardStack();

    /** 坐次信息 */
    private static  Map<Integer, LivingEntity> turnOrder = new ConcurrentHashMap<>();

    // ===============================
    // 新增：弃牌堆核心结构
    // ===============================
    /**
     * 弃牌堆：使用ConcurrentLinkedQueue确保线程安全（支持多线程并发丢弃/重洗）
     * 存储被丢弃的卡牌ItemStack（保留卡牌完整信息：花色、点数、类型）
     */
    private static final ConcurrentLinkedQueue<ItemStack> DISCARD_PILE = new ConcurrentLinkedQueue<>();
    /** 弃牌堆操作锁：避免重洗时同时丢弃导致的数据不一致 */
    private static final Object DISCARD_PILE_LOCK = new Object();

    /** 手牌槽位缓存：key=玩家UUID，value=该玩家手牌所在的槽位索引列表 */
    private static final ConcurrentHashMap<UUID, List<Integer>> HAND_CARD_SLOTS_CACHE = new ConcurrentHashMap<>();

    public static CardStack getCardStack() {
        return cardStack;
    }

    public static void turnOn(PlayerEntity player,boolean on) {
        AUTO_DRAW_HAND.put(player.getUuid(),on);
    }


    /**
     * 获取自动发牌状态（默认 false）
     */
    public static boolean getAutoHand(PlayerEntity player) {
        return AUTO_DRAW_HAND.getOrDefault(player.getUuid(),false);
    }

    /**
     * 切换自动发牌状态（返回新的状态）
     */
    public static boolean toggleAutoHand(PlayerEntity player) {
        boolean newValue = !getAutoHand(player);
        AUTO_DRAW_HAND.put(player.getUuid(), newValue);
        return newValue;
    }

    public static void setAutoHand(PlayerEntity player, boolean on) {
        AUTO_DRAW_HAND.put(player.getUuid(), on);
    }

    /** 测试模式下的卡牌池 */
    private static final List<Item> TEST_CARDS = Arrays.asList(
            ModItems.SHA, ModItems.SHAN, ModItems.TAO, ModItems.JIU,
            ModItems.WUXIE, ModItems.WUZHONG, ModItems.CHAIQIAO, ModItems.SHUNSHOU
    );

    // 添加字段用于计时
    private static int drawCardTickCounter = 0;
    private static final int DRAW_CARD_INTERVAL = 15 * 20; // 15秒 * 20 ticks/秒


    /* =========================================================
     * 新增：弃牌堆核心方法
     * ========================================================= */
    /**
     * 将卡牌加入弃牌堆（核心方法）
     * @param stack 要丢弃的卡牌ItemStack（必须是非空且属于卡牌的物品）
     * @return 丢弃成功返回true，失败（空栈/非卡牌）返回false
     */
    public static boolean discard(ItemStack stack) {
        // 1. 校验输入有效性：非空栈且是卡牌
        if (stack.isEmpty() || !(stack.getItem() instanceof Card)) {
            LOGGER.warn("尝试丢弃无效卡牌：空栈或非卡牌物品");
            return false;
        }

        // 2. 线程安全添加到弃牌堆（加锁避免与重洗操作冲突）
        synchronized (DISCARD_PILE_LOCK) {
            DISCARD_PILE.offer(stack.copy()); // 复制卡牌信息，避免原栈修改影响弃牌堆
            LOGGER.debug("卡牌已加入弃牌堆：{}（剩余数量：{}）",
                    stack.getName().getString(), DISCARD_PILE.size());
        }

        return true;
    }

    /**
     * 从弃牌堆重洗卡牌到牌堆（牌堆为空时自动调用）
     * @return 重洗的卡牌数量，0表示弃牌堆也为空
     */
    public static int reshuffleFromDiscardPile() {
        synchronized (DISCARD_PILE_LOCK) {
            // 1. 检查弃牌堆是否有卡牌
            if (DISCARD_PILE.isEmpty()) {
                LOGGER.warn("弃牌堆为空，无法重洗补充牌堆");
                return 0;
            }

            // 2. 收集弃牌堆所有卡牌
            List<ItemStack> discardCards = new ArrayList<>(DISCARD_PILE);
            int reshuffleCount = discardCards.size();

            // 3. 随机打乱卡牌顺序（模拟洗牌）
            Collections.shuffle(discardCards, RANDOM);

            // 4. 将打乱的卡牌添加到牌堆
            for (ItemStack stack : discardCards) {
                if (stack.getItem() instanceof Card card) {
                    cardStack.addCard(card.getBaseId(), card.getSuit(), card.getNumber());
                }
            }

            // 5. 清空弃牌堆（重洗完成）
            DISCARD_PILE.clear();

            return reshuffleCount;
        }
    }

    /**
     * 获取弃牌堆当前卡牌数量
     * @return 弃牌堆大小
     */
    public static int getDiscardPileSize() {
        synchronized (DISCARD_PILE_LOCK) {
            return DISCARD_PILE.size();
        }
    }

    /**
     * 清空弃牌堆（游戏重置时调用）
     */
    public static void clearDiscardPile() {
        synchronized (DISCARD_PILE_LOCK) {
            DISCARD_PILE.clear();
            LOGGER.info("弃牌堆已清空");
        }
    }


    /* =========================================================
     * 测试模式管理（兼容弃牌堆）
     * ========================================================= */

    /**
     * 启动测试模式：初始化牌堆、分发初始牌，并定时摸牌
     */
    public static void startMode(List<ServerPlayerEntity> players, List<GeneralEntity> generals, MinecraftServer server, String mode, PlayerEntity host) {
        LOGGER.info("进入测试模式：正在初始化牌堆与角色手牌...");
        cardStack.reset("mode");
        clearDiscardPile(); // 初始化时清空弃牌堆

        // 发放初始牌
        players.forEach(CardGameManager::initHand);
        generals.forEach(CardGameManager::initHand);

        switch ( mode) {
            case "test":
                scheduler.scheduleAtFixedRate(() -> {
                    server.execute(() -> {  // 确保逻辑在主线程执行
                        players.forEach(p -> {
                            int drawCnt = DrawCardMap.getOrDefault(p.getUuid(), 2);
                            if (ShaEntity.hasSkill(p, Skills.yinzi)) {
                                drawCnt += 1;
                                SkillSoundManager.playSkillSound("yinzi", p);
                            }
                            giveCard(p, drawCnt);
                        });
                        generals.forEach(g -> {
                            int drawCnt = g.getDrawNum() <= 0 ? 2 : g.getDrawNum();
                            if (ShaEntity.hasSkill(g, Skills.yinzi)) {
                                drawCnt += 1;
                                SkillSoundManager.playSkillSound("yinzi", g);
                            }
                            giveCard(g, drawCnt);
                        });
                    });
                }, 0, 15, TimeUnit.SECONDS);
                break;
            default:
                break;
        }
    }

    public static void EndTurn(int lastTurn, LivingEntity livingEntity) {
        String name;
        if (livingEntity instanceof PlayerEntity  player) name = player.getEntityName();
        else if (livingEntity instanceof GeneralEntity general) name = Objects.requireNonNullElse(general.getCustomName(), Text.literal("未命名")).getString();
        else name =  "未知";
        DisCardTurn(lastTurn, livingEntity, name);

        say(name + "的回合结束", livingEntity.getWorld());
    }

    private static void DisCardTurn(int lastTurn, LivingEntity livingEntity, String name) {
        // 实体有效性防护（避免空指针）
        if (livingEntity == null || !livingEntity.isAlive() || livingEntity.getWorld() == null) {
            LOGGER.error("DisCardTurn：轮次{}的实体无效（null/已死亡/无世界），跳过弃牌流程", lastTurn);
            return;
        }
        World world = livingEntity.getWorld();
        ScheduledExecutorService tempScheduler = Executors.newSingleThreadScheduledExecutor();

        try {
            say(name + "进入弃牌阶段", world);
            livingEntity.sendMessage(Text.literal("请打开手牌工具进行弃牌，3秒后自动弃牌（手牌和背包中的卡牌均会被统计）"));

            if (!(livingEntity instanceof PlayerEntity player)) {
                LOGGER.debug("DisCardTurn：轮次{}的实体非玩家（{}），跳过弃牌", lastTurn, name);
                return;
            }

            // 关键：将player赋值给final变量，供lambda使用
            final PlayerEntity finalPlayer = player;
            final World finalWorld = world;

            // 统计手牌区（独立存储）+ 背包区所有卡牌
            int currentHand = 0;
            HandInventory hand = getPlayerHand(player);
            boolean hasHandManager = hand != null;

            // 1. 统计手牌区
            if (hasHandManager) {
                for (ItemStack s : hand.getItems()) {
                    if (!s.isEmpty() && s.getItem() instanceof Card) {
                        currentHand += s.getCount();
                    }
                }
            } else {
                boolean giveSuccess = player.getInventory().insertStack(ModItems.HANDMANAGER.getDefaultStack());
                if (giveSuccess) {
                    hand = getPlayerHand(player);
                    hasHandManager = true;
                    for (ItemStack s : hand.getItems()) {
                        if (!s.isEmpty() && s.getItem() instanceof Card) {
                            currentHand += s.getCount();
                        }
                    }
                } else {
                    player.sendMessage(Text.of("你的背包已满，无法获得手牌工具，将统计所有背包卡牌"), false);
                }
            }

            // 2. 统计背包区（0-35槽位）
            for (int i = 0; i < 36; i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (!stack.isEmpty() && stack.getItem() instanceof Card) {
                    currentHand += stack.getCount();
                }
            }

            // 计算需要弃牌的数量
            int maxHand = getHandCapacity(player);
            final int needDiscard = Math.max(0, currentHand - maxHand);
            final boolean finalHasHandManager = hasHandManager;
            final HandInventory finalHand = hand;

            if (needDiscard > 0) {
                tempScheduler.schedule(() -> {
                    MinecraftServer server = finalWorld.getServer();
                    if (server == null) {
                        LOGGER.error("DisCardTurn：无法获取服务器实例，自动弃牌失败");
                        return;
                    }

                    server.execute(() -> {
                        // 修复：用实体所在世界是否为当前世界判断，替代不存在的contains()
                        if (!finalPlayer.isAlive() || finalPlayer.getWorld() != finalWorld) {
                            LOGGER.error("DisCardTurn：玩家{}已离线/死亡，终止自动弃牌", name);
                            return;
                        }

                        int discardedCount = 0;
                        DiscardInventory discardInventory = getPlayerDiscardInventory(finalPlayer);

                        // 1. 优先从弃牌区弃牌
                        if (discardInventory != null) {
                            for (int i = 0; i < discardInventory.size() && discardedCount < needDiscard; i++) {
                                ItemStack stack = discardInventory.getStack(i);
                                if (stack.isEmpty()) continue;

                                discard(stack);
                                discardInventory.removeStack(i);
                                discardInventory.markDirty();
                                discardedCount++;
                            }
                        }

                        // 2. 从手牌区和背包区弃牌
                        if (discardedCount < needDiscard) {
                            // 2.1 手牌区弃牌
                            if (finalHasHandManager && finalHand != null) {
                                for (int i = 0; i < finalHand.getItems().size() && discardedCount < needDiscard; i++) {
                                    ItemStack stack = finalHand.getItems().get(i);
                                    if (stack.isEmpty() || !(stack.getItem() instanceof Card)) continue;

                                    int discardNum = Math.min(stack.getCount(), needDiscard - discardedCount);
                                    for (int j = 0; j < discardNum; j++) {
                                        ItemStack discardStack = stack.split(1);
                                        finalHand.markDirty();
                                        discard(discardStack);
                                        discardedCount++;
                                    }
                                    if (stack.isEmpty()) {
                                        finalHand.getItems().set(i, ItemStack.EMPTY);
                                    }
                                }
                            }

                            // 2.2 背包区弃牌
                            if (discardedCount < needDiscard) {
                                for (int i = 0; i < 36 && discardedCount < needDiscard; i++) {
                                    ItemStack stack = finalPlayer.getInventory().getStack(i);
                                    if (stack.isEmpty() || !(stack.getItem() instanceof Card)) {
                                        continue;
                                    }

                                    int discardNum = Math.min(stack.getCount(), needDiscard - discardedCount);
                                    for (int j = 0; j < discardNum; j++) {
                                        ItemStack discardStack = stack.split(1);
                                        discard(discardStack);
                                        discardedCount++;
                                    }
                                }
                            }
                        }

                        finalPlayer.sendMessage(Text.of("自动弃牌完成，共从手牌和背包中弃置" + discardedCount + "张牌"), false);
                    });
                }, 3, TimeUnit.SECONDS);
            } else {
                player.sendMessage(Text.of("你当前手牌和背包中的卡牌总数（" + currentHand + "张）未超过上限（" + maxHand + "张），无需弃牌！"), false);
            }
        } finally {
            // 确保调度器关闭
            tempScheduler.shutdown();
            try {
                if (!tempScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    tempScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                tempScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }




    private static DiscardInventory getPlayerDiscardInventory(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            for (ItemStack stack : ((ServerPlayerEntity) player).getInventory().main) {
                if (stack.getItem() instanceof HandManagerItem) {
                    return new DiscardInventory(stack);
                }
            }
        }
        return null;
    }

    public static void StartTurn(int i, LivingEntity livingEntity) {
        String name;
        if (livingEntity instanceof PlayerEntity  player) name = player.getEntityName();
        else if (livingEntity instanceof GeneralEntity general) name = Objects.requireNonNullElse(general.getCustomName(), Text.literal("未命名")).getString();
        else name =  "未知";
        say("第" + i + "轮开始，是" + livingEntity.getName().getString() + "的回合", livingEntity.getWorld());
        scheduler.schedule(() -> {
            PrepareTurn(i,livingEntity,name);
        }, 2, TimeUnit.SECONDS);
    }

    private static void PrepareTurn(int i, LivingEntity livingEntity,String name) {
        livingEntity.sendMessage(Text.of("准备阶段"));
        scheduler.schedule(() -> {
            JudgmentStage(i,livingEntity,name);
        }, 2, TimeUnit.SECONDS);
    }

    private static void JudgmentStage(int i, LivingEntity livingEntity,String name) {
        say(name + "的判定阶段", livingEntity.getWorld());
        scheduler.schedule(() -> {
            DrawingStage(i,livingEntity,name);
        }, 2, TimeUnit.SECONDS);
    }

    private static void DrawingStage(int i, LivingEntity livingEntity,String name) {
        say(name + "的摸牌阶段", livingEntity.getWorld());
        int bones = 0;
        if (ShaEntity.hasSkill(livingEntity, Skills.yinzi)) bones ++;
        if (livingEntity instanceof PlayerEntity player) {
            int drawCnt = DrawCardMap.getOrDefault(player.getUuid(), 2);
            if (AUTO_DRAW_HAND.getOrDefault(player,true))
            {
                HandInventory hand = getPlayerHand( player);
                if (hand != null) giveCardTohand(player, drawCnt +  bones, hand);
                else {
                    if(player.giveItemStack(ModItems.HANDMANAGER.getDefaultStack()))
                    {
                        hand = getPlayerHand( player);
                        giveCardTohand(player, drawCnt +  bones, hand);
                    }else {
                        player.sendMessage(Text.of("你的背包满了，无法获得手牌！"), false);
                    }
                }
            }else {
                giveCard( player, drawCnt +  bones);
            }
        }
        else if (livingEntity instanceof GeneralEntity general) {
            int drawCnt = general.getDrawNum() <= 0 ? 2 : general.getDrawNum();
            giveCard(general,drawCnt + bones);
        }
    }

    private static void say(String msg,World world){
        world.getPlayers().forEach(p ->
                p.sendMessage(Text.of( "系统：" + msg), false));
    }

    public static HandInventory getPlayerHand(PlayerEntity player) {
        PlayerInventory inventory = player.getInventory();

        // 遍历玩家所有物品栏和背包
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() instanceof HandManagerItem) {
                return new HandInventory(stack);
            }
        }

        // 尝试再从主手/副手获取（可选，保险起见）
        ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);
        if (mainHand.getItem() instanceof HandManagerItem) {
            return new HandInventory(mainHand);
        }
        ItemStack offHand = player.getStackInHand(Hand.OFF_HAND);
        if (offHand.getItem() instanceof HandManagerItem) {
            return new HandInventory(offHand);
        }

        return null; // 玩家没有手牌区
    }

    /**
     * 安全读取玩家 hand 某个 slot 的 ItemStack（如果 slot 无效或 HandInventory 不存在返回 ItemStack.EMPTY）
     * slot index 按你的 HandInventory 约定（0..N-1）
     */
    public static ItemStack getSlotItem(PlayerEntity player, int slotIndex) {
        Optional<HandInventory> maybe = Optional.ofNullable(getPlayerHand(player));
        if (maybe.isEmpty()) return ItemStack.EMPTY;
        HandInventory hi = maybe.get();
        // 假设 ImplementedInventory 风格：hi.getItems() 返回 DefaultedList<ItemStack>
        try {
            DefaultedList<ItemStack> items = hi.getItems();
            if (slotIndex < 0 || slotIndex >= items.size()) return ItemStack.EMPTY;
            return items.get(slotIndex);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    /** 初始化玩家手牌（清空 → 发4张牌） */
    private static void initHand(PlayerEntity player) {
        clearHand(player);
        DrawCardMap.putIfAbsent(player.getUuid(), 2);
        giveCard(player, 4); // 一次发4张，减少循环次数
    }

    /** 初始化武将手牌（清空 → 发4张牌） */
    private static void initHand(GeneralEntity general) {
        clearHand(general);
        if (general.getDrawNum() != 2) {
            general.setDrawNum(2);
        }
        giveCard(general, 4); // 一次发4张，减少循环次数
    }

    /** 清空玩家的手牌（仅清理属于牌的物品，清空时自动丢弃到弃牌堆） */
    public static void clearHand(PlayerEntity player) {
        boolean changed = false;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (isCard(stack.getItem())) {
                discard(stack); // 新增：清空手牌时将卡牌加入弃牌堆
                player.getInventory().setStack(i, ItemStack.EMPTY);
                changed = true;
            }
        }
        // 如果有变化，更新缓存
        if (changed && player instanceof ServerPlayerEntity serverPlayer) {
            updateHandCardCache(serverPlayer);
        }
    }

    /** 清空武将的手牌（仅清理属于牌的物品，清空时自动丢弃到弃牌堆） */
    private static void clearHand(GeneralEntity general) {
        for (int i = 0; i < general.getInventory().size(); i++) {
            ItemStack stack = general.getInventory().getStack(i);
            if (isCard(stack.getItem())) {
                discard(stack); // 新增：清空手牌时将卡牌加入弃牌堆
                general.getInventory().setStack(i, ItemStack.EMPTY);
            }
        }
    }

    /** 判断物品是否是测试卡牌 */
    static boolean isCard(Item item) {
        return ModItems.ALL_CARDS.contains(item);
    }

    /* =========================================================
     * 发牌逻辑（兼容弃牌堆：牌堆为空时从弃牌堆重洗）
     * ========================================================= */

    /** 给玩家发指定数量的牌 */
    public static void giveCard(PlayerEntity player, int drawcnt) {
        if (drawcnt <= 0) return;

        List<Item> drawnCards = new ArrayList<>(drawcnt);

        // 先从牌堆获取所有需要的牌（新增：牌堆为空时从弃牌堆重洗）
        for (int i = 0; i < drawcnt; i++) {
            Item cardItem = cardStack.draw();
            if (cardItem == null) {
                // 牌堆为空，尝试从弃牌堆重洗
                int reshuffled = reshuffleFromDiscardPile();
                if (reshuffled == 0) { // 弃牌堆也为空
                    player.sendMessage(Text.of("牌堆与弃牌堆均为空，无法摸牌"), false);
                    return;
                }
                // 重洗后再次尝试抽牌
                cardItem = cardStack.draw();
                if (cardItem == null) {
                    player.sendMessage(Text.of("重洗后仍然无法获得牌"), false);
                    return;
                }
            }
            drawnCards.add(cardItem);
        }

        // 批量添加卡牌并发送合并消息
        List<String> cardNames = new ArrayList<>();
        boolean success = true;

        for (Item cardItem : drawnCards) {
            ItemStack newCardStack = new ItemStack(cardItem);
            if (!player.getInventory().insertStack(newCardStack)) {
                player.sendMessage(Text.of("你的背包满了，无法获得所有牌"), false);
                success = false;
                break;
            }
            if (cardItem instanceof Card card) {
                cardNames.add(Text.translatable("item.sgsmod.card." + card.getBaseId()).getString());
            }
        }

        // 发送合并消息，减少网络传输
        if (success && !cardNames.isEmpty()) {
            String message = "摸到：" + String.join("、", cardNames);
            player.sendMessage(Text.of(message), false);
        }

        // 更新手牌缓存
        if (player instanceof ServerPlayerEntity serverPlayer) {
            updateHandCardCache(serverPlayer);
        }
    }

    /** 给玩家发指定数量的牌 */
    public static void giveCardTohand(PlayerEntity player, int drawcnt,HandInventory hand) {
        if (drawcnt <= 0) return;

        List<Item> drawnCards = new ArrayList<>(drawcnt);

        // 先从牌堆获取所有需要的牌（新增：牌堆为空时从弃牌堆重洗）
        for (int i = 0; i < drawcnt; i++) {
            Item cardItem = cardStack.draw();
            if (cardItem == null) {
                // 牌堆为空，尝试从弃牌堆重洗
                int reshuffled = reshuffleFromDiscardPile();
                if (reshuffled == 0) { // 弃牌堆也为空
                    player.sendMessage(Text.of("牌堆与弃牌堆均为空，无法摸牌"), false);
                    return;
                }
                // 重洗后再次尝试抽牌
                cardItem = cardStack.draw();
                if (cardItem == null) {
                    player.sendMessage(Text.of("重洗后仍然无法获得牌"), false);
                    return;
                }
            }
            drawnCards.add(cardItem);
        }

        // 批量添加卡牌并发送合并消息
        List<String> cardNames = new ArrayList<>();
        boolean success = true;

        for (Item cardItem : drawnCards) {
            ItemStack newCardStack = new ItemStack(cardItem);
            if (!hand.addItem(newCardStack)) {
                player.sendMessage(Text.of("你的手牌区满了，无法获得所有牌"), false);
                success = false;
                break;
            }
            if (cardItem instanceof Card card) {
                cardNames.add(Text.translatable("item.sgsmod.card." + card.getBaseId()).getString());
            }
        }

        // 发送合并消息，减少网络传输
        if (success && !cardNames.isEmpty()) {
            String message = "摸到：" + String.join("、", cardNames);
            player.sendMessage(Text.of(message), false);
        }
    }


    /** 给武将发指定数量的牌（兼容弃牌堆） */
    public static void giveCard(GeneralEntity general, int drawcnt) {
        if (drawcnt <= 0) return;

        List<String> cardNames = new ArrayList<>();

        for (int i = 0; i < drawcnt; i++) {
            Item cardItem = cardStack.draw();
            if (cardItem == null) {
                // 牌堆为空，尝试从弃牌堆重洗
                int reshuffled = reshuffleFromDiscardPile();
                if (reshuffled == 0) {
                    general.say("牌堆与弃牌堆均为空，无法摸牌");
                    return;
                }
                // 重洗后再次尝试抽牌
                cardItem = cardStack.draw();
                if (cardItem == null) {
                    general.say("重洗后仍然无法获得牌");
                    return;
                }
            }

            ItemStack newCardStack = new ItemStack(cardItem);
            if (general.getInventory().addStack(newCardStack).isEmpty()) {
                if (cardItem instanceof Card card) {
                    cardNames.add(Text.translatable("item.sgsmod.card." + card.getBaseId()).getString());
                }
            }
        }

        // 发送合并消息
        if (!cardNames.isEmpty()) {
            String message = "摸到：" + String.join("、", cardNames);
            general.say(message);
        }
    }

    /** 获取卡牌的显示名称 */
    private static Text cardName(Item item) {
        if (item instanceof Card card) {
            return Text.of("摸到：" + Text.translatable("item.sgsmod.card." + card.getBaseId()).getString());
        }
        return Text.of("未知牌");
    }

    /** 停止所有调度任务并清理（新增：清空弃牌堆） */
    public static void clearAll() {
        // 停止调度器任务
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();  // 立即停止所有任务
            scheduler = Executors.newSingleThreadScheduledExecutor(); // 重新创建（后续可用）
        }

        // 清空所有游戏数据（含濒死状态缓存）
        turnOrder.clear();
        DYING_PLAYERS.clear();
        DrawCardMap.clear();
        HAND_CARD_SLOTS_CACHE.clear();
        cardsBesideHero.clear();
        ORIGINAL_FLYING.clear();
        ORIGINAL_SPEED.clear();

        // 重置牌堆与弃牌堆
        cardStack.reset("test");
        clearDiscardPile(); // 新增：清空弃牌堆

        LOGGER.info("所有游戏数据已清空重置（含牌堆与弃牌堆）");
    }


    /* =========================================================
     * 濒死状态管理（核心优化区）
     * ========================================================= */

    /** 进入濒死状态 */
    public static void enterDyingState(PlayerEntity player) {
        UUID playerId = player.getUuid();
        if (DYING_PLAYERS.containsKey(playerId)) return; // 避免重复进入

        DYING_PLAYERS.put(playerId, 0); // 初始化计时
        player.setHealth(1.0f);
        player.setVelocity(Vec3d.ZERO);
        player.setPitch(90.0f);

        // 缓存玩家原始状态（用于后续恢复）
        PlayerAbilities abilities = player.getAbilities();
        ORIGINAL_FLYING.putIfAbsent(playerId, abilities.flying);
        ORIGINAL_SPEED.putIfAbsent(playerId, player.getMovementSpeed());

        // 发送濒死消息
        Text message = Text.of(player.getName().getString() + " 已濒死，等待救援...");
        player.sendMessage(Text.of("你已濒死，等待救援..."), false);
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            for (ServerPlayerEntity otherPlayer : serverWorld.getPlayers()) {
                if (!otherPlayer.getUuid().equals(playerId)) {
                    otherPlayer.sendMessage(message, false);
                }
            }
        }
    }

    /** 退出濒死状态（恢复原始状态） */
    public static void exitDyingState(PlayerEntity player) {
        UUID playerId = player.getUuid();
        if (!DYING_PLAYERS.containsKey(playerId)) return;

        // 1. 恢复原始飞行状态
        PlayerAbilities abilities = player.getAbilities();
        if (ORIGINAL_FLYING.containsKey(playerId)) {
            boolean originalFlying = ORIGINAL_FLYING.get(playerId);
            abilities.flying = originalFlying;
            // 仅非创造模式且原始不飞行时，禁用飞行权限
            if (!originalFlying && !abilities.creativeMode) {
                abilities.allowFlying = false;
            }
            abilities.setFlySpeed(0.05f); // 恢复默认飞行速度
            ORIGINAL_FLYING.remove(playerId);
        }

        // 2. 恢复原始移动速度
        if (ORIGINAL_SPEED.containsKey(playerId)) {
            player.setMovementSpeed(ORIGINAL_SPEED.get(playerId));
            ORIGINAL_SPEED.remove(playerId);
        }

        // 3. 恢复物理与视觉状态
        player.setNoGravity(false);
        player.setBodyYaw(player.getBodyYaw() - 90.0f);
        player.setPose(EntityPose.STANDING);
        player.setVelocity(Vec3d.ZERO);

        // 4. 移除濒死标记
        DYING_PLAYERS.remove(playerId);
        player.sendMessage(Text.of("你被救活了！"), false);
    }

    /** 判断玩家是否濒死 */
    public static boolean isInDyingState(PlayerEntity player) {
        return DYING_PLAYERS.containsKey(player.getUuid());
    }

    /* =========================================================
     * 卡牌属性与体力管理（新增：使用卡牌后丢弃到弃牌堆）
     * ========================================================= */

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
     * 偷牌 / 拆牌逻辑（新增：拆牌时将卡牌加入弃牌堆）
     * ========================================================= */

    /** 获取一个玩家的牌 */
    public static void ObtainCard(LivingEntity e1, LivingEntity e2) {
        if (e1 instanceof ServerPlayerEntity player) {
            // 检查背包是否有空格
            if (player.getInventory().getEmptySlot() == -1) {
                player.sendMessage(Text.of("你的背包已满，无法获得卡牌！"), false);
                return;
            }

            // 检查目标是否为玩家
            if (e2 instanceof ServerPlayerEntity targetPlayer) {
                // 从缓存获取目标玩家的卡牌索引
                List<Integer> targetCardSlots = HAND_CARD_SLOTS_CACHE.getOrDefault(targetPlayer.getUuid(), Collections.emptyList());
                if (targetCardSlots.isEmpty()) {
                    player.sendMessage(Text.of("目标玩家没有卡牌！"), false);
                    return;
                }

                // 随机选择一张卡牌
                int randomIndex = RANDOM.nextInt(targetCardSlots.size());
                int selectedSlot = targetCardSlots.get(randomIndex);
                ItemStack stolenCard = targetPlayer.getInventory().getStack(selectedSlot);

                if (stolenCard.isEmpty() || !(stolenCard.getItem() instanceof Card)) {
                    // 缓存可能过时，更新缓存后重试
                    updateHandCardCache(targetPlayer);
                    player.sendMessage(Text.of("目标玩家没有卡牌！"), false);
                    return;
                }

                // 移除目标玩家的卡牌（不丢弃，而是转移给偷取者）
                targetPlayer.getInventory().removeStack(selectedSlot);

                // 将卡牌添加到偷取者的背包
                String name = stolenCard.getName().getString();
                boolean success = player.getInventory().insertStack(stolenCard);

                if (success) {
                    player.sendMessage(Text.of("你成功偷取了" + name + "！"), false);
                    targetPlayer.sendMessage(Text.of("你的卡牌被偷走了！"), false);
                    // 更新双方缓存
                    updateHandCardCache(targetPlayer);
                    updateHandCardCache(player);
                } else {
                    // 如果背包满了，将卡牌加入弃牌堆
                    discard(stolenCard); // 新增：背包满时丢弃到弃牌堆
                    player.sendMessage(Text.of("你的背包已满，偷取的卡牌已丢弃！"), false);
                    targetPlayer.sendMessage(Text.of("偷取者背包满，你的卡牌已被丢弃！"), false);
                    updateHandCardCache(targetPlayer);
                }
            } else if (e2 instanceof GeneralEntity targetGeneral) {
                List<Integer> targetCardSlots = getGeneralCardSlots(targetGeneral);
                if (targetCardSlots.isEmpty()) {
                    player.sendMessage(Text.of("目标武将没有卡牌！"), false);
                    return;
                }

                // 随机选择一张卡牌
                int randomIndex = RANDOM.nextInt(targetCardSlots.size());
                int selectedSlot = targetCardSlots.get(randomIndex);
                ItemStack stolenCard = targetGeneral.getInventory().getStack(selectedSlot);

                if (stolenCard.isEmpty() || !(stolenCard.getItem() instanceof Card)) {
                    player.sendMessage(Text.of("目标武将没有卡牌！"), false);
                    return;
                }

                targetGeneral.getInventory().removeStack(selectedSlot);
                // 将卡牌添加到偷取者的背包
                String name = stolenCard.getName().getString();
                boolean success = player.getInventory().insertStack(stolenCard);

                if (!success) {
                    discard(stolenCard); // 新增：背包满时丢弃到弃牌堆
                    player.sendMessage(Text.of("你的背包已满，偷取的卡牌已丢弃！"), false);
                } else {
                    player.sendMessage(Text.of("你成功偷取了" + name + "！"), false);
                    updateHandCardCache(player);
                }
            }
        } else if (e1 instanceof GeneralEntity general) {
            // 检查目标是否为玩家
            if (e2 instanceof ServerPlayerEntity targetPlayer) {
                // 从缓存获取目标玩家的卡牌索引
                List<Integer> targetCardSlots = HAND_CARD_SLOTS_CACHE.getOrDefault(targetPlayer.getUuid(), Collections.emptyList());
                if (targetCardSlots.isEmpty()) {
                    general.say("目标玩家没有卡牌！");
                    return;
                }

                // 随机选择一张卡牌
                int randomIndex = RANDOM.nextInt(targetCardSlots.size());
                int selectedSlot = targetCardSlots.get(randomIndex);
                ItemStack stolenCard = targetPlayer.getInventory().getStack(selectedSlot);

                if (stolenCard.isEmpty() || !(stolenCard.getItem() instanceof Card)) {
                    // 缓存可能过时，更新缓存后重试
                    updateHandCardCache(targetPlayer);
                    general.say("目标玩家没有卡牌！");
                    return;
                }

                // 移除目标玩家的卡牌（转移给武将）
                targetPlayer.getInventory().removeStack(selectedSlot);

                // 将卡牌添加到武将的背包
                String name = stolenCard.getName().getString();
                ItemStack remainder = general.getInventory().addStack(stolenCard);
                boolean success = remainder.isEmpty();

                if (success) {
                    general.say("成功偷取了" + name + "！");
                    targetPlayer.sendMessage(Text.of("你的卡牌被偷走了！"), false);
                    // 更新目标玩家缓存
                    updateHandCardCache(targetPlayer);
                } else {
                    discard(remainder); // 新增：背包满时丢弃剩余卡牌
                    general.say("背包已满，偷取的卡牌已丢弃！");
                    targetPlayer.sendMessage(Text.of("偷取者背包满，你的卡牌已被丢弃！"), false);
                    updateHandCardCache(targetPlayer);
                }
            } else if (e2 instanceof GeneralEntity targetGeneral) {
                // 获取目标武将的卡牌索引
                List<Integer> targetCardSlots = getGeneralCardSlots(targetGeneral);
                if (targetCardSlots.isEmpty()) {
                    general.say(targetGeneral.getName().getString() + "没有卡牌！");
                    return;
                }

                // 随机选择一张卡牌
                int randomIndex = RANDOM.nextInt(targetCardSlots.size());
                int selectedSlot = targetCardSlots.get(randomIndex);
                ItemStack stolenCard = targetGeneral.getInventory().getStack(selectedSlot);

                if (stolenCard.isEmpty() || !(stolenCard.getItem() instanceof Card)) {
                    general.say(targetGeneral.getName().getString() + "没有卡牌！");
                    return;
                }

                // 移除目标武将的卡牌（转移给偷取者）
                targetGeneral.getInventory().removeStack(selectedSlot);
                // 将卡牌添加到偷取者的背包
                String name = stolenCard.getName().getString();
                ItemStack remainder = general.getInventory().addStack(stolenCard);
                boolean success = remainder.isEmpty();

                if (!success) {
                    discard(remainder); // 新增：背包满时丢弃剩余卡牌
                    general.say("背包已满，偷取的卡牌已丢弃！");
                } else {
                    general.say("成功偷取了一张" + name + "！");
                }
            }
        }
    }

    /** 获取武将的卡牌槽位 */
    private static List<Integer> getGeneralCardSlots(GeneralEntity general) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < general.getInventory().size(); i++) {
            ItemStack stack = general.getInventory().getStack(i);
            if (stack.getItem() instanceof Card) {
                slots.add(i);
            }
        }
        return slots;
    }

    /** 当玩家库存变化时，更新手牌缓存 */
    public static void updateHandCardCache(ServerPlayerEntity player) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() instanceof Card) {
                slots.add(i);
            }
        }
        HAND_CARD_SLOTS_CACHE.put(player.getUuid(), slots);
    }

    public static boolean removeRandomCard(LivingEntity target, String area) {
        if (target instanceof ServerPlayerEntity player) {
            return removePlayerCard(player, area);
        } else if (target instanceof GeneralEntity general) {
            return removeGeneralCard(general, area);
        }
        return false;
    }

    /** 移除玩家的卡牌（新增：移除的卡牌加入弃牌堆） */
    private static boolean removePlayerCard(ServerPlayerEntity player, String area) {
        switch (area) {
            case "PD":
                // TODO: 添加处理判定区的逻辑
                break;

            case "SP": {
                // 从缓存获取手牌槽位
                List<Integer> cardSlots = HAND_CARD_SLOTS_CACHE.getOrDefault(player.getUuid(), Collections.emptyList());

                if (!cardSlots.isEmpty()) {
                    int randomIndex = RANDOM.nextInt(cardSlots.size());
                    int slot = cardSlots.get(randomIndex);
                    ItemStack removedCard = player.getInventory().removeStack(slot);

                    if (!removedCard.isEmpty()) {
                        discard(removedCard); // 新增：拆牌时将卡牌加入弃牌堆
                        player.sendMessage(Text.of("你的" + removedCard.getName().getString() + "被拆了（已加入弃牌堆）！"), false);
                        updateHandCardCache(player); // 更新缓存
                        return true;
                    }
                }
                break;
            }

            case "ZB": {
                // 随机移除装备（0-3盔甲，4副手，5主手）
                List<Integer> equipmentSlots = new ArrayList<>();
                for (int i = 0; i < 6; i++) {
                    ItemStack equipment = player.getInventory().getStack(i);
                    if (!equipment.isEmpty() && !(equipment.getItem() instanceof Card)) {
                        equipmentSlots.add(i);
                    }
                }

                if (!equipmentSlots.isEmpty()) {
                    int randomSlot = equipmentSlots.get(RANDOM.nextInt(equipmentSlots.size()));
                    ItemStack removedEquipment = player.getInventory().removeStack(randomSlot);
                    if (!removedEquipment.isEmpty()) {
                        // 装备非卡牌，无需加入弃牌堆
                        player.sendMessage(Text.of("你的" + removedEquipment.getName().getString() + "被移除了！"), false);
                        return true;
                    }
                } else {
                    player.sendMessage(Text.of("你没有装备任何物品！"), false);
                }
                break;
            }

            case "WP": {
                // 随机移除背包物品
                List<Integer> nonEmptySlots = new ArrayList<>();
                for (int i = 0; i < player.getInventory().size(); i++) {
                    if (!player.getInventory().getStack(i).isEmpty()) {
                        nonEmptySlots.add(i);
                    }
                }

                if (!nonEmptySlots.isEmpty()) {
                    int randomSlot = nonEmptySlots.get(RANDOM.nextInt(nonEmptySlots.size()));
                    ItemStack removed = player.getInventory().removeStack(randomSlot);
                    if (!removed.isEmpty()) {
                        if (removed.getItem() instanceof Card) {
                            discard(removed); // 新增：移除卡牌时加入弃牌堆
                            player.sendMessage(Text.of("你被拆了" + removed.getName().getString() + "（已加入弃牌堆）！"), false);
                            updateHandCardCache(player);
                        } else {
                            player.sendMessage(Text.of("你被拆了" + removed.getName().getString()), false);
                        }
                        return true;
                    }
                } else {
                    player.sendMessage(Text.of("你没有可拆的物品！"), false);
                }
                break;
            }
        }
        return false;
    }

    /** 移除武将的卡牌（新增：移除的卡牌加入弃牌堆） */
    private static boolean removeGeneralCard(GeneralEntity general, String area) {
        switch (area) {
            case "PD":
                // TODO: 添加处理判定区的逻辑
                break;

            case "SP": {
                // 获取武将的手牌槽位
                List<Integer> cardSlots = getGeneralCardSlots(general);

                if (!cardSlots.isEmpty()) {
                    int randomIndex = RANDOM.nextInt(cardSlots.size());
                    int slot = cardSlots.get(randomIndex);
                    ItemStack removedCard = general.getInventory().removeStack(slot);
                    if (!removedCard.isEmpty()) {
                        discard(removedCard); // 新增：拆牌时加入弃牌堆
                        general.say("我的" + removedCard.getName().getString() + "被拆了（已加入弃牌堆）！");
                        return true;
                    }
                }
                break;
            }

            case "ZB":
                // TODO: 如果需要拆 GeneralEntity 装备，可以仿照 SP 或 ZB 写法
                break;
        }
        return false;
    }


    /* =========================================================
     * 其他工具方法
     * ========================================================= */

    public static LivingEntity getNearestLivingEntity(PlayerEntity player, double maxDistance) {
        ServerWorld world = (ServerWorld) player.getWorld();

        // 获取玩家周围所有 LivingEntity
        List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class,
                player.getBoundingBox().expand(maxDistance),
                e -> e != player && e.isAlive()); // 排除自己，必须存活

        LivingEntity nearest = null;
        double minDistanceSq = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            double distanceSq = player.squaredDistanceTo(entity);
            if (distanceSq < minDistanceSq) {
                minDistanceSq = distanceSq;
                nearest = entity;
            }
        }

        return nearest;
    }

    public static void throwShaAt(PlayerEntity player, LivingEntity target) {
        World world = player.getWorld();
        ShaEntity sha = new ShaEntity(player, world);
        sha.setPosition(player.getX(), player.getEyeY(), player.getZ());

        // 处理壮誓状态效果（保持注释，不改变原有逻辑）
        // if (hasStatusEffect(ModEffects.ZHUANGSHI)) {
        //     StatusEffectInstance effect = getStatusEffect(ModEffects.ZHUANGSHI);
        //     int remaining = effect.getAmplifier() + 1;
        //     if (remaining > 0) {
        //         sha.setResponsible(false);
        //         addStatusEffect(new StatusEffectInstance(ModEffects.ZHUANGSHI,
        //                 effect.getDuration(),
        //                 remaining - 1,
        //                 false, true));
        //     }
        // }

        sha.setVelocity(target.getPos().subtract(player.getPos()).normalize().multiply(1.5));
        world.spawnEntity(sha);
        player.playSound(ModSoundEvents.SHA_ENTITY_THROW, 1.0F, 1.0F);
    }

    /**
     * 服务器Tick逻辑（核心优化：濒死玩家移动限制）
     * 完全避开ServerPlayerInteractionManager权限问题，用通用API锁死移动
     */
    public static void tick(ServerWorld world) {
        Iterator<Map.Entry<UUID, Integer>> iterator = DYING_PLAYERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            UUID uuid = entry.getKey();
            PlayerEntity player = world.getPlayerByUuid(uuid);

            if (player == null) {
                // 玩家离开，清理所有缓存
                iterator.remove();
                ORIGINAL_FLYING.remove(uuid);
                ORIGINAL_SPEED.remove(uuid);
                continue;
            }

            // ===============================
            // 1. 锁死移动能力（通用API，无版本依赖）
            // ===============================
            PlayerAbilities abilities = player.getAbilities();
            abilities.allowFlying = true;  // 允许飞行（防止被禁用）
            abilities.flying = true;       // 进入飞行模式（无法行走）
            abilities.setFlySpeed(0.0f);   // 飞行速度设为0，彻底锁死
            player.setMovementSpeed(0.0f); // 锁死移动速度

            // ===============================
            // 2. 物理锁定位置（抵消外力）
            // ===============================
            Vec3d lockedPos = player.getPos();
            player.refreshPositionAndAngles(
                    MathHelper.clamp(lockedPos.x, -29999872, 29999872), // 合法坐标限制
                    lockedPos.y,
                    MathHelper.clamp(lockedPos.z, -29999872, 29999872),
                    player.getYaw(),  // 保留视角旋转（可选）
                    90.0f             // 固定身体朝下（濒死姿态）
            );
            player.setVelocity(Vec3d.ZERO); // 清除惯性

            // ===============================
            // 3. 禁用动作状态
            // ===============================
            player.setSprinting(false);
            player.setSneaking(false);
            player.setOnGround(true); // 防止掉落动画

            // ===============================
            // 4. 强制濒死视觉姿态
            // ===============================
            if (player.getPose() != EntityPose.SLEEPING) {
                player.setNoGravity(true);
                player.setPitch(90.0f);
                player.setPose(EntityPose.SLEEPING);
            }

            // 更新濒死计时
            entry.setValue(entry.getValue() + 1);
        }
    }


    // 盖上一名玩家的x张物品（修正版）
    public static void cover(ServerPlayerEntity player, int x) {
        UUID uuid = player.getUuid();
        List<ItemStack> items = new ArrayList<>();
        List<Integer> originalSlots = new ArrayList<>(); // 记录物品原槽位索引（用于恢复）

        // 遍历所有槽位，收集非空物品
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.size() && items.size() < x; i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) { // 只处理非空物品
                items.add(stack.copyAndEmpty()); // 复制并清空原物品
                originalSlots.add(i); // 记录原槽位
            }
        }

        // 存储物品和原槽位
        cardsBesideHero.put(uuid, new CoveredItems(items, originalSlots));
        updateHandCardCache(player);
    }

    // 获得轮次Map
    public static void GetTurnOrder(Map<Integer, LivingEntity> turns) {
        turnOrder = turns;
    }

    public static Map<Integer, LivingEntity> getTurnOrder() {
        return turnOrder;
    }

    public static int getHandCapacity(PlayerEntity player) {
        if (ShaEntity.hasSkill(player, Skills.yinzi)) {
            float maxHealth = player.getMaxHealth();
            return maxHealth > 0 ? (int) (maxHealth / 4) : 1; // 避免除零，默认返回1
        } else {
            float health = player.getHealth();
            return health > 0 ? (int) (health / 4) : 1; // 避免除零，默认返回1
        }
    }

    // 辅助类：存储被盖的物品和原槽位
    static class CoveredItems {
        List<ItemStack> items;
        List<Integer> originalSlots;

        CoveredItems(List<ItemStack> items, List<Integer> originalSlots) {
            this.items = items;
            this.originalSlots = originalSlots;
        }
    }

    // 恢复一名玩家的x张物品（对应修正版）
    public static void restoreCardsBesideHero(PlayerEntity player, int x) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        UUID uuid = player.getUuid();
        CoveredItems coveredItems = cardsBesideHero.get(uuid);
        if (coveredItems == null || coveredItems.items.isEmpty()) return;

        Inventory inventory = player.getInventory();
        int restoreCount = Math.min(coveredItems.items.size(), x);

        // 恢复物品到原槽位
        for (int i = 0; i < restoreCount; i++) {
            int originalSlot = coveredItems.originalSlots.get(i);
            ItemStack stack = coveredItems.items.get(i);
            inventory.setStack(originalSlot, stack);
        }

        // 移除已恢复的物品和槽位记录
        if (x >= coveredItems.items.size()) {
            cardsBesideHero.remove(uuid);
        } else {
            List<ItemStack> remainingItems = coveredItems.items.subList(x, coveredItems.items.size());
            List<Integer> remainingSlots = coveredItems.originalSlots.subList(x, coveredItems.originalSlots.size());
            cardsBesideHero.put(uuid, new CoveredItems(remainingItems, remainingSlots));
        }

        updateHandCardCache(serverPlayer);
    }
    public static class ClientTurnManager {  // 改为 static
        private static final Map<Integer, LivingEntity> clientTurnOrder = new ConcurrentHashMap<>();

        // 更新客户端轮次数据（静态方法）
        public static void updateTurnOrder(Map<Integer, LivingEntity> newTurns) {
            clientTurnOrder.clear();
            clientTurnOrder.putAll(newTurns);
        }

        // 获取客户端轮次数据（静态方法）
        public static Map<Integer, LivingEntity> getClientTurnOrder() {
            return clientTurnOrder;
        }
    }
}