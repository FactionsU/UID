package com.massivecraft.factions.perms;

import com.massivecraft.factions.config.annotation.ConfigName;

public class PermissiblePermDefaultInfo {
    public static PermissiblePermDefaultInfo defaultFalse() {
        return new PermissiblePermDefaultInfo(false, false, true);
    }

    public static PermissiblePermDefaultInfo defaultTrue() {
        return new PermissiblePermDefaultInfo(true, false, true);
    }

    public boolean isLocked() {
        return this.locked;
    }

    public boolean defaultAllowed() {
        return this.defaultAllowed;
    }

    public boolean shouldShowInGame() {
        return this.visibleInGame;
    }

    public PermissiblePermDefaultInfo(boolean defaultAllowed, boolean locked, boolean visibleInGame) {
        this.defaultAllowed = defaultAllowed;
        this.locked = locked;
        this.visibleInGame = visibleInGame;
    }

    private boolean locked = false;
    @ConfigName("default")
    private boolean defaultAllowed = false;
    private boolean visibleInGame = true;
}
