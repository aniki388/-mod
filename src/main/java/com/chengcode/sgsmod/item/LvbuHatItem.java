package com.chengcode.sgsmod.item;

import com.chengcode.sgsmod.render.LvbuHatRender;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.RenderUtils;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LvbuHatItem extends Item implements GeoItem  {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache( this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public LvbuHatItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stackInHand = player.getStackInHand(hand);
        ItemStack headSlotStack = player.getEquippedStack(EquipmentSlot.HEAD);

        if (headSlotStack.isEmpty()) {
            // 如果头盔槽是空的，就戴上
            player.equipStack(EquipmentSlot.HEAD, stackInHand.copy());


            stackInHand.decrement(1);


            return TypedActionResult.success(stackInHand, world.isClient());
        } else {
            // 如果头盔槽有东西，就不戴
            return TypedActionResult.fail(stackInHand);
        }
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private final LvbuHatRender renderer = new LvbuHatRender();
            @Override
            public BuiltinModelItemRenderer getCustomRenderer() {
                return this.renderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }

    @Override
    public double getTick(Object itemStack) {
        return RenderUtils.getCurrentTick();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<LvbuHatItem> lvbuHatItemAnimationState) {
//        lvbuHatItemAnimationState.getController().setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        return PlayState.STOP;
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
