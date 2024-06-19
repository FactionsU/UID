package dev.kitteh.factions;

import dev.kitteh.factions.permissible.PermissibleAction;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.permissible.Selectable;
import dev.kitteh.factions.util.BanInfo;
import dev.kitteh.factions.util.LazyLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@NullMarked
public interface Faction extends Participator, Selectable {
    int getId();

    Map<UUID, List<String>> getAnnouncements();

    void addAnnouncement(FPlayer fPlayer, String msg);

    void sendUnreadAnnouncements(FPlayer fPlayer);

    void removeAnnouncements(FPlayer fPlayer);

    Map<String, LazyLocation> getWarps();

    @Nullable
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

    Set<UUID> getInvites();

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

    String getTag(@Nullable Faction otherFaction);

    String getTag(@Nullable FPlayer otherFplayer);

    void setTag(String str);

    String getComparisonTag();

    String getDescription();

    void setDescription(String value);

    String getLink();

    void setLink(String value);

    void setHome(Location home);

    void delHome();

    boolean hasHome();

    @Nullable
    Location getHome();

    long getFoundedDate();

    void setFoundedDate(long newDate);

    void confirmValidHome();

    boolean noPvPInTerritory();

    boolean noMonstersInTerritory();

    boolean isNormal();

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
    boolean hasAccess(Selectable selectable, PermissibleAction permissibleAction, @Nullable FLocation location);

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

    /**
     * Gets the exact faction power, which is not used for claim/raidability calculations
     *
     * @return exact power
     */
    double getPowerExact();

    /**
     * Gets the exact faction max power
     *
     * @return exactmax power
     */
    double getPowerMaxExact();

    /**
     * Gets the faction power, as used for claims/raidability calculations
     *
     * @return
     */
    int getPower();

    int getPowerMax();

    @Nullable
    Integer getPermanentPower();

    void setPermanentPower(@Nullable Integer permanentPower);

    boolean hasPermanentPower();

    double getPowerBoost();

    void setPowerBoost(double powerBoost);

    boolean hasLandInflation();

    boolean isPowerFrozen();

    // -------------------------------
    // FPlayers
    // -------------------------------

    boolean addFPlayer(FPlayer fplayer);

    boolean removeFPlayer(FPlayer fplayer);

    int getSize();

    Set<FPlayer> getFPlayers();

    Set<FPlayer> getFPlayersWhereOnline(boolean online);

    Set<FPlayer> getFPlayersWhereOnline(boolean online, @Nullable FPlayer viewer);

    @Nullable
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

    Map<FLocation, Set<UUID>> getClaimOwnership();

    void clearAllClaimOwnership();

    void clearClaimOwnership(FLocation loc);

    void clearClaimOwnership(FPlayer player);

    int getCountOfClaimsWithOwners();

    boolean doesLocationHaveOwnersSet(FLocation loc);

    boolean isPlayerInOwnerList(FPlayer player, FLocation loc);

    void setPlayerAsOwner(FPlayer player, FLocation loc);

    void removePlayerAsOwner(FPlayer player, FLocation loc);

    Set<UUID> getOwnerList(FLocation loc);

    String getOwnerListString(FLocation loc);

    boolean playerHasOwnershipRights(FPlayer fplayer, FLocation loc);

    Set<FLocation> getAllClaims();
}
