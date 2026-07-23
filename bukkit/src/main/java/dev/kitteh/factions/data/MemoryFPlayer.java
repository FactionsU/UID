package dev.kitteh.factions.data;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Participator;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.annotation.NoFinalFields;
import dev.kitteh.factions.chat.ChatTarget;
import dev.kitteh.factions.command.defaults.CmdZone;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.event.FPlayerLeaveEvent;
import dev.kitteh.factions.event.FactionAutoDisbandEvent;
import dev.kitteh.factions.event.LandClaimEvent;
import dev.kitteh.factions.event.LandUnclaimEvent;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.integration.ExternalChecks;
import dev.kitteh.factions.landraidcontrol.DTRControl;
import dev.kitteh.factions.landraidcontrol.PowerControl;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.scoreboard.FScoreboard;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.upgrade.Upgrades;
import dev.kitteh.factions.util.ComponentDispatcher;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.WarmUpUtil;
import dev.kitteh.factions.util.WorldUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


/**
 * Logged in players always have exactly one FPlayer instance. Logged out players may or may not have an FPlayer
 * instance. They will always have one if they are part of a faction. This is because only players with a faction are
 * saved to disk (in order to not waste disk space).
 * <p>
 * The FPlayer is linked to a minecraft player using the player name.
 * <p>
 * The same instance is always returned for the same player. This means you can use the == operator. No .equals method
 * necessary.
 */
@ApiStatus.Internal
@NoFinalFields
@NullMarked
@SuppressWarnings("FieldMayBeFinal")
public abstract class MemoryFPlayer implements FPlayer {

    protected int factionId;
    protected Role role = Role.NORMAL;
    protected String titleMM = "";
    protected @Nullable
    transient Component titleComponent;
    protected @Nullable String title; // Keeping for now for conversion
    protected double power;
    protected double powerBoost;
    protected long lastPowerUpdateTime;
    protected long lastLoginTime;
    protected ChatTarget chatTarget = ChatTarget.PUBLIC;
    protected boolean ignoreAllianceChat = false;
    protected boolean ignoreTruceChat = false;
    protected UUID id;
    protected String name;
    protected boolean monitorJoins;
    protected boolean spyingChat = false;
    protected boolean showScoreboard = true;
    protected WarmUpUtil.@Nullable Warmup warmup;
    protected int warmupTask;
    protected boolean isAdminBypassing = false;
    protected int kills, deaths;
    protected double duesDebt;
    protected List<LocalDate> missedDuesDates = new ArrayList<>();
    protected boolean willAutoLeave = true;
    protected int mapHeight; // default to old value
    protected boolean isFlying = false;
    protected boolean isAutoFlying = false;
    protected boolean flyTrailsState = false;
    protected @Nullable String flyTrailsEffect = null;
    protected boolean seeingChunk = false;
    protected FLocation lastStoodAt = new FLocation("world", 0, 0); // Where did this player stand the last time we checked?

    // Non-serialized data
    protected transient boolean mapAutoUpdating;
    protected transient @Nullable Faction autoClaimFor;
    protected transient @Nullable String autoSetZone;
    protected transient @Nullable Faction autoUnclaimFor;
    protected transient boolean loginPvpDisabled;
    protected transient long respawnInvulnerableUntil;
    protected transient long lastFrostwalkerMessage;
    protected transient boolean shouldTakeFallDamage = true;
    protected transient @Nullable OfflinePlayer offlinePlayer;
    protected transient Set<String> bannedBy = new HashSet<>();

    public void cleanupDeserialization() {
        this.shouldTakeFallDamage = true;
        //noinspection ConstantValue
        if (this.title != null && this.titleMM == null) { // Migration from 0.7.x
            this.titleComponent = LegacyComponentSerializer.legacySection().deserialize(this.title);
            this.titleMM = MiniMessage.miniMessage().serialize(this.titleComponent);
        }
        this.title = null;
        this.bannedBy = new HashSet<>();
        for (Faction faction : Factions.factions().all()) {
            if (!faction.isBanned(this)) continue;
            this.bannedBy.add(String.valueOf(faction.id()));
        }
        //noinspection ConstantValue
        if (this.missedDuesDates == null) {
            this.missedDuesDates = new ArrayList<>();
        }
    }

    public void onLogInOut() {
        if (this.asPlayer() instanceof Player player) {
            this.kills = player.getStatistic(Statistic.PLAYER_KILLS);
            this.deaths = player.getStatistic(Statistic.DEATHS);
            this.autoLeaveExempt(Permission.AUTO_LEAVE_BYPASS.has(player));
        }
        this.lastLoginTime = System.currentTimeMillis();
        if (Confs.main().factions().pvp().getNoPVPDamageToOthersForXSecondsAfterLogin() > 0) {
            this.loginPvpDisabled = true;
        }
    }

    @Override
    public Faction faction() {
        Faction faction = Factions.factions().get(this.factionId);
        if (faction == null) {
            AbstractFactionsPlugin.instance().getLogger().warning("Found null faction (id " + this.factionId + ") for player " + this.name());
            this.resetFactionData(true);
            faction = Factions.factions().wilderness();
        }
        return faction;
    }

    public int getFactionId() {
        return this.factionId;
    }

    @Override
    public boolean hasFaction() {
        return factionId != Factions.ID_WILDERNESS;
    }

    @Override
    public void faction(Faction faction) {
        Faction oldFaction = this.faction();
        ((MemoryFaction) oldFaction).removeMember(this);
        ((MemoryFaction) faction).addMember(this);
        this.factionId = faction.id();
    }

    @Override
    public void monitorJoins(boolean monitor) {
        this.monitorJoins = monitor;
    }

    @Override
    public boolean monitorJoins() {
        return this.monitorJoins;
    }

    @Override
    public Role role() {
        return this.role;
    }

    @Override
    public void role(Role role) {
        this.role = Objects.requireNonNull(role);
    }

    @Override
    public double duesDebt() {
        return this.duesDebt;
    }

    @Override
    public void duesDebt(double amount) {
        this.duesDebt = Math.max(0, amount);
    }

    @Override
    public List<LocalDate> missedDuesDates() {
        return List.copyOf(this.missedDuesDates);
    }

