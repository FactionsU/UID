package dev.kitteh.factions.util;

import dev.kitteh.factions.FactionsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.AvailableSince("4.0.0")
public class TextUtil {
    private TextUtil() {
    }

    @ApiStatus.Obsolete
    public static String getLegacyString(TextColor color) {
        if (color instanceof NamedTextColor namedTextColor) {
            ChatColor col = switch (namedTextColor.toString()) {
                case "black" -> ChatColor.BLACK;
                case "dark_blue" -> ChatColor.DARK_BLUE;
                case "dark_green" -> ChatColor.DARK_GREEN;
                case "dark_aqua" -> ChatColor.DARK_AQUA;
                case "dark_red" -> ChatColor.DARK_RED;
                case "dark_purple" -> ChatColor.DARK_PURPLE;
                case "gold" -> ChatColor.GOLD;
                case "gray" -> ChatColor.GRAY;
                case "dark_gray" -> ChatColor.DARK_GRAY;
                case "blue" -> ChatColor.BLUE;
                case "green" -> ChatColor.GREEN;
                case "aqua" -> ChatColor.AQUA;
                case "red" -> ChatColor.RED;
                case "light_purple" -> ChatColor.LIGHT_PURPLE;
                case "yellow" -> ChatColor.YELLOW;
                default -> ChatColor.WHITE;
            };
            return col.toString();
        }
        String hexed = String.format("%06x", color.value());
        final StringBuilder builder = new StringBuilder(ChatColor.COLOR_CHAR + "x");
        for (int x = 0; x < hexed.length(); x++) {
            builder.append(ChatColor.COLOR_CHAR).append(hexed.charAt(x));
        }
        return builder.toString();
    }

    @ApiStatus.Obsolete
    @Deprecated(forRemoval = true, since = "4.3.0")
    public static String parse(String str, Object... args) {
        return String.format(parse(str), args);
    }

    @ApiStatus.Obsolete
    @Deprecated(forRemoval = true, since = "4.3.0")
    public static String parse(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public static String upperCaseFirst(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    @ApiStatus.Obsolete
    @Deprecated(forRemoval = true, since = "4.3.0")
    public static String repeat(String s, int times) {
        return s.repeat(times);
    }

    @Deprecated(forRemoval = true, since = "4.5.0")
    public static String getMaterialName(Material material) {
        return material.toString().replace('_', ' ').toLowerCase();
    }

    @ApiStatus.AvailableSince("4.3.0")
    public static Component titleize(Component title, @Nullable Context ctx) {
        var tiTL = FactionsPlugin.instance().tl().placeholders().title();
        Component center = Mini.parse(tiTL.getTitleCenter(), Placeholder.component("content", title));
        int centerLen = PlainTextComponentSerializer.plainText().serialize(center).length();
        int sideLen = 26 - (centerLen / 2);

        String leftRepeat = tiTL.getLeftRepeat().repeat(sideLen / tiTL.getLeftRepeat().length());
        String rightRepeat = tiTL.getRightRepeat().repeat(sideLen / tiTL.getRightRepeat().length());

        TagResolver tagResolver = TagResolver.resolver(Placeholder.parsed("left_repeat", leftRepeat),
                Placeholder.parsed("right_repeat", rightRepeat),
                Placeholder.styling("left_color", c -> c.color(tiTL.getLeftColor())),
                Placeholder.styling("right_color", c -> c.color(tiTL.getRightColor())),
                Placeholder.component("center", center));

        if (ctx == null) {
            return Mini.parse(tiTL.getTitleMain(), tagResolver);
        } else {
            return ctx.deserialize(tiTL.getTitleMain(), tagResolver);
        }
    }

    @ApiStatus.AvailableSince("4.3.0")
    public static Component titleize(Component title) {
        return titleize(title, null);
    }

    @ApiStatus.Obsolete
    @Deprecated(forRemoval = true, since = "4.3.0")
    public static String titleizeLegacy(String str) {
        return Mini.toLegacy(titleize(str));
    }

    @ApiStatus.Obsolete
    @Deprecated(forRemoval = true, since = "4.3.0")
    public static Component titleize(String string) {
        return titleize(LegacyComponentSerializer.legacySection().deserialize(string));
    }
}
