package com.chengcode.sgsmod.entity;

import com.chengcode.sgsmod.Sgsmod;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

public class WeiYanEntityRenderer extends MobEntityRenderer<WeiYanEntity, BipedEntityModel<WeiYanEntity>> {
    private static final Identifier TEXTURE = new Identifier(Sgsmod.MOD_ID, "textures/entity/weiyan.png");

    public WeiYanEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public void render(WeiYanEntity entity, float f, float g, net.minecraft.client.util.math.MatrixStack matrices,
                      net.minecraft.client.render.VertexConsumerProvider vertexConsumers, int light) {
        if (entity.isDying()) {
            entity.setPose(net.minecraft.entity.EntityPose.SLEEPING);
        }
        super.render(entity, f, g, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(WeiYanEntity entity) {
        return TEXTURE;
    }
}
