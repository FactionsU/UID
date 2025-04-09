package dev.kitteh.factions.data;

import com.google.gson.annotations.Expose;
import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.config.file.PermissionsConfig;
import dev.kitteh.factions.event.FactionAutoDisbandEvent;
import dev.kitteh.factions.event.FactionNewAdminEvent;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.integration.LWC;
import dev.kitteh.factions.landraidcontrol.DTRControl;
import dev.kitteh.factions.landraidcontrol.LandRaidControl;
import dev.kitteh.factions.permissible.PermSelector;
import dev.kitteh.factions.permissible.PermissibleAction;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.permissible.Selectable;
import dev.kitteh.factions.util.BanInfo;
import dev.kitteh.factions.util.LazyLocation;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@NullMarked
public abstract class MemoryFaction implements Faction {
    protected int id;
    protected boolean peacefulExplosionsEnabled;
    protected boolean permanent;
    protected String tag;
    protected String description;
    protected @Nullable String link;
    protected boolean open;
    protected boolean peaceful;
    protected @Nullable Integer permanentPower;
    protected @Nullable LazyLocation home;
    protected long foundedDate;
    protected transient long lastPlayerLoggedOffTime;
    protected double powerBoost;
    protected Map<Integer, Relation> relationWish = new HashMap<>();
    protected Map<FLocation, Set<UUID>> claimOwnership = new ConcurrentHashMap<>();
    @Expose(deserialize = false)
    transient Set<FPlayer> fplayers = new HashSet<>();
    protected Set<UUID> invites = new HashSet<>();
    protected HashMap<UUID, List<String>> announcements = new HashMap<>();
    protected ConcurrentHashMap<String, LazyLocation> warps = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<String, String> warpPasswords = new ConcurrentHashMap<>();
    private long lastDeath;
    protected int maxVaults;
    protected Role defaultRole;
    protected LinkedHashMap<PermSelector, Map<String, Boolean>> permissions = new LinkedHashMap<>();
    protected Set<BanInfo> bans = new HashSet<>();
    protected double dtr;
    protected long lastDTRUpdateTime;
    protected long frozenDTRUntilTime;
    protected int tntBank;
    protected transient @Nullable OfflinePlayer offlinePlayer;

    public void cleanupDeserialization() {
        this.fplayers = new HashSet<>();
        this.offlinePlayer = null;
        this.getOfflinePlayer();
    }

    @Override
    public HashMap<UUID, List<String>> getAnnouncements() {
        return this.announcements;
    }

    @Override
    public void addAnnouncement(FPlayer fPlayer, String msg) {
        announcements.computeIfAbsent(fPlayer.getUniqueId(), k -> new ArrayList<>()).add(msg);
    }

    @Override
    public void sendUnreadAnnouncements(FPlayer fPlayer) {
        List<String> ann = announcements.remove(fPlayer.getUniqueId());
        if (ann == null) {
            return;
        }
        fPlayer.msg(TL.FACTIONS_ANNOUNCEMENT_TOP);
        for (String s : ann) {
            fPlayer.sendMessage(s);
        }
        fPlayer.msg(TL.FACTIONS_ANNOUNCEMENT_BOTTOM);
    }

    @Override
    public void removeAnnouncements(FPlayer fPlayer) {
        announcements.remove(fPlayer.getUniqueId());
    }

    @Override
    public ConcurrentHashMap<String, LazyLocation> getWarps() {
        return this.warps;
    }

    @Override
    public LazyLocation getWarp(String name) {
        return this.warps.get(name);
    }

    @Override
    public void setWarp(String name, LazyLocation loc) {
        this.warps.put(name, loc);
    }

    @Override
    public boolean isWarp(String name) {
        return this.warps.containsKey(name);
    }

    @Override
    public boolean removeWarp(String name) {
        warpPasswords.remove(name); // remove password no matter what.
        return warps.remove(name) != null;
    }

