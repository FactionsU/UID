package dev.kitteh.factions;

import dev.kitteh.factions.permissible.PermSelector;
import dev.kitteh.factions.permissible.PermState;
import dev.kitteh.factions.permissible.PermissibleAction;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.permissible.Selectable;
import dev.kitteh.factions.upgrade.Upgrade;
import dev.kitteh.factions.upgrade.Upgrades;
import dev.kitteh.factions.util.BanInfo;
import dev.kitteh.factions.util.LazyLocation;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@NullMarked
public interface Faction extends Participator, Selectable {
    interface Permissions {
        interface SelectorPerms {
            /**
             * Gets this selector's permission state for the given action.
             *
             * @param action action
             * @return perm state
             */
            default PermState get(PermissibleAction action) {
                return this.get(action.getName());
            }

            /**
             * Gets this selector's permission state for the given action name.
             *
             * @param action action name
             * @return perm state
             */
            PermState get(String action);

            /**
             * Sets this selector's permission state for the given action.
             *
             * @param action action
             * @param state  perm state
             */
            default void set(PermissibleAction action, PermState state) {
                this.set(action.getName(), state);
            }

            /**
             * Sets this selector's permission state for the given action name.
             *
             * @param action action
             * @param state  perm state
             */
            void set(String action, PermState state);

            /**
             * Gets all action names with set permission states.
             * May include names not presently registered with the plugin.
             *
             * @return actions
             */
            Collection<String> actions();
        }

        /**
         * Gets an immutable, ordered list of the selectors tracked.
         *
         * @return the selectors
         */
        List<PermSelector> selectors();

        /**
         * Moves the given selector up in the order.
         *
         * @param selector selector to move up
         */
        void moveSelectorUp(PermSelector selector);

        /**
         * Moves the given selector down in the order.
         *
         * @param selector selector to move down
         */
        void moveSelectorDown(PermSelector selector);

        /**
         * Gets a selector's perms.
         *
         * @param selector selector to get
         * @return perms
         * @throws IllegalArgumentException if this selector is not tracked
         */
        SelectorPerms get(PermSelector selector);

        /**
         * Gets if a given selector is tracked.
         *
         * @param selector selector
         * @return true if tracked
         */
        boolean has(PermSelector selector);

        /**
         * Adds a selector. If an already added selector is provided, functions the same as {@link #get(PermSelector)}.
         *
         * @param selector selector
         * @return the selector perms for immediate editing
         */
        SelectorPerms add(PermSelector selector);

        /**
         * Removes a selector.
         *
         * @param selector selector to remove
         */
        void remove(PermSelector selector);
    }

    /**
     * Controller for the faction's zones.
     */
    interface Zones {
        /**
         * Creates a zone.
         *
         * @param name zone name
         * @return new zone
         * @throws IllegalArgumentException if the name is already in use
         */
        Zone create(String name);

        /**
         * Gets the zone at a given location.
         *
         * @param fLocation location
         * @return zone or default zone if not this faction's territory
         */
        Zone get(FLocation fLocation);

        /**
         * Gets a zone by name.
         *
         * @param name zone name
         * @return matching zone or null for no match
         */
        @Nullable
        Zone get(String name);

        /**
         * Gets the main, default zone.
         *
         * @return main zone
         */
        Zone main();

        /**
         * Sets the zone for a given location
         *
         * @param zone      zone
         * @param fLocation location
         * @throws IllegalArgumentException if the zone is not an active zone for this faction or if the location is not owned by the faction
         */
        void set(Zone zone, FLocation fLocation);
    }

    /**
     * An individual zone.
     */
    interface Zone {
        /**
         * Gets the zone's internal ID.
         *
         * @return zone id
         */
        int id();

        /**
         * Gets the zone's name.
         *
         * @return zone name
         */
        String name();

        /**
         * Sets the zone's name.
         *
         * @param name new name
         */
        void name(String name);

        /**
         * Gets the zone's greeting.
         *
         * @return greeting
         */
        Component greeting();

        /**
         * Gets the zone's raw MiniMessage greeting.
         *
         * @return MiniMessage greeting or null if the zone uses the main zone's greeting
         */
        @Nullable
        String greetingString();

        /**
         * Sets the zone's greeting in MiniMessage format. Takes the 'tag' placeholder to inject the faction tag.
         * If null, will default to the main zone's greeting.
         *
         * @param greeting new greeting
         * @throws IllegalArgumentException if sending null for the main zone
         */
        void greeting(@Nullable String greeting);

        /**
         * Gets the zone's permissions.
         *
         * @return zone permissions
         */
        Permissions permissions();
    }

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

    void removeWarpPassword(String warp);

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

    boolean isPeacefulExplosionsEnabled();

    default boolean noExplosionsInTerritory() {
        return this.isShielded() || (this.isPeaceful() && !this.isPeacefulExplosionsEnabled());
    }

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

    Permissions permissions();

    int getLandRounded();

    int getLandRoundedInWorld(String worldName);

    int getTNTBank();

    void setTNTBank(int amount);

    boolean isShielded();

    int getUpgradeLevel(Upgrade upgrade);

    void setUpgradeLevel(Upgrade upgrade, int level);

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

    default int getMaxMembers() {
        int confMax = FactionsPlugin.getInstance().conf().factions().other().getFactionMemberLimit();
        if (confMax < 1) {
            return Integer.MAX_VALUE;
        }
        if (Universe.getInstance().isUpgradeEnabled(Upgrades.MAX_MEMBERS) && this.getUpgradeLevel(Upgrades.MAX_MEMBERS) > 0) {
            int boost = Universe.getInstance().getUpgradeSettings(Upgrades.MAX_MEMBERS).valueAt(Upgrades.Variables.POSITIVE_INCREASE, this.getUpgradeLevel(Upgrades.MAX_MEMBERS)).intValue();
            return confMax + boost;
        } else {
            return confMax;
        }
    }

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

    @Override
    default void sendMessage(@NonNull Component component) {
        for (FPlayer fplayer : this.getFPlayersWhereOnline(true)) {
            fplayer.sendMessage(component);
        }
    }

    void sendMessage(String message);

    void sendMessage(List<String> messages);

    Set<FLocation> getAllClaims();

    /**
     * Gets the faction's zone controller.
     *
     * @return zones
     */
    Zones zones();
}
