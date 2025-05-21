package dev.kitteh.factions.data;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.config.file.PermissionsConfig;
import dev.kitteh.factions.event.FactionAutoDisbandEvent;
import dev.kitteh.factions.event.FactionNewAdminEvent;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.landraidcontrol.DTRControl;
import dev.kitteh.factions.landraidcontrol.LandRaidControl;
import dev.kitteh.factions.permissible.*;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.upgrade.Upgrade;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import dev.kitteh.factions.util.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public abstract class MemoryFaction implements Faction {
    public static class Permissions implements Faction.Permissions {
        public static class SelectorPerms implements Faction.Permissions.SelectorPerms {
            private Map<String, Boolean> perms;

            public SelectorPerms() {
                this.perms = new HashMap<>();
            }

            public SelectorPerms(Map<String, Boolean> perms) {
                this.perms = new HashMap<>(perms);
            }

            public Map<String, Boolean> getPerms() {
                return this.perms;
            }

            @Override
            public PermState get(String action) {
                return PermState.of(this.perms.get(action.toUpperCase()));
            }

            @Override
            public void set(String action, PermState state) {
                action = action.toUpperCase();
                if (Objects.requireNonNull(state) == PermState.UNSET) {
                    this.perms.remove(action);
                } else {
                    this.perms.put(action, state == PermState.ALLOW);
                }
            }

            @Override
            public Collection<String> actions() {
                return this.perms.keySet().stream().sorted().toList();
            }
        }

        private List<PermSelector> selectorOrder = new ArrayList<>();
        private Map<PermSelector, SelectorPerms> perms = new HashMap<>();

        @Override
        public List<PermSelector> selectors() {
            return Collections.unmodifiableList(this.selectorOrder);
        }

        @Override
        public SelectorPerms get(PermSelector selector) {
            SelectorPerms perm = this.perms.get(selector);
            if (perm == null) {
                throw new IllegalArgumentException("Selector " + selector + " is not present");
            }
            return perm;
        }

        @Override
        public boolean has(PermSelector selector) {
            return this.selectorOrder.contains(selector);
        }

        @Override
        public SelectorPerms add(PermSelector selector) {
            if (!this.selectorOrder.contains(selector)) {
                this.selectorOrder.add(selector);
            }
            return this.perms.computeIfAbsent(selector, k -> new SelectorPerms());
        }

        @Override
        public void remove(PermSelector selector) {
            this.selectorOrder.remove(selector);
            this.perms.remove(selector);
        }

        @Override
        public void moveSelectorUp(PermSelector selector) {
            int index = this.selectorOrder.indexOf(selector);
            if (index <= 0) { // Not present or already top
                return;
            }
            this.selectorOrder.remove(index);
            index--;
            this.selectorOrder.add(index, selector);
        }

        @Override
        public void moveSelectorDown(PermSelector selector) {
            int index = this.selectorOrder.indexOf(selector);
            if (index == -1 || index == this.selectorOrder.size() - 1) { // Not present or already bottom
                return;
            }
            this.selectorOrder.remove(index);
            index++;
            this.selectorOrder.add(index, selector);
        }

        @Override
        public void clear() {
            this.perms.clear();
            this.selectorOrder.clear();
        }
    }

    protected static class Zone implements Faction.Zone {
        private int id;
        private String name;
        @Nullable
        private String greeting;
        @Nullable
        private transient Component greetingComponent;
        private Permissions perms = new Permissions();
        private transient MemoryFaction faction;

        private Zone(int id, String name, MemoryFaction faction) {
            this.id = id;
            this.name = name;
            this.faction = faction;
        }

        @Override
        public int id() {
            return this.id;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public void name(String name) {
            this.name = Objects.requireNonNull(name);
        }

        @Override
        public Component greeting() {
            if (this.greetingComponent == null) {
                if (this.greeting == null) {
                    this.greetingComponent = this.faction.zones.main.greetingComponent;
                    if (this.greetingComponent == null) {
                        this.greetingComponent = Component.text("");
                    }
                } else {
                    this.greetingComponent = MiniMessage.miniMessage().deserialize(this.greeting,
                            Placeholder.unparsed("tag", this.faction.tag));
                }
            }
            return this.greetingComponent;
        }

        @Override
        public @Nullable String greetingString() {
            return this.greeting;
        }

        @Override
        public void greeting(@Nullable String greeting) {
            if (this.id == 0) {
                this.greeting = Objects.requireNonNull(greeting);
            } else {
                this.greeting = greeting;
            }
        }

        @Override
        public Faction.Permissions permissions() {
            return this.id == 0 ? this.faction.perms : this.perms;
        }

        @Override
        public boolean canPlayerManage(FPlayer fPlayer) {
            //noinspection ConstantValue
            if (fPlayer == null) {
                return false; // Fail in a safe way because people are foolish
            }
            if (fPlayer.faction() == this.faction && (fPlayer.role() == Role.ADMIN)) {
                return true;
            }
            if (this.faction.hasOverrideAccess(fPlayer, PermissibleActions.ZONE) instanceof Boolean bool) {
                return bool;
            }
            if (this.faction.hasAccess(fPlayer, PermissibleActions.ZONE, this.perms) instanceof Boolean bool) {
                return bool;
            }
            return false;
        }
    }

    protected static class Zones implements Faction.Zones {
        private Object2ObjectOpenHashMap<String, WorldTracker> worldTrackers = new Object2ObjectOpenHashMap<>();
        private Int2ObjectOpenHashMap<Zone> zones = new Int2ObjectOpenHashMap<>();
        private int nextId = 1;
        private transient Zone main;
        private transient MemoryFaction faction;

        private Zones(MemoryFaction faction) {
            this.cleanupDeserialization(faction);
        }

        @SuppressWarnings("ConstantValue")
        private void cleanupDeserialization(MemoryFaction faction) {
            this.faction = faction;
            if (this.main == null) {
                this.main = this.zones.get(0);
                if (this.main == null) {
                    this.main = new Zone(0, "main", this.faction);
                    main.greeting = "<tag> welcomes you!"; // TODO default in config
                    this.zones.put(0, this.main);
                }
                this.zones.defaultReturnValue(this.main);
            }
            for (Zone zone : this.zones.values()) {
                zone.faction = faction;
            }
        }

        @Override
        public Zone main() {
            return this.main;
        }

        @Override
        public Zone create(String name) {
            if (this.get(name) != null) {
                throw new IllegalArgumentException("The name '" + name + "' already exists");
            }
            Zone zone = new Zone(this.nextId++, name, this.faction);
            this.zones.put(zone.id(), zone);
            return zone;
        }

        @Override
        public void delete(String name) {
            this.zones.values().stream().filter(zone -> zone.name().equals(name)).findFirst().ifPresent(zone -> this.zones.remove(zone.id));
        }

        @Override
        public Zone get(FLocation fLocation) {
            WorldTracker tracker = this.worldTrackers.get(fLocation.worldName());
            int id;
            //noinspection ConstantValue
            if (tracker == null || (id = tracker.idAt(fLocation)) == WorldTracker.NO_MATCH) {
                return this.main();
            }

            return this.zones.get(id);
        }

        @Override
        public Faction.@Nullable Zone get(String name) {
            return this.zones.values().stream().filter(zone -> zone.name().equals(name)).findFirst().orElse(null);
        }

        @Override
        public List<Faction.Zone> get() {
            List<Faction.Zone> zones = new ArrayList<>();
            this.zones.values().stream()
                    .sorted(Comparator.comparing(zone -> zone.id))
                    .forEach(zones::add);
            return Collections.unmodifiableList(zones);
        }

        @Override
        public void set(Faction.Zone zone, FLocation fLocation) {
            if (fLocation.faction() != this.faction) {
                throw new IllegalArgumentException("Cannot assign non-owned territory");
            }
            if (this.zones.get(zone.id()) != zone) {
                throw new IllegalArgumentException("Invalid zone for this faction");
            }
            if (zone == this.main) {
                WorldTracker tracker = this.worldTrackers.get(fLocation.worldName());
                //noinspection ConstantValue
                if (tracker != null) {
                    tracker.removeClaim(fLocation);
                    if (tracker.countClaims() == 0) {
                        this.worldTrackers.remove(fLocation.worldName());
                    }
                }
                return;
            }
            this.getAndCreate(fLocation.worldName()).addClaim(zone.id(), fLocation);
        }

        protected WorldTracker getAndCreate(String world) {
            return this.worldTrackers.computeIfAbsent(world, k -> new WorldTracker(world));
        }
    }

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
    protected @Nullable Map<FLocation, Set<UUID>> claimOwnership; // Leaving for now, for people who want to look back at it
    protected transient Set<FPlayer> fplayers = new HashSet<>();
    protected Set<UUID> invites = new HashSet<>();
    protected HashMap<UUID, List<String>> announcements = new HashMap<>();
    protected ConcurrentHashMap<String, LazyLocation> warps = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<String, String> warpPasswords = new ConcurrentHashMap<>();
    protected long lastDeath;
    protected int maxVaults;
    protected Role defaultRole;
    protected @Nullable LinkedHashMap<PermSelector, Map<String, Boolean>> permissions; // legacy importing, lazier this way
    protected Set<BanInfo> bans = new HashSet<>();
    protected double dtr;
    protected long lastDTRUpdateTime;
    protected long frozenDTRUntilTime;
    protected int tntBank;
    protected Object2IntOpenHashMap<String> upgrades = new Object2IntOpenHashMap<>();
    protected transient @Nullable OfflinePlayer offlinePlayer;
    protected MemoryFaction.Permissions perms = new MemoryFaction.Permissions();
    protected Zones zones = new Zones(this);

    @SuppressWarnings("ConstantValue")
    public void cleanupDeserialization() {
        this.fplayers = new HashSet<>();
        this.offlinePlayer = null;
        this.asOfflinePlayer();
        if (this.upgrades == null) {
            this.upgrades = new Object2IntOpenHashMap<>();
        }
        if (this.permissions != null) {
            this.perms = new MemoryFaction.Permissions();
            for (Map.Entry<PermSelector, Map<String, Boolean>> entry : this.permissions.entrySet()) {
                this.perms.selectorOrder.add(entry.getKey());
                this.perms.perms.put(entry.getKey(), new Permissions.SelectorPerms(entry.getValue()));
            }
            this.permissions = null;
        }
        if (this.perms == null || !this.isNormal()) {
            this.resetPerms();
        }
        if (this.zones == null) {
            this.zones = new Zones(this);
        }
        this.zones.cleanupDeserialization(this); // Sets the transient helper main value.
    }

    @Override
    public void addAnnouncement(FPlayer fPlayer, String msg) {
        announcements.computeIfAbsent(fPlayer.uniqueId(), k -> new ArrayList<>()).add(msg);
    }

    @Override
    public void sendUnreadAnnouncements(FPlayer fPlayer) {
        List<String> ann = announcements.remove(fPlayer.uniqueId());
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
    public ConcurrentHashMap<String, LazyLocation> warps() {
        return this.warps;
    }

    @Override
    public LazyLocation warp(String name) {
        return this.warps.get(name);
    }

    @Override
    public void createWarp(String name, LazyLocation loc) {
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
    public int maxVaults() {
        return this.maxVaults;
    }

    @Override
    public void maxVaults(int value) {
        this.maxVaults = value;
    }

    @Override
    public Set<UUID> invites() {
        return invites;
    }

    @Override
    public int id() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        this.offlinePlayer = null;
    }

    @Override
    public void invite(FPlayer fplayer) {
        this.invites.add(fplayer.uniqueId());
    }

    @Override
    public void deInvite(FPlayer fplayer) {
        this.invites.remove(fplayer.uniqueId());
    }

    @Override
    public boolean hasInvite(FPlayer fplayer) {
        return this.invites.contains(fplayer.uniqueId());
    }

    @Override
    public void ban(FPlayer target, FPlayer banner) {
        BanInfo info = new BanInfo(banner.uniqueId(), target.uniqueId(), System.currentTimeMillis());
        this.bans.add(info);
    }

    @Override
    public void unban(FPlayer player) {
        bans.removeIf(banInfo -> banInfo.banned().equals(player.uniqueId()));
    }

    @Override
    public boolean isBanned(FPlayer player) {
        for (BanInfo info : bans) {
            if (info.banned().equals(player.uniqueId())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Set<BanInfo> bans() {
        return this.bans;
    }

    @Override
    public boolean open() {
        return open;
    }

    @Override
    public void open(boolean isOpen) {
        open = isOpen;
    }

    @Override
    public boolean peaceful() {
        return this.peaceful;
    }

    @Override
    public void peaceful(boolean isPeaceful) {
        this.peaceful = isPeaceful;
    }

    @Override
    public void peacefulExplosionsEnabled(boolean val) {
        peacefulExplosionsEnabled = val;
    }

    @Override
    public boolean peacefulExplosionsEnabled() {
        return this.peacefulExplosionsEnabled;
    }

    @Override
    public boolean permanent() {
        return permanent || !this.isNormal();
    }

    @Override
    public void permanent(boolean isPermanent) {
        permanent = isPermanent;
    }

    @Override
    public String tag() {
        return this.tag;
    }

    @Override
    public String tagString(@Nullable Faction otherFaction) {
        if (otherFaction == null) {
            return tag();
        }
        return this.colorStringTo(otherFaction) + this.tag();
    }

    @Override
    public String tagString(@Nullable FPlayer otherFplayer) {
        if (otherFplayer == null) {
            return tag();
        }
        return this.colorStringTo(otherFplayer) + this.tag();
    }

    @Override
    public void tag(String str) {
        if (FactionsPlugin.instance().conf().factions().other().isTagForceUpperCase()) {
            str = str.toUpperCase();
        }

        // Wipe processed greetings for tag placeholder
        this.zones.zones.values().forEach(zone -> zone.greetingComponent = null);
        this.tag = str;
    }

    @Override
    public String description() {
        return this.description;
    }

    @Override
    public void description(String value) {
        this.description = value;
    }

    @Override
    public String link() {
        if (this.link == null) {
            this.link = FactionsPlugin.instance().conf().commands().link().getDefaultURL();
        }
        return this.link;
    }

    @Override
    public void link(String value) {
        this.link = value;
    }

    @Override
    public void home(Location home) {
        this.home = new LazyLocation(home);
    }

    @Override
    public void delHome() {
        this.home = null;
    }

    @Override
    public @Nullable Location home() {
        confirmValidHome();
        return (this.home != null) ? this.home.asLocation() : null;
    }

    public void confirmValidHome() {
        if ((!FactionsPlugin.instance().conf().factions().homes().isMustBeInClaimedTerritory()) || (this.home == null) || (new FLocation(this.home).faction() == this)) {
            return;
        }

        msg(TL.FACTION_HOME_UNSET);
        this.home = null;
    }

    @Override
    public Instant founded() {
        if (this.foundedDate == 0) {
            founded(Instant.now());
        }
        return Instant.ofEpochMilli(this.foundedDate);
    }

    @Override
    public void founded(Instant when) {
        this.foundedDate = when.toEpochMilli();
    }

    @Override
    public OfflinePlayer asOfflinePlayer() {
        if (this.offlinePlayer == null) {
            this.offlinePlayer = FactionsPlugin.instance().factionOfflinePlayer("faction-" + this.id);
        }
        return this.offlinePlayer;
    }

    @Override
    public void lastDeath(Instant time) {
        this.lastDeath = time.toEpochMilli();
    }

    @Override
    public Instant lastDeath() {
        return Instant.ofEpochMilli(this.lastDeath);
    }

    @Override
    public int kills() {
        return members().stream().mapToInt(FPlayer::kills).sum();
    }

    @Override
    public int deaths() {
        int deaths = 0;
        for (FPlayer fp : members()) {
            deaths += fp.deaths();
        }

        return deaths;
    }

    @Override
    public boolean hasAccess(Selectable selectable, PermissibleAction permissibleAction, @Nullable FLocation location) {
        //noinspection ConstantValue
        if (selectable == null || permissibleAction == null) {
            return false; // Fail in a safe way because people are foolish
        }

        if (permissibleAction.prerequisite() instanceof Upgrade prereq && (!Universe.universe().isUpgradeEnabled(prereq) || this.upgradeLevel(prereq) == 0)) {
            return false;
        }

        if (selectable == Role.ADMIN || (selectable instanceof FPlayer fp && fp.faction() == this && fp.role() == Role.ADMIN)) {
            return true;
        }

        if (this.hasOverrideAccess(selectable, permissibleAction) instanceof Boolean bool) {
            return bool;
        }

        if (location != null) {
            Zone zone = this.zones.get(location);
            if (zone.id != 0 && this.hasAccess(selectable, permissibleAction, zone.perms) instanceof Boolean bool) {
                return bool;
            }
        }

        if (this.hasAccess(selectable, permissibleAction, this.perms) instanceof Boolean bool) {
            return bool;
        }

        return false;
    }

    private @Nullable Boolean hasOverrideAccess(Selectable selectable, PermissibleAction permissibleAction) {
        PermissionsConfig permConf = FactionsPlugin.instance().configManager().permissionsConfig();
        List<PermSelector> priority = permConf.getOverridePermissionsOrder().stream().filter(s -> s.test(selectable, this)).toList();
        for (PermSelector selector : priority) {
            Boolean bool = permConf.getOverridePermissions().get(selector).get(permissibleAction.name());
            if (bool != null) {
                return bool;
            }
        }
        return null;
    }

    private @Nullable Boolean hasAccess(Selectable selectable, PermissibleAction permissibleAction, Permissions permissions) {
        for (PermSelector selector : permissions.selectorOrder) {
            if (selector.test(selectable, this)) {
                Permissions.SelectorPerms perm = permissions.perms.get(selector);
                if (perm.perms.get(permissibleAction.name()) instanceof Boolean bool) {
                    return bool;
                }
            }
        }
        return null;
    }

    @Override
    public Permissions permissions() {
        return this.perms;
    }

    public void resetPerms() {
        this.perms.clear();

        if (!this.isNormal()) {
            return;
        }

        PermissionsConfig permConf = FactionsPlugin.instance().configManager().permissionsConfig();
        for (PermSelector selector : permConf.getDefaultPermissionsOrder()) {
            this.perms.selectorOrder.add(selector);
            this.perms.perms.put(selector, new Permissions.SelectorPerms(new HashMap<>(permConf.getDefaultPermissions().get(selector))));
        }
    }

    @Override
    public Role defaultRole() {
        return this.defaultRole;
    }

    @Override
    public void defaultRole(Role role) {
        this.defaultRole = role;
    }

    public MemoryFaction(int id, String tag) {
        this.id = id;
        this.open = FactionsPlugin.instance().conf().factions().other().isNewFactionsDefaultOpen();
        this.tag = tag;
        this.description = TL.GENERIC_DEFAULTDESCRIPTION.toString();
        this.lastPlayerLoggedOffTime = 0;
        this.peaceful = FactionsPlugin.instance().conf().factions().other().isNewFactionsDefaultPeaceful();
        this.peacefulExplosionsEnabled = false;
        this.permanent = false;
        this.powerBoost = 0.0;
        this.foundedDate = System.currentTimeMillis();
        this.maxVaults = FactionsPlugin.instance().conf().playerVaults().getDefaultMaxVaults();
        this.defaultRole = FactionsPlugin.instance().conf().factions().other().getDefaultRole();
        this.dtr = FactionsPlugin.instance().conf().factions().landRaidControl().dtr().getStartingDTR();

        resetPerms(); // Reset on new Faction so it has default values.
    }

    @Override
    public Relation relationWish(Faction otherFaction) {
        if (this.relationWish.containsKey(otherFaction.id())) {
            return this.relationWish.get(otherFaction.id());
        }
        return FactionsPlugin.instance().conf().factions().other().getDefaultRelation();
    }

    @Override
    public void relationWish(Faction otherFaction, Relation relation) {
        if (this.relationWish.containsKey(otherFaction.id()) && relation.equals(Relation.NEUTRAL)) {
            this.relationWish.remove(otherFaction.id());
        } else {
            this.relationWish.put(otherFaction.id(), relation);
        }
    }

    @Override
    public double dtr() {
        LandRaidControl lrc = FactionsPlugin.instance().landRaidControl();
        if (lrc instanceof DTRControl dtrControl) {
            dtrControl.updateDTR(this);
        }
        return this.dtr;
    }

    @Override
    public double dtrWithoutUpdate() {
        return this.dtr;
    }

    @Override
    public void dtr(double dtr) {
        double start = this.dtr;
        this.dtr = dtr;
        this.lastDTRUpdateTime = System.currentTimeMillis();
        if (start != this.dtr && FactionsPlugin.instance().landRaidControl() instanceof DTRControl dtrControl) {
            dtrControl.onDTRChange(this, start, this.dtr);
        }
    }

    @Override
    public long dtrLastUpdated() {
        return this.lastDTRUpdateTime;
    }

    @Override
    public long dtrFrozenUntil() {
        return this.frozenDTRUntilTime;
    }

    @Override
    public void dtrFrozenUntil(long time) {
        this.frozenDTRUntilTime = time;
    }

    @Override
    public boolean dtrFrozen() {
        return System.currentTimeMillis() < this.frozenDTRUntilTime;
    }

    @Override
    public double powerExact() {
        if (this.permanentPower != null) {
            return this.permanentPower;
        }

        double ret = 0;
        for (FPlayer fplayer : fplayers) {
            ret += fplayer.power();
        }
        if (FactionsPlugin.instance().conf().factions().landRaidControl().power().getFactionMax() > 0 && ret > FactionsPlugin.instance().conf().factions().landRaidControl().power().getFactionMax()) {
            ret = FactionsPlugin.instance().conf().factions().landRaidControl().power().getFactionMax();
        }
        return ret + this.powerBoost;
    }

    @Override
    public double powerMaxExact() {
        if (this.permanentPower != null) {
            return this.permanentPower;
        }

        double ret = 0;
        for (FPlayer fplayer : fplayers) {
            ret += fplayer.powerMax();
        }
        if (FactionsPlugin.instance().conf().factions().landRaidControl().power().getFactionMax() > 0 && ret > FactionsPlugin.instance().conf().factions().landRaidControl().power().getFactionMax()) {
            ret = FactionsPlugin.instance().conf().factions().landRaidControl().power().getFactionMax();
        }
        return ret + this.powerBoost;
    }

    @Override
    public @Nullable Integer permanentPower() {
        return this.permanentPower;
    }

    @Override
    public void permanentPower(@Nullable Integer permanentPower) {
        this.permanentPower = permanentPower;
    }

    @Override
    public double powerBoost() {
        return this.powerBoost;
    }

    @Override
    public void powerBoost(double powerBoost) {
        this.powerBoost = powerBoost;
    }

    @Override
    public boolean isPowerFrozen() {
        int freezeSeconds = FactionsPlugin.instance().conf().factions().landRaidControl().power().getPowerFreeze();
        return freezeSeconds != 0 && System.currentTimeMillis() - lastDeath < freezeSeconds * 1000L;
    }

    @Override
    public int tntBank() {
        return this.tntBank;
    }

    @Override
    public void tntBank(int amount) {
        this.tntBank = amount;
    }

    @Override
    public boolean isShielded() {
        return false; // TODO
    }

    @Override
    public int upgradeLevel(Upgrade upgrade) {
        if (!this.isNormal() || !Universe.universe().isUpgradeEnabled(upgrade)) {
            return 0;
        }
        UpgradeSettings settings = Universe.universe().upgradeSettings(upgrade);
        return Math.min(settings.maxLevel(), this.upgrades.getOrDefault(upgrade.name(), settings.startingLevel()));
    }

    @Override
    public void upgradeLevel(Upgrade upgrade, int level) {
        int newLevel = Math.min(upgrade.maxLevel(), level);
        int oldLevel = this.upgradeLevel(upgrade);
        this.upgrades.put(upgrade.name(), newLevel);
        this.sendMessage(MiniMessage.miniMessage().deserialize("<green>Upgraded <upgrade> to level " + newLevel + "!", Placeholder.component("upgrade", upgrade.nameComponent())));
        upgrade.onChange(this, oldLevel, newLevel);
    }

    public void addMember(FPlayer fPlayer) {
        if (this.isNormal()) {
            fplayers.add(fPlayer);
        }
    }

    public void removeMember(FPlayer fPlayer) {
        this.announcements.remove(fPlayer.uniqueId());
        this.fplayers.remove(fPlayer);
    }

    @Override
    public int size() {
        return fplayers.size();
    }

    @Override
    public Set<FPlayer> members() {
        return Set.copyOf(fplayers);
    }

    @Override
    public Set<FPlayer> membersOnline(boolean online) {
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
    public Set<FPlayer> membersOnline(boolean online, @Nullable FPlayer viewer) {
        if (viewer == null) {
            return membersOnline(online);
        }
        Set<FPlayer> ret = new HashSet<>();
        if (!this.isNormal()) {
            return ret;
        }

        Player viewerPlayer = viewer.asPlayer();

        for (FPlayer viewed : fplayers) {
            // Add if their online status is what we want
            boolean on = viewed.asPlayer() instanceof Player viewedPlayer && WorldUtil.isEnabled(viewedPlayer.getWorld()) && (viewerPlayer == null || viewerPlayer.canSee(viewedPlayer));
            if (on == online) {
                ret.add(viewed);
            }
        }

        return ret;
    }

    @Override
    public @Nullable FPlayer admin() {
        if (!this.isNormal()) {
            return null;
        }

        for (FPlayer fplayer : fplayers) {
            if (fplayer.role() == Role.ADMIN) {
                return fplayer;
            }
        }
        return null;
    }

    @Override
    public ArrayList<FPlayer> members(Role role) {
        ArrayList<FPlayer> ret = new ArrayList<>();
        if (!this.isNormal()) {
            return ret;
        }

        for (FPlayer fplayer : fplayers) {
            if (fplayer.role() == role) {
                ret.add(fplayer);
            }
        }

        return ret;
    }

    @Override
    public ArrayList<Player> membersOnlineAsPlayers() {
        ArrayList<Player> ret = new ArrayList<>();
        if (this.fplayers.isEmpty()) {
            return ret;
        }

        for (Player player : AbstractFactionsPlugin.getInstance().getServer().getOnlinePlayers()) {
            FPlayer fplayer = FPlayers.fPlayers().get(player);
            if (fplayer.faction() == this) {
                ret.add(player);
            }
        }

        return ret;
    }

    // slightly faster check than getOnlinePlayers() if you just want to see if
    // there are any players online
    @Override
    public boolean hasMembersOnline() {
        // only real factions can have players online, not safe zone / war zone
        if (this.fplayers.isEmpty()) {
            return false;
        }

        for (Player player : AbstractFactionsPlugin.getInstance().getServer().getOnlinePlayers()) {
            FPlayer fplayer = FPlayers.fPlayers().get(player);
            if (fplayer.faction() == this) {
                return true;
            }
        }

        // even if all players are technically logged off, maybe someone was on
        // recently enough to not consider them officially offline yet
        return FactionsPlugin.instance().conf().factions().other().getConsiderFactionsReallyOfflineAfterXMinutes() > 0 && System.currentTimeMillis() < lastPlayerLoggedOffTime + (FactionsPlugin.instance().conf().factions().other().getConsiderFactionsReallyOfflineAfterXMinutes() * 60000);
    }

    @Override
    public void trackMemberLoggedOff() {
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
        if (this.permanent() && FactionsPlugin.instance().conf().factions().specialCase().isPermanentFactionsDisableLeaderPromotion()) {
            return;
        }

        FPlayer oldLeader = this.admin();

        // get list of coleaders, or mods, or list of normal members if there are no moderators
        ArrayList<FPlayer> replacements = this.members(Role.COLEADER);
        if (replacements.isEmpty()) {
            replacements = this.members(Role.MODERATOR);
        }

        if (replacements.isEmpty()) {
            replacements = this.members(Role.NORMAL);
        }

        if (replacements.isEmpty()) { // faction admin  is the only  member; one-man  faction
            if (this.permanent()) {
                if (oldLeader != null) {
                    oldLeader.role(Role.NORMAL);
                }
                return;
            }

            // no members left and faction isn't permanent, so disband it
            if (FactionsPlugin.instance().conf().logging().isFactionDisband()) {
                FactionsPlugin.instance().log("The faction " + this.tag() + " (" + this.id() + ") has been disbanded since it has no members left.");
            }

            for (FPlayer fplayer : FPlayers.fPlayers().online()) {
                fplayer.msg(TL.LEAVE_DISBANDED, this.tagString(fplayer));
            }

            AbstractFactionsPlugin.getInstance().getServer().getPluginManager().callEvent(new FactionAutoDisbandEvent(this));

            Factions.factions().remove(this);
        } else { // promote new faction admin
            Bukkit.getServer().getPluginManager().callEvent(new FactionNewAdminEvent(replacements.getFirst(), this));

            if (oldLeader != null) {
                oldLeader.role(Role.COLEADER);
            }
            replacements.getFirst().role(Role.ADMIN);
            this.msg(TL.FACTION_NEWLEADER, oldLeader == null ? "" : oldLeader.name(), replacements.getFirst().name());
            FactionsPlugin.instance().log("Faction " + this.tag() + " (" + this.id() + ") admin was removed. Replacement admin: " + replacements.getFirst().name());
        }
    }

    @Override
    public void msg(String message, Object... args) {
        message = AbstractFactionsPlugin.getInstance().txt().parse(message, args);

        for (FPlayer fplayer : this.membersOnline(true)) {
            fplayer.sendMessage(message);
        }
    }

    public void remove() {
        if (Econ.shouldBeUsed() && FactionsPlugin.instance().conf().economy().isBankEnabled()) {
            Econ.setBalance(this, 0);
        }

        // Clean the board
        ((MemoryBoard) Board.board()).clean(this);

        for (FPlayer fPlayer : fplayers) {
            fPlayer.resetFactionData(true);
        }
    }

    @Override
    public Faction.Zones zones() {
        return this.zones;
    }
}