    @Override
    public boolean isWarpPassword(String warp, String password) {
        return hasWarpPassword(warp) && warpPasswords.get(warp.toLowerCase()).equals(password);
    }

    @Override
    public boolean hasWarpPassword(String warp) {
        return warpPasswords.containsKey(warp.toLowerCase());
    }

    @Override
    public void setWarpPassword(String warp, String password) {
        warpPasswords.put(warp.toLowerCase(), password);
    }

    @Override
    public void removeWarpPassword(String warp) {
        warpPasswords.remove(warp.toLowerCase());
    }

    @Override
    public void clearWarps() {
        warps.clear();
    }

    @Override
    public int getMaxVaults() {
        return this.maxVaults;
    }

    @Override
    public void setMaxVaults(int value) {
        this.maxVaults = value;
    }

    @Override
    public Set<UUID> getInvites() {
        return invites;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        this.offlinePlayer = null;
    }

    @Override
    public void invite(FPlayer fplayer) {
        this.invites.add(fplayer.getUniqueId());
    }

    @Override
    public void deinvite(FPlayer fplayer) {
        this.invites.remove(fplayer.getUniqueId());
    }

    @Override
    public boolean isInvited(FPlayer fplayer) {
        return this.invites.contains(fplayer.getUniqueId());
    }

    @Override
    public void ban(FPlayer target, FPlayer banner) {
        BanInfo info = new BanInfo(banner.getUniqueId(), target.getUniqueId(), System.currentTimeMillis());
        this.bans.add(info);
    }

    @Override
    public void unban(FPlayer player) {
        bans.removeIf(banInfo -> banInfo.banned().equals(player.getUniqueId()));
    }

