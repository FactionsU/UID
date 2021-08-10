package com.massivecraft.factions.perms;

import com.massivecraft.factions.util.TL;
import com.massivecraft.factions.util.material.MaterialDb;
import org.bukkit.Material;

public enum PermissibleActions implements PermissibleAction {
    BUILD(TL.PERM_BUILD, TL.PERM_SHORT_BUILD, "STONE"),
    DESTROY(TL.PERM_DESTROY, TL.PERM_SHORT_DESTROY, "WOODEN_PICKAXE"),
    PAINBUILD(TL.PERM_PAINBUILD, TL.PERM_SHORT_PAINBUILD, "WOODEN_SWORD"),
    ITEM(TL.PERM_ITEM, TL.PERM_SHORT_ITEM, "ITEM_FRAME"),
    CONTAINER(TL.PERM_CONTAINER, TL.PERM_SHORT_CONTAINER, "CHEST_MINECART"),
    BUTTON(TL.PERM_BUTTON, TL.PERM_SHORT_BUTTON, "STONE_BUTTON"),
    DOOR(TL.PERM_DOOR, TL.PERM_SHORT_DOOR, "IRON_DOOR"),
    LEVER(TL.PERM_LEVER, TL.PERM_SHORT_LEVER, "LEVER"),
    PLATE(TL.PERM_PLATE, TL.PERM_SHORT_PLATE, "STONE_PRESSURE_PLATE"),
    FROSTWALK(TL.PERM_FROSTWALK, TL.PERM_SHORT_FROSTWALK, "ICE"),
    INVITE(TL.PERM_INVITE, TL.PERM_SHORT_INVITE, "FISHING_ROD"),
    KICK(TL.PERM_KICK, TL.PERM_SHORT_KICK, "LEATHER_BOOTS"),
    BAN(TL.PERM_BAN, TL.PERM_SHORT_BAN, "BARRIER"),
    PROMOTE(TL.PERM_PROMOTE, TL.PERM_SHORT_PROMOTE, "ANVIL"),
    DISBAND(TL.PERM_DISBAND, TL.PERM_SHORT_DISBAND, "BONE"),
    ECONOMY(TL.PERM_ECONOMY, TL.PERM_SHORT_ECONOMY, "GOLD_INGOT"),
    TERRITORY(TL.PERM_TERRITORY, TL.PERM_SHORT_TERRITORY, "GRASS_BLOCK"),
    OWNER(TL.PERM_OWNER, TL.PERM_SHORT_OWNER, "FENCE_GATE"),
    HOME(TL.PERM_HOME, TL.PERM_SHORT_HOME, "TORCH"),
    SETHOME(TL.PERM_SETHOME, TL.PERM_SHORT_SETHOME, "COMPASS"),
    LISTCLAIMS(TL.PERM_LISTCLAIMS, TL.PERM_SHORT_LISTCLAIMS, "MAP"),
    SETWARP(TL.PERM_SETWARP, TL.PERM_SHORT_SETWARP, "END_PORTAL_FRAME"),
    TNTDEPOSIT(TL.PERM_TNTDEPOSIT, TL.PERM_SHORT_TNTDEPOSIT, "TNT"),
    TNTWITHDRAW(TL.PERM_TNTWITHDRAW, TL.PERM_SHORT_TNTWITHDRAW, "TNT"),
    WARP(TL.PERM_WARP, TL.PERM_SHORT_WARP, "ENDER_PEARL"),
    FLY(TL.PERM_FLY, TL.PERM_SHORT_FLY, "FEATHER"),
    ;

    private final boolean factionOnly;
    private final String materialName;
    private final TL desc;
    private final TL shortDesc;
    private Material material;

    PermissibleActions(TL desc, TL shortDesc, String materialName) {
        this.factionOnly = false;
        this.desc = desc;
        this.shortDesc = shortDesc;
        this.materialName = materialName;
    }

    public boolean isFactionOnly() {
        return this.factionOnly;
    }

    @Override
    public Material getMaterial() {
        if (this.material == null) {
            this.material = MaterialDb.get(this.materialName, Material.STONE);
        }
        return this.material;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public String getDescription() {
        return this.desc.toString();
    }

    @Override
    public String getShortDescription() {
        return this.shortDesc.toString();
    }

    @Override
    public String toString() {
        return name();
    }
}
