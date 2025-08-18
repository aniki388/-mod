package com.chengcode.sgsmod.model;

import com.chengcode.sgsmod.Sgsmod;
import com.chengcode.sgsmod.animation.ElephantAnimation;
import com.chengcode.sgsmod.entity.general.ElephantEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.animation.AnimationHelper;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

public class ElephantModel<T extends ElephantEntity> extends SinglePartEntityModel<T> {
    private final Vector3f tempVec = new Vector3f();
    private float animationTime = 0f;
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(new Identifier(Sgsmod.MOD_ID, "elephant"), "main");
    private final ModelPart elephant_root;
    private final ModelPart bone;
    private final ModelPart trunk_seg1;
    private final ModelPart tusk_left;
    private final ModelPart back_foot_left;
    private final ModelPart front_foot_right;
    private final ModelPart front_foot_left;
    private final ModelPart back_foot_right;

    public ElephantModel(ModelPart root) {
        this.elephant_root = root.getChild("elephant_root");
        this.bone = root.getChild("bone");
        this.trunk_seg1 = this.bone.getChild("trunk_seg1");
        this.tusk_left = this.bone.getChild("tusk_left");
        this.back_foot_left = this.bone.getChild("back_foot_left");
        this.front_foot_right = this.bone.getChild("front_foot_right");
        this.front_foot_left = this.bone.getChild("front_foot_left");
        this.back_foot_right = this.bone.getChild("back_foot_right");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        ModelPartData elephant_root = modelPartData.addChild("elephant_root", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData bone = modelPartData.addChild("bone", ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-10.0F, -18.0F, -6.0F, 20.0F, 12.0F, 28.0F)
                        .uv(0, 40).cuboid(-8.0F, -6.0F, -2.0F, 16.0F, 4.0F, 22.0F)
                        .uv(0, 66).cuboid(-7.0F, -20.0F, -12.0F, 14.0F, 8.0F, 8.0F)
                        .uv(0, 66).cuboid(-7.0F, -20.0F, -12.0F, 0.0F, 8.0F, 8.0F)
                        .uv(0, 82).cuboid(-6.0F, -22.0F, 6.0F, 12.0F, 4.0F, 8.0F),
                ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData tail_r1 = bone.addChild("tail_r1", ModelPartBuilder.create()
                        .uv(20, 94).cuboid(-2.0F, -8.0F, 21.0F, 2.0F, 2.0F, 8.0F),
                ModelTransform.of(0.0F, -14.0F, 0.0F, -0.6981F, 0.0F, 0.0F));

        ModelPartData trunk_tip_r1 = bone.addChild("trunk_tip_r1", ModelPartBuilder.create()
                        .uv(100, 87).cuboid(-1.0F, -10.0F, -16.0F, 2.0F, 4.0F, 4.0F),
                ModelTransform.of(-11.0F, -3.0F, 6.0F, -0.5236F, 0.0F, 0.0F));

        ModelPartData trunk_seg2_r1 = bone.addChild("trunk_seg2_r1", ModelPartBuilder.create()
                        .uv(96, 32).cuboid(0.0F, -12.0F, -16.0F, 2.0F, 4.0F, 4.0F),
                ModelTransform.of(10.0F, -3.0F, 8.0F, -0.3491F, 0.0F, 0.0F));

        ModelPartData ear_right_r1 = bone.addChild("ear_right_r1", ModelPartBuilder.create()
                        .uv(82, 73).cuboid(7.0F, -18.0F, -10.0F, 6.0F, 6.0F, 8.0F),
                ModelTransform.of(-5.0F, -4.0F, -2.0F, 0.0F, 0.0F, 0.3142F));

        ModelPartData ear_left_r1 = bone.addChild("ear_left_r1", ModelPartBuilder.create()
                        .uv(82, 59).cuboid(-13.0F, -18.0F, -10.0F, 6.0F, 6.0F, 8.0F),
                ModelTransform.of(5.0F, -5.0F, -2.0F, 0.0F, 0.0F, -0.3142F));

        ModelPartData trunk_seg1 = bone.addChild("trunk_seg1", ModelPartBuilder.create(),
                ModelTransform.pivot(0.0F, 3.0F, 0.0F));

        ModelPartData trunk_seg1_r1 = trunk_seg1.addChild("trunk_seg1_r1", ModelPartBuilder.create()
                        .uv(96, 0).cuboid(-2.0F, -14.0F, -15.0F, 4.0F, 14.0F, 2.0F),
                ModelTransform.of(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

        ModelPartData tusk_left = bone.addChild("tusk_left", ModelPartBuilder.create(),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData tusk_left_r1 = tusk_left.addChild("tusk_left_r1", ModelPartBuilder.create()
                        .uv(44, 66).cuboid(-5.0F, -14.0F, -16.0F, 3.0F, 3.0F, 16.0F)
                        .uv(76, 40).cuboid(2.0F, -14.0F, -16.0F, 3.0F, 3.0F, 16.0F),
                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.2618F, 0.0F, 0.0F));

        ModelPartData back_foot_left = bone.addChild("back_foot_left", ModelPartBuilder.create()
                        .uv(96, 16).cuboid(-1.0F, -1.0F, 6.0F, 4.0F, 2.0F, 6.0F)
                        .uv(80, 87).cuboid(-1.0F, -9.0F, 6.0F, 4.0F, 8.0F, 6.0F),
                ModelTransform.pivot(-8.0F, 0.0F, 8.0F));

        ModelPartData front_foot_right = bone.addChild("front_foot_right", ModelPartBuilder.create()
                        .uv(96, 24).cuboid(13.0F, -1.0F, -8.0F, 4.0F, 2.0F, 6.0F)
                        .uv(60, 85).cuboid(13.0F, -7.0F, -8.0F, 4.0F, 6.0F, 6.0F),
                ModelTransform.pivot(-8.0F, 0.0F, 8.0F));

        ModelPartData front_foot_left = bone.addChild("front_foot_left", ModelPartBuilder.create()
                        .uv(96, 16).cuboid(-1.0F, -1.0F, -8.0F, 4.0F, 2.0F, 6.0F)
                        .uv(40, 85).cuboid(-1.0F, -13.0F, -8.0F, 4.0F, 12.0F, 6.0F),
                ModelTransform.pivot(-8.0F, 0.0F, 8.0F));

        ModelPartData back_foot_right = bone.addChild("back_foot_right", ModelPartBuilder.create()
                        .uv(96, 24).cuboid(13.0F, -1.0F, 6.0F, 4.0F, 2.0F, 6.0F)
                        .uv(0, 94).cuboid(13.0F, -6.0F, 6.0F, 4.0F, 5.0F, 6.0F),
                ModelTransform.pivot(-8.0F, 0.0F, 8.0F));

        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {

        // 先重置所有骨骼，防止残留
        this.getPart().traverse().forEach(ModelPart::resetTransform);

        // 动画播放时间（毫秒）：
        long runningTime = (long)(ageInTicks * 50); // 1 tick = 50 ms

        // 播放攻击动画（优先级更高）
        if (entity.attackAnimationState.isRunning()) {
            AnimationHelper.animate(
                    this,
                    ElephantAnimation.ATTACK,
                    entity.attackAnimationState.getTimeRunning(),
                    1.0f,
                    tempVec
            );
        }
        // 播放行走动画
        else if (entity.walkAnimationState.isRunning()) {
            AnimationHelper.animate(
                    this,
                    ElephantAnimation.WALK,
                    runningTime,
                    1.0f,
                    tempVec
            );
        }
    }





    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        matrices.push();
        matrices.translate(0.0D, -0.7D, 0.0D);
        matrices.scale(1.5F, 1.5F, 1.5F);
        elephant_root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        bone.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        matrices.pop();
    }

    @Override
    public ModelPart getPart() {
        return this.bone;
    }
}