    @Override
    public boolean isBanned(FPlayer player) {
        for (BanInfo info : bans) {
            if (info.banned().equals(player.getUniqueId())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Set<BanInfo> getBannedPlayers() {
        return this.bans;
    }

    @Override
    public boolean getOpen() {
        return open;
    }

    @Override
    public void setOpen(boolean isOpen) {
        open = isOpen;
    }

    @Override
    public boolean isPeaceful() {
        return this.peaceful;
    }

    @Override
    public void setPeaceful(boolean isPeaceful) {
        this.peaceful = isPeaceful;
    }

    @Override
    public void setPeacefulExplosionsEnabled(boolean val) {
        peacefulExplosionsEnabled = val;
    }

    @Override
    public boolean getPeacefulExplosionsEnabled() {
        return this.peacefulExplosionsEnabled;
    }

    @Override
    public boolean noExplosionsInTerritory() {
        return this.peaceful && !peacefulExplosionsEnabled;
    }

    @Override
    public boolean isPermanent() {
        return permanent || !this.isNormal();
    }

    @Override
    public void setPermanent(boolean isPermanent) {
        permanent = isPermanent;
    }

    @Override
    public String getTag() {
        return this.tag;
    }

    @Override
    public String getTag(String prefix) {
        return prefix + this.tag;
    }

    @Override
    public String getTag(@Nullable Faction otherFaction) {
        if (otherFaction == null) {
            return getTag();
        }
        return this.getTag(this.getColorStringTo(otherFaction));
    }

    @Override
    public String getTag(@Nullable FPlayer otherFplayer) {
        if (otherFplayer == null) {
            return getTag();
        }
        return this.getTag(this.getColorStringTo(otherFplayer));
    }

    @Override
    public void setTag(String str) {
        if (FactionsPlugin.getInstance().conf().factions().other().isTagForceUpperCase()) {
            str = str.toUpperCase();
        }
        this.tag = str;
    }

    @Override
    public String getComparisonTag() {
        return MiscUtil.getComparisonString(this.tag);
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String value) {
        this.description = value;
    }

    @Override
    public String getLink() {
        if (this.link == null) {
            this.link = FactionsPlugin.getInstance().conf().commands().link().getDefaultURL();
        }
        return this.link;
    }

    @Override
    public void setLink(String value) {
        this.link = value;
    }

    @Override
    public void setHome(Location home) {
        this.home = new LazyLocation(home);
    }

    @Override
    public void delHome() {
        this.home = null;
    }

    @Override
    public boolean hasHome() {
        return this.getHome() != null;
    }

    @Override
    public @Nullable Location getHome() {
        confirmValidHome();
        return (this.home != null) ? this.home.getLocation() : null;
    }

    @Override
    public long getFoundedDate() {
        if (this.foundedDate == 0) {
            setFoundedDate(System.currentTimeMillis());
        }
        return this.foundedDate;
    }

    @Override
    public void setFoundedDate(long newDate) {
        this.foundedDate = newDate;
    }

    @Override
    public void confirmValidHome() {
        if ((!FactionsPlugin.getInstance().conf().factions().homes().isMustBeInClaimedTerritory()) || (this.home == null) || (Board.getInstance().getFactionAt(new FLocation(this.home)) == this)) {
            return;
        }

        msg(TL.FACTION_HOME_UNSET);
        this.home = null;
    }

    @Override
    public String getAccountId() {
        return "faction-" + this.getId();
    }

    @Override
    public OfflinePlayer getOfflinePlayer() {
        if (this.offlinePlayer == null) {
            this.offlinePlayer = FactionsPlugin.getInstance().getFactionOfflinePlayer(this.getAccountId());
        }
        return this.offlinePlayer;
    }

    @Override
    public void setLastDeath(long time) {
        this.lastDeath = time;
    }

    public long getLastDeath() {
        return this.lastDeath;
    }

    @Override
    public int getKills() {
        int kills = 0;
        for (FPlayer fp : getFPlayers()) {
            kills += fp.getKills();
        }

        return kills;
    }

    @Override
    public int getDeaths() {
        int deaths = 0;
        for (FPlayer fp : getFPlayers()) {
            deaths += fp.getDeaths();
        }

        return deaths;
    }

    // -------------------------------------------- //
    // F Permissions stuff
    // -------------------------------------------- //

    @Override
    public boolean hasAccess(Selectable selectable, PermissibleAction permissibleAction, @Nullable FLocation location) {
        //noinspection ConstantValue
        if (selectable == null || permissibleAction == null) {
            return false; // Fail in a safe way
        }

        if (selectable == Role.ADMIN || (selectable instanceof FPlayer && ((FPlayer) selectable).getFaction() == this && ((FPlayer) selectable).getRole() == Role.ADMIN)) {
            return true;
        }

        PermissionsConfig permConf = FactionsPlugin.getInstance().getConfigManager().getPermissionsConfig();
        List<PermSelector> priority = permConf.getOverridePermissionsOrder().stream().filter(s -> s.test(selectable, this)).toList();
        for (PermSelector selector : priority) {
            Boolean bool = permConf.getOverridePermissions().get(selector).get(permissibleAction.getName());
            if (bool != null) {
                //FactionsPlugin.getInstance().debug("Permissions for " + selectable.getClass().getSimpleName() + ' ' + (selectable instanceof FPlayer ? ((FPlayer) selectable).getName() : (selectable instanceof Faction) ? ((Faction) selectable).getTag() : "") + " override " + selector.serialize() + ": " + bool);
                return bool;
            }
        }

        if (location != null) {
            // TODO migrate ownership stuffs
        }

        for (Map.Entry<PermSelector, Map<String, Boolean>> entry : permissions.entrySet()) {
            if (entry.getKey().test(selectable, this)) {
                Boolean bool = entry.getValue().get(permissibleAction.getName());
                if (bool != null) {
                    //FactionsPlugin.getInstance().debug("Permissions for " + selectable.getClass().getSimpleName() + ' ' + (selectable instanceof FPlayer ? ((FPlayer) selectable).getName() : (selectable instanceof Faction) ? ((Faction) selectable).getTag() : "") + " override " + entry.getKey().serialize() + ": " + bool);
                    return bool;
                }
            }
        }

        return false;
    }

    public LinkedHashMap<PermSelector, Map<String, Boolean>> getPermissions() {
        return permissions;
    }

    public void setPermissions(LinkedHashMap<PermSelector, Map<String, Boolean>> permissions) {
        this.permissions = permissions;
    }

    public void checkPerms() {
        //noinspection ConstantValue
        if (this.permissions == null || this.permissions.isEmpty()) {
            this.resetPerms();
        }
    }

    public void resetPerms() {
        //noinspection ConstantValue
        if (permissions == null) {
            permissions = new LinkedHashMap<>();
        } else {
            permissions.clear();
        }

        PermissionsConfig permConf = FactionsPlugin.getInstance().getConfigManager().getPermissionsConfig();
        for (PermSelector selector : permConf.getDefaultPermissionsOrder()) {
            Map<String, Boolean> map = new LinkedHashMap<>(permConf.getDefaultPermissions().get(selector));
            permissions.put(selector, map);
        }
    }

    @Override
    public Role getDefaultRole() {
        return this.defaultRole;
    }

    @Override
    public void setDefaultRole(Role role) {
        this.defaultRole = role;
    }

    // -------------------------------------------- //
    // Construct
    // -------------------------------------------- //
    public MemoryFaction(int id) {
        this.id = id;
        this.open = FactionsPlugin.getInstance().conf().factions().other().isNewFactionsDefaultOpen();
        this.tag = "???";
        this.description = TL.GENERIC_DEFAULTDESCRIPTION.toString();
        this.lastPlayerLoggedOffTime = 0;
        this.peaceful = FactionsPlugin.getInstance().conf().factions().other().isNewFactionsDefaultPeaceful();
        this.peacefulExplosionsEnabled = false;
        this.permanent = false;
        this.powerBoost = 0.0;
        this.foundedDate = System.currentTimeMillis();
        this.maxVaults = FactionsPlugin.getInstance().conf().playerVaults().getDefaultMaxVaults();
        this.defaultRole = FactionsPlugin.getInstance().conf().factions().other().getDefaultRole();
        this.dtr = FactionsPlugin.getInstance().conf().factions().landRaidControl().dtr().getStartingDTR();

        resetPerms(); // Reset on new Faction so it has default values.
    }

    // -------------------------------------------- //
    // Extra Getters And Setters
    // -------------------------------------------- //
    @Override
    public boolean noPvPInTerritory() {
        return isSafeZone() || (peaceful && FactionsPlugin.getInstance().conf().factions().specialCase().isPeacefulTerritoryDisablePVP());
    }

    @Override
    public boolean noMonstersInTerritory() {
        return isSafeZone() ||
                (peaceful && FactionsPlugin.getInstance().conf().factions().specialCase().isPeacefulTerritoryDisableMonsters());
    }

    // -------------------------------
    // Understand the type
    // -------------------------------

    @Override
    public boolean isNormal() {
        return !(this.isWilderness() || this.isSafeZone() || this.isWarZone());
    }

    @Override
    public boolean isWilderness() {
        return this.id == Factions.ID_WILDERNESS;
    }

    @Override
    public boolean isSafeZone() {
        return this.id == Factions.ID_SAFEZONE;
    }

    @Override
    public boolean isWarZone() {
        return this.id == Factions.ID_WARZONE;
    }

    @Override
    public boolean isPlayerFreeType() {
        return this.isSafeZone() || this.isWarZone();
    }

    // -------------------------------
    // Relation and relation colors
    // -------------------------------

    @Override
    public Relation getRelationWish(Faction otherFaction) {
        if (this.relationWish.containsKey(otherFaction.getId())) {
            return this.relationWish.get(otherFaction.getId());
        }
        return FactionsPlugin.getInstance().conf().factions().other().getDefaultRelation();
    }

    @Override
    public void setRelationWish(Faction otherFaction, Relation relation) {
        if (this.relationWish.containsKey(otherFaction.getId()) && relation.equals(Relation.NEUTRAL)) {
            this.relationWish.remove(otherFaction.getId());
        } else {
            this.relationWish.put(otherFaction.getId(), relation);
        }
    }

    @Override
    public int getRelationCount(Relation relation) {
        int count = 0;
        for (Faction faction : Factions.getInstance().getAllFactions()) {
            if (faction.getRelationTo(this) == relation) {
                count++;
            }
        }
        return count;
    }

    // ----------------------------------------------//
    // DTR
    // ----------------------------------------------//

    @Override
    public double getDTR() {
        LandRaidControl lrc = FactionsPlugin.getInstance().getLandRaidControl();
        if (lrc instanceof DTRControl) {
            ((DTRControl) lrc).updateDTR(this);
        }
        return this.dtr;
    }

    @Override
    public double getDTRWithoutUpdate() {
        return this.dtr;
    }

    @Override
    public void setDTR(double dtr) {
        double start = this.dtr;
        this.dtr = dtr;
        this.lastDTRUpdateTime = System.currentTimeMillis();
        if (start != this.dtr && FactionsPlugin.getInstance().getLandRaidControl() instanceof DTRControl) {
            ((DTRControl) FactionsPlugin.getInstance().getLandRaidControl()).onDTRChange(this, start, this.dtr);
        }
    }

    @Override
    public long getLastDTRUpdateTime() {
        return this.lastDTRUpdateTime;
    }

    @Override
    public long getFrozenDTRUntilTime() {
        return this.frozenDTRUntilTime;
    }

    @Override
    public void setFrozenDTR(long time) {
        this.frozenDTRUntilTime = time;
    }

    @Override
    public boolean isFrozenDTR() {
        return System.currentTimeMillis() < this.frozenDTRUntilTime;
    }

    // ----------------------------------------------//
    // Power
    // ----------------------------------------------//
    @Override
    public double getPowerExact() {
        if (this.permanentPower != null) {
            return this.permanentPower;
        }

        double ret = 0;
        for (FPlayer fplayer : fplayers) {
            ret += fplayer.getPower();
        }
        if (FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getFactionMax() > 0 && ret > FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getFactionMax()) {
            ret = FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getFactionMax();
        }
        return ret + this.powerBoost;
    }

    @Override
    public double getPowerMaxExact() {
        if (this.permanentPower != null) {
            return this.permanentPower;
        }

        double ret = 0;
        for (FPlayer fplayer : fplayers) {
            ret += fplayer.getPowerMax();
        }
        if (FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getFactionMax() > 0 && ret > FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getFactionMax()) {
            ret = FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getFactionMax();
        }
        return ret + this.powerBoost;
    }

    @Override
    public int getPower() {
        return (int) Math.round(this.getPowerExact());
    }

    @Override
    public int getPowerMax() {
        return (int) Math.round(this.getPowerMaxExact());
    }

    @Override
    public boolean hasLandInflation() {
        return FactionsPlugin.getInstance().getLandRaidControl().hasLandInflation(this);
    }

    @Override
    public @Nullable Integer getPermanentPower() {
        return this.permanentPower;
    }

    @Override
    public void setPermanentPower(@Nullable Integer permanentPower) {
        this.permanentPower = permanentPower;
    }

    @Override
    public boolean hasPermanentPower() {
        return this.permanentPower != null;
    }

    @Override
    public double getPowerBoost() {
        return this.powerBoost;
    }

    @Override
    public void setPowerBoost(double powerBoost) {
        this.powerBoost = powerBoost;
    }

    @Override
    public boolean isPowerFrozen() {
        int freezeSeconds = FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getPowerFreeze();
        return freezeSeconds != 0 && System.currentTimeMillis() - lastDeath < freezeSeconds * 1000L;
    }

    @Override
    public int getLandRounded() {
        return Board.getInstance().getFactionCoordCount(this);
    }

    @Override
    public int getLandRoundedInWorld(String worldName) {
        return Board.getInstance().getFactionCoordCountInWorld(this, worldName);
    }

    @Override
    public int getTNTBank() {
        return this.tntBank;
    }

    @Override
    public void setTNTBank(int amount) {
        this.tntBank = amount;
    }

    // -------------------------------
    // FPlayers
    // -------------------------------

    @Override
    public boolean addFPlayer(FPlayer fplayer) {
        return !this.isPlayerFreeType() && fplayers.add(fplayer);
    }

    @Override
    public boolean removeFPlayer(FPlayer fplayer) {
        return !this.isPlayerFreeType() && fplayers.remove(fplayer);
    }

    @Override
    public int getSize() {
        return fplayers.size();
    }

    @Override
    public Set<FPlayer> getFPlayers() {
        // return a shallow copy of the FPlayer list, to prevent tampering and
        // concurrency issues
        return new HashSet<>(fplayers);
    }

    @Override
    public Set<FPlayer> getFPlayersWhereOnline(boolean online) {
        Set<FPlayer> ret = new HashSet<>();
        if (!this.isNormal()) {
            return ret;
        }

        for (FPlayer fplayer : fplayers) {
            if (fplayer.isOnline() == online) {
                ret.add(fplayer);
            }
        }

        return ret;
    }

    @Override
    public Set<FPlayer> getFPlayersWhereOnline(boolean online, @Nullable FPlayer viewer) {
        if (viewer == null) {
            return getFPlayersWhereOnline(online);
        }
        Set<FPlayer> ret = new HashSet<>();
        if (!this.isNormal()) {
            return ret;
        }

        for (FPlayer viewed : fplayers) {
            // Add if their online status is what we want
            if (viewed.isOnline() == online) {
                // If we want online, check to see if we are able to see this player
                // This checks if they are in vanish.
                if (online
                        && viewed.getPlayer() != null
                        && viewer.getPlayer() != null
                        && viewer.getPlayer().canSee(viewed.getPlayer())) {
                    ret.add(viewed);
                    // If we want offline, just add them.
                    // Prob a better way to do this but idk.
                } else if (!online) {
                    ret.add(viewed);
                }
            }
        }

        return ret;
    }

    @Override
    public @Nullable FPlayer getFPlayerAdmin() {
        if (!this.isNormal()) {
            return null;
        }

        for (FPlayer fplayer : fplayers) {
            if (fplayer.getRole() == Role.ADMIN) {
                return fplayer;
            }
        }
        return null;
    }

    @Override
    public ArrayList<FPlayer> getFPlayersWhereRole(Role role) {
        ArrayList<FPlayer> ret = new ArrayList<>();
        if (!this.isNormal()) {
            return ret;
        }

        for (FPlayer fplayer : fplayers) {
            if (fplayer.getRole() == role) {
                ret.add(fplayer);
            }
        }

        return ret;
    }

    @Override
    public ArrayList<Player> getOnlinePlayers() {
        ArrayList<Player> ret = new ArrayList<>();
        if (this.isPlayerFreeType()) {
            return ret;
        }

        for (Player player : FactionsPlugin.getInstance().getServer().getOnlinePlayers()) {
            FPlayer fplayer = FPlayers.getInstance().getByPlayer(player);
            if (fplayer.getFaction() == this) {
                ret.add(player);
            }
        }

        return ret;
    }

    // slightly faster check than getOnlinePlayers() if you just want to see if
    // there are any players online
    @Override
    public boolean hasPlayersOnline() {
        // only real factions can have players online, not safe zone / war zone
        if (this.isPlayerFreeType()) {
            return false;
        }

        for (Player player : FactionsPlugin.getInstance().getServer().getOnlinePlayers()) {
            FPlayer fplayer = FPlayers.getInstance().getByPlayer(player);
            if (fplayer.getFaction() == this) {
                return true;
            }
        }

        // even if all players are technically logged off, maybe someone was on
        // recently enough to not consider them officially offline yet
        return FactionsPlugin.getInstance().conf().factions().other().getConsiderFactionsReallyOfflineAfterXMinutes() > 0 && System.currentTimeMillis() < lastPlayerLoggedOffTime + (FactionsPlugin.getInstance().conf().factions().other().getConsiderFactionsReallyOfflineAfterXMinutes() * 60000);
    }

    @Override
    public void memberLoggedOff() {
        if (this.isNormal()) {
            lastPlayerLoggedOffTime = System.currentTimeMillis();
        }
    }

    // used when current leader is about to be removed from the faction;
    // promotes new leader, or disbands faction if no other members left
    @Override
    public void promoteNewLeader() {
        if (!this.isNormal()) {
            return;
        }
        if (this.isPermanent() && FactionsPlugin.getInstance().conf().factions().specialCase().isPermanentFactionsDisableLeaderPromotion()) {
            return;
        }

        FPlayer oldLeader = this.getFPlayerAdmin();

        // get list of coleaders, or mods, or list of normal members if there are no moderators
        ArrayList<FPlayer> replacements = this.getFPlayersWhereRole(Role.COLEADER);
        if (replacements.isEmpty()) {
            replacements = this.getFPlayersWhereRole(Role.MODERATOR);
        }

        if (replacements.isEmpty()) {
            replacements = this.getFPlayersWhereRole(Role.NORMAL);
        }

        if (replacements.isEmpty()) { // faction admin  is the only  member; one-man  faction
            if (this.isPermanent()) {
                if (oldLeader != null) {
                    oldLeader.setRole(Role.NORMAL);
                }
                return;
            }

            // no members left and faction isn't permanent, so disband it
            if (FactionsPlugin.getInstance().conf().logging().isFactionDisband()) {
                FactionsPlugin.getInstance().log("The faction " + this.getTag() + " (" + this.getId() + ") has been disbanded since it has no members left.");
            }

            for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
                fplayer.msg(TL.LEAVE_DISBANDED, this.getTag(fplayer));
            }

            FactionsPlugin.getInstance().getServer().getPluginManager().callEvent(new FactionAutoDisbandEvent(this));

            Factions.getInstance().removeFaction(this);
        } else { // promote new faction admin
            Bukkit.getServer().getPluginManager().callEvent(new FactionNewAdminEvent(replacements.getFirst(), this));

            if (oldLeader != null) {
                oldLeader.setRole(Role.COLEADER);
            }
            replacements.getFirst().setRole(Role.ADMIN);
            this.msg(TL.FACTION_NEWLEADER, oldLeader == null ? "" : oldLeader.getName(), replacements.getFirst().getName());
            FactionsPlugin.getInstance().log("Faction " + this.getTag() + " (" + this.getId() + ") admin was removed. Replacement admin: " + replacements.getFirst().getName());
        }
    }

    // ----------------------------------------------//
    // Messages
    // ----------------------------------------------//
    @Override
    public void msg(String message, Object... args) {
        message = FactionsPlugin.getInstance().txt().parse(message, args);

        for (FPlayer fplayer : this.getFPlayersWhereOnline(true)) {
            fplayer.sendMessage(message);
        }
    }

    @Override
    public void sendMessage(String message) {
        for (FPlayer fplayer : this.getFPlayersWhereOnline(true)) {
            fplayer.sendMessage(message);
        }
    }

    @Override
    public void sendMessage(List<String> messages) {
        for (FPlayer fplayer : this.getFPlayersWhereOnline(true)) {
            fplayer.sendMessage(messages);
        }
    }

    // ----------------------------------------------//
    // Ownership of specific claims
    // ----------------------------------------------//

    @Override
    public Map<FLocation, Set<UUID>> getClaimOwnership() {
        return claimOwnership;
    }

    @Override
    public void clearAllClaimOwnership() {
        claimOwnership.clear();
    }

    @Override
    public void clearClaimOwnership(FLocation loc) {
        if (LWC.getEnabled() && FactionsPlugin.getInstance().conf().lwc().isResetLocksOnUnclaim()) {
            LWC.clearAllLocks(loc);
        }
        claimOwnership.remove(loc);
    }

    @Override
    public void clearClaimOwnership(FPlayer player) {
        Set<UUID> ownerData;

        for (Entry<FLocation, Set<UUID>> entry : claimOwnership.entrySet()) {
            ownerData = entry.getValue();

            if (ownerData == null) {
                continue;
            }

            ownerData.remove(player.getUniqueId());

            if (ownerData.isEmpty()) {
                if (LWC.getEnabled() && FactionsPlugin.getInstance().conf().lwc().isResetLocksOnUnclaim()) {
                    LWC.clearAllLocks(entry.getKey());
                }
                claimOwnership.remove(entry.getKey());
            }
        }
    }

    @Override
    public int getCountOfClaimsWithOwners() {
        return claimOwnership.isEmpty() ? 0 : claimOwnership.size();
    }

    @Override
    public boolean doesLocationHaveOwnersSet(FLocation loc) {
        return !claimOwnership.getOrDefault(loc, Set.of()).isEmpty();
    }

    @Override
    public boolean isPlayerInOwnerList(FPlayer player, FLocation loc) {
        return claimOwnership.getOrDefault(loc, Set.of()).contains(player.getUniqueId());
    }

    @Override
    public void setPlayerAsOwner(FPlayer player, FLocation loc) {
        claimOwnership.computeIfAbsent(loc, k -> new HashSet<>()).add(player.getUniqueId());
    }

    @Override
    public void removePlayerAsOwner(FPlayer player, FLocation loc) {
        Set<UUID> ownerData = claimOwnership.get(loc);
        if (ownerData == null) {
            return;
        }
        ownerData.remove(player.getUniqueId());
    }

    @Override
    public Set<UUID> getOwnerList(FLocation loc) {
        return new HashSet<>(claimOwnership.get(loc));
    }

    @Override
    public String getOwnerListString(FLocation loc) {
        Set<UUID> ownerData = claimOwnership.get(loc);
        if (ownerData == null || ownerData.isEmpty()) {
            return "";
        }
        return ownerData.stream().map(FPlayers.getInstance()::getById).map(FPlayer::getName).collect(Collectors.joining(", "));
    }

    @Override
    public boolean playerHasOwnershipRights(FPlayer fplayer, FLocation loc) {
        // in own faction, with sufficient role or permission to bypass
        // ownership?
        if (fplayer.getFaction() == this && (fplayer.getRole().isAtLeast(FactionsPlugin.getInstance().conf().factions().ownedArea().isModeratorsBypass() ? Role.MODERATOR : Role.ADMIN) || Permission.OWNERSHIP_BYPASS.has(fplayer.getPlayer()))) {
            return true;
        }

        // make sure claimOwnership is initialized
        if (claimOwnership.isEmpty()) {
            return true;
        }

        // need to check the ownership list, then
        Set<UUID> ownerData = claimOwnership.get(loc);

        // if no owner list, owner list is empty, or player is in owner list,
        // they're allowed
        return ownerData == null || ownerData.isEmpty() || ownerData.contains(fplayer.getUniqueId());
    }

    // ----------------------------------------------//
    // Persistance and entity management
    // ----------------------------------------------//
    public void remove() {
        if (Econ.shouldBeUsed() && FactionsPlugin.getInstance().conf().economy().isBankEnabled()) {
            Econ.setBalance(this, 0);
        }

        // Clean the board
        ((MemoryBoard) Board.getInstance()).clean(this);

        for (FPlayer fPlayer : fplayers) {
            fPlayer.resetFactionData(true);
        }
    }

    @Override
    public Set<FLocation> getAllClaims() {
        return Board.getInstance().getAllClaims(this);
    }
}
