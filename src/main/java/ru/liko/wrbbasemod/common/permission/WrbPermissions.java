package ru.liko.wrbbasemod.common.permission;

import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;

import ru.liko.wrbbasemod.Wrbbasemod;

public final class WrbPermissions {

    public static final PermissionNode<Boolean> RANK_UP = new PermissionNode<>(Wrbbasemod.MODID, "rank.up", PermissionTypes.BOOLEAN, (player, playerUUID, context) -> player != null && player.hasPermissions(2));
    public static final PermissionNode<Boolean> RANK_DOWN = new PermissionNode<>(Wrbbasemod.MODID, "rank.down", PermissionTypes.BOOLEAN, (player, playerUUID, context) -> player != null && player.hasPermissions(2));

    private WrbPermissions() {}

    public static void addNodes(PermissionGatherEvent.Nodes event) {
        event.addNodes(RANK_UP, RANK_DOWN);
    }
}
