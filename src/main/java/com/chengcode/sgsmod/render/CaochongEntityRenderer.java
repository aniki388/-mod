package com.chengcode.sgsmod.render;

import com.chengcode.sgsmod.entity.general.CaochongEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

public class CaochongEntityRenderer extends MobEntityRenderer<CaochongEntity, PlayerEntityModel<CaochongEntity>> {
    public static final Identifier TEXTURE = new Identifier("sgsmod:textures/entity/caochong.png");
    public CaochongEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getModelLoader().getModelPart(EntityModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public Identifier getTexture(CaochongEntity entity) {
        return TEXTURE;
    }
}
