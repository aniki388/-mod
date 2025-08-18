package com.chengcode.sgsmod.render;

import com.chengcode.sgsmod.item.LvbuHatItem;
import com.chengcode.sgsmod.model.LvbuHatModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class LvbuHatRender extends GeoItemRenderer<LvbuHatItem> {
    public LvbuHatRender() {
        super(new LvbuHatModel());
    }
}
