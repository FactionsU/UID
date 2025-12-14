package dev.kitteh.factions.permissible;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.config.file.TranslationsConfig;
import dev.kitteh.factions.upgrade.Upgrade;
import dev.kitteh.factions.upgrade.Upgrades;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

/**
 * Default permissible actions.
 */
@SuppressWarnings("Convert2MethodRef")
@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public enum PermissibleActions implements PermissibleAction {
    BUILD(tl -> tl.getBuild(), tl -> tl.getBuildShort()),
    DESTROY(tl -> tl.getDestroy(), tl -> tl.getDestroyShort()),
    PAINBUILD(tl -> tl.getPainBuild(), tl -> tl.getPainBuildShort()),

    ITEM(tl -> tl.getItem(), tl -> tl.getItemShort()),

    CONTAINER(tl -> tl.getContainer(), tl -> tl.getContainerShort()),

    BUTTON(tl -> tl.getButton(), tl -> tl.getButtonShort()),
    DOOR(tl -> tl.getDoor(), tl -> tl.getDoorShort()),
    LEVER(tl -> tl.getLever(), tl -> tl.getLeverShort()),
    PLATE(tl -> tl.getPlate(), tl -> tl.getPlateShort()),

    FROSTWALK(tl -> tl.getFrostWalk(), tl -> tl.getFrostWalkShort()),

    INVITE(tl -> tl.getInvite(), tl -> tl.getInviteShort()),
    KICK(tl -> tl.getKick(), tl -> tl.getKickShort()),
    BAN(tl -> tl.getBan(), tl -> tl.getBanShort()),
    PROMOTE(tl -> tl.getPromote(), tl -> tl.getPromoteShort()),

    DISBAND(tl -> tl.getDisband(), tl -> tl.getDisbandShort()),

    ECONOMY(tl -> tl.getEconomy(), tl -> tl.getEconomyShort()),

    TERRITORY(tl -> tl.getTerritory(), tl -> tl.getTerritoryShort()),

    HOME(tl -> tl.getHome(), tl -> tl.getHomeShort()),
    SETHOME(tl -> tl.getSetHome(), tl -> tl.getSetHomeShort()),

    LISTCLAIMS(tl -> tl.getListClaims(), tl -> tl.getListClaimsShort()),

    WARP(tl -> tl.getWarp(), tl -> tl.getWarpShort()),
    SETWARP(tl -> tl.getSetWarp(), tl -> tl.getSetWarpShort()),

    TNTDEPOSIT(tl -> tl.getTntDeposit(), tl -> tl.getTntDepositShort()),
    TNTWITHDRAW(tl -> tl.getTntWithdraw(), tl -> tl.getTntWithdrawShort()),

    SHIELD(tl -> tl.getShield(), tl -> tl.getShieldShort(), Upgrades.SHIELD),

    FLY(tl -> tl.getFly(), tl -> tl.getFlyShort(), Upgrades.FLIGHT),

    UPGRADE(tl -> tl.getUpgrade(), tl -> tl.getUpgradeShort()),

    ZONE(tl -> tl.getZone(), tl -> tl.getZoneShort(), Upgrades.ZONES),
    ;

    private final String desc;
    private final String shortDesc;
    private @Nullable Upgrade prerequisite;

    PermissibleActions(Function<TranslationsConfig.Protection.Permissions, String> desc, Function<TranslationsConfig.Protection.Permissions, String> shortDesc) {
        this.desc = desc.apply(FactionsPlugin.instance().tl().protection().permissions());
        this.shortDesc = shortDesc.apply(FactionsPlugin.instance().tl().protection().permissions());
    }

    PermissibleActions(Function<TranslationsConfig.Protection.Permissions, String> desc, Function<TranslationsConfig.Protection.Permissions, String> shortDesc, Upgrade prerequisite) {
        this(desc, shortDesc);
        this.prerequisite = prerequisite;
    }

    @Override
    public String description() {
        return this.desc;
    }

    @Override
    public String shortDescription() {
        return this.shortDesc;
    }

    @Override
    public @Nullable Upgrade prerequisite() {
        return this.prerequisite;
    }

    @Override
    public String toString() {
        return name();
    }
}
