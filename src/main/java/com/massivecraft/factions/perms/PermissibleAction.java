package com.massivecraft.factions.perms;

import org.bukkit.Material;

public interface PermissibleAction {
    boolean isFactionOnly();

    Material getMaterial();

    String getName();

    String getDescription();

    String getShortDescription();

    PermissiblePermDefaultInfo getDefaultPermInfo(boolean online, Permissible permissible);
}