    @Override
    public void addMissedDuesDate(LocalDate date) {
        this.missedDuesDates.add(date);
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
    public boolean autoLeaveExempt() {
        return !this.willAutoLeave;
    }

    @Override
    public void autoLeaveExempt(boolean exempt) {
        this.willAutoLeave = !exempt;
    }

    @Override
    public long lastFrostwalkerMessageTime() {
        return this.lastFrostwalkerMessage;
    }

    @Override
    public void updateLastFrostwalkerMessageTime() {
        this.lastFrostwalkerMessage = System.currentTimeMillis();
    }

    @Override
    public @Nullable Faction autoClaim() {
        return autoClaimFor;
    }

    @Override
    public void autoClaim(@Nullable Faction faction) {
        this.autoClaimFor = faction;
        if (faction != null) {
            this.autoUnclaimFor = null;
        }
    }

    @Override
    public @Nullable String autoSetZone() {
        return this.autoSetZone;
    }

    @Override
    public void autoSetZone(@Nullable String zone) {
        this.autoSetZone = zone;
    }

    @Override
    public @Nullable Faction autoUnclaim() {
        return autoUnclaimFor;
    }

    @Override
    public void autoUnclaim(@Nullable Faction faction) {
        this.autoUnclaimFor = faction;
        if (faction != null) {
            this.autoClaimFor = null;
            this.autoSetZone = null;
        }
    }

    @Override
    public boolean adminBypass() {
        return this.isAdminBypassing;
    }

    @Override
    public boolean isVanished() {
        Player player = this.asPlayer();
        if (player != null) {
            if (ExternalChecks.isVanished(player)) {
                return true;
            }
            for (@Nullable MetadataValue metadataValue : player.getMetadata("vanished")) {
                if (metadataValue != null && metadataValue.asBoolean()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void adminBypass(boolean val) {
        this.isAdminBypassing = val;
        if (this.asPlayer() instanceof Player player) {
            player.updateCommands();
        }
    }

    @Override
    public void chatTarget(ChatTarget chatTarget) {
        this.chatTarget = Objects.requireNonNull(chatTarget);
    }

    @Override
    public ChatTarget chatTarget() {
        //noinspection ConstantValue
        if (this.chatTarget == null || this.factionId == Factions.ID_WILDERNESS ||
                (this.chatTarget instanceof ChatTarget.Relation && !Confs.main().factions().chat().internalChat().isRelationChatEnabled()) ||
                (this.chatTarget instanceof ChatTarget.Role && !Confs.main().factions().chat().internalChat().isFactionMemberChatEnabled())) {
            this.chatTarget = ChatTarget.PUBLIC;
        }
        return this.chatTarget;
    }

    @Override
    public void ignoreAllianceChat(boolean ignore) {
        this.ignoreAllianceChat = ignore;
    }

    @Override
    public boolean ignoreAllianceChat() {
        return ignoreAllianceChat;
    }

    @Override
    public void ignoreTruceChat(boolean ignore) {
        this.ignoreTruceChat = ignore;
    }

    @Override
    public boolean ignoreTruceChat() {
        return ignoreTruceChat;
    }

    @Override
    public void spyingChat(boolean chatSpying) {
        this.spyingChat = chatSpying;
    }

    @Override
    public boolean spyingChat() {
        return spyingChat;
    }

    @Override
    public OfflinePlayer asOfflinePlayer() {
        if (this.offlinePlayer == null) {
            this.offlinePlayer = Bukkit.getPlayer(this.id);
            if (this.offlinePlayer == null) {
                this.offlinePlayer = AbstractFactionsPlugin.instance().getOfflinePlayer(this.name, this.id);
            }
        }
        return this.offlinePlayer;
    }

    public void setOfflinePlayer(@Nullable Player player) {
        this.offlinePlayer = player;
    }

    public MemoryFPlayer(UUID id) {
        this.id = id;
        this.name = id.toString();
        this.autoClaimFor = null;
        this.autoUnclaimFor = null;
        this.autoSetZone = null;
        this.power = Confs.main().factions().landRaidControl().power().getPlayerStarting();
        this.lastPowerUpdateTime = System.currentTimeMillis();
        this.lastLoginTime = System.currentTimeMillis();
        this.mapAutoUpdating = false;
        this.autoClaimFor = null;
        this.loginPvpDisabled = Confs.main().factions().pvp().getNoPVPDamageToOthersForXSecondsAfterLogin() > 0;
        this.powerBoost = 0.0;
        this.kills = 0;
        this.deaths = 0;
        this.mapHeight = Confs.main().map().getHeight();

        Faction newFaction = Factions.factions().get(Confs.main().factions().other().getNewPlayerStartingFactionID());
        if (newFaction != null) {
            this.factionId = Confs.main().factions().other().getNewPlayerStartingFactionID();
        }
    }

    @Override
    public void resetFactionData() {
        this.resetFactionData(false);
    }

    @Override
    public void resetFactionData(boolean updateCommands) {
        // clean up any territory ownership in old faction, if there is one
        Faction currentFaction = Factions.factions().get(this.factionId);
        if (currentFaction != null) {
            ((MemoryFaction) currentFaction).removeMember(this);
        }

        this.factionId = Factions.ID_WILDERNESS; // The default neutral faction
        this.role = Role.NORMAL;
        this.duesDebt = 0; // World's best debt forgiveness system
        this.missedDuesDates.clear();
        this.titleMM = "";
        this.titleComponent = null;
        this.autoClaimFor = null;
        this.autoUnclaimFor = null;
        this.autoSetZone = null;

        if (updateCommands && this.asPlayer() instanceof Player player) {
            player.updateCommands();
        }
    }

    @Override
    public long lastLogin() {
        return lastLoginTime;
    }

    @Override
    public boolean mapAutoUpdating() {
        return mapAutoUpdating;
    }

    @Override
    public void mapAutoUpdating(boolean mapAutoUpdating) {
        this.mapAutoUpdating = mapAutoUpdating;
    }

    @Override
    public boolean loginPVPDisabled() {
        if (!loginPvpDisabled) {
            return false;
        }
        if (this.lastLoginTime + (Confs.main().factions().pvp().getNoPVPDamageToOthersForXSecondsAfterLogin() * 1000L) < System.currentTimeMillis()) {
            this.loginPvpDisabled = false;
            return false;
        }
        return true;
    }

    @Override
    public void respawnInvulnerability(int seconds) {
        this.respawnInvulnerableUntil = seconds > 0 ? System.currentTimeMillis() + (seconds * 1000L) : 0;
    }

    @Override
    public boolean respawnInvulnerable() {
        if (this.respawnInvulnerableUntil == 0) {
            return false;
        }
        if (this.respawnInvulnerableUntil < System.currentTimeMillis()) {
            this.respawnInvulnerableUntil = 0;
            return false;
        }
        return true;
    }

    @Override
    public FLocation lastStoodAt() {
        //noinspection ConstantValue
        if (this.lastStoodAt == null) {
            this.lastStoodAt = new FLocation(Bukkit.getWorlds().getFirst().getName(), 0, 0);
        }
        return this.lastStoodAt;
    }

    @Override
    public void lastStoodAt(FLocation flocation) {
        this.lastStoodAt = flocation;
    }

    @Override
    public Component title() {
        if (this.hasFaction()) {
            if (this.titleComponent == null) {
                this.titleComponent = this.titleMM.isEmpty() ? Component.empty() : Mini.parseLimited(this.titleMM);
            }
            return this.titleComponent;
        }
        return Component.empty();
    }

    @Override
    public void title(Component title) {
        this.titleComponent = title;
        this.titleMM = MiniMessage.miniMessage().serialize(title);
    }

    @Override
    public String name() {
        //noinspection ConstantValue
        if (this.name == null) {
            this.name = this.id.toString();
        }
        return name;
    }

    private static class NameLookup {
        String name = "";
    }

    public void setName(String name) {
        if (!name.equalsIgnoreCase(this.name)) {
            for (FPlayer fplayer : FPlayers.fPlayers().all()) {
                if (fplayer.name().equalsIgnoreCase(name)) {
                    UUID u = fplayer.uniqueId();
                    String uuidName = u.toString();
                    ((MemoryFPlayer) fplayer).name = uuidName; // Done this way to avoid loop
                    if (u.version() != 4) {
                        continue;
                    }
                    String uuid = uuidName.replace("-", "");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                URL url = new URI("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid).toURL();
                                NameLookup lookup = AbstractFactionsPlugin.instance().gson().fromJson(new InputStreamReader(url.openStream()), NameLookup.class);
                                String newName = lookup.name;
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        //noinspection ConstantValue
                                        if (newName != null && fplayer.name().equals(uuidName)) {
                                            ((MemoryFPlayer) fplayer).setName(newName);
                                        }
                                    }
                                }.runTask(AbstractFactionsPlugin.instance());
                            } catch (Exception ignored) {
                            }
                        }
                    }.runTaskAsynchronously(AbstractFactionsPlugin.instance());
                }
            }
        }
        this.name = name;
    }

    public String tagOrEmpty() {
        return this.hasFaction() ? this.faction().tag() : "";
    }

    @Override
    public Component nameWithTitle() {
        Component title = this.title();
        if (title.equals(Component.empty())) {
            return Component.text(this.role.getPrefix() + this.name);
        }
        if (Confs.tl().placeholders().isPlayerTitleColorContinuesIntoName()) {
            return Component.text().append(Component.text(this.role.getPrefix())).append(title.append(Component.text(" " + this.name))).build();
        }
        return Component.text().append(Component.text(this.role.getPrefix())).append(title).append(Component.text(" " + this.name)).build();
    }

    @Override
    public Component nameWithTag() {
        String tag = this.tagOrEmpty();
        if (tag.isEmpty()) {
            return Component.text(this.role.getPrefix() + this.name);
        }
        return Component.text(this.role.getPrefix() + tag + " " + this.name);
    }

    @Override
    public int kills() {
        if (this.asPlayer() instanceof Player player) {
            this.kills = player.getStatistic(Statistic.PLAYER_KILLS);
        }
        return this.kills;
    }

    @Override
    public int deaths() {
        if (this.asPlayer() instanceof Player player) {
            this.deaths = player.getStatistic(Statistic.DEATHS);
        }
        return this.deaths;
    }

    @Override
    public double power() {
        this.updatePower();
        return this.power;
    }

    @Override
    public void power(double power) {
        this.power = Math.clamp(power, this.powerMin(), this.powerMax());
    }

    @Override
    public void alterPower(double delta) {
        int start = (int) Math.round(this.power);
        this.power += delta;
        if (this.power > this.powerMax()) {
            this.power = this.powerMax();
        } else if (this.power < this.powerMin()) {
            this.power = this.powerMin();
        }
        int end = (int) Math.round(this.power);
        if (this.hasFaction() && end != start && FactionsPlugin.instance().landRaidControl() instanceof PowerControl) {
            ((PowerControl) FactionsPlugin.instance().landRaidControl()).onPowerChange(this.faction(), start, end);
        }
    }

    @Override
    public double powerMax() {
        return Confs.main().factions().landRaidControl().power().getPlayerMax() + this.powerBoost;
    }

    @Override
    public double powerMin() {
        return Confs.main().factions().landRaidControl().power().getPlayerMin() + this.powerBoost;
    }

    @Override
    public void updatePower() {
        long lastUpdate = this.lastPowerUpdateTime;
        if (!this.isOnline()) {
            losePowerFromBeingOffline();
            if (!Confs.main().factions().landRaidControl().power().isRegenOffline()) {
                return;
            }
        } else if (hasFaction() && faction().isPowerFrozen()) {
            return; // Don't let power regen if faction power is frozen.
        } else if (Confs.main().plugins().general().isPreventRegenWhileAfk() && this.asPlayer() instanceof Player plr && ExternalChecks.isAfk(plr)) {
            return;
        }
        long now = System.currentTimeMillis();
        long millisPassed = now - lastUpdate;
        if (millisPassed < 500) { // Don't need to update every single call!
            return;
        }
        this.lastPowerUpdateTime = now;

        Player thisPlayer = this.asPlayer();
        if (thisPlayer != null && thisPlayer.isDead()) {
            return;  // don't let dead players regain power until they respawn
        }

        int millisPerMinute = 60 * 1000;
        double perMinute = Confs.main().factions().landRaidControl().power().getPowerPerMinute();
        if (this.hasFaction()) {
            int lvl = this.faction().upgradeLevel(Upgrades.POWER_REGEN);
            if (lvl > 0) {
                double boost = Math.max(0, Universe.universe().upgradeSettings(Upgrades.POWER_REGEN).valueAt(Upgrades.Variables.PERCENT, lvl).doubleValue());
                perMinute *= (1 + boost);
            }
        }
        this.alterPower(millisPassed * perMinute / millisPerMinute);
    }

    @Override
    public void losePowerFromBeingOffline() {
        long now = System.currentTimeMillis();
        if (Confs.main().factions().landRaidControl().power().getOfflineLossPerDay() > 0.0 && this.power > Confs.main().factions().landRaidControl().power().getOfflineLossLimit()) {
            long millisPassed = now - this.lastPowerUpdateTime;

            double loss = millisPassed * Confs.main().factions().landRaidControl().power().getOfflineLossPerDay() / (24 * 60 * 60 * 1000);
            double offlineLossLimit = Confs.main().factions().landRaidControl().power().getOfflineLossLimit();
            if (this.power - loss < offlineLossLimit) {
                loss = this.power - offlineLossLimit;
            }
            this.alterPower(-loss);
        }
        this.lastPowerUpdateTime = now;
    }

    @Override
    public void onDeath() {
        if (hasFaction()) {
            faction().lastDeath(Instant.now());
        }
    }

    @Override
    public boolean isInOwnTerritory() {
        return getStandingInFaction() == this.faction();
    }

    @Override
    public boolean isInOthersTerritory() {
        Faction factionHere = getStandingInFaction();
        return factionHere.isNormal() && factionHere != this.faction();
    }

    @Override
    public boolean isInAllyTerritory() {
        return getStandingInFaction().relationTo(this).isAlly();
    }

    @Override
    public boolean isInNeutralTerritory() {
        return getStandingInFaction().relationTo(this).isNeutral();
    }

    @Override
    public boolean isInEnemyTerritory() {
        return getStandingInFaction().relationTo(this).isEnemy();
    }

    private Faction getStandingInFaction() {
        Player player = this.asPlayer();
        return Board.board().factionAt(player == null ? this.lastStoodAt : new FLocation(player));
    }

    @Override
    public void sendFactionHereMessage(Faction from) {
        Faction toShow = Board.board().factionAt(lastStoodAt());
        boolean showTitle = Confs.main().factions().enterTitles().isEnabled();
        boolean showChat = true;
        Player player = asPlayer();

        var tl = Confs.tl().general().enterTitles();

        if (showTitle && player != null) {
            int in = Confs.main().factions().enterTitles().getFadeIn();
            int stay = Confs.main().factions().enterTitles().getStay();
            int out = Confs.main().factions().enterTitles().getFadeOut();

            FactionResolver factionResolver = FactionResolver.of(toShow);
            ComponentDispatcher.sendTitle(player, Mini.parse(tl.getTitle(), this, factionResolver), Mini.parse(tl.getSubtitle(), this, factionResolver), in, stay, out);

            showChat = Confs.main().factions().enterTitles().isAlsoShowChat();
        }

        if (showInfoBoard(toShow)) {
            FScoreboard.get(this).setTemporarySidebar(toShow);
            showChat = Confs.main().scoreboard().info().isAlsoSendChat();
        }
        if (showChat) {
            this.sendRichMessage(tl.getChat(), FactionResolver.of("oldfaction", from), FactionResolver.of("newfaction", toShow));
        }
    }

    /**
     * Check if the scoreboard should be shown. Simple method to be used by above method.
     *
     * @param toShow Faction to be shown.
     * @return true if should show, otherwise false.
     */
    public boolean showInfoBoard(Faction toShow) {
        return showScoreboard && !toShow.isWarZone() && !toShow.isWilderness() && !toShow.isSafeZone() && Confs.main().scoreboard().info().isEnabled() && FScoreboard.get(this) != null;
    }

    @Override
    public boolean showScoreboard() {
        return this.showScoreboard;
    }

    @Override
    public void showScoreboard(boolean show) {
        this.showScoreboard = show;
    }

    @Override
    public void leave(boolean makePay) {
        Faction myFaction = this.faction();
        boolean econMakePay = makePay && Econ.shouldBeUsed() && !this.adminBypass();

        boolean perm = myFaction.isPermanent();

        if (!perm && this.role() == Role.ADMIN && myFaction.members().size() > 1) {
            sendRichMessage(Confs.tl().commands().leave().getPassAdmin());
            return;
        }

        if (makePay && !FactionsPlugin.instance().landRaidControl().canLeaveFaction(this)) {
            return;
        }

        // if economy is enabled and they're not on the bypass list, make sure they can pay
        if (econMakePay && !Econ.hasAtLeast(this, Confs.main().economy().getCostLeave(), Confs.tl().economy().actions().getLeaveTo())) {
            return;
        }

        FPlayerLeaveEvent leaveEvent = new FPlayerLeaveEvent(this, myFaction, FPlayerLeaveEvent.Reason.LEAVE);
        Bukkit.getServer().getPluginManager().callEvent(leaveEvent);
        if (leaveEvent.isCancelled()) {
            return;
        }

        // then make 'em pay (if applicable)
        if (econMakePay && !Econ.modifyMoney(this, -Confs.main().economy().getCostLeave(), Confs.tl().economy().actions().getLeaveTo(), Confs.tl().economy().actions().getLeaveFor())) {
            return;
        }

        // Am I the last one in the faction?
        if (myFaction.members().size() == 1) {
            // Transfer all money
            if (Econ.shouldBeUsedWithBanks()) {
                if (!perm || Confs.main().economy().isBankPermanentFactionSendBalanceToLastLeaver()) {
                    //Give all the faction's money to the disbander
                    double amount = Econ.getBalance(myFaction);
                    Econ.transferMoney(this, myFaction, this, amount, false);

                    if (amount > 0.0) {
                        String amountString = Econ.moneyString(amount);
                        this.sendRichMessage(Confs.tl().commands().disband().getEconHoldings(), Placeholder.unparsed("amount", amountString));
                        AbstractFactionsPlugin.instance().log(this.name() + " has been given bank holdings of " + amountString + " from disbanding " + myFaction.tag() + ".");
                    }
                }
            }
        }

        if (myFaction.isNormal()) {
            for (FPlayer fplayer : myFaction.membersOnline(true)) {
                fplayer.sendMessage(Mini.parse(Confs.tl().commands().leave().getLeftNotice(), fplayer,
                        FPlayerResolver.of("player", this),
                        FactionResolver.of(myFaction)));
            }

            if (Confs.main().logging().isFactionLeave()) {
                AbstractFactionsPlugin.instance().log(this.name() + " left faction " + myFaction.tag() + ".");
            }
        }

        this.resetFactionData(true);
        if (Confs.main().commands().fly().isEnable()) {
            flying(false);
        }

        if (myFaction.isNormal() && !perm && myFaction.members().isEmpty()) {
            // Remove this faction
            for (FPlayer fplayer : FPlayers.fPlayers().online()) {
                fplayer.sendRichMessage(Confs.tl().commands().leave().getDisbanded(),
                        FactionResolver.of(myFaction));
            }

            AbstractFactionsPlugin.instance().getServer().getPluginManager().callEvent(new FactionAutoDisbandEvent(myFaction));
            Factions.factions().remove(myFaction);
            if (Confs.main().logging().isFactionDisband()) {
                AbstractFactionsPlugin.instance().log("The faction " + myFaction.tag() + " (" + myFaction.id() + ") was disbanded due to the last player (" + this.name() + ") leaving.");
            }
        }
    }

    @Override
    public void attemptAutoSetZone(FLocation flocation) {
        if (this.hasFaction() && this.autoSetZone != null) {
            Faction faction = this.faction();
            Faction.Zone zone = faction.zones().get(this.autoSetZone);
            if (zone == null) {
                this.autoSetZone = null;
                return;
            }
            Faction.Zone currentZone = faction.zones().get(flocation);
            if (currentZone == zone) {
                return;
            }
            if (CmdZone.claim(this, faction, flocation, zone, false)) {
                this.sendRichMessage(Confs.tl().commands().zone().claim().getSuccess(), Placeholder.unparsed("oldzone", currentZone.name()), Placeholder.unparsed("newzone", zone.name()));
            }
        }
    }

    @Override
    public boolean canClaimForFaction(Faction forFaction) {
        Player player = this.asPlayer();
        if (player == null || forFaction.isWilderness()) {
            return false;
        }
        return this.adminBypass() ||
                (forFaction == this.faction() && this.faction().hasAccess(this, PermissibleActions.TERRITORY, null)) ||
                (forFaction.isSafeZone() && Permission.MANAGE_SAFE_ZONE.has(player)) ||
                (forFaction.isWarZone() && Permission.MANAGE_WAR_ZONE.has(player));
    }

    @Override
    public boolean canClaimForFactionAtLocation(Faction forFaction, FLocation flocation, boolean notifyFailure) {
        Player player = this.asPlayer();
        if (player == null) {
            return false;
        }
        AbstractFactionsPlugin plugin = AbstractFactionsPlugin.instance();
        Component denyReason = null;
        Faction myFaction = faction();
        Faction currentFaction = Board.board().factionAt(flocation);
        int ownedLand = forFaction.claimCount();
        int factionBuffer = Confs.main().factions().claims().getBufferZone();
        int worldBuffer = Confs.main().worldBorder().getBuffer();
        var claimTl = Confs.tl().claiming().claim();

        if (Confs.main().plugins().worldGuard().isCheckingEither() && plugin.getWorldguard() != null && plugin.getWorldguard().checkForRegionsInChunk(flocation.asChunk())) {
            // Checks for WorldGuard regions in the chunk attempting to be claimed
            denyReason = Mini.parse(claimTl.getProtectedLand(), this);
        } else if (Confs.main().factions().claims().getWorldsNoClaiming().contains(flocation.worldName())) {
            // Cannot claim in this world
            denyReason = Mini.parse(claimTl.getDisabled(), this);
        } else if (this.adminBypass()) {
            // Admin bypass
            return true;
        } else if (forFaction.isSafeZone() && Permission.MANAGE_SAFE_ZONE.has(player)) {
            // Safezone and can claim for such
            return true;
        } else if (forFaction.isWarZone() && Permission.MANAGE_WAR_ZONE.has(player)) {
            // Warzone and can claim for such
            return true;
        } else if (!forFaction.hasAccess(this, PermissibleActions.TERRITORY, null)) {
            // Lacking perms to territory claim
            denyReason = Mini.parse(claimTl.getCantClaim(), this, FactionResolver.of("faction", forFaction));
        } else if (forFaction == currentFaction) {
            // Already owned by this faction, nitwit
            denyReason = Mini.parse(claimTl.getAlreadyOwn(), this, FactionResolver.of("faction", forFaction));
        } else if (forFaction.members().size() < Confs.main().factions().claims().getRequireMinFactionMembers()) {
            // Need more members in order to claim land
            denyReason = Mini.parse(claimTl.getMembers(), this, Placeholder.unparsed("count", String.valueOf(Confs.main().factions().claims().getRequireMinFactionMembers())));
        } else if (currentFaction.isSafeZone()) {
            // Cannot claim safezone
            denyReason = Mini.parse(claimTl.getSafeZone(), this);
        } else if (currentFaction.isWarZone()) {
            // Cannot claim warzone
            denyReason = Mini.parse(claimTl.getWarZone(), this);
        } else if (plugin.landRaidControl() instanceof PowerControl && ownedLand >= forFaction.power()) {
            // Already own at least as much land as power
            denyReason = Mini.parse(claimTl.getPower(), this);
        } else if (plugin.landRaidControl() instanceof DTRControl && ownedLand >= plugin.landRaidControl().landLimit(forFaction)) {
            // Already own at least as much land as land limit (DTR)
            denyReason = Mini.parse(claimTl.getDtrLand(), this);
        } else if (Confs.main().factions().claims().getLandsMax() != 0 && ownedLand >= Confs.main().factions().claims().getLandsMax() && forFaction.isNormal()) {
            // Land limit reached
            denyReason = Mini.parse(claimTl.getLimit(), this);
        } else if (currentFaction.relationTo(forFaction) == Relation.ALLY) {
            // Can't claim ally
            denyReason = Mini.parse(claimTl.getAlly(), this);
        } else if (Confs.main().factions().claims().isMustBeConnected() && !this.adminBypass() && myFaction.claimCount(flocation.world()) > 0 && Board.board().isDisconnectedLocation(flocation, myFaction) && (!Confs.main().factions().claims().isCanBeUnconnectedIfOwnedByOtherFaction() || !currentFaction.isNormal())) {
            // Must be contiguous/connected
            if (Confs.main().factions().claims().isCanBeUnconnectedIfOwnedByOtherFaction()) {
                denyReason = Mini.parse(claimTl.getContiguous(), this);
            } else {
                denyReason = Mini.parse(claimTl.getFactionContiguous(), this);
            }
        } else if (contiguousChecks(forFaction, flocation) instanceof Component deny) {
            denyReason = deny;
        } else if (!(currentFaction.isNormal() && Confs.main().factions().claims().isAllowOverClaimAndIgnoringBuffer() && currentFaction.hasLandInflation()) && factionBuffer > 0 && Board.board().hasFactionWithin(flocation, myFaction, factionBuffer)) {
            // Too close to buffer
            denyReason = Mini.parse(claimTl.getTooCloseToOtherFaction(), this, Placeholder.unparsed("count", String.valueOf(factionBuffer)));
        } else if (flocation.isOutsideWorldBorder(worldBuffer)) {
            // Border buffer
            if (worldBuffer > 0) {
                denyReason = Mini.parse(claimTl.getOutsideBorderBuffer(), this, Placeholder.unparsed("count", String.valueOf(worldBuffer)));
            } else {
                denyReason = Mini.parse(claimTl.getOutsideWorldBorder(), this);
            }
        } else if (currentFaction.isNormal()) {
            if (myFaction.isPeaceful()) {
                // Cannot claim as peaceful
                denyReason = Mini.parse(claimTl.getPeaceful(), this, FactionResolver.of("faction", currentFaction));
            } else if (currentFaction.isPeaceful()) {
                // Cannot claim from peaceful
                denyReason = Mini.parse(claimTl.getPeacefulTarget(), this, FactionResolver.of("faction", currentFaction));
            } else if (!currentFaction.hasLandInflation()) {
                // Cannot claim other faction (perhaps based on power/land ratio)
                denyReason = Mini.parse(claimTl.getThisIsSparta(), this, FactionResolver.of("faction", currentFaction));
            } else if (currentFaction.hasLandInflation() && !Confs.main().factions().claims().isAllowOverClaim()) {
                // deny over claim when it normally would be allowed.
                denyReason = Mini.parse(claimTl.getOverclaimDisabled(), this);
            } else if (!Board.board().isBorderLocation(flocation)) {
                denyReason = Mini.parse(claimTl.getBorder(), this);
            }
        }

        if (notifyFailure && denyReason != null) {
            sendMessage(denyReason);
        }
        return denyReason == null;
    }

    private @Nullable Component contiguousChecks(Faction forFaction, FLocation fLocation) {
        var claims = Confs.main().factions().claims();
        int maxChunks = claims.getContiguousTotalChunks();
        int maxDistance = claims.getContiguousDistance();

        if (maxChunks == 0 && maxDistance == 0) {
            return null;
        }

        Set<FLocation> group = new HashSet<>();
        Deque<FLocation> queue = new ArrayDeque<>();
        group.add(fLocation);
        queue.add(fLocation);
        int minX = fLocation.x(), maxX = fLocation.x(), minZ = fLocation.z(), maxZ = fLocation.z();
        while (!queue.isEmpty()) {
            FLocation current = queue.poll();
            for (FLocation neighbor : new FLocation[]{current.relative(1, 0), current.relative(-1, 0), current.relative(0, 1), current.relative(0, -1)}) {
                if (group.contains(neighbor) || neighbor.faction() != forFaction) {
                    continue;
                }
                group.add(neighbor);
                queue.add(neighbor);
                minX = Math.min(minX, neighbor.x());
                maxX = Math.max(maxX, neighbor.x());
                minZ = Math.min(minZ, neighbor.z());
                maxZ = Math.max(maxZ, neighbor.z());
            }
        }

        var claimTl = Confs.tl().claiming().claim();
        if (maxChunks > 0 && group.size() > maxChunks) {
            return Mini.parse(claimTl.getContiguousTotalChunks(), this, Placeholder.unparsed("count", String.valueOf(maxChunks)));
        }
        if (maxDistance > 0 && ((maxX - minX + 1) > maxDistance || (maxZ - minZ + 1) > maxDistance)) {
            return Mini.parse(claimTl.getContiguousDistance(), this, Placeholder.unparsed("count", String.valueOf(maxDistance)));
        }
        return null;
    }

    @Override
    public boolean attemptClaim(Faction forFaction, Location location, boolean notifyFailure) {
        return attemptClaim(forFaction, new FLocation(location), notifyFailure);
    }

    @Override
    public boolean attemptClaim(Faction forFaction, FLocation flocation, boolean notifyFailure) {
        // notifyFailure is false if called by auto-claim; no need to notify on every failure for it
        // return value is false on failure, true on success

        Faction currentFaction = Board.board().factionAt(flocation);

        int ownedLand = forFaction.claimCount();

        if (!this.canClaimForFactionAtLocation(forFaction, flocation, notifyFailure)) {
            return false;
        }

        // if economy is enabled and they're not on the bypass list, make sure they can pay
        boolean mustPay = Econ.shouldBeUsed() && !this.adminBypass() && !forFaction.isSafeZone() && !forFaction.isWarZone();
        double cost = 0.0;
        Participator payee = null;
        if (mustPay) {
            cost = Econ.calculateClaimCost(ownedLand, currentFaction.isNormal());

            if (Confs.main().economy().getClaimUnconnectedFee() != 0.0 && forFaction.claimCount(flocation.world()) > 0 && Board.board().isDisconnectedLocation(flocation, forFaction)) {
                cost += Confs.main().economy().getClaimUnconnectedFee();
            }

            if (Confs.main().economy().isBankEnabled() && Confs.main().economy().isBankFactionPaysLandCosts() && this.hasFaction() && this.faction().hasAccess(this, PermissibleActions.ECONOMY, null)) {
                payee = this.faction();
            } else {
                payee = this;
            }

            if (!Econ.hasAtLeast(payee, cost, Confs.tl().economy().actions().getClaimTo())) {
                return false;
            }
        }

        LandClaimEvent claimEvent = new LandClaimEvent(flocation, forFaction, this);
        Bukkit.getServer().getPluginManager().callEvent(claimEvent);
        if (claimEvent.isCancelled()) {
            return false;
        }

        // then make 'em pay (if applicable)
        if (mustPay && !Econ.modifyMoney(payee, -cost, Confs.tl().economy().actions().getClaimTo(), Confs.tl().economy().actions().getClaimFor())) {
            return false;
        }

        // Was an over claim
        if (mustPay && currentFaction.isNormal() && currentFaction.hasLandInflation()) {
            // Give them money for over claiming.
            Econ.modifyMoney(payee, cost * Confs.main().economy().getOverclaimRewardMultiplier(), Confs.tl().economy().actions().getOverclaimTo(), Confs.tl().economy().actions().getOverclaimFor());
        }

        // announce success
        Set<FPlayer> informTheseFPlayers = new HashSet<>();
        informTheseFPlayers.add(this);
        informTheseFPlayers.addAll(forFaction.membersOnline(true));
        for (FPlayer fp : informTheseFPlayers) {
            fp.sendMessage(Mini.parse(Confs.tl().claiming().claim().getClaimed(), fp,
                    FPlayerResolver.of("player", this),
                    FactionResolver.of(forFaction),
                    FactionResolver.of("fromfaction", currentFaction)));
        }


        Board.board().claim(flocation, forFaction);

        forFaction.sendRichMessage(Confs.tl().claiming().claim().getClaimedRent(), FactionResolver.of(forFaction));

        if (Confs.main().logging().isLandClaims()) {
            AbstractFactionsPlugin.instance().log(this.name() + " claimed land at (" + flocation.asCoordString() + ") for the faction: " + forFaction.tag());
        }

        return true;
    }

    @Override
    public boolean attemptUnclaim(Faction forFaction, FLocation flocation, boolean notifyFailure) {
        Faction targetFaction = Board.board().factionAt(flocation);

        if (!targetFaction.equals(forFaction)) {
            if (notifyFailure) {
                this.sendRichMessage(Confs.tl().claiming().unclaim().getWrongFactionOther());
            }
            return false;
        }

        Player player = this.asPlayer();
        if (player == null) {
            return false;
        }

        var unclaimTl = Confs.tl().claiming().unclaim();
        if (targetFaction.isSafeZone()) {
            if (Permission.MANAGE_SAFE_ZONE.has(player)) {
                Board.board().unclaim(flocation);
                this.sendRichMessage(unclaimTl.getSafeZoneSuccess());

                if (Confs.main().logging().isLandUnclaims()) {
                    AbstractFactionsPlugin.instance().log(this.name() + " unclaimed land at (" + flocation.asCoordString() + ") from the faction: " + targetFaction.tag());
                }
                return true;
            } else {
                if (notifyFailure) {
                    this.sendRichMessage(unclaimTl.getSafeZoneNoPerm());
                }
                return false;
            }
        } else if (targetFaction.isWarZone()) {
            if (Permission.MANAGE_WAR_ZONE.has(player)) {
                Board.board().unclaim(flocation);
                this.sendRichMessage(unclaimTl.getWarZoneSuccess());

                if (Confs.main().logging().isLandUnclaims()) {
                    AbstractFactionsPlugin.instance().log(this.name() + " unclaimed land at (" + flocation.asCoordString() + ") from the faction: " + targetFaction.tag());
                }
                return true;
            } else {
                if (notifyFailure) {
                    this.sendRichMessage(unclaimTl.getWarZoneNoPerm());
                }
                return false;
            }
        }

        if (this.adminBypass()) {
            LandUnclaimEvent unclaimEvent = new LandUnclaimEvent(flocation, targetFaction, this);
            Bukkit.getServer().getPluginManager().callEvent(unclaimEvent);
            if (unclaimEvent.isCancelled()) {
                return false;
            }

            Board.board().unclaim(flocation);

            targetFaction.sendRichMessage(unclaimTl.getUnclaimed(),
                    FPlayerResolver.of("player", this));
            targetFaction.sendRichMessage(unclaimTl.getFactionUnclaimedRent(), FactionResolver.of(targetFaction));
            this.sendRichMessage(unclaimTl.getUnclaims());

            if (Confs.main().logging().isLandUnclaims()) {
                AbstractFactionsPlugin.instance().log(this.name() + " unclaimed land at (" + flocation.asCoordString() + ") from the faction: " + targetFaction.tag());
            }

            return true;
        }

        if (!this.hasFaction()) {
            if (notifyFailure) {
                this.sendRichMessage(unclaimTl.getNotAMember());
            }
            return false;
        }

        if (!targetFaction.hasAccess(this, PermissibleActions.TERRITORY, flocation)) {
            if (notifyFailure) {
                this.sendRichMessage(Confs.tl().claiming().claim().getCantUnclaim(),
                        FactionResolver.of(targetFaction));
            }
            return false;
        }

        if (this.faction() != targetFaction) {
            if (notifyFailure) {
                this.sendRichMessage(unclaimTl.getWrongFaction());
            }
            return false;
        }

        LandUnclaimEvent unclaimEvent = new LandUnclaimEvent(flocation, targetFaction, this);
        Bukkit.getServer().getPluginManager().callEvent(unclaimEvent);
        if (unclaimEvent.isCancelled()) {
            return false;
        }

        if (Econ.shouldBeUsed()) {
            double refund = Econ.calculateClaimRefund(this.faction().claimCount());

            if (Confs.main().economy().isBankEnabled() && Confs.main().economy().isBankFactionPaysLandCosts()) {
                if (!Econ.modifyMoney(this.faction(), refund, Confs.tl().economy().actions().getUnclaimTo(), Confs.tl().economy().actions().getUnclaimFor())) {
                    return false;
                }
            } else {
                if (!Econ.modifyMoney(this, refund, Confs.tl().economy().actions().getUnclaimTo(), Confs.tl().economy().actions().getUnclaimFor())) {
                    return false;
                }
            }
        }

        Board.board().unclaim(flocation);
        targetFaction.sendRichMessage(unclaimTl.getFactionUnclaimed(),
                FPlayerResolver.of("player", this));
        targetFaction.sendRichMessage(unclaimTl.getFactionUnclaimedRent(), FactionResolver.of(targetFaction));

        if (Confs.main().logging().isLandUnclaims()) {
            AbstractFactionsPlugin.instance().log(this.name() + " unclaimed land at (" + flocation.asCoordString() + ") from the faction: " + targetFaction.tag());
        }

        return true;
    }

    public boolean shouldBeSaved() {
        return this.hasFaction() ||
                (FactionsPlugin.instance().landRaidControl() instanceof PowerControl &&
                        (this.powerRounded() != Confs.main().factions().landRaidControl().power().getPlayerStarting() ||
                                this.powerBoost() != 0));
    }

    @SuppressWarnings("removal")
    @Override
    public void msgLegacy(String str, Object... args) {
        Player player = this.asPlayer();
        if (player == null) {
            return;
        }
        player.sendMessage(String.format(ChatColor.translateAlternateColorCodes('&', str), args));
    }

    @Override
    public @Nullable Player asPlayer() {
        return Bukkit.getPlayer(this.id);
    }

    @Override
    public boolean isOnline() {
        Player player = this.asPlayer();
        return player != null && WorldUtil.isEnabled(player);
    }

    @Override
    public void flightCheck() {
        if (Confs.main().commands().fly().isEnable() && !this.adminBypass()) {
            boolean canFly = this.canFlyAtLocation(this.lastStoodAt());
            if (this.flying() && !canFly) {
                this.flying(false);
            } else if (this.autoFlying() && !this.flying() && canFly) {
                this.flying(true);
            }
        }
    }

    @Override
    public boolean flying() {
        return isFlying;
    }

    @Override
    public void flying(boolean fly, boolean notify) {
        Player player = asPlayer();
        if (player != null) {
            player.setAllowFlight(fly);
            player.setFlying(fly);
        }

        if (notify) {
            sendRichMessage(Confs.tl().commands().fly().getChange(),
                    Placeholder.unparsed("state", fly ? "enabled" : "disabled"));
        }

        // If leaving fly mode, don't let them take fall damage for x seconds.
        if (!fly) {
            int cooldown = Confs.main().commands().fly().getFallDamageCooldown();

            // If the value is 0 or lower, make them take fall damage.
            // Otherwise, start a timer and have this cancel after a few seconds.
            // Short task so we're just doing it in method. Not clean but eh.
            if (cooldown > 0) {
                takeFallDamage(false);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        takeFallDamage(true);
                    }
                }.runTaskLater(AbstractFactionsPlugin.instance(), 20L * cooldown);
            }
        }

