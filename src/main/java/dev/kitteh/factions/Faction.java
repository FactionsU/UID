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
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
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
                return this.get(action.name());
            }

            /**
             * Gets this selector's permission state for the given action name.
             *
             * @param action action name, case-insensitive
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
                this.set(action.name(), state);
            }

            /**
             * Sets this selector's permission state for the given action name.
             *
             * @param action action, case-insensitive
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

        /**
         * Clears everything.
         */
        void clear();
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
         * Deletes a zone.
         *
         * @param name named zone
         */
        void delete(String name);

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
         * Gets an immutable list of all zones.
         *
         * @return zones
         */
        List<Zone> get();

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

        /**
         * Gets if the given player can manage this zone, meaning they have the ZONE permission for specifically this zone (if not overridden).
         *
         * @param fPlayer player
         * @return true if can manage
         */
        boolean canPlayerManage(FPlayer fPlayer);
    }

    int id();

    void addAnnouncement(FPlayer fPlayer, String msg);

    void sendUnreadAnnouncements(FPlayer fPlayer);

    Map<String, LazyLocation> warps();

    @Nullable
    LazyLocation warp(String name);

    default boolean isWarp(String name) {
        return warp(name) != null;
    }

    void createWarp(String name, LazyLocation loc);

    boolean hasWarpPassword(String warp);

    boolean isWarpPassword(String warp, String password);

    void setWarpPassword(String warp, String password);

    void removeWarpPassword(String warp);

    boolean removeWarp(String name);

    void clearWarps();

    int maxVaults();

    void maxVaults(int value);

    Set<UUID> invites();

    void invite(FPlayer fplayer);

    void deInvite(FPlayer fplayer);

    boolean hasInvite(FPlayer fplayer);

    void ban(FPlayer target, FPlayer banner);

    void unban(FPlayer player);

    boolean isBanned(FPlayer player);

    Set<BanInfo> bans();

    boolean open();

    void open(boolean isOpen);

    boolean peaceful();

    void peaceful(boolean isPeaceful);

    void peacefulExplosionsEnabled(boolean val);

    boolean peacefulExplosionsEnabled();

    default boolean noExplosionsInTerritory() {
        return this.isShielded() || (this.peaceful() && !this.peacefulExplosionsEnabled());
    }

    boolean permanent();

    void permanent(boolean isPermanent);

    String tag();

    void tag(String str);

    String tagString(@Nullable Faction otherFaction);

    String tagString(@Nullable FPlayer otherFplayer);

    String description();

    void description(String value);

    String link();

    void link(String value);

    void home(Location home);

    void delHome();

    default boolean hasHome() {
        return this.home() != null;
    }

    @Nullable
    Location home();

    Instant founded();

    void founded(Instant when);

    default boolean noPvPInTerritory() {
        return isSafeZone() || (peaceful() && FactionsPlugin.instance().conf().factions().specialCase().isPeacefulTerritoryDisablePVP());
    }

    default boolean noMonstersInTerritory() {
        return isSafeZone() || (peaceful() && FactionsPlugin.instance().conf().factions().specialCase().isPeacefulTerritoryDisableMonsters());
    }

    default boolean isNormal() {
        return !(this.isWilderness() || this.isSafeZone() || this.isWarZone());
    }

    default boolean isWilderness() {
        return this.id() == Factions.ID_WILDERNESS;
    }

    default boolean isSafeZone() {
        return this.id() == Factions.ID_SAFEZONE;
    }

    default boolean isWarZone() {
        return this.id() == Factions.ID_WARZONE;
    }

    void lastDeath(Instant time);

    Instant lastDeath();

    default int kills() {
        return members().stream().mapToInt(FPlayer::kills).sum();
    }

    default int deaths() {
        return members().stream().mapToInt(FPlayer::deaths).sum();
    }

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

    int tntBank();

    void tntBank(int amount);

    boolean isShielded();

    int upgradeLevel(Upgrade upgrade);

    void upgradeLevel(Upgrade upgrade, int level);

    Relation relationWish(Faction otherFaction);

    void relationWish(Faction otherFaction, Relation relation);

    default int relationCount(Relation relation) {
        int count = 0;
        for (Faction faction : Factions.factions().all()) {
            if (faction.relationTo(this) == relation) {
                count++;
            }
        }
        return count;
    }

    double dtr();

    double dtrWithoutUpdate();

    void dtr(double dtr);

    long dtrLastUpdated();

    long dtrFrozenUntil();

    void dtrFrozenUntil(long time);

    default boolean dtrFrozen() {
        return System.currentTimeMillis() < this.dtrFrozenUntil();
    }

    /**
     * Gets the exact faction power, which is not used for claim/raidability calculations
     *
     * @return exact power
     */
    double powerExact();

    /**
     * Gets the exact faction max power
     *
     * @return exactmax power
     */
    double powerMaxExact();

    /**
     * Gets the faction power, as used for claims/raidability calculations
     *
     * @return power
     */
    default int power() {
        return (int) Math.round(this.powerExact());
    }

    default int powerMax() {
        return (int) Math.round(this.powerMaxExact());
    }

    @Nullable
    Integer permanentPower();

    void permanentPower(@Nullable Integer permanentPower);

    default boolean hasPermanentPower() {
        return this.permanentPower() != null;
    }

    double powerBoost();

    void powerBoost(double powerBoost);

    default boolean hasLandInflation() {
        return FactionsPlugin.instance().landRaidControl().hasLandInflation(this);
    }

    boolean isPowerFrozen();

    int size();

    default int memberLimit() {
        int confMax = FactionsPlugin.instance().conf().factions().other().getFactionMemberLimit();
        if (confMax < 1) {
            return Integer.MAX_VALUE;
        }
        if (Universe.universe().isUpgradeEnabled(Upgrades.MAX_MEMBERS) && this.upgradeLevel(Upgrades.MAX_MEMBERS) > 0) {
            int boost = Universe.universe().upgradeSettings(Upgrades.MAX_MEMBERS).valueAt(Upgrades.Variables.POSITIVE_INCREASE, this.upgradeLevel(Upgrades.MAX_MEMBERS)).intValue();
            return confMax + boost;
        } else {
            return confMax;
        }
    }

    Set<FPlayer> members();

    Set<FPlayer> membersOnline(boolean online);

    Set<FPlayer> membersOnline(boolean online, @Nullable FPlayer viewer);

    @Nullable
    FPlayer admin();

    List<FPlayer> members(Role role);

    List<Player> membersOnlineAsPlayers();

    boolean hasMembersOnline();

    void trackMemberLoggedOff();

    void promoteNewLeader();

    Role defaultRole();

    void defaultRole(Role role);

    @Override
    default void sendMessage(@NonNull Component component) {
        for (FPlayer fplayer : this.membersOnline(true)) {
            fplayer.sendMessage(component);
        }
    }

    default void sendMessage(String message) {
        for (FPlayer fplayer : this.membersOnline(true)) {
            fplayer.sendMessage(message);
        }
    }

    default void sendMessage(List<String> messages) {
        for (FPlayer fplayer : this.membersOnline(true)) {
            fplayer.sendMessage(messages);
        }
    }

    default Set<FLocation> claims() {
        return Board.board().allClaims(this);
    }

    default int claimCount() {
        return Board.board().claimCount(this);
    }

    default int claimCount(World world) {
        return Board.board().claimCount(this, world);
    }

    /**
     * Gets the faction's zone controller.
     *
     * @return zones
     */
    Zones zones();
}
