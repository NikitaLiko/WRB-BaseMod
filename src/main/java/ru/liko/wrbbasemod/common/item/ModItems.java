package ru.liko.wrbbasemod.common.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.RegistryObject;

import ru.liko.wrbbasemod.Wrbbasemod;

public final class ModItems {

    private ModItems() {}

    public static final RegistryObject<Item> MILITARY_PDA = Wrbbasemod.ITEMS.register(
            "military_pda",
            () -> new MilitaryPdaItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON))
    );
}
