package com.chengcode.sgsmod.entity;

import com.chengcode.sgsmod.card.WuXieItem;
import com.chengcode.sgsmod.effect.ModEffects;
import com.chengcode.sgsmod.entity.ai.GeneralTargetGoal;
import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.item.ModItems;
import com.chengcode.sgsmod.manager.WuXieStack;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import com.chengcode.sgsmod.sound.SkillSoundManager;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Supplier;

/**
 * 三国杀武将实体
 * <p>
 * 该实体类用于实现三国杀游戏中的武将，具备：
 * <ul>
 *     <li>自动出牌功能</li>
 *     <li>战术牌生成与响应</li>
 *     <li>技能与 AI 逻辑</li>
 *     <li>濒死状态处理</li>
 * </ul>
 * 该类继承 {@link net.minecraft.entity.mob.PathAwareEntity}，
 * 并集成了 Minecraft 实体的路径规划与 AI 行为。
 * </p>
 *
 * <h2>主要功能</h2>
 * <ol>
 *     <li>{@code spawnTacticCard}：通用的生成型战术牌方法</li>
 *     <li>{@code respondTacticCard}：通用的响应型战术牌方法</li>
 *     <li>卡牌消耗与任务队列调度</li>
 * </ol>
 *
 * <h2>使用场景</h2>
 * <p>
 * 在游戏运行中，GeneralEntity 会根据手牌和局势自动调用生成或响应战术牌的方法，
 * 实现类似三国杀中 AI 武将的出牌和防御逻辑。
 * </p>
 *
 * <h2>注意事项</h2>
 * <ul>
 *     <li>新增战术牌时应优先使用封装好的方法调用，避免重复代码</li>
 *     <li>任务队列 {@code WuXieItem.taskQueue} 需保持线程安全</li>
 * </ul>
 *
 * @author 方溯_source
 * @version 1.0
 * @since 2025-08-10
 */
public class GeneralEntity extends PathAwareEntity {

    // ===============================
    // 常量定义
    // ===============================

    /** 壮誓技能冷却（单位：tick，300 tick = 15 秒） */
    private static final long ZHUANGSHI_COOLDOWN = 300;

    // ===============================
    // 字段
    // ===============================

    /** 可否响应锦囊牌（无懈可击等） */
    private boolean canResponse = true;

    /** 技能开关 */
    private boolean isWushuangEnabled = false;
    private boolean isKuangguEnabled = false;
    private boolean isJieLiegongEnabled = false;
    private boolean isZhuangshiEnabled = false;

    /** 临时状态 */
    private boolean nextShaBonus = false;
    private boolean drinkJiu = false;

    /** 玩家背包 */
    private final SimpleInventory inventory = new SimpleInventory(36);

    /** 已救过本武将的玩家 UUID（不攻击） */
    private final Set<UUID> rescuers = new HashSet<>();

    /** 敌人 UUID */
    private final Set<UUID> enemies = new HashSet<>();

    /** 壮誓技能上次发动时间 */
    private long lastZhuangshiTime = 0;

    /** 濒死状态标志 */
    private boolean dying = false;
    private long dyingStartTime = -1;

    // ===============================
    // 构造 & 属性
    // ===============================

