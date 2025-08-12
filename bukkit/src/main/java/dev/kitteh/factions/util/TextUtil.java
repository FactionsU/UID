package dev.kitteh.factions.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;

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
    public static String parse(String str, Object... args) {
        return String.format(parse(str), args);
    }

    @ApiStatus.Obsolete
    public static String parse(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public static String upperCaseFirst(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static String repeat(String s, int times) {
        if (times <= 0) {
            return "";
        } else {
            return s + repeat(s, times - 1);
        }
    }

    public static String getMaterialName(Material material) {
        return material.toString().replace('_', ' ').toLowerCase();
    }

    private final static String titleizeLine = repeat("_", 52);
    private final static int titleizeBalance = -1;

    @ApiStatus.Obsolete
    public static String titleizeLegacy(String str) {
        String center = ".[ " + ChatColor.DARK_GREEN + str + ChatColor.GOLD + " ].";
        int centerlen = ChatColor.stripColor(center).length();
        int pivot = titleizeLine.length() / 2;
        int eatLeft = (centerlen / 2) - titleizeBalance;
        int eatRight = (centerlen - eatLeft) + titleizeBalance;

        if (eatLeft < pivot) {
            return ChatColor.GOLD + titleizeLine.substring(0, pivot - eatLeft) + center + titleizeLine.substring(pivot + eatRight);
        } else {
            return ChatColor.GOLD + center;
        }
    }

    public static Component titleize(String string) {
        String str = MiniMessage.miniMessage().serialize(LegacyComponentSerializer.legacySection().deserialize(string));
        String center = ".[ <dark_green>" + str + "<gold> ].";
        int centerLen = ChatColor.stripColor(Mini.toLegacy(Mini.parse(center))).length();
        int pivot = titleizeLine.length() / 2;
        int eatLeft = (centerLen / 2) - titleizeBalance;
        int eatRight = (centerLen - eatLeft) + titleizeBalance;

        if (eatLeft < pivot) {
            return Mini.parse("<gold>" + titleizeLine.substring(0, pivot - eatLeft) + center + titleizeLine.substring(pivot + eatRight));
        } else {
            return Mini.parse("<gold>" + center);
        }
    }
}
