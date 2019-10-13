package com.massivecraft.factions.config.transition.oldclass.v0;

import org.bukkit.ChatColor;

public enum OldAccessV0 {
    ALLOW("Allow", ChatColor.GREEN),
    DENY("Deny", ChatColor.DARK_RED),
    UNDEFINED("Undefined", ChatColor.GRAY);

    private String name;
    private ChatColor color;

    OldAccessV0(String name, ChatColor color) {
        this.name = name;
        this.color = color;
    }

    /**
     * Case insensitive check for access.
     *
     * @param check check
     * @return access
     */
    public static OldAccessV0 fromString(String check) {
        for (OldAccessV0 access : values()) {
            if (access.name().equalsIgnoreCase(check)) {
                return access;
            }
        }

        return null;
    }

    public String getName() {
        return this.name;
    }

    public ChatColor getColor() {
        return color;
    }

    @Override
    public String toString() {
        return name();
    }
}