    public GeneralEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        initGoals();
    }
    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0D, true));

        this.goalSelector.add(2, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(4, new LookAtEntityGoal(this, GeneralEntity.class, 8.0F));
        this.goalSelector.add(5, new LookAroundGoal(this));


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

    // Getter / Setter
    public boolean isWushuangEnabled() { return isWushuangEnabled; }
    public void setWushuangEnabled(boolean enabled) { this.isWushuangEnabled = enabled; }

    public boolean isKuangguEnabled() { return isKuangguEnabled; }
    public void setKuangguEnabled(boolean enabled) { this.isKuangguEnabled = enabled; }

    public boolean isJieLiegongEnabled() { return isJieLiegongEnabled; }
    public void setJieLiegongEnabled(boolean enabled) { this.isJieLiegongEnabled = enabled; }

    public boolean isZhuangshiEnabled() { return isZhuangshiEnabled; }
    public void setZhuangshiEnabled(boolean enabled) { this.isZhuangshiEnabled = enabled; }

    public boolean canResponse() { return canResponse; }

    public boolean isDying() { return dying; }

    public SimpleInventory getInventory() { return inventory; }

    public Set<UUID> getRescuers() { return rescuers; }

    public Set<UUID> getEnemies() { return enemies; }

    // ===============================
    // 生命周期 & AI
    // ===============================
    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient) {;
            handleCardUsage();
        }

    }


    // ===============================
    // 出牌 & 技能逻辑
    // ===============================

    /**
     * AI 出牌逻辑
     */
    protected void handleCardUsage() {
        if (getWorld().isClient || !isAlive()) return;

        // 桃（低血量自动使用）
        if (consumeCardIfLowHp(ModItems.TAO, 5.0F)) return;

        // 杀 + 酒
        boolean hasSha = hasCard(ModItems.SHA, ModItems.LEISHA, ModItems.HUOSHA);
        if (hasSha && shouldPrepareAttack()) {
            if (!drinkJiu && consumeCard(ModItems.JIU)) {
                nextShaBonus = true;
                drinkJiu = true;
                playSound(ModSoundEvents.JIU_DRINK, 1.0f, 1.0f);
                say("使用『酒』！");
            }
            if (getTarget() instanceof PlayerEntity target && squaredDistanceTo(target) <= 49) {
                activateZhuangshi();
                if (tryUseSha()) throwShaAt(target);
            }
        }

        // 无中生有
        spawnTacticCard(ModItems.WUZHONG, ModSoundEvents.WUZHONG,
                () -> new WuZhongEntity(ModEntities.WUZHONG_ENTITY, this.getWorld()),
                "GENERAL_WUZHONG", canResponse, "『无中生有』！");

        // 顺手牵羊
        spawnTacticCard(ModItems.SHUNSHOU, ModSoundEvents.SHUNSHOU,
                () -> new ShunshouEntity(ModEntities.SHUNSHOU_ENTITY, this.getWorld()),
                "GENERAL_SHUNSHOU", canResponse, "使用『顺手牵羊』！");

        // 过河拆桥
        spawnTacticCard(ModItems.CHAIQIAO, ModSoundEvents.CHAIQIAO,
                () -> new ChaiqiaoEntity(ModEntities.CHAIQIAO_ENTITY, this.getWorld()),
                "GENERAL_CHAIQIAO", canResponse, "使用『过河拆桥』！");

        // 无懈可击
        respondTacticCard(ModItems.WUXIE, ModSoundEvents.WUXIE, "使用『无懈可击』！");


        // 濒死状态处理
        if (isDying()) {
            handleDyingState();
        }
    }
    /**
     * 生成型锦囊牌
     *
     * @param cardItem       锦囊牌物品
     * @param soundEvent     播放的音效
     * @param entitySupplier 提供实体实例的函数
     * @param cardId         锦囊牌 ID
     * @param canResponse    是否可响应
     * @param sayText        播报台词
     */
    private void spawnTacticCard(Item cardItem, SoundEvent soundEvent,
                                 Supplier<TacticCardEntity> entitySupplier,
                                 String cardId, boolean canResponse, String sayText) {
        if (hasCard(cardItem) && consumeCard(cardItem)) {
            say(sayText);
            this.playSound(soundEvent, 1.0f, 1.0f);

            TacticCardEntity entity = entitySupplier.get();
            entity.setOwner(this);
            entity.setResponse(canResponse);
            entity.setPosition(this.getX(), this.getY() + 1.0, this.getZ());
            entity.setVelocity(0, 0.05, 0);
            entity.setCardId(cardId);

            WuXieStack.addWuXieStack(cardId, 0);
            WuXieItem.taskQueue.offer(TacticCardEntity.scheduleTacticCardEffect(entity));
            if (WuXieItem.taskQueue.size() == 1) {
                WuXieItem.processNextTask();
            }
        }
    }

    /**
     * 响应型锦囊牌（如无懈可击）
     *
     * @param cardItem   锦囊牌物品
     * @param soundEvent 播放音效
     * @param sayText    播报台词
     */
    private void respondTacticCard(Item cardItem, SoundEvent soundEvent, String sayText) {
        if (hasCard(cardItem) && consumeCard(cardItem)) {
            PriorityQueue<WuXieItem.NearbyCard> nearbyCards =
                    new PriorityQueue<>(Comparator.comparingDouble(a -> a.distance));

            List<Entity> nearbyEntities = this.getWorld().getEntitiesByClass(Entity.class,
                    this.getBoundingBox().expand(5.0),
                    entity -> entity instanceof TacticCardEntity);

            for (Entity entity : nearbyEntities) {
                if (entity instanceof WuZhongEntity wuZhongEntity) {
                    nearbyCards.offer(new WuXieItem.NearbyCard(wuZhongEntity, this.squaredDistanceTo(entity)));
                }
            }

            WuXieItem.NearbyCard nearestCard = nearbyCards.poll();
            if (nearestCard != null) {
                String targetCardId = nearestCard.card.getCardId();
                WuXieStack.plusWuxieCnt(targetCardId);
                say(sayText);
                this.playSound(soundEvent, 1.0f, 1.0f);
            }
        }
    }



    // ===============================
    // 状态处理
    // ===============================

    /** 进入濒死状态 */
    private void enterDyingState() {
        if (dying) return;
        dying = true;
        dyingStartTime = age;
        setHealth(1.0F);
        speed = 0.0F;
        say("进入濒死状态！快来救我！");
    }

    /** 从濒死状态复活 */
    public void reviveFromDying() {
        if (!dying) return;
        dying = false;
        speed = 2.0F;
        say("脱离濒死状态！");
    }

    /** 濒死状态 AI 行为 */
    private void handleDyingState() {
        if (age - dyingStartTime > 400) {
            say(getName() + "死亡");
            kill();
            return;
        }
        if (consumeCard(ModItems.TAO) || consumeCard(ModItems.JIU)) {
            heal(5.0F);
            say("抢救成功！");
            if (getHealth() > 0) {
                dying = false;
                say("脱离濒死状态！");
            }
        }
    }

    // ===============================
    // 技能
    // ===============================

    /** 检查壮誓技能触发条件 */
    protected boolean checkZhuangshiConditions() {
        return isZhuangshiEnabled() &&
                !isDying() &&
                getHealth() > getMaxHealth() / 4.0f &&
                age - lastZhuangshiTime > ZHUANGSHI_COOLDOWN;
    }

    /** 激活壮誓技能 */
    protected void activateZhuangshi() {
        if (!checkZhuangshiConditions()) return;

        SkillSoundManager.playSkillSound("zhuangshi", this);
        CardGameManager.loseHealth(this, getMaxHealth() / 4.0f);
        say("发动『壮誓』 失去体力：" + getMaxHealth() / 4.0f);

        // 发牌
        for (int i = 0; i < getHealth() / 20.0f; i++) {
            CardGameManager.giveCard(this);
        }

        // 丢弃非杀牌并统计
        int cnt = 0;
        StringBuilder loseCards = new StringBuilder();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() != ModItems.SHA && !stack.isEmpty()) {
                loseCards.append(stack.getName().getString()).append(":")
                        .append(stack.getCount()).append("张，");
                cnt += stack.getCount();
                stack.decrement(stack.getCount());
            }
        }
        if (loseCards.length() > 1) loseCards.deleteCharAt(loseCards.length() - 1);
        say("发动『壮誓』 丢弃牌：" + loseCards);

        // 添加状态效果
        addStatusEffect(new StatusEffectInstance(ModEffects.ZHUANGSHI,
                20 * 10,  // 持续 10 秒
                cnt,
                false,
                true
        ));

        lastZhuangshiTime = age;
    }

    // ===============================
    // 工具方法
    // ===============================

    /** 低血量自动使用指定卡 */
    private boolean consumeCardIfLowHp(Item item, float healAmount) {
        if (getHealth() < getMaxHealth() - healAmount - 5.0F) {
            return consumeCard(item);
        }
        return false;
    }

    /** 检查是否有指定卡 */
    protected boolean hasCard(Item... cards) {
        for (Item card : cards) {
            for (int i = 0; i < inventory.size(); i++) {
                if (inventory.getStack(i).getItem() == card) return true;
            }
        }
        return false;
    }

    /** 消耗指定卡 */
    protected boolean consumeCard(Item item) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                stack.decrement(1);
                if (stack.isEmpty()) inventory.setStack(i, ItemStack.EMPTY);
                return true;
            }
        }
        return false;
    }

    /** 尝试使用杀 */
    public boolean tryUseSha() { return consumeCard(ModItems.SHA); }

    /** 尝试使用闪 */
    public boolean tryUseShan() { return consumeCard(ModItems.SHAN); }

    /** 向目标投掷杀 */
    private void throwShaAt(PlayerEntity target) {
        ShaEntity sha = new ShaEntity(this, getWorld());
        sha.setPosition(getX(), getEyeY(), getZ());

        float bonus = nextShaBonus ? 5.0F : 0.0F;
        nextShaBonus = false;
        drinkJiu = false;

        // 处理壮誓状态效果
        if (hasStatusEffect(ModEffects.ZHUANGSHI)) {
            StatusEffectInstance effect = getStatusEffect(ModEffects.ZHUANGSHI);
            int remaining = effect.getAmplifier() + 1;
            if (remaining > 0) {
                sha.setResponsible(false);
                addStatusEffect(new StatusEffectInstance(ModEffects.ZHUANGSHI,
                        effect.getDuration(),
                        remaining - 1,
                        false, true));
            }
        }

        sha.setBonusDamage(bonus);
        sha.setVelocity(target.getPos().subtract(getPos()).normalize().multiply(1.5));
        getWorld().spawnEntity(sha);
        playSound(ModSoundEvents.SHA_ENTITY_THROW, 1.0F, 1.0F);
    }

    /** 广播消息 */
    public void say(String msg) {
        getWorld().getPlayers().forEach(p ->
                p.sendMessage(Text.of(getName().getString() + "：" + msg), false));
    }

    /** 是否准备攻击 */
    protected boolean shouldPrepareAttack() {
        if (!(getTarget() instanceof PlayerEntity player)) return false;
        if (!player.isAlive() || player.isCreative()) return false;
        return enemies.contains(player.getUuid());
    }

    // ===============================
    // 事件方法
    // ===============================

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof PlayerEntity player) {
            enemies.add(player.getUuid());
            boolean damageFromSha = source.getAttacker() instanceof ShaEntity;
            if (amount > 1.5f && !damageFromSha && tryUseShan()) {
                playSound(ModSoundEvents.SHAN, 1.0f, 1.0f);
                return false;
            }
            setTarget(player);
        }
        if (getHealth() - amount <= 0 && !isDying()) {
            enterDyingState();
            return false;
        }
        return super.damage(source, amount);
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (dying) {
            setVelocity(Vec3d.ZERO); // 濒死不能移动
            return;
        }
        super.travel(movementInput);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack item = player.getStackInHand(hand);
        if (dying && item.getItem() == ModItems.TAO) {
            item.decrement(1);
            heal(5.0F);
            reviveFromDying();
            rescuers.add(player.getUuid());
            player.sendMessage(Text.of("义释" + getName().getString()), false);
            setTarget(null);
            say("谢恩不杀！");
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }
}
