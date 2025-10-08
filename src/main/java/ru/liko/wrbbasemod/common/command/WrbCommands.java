package ru.liko.wrbbasemod.common.command;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.permission.PermissionAPI;

import ru.liko.wrbbasemod.Wrbbasemod;
import ru.liko.wrbbasemod.common.network.WrbNetworking;
import ru.liko.wrbbasemod.common.network.packet.SyncWrbDataPacket;
import ru.liko.wrbbasemod.common.permission.WrbPermissions;
import ru.liko.wrbbasemod.common.player.WrbPlayerDataProvider;
import ru.liko.wrbbasemod.common.player.WrbRank;

@Mod.EventBusSubscriber(modid = Wrbbasemod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WrbCommands {

    private static final SimpleCommandExceptionType NO_PERMISSION = new SimpleCommandExceptionType(Component.literal("Недостаточно прав для изменения званий."));

    private static final SuggestionProvider<CommandSourceStack> RANK_SUGGESTIONS =
            (context, builder) -> {
                List<String> suggestions = Arrays.stream(WrbRank.values())
                        .flatMap(rank -> Stream.of(rank.getId(), rank.getDisplayName().getString()))
                        .toList();
                return net.minecraft.commands.SharedSuggestionProvider.suggest(suggestions, builder);
            };

    private WrbCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("wrb")
                        .then(Commands.literal("rank")
                                .then(buildRankBranch("up", true))
                                .then(buildRankBranch("down", false)))
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> buildRankBranch(String literal, boolean promotion) {
        return Commands.literal(literal)
                // Приоритетная команда: /wrb rank up <player> <rank>
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("rank", StringArgumentType.greedyString())
                                .suggests(RANK_SUGGESTIONS)
                                .executes(ctx -> executeRankChange(ctx, EntityArgument.getPlayer(ctx, "target"), StringArgumentType.getString(ctx, "rank"), promotion))))
                // Команда для себя: /wrb rank up <rank> (только если нет конфликта с именами игроков)
                .then(Commands.argument("rank", StringArgumentType.greedyString())
                        .suggests(RANK_SUGGESTIONS)
                        .executes(ctx -> {
                            // Проверяем, не является ли это именем игрока
                            String rankStr = StringArgumentType.getString(ctx, "rank");
                            CommandSourceStack source = ctx.getSource();

                            // Если есть игрок с таким именем, показываем ошибку
                            try {
                                source.getServer().getPlayerList().getPlayerByName(rankStr);
                                source.sendFailure(Component.literal("Неоднозначная команда. Используйте: /wrb rank " + literal + " <игрок> <ранг>"));
                                return 0;
                            } catch (Exception ignored) {
                                // Игрока с таким именем нет, это действительно ранг
                            }

                            return executeRankChange(ctx, source.getPlayerOrException(), rankStr, promotion);
                        }));
    }

    private static int executeRankChange(CommandContext<CommandSourceStack> ctx, ServerPlayer target, String rawRank, boolean promotion) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer executor = source.getEntity() instanceof ServerPlayer player ? player : null;

        // Проверяем права доступа
        if (executor != null) {
            boolean allowed = PermissionAPI.getPermission(executor, promotion ? WrbPermissions.RANK_UP : WrbPermissions.RANK_DOWN);
            if (!allowed) {
                throw NO_PERMISSION.create();
            }
        }

        return target.getCapability(WrbPlayerDataProvider.WRB_PLAYER_DATA_CAPABILITY).map(data -> {
            WrbRank current = data.getRank();
            WrbRank desired = WrbRank.fromString(rawRank).orElse(null);
            if (desired == null) {
                source.sendFailure(Component.literal("Неизвестное звание: " + rawRank));
                return 0;
            }

            // Проверяем правильность изменения ранга
            if (promotion && desired.ordinal() <= current.ordinal()) {
                source.sendFailure(Component.literal("Новое звание должно быть выше текущего."));
                return 0;
            }
            if (!promotion && desired.ordinal() >= current.ordinal()) {
                source.sendFailure(Component.literal("Новое звание должно быть ниже текущего."));
                return 0;
            }

            // Применяем изменения
            data.setRank(desired);
            WrbNetworking.sendToClient(new SyncWrbDataPacket(data), target);

            // Отправляем сообщения
            Component message = Component.literal("Звание игрока " + target.getName().getString() + " изменено на ").append(desired.getDisplayName());
            source.sendSuccess(() -> message, true);
            if (target != executor) {
                target.displayClientMessage(Component.literal("Ваше звание изменено на ").append(desired.getDisplayName()), false);
            }
            return 1;
        }).orElse(0);
    }
}