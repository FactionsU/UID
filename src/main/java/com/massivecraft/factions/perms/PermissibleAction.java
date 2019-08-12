package com.massivecraft.factions.perms;

import com.massivecraft.factions.config.file.DefaultPermissionsConfig;
import com.massivecraft.factions.util.TL;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum PermissibleAction {
    BUILD(DefaultPermissionsConfig.Permissions::getBuild),
    DESTROY(DefaultPermissionsConfig.Permissions::getDestroy),
    PAINBUILD(DefaultPermissionsConfig.Permissions::getPainBuild),
    ITEM(DefaultPermissionsConfig.Permissions::getItem),
    CONTAINER(DefaultPermissionsConfig.Permissions::getContainer),
    BUTTON(DefaultPermissionsConfig.Permissions::getButton),
    DOOR(DefaultPermissionsConfig.Permissions::getDoor),
    LEVER(DefaultPermissionsConfig.Permissions::getLever),
    PLATE(DefaultPermissionsConfig.Permissions::getPlate),
    FROSTWALK(DefaultPermissionsConfig.Permissions::getFrostWalk),
    INVITE(true, DefaultPermissionsConfig.Permissions::getInvite),
    KICK(true, DefaultPermissionsConfig.Permissions::getKick),
    BAN(true, DefaultPermissionsConfig.Permissions::getBan),
    PROMOTE(true, DefaultPermissionsConfig.Permissions::getPromote),
    DISBAND(true, DefaultPermissionsConfig.Permissions::getDisband),
    ECONOMY(true, DefaultPermissionsConfig.Permissions::getEconomy),
    TERRITORY(true, DefaultPermissionsConfig.Permissions::getTerritory),
    SETHOME(true, DefaultPermissionsConfig.Permissions::getSetHome),
    SETWARP(true, DefaultPermissionsConfig.Permissions::getSetWarp),
    WARP(DefaultPermissionsConfig.Permissions::getWarp),
    FLY(DefaultPermissionsConfig.Permissions::getFly),
    ;

    private final boolean factionOnly;
    private Function<DefaultPermissionsConfig.Permissions, DefaultPermissionsConfig.Permissions.FullPermInfo> fullFunction;
    private Function<DefaultPermissionsConfig.Permissions, DefaultPermissionsConfig.Permissions.FactionOnlyPermInfo> factionOnlyFunction;

    PermissibleAction(Function<DefaultPermissionsConfig.Permissions, DefaultPermissionsConfig.Permissions.FullPermInfo> fullFunction) {
        this.factionOnly = false;
        this.fullFunction = fullFunction;
    }

    PermissibleAction(boolean factionOnly, Function<DefaultPermissionsConfig.Permissions, DefaultPermissionsConfig.Permissions.FactionOnlyPermInfo> factionOnlyFunction) {
        this.factionOnly = factionOnly;
        if (this.factionOnly) {
            this.factionOnlyFunction = factionOnlyFunction;
        } else {
            throw new AssertionError("May only set factionOnly actions in this constructor");
        }
    }

    private static Map<String, PermissibleAction> map = new HashMap<>();

    static {
        for (PermissibleAction action : values()) {
            map.put(action.name().toLowerCase(), action);
        }
    }

    public boolean isFactionOnly() {
        return this.factionOnly;
    }

    public DefaultPermissionsConfig.Permissions.FullPermInfo getFullPerm(DefaultPermissionsConfig.Permissions permissions) {
        return this.fullFunction.apply(permissions);
    }

    public DefaultPermissionsConfig.Permissions.FactionOnlyPermInfo getFactionOnly(DefaultPermissionsConfig.Permissions permissions) {
        return this.factionOnlyFunction.apply(permissions);
    }

    /**
     * Case insensitive check for action.
     *
     * @param check check
     * @return permissible
     */
    public static PermissibleAction fromString(String check) {
        return check == null ? null : map.get(check.toLowerCase());
    }

    public String getDescription() {
        switch(this) {
            case BUILD:
                return TL.PERM_BUILD.toString();
            case DESTROY:
                return TL.PERM_DESTROY.toString();
            case PAINBUILD:
                return TL.PERM_PAINBUILD.toString();
            case ITEM:
                return TL.PERM_ITEM.toString();
            case CONTAINER:
                return TL.PERM_CONTAINER.toString();
            case BUTTON:
                return TL.PERM_BUTTON.toString();
            case DOOR:
                return TL.PERM_DOOR.toString();
            case LEVER:
                return TL.PERM_LEVER.toString();
            case PLATE:
                return TL.PERM_PLATE.toString();
            case FROSTWALK:
                return TL.PERM_FROSTWALK.toString();
            case INVITE:
                return TL.PERM_INVITE.toString();
            case KICK:
                return TL.PERM_KICK.toString();
            case BAN:
                return TL.PERM_BAN.toString();
            case PROMOTE:
                return TL.PERM_PROMOTE.toString();
            case DISBAND:
                return TL.PERM_DISBAND.toString();
            case ECONOMY:
                return TL.PERM_ECONOMY.toString();
            case TERRITORY:
                return TL.PERM_TERRITORY.toString();
            case SETHOME:
                return TL.PERM_SETHOME.toString();
            case SETWARP:
                return TL.PERM_SETWARP.toString();
            case WARP:
                return TL.PERM_WARP.toString();
            case FLY:
                return TL.PERM_FLY.toString();
        }
        throw new AssertionError("No description available! Somebody forgot to run unit tests!");
    }

    @Override
    public String toString() {
        return name();
    }

}
