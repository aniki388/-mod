package com.chengcode.sgsmod.entity;

import com.chengcode.sgsmod.card.*;
import com.chengcode.sgsmod.effect.ModEffects;
import com.chengcode.sgsmod.entity.ai.GeneralTargetGoal;
import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.item.ModItems;
import com.chengcode.sgsmod.manager.WuXieStack;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import com.chengcode.sgsmod.sound.SkillSoundManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * 三国杀武将实体
 * 最终优化版：支持多玩家破军任务、修复称象消息、完善弃牌堆逻辑（所有弃牌场景均加入弃牌堆）
 */
public class GeneralEntity extends PathAwareEntity {
    // ===============================
    // 常量定义
    // ===============================
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralEntity.class);
    private static final long ZHUANGSHI_COOLDOWN = 300; // 壮誓冷却（15秒）
    private static final int SHA_COOLDOWN_TICKS = 45;   // 杀冷却（2.25秒）
    private static final long CARD_CACHE_UPDATE_INTERVAL = 5; // 手牌缓存更新间隔（5tick）
    private static final long MESSAGE_COOLDOWN_MS = 1000;    // 普通消息冷却（1秒）

    // ===============================
    // 核心字段
    // ===============================
    private int shaCooldown = 0;
    private boolean canResponse = true;

    // 技能开关
    private boolean isWushuangEnabled = false;
    private boolean isKuangguEnabled = false;
    private boolean isJieLiegongEnabled = false;
    private boolean isZhuangshiEnabled = false;
    private boolean isJiang = false;
    private boolean isHunZiEnabled = false;
    private boolean isYinZiEnabled = false;
    private boolean isPojunEnabled = false;
    private boolean isChengxiangEnabled = false;

    // 临时状态
    private boolean nextShaBonus = false;
    private boolean drinkJiu = false;

    // 背包与关系管理
    private final SimpleInventory inventory = new SimpleInventory(36);
    private final Set<UUID> rescuers = new HashSet<>();
    private final Set<UUID> enemies = new HashSet<>();

    // 技能计时与状态
    private long lastZhuangshiTime = 0;
    private boolean dying = false;
    private long dyingStartTime = -1;
    private static int DRAW_NUM = 2;

    // ===============================
    // 优化字段（新增/修改）
    // ===============================
    private final Map<String, Integer> handCardCache = new HashMap<>();
    private long lastCacheUpdateTick = 0;
    private long lastSayTime = 0;
    private String lastMessage = ""; // 消息去重：解决称象高频消息被拦截
    private static final DelayedTaskManager DELAYED_TASK_MANAGER = new DelayedTaskManager(); // 多任务兼容的管理器

    // ===============================
    // 构造 & 基础配置
    // ===============================
    public GeneralEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        initGoals();
    }

    @Override
    protected void initGoals() {
        // 移动与攻击目标
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(4, new LookAtEntityGoal(this, GeneralEntity.class, 8.0F));
        this.goalSelector.add(5, new LookAroundGoal(this));

        // 仇恨目标
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new GeneralTargetGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, HostileEntity.class, true));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 15.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2D);
    }

    @Override
    public EntityPose getPose() {
        return isDying() ? EntityPose.SLEEPING : super.getPose();
    }

    // ===============================
    // Getter / Setter
    // ===============================
    public boolean isWushuangEnabled() { return isWushuangEnabled; }
    public void setWushuangEnabled(boolean enabled) { this.isWushuangEnabled = enabled; }
    public boolean isKuangguEnabled() { return isKuangguEnabled; }
    public void setKuangguEnabled(boolean enabled) { this.isKuangguEnabled = enabled; }
    public boolean isJieLiegongEnabled() { return isJieLiegongEnabled; }
    public void setJieLiegongEnabled(boolean enabled) { this.isJieLiegongEnabled = enabled; }
    public boolean isZhuangshiEnabled() { return isZhuangshiEnabled; }
    public void setZhuangshiEnabled(boolean enabled) { this.isZhuangshiEnabled = enabled; }
    public boolean isJiangEnabled() { return isJiang; }
    public void setJiangEnabled(boolean enabled) { this.isJiang = enabled; }
    public boolean isHunZiEnabled() {return isHunZiEnabled;}
    public void setHunZiEnabled(boolean enabled) { this.isHunZiEnabled = enabled; }
    public boolean isYinZiEnabled() {return isYinZiEnabled;}
    public void setYinZiEnabled(boolean enabled) { this.isYinZiEnabled = enabled; }
    public boolean isPojunEnabled() {return isPojunEnabled;}
    public void setPojunEnabled(boolean enabled) { this.isPojunEnabled = enabled; }
    public boolean isChengxiangEnabled() {return isChengxiangEnabled;}
    public void setChengxiangEnabled(boolean enabled) { this.isChengxiangEnabled = enabled; }
    public boolean canResponse() { return canResponse; }
    public boolean isDying() { return dying; }
    public SimpleInventory getInventory() { return inventory; }
    public Set<UUID> getRescuers() { return rescuers; }
    public Set<UUID> getEnemies() { return enemies; }
    public int getDrawNum() { return DRAW_NUM; }
    public void setDrawNum(int num) { DRAW_NUM = num; }

    // ===============================
    // 生命周期：Tick与资源清理
    // ===============================
    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient) {
            handleCardUsage();
            shaCooldown = Math.max(0, shaCooldown - 1); // 避免负冷却
        }
    }

    /**
     * 实体被移除时清理资源（解决Chunk迭代空指针）
     */
    @Override
    public void onRemoved() {
        super.onRemoved();
        DELAYED_TASK_MANAGER.removeTasksForEntity(this.getUuid()); // 清理当前实体关联的任务
        enemies.clear();
        rescuers.clear();
        handCardCache.clear();
        LOGGER.info("[{}] 实体已移除，所有资源已清理", getName().getString());
    }

    // ===============================
    // 核心逻辑：出牌与技能
    // ===============================
    protected void handleCardUsage() {
        if (getWorld().isClient || !isAlive()) return;

        // 魂姿觉醒
        if (this.getHealth() <= this.getMaxHealth() / 4 && isHunZiEnabled()) {
            SkillSoundManager.playSkillSound("hunzi", this);
            getWorld().getPlayers().forEach(p -> {
                Text message = Text.of(
                        "§6§l" + this.getName().getString() +
                                " §f» §d§l已觉醒技能: " +
                                "§c§k!!§r§4§l魂§c§l姿§k!!"
                );
                sendMessageWithCooldown(p, message, MESSAGE_COOLDOWN_MS);
            });
            setHunZiEnabled(false);
            setYinZiEnabled(true);
        }

        // 濒死优先处理
        if (isDying()) {
            handleDyingState();
            return;
        }

        // 回复类卡牌（桃、酒）
        if (tryUseRecoveryCards()) {
            return;
        }

        // 攻击类卡牌（杀）
        if (tryUseAttackCards()) {
            return;
        }

        // 锦囊类卡牌
        tryUseTacticCards();
    }

    /**
     * 回复类卡牌逻辑（桃、酒）
     */
    private boolean tryUseRecoveryCards() {
        // 低血量用桃（使用后自动加入弃牌堆，通过consumeCard实现）
        if (hasCard("tao") && consumeCardIfLowHp("tao", 5.0F)) {
            CardGameManager.recoverHealth(this, 5.0F);
            return true;
        }

        // 用酒加伤（使用后自动加入弃牌堆，通过consumeCard实现）
        if (!drinkJiu && hasCard("jiu") && consumeCard("jiu")) {
            nextShaBonus = true;
            drinkJiu = true;
            playSound(ModSoundEvents.JIU_DRINK, 1.0f, 1.0f);
            say("使用『酒』！");
            return true;
        }

        return false;
    }

    /**
     * 攻击类卡牌逻辑（杀+破军）- 核心优化：多玩家破军任务
     */
    private boolean tryUseAttackCards() {
        LivingEntity target = getTarget();
        if (target == null || squaredDistanceTo(target) > 49) {
            return false;
        }

        if (hasCard("sha") && shaCooldown <= 0) {
            ItemStack shaStack = findCardInInventory("sha");
            // 安全校验：过滤空栈/空气/类型错误
            if (isInvalidCardStack(shaStack, ShaCardItem.class, "杀牌")) {
                handCardCache.remove("sha");
                return false;
            }

            ShaCardItem sha = (ShaCardItem) shaStack.getItem();
            activateZhuangshi();
            boolean isRedColor = sha.getColor() == ShaCardItem.RED;
            boolean usedSha = false;

            // 壮誓状态：直接出杀（使用后自动加入弃牌堆）
            if (hasStatusEffect(ModEffects.ZHUANGSHI)) {
                usedSha = tryUseSha(sha);
                if (usedSha) throwShaAt(target, isRedColor);
            }
            // 普通状态：出杀后处理破军（使用后自动加入弃牌堆）
            else {
                usedSha = tryUseSha(sha);
                if (usedSha) {
                    // 破军技能：多玩家任务兼容
                    if (isPojunEnabled() && target instanceof ServerPlayerEntity p) {
                        say("发动技能【破军】");
                        SkillSoundManager.playSkillSound("pojun", this);
                        int inventorySize = p.getInventory().size();
                        CardGameManager.cover(p, inventorySize);

                        // 新增破军任务：绑定玩家UUID，支持多玩家独立执行
                        DELAYED_TASK_MANAGER.addPojunTask(
                                60,                          // 延迟3秒（60tick）
                                p.getUuid(),                 // 目标玩家唯一标识
                                this.getUuid(),              // 发起者（当前武将）UUID
                                () -> {                      // 任务逻辑：恢复卡牌
                                    CardGameManager.restoreCardsBesideHero(p, inventorySize);
                                    p.sendMessage(Text.of("【破军】效果结束，手牌已恢复！"), false);
                                    LOGGER.debug("破军任务完成：玩家={}，发起者={}",
                                            p.getName().getString(), getName().getString());
                                }
                        );
                    }
                    throwShaAt(target, isRedColor);
                    shaCooldown = SHA_COOLDOWN_TICKS;
                }
            }

            // 激昂：红杀触发摸牌
            if (usedSha && isRedColor && isJiangEnabled()) {
                say("发动技能【激昂】");
                CardGameManager.giveCard(this, 1);
                SkillSoundManager.playSkillSound("jiang", this);
            }
            return usedSha;
        }
        return false;
    }

    /**
     * 锦囊类卡牌逻辑 - 安全校验统一化（使用后自动加入弃牌堆）
     */
    private void tryUseTacticCards() {
        // 1. 无中生有
        if (hasCard("wuzhong") && tryUseTacticCard("wuzhong", WuZhongItem.class, "无中生有",
                ModSoundEvents.WUZHONG, () -> new WuZhongEntity(ModEntities.WUZHONG_ENTITY, getWorld()))) {
            return;
        }

        // 2. 顺手牵羊
        if (hasCard("shunshou") && tryUseTacticCard("shunshou", ShunshouItem.class, "顺手牵羊",
                ModSoundEvents.SHUNSHOU, () -> new ShunshouEntity(ModEntities.SHUNSHOU_ENTITY, getWorld()))) {
            return;
        }

        // 3. 过河拆桥
        if (hasCard("chaiqiao") && tryUseTacticCard("chaiqiao", ChaiqiaoItem.class, "过河拆桥",
                ModSoundEvents.CHAIQIAO, () -> new ChaiqiaoEntity(ModEntities.CHAIQIAO_ENTITY, getWorld()))) {
            return;
        }

        // 4. 无懈可击（使用后自动加入弃牌堆）
        if (hasCard("wuxie")) {
            ItemStack wuxieStack = findCardInInventory("wuxie");
            if (!isInvalidCardStack(wuxieStack, WuXieItem.class, "无懈可击")) {
                respondTacticCard((WuXieItem) wuxieStack.getItem(), ModSoundEvents.WUXIE, "使用『无懈可击』！");
            }
            return;
        }

        // 5. 南蛮入侵
        if (hasCard("nanman") && tryUseTacticCard("nanman", NanmanCardItem.class, "南蛮入侵",
                ModSoundEvents.NANMAN, () -> new NanmanCardEntity(ModEntities.NANMAN_CARD_ENTITY, getWorld()))) {
            return;
        }

        // 6. 万箭齐发
        if (hasCard("wanjian") && tryUseTacticCard("wanjian", WanJianItem.class, "万箭齐发",
                ModSoundEvents.WANJIAN, () -> new WanJianEntity(ModEntities.WANJIAN, getWorld()))) {
            return;
        }

        // 7. 桃园结义
        if (hasCard("taoyuan") && tryUseTacticCard("taoyuan", TaoYuanItem.class, "桃园结义",
                ModSoundEvents.TAOYUAN, () -> new TaoYuanEntity(ModEntities.TAOYUAN_CARD_ENTITY, getWorld()))) {
            return;
        }
    }

    /**
     * 通用锦囊牌使用方法（减少重复代码）- 使用后自动加入弃牌堆
     */
    private boolean tryUseTacticCard(String baseId, Class<? extends Card> cardClass, String cardName,
                                     SoundEvent sound, Supplier<TacticCardEntity> entitySupplier) {
        ItemStack stack = findCardInInventory(baseId);
        if (isInvalidCardStack(stack, cardClass, cardName)) {
            handCardCache.remove(baseId);
            return false;
        }

        // 消耗卡牌（自动加入弃牌堆）
        if (consumeCard(baseId)) {
            String sayText = "使用『" + cardName + "』！";
            say(sayText);
            getWorld().playSound(null, getX(), getY(), getZ(), sound, SoundCategory.PLAYERS, 1.0f, 1.0f);

            TacticCardEntity entity = entitySupplier.get();
            entity.setOwner(this);
            entity.setResponse(canResponse);
            entity.setPosition(getX(), getY() + 1.0, getZ());
            entity.setVelocity(0, 0.05, 0);
            String cardId = getUuid() + "_" + baseId.toUpperCase();
            entity.setCardId(cardId);
            getWorld().spawnEntity(entity);

            WuXieStack.addWuXieStack(cardId, 0);
            WuXieItem.taskQueue.offer(TacticCardEntity.scheduleTacticCardEffect(entity));
            if (WuXieItem.taskQueue.size() == 1) {
                WuXieItem.processNextTask();
            }
            return true;
        }
        return false;
    }

    /**
     * 响应型锦囊（无懈可击）- 使用后自动加入弃牌堆
     */
    private void respondTacticCard(WuXieItem wuxie, SoundEvent soundEvent, String sayText) {
        PriorityQueue<WuXieItem.NearbyCard> nearbyCards =
                new PriorityQueue<>(Comparator.comparingDouble(a -> a.distance));

        // 查找16格内的锦囊实体
        List<Entity> nearbyEntities = getWorld().getEntitiesByClass(Entity.class,
                getBoundingBox().expand(16.0), entity -> entity instanceof TacticCardEntity);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof WuZhongEntity wuZhongEntity) {
                nearbyCards.offer(new WuXieItem.NearbyCard(wuZhongEntity, squaredDistanceTo(entity)));
            }
        }

        WuXieItem.NearbyCard nearestCard = nearbyCards.poll();
        if (nearestCard != null) {
            String targetCardId = nearestCard.card.getCardId();
            boolean isSelf = targetCardId.startsWith(getUuid().toString());

            // 判断是否需要响应（无懈可击奇偶逻辑）
            if ((isSelf && WuXieStack.getWuxieCnt(targetCardId) % 2 == 1) ||
                    (!isSelf && WuXieStack.getWuxieCnt(targetCardId) % 2 == 0)) {
                WuXieStack.plusWuxieCnt(targetCardId);
                say(sayText);
                consumeCard(wuxie.getBaseId()); // 消耗无懈可击，自动加入弃牌堆
                getWorld().playSound(null, getX(), getY(), getZ(), soundEvent, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        }
    }

    // ===============================
    // 状态处理：濒死与壮誓（壮誓弃牌加入弃牌堆）
    // ===============================
    private void enterDyingState() {
        if (dying) return;
        dying = true;
        dyingStartTime = age;
        setHealth(1.0F);
        speed = 0.0F;
        say("进入濒死状态！快来救我！");
    }

    public void reviveFromDying() {
        if (!dying) return;
        dying = false;
        speed = 2.0F;
        say("脱离濒死状态！");
    }

    private void handleDyingState() {
        if (age - dyingStartTime > 400) { // 20秒内未获救则死亡
            say(getName().getString() + "死亡");
            kill();
            return;
        }
        // 自救（桃/酒，使用后自动加入弃牌堆）
        if (consumeCard("tao") || consumeCard("jiu")) {
            heal(5.0F);
            say("抢救成功！");
            if (getHealth() > 0) {
                dying = false;
                reviveFromDying();
            }
        }
    }

    /**
     * 壮誓技能：失血换状态 - 核心修复：弃置非杀牌时加入弃牌堆
     */
    protected void activateZhuangshi() {
        if (!checkZhuangshiConditions()) return;

        SkillSoundManager.playSkillSound("zhuangshi", this);
        float loseHealth = (float) (getMaxHealth() / 4.0f);
        CardGameManager.loseHealth(this, loseHealth);
        say("发动『壮誓』 失去体力：" + String.format("%.1f", loseHealth));

        // 摸牌（按当前血量计算）
        int drawCount = Math.max(1, (int) (getHealth() / 20.0f));
        CardGameManager.giveCard(this, drawCount);

        // 弃置非杀牌 - 新增：弃牌时加入弃牌堆
        int discardCnt = 0;
        StringBuilder discardLog = new StringBuilder();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!(stack.getItem() instanceof ShaCardItem) && !stack.isEmpty()) {
                // 关键修复：将弃置的卡牌加入弃牌堆（复制栈避免原栈清空影响）
                CardGameManager.discard(stack.copy());
                // 记录弃牌日志
                discardLog.append(stack.getName().getString()).append(":").append(stack.getCount()).append("张，");
                discardCnt += stack.getCount();
                // 清空原栈（弃牌完成）
                stack.decrement(stack.getCount());
                // 同步更新手牌缓存（移除已弃置的卡牌）
                if (stack.getItem() instanceof Card card) {
                    handCardCache.remove(card.getBaseId());
                }
            }
        }

        // 发送弃牌日志（去重冷却）
        if (discardCnt > 0) {
            if (discardLog.length() > 1) {
                discardLog.deleteCharAt(discardLog.length() - 1);
            }
            say("发动『壮誓』 丢弃牌：" + discardLog);

            // 添加壮誓状态（强度=弃牌数）
            addStatusEffect(new StatusEffectInstance(ModEffects.ZHUANGSHI,
                    20 * 10, discardCnt, false, true));
        }

        lastZhuangshiTime = age;
    }

    private boolean checkZhuangshiConditions() {
        return isZhuangshiEnabled() && !isDying() &&
                getHealth() > getMaxHealth() / 4.0f &&
                age - lastZhuangshiTime > ZHUANGSHI_COOLDOWN;
    }

    // ===============================
    // 工具方法：缓存、消息、任务（consumeCard完善弃牌逻辑）
    // ===============================
    /**
     * 卡牌栈有效性校验（统一逻辑）
     */
    private boolean isInvalidCardStack(ItemStack stack, Class<? extends Card> cardClass, String cardName) {
        if (stack.isEmpty()) {
            LOGGER.error("[{}] 空栈无法作为{}", getName().getString(), cardName);
            return true;
        }
        if (stack.getItem() instanceof AirBlockItem) {
            LOGGER.error("[{}] 空气物品无法作为{}", getName().getString(), cardName);
            return true;
        }
        if (!cardClass.isInstance(stack.getItem())) {
            LOGGER.error("[{}] 类型错误：{} 不是{}",
                    getName().getString(), stack.getItem().getClass().getSimpleName(), cardName);
            return true;
        }
        return false;
    }

    /**
     * 手牌缓存更新（减少库存遍历）
     */
    private void updateHandCardCache() {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;
        long currentTick = serverWorld.getTime();
        if (currentTick - lastCacheUpdateTick < CARD_CACHE_UPDATE_INTERVAL) {
            return;
        }

        lastCacheUpdateTick = currentTick;
        handCardCache.clear();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() instanceof Card card) {
                String baseId = card.getBaseId();
                if (!handCardCache.containsKey(baseId)) {
                    handCardCache.put(baseId, i);
                }
            }
        }
    }

    public ItemStack findCardInInventory(String baseId) {
        updateHandCardCache();
        Integer slot = handCardCache.get(baseId);
        if (slot == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = inventory.getStack(slot);
        if (stack.getItem() instanceof AirBlockItem) {
            handCardCache.remove(baseId);
            return ItemStack.EMPTY;
        }
        return stack;
    }

    protected boolean hasCard(String baseId) {
        updateHandCardCache();
        return handCardCache.containsKey(baseId);
    }

    /**
     * 消耗卡牌（核心完善：消耗时自动将卡牌加入弃牌堆）
     * @param baseId 卡牌baseId
     * @return 消耗成功返回true
     */
    protected boolean consumeCard(String baseId) {
        updateHandCardCache();
        Integer slot = handCardCache.get(baseId);
        if (slot == null) {
            return false;
        }

        ItemStack stack = inventory.getStack(slot);
        // 校验卡牌有效性（空栈/非目标卡牌）
        if (stack.isEmpty() || !(stack.getItem() instanceof Card card) || !card.getBaseId().equals(baseId)) {
            handCardCache.remove(baseId);
            return false;
        }

        // 关键步骤1：将消耗的卡牌加入弃牌堆（复制栈，确保弃牌堆记录完整卡牌信息）
        boolean discardSuccess = CardGameManager.discard(stack.copy());
        if (!discardSuccess) {
            LOGGER.warn("[{}] 卡牌消耗失败：无法加入弃牌堆，卡牌={}", getName().getString(), baseId);
            return false;
        }

        stack.decrement(1);

        // 关键步骤3：若卡牌消耗为空，清空槽位并更新缓存
        if (stack.isEmpty()) {
            inventory.setStack(slot, ItemStack.EMPTY);
            handCardCache.remove(baseId); // 缓存同步移除
        }

        LOGGER.debug("[{}] 卡牌消耗成功：{}（已加入弃牌堆）", getName().getString(), baseId);
        return true;
    }

    /**
     * 消息发送：支持去重冷却（解决称象高频消息）
     */
    public void say(String msg) {
        long now = System.currentTimeMillis();
        // 仅拦截相同消息的高频发送，不同消息允许连续发送（如称象展示多张牌）
        if (now - lastSayTime < MESSAGE_COOLDOWN_MS && lastMessage.equals(msg)) {
            return;
        }
        lastSayTime = now;
        lastMessage = msg;

        // 发送消息前检查玩家列表是否为空
        List<? extends PlayerEntity> players = getWorld().getPlayers();
        if (players.isEmpty()) {
            LOGGER.warn("[{}] 无在线玩家，消息发送失败：{}", getName().getString(), msg);
            return;
        }

        players.forEach(p -> p.sendMessage(Text.of(getName().getString() + "：" + msg), false));
    }

    /**
     * 强制发送消息（无冷却，用于重要通知）
     */
    private void forceSay(String msg) {
        List<? extends PlayerEntity> players = getWorld().getPlayers();
        if (players.isEmpty()) {
            LOGGER.warn("[{}] 无在线玩家，强制消息发送失败：{}", getName().getString(), msg);
            return;
        }
        players.forEach(p -> p.sendMessage(Text.of(getName().getString() + "：" + msg), false));
    }

    private void sendMessageWithCooldown(PlayerEntity player, Text message, long cooldownMs) {
        long now = System.currentTimeMillis();
        if (now - lastSayTime < cooldownMs) {
            return;
        }
        lastSayTime = now;
        player.sendMessage(message, false);
    }

    private boolean consumeCardIfLowHp(String baseId, float healAmount) {
        return getHealth() < getMaxHealth() - healAmount - 5.0F && consumeCard(baseId);
    }

    public boolean tryUseSha(ShaCardItem sha) {
        return consumeCard(sha.getBaseId());
    }

    public boolean tryUseShan() {
        return hasCard("shan") && consumeCard("shan");
    }

    /**
     * 投掷杀实体
     */
    private void throwShaAt(LivingEntity target, boolean isRedColor) {
        ShaEntity sha = new ShaEntity(this, getWorld());
        sha.setPosition(getX(), getEyeY(), getZ());
        sha.setRedColor(isRedColor);
        float bonus = nextShaBonus ? 5.0F : 0.0F;
        nextShaBonus = false;
        drinkJiu = false;

        // 壮誓状态：杀无响应
        if (hasStatusEffect(ModEffects.ZHUANGSHI)) {
            StatusEffectInstance effect = getStatusEffect(ModEffects.ZHUANGSHI);
            int remaining = effect.getAmplifier() + 1;
            if (remaining > 0) {
                sha.setResponsible(false);
                addStatusEffect(new StatusEffectInstance(ModEffects.ZHUANGSHI,
                        effect.getDuration(), remaining - 1, false, true));
            }
        }

        // 破军伤害加成
        if (isPojunEnabled() && target instanceof ServerPlayerEntity player) {
            int attackerCards = ShaEntity.countCards(this);
            int targetCards = ShaEntity.countCards(target);
            if (targetCards <= attackerCards) {
                bonus += 5;
                SkillSoundManager.playSkillSound("pojun", player);
            }
        }

        sha.setBonusDamage(bonus);
        sha.setVelocity(target.getPos().subtract(getPos()).normalize().multiply(1.5));
        getWorld().spawnEntity(sha);
        playSound(ModSoundEvents.SHA_ENTITY_THROW, 1.0F, 1.0F);
    }

    private boolean shouldPrepareAttack() {
        if (!(getTarget() instanceof PlayerEntity player)) return false;
        return player.isAlive() && !player.isCreative() && enemies.contains(player.getUuid());
    }

    // ===============================
    // 事件方法：伤害与交互
    // ===============================
    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof PlayerEntity player) {
            enemies.add(player.getUuid());
            setTarget(player);
            boolean isShaDamage = source.getSource() instanceof ShaEntity;

            // 用闪抵消非杀伤害（使用闪后自动加入弃牌堆）
            if (amount > 1.5f && !isShaDamage && tryUseShan()) {
                getWorld().playSound(null, getBlockPos(), ModSoundEvents.SHAN, SoundCategory.PLAYERS, 1.0f, 1.0f);
                return false;
            }

            // 称象技能：修复消息发送（用普通say，支持不同牌名连续发送）
            if (isChengxiangEnabled() && !getWorld().isClient) {
                List<Card> cards = CardGameManager.getCardStack().getTop(4);
                say("发动【称象】展示牌：");
                for (Card card : cards) {
                    // 不同卡牌消息不同，不会被去重冷却拦截
                    say(card.getName().getString() + ":点数" + card.getNumber());
                }

                // 排序拿牌（点数和≤13）
                cards.sort(Comparator.comparingInt(Card::getNumber));
                int totalPoint = 0;
                StringBuilder getCardsLog = new StringBuilder("获得牌：");
                for (Card card : cards) {
                    if (totalPoint + card.getNumber() <= 13) {
                        totalPoint += card.getNumber();
                        getCardsLog.append(card.getName().getString()).append(" ");
                        // 添加到背包并更新缓存
                        ItemStack cardStack = card.getDefaultStack();
                        if (inventory.addStack(cardStack).isEmpty()) {
                            handCardCache.put(card.getBaseId(), findEmptySlot());
                        }
                    }
                }
                say(getCardsLog.toString().trim());
                SkillSoundManager.playSkillSound("chengxiang", this);
            }
        }

        // 触发濒死
        if (getHealth() - amount <= 0 && !isDying()) {
            enterDyingState();
            return false;
        }

        return super.damage(source, amount);
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (dying) {
            setVelocity(Vec3d.ZERO); // 濒死禁止移动
            return;
        }
        super.travel(movementInput);
    }

    private int findEmptySlot() {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).isEmpty()) {
                return i;
            }
        }
        return -1; // 无空槽位
    }

    public boolean isAwakend() {
        return false;
    }

    /**
     * 玩家用桃救援（玩家消耗桃时，需确保玩家端也将桃加入弃牌堆）
     * 注：玩家端弃牌逻辑需在PlayerEntity交互代码中补充，此处仅处理武将端
     */
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack item = player.getStackInHand(hand);
        if (dying && item.getItem() == ModItems.TAO) {
            // 玩家消耗桃：此处需通知玩家端将桃加入弃牌堆（建议通过Packet或事件实现）
            item.decrement(1);
            // 武将端回复血量
            heal(5.0F);
            say("抢救成功！感谢" + player.getName().getString() + "！");
            if (getHealth() > 0) {
                dying = false;
                reviveFromDying();
            }
            rescuers.add(player.getUuid());
            player.sendMessage(Text.of("义释" + getName().getString()), false);
            setTarget(null);
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    // ===============================
    // 核心优化：多任务兼容的延迟任务管理器
    // ===============================
    private static class DelayedTaskManager {
        // 线程安全队列：支持高并发任务添加/移除
        private final ConcurrentLinkedQueue<PlayerTask> taskQueue = new ConcurrentLinkedQueue<>();

        // 静态注册：全服仅1个Tick监听器
        DelayedTaskManager() {
            ServerTickEvents.START_SERVER_TICK.register(this::processTasks);
        }

        /**
         * 新增破军任务（支持多玩家，自动去重）
         * @param delayTicks 延迟时间（tick）
         * @param targetPlayerUuid 目标玩家UUID
         * @param initiatorUuid 发起者（武将）UUID
         * @param action 任务逻辑
         */
        public void addPojunTask(int delayTicks, UUID targetPlayerUuid, UUID initiatorUuid, Runnable action) {
            // 去重：同一玩家同一时间仅1个破军任务
            taskQueue.removeIf(task ->
                    task.taskType.equals("POJUN") && task.targetPlayerUuid.equals(targetPlayerUuid)
            );

            PlayerTask newTask = new PlayerTask(
                    delayTicks, action, targetPlayerUuid, initiatorUuid, "POJUN"
            );
            taskQueue.offer(newTask);
            LOGGER.debug("新增破军任务：目标玩家={}，发起者={}，延迟={}tick",
                    targetPlayerUuid, initiatorUuid, delayTicks);
        }

        /**
         * 移除指定实体关联的所有任务（实体删除时调用）
         */
        public void removeTasksForEntity(UUID entityUuid) {
            taskQueue.removeIf(task ->
                    task.initiatorUuid.equals(entityUuid) || task.targetPlayerUuid.equals(entityUuid)
            );
            LOGGER.debug("清理实体关联任务：实体UUID={}", entityUuid);
        }

        /**
         * Tick时处理任务（线程安全）
         */
        private void processTasks(MinecraftServer server) {
            if (taskQueue.isEmpty()) return;

            Iterator<PlayerTask> iterator = taskQueue.iterator();
            while (iterator.hasNext()) {
                PlayerTask task = iterator.next();

                // 倒计时未结束：跳过
                if (task.ticksRemaining-- > 0) {
                    continue;
                }

                // 执行任务：捕获异常，避免单个任务崩溃全局
                try {
                    // 校验目标玩家是否在线
                    if (isPlayerOnline(server, task.targetPlayerUuid)) {
                        task.action.run();
                    } else {
                        LOGGER.warn("跳过任务：目标玩家离线，类型={}，玩家UUID={}",
                                task.taskType, task.targetPlayerUuid);
                    }
                } catch (Exception e) {
                    LOGGER.error("任务执行失败：类型={}，目标玩家={}，发起者={}",
                            task.taskType, task.targetPlayerUuid, task.initiatorUuid, e);
                }

                // 任务完成：移除队列
                iterator.remove();
            }
        }

        /**
         * 校验玩家是否在线
         */
        private boolean isPlayerOnline(MinecraftServer server, UUID playerUuid) {
            return server.getPlayerManager().getPlayer(playerUuid) != null;
        }

        /**
         * 任务封装类：绑定玩家UUID与任务类型
         */
        private static class PlayerTask {
            int ticksRemaining;
            Runnable action;
            UUID targetPlayerUuid; // 目标玩家（如破军的被攻击者）
            UUID initiatorUuid;    // 发起者（如破军的武将）
            String taskType;       // 任务类型（POJUN/LEBUSISHU等）

            PlayerTask(int ticksRemaining, Runnable action, UUID targetPlayerUuid,
                       UUID initiatorUuid, String taskType) {
                this.ticksRemaining = ticksRemaining;
                this.action = action;
                this.targetPlayerUuid = targetPlayerUuid;
                this.initiatorUuid = initiatorUuid;
                this.taskType = taskType;
            }
        }
    }
}