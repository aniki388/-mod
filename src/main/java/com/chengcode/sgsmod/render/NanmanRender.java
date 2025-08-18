package com.chengcode.sgsmod.render;

import com.chengcode.sgsmod.Sgsmod;
import com.chengcode.sgsmod.entity.general.NanmanEntity;
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

public class NanmanRender extends MobEntityRenderer<NanmanEntity, PlayerEntityModel<NanmanEntity>> {
    public static final Identifier TEXTURE = new Identifier(Sgsmod.MOD_ID, "textures/entity/nanman.png");
    public NanmanRender(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getModelLoader().getModelPart(EntityModelLayers.PLAYER), false), 0.5f);
        this.addFeature(new OuterLayerFeatureRenderer(this));
    }

    @Override
    public Identifier getTexture(NanmanEntity entity) {
        return TEXTURE;
    }

    /**
     * 外层渲染（帽子、外套、袖子、裤子）
     */
    private static class OuterLayerFeatureRenderer extends FeatureRenderer<NanmanEntity, PlayerEntityModel<NanmanEntity>> {
        public OuterLayerFeatureRenderer(FeatureRendererContext<NanmanEntity, PlayerEntityModel<NanmanEntity>> context) {
            super(context);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, NanmanEntity entity,
                           float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {

            PlayerEntityModel<NanmanEntity> model = this.getContextModel();

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
