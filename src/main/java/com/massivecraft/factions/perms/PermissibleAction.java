package com.massivecraft.factions.perms;

import com.massivecraft.factions.config.file.DefaultPermissionsConfig;
import com.massivecraft.factions.util.TL;
import com.massivecraft.factions.util.material.FactionMaterial;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum PermissibleAction {
    BUILD(DefaultPermissionsConfig.Permissions::getBuild, TL.PERM_BUILD, TL.PERM_SHORT_BUILD, "STONE"),
    DESTROY(DefaultPermissionsConfig.Permissions::getDestroy, TL.PERM_DESTROY, TL.PERM_SHORT_DESTROY, "WOODEN_PICKAXE"),
    PAINBUILD(DefaultPermissionsConfig.Permissions::getPainBuild, TL.PERM_PAINBUILD, TL.PERM_SHORT_PAINBUILD, "WOODEN_SWORD"),
    ITEM(DefaultPermissionsConfig.Permissions::getItem, TL.PERM_ITEM, TL.PERM_SHORT_ITEM, "ITEM_FRAME"),
    CONTAINER(DefaultPermissionsConfig.Permissions::getContainer, TL.PERM_CONTAINER, TL.PERM_SHORT_CONTAINER, "CHEST_MINECART"),
    BUTTON(DefaultPermissionsConfig.Permissions::getButton, TL.PERM_BUTTON, TL.PERM_SHORT_BUTTON, "STONE_BUTTON"),
    DOOR(DefaultPermissionsConfig.Permissions::getDoor, TL.PERM_DOOR, TL.PERM_SHORT_DOOR, "IRON_DOOR"),
    LEVER(DefaultPermissionsConfig.Permissions::getLever, TL.PERM_LEVER, TL.PERM_SHORT_LEVER, "LEVER"),
    PLATE(DefaultPermissionsConfig.Permissions::getPlate, TL.PERM_PLATE, TL.PERM_SHORT_PLATE, "STONE_PRESSURE_PLATE"),
    FROSTWALK(DefaultPermissionsConfig.Permissions::getFrostWalk, TL.PERM_FROSTWALK, TL.PERM_SHORT_FROSTWALK, "ICE"),
    INVITE(true, DefaultPermissionsConfig.Permissions::getInvite, TL.PERM_INVITE, TL.PERM_SHORT_INVITE, "FISHING_ROD"),
    KICK(true, DefaultPermissionsConfig.Permissions::getKick, TL.PERM_KICK, TL.PERM_SHORT_KICK, "LEATHER_BOOTS"),
    BAN(true, DefaultPermissionsConfig.Permissions::getBan, TL.PERM_BAN, TL.PERM_SHORT_BAN, "BARRIER"),
    PROMOTE(true, DefaultPermissionsConfig.Permissions::getPromote, TL.PERM_PROMOTE, TL.PERM_SHORT_PROMOTE, "ANVIL"),
    DISBAND(true, DefaultPermissionsConfig.Permissions::getDisband, TL.PERM_DISBAND, TL.PERM_SHORT_DISBAND, "BONE"),
    ECONOMY(true, DefaultPermissionsConfig.Permissions::getEconomy, TL.PERM_ECONOMY, TL.PERM_SHORT_ECONOMY, "GOLD_INGOT"),
    TERRITORY(true, DefaultPermissionsConfig.Permissions::getTerritory, TL.PERM_TERRITORY, TL.PERM_SHORT_TERRITORY, "GRASS_BLOCK"),
    OWNER(true, DefaultPermissionsConfig.Permissions::getOwner, TL.PERM_OWNER, TL.PERM_SHORT_OWNER, "FENCE_GATE"),
    HOME(DefaultPermissionsConfig.Permissions::getHome, TL.PERM_HOME, TL.PERM_SHORT_HOME, "TORCH"),
    SETHOME(true, DefaultPermissionsConfig.Permissions::getSetHome, TL.PERM_SETHOME, TL.PERM_SHORT_SETHOME, "COMPASS"),
    SETWARP(true, DefaultPermissionsConfig.Permissions::getSetWarp, TL.PERM_SETWARP, TL.PERM_SHORT_SETWARP, "END_PORTAL_FRAME"),
    TNTDEPOSIT(true, DefaultPermissionsConfig.Permissions::getTNTDeposit, TL.PERM_TNTDEPOSIT, TL.PERM_SHORT_TNTDEPOSIT, "TNT"),
    TNTWITHDRAW(true, DefaultPermissionsConfig.Permissions::getTNTWithdraw, TL.PERM_TNTWITHDRAW, TL.PERM_SHORT_TNTWITHDRAW, "TNT"),
    WARP(DefaultPermissionsConfig.Permissions::getWarp, TL.PERM_WARP, TL.PERM_SHORT_WARP, "ENDER_PEARL"),
    FLY(DefaultPermissionsConfig.Permissions::getFly, TL.PERM_FLY, TL.PERM_SHORT_FLY, "FEATHER"),
    ;

    private final boolean factionOnly;
    private final String materialName;
    private final TL desc;
    private final TL shortDesc;
    private Material material;
    private Function<DefaultPermissionsConfig.Permissions, DefaultPermissionsConfig.Permissions.FullPermInfo> fullFunction;
    private Function<DefaultPermissionsConfig.Permissions, DefaultPermissionsConfig.Permissions.FactionOnlyPermInfo> factionOnlyFunction;

    PermissibleAction(Function<DefaultPermissionsConfig.Permissions, DefaultPermissionsConfig.Permissions.FullPermInfo> fullFunction, TL desc, TL shortDesc, String materialName) {
        this.factionOnly = false;
        this.fullFunction = fullFunction;
        this.desc = desc;
        this.shortDesc = shortDesc;
        this.materialName = materialName;
    }

    PermissibleAction(boolean factionOnly, Function<DefaultPermissionsConfig.Permissions, DefaultPermissionsConfig.Permissions.FactionOnlyPermInfo> factionOnlyFunction, TL desc, TL shortDesc, String materialName) {
        this.factionOnly = factionOnly;
        if (this.factionOnly) {
            this.factionOnlyFunction = factionOnlyFunction;
        } else {
            throw new AssertionError("May only set factionOnly actions in this constructor");
        }
        this.desc = desc;
        this.shortDesc = shortDesc;
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

    public String getShortDescription() {
        return this.shortDesc.toString();
    }

    @Override
    public String toString() {
        return name();
    }

}
