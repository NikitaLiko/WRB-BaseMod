package ru.liko.wrbbasemod.common.player;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import ru.liko.wrbbasemod.Wrbbasemod;

/**
 * Capability provider that attaches {@link WrbPlayerData} to every player.
 */
public class WrbPlayerDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static final Capability<WrbPlayerData> WRB_PLAYER_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation ID = new ResourceLocation(Wrbbasemod.MODID, "player_data");

    private WrbPlayerData data;
    private LazyOptional<WrbPlayerData> optionalData;

    private WrbPlayerData getOrCreateData() {
        if (data == null) {
            data = new WrbPlayerData();
            optionalData = LazyOptional.of(() -> data);
        }
        return data;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == WRB_PLAYER_DATA_CAPABILITY) {
            return getOptional().cast();
        }
        return LazyOptional.empty();
    }

    private LazyOptional<WrbPlayerData> getOptional() {
        if (optionalData == null) {
            optionalData = LazyOptional.of(this::getOrCreateData);
        }
        return optionalData;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        getOrCreateData().saveNBTData(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getOrCreateData().loadNBTData(nbt);
    }

    public void invalidate() {
        if (optionalData != null) {
            optionalData.invalidate();
            optionalData = null;
        }
    }
}
