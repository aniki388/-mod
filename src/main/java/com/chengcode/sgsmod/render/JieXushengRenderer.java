package com.chengcode.sgsmod.render;

import com.chengcode.sgsmod.Sgsmod;
import com.chengcode.sgsmod.entity.general.JieXushengEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

public class JieXushengRenderer extends MobEntityRenderer<JieXushengEntity, PlayerEntityModel<JieXushengEntity>> {
    public static final Identifier TEXTURE = new Identifier(Sgsmod.MOD_ID,"textures/entity/jiexusheng.png");
    public JieXushengRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getModelLoader().getModelPart(EntityModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public Identifier getTexture(JieXushengEntity entity) {
        return TEXTURE;
    }
}
