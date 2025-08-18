package com.chengcode.sgsmod.render;

import com.chengcode.sgsmod.Sgsmod;
import com.chengcode.sgsmod.effect.ModEffects;
import com.chengcode.sgsmod.entity.general.WeiYanEntity;
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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class WeiYanEntityRenderer extends MobEntityRenderer<WeiYanEntity, PlayerEntityModel<WeiYanEntity>> {
    private static final Identifier BASE_TEXTURE = new Identifier(Sgsmod.MOD_ID, "textures/entity/weiyan.png");
    private static final Identifier HY_TEXTURE = new Identifier(Sgsmod.MOD_ID, "textures/entity/hongyan_weiyan.png");

    public WeiYanEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getModelLoader().getModelPart(EntityModelLayers.PLAYER), false), 0.5f);

        // 添加外层渲染层
        this.addFeature(new OuterLayerFeatureRenderer(this));
    }

    @Override
    public void render(WeiYanEntity entity, float f, float g, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        if (entity.isDying()) {
            entity.setPose(net.minecraft.entity.EntityPose.SLEEPING);
        }
        super.render(entity, f, g, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(WeiYanEntity entity) {
        if (entity.hasStatusEffect(ModEffects.ZHUANGSHI)) return HY_TEXTURE;
        return BASE_TEXTURE;
    }

    private static class OuterLayerFeatureRenderer extends FeatureRenderer<WeiYanEntity, PlayerEntityModel<WeiYanEntity>> {
        public OuterLayerFeatureRenderer(FeatureRendererContext<WeiYanEntity, PlayerEntityModel<WeiYanEntity>> context) {
            super(context);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, WeiYanEntity entity,
                           float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {

            PlayerEntityModel<WeiYanEntity> model = this.getContextModel();

            model.setVisible(false);
            model.hat.visible = true;
            model.jacket.visible = true;
            model.leftSleeve.visible = true;
            model.rightSleeve.visible = true;
            model.leftPants.visible = true;
            model.rightPants.visible = true;

            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(getTexture(entity)));
            model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV,
                    1.0F, 1.0F, 1.0F, 1.0F);

            model.setVisible(true);
        }
    }
}
