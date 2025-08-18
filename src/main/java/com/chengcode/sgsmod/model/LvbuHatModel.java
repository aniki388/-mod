package com.chengcode.sgsmod.model;

import com.chengcode.sgsmod.Sgsmod;
import com.chengcode.sgsmod.item.LvbuHatItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class LvbuHatModel extends GeoModel<LvbuHatItem> {
    @Override
    public Identifier getModelResource(LvbuHatItem lvbuHatItem) {
        return new Identifier(Sgsmod.MOD_ID, "geo/lvbu_hat.geo.json");
    }

    @Override
    public Identifier getTextureResource(LvbuHatItem lvbuHatItem) {
        return new Identifier(Sgsmod.MOD_ID, "textures/item/lvbu_hat.png");
    }

    @Override
    public Identifier getAnimationResource(LvbuHatItem lvbuHatItem) {
        return new Identifier(Sgsmod.MOD_ID, "animations/lvbu_hat.animation.json");
    }
}
