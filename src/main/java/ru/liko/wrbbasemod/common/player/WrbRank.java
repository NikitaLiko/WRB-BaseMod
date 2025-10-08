package ru.liko.wrbbasemod.common.player;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Represents the military ranks that are available to players.
 * The enum keeps the display component as well as a normalized identifier that can be used in commands.
 */
public enum WrbRank {
    PRIVATE("private", Component.literal("\u0420\u044F\u0434\u043E\u0432\u043E\u0439").withStyle(ChatFormatting.GRAY)),
    CORPORAL("corporal", Component.literal("\u0415\u0444\u0440\u0435\u0439\u0442\u043E\u0440").withStyle(ChatFormatting.BLUE)),
    SERGEANT("sergeant", Component.literal("\u0421\u0435\u0440\u0436\u0430\u043D\u0442").withStyle(ChatFormatting.DARK_AQUA)),
    STAFF_SERGEANT("staff_sergeant", Component.literal("\u0421\u0442\u0430\u0440\u0448\u0438\u0439\u0020\u0441\u0435\u0440\u0436\u0430\u043D\u0442").withStyle(ChatFormatting.GREEN)),
    SECOND_LIEUTENANT("second_lieutenant", Component.literal("\u041C\u043B\u0430\u0434\u0448\u0438\u0439\u0020\u043B\u0435\u0439\u0442\u0435\u043D\u0430\u043D\u0442").withStyle(ChatFormatting.GOLD)),
    LIEUTENANT("lieutenant", Component.literal("\u041B\u0435\u0439\u0442\u0435\u043D\u0430\u043D\u0442").withStyle(ChatFormatting.YELLOW)),
    CAPTAIN("captain", Component.literal("\u041A\u0430\u043F\u0438\u0442\u0430\u043D").withStyle(ChatFormatting.RED)),
    MAJOR("major", Component.literal("\u041C\u0430\u0439\u043E\u0440").withStyle(ChatFormatting.DARK_BLUE)),
    LIEUTENANT_COLONEL("lieutenant_colonel", Component.literal("\u041F\u043E\u0434\u043F\u043E\u043B\u043A\u043E\u0432\u043D\u0438\u043A").withStyle(ChatFormatting.BLUE)),
    COLONEL("colonel", Component.literal("\u041F\u043E\u043B\u043A\u043E\u0432\u043D\u0438\u043A").withStyle(ChatFormatting.DARK_RED)),
    MAJOR_GENERAL("major_general", Component.literal("\u0413\u0435\u043D\u0435\u0440\u0430\u043B\u002D\u041C\u0430\u0439\u043E\u0440").withStyle(ChatFormatting.DARK_GREEN)),
    COLONEL_GENERAL("colonel_general", Component.literal("\u0413\u0435\u043D\u0435\u0440\u0430\u043B\u002D\u041F\u043E\u043B\u043A\u043E\u0432\u043D\u0438\u043A").withStyle(ChatFormatting.DARK_PURPLE));

    private final String id;
    private final MutableComponent displayName;
    private final Set<String> aliases;

    WrbRank(String id, MutableComponent displayName) {
        this.id = id;
        this.displayName = displayName;
        this.aliases = Set.of(id, displayName.getString().toLowerCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_'));
    }

    public String getId() {
        return id;
    }

    public MutableComponent getDisplayName() {
        return displayName.copy();
    }

    /**
     * @return a dedicated key that is safe to store in persistent data.
     */
    public String getPersistenceKey() {
        return id;
    }

    /**
     * Parses the provided input to a rank. The method accepts english identifiers, russian names,
     * and ignores case as well as spaces and hyphens.
     */
    public static Optional<WrbRank> fromString(String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }
        String normalized = normalize(input);
        return Arrays.stream(values())
                .filter(rank -> rank.aliases.contains(normalized))
                .findFirst();
    }

    public static WrbRank fromKeyOrDefault(String key) {
        return fromString(key).orElse(PRIVATE);
    }

    private static String normalize(String raw) {
        return raw.toLowerCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_');
    }
}
