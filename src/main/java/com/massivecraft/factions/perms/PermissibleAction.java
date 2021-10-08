package com.massivecraft.factions.perms;

import org.bukkit.Material;

public interface PermissibleAction {
    @Deprecated
    boolean isFactionOnly();

    @Deprecated
    Material getMaterial();

    String getName();

    String getDescription();

    String getShortDescription();
}
