package com.chengcode.sgsmod.entity;

import com.chengcode.sgsmod.Sgsmod;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.entity.EntityRendererFactory;


public class LvBuEntityRenderer extends MobEntityRenderer<LvBuEntity, BipedEntityModel<LvBuEntity>> {
    private static final Identifier TEXTURE = new Identifier(Sgsmod.MOD_ID, "textures/entity/lvbu.png");

    public LvBuEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public void render(LvBuEntity entity, float f, float g, net.minecraft.client.util.math.MatrixStack matrices,
                       net.minecraft.client.render.VertexConsumerProvider vertexConsumers, int light) {

        // 如果是濒死状态，则强制设置为 SLEEPING 姿势
        if (entity.isDying()) {
            entity.setPose(net.minecraft.entity.EntityPose.SLEEPING);
        }

        super.render(entity, f, g, matrices, vertexConsumers, light);
    }


    @Override
    public Identifier getTexture(LvBuEntity entity) {
        return TEXTURE;
    }
}