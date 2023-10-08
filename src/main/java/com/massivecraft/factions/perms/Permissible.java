package com.massivecraft.factions.perms;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;

public interface Permissible extends Selectable {
    String name();

    ChatColor getColor();

    TextColor getTextColor();

    String getTranslation();
}
