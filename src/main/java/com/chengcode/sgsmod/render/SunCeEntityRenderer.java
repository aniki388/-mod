package com.chengcode.sgsmod.render;

import com.chengcode.sgsmod.Sgsmod;
import com.chengcode.sgsmod.entity.general.SunCeEntity;
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

public class SunCeEntityRenderer extends MobEntityRenderer<SunCeEntity, PlayerEntityModel<SunCeEntity>> {
    private static final Identifier TEXTURE = new Identifier(Sgsmod.MOD_ID, "textures/entity/sunce.png");

    public SunCeEntityRenderer(EntityRendererFactory.Context context) {
        // true 表示 slim 手型（Alex），false 表示 normal（Steve）
        super(context, new PlayerEntityModel<>(context.getModelLoader().getModelPart(EntityModelLayers.PLAYER), false), 0.5f);

        this.addFeature(new OuterLayerFeatureRenderer(this));
    }

    @Override
    public Identifier getTexture(SunCeEntity entity) {
        return TEXTURE;
    }

    private static class OuterLayerFeatureRenderer extends FeatureRenderer<SunCeEntity, PlayerEntityModel<SunCeEntity>> {
        public OuterLayerFeatureRenderer(FeatureRendererContext<SunCeEntity, PlayerEntityModel<SunCeEntity>> context) {
            super(context);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, SunCeEntity entity,
                           float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {

            PlayerEntityModel<SunCeEntity> model = this.getContextModel();

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