        isFlying = fly;
    }

    @Override
    public boolean autoFlying() {
        return isAutoFlying;
    }

    @Override
    public void autoFlying(boolean autoFly) {
        this.isAutoFlying = autoFly;
    }

    @Override
    public boolean canFlyAtLocation(FLocation location) {
        Player player = this.asPlayer();
        if (player == null) {
            return false;
        }
        Faction faction = Board.board().factionAt(location);
        if (faction.isWilderness()) {
            return Permission.FLY_WILDERNESS.has(player);
        } else if (faction.isSafeZone()) {
            return Permission.FLY_SAFEZONE.has(player);
        } else if (faction.isWarZone()) {
            return Permission.FLY_WARZONE.has(player);
        }

        // admin bypass (ops) can fly.
        if (isAdminBypassing) {
            return true;
        }

        return faction.hasAccess(this, PermissibleActions.FLY, location);
    }

    @Override
    public boolean takeFallDamage() {
        return this.shouldTakeFallDamage;
    }

    @Override
    public void takeFallDamage(boolean fallDamage) {
        this.shouldTakeFallDamage = fallDamage;
    }

    @Override
    public boolean seeChunk() {
        return seeingChunk;
    }

    @Override
    public void seeChunk(boolean seeingChunk) {
        this.seeingChunk = seeingChunk;
        AbstractFactionsPlugin.instance().seeChunkUtil().updatePlayerInfo(this.id, seeingChunk);
    }

    @Override
    public boolean flyTrail() {
        return flyTrailsState;
    }

    @Override
    public void flyTrail(boolean state) {
        flyTrailsState = state;
    }

    @Override
    public @Nullable String flyTrailEffect() {
        return flyTrailsEffect;
    }

    @Override
    public void flyTrailEffect(String effect) {
        flyTrailsEffect = effect;
    }

    @Override
    public void sendMessage(Component component) {
        if (this.asPlayer() instanceof Player player) {
            ComponentDispatcher.send(player, component);
        }
    }

    @Override
    public void sendRichMessage(String miniMessage, TagResolver... resolvers) {
        if (this.asPlayer() instanceof Player player) {
            ComponentDispatcher.send(player, Mini.parse(miniMessage, this, resolvers));
        }
    }

    @SuppressWarnings("removal")
    @Override
    public void sendMessageLegacy(String msg) {
        Player player = this.asPlayer();
        if (player == null) {
            return;
        }
        player.sendMessage(msg);
    }

    @SuppressWarnings("removal")
    @Override
    public void sendMessageLegacy(List<String> msgs) {
        Player player = this.asPlayer();
        if (player == null) {
            return;
        }
        for (String msg : msgs) {
            player.sendMessage(msg);
        }
    }

    @Override
    public int mapHeight() {
        if (this.mapHeight < 1) {
            this.mapHeight = Confs.main().map().getHeight();
        }

        return this.mapHeight;
    }

    @Override
    public void mapHeight(int height) {
        this.mapHeight = Math.min(height, (Confs.main().map().getHeight() * 2));
    }

    @Override
    public UUID uniqueId() {
        return id;
    }

    @Override
    public void cancelWarmup() {
        if (warmup != null) {
            Bukkit.getScheduler().cancelTask(warmupTask);
            warmup = null;
        }
    }

    @Override
    public WarmUpUtil.@Nullable Warmup warmup() {
        return warmup;
    }

    @Override
    public void addWarmup(WarmUpUtil.Warmup warmup, int taskId) {
        if (this.warmup != null) {
            this.cancelWarmup();
        }
        this.warmup = warmup;
        this.warmupTask = taskId;
    }

    public Set<String> bannedBy() {
        return bannedBy;
    }
}
