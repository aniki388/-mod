package com.chengcode.sgsmod.render;

import com.chengcode.sgsmod.Sgsmod;
import com.chengcode.sgsmod.entity.general.ElephantEntity;
import com.chengcode.sgsmod.entity.ModEntityModelLayers;
import com.chengcode.sgsmod.model.ElephantModel;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class ElephantEntityRenderer extends MobEntityRenderer<ElephantEntity, ElephantModel<ElephantEntity>> {
    private static final Identifier TEXTUR = new Identifier(Sgsmod.MOD_ID, "textures/entity/elephant.png");

    public ElephantEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new ElephantModel<>(context.getPart(ModEntityModelLayers.ELEPHANT_LAYER)), 2.0f);
    }

    @Override
    public Identifier getTexture(ElephantEntity entity) {
        return TEXTUR;
    }
}
