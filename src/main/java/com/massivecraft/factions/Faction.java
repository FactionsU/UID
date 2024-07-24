package com.massivecraft.factions;

import com.massivecraft.factions.iface.EconomyParticipator;
import com.massivecraft.factions.perms.PermissibleAction;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.perms.Selectable;
import com.massivecraft.factions.struct.BanInfo;
import com.massivecraft.factions.util.LazyLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Faction extends EconomyParticipator, Selectable {
    Map<String, List<String>> getAnnouncements();

    Map<String, LazyLocation> getWarps();

    LazyLocation getWarp(String name);

    void setWarp(String name, LazyLocation loc);

    boolean isWarp(String name);

    boolean hasWarpPassword(String warp);

    boolean isWarpPassword(String warp, String password);

    void setWarpPassword(String warp, String password);

    boolean removeWarp(String name);

    void clearWarps();

    int getMaxVaults();

    void setMaxVaults(int value);

    void addAnnouncement(FPlayer fPlayer, String msg);

    void sendUnreadAnnouncements(FPlayer fPlayer);

    void removeAnnouncements(FPlayer fPlayer);

    Set<String> getInvites();

    @Deprecated
    String getId();

    int getIntId();

    void invite(FPlayer fplayer);

    void deinvite(FPlayer fplayer);

    boolean isInvited(FPlayer fplayer);

    void ban(FPlayer target, FPlayer banner);

    void unban(FPlayer player);

    boolean isBanned(FPlayer player);

    Set<BanInfo> getBannedPlayers();

    boolean getOpen();

    void setOpen(boolean isOpen);

    boolean isPeaceful();

    void setPeaceful(boolean isPeaceful);

    void setPeacefulExplosionsEnabled(boolean val);

    boolean getPeacefulExplosionsEnabled();

    boolean noExplosionsInTerritory();

    boolean isPermanent();

    void setPermanent(boolean isPermanent);

    String getTag();

    String getTag(String prefix);

    String getTag(Faction otherFaction);

    String getTag(FPlayer otherFplayer);

    void setTag(String str);

    String getComparisonTag();

    String getDescription();

    void setDescription(String value);

    String getLink();

    void setLink(String value);

    void setHome(Location home);

    void delHome();

    boolean hasHome();

    Location getHome();

    long getFoundedDate();

    void setFoundedDate(long newDate);

    void confirmValidHome();

    boolean noPvPInTerritory();

    boolean noMonstersInTerritory();

    boolean isNormal();

    /**
     * Players in the wilderness faction are consdiered not in a faction.
     *
     * @return true if wilderness
     * @deprecated use {@link #isWilderness()} instead
     */
    @Deprecated
    default boolean isNone() {
        return isWilderness();
    }

    boolean isWilderness();

    boolean isSafeZone();

    boolean isWarZone();

    boolean isPlayerFreeType();

    void setLastDeath(long time);

    int getKills();

    int getDeaths();

    /**
     * Get the access of a selectable for a given chunk.
     *
     * @param selectable        selectable
     * @param permissibleAction permissible
     * @param location          location
     * @return player's access
     */
    boolean hasAccess(Selectable selectable, PermissibleAction permissibleAction, FLocation location);

    int getLandRounded();

    int getLandRoundedInWorld(String worldName);

    int getTNTBank();

    void setTNTBank(int amount);

    // -------------------------------
    // Relation and relation colors
    // -------------------------------

    Relation getRelationWish(Faction otherFaction);

    void setRelationWish(Faction otherFaction, Relation relation);

    int getRelationCount(Relation relation);

    // ----------------------------------------------//
    // DTR
    // ----------------------------------------------//

    double getDTR();

    double getDTRWithoutUpdate();

    void setDTR(double dtr);

    long getLastDTRUpdateTime();

    long getFrozenDTRUntilTime();

    void setFrozenDTR(long time);

    boolean isFrozenDTR();

    // ----------------------------------------------//
    // Power
    // ----------------------------------------------//
    double getPower();

    double getPowerMax();

    int getPowerRounded();

    int getPowerMaxRounded();

    Integer getPermanentPower();

    void setPermanentPower(Integer permanentPower);

    boolean hasPermanentPower();

    double getPowerBoost();

    void setPowerBoost(double powerBoost);

    boolean hasLandInflation();

    boolean isPowerFrozen();

    // -------------------------------
    // FPlayers
    // -------------------------------

    // maintain the reference list of FPlayers in this faction
    void refreshFPlayers();

    boolean addFPlayer(FPlayer fplayer);

    boolean removeFPlayer(FPlayer fplayer);

    int getSize();

    Set<FPlayer> getFPlayers();

    Set<FPlayer> getFPlayersWhereOnline(boolean online);

    Set<FPlayer> getFPlayersWhereOnline(boolean online, FPlayer viewer);

    FPlayer getFPlayerAdmin();

    List<FPlayer> getFPlayersWhereRole(Role role);

    List<Player> getOnlinePlayers();

    // slightly faster check than getOnlinePlayers() if you just want to see if
    // there are any players online
    boolean hasPlayersOnline();

    void memberLoggedOff();

    // used when current leader is about to be removed from the faction;
    // promotes new leader, or disbands faction if no other members left
    void promoteNewLeader();

    Role getDefaultRole();

    void setDefaultRole(Role role);

    void sendMessage(String message);

    void sendMessage(List<String> messages);

    // ----------------------------------------------//
    // Ownership of specific claims
    // ----------------------------------------------//

    Map<FLocation, Set<String>> getClaimOwnership();

    void clearAllClaimOwnership();

    void clearClaimOwnership(FLocation loc);

    void clearClaimOwnership(FPlayer player);

    int getCountOfClaimsWithOwners();

    boolean doesLocationHaveOwnersSet(FLocation loc);

    boolean isPlayerInOwnerList(FPlayer player, FLocation loc);

    void setPlayerAsOwner(FPlayer player, FLocation loc);

    void removePlayerAsOwner(FPlayer player, FLocation loc);

    Set<String> getOwnerList(FLocation loc);

    String getOwnerListString(FLocation loc);

    boolean playerHasOwnershipRights(FPlayer fplayer, FLocation loc);

    // ----------------------------------------------//
    // Persistance and entity management
    // ----------------------------------------------//
    void remove();

    Set<FLocation> getAllClaims();
}
