package com.massivecraft.factions.perms;

import com.massivecraft.factions.config.file.DefaultPermissionsConfig;
import com.massivecraft.factions.util.TL;
import com.massivecraft.factions.util.material.FactionMaterial;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum PermissibleAction {
    BUILD(DefaultPermissionsConfig.Permissions::getBuild, "STONE"),
    DESTROY(DefaultPermissionsConfig.Permissions::getDestroy, "WOODEN_PICKAXE"),
    PAINBUILD(DefaultPermissionsConfig.Permissions::getPainBuild, "WOODEN_SWORD"),
    ITEM(DefaultPermissionsConfig.Permissions::getItem, "ITEM_FRAME"),
    CONTAINER(DefaultPermissionsConfig.Permissions::getContainer, "CHEST_MINECART"),
    BUTTON(DefaultPermissionsConfig.Permissions::getButton, "STONE_BUTTON"),
    DOOR(DefaultPermissionsConfig.Permissions::getDoor, "IRON_DOOR"),
    LEVER(DefaultPermissionsConfig.Permissions::getLever, "LEVER"),
    PLATE(DefaultPermissionsConfig.Permissions::getPlate, "STONE_PRESSURE_PLATE"),
    FROSTWALK(DefaultPermissionsConfig.Permissions::getFrostWalk, "ICE"),
    INVITE(true, DefaultPermissionsConfig.Permissions::getInvite, "FISHING_ROD"),
    KICK(true, DefaultPermissionsConfig.Permissions::getKick, "LEATHER_BOOTS"),
    BAN(true, DefaultPermissionsConfig.Permissions::getBan, "BARRIER"),
    PROMOTE(true, DefaultPermissionsConfig.Permissions::getPromote, "ANVIL"),
    DISBAND(true, DefaultPermissionsConfig.Permissions::getDisband, "BONE"),
    ECONOMY(true, DefaultPermissionsConfig.Permissions::getEconomy, "GOLD_INGOT"),
    TERRITORY(true, DefaultPermissionsConfig.Permissions::getTerritory, "GRASS_BLOCK"),
    OWNER(true, DefaultPermissionsConfig.Permissions::getOwner, "FENCE_GATE"),
    HOME(DefaultPermissionsConfig.Permissions::getHome, "TORCH"),
    SETHOME(true, DefaultPermissionsConfig.Permissions::getSetHome, "COMPASS"),
    SETWARP(true, DefaultPermissionsConfig.Permissions::getSetWarp, "END_PORTAL_FRAME"),
    WARP(DefaultPermissionsConfig.Permissions::getWarp, "ENDER_PEARL"),
    FLY(DefaultPermissionsConfig.Permissions::getFly, "FEATHER"),
    ;

    private final boolean factionOnly;
    private final String materialName;
    private Material material;
    private Function<DefaultPermissionsConfig.Permissions, DefaultPermissionsConfig.Permissions.FullPermInfo> fullFunction;
    private Function<DefaultPermissionsConfig.Permissions, DefaultPermissionsConfig.Permissions.FactionOnlyPermInfo> factionOnlyFunction;

    PermissibleAction(Function<DefaultPermissionsConfig.Permissions, DefaultPermissionsConfig.Permissions.FullPermInfo> fullFunction, String materialName) {
        this.factionOnly = false;
        this.fullFunction = fullFunction;
        this.materialName = materialName;
    }

    PermissibleAction(boolean factionOnly, Function<DefaultPermissionsConfig.Permissions, DefaultPermissionsConfig.Permissions.FactionOnlyPermInfo> factionOnlyFunction, String materialName) {
        this.factionOnly = factionOnly;
        if (this.factionOnly) {
            this.factionOnlyFunction = factionOnlyFunction;
        } else {
            throw new AssertionError("May only set factionOnly actions in this constructor");
        }
        this.materialName = materialName;
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

    public Material getMaterial() {
        if (this.material == null) {
            this.material = FactionMaterial.from(this.materialName).get();
        }
        return this.material;
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
        switch (this) {
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
            case HOME:
                return TL.PERM_HOME.toString();
            case SETHOME:
                return TL.PERM_SETHOME.toString();
            case SETWARP:
                return TL.PERM_SETWARP.toString();
            case WARP:
                return TL.PERM_WARP.toString();
            case FLY:
                return TL.PERM_FLY.toString();
            case OWNER:
                return TL.PERM_OWNER.toString();
        }
        throw new AssertionError("No description available! Somebody forgot to run unit tests!");
    }

    @Override
    public String toString() {
        return name();
    }

}
