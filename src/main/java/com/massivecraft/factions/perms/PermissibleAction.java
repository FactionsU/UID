package com.massivecraft.factions.perms;

import com.massivecraft.factions.config.file.DefaultPermissionsConfig;
import com.massivecraft.factions.util.TL;
import com.massivecraft.factions.util.material.FactionMaterial;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum PermissibleAction {
    BUILD(DefaultPermissionsConfig.Permissions::getBuild, TL.PERM_BUILD, "STONE"),
    DESTROY(DefaultPermissionsConfig.Permissions::getDestroy, TL.PERM_DESTROY, "WOODEN_PICKAXE"),
    PAINBUILD(DefaultPermissionsConfig.Permissions::getPainBuild, TL.PERM_PAINBUILD, "WOODEN_SWORD"),
    ITEM(DefaultPermissionsConfig.Permissions::getItem, TL.PERM_ITEM, "ITEM_FRAME"),
    CONTAINER(DefaultPermissionsConfig.Permissions::getContainer, TL.PERM_CONTAINER, "CHEST_MINECART"),
    BUTTON(DefaultPermissionsConfig.Permissions::getButton, TL.PERM_BUTTON, "STONE_BUTTON"),
    DOOR(DefaultPermissionsConfig.Permissions::getDoor, TL.PERM_DOOR, "IRON_DOOR"),
    LEVER(DefaultPermissionsConfig.Permissions::getLever, TL.PERM_LEVER, "LEVER"),
    PLATE(DefaultPermissionsConfig.Permissions::getPlate, TL.PERM_PLATE, "STONE_PRESSURE_PLATE"),
    FROSTWALK(DefaultPermissionsConfig.Permissions::getFrostWalk, TL.PERM_FROSTWALK, "ICE"),
    INVITE(true, DefaultPermissionsConfig.Permissions::getInvite, TL.PERM_INVITE, "FISHING_ROD"),
    KICK(true, DefaultPermissionsConfig.Permissions::getKick, TL.PERM_KICK, "LEATHER_BOOTS"),
    BAN(true, DefaultPermissionsConfig.Permissions::getBan, TL.PERM_BAN, "BARRIER"),
    PROMOTE(true, DefaultPermissionsConfig.Permissions::getPromote, TL.PERM_PROMOTE, "ANVIL"),
    DISBAND(true, DefaultPermissionsConfig.Permissions::getDisband, TL.PERM_DISBAND, "BONE"),
    ECONOMY(true, DefaultPermissionsConfig.Permissions::getEconomy, TL.PERM_ECONOMY, "GOLD_INGOT"),
    TERRITORY(true, DefaultPermissionsConfig.Permissions::getTerritory, TL.PERM_TERRITORY, "GRASS_BLOCK"),
    OWNER(true, DefaultPermissionsConfig.Permissions::getOwner, TL.PERM_OWNER, "FENCE_GATE"),
    HOME(DefaultPermissionsConfig.Permissions::getHome, TL.PERM_HOME, "TORCH"),
    SETHOME(true, DefaultPermissionsConfig.Permissions::getSetHome, TL.PERM_SETHOME, "COMPASS"),
    SETWARP(true, DefaultPermissionsConfig.Permissions::getSetWarp, TL.PERM_SETWARP, "END_PORTAL_FRAME"),
    TNTDEPOSIT(true, DefaultPermissionsConfig.Permissions::getTNTDeposit, TL.PERM_TNTDEPOSIT, "TNT"),
    TNTWITHDRAW(true, DefaultPermissionsConfig.Permissions::getTNTWithdraw, TL.PERM_TNTWITHDRAW, "TNT"),
    WARP(DefaultPermissionsConfig.Permissions::getWarp, TL.PERM_WARP, "ENDER_PEARL"),
    FLY(DefaultPermissionsConfig.Permissions::getFly, TL.PERM_FLY, "FEATHER"),
    ;

    private final boolean factionOnly;
    private final String materialName;
    private final TL desc;
    private Material material;
    private Function<DefaultPermissionsConfig.Permissions, DefaultPermissionsConfig.Permissions.FullPermInfo> fullFunction;
    private Function<DefaultPermissionsConfig.Permissions, DefaultPermissionsConfig.Permissions.FactionOnlyPermInfo> factionOnlyFunction;

    PermissibleAction(Function<DefaultPermissionsConfig.Permissions, DefaultPermissionsConfig.Permissions.FullPermInfo> fullFunction, TL desc, String materialName) {
        this.factionOnly = false;
        this.fullFunction = fullFunction;
        this.desc = desc;
        this.materialName = materialName;
    }

    PermissibleAction(boolean factionOnly, Function<DefaultPermissionsConfig.Permissions, DefaultPermissionsConfig.Permissions.FactionOnlyPermInfo> factionOnlyFunction, TL desc, String materialName) {
        this.factionOnly = factionOnly;
        if (this.factionOnly) {
            this.factionOnlyFunction = factionOnlyFunction;
        } else {
            throw new AssertionError("May only set factionOnly actions in this constructor");
        }
        this.desc = desc;
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
        return this.desc.toString();
    }

    @Override
    public String toString() {
        return name();
    }

}
