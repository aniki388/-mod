package com.chengcode.sgsmod.entity;

import com.chengcode.sgsmod.combat.AttackLevel;
import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.item.CombatState;
import com.chengcode.sgsmod.item.ModItems;
import com.chengcode.sgsmod.network.ServerReceiver;
import com.chengcode.sgsmod.skill.Skills;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import com.chengcode.sgsmod.sound.SkillSoundManager;
import com.chengcode.sgsmod.skill.ModSkills;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class ShaEntity extends ThrownItemEntity {
    private boolean responsible = true;
    private float bonusDamage = 0.0F;

    public ShaEntity(EntityType<? extends ThrownItemEntity> type, World world) {
        super(type, world);
    }

    public ShaEntity(LivingEntity owner, World world) {
        super(ModEntities.SHA_ENTITY, owner, world);
    }

    public void setBonusDamage(float bonus) {
        this.bonusDamage = bonus;
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SHA;
    }

    private void dropAsItem() {
        this.getWorld().spawnEntity(new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), new ItemStack(ModItems.SHA)));
    }

    private boolean hasShan(ServerPlayerEntity player) {
        return countShanCards(player) > 0;
    }

    private int countShanCards(ServerPlayerEntity player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).getItem() == ModItems.SHAN) {
                count++;
            }
        }
        return count;
    }

    private int countCards(Entity owner) {
        int count = 0;
        if (owner instanceof ServerPlayerEntity player) {
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack stack = player.getInventory().getStack(i);

                if (stack.getItem() == ModItems.SHA ||
                        stack.getItem() == ModItems.SHAN ||
                        stack.getItem() == ModItems.LEISHA ||
                        stack.getItem() == ModItems.HUOSHA ||
                        stack.getItem() == ModItems.JIU ||
                        stack.getItem() == ModItems.TAO ||
                        stack.getItem() == ModItems.CARD_STACK ||
                        stack.getItem() == ModItems.WUZHONG ||
                        stack.getItem() == ModItems.WUXIE) {
                    count += stack.getCount();
                }
            }
        } else if (owner instanceof GeneralEntity general) {
            for (int i = 0; i < general.getInventory().size(); i++) {
                ItemStack stack = general.getInventory().getStack(i);

                if (stack.getItem() == ModItems.SHA ||
                        stack.getItem() == ModItems.SHAN ||
                        stack.getItem() == ModItems.LEISHA ||
                        stack.getItem() == ModItems.HUOSHA ||
                        stack.getItem() == ModItems.JIU ||
                        stack.getItem() == ModItems.TAO ||
                        stack.getItem() == ModItems.CARD_STACK ||
                        stack.getItem() == ModItems.WUZHONG ||
                        stack.getItem() == ModItems.WUXIE) {
                    count += stack.getCount();
                }
            }
        }
        return count;
    }

    private boolean hasSkill(Entity owner, Skills skill) {
        if (owner instanceof ServerPlayerEntity player) {
            return ModSkills.hasSkill(player, skill.name().toLowerCase());
        } else if (owner instanceof GeneralEntity general) {
            switch (skill) {
                case wushuang:
                    return general.isWushuangEnabled();
                case jieliegong:
                    return general.isJieLiegongEnabled();
                case kuanggu:
                    return general.isKuangguEnabled();
                default:
                    return false;
            }
        }
        return false;
    }



    private int handleShaHitPlayer(PlayerEntity target, int totalDamage) {
        if (!(target instanceof ServerPlayerEntity serverPlayer)) return 0;

        Entity owner = this.getOwner();
        boolean canResponse = isResponsible() ;
        boolean isWushuang = hasSkill(owner,Skills.wushuang);
        boolean isJieLiegong = hasSkill(owner,Skills.jieliegong);
        boolean isKuanggu = hasSkill(owner,Skills.kuanggu);

        if (isWushuang) {
            SkillSoundManager.playSkillSound("wushuang", (LivingEntity) owner);
        }
        if (isJieLiegong) {
            SkillSoundManager.playSkillSound("jieliegong", (LivingEntity) owner);
            if (owner instanceof LivingEntity living){
                int ownerCards = countCards(owner);
                int targetCards = countCards(target);
                if (ownerCards > targetCards) {
                    canResponse = false;
                }
                float ownerHealth = living.getHealth();
                float targetHealth = target.getHealth();
                if (ownerHealth <= targetHealth) {
                    totalDamage += 5;
                }
            }
        }
        if (canResponse) {
            if (hasShan(serverPlayer)) {
                if (isWushuang) {
                    if(!ServerReceiver.promptDoubleShan(serverPlayer, totalDamage)){
                        totalDamage = 0;
                    };
                } else {
                    if(!ServerReceiver.promptShan(serverPlayer, totalDamage)){
                        totalDamage = 0;
                    };
                }
            } else {
                serverPlayer.sendMessage(Text.of("你没有『闪』，受到" + totalDamage + "点伤害"), false);
                serverPlayer.damage(this.getWorld().getDamageSources().thrown(this, this.getOwner()), totalDamage);
            }
        }else {
            serverPlayer.sendMessage(Text.of("你无法响应！"), false);
            serverPlayer.damage(this.getWorld().getDamageSources().thrown(this, this.getOwner()), totalDamage);
        }
        if (isKuanggu && totalDamage >  0) {
            SkillSoundManager.playSkillSound("kuanggu",(LivingEntity) owner);
            if(owner instanceof GeneralEntity general){
                general.say("发动『狂骨』 恢复体力：5");
                general.heal(5.0f);
                general.say("获得一张牌");
                CardGameManager.giveCard(general);
            }else if (owner instanceof PlayerEntity player){
                player.sendMessage(Text.of("发动『狂骨』 恢复体力：5"), false);
                player.heal(5.0f);
                player.sendMessage(Text.of("获得一张牌"), false);
                CardGameManager.giveCard(player);
            }
        }
        return totalDamage;
    }

    private int handleShaHitGeneral(GeneralEntity general, int totalDamage) {
        Entity owner = this.getOwner();
        boolean canResponse = isResponsible();
        boolean isWushuang = hasSkill(owner,Skills.wushuang);
        boolean isJieLiegong = hasSkill(owner,Skills.jieliegong);
        boolean isKuanggu = hasSkill(owner,Skills.kuanggu);
        if (isWushuang) {
            SkillSoundManager.playSkillSound("wushuang", (LivingEntity) owner);
        }

        if (isJieLiegong) {
            SkillSoundManager.playSkillSound("jieliegong", (LivingEntity) owner);
            if (owner instanceof LivingEntity living){
                int ownerCards = countCards(living);
                int targetCards = countCards(general);
                if (ownerCards > targetCards) {
                    canResponse = false;
                }
                float ownerHealth = living.getHealth();
                float targetHealth = general.getHealth();
                if (ownerHealth <= targetHealth) {
                    totalDamage += 5;
                }
            }
        }


        int cntShan = general.getInventory().count(ModItems.SHAN);
        if (cntShan >= 1) {
            if (isWushuang) {
                if (cntShan >= 2) {
                    if (general.consumeCard(ModItems.SHAN)) {
                        general.playSound(ModSoundEvents.SHAN, 1.0F, 1.0F);
                        totalDamage = 0;
                    }
                    if (general.consumeCard(ModItems.SHAN)) {
                        general.playSound(ModSoundEvents.SHAN, 1.0F, 1.0F);
                        totalDamage = 0;
                    }
                }else if (!canResponse) {
                    general.damage(this.getWorld().getDamageSources().thrown(this, this.getOwner()), totalDamage);
                }
                else {
                    general.damage(this.getWorld().getDamageSources().thrown(this, this.getOwner()), totalDamage);
                }
            }else if (!canResponse) {
                general.damage(this.getWorld().getDamageSources().thrown(this, this.getOwner()), totalDamage);
            }
            else {
                if (general.consumeCard(ModItems.SHAN)) {
                    general.playSound(ModSoundEvents.SHAN, 1.0F, 1.0F);
                    totalDamage = 0;
                }
            }
        } else {
            general.damage(this.getWorld().getDamageSources().thrown(this, this.getOwner()), totalDamage);
        }

        if (isKuanggu && totalDamage > 0) {
            SkillSoundManager.playSkillSound("kuanggu",(LivingEntity) owner);
            if(owner instanceof GeneralEntity generalEntity){
                generalEntity.say("发动『狂骨』 恢复体力：5");
                CardGameManager.recoverHealth(generalEntity, 5.0f);
                generalEntity.say("获得一张牌");
                CardGameManager.giveCard(generalEntity);
            }else if (owner instanceof PlayerEntity player){
                player.sendMessage(Text.of("发动『狂骨』 恢复体力：5"), false);
                player.sendMessage(Text.of("获得一张牌"), false);
                CardGameManager.recoverHealth(player, 5.0f);
                CardGameManager.giveCard(player);
            }
        }
        return totalDamage;
    }



    @Override
    protected void onCollision(HitResult hitResult) {
        if (!this.getWorld().isClient) {
            switch (hitResult.getType()) {
                case ENTITY -> {
                    EntityHitResult entityHit = (EntityHitResult) hitResult;
                    Entity hitEntity = entityHit.getEntity();

                    int baseDamage = 5;
                    int bonus = 0;

                    if (this.getOwner() instanceof PlayerEntity owner) {
                        CombatState state = CombatState.get(owner);
                        bonus = state.getBonusDamage();
                        state.clear();
                    }
                    LivingEntity owner = (LivingEntity) this.getOwner();

                    int totalDamage = baseDamage + bonus + (int) this.bonusDamage;

                    if (hitEntity instanceof PlayerEntity target) {
                        AttackLevel level = AttackLevel.fromDamage(handleShaHitPlayer(target, totalDamage));
                        playSound(level);
                    } else if (hitEntity instanceof GeneralEntity target) {
                        AttackLevel level = AttackLevel.fromDamage(handleShaHitGeneral(target, totalDamage));
                        playSound(level);
                    }else if (hitEntity instanceof LivingEntity livingEntity) {
                        livingEntity.damage(this.getWorld().getDamageSources().thrown(this, this.getOwner()), totalDamage);
                        AttackLevel level = AttackLevel.fromDamage(totalDamage);
                        playSound(level);
                    }
                    else {
                        dropAsItem();
                    }
                }
                case BLOCK -> dropAsItem();
            }
            this.discard();
        }
    }

    private void playSound(AttackLevel level) {
        for (SoundEvent sfx : level.getSounds()) {
            this.getWorld().playSound(null, getX(), getY(), getZ(), sfx, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }

    public boolean isResponsible() {
        return responsible;
    }
    public void setResponsible(boolean responsible) {
        this.responsible = responsible;
    }
}
