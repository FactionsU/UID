package com.massivecraft.factions.util.particle;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public class ParticleColor {

    private final Color color;

    private final float red;
    private final float green;
    private final float blue;

    ParticleColor(Color color) {
        this.color = color;
        this.red = color.getRed();
        this.green = color.getGreen();
        this.blue = color.getBlue();
    }

    public float getOffsetX() {
        if (red == 0) {
            return Float.MIN_VALUE;
        }
        return red / 255;
    }

    public float getOffsetY() {
        return green / 255;
    }

    public float getOffsetZ() {
        return blue / 255;
    }

    // Why Spigot?
    public static ParticleColor fromChatColor(ChatColor chatColor) {
        return switch (chatColor) {
            case AQUA -> new ParticleColor(Color.AQUA);
            case BLACK -> new ParticleColor(Color.BLACK);
            case BLUE, DARK_AQUA, DARK_BLUE -> new ParticleColor(Color.BLUE);
            case DARK_GRAY, GRAY -> new ParticleColor(Color.GRAY);
            case DARK_GREEN -> new ParticleColor(Color.GREEN);
            case DARK_PURPLE, LIGHT_PURPLE -> new ParticleColor(Color.PURPLE);
            case DARK_RED, RED -> new ParticleColor(Color.RED);
            case GOLD, YELLOW -> new ParticleColor(Color.YELLOW);
            case GREEN -> new ParticleColor(Color.LIME);
            case WHITE -> new ParticleColor(Color.WHITE);
            default -> null;
        };

    }

    public Color getColor() {
        return color;
    }
}
