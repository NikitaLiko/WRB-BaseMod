package ru.liko.wrbbasemod.common.item.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import ru.liko.wrbbasemod.Wrbbasemod;
import ru.liko.wrbbasemod.common.item.MilitaryPdaItem;

public class MilitaryPdaModel extends GeoModel<MilitaryPdaItem> {
    private static final ResourceLocation MODEL = new ResourceLocation(Wrbbasemod.MODID, "geo/military_pda.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Wrbbasemod.MODID, "textures/item/military_pda.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Wrbbasemod.MODID, "animations/military_pda.animation.json");

    @Override
    public ResourceLocation getModelResource(MilitaryPdaItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MilitaryPdaItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MilitaryPdaItem animatable) {
        return ANIMATION;
    }
}
