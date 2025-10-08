package ru.liko.wrbbasemod.common.player;

import net.minecraft.nbt.CompoundTag;

/**
 * Stores per-player data for the mod (rank and PDA state).
 */
public class WrbPlayerData {
    private static final String TAG_COMPASS_ACTIVE = "CompassActive";
    private static final String TAG_RANK = "Rank";

    private boolean compassActive = false;
    private WrbRank rank = WrbRank.PRIVATE;

    public boolean isCompassActive() {
        return compassActive;
    }

    public void setCompassActive(boolean compassActive) {
        this.compassActive = compassActive;
    }

    public WrbRank getRank() {
        return rank;
    }

    public void setRank(WrbRank rank) {
        this.rank = rank == null ? WrbRank.PRIVATE : rank;
    }

    public void copyFrom(WrbPlayerData other) {
        this.compassActive = other.compassActive;
        this.rank = other.rank;
    }

    public void saveNBTData(CompoundTag tag) {
        tag.putBoolean(TAG_COMPASS_ACTIVE, compassActive);
        tag.putString(TAG_RANK, rank.getPersistenceKey());
    }

    public void loadNBTData(CompoundTag tag) {
        compassActive = tag.getBoolean(TAG_COMPASS_ACTIVE);
        setRank(WrbRank.fromKeyOrDefault(tag.getString(TAG_RANK)));
    }
}
