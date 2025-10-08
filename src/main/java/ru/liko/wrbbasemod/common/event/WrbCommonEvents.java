package ru.liko.wrbbasemod.common.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;

import ru.liko.wrbbasemod.Wrbbasemod;
import ru.liko.wrbbasemod.common.network.WrbNetworking;
import ru.liko.wrbbasemod.common.network.packet.SyncWrbDataPacket;
import ru.liko.wrbbasemod.common.permission.WrbPermissions;
import ru.liko.wrbbasemod.common.player.WrbPlayerData;
import ru.liko.wrbbasemod.common.player.WrbPlayerDataProvider;
import ru.liko.wrbbasemod.common.player.WrbRank;

@Mod.EventBusSubscriber(modid = Wrbbasemod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WrbCommonEvents {

    private WrbCommonEvents() {}

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(WrbPlayerDataProvider.ID, new WrbPlayerDataProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        LazyOptional<WrbPlayerData> oldCap = event.getOriginal().getCapability(WrbPlayerDataProvider.WRB_PLAYER_DATA_CAPABILITY);
        LazyOptional<WrbPlayerData> newCap = event.getEntity().getCapability(WrbPlayerDataProvider.WRB_PLAYER_DATA_CAPABILITY);
        oldCap.ifPresent(oldData -> newCap.ifPresent(newData -> newData.copyFrom(oldData)));
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(WrbPlayerDataProvider.WRB_PLAYER_DATA_CAPABILITY)
                    .ifPresent(data -> WrbNetworking.sendToClient(new SyncWrbDataPacket(data), serverPlayer));
        }
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Player target)) {
            return;
        }
        if (!event.getLevel().isClientSide() && event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                target.getCapability(WrbPlayerDataProvider.WRB_PLAYER_DATA_CAPABILITY).ifPresent(data -> {
                    WrbRank rank = data.getRank();
                    serverPlayer.displayClientMessage(
                            Component.literal(target.getName().getString() + " - ").append(rank.getDisplayName()),
                            true
                    );
                });
            }
        }
    }

    @SubscribeEvent
    public static void onPermissionGather(PermissionGatherEvent.Nodes event) {
        WrbPermissions.addNodes(event);
    }
}

@Mod.EventBusSubscriber(modid = Wrbbasemod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
final class WrbModBusEvents {

    private WrbModBusEvents() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(WrbPlayerData.class);
    }
}
