package ru.liko.wrbbasemod.common.item.render;

import software.bernie.geckolib.renderer.GeoItemRenderer;

import ru.liko.wrbbasemod.common.item.MilitaryPdaItem;
import ru.liko.wrbbasemod.common.item.model.MilitaryPdaModel;

public class MilitaryPdaRenderer extends GeoItemRenderer<MilitaryPdaItem> {
    public MilitaryPdaRenderer() {
        super(new MilitaryPdaModel());
    }
}
