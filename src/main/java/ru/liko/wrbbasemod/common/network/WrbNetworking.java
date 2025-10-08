package ru.liko.wrbbasemod.common.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import ru.liko.wrbbasemod.Wrbbasemod;
import ru.liko.wrbbasemod.common.network.packet.SyncWrbDataPacket;

public final class WrbNetworking {

    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Wrbbasemod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int nextId = 0;

    private WrbNetworking() {}

    public static void register() {
        CHANNEL.registerMessage(nextId++, SyncWrbDataPacket.class, SyncWrbDataPacket::encode, SyncWrbDataPacket::decode, SyncWrbDataPacket::handle);
    }

    public static void sendToClient(Object msg, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }
}
