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

    @Deprecated
    static PermissibleAction valueOf(String name) {
        PermissibleAction action = PermissibleActionRegistry.get(name);
        if (action == null) {
            throw new IllegalArgumentException("Invalid name '" + name + "'");
        }
        return action;
    }
}
