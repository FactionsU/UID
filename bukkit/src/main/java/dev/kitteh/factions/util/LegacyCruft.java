package dev.kitteh.factions.util;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;

public class LegacyCruft {
    private LegacyCruft() {}

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
}
