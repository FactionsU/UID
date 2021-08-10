package com.massivecraft.factions.perms;

import org.bukkit.ChatColor;

public interface Permissible extends Selectable {
    String name();

    ChatColor getColor();

    String getTranslation();
}
