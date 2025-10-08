package ru.liko.wrbbasemod.common.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import ru.liko.wrbbasemod.common.player.WrbPlayerData;
import ru.liko.wrbbasemod.common.player.WrbPlayerDataProvider;
import ru.liko.wrbbasemod.common.player.WrbRank;

public class SyncWrbDataPacket {
    private final boolean compassActive;
    private final String rankId;

    public SyncWrbDataPacket(boolean compassActive, String rankId) {
        this.compassActive = compassActive;
        this.rankId = rankId;
    }

    public SyncWrbDataPacket(WrbPlayerData data) {
        this(data.isCompassActive(), data.getRank().getPersistenceKey());
    }

    public static void encode(SyncWrbDataPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.compassActive);
        buf.writeUtf(packet.rankId);
    }

    public static SyncWrbDataPacket decode(FriendlyByteBuf buf) {
        boolean compassActive = buf.readBoolean();
        String rank = buf.readUtf();
        return new SyncWrbDataPacket(compassActive, rank);
    }

    public static void handle(SyncWrbDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
                if (minecraft.player == null) {
                    return;
                }
                minecraft.player.getCapability(WrbPlayerDataProvider.WRB_PLAYER_DATA_CAPABILITY)
                        .ifPresent(data -> {
                            data.setCompassActive(packet.compassActive);
                            data.setRank(WrbRank.fromKeyOrDefault(packet.rankId));
                        });
            } else {
                ServerPlayer serverPlayer = context.getSender();
                if (serverPlayer == null) {
                    return;
                }
                serverPlayer.getCapability(WrbPlayerDataProvider.WRB_PLAYER_DATA_CAPABILITY)
                        .ifPresent(data -> {
                            data.setCompassActive(packet.compassActive);
                            data.setRank(WrbRank.fromKeyOrDefault(packet.rankId));
                        });
            }
        });
        context.setPacketHandled(true);
    }
}
