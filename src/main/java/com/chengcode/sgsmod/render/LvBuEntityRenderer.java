package com.chengcode.sgsmod.render;

import com.chengcode.sgsmod.Sgsmod;
import com.chengcode.sgsmod.entity.general.LvBuEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;

public class LvBuEntityRenderer extends MobEntityRenderer<LvBuEntity, PlayerEntityModel<LvBuEntity>> {
    private static final Identifier TEXTURE = new Identifier(Sgsmod.MOD_ID, "textures/entity/lvbu.png");

    public LvBuEntityRenderer(EntityRendererFactory.Context context) {
        // 使用 PlayerEntityModel（false = Steve 模型手型）
        super(context, new PlayerEntityModel<>(context.getModelLoader().getModelPart(EntityModelLayers.PLAYER), false), 0.5f);

        // 添加外层贴图渲染
        this.addFeature(new OuterLayerFeatureRenderer(this));
    }

    @Override
    public void render(LvBuEntity entity, float f, float g, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {

        // 保留原逻辑：濒死状态 → 睡姿
        if (entity.isDying()) {
            entity.setPose(net.minecraft.entity.EntityPose.SLEEPING);
        }

        super.render(entity, f, g, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(LvBuEntity entity) {
        return TEXTURE;
    }

    /**
     * 外层渲染（帽子、外套、袖子、裤子）
     */
    private static class OuterLayerFeatureRenderer extends FeatureRenderer<LvBuEntity, PlayerEntityModel<LvBuEntity>> {
        public OuterLayerFeatureRenderer(FeatureRendererContext<LvBuEntity, PlayerEntityModel<LvBuEntity>> context) {
            super(context);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, LvBuEntity entity,
                           float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {

            PlayerEntityModel<LvBuEntity> model = this.getContextModel();

            // 隐藏基础层
            model.setVisible(false);

            // 外层部件
            model.hat.visible = true;
            model.jacket.visible = true;
            model.leftSleeve.visible = true;
            model.rightSleeve.visible = true;
            model.leftPants.visible = true;
            model.rightPants.visible = true;

            // 渲染外层（身体衣服）
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(getTexture(entity)));
            model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV,
                    1.0F, 1.0F, 1.0F, 1.0F);

            // 渲染帽子
            ItemStack headStack = entity.getEquippedStack(EquipmentSlot.HEAD);
            if (!headStack.isEmpty()) {
                matrices.push(); // 保存矩阵状态
                try {
                    model.head.rotate(matrices); // 让帽子跟随头部旋转
                    matrices.translate(0.0, 0, 0.0);
                    matrices.multiply(new Quaternionf().rotationY((float) Math.toRadians(180)));
                    matrices.multiply(new Quaternionf().rotationX((float) Math.toRadians(180)));
                    MinecraftClient.getInstance().getItemRenderer().renderItem(
                            headStack,
                            ModelTransformationMode.HEAD,
                            light,
                            OverlayTexture.DEFAULT_UV,
                            matrices,
                            vertexConsumers,
                            entity.getWorld(),
                            0
                    );
                } finally {
                    matrices.pop(); // 恢复矩阵状态
                }
            }

            // 恢复显示
            model.setVisible(true);
        }
    }
}
