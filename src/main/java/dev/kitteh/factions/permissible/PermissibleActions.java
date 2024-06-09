package dev.kitteh.factions.permissible;

import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.material.MaterialDb;
import org.bukkit.Material;

public enum PermissibleActions implements PermissibleAction {
    BUILD(TL.PERM_BUILD, TL.PERM_SHORT_BUILD),
    DESTROY(TL.PERM_DESTROY, TL.PERM_SHORT_DESTROY),
    PAINBUILD(TL.PERM_PAINBUILD, TL.PERM_SHORT_PAINBUILD),
    ITEM(TL.PERM_ITEM, TL.PERM_SHORT_ITEM),
    CONTAINER(TL.PERM_CONTAINER, TL.PERM_SHORT_CONTAINER),
    BUTTON(TL.PERM_BUTTON, TL.PERM_SHORT_BUTTON),
    DOOR(TL.PERM_DOOR, TL.PERM_SHORT_DOOR),
    LEVER(TL.PERM_LEVER, TL.PERM_SHORT_LEVER),
    PLATE(TL.PERM_PLATE, TL.PERM_SHORT_PLATE),
    FROSTWALK(TL.PERM_FROSTWALK, TL.PERM_SHORT_FROSTWALK),
    INVITE(TL.PERM_INVITE, TL.PERM_SHORT_INVITE),
    KICK(TL.PERM_KICK, TL.PERM_SHORT_KICK),
    BAN(TL.PERM_BAN, TL.PERM_SHORT_BAN),
    PROMOTE(TL.PERM_PROMOTE, TL.PERM_SHORT_PROMOTE),
    DISBAND(TL.PERM_DISBAND, TL.PERM_SHORT_DISBAND),
    ECONOMY(TL.PERM_ECONOMY, TL.PERM_SHORT_ECONOMY),
    TERRITORY(TL.PERM_TERRITORY, TL.PERM_SHORT_TERRITORY),
    OWNER(TL.PERM_OWNER, TL.PERM_SHORT_OWNER),
    HOME(TL.PERM_HOME, TL.PERM_SHORT_HOME),
    SETHOME(TL.PERM_SETHOME, TL.PERM_SHORT_SETHOME),
    LISTCLAIMS(TL.PERM_LISTCLAIMS, TL.PERM_SHORT_LISTCLAIMS),
    SETWARP(TL.PERM_SETWARP, TL.PERM_SHORT_SETWARP),
    TNTDEPOSIT(TL.PERM_TNTDEPOSIT, TL.PERM_SHORT_TNTDEPOSIT),
    TNTWITHDRAW(TL.PERM_TNTWITHDRAW, TL.PERM_SHORT_TNTWITHDRAW),
    WARP(TL.PERM_WARP, TL.PERM_SHORT_WARP),
    FLY(TL.PERM_FLY, TL.PERM_SHORT_FLY),
    ;

    private final TL desc;
    private final TL shortDesc;

    PermissibleActions(TL desc, TL shortDesc) {
        this.desc = desc;
        this.shortDesc = shortDesc;
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
