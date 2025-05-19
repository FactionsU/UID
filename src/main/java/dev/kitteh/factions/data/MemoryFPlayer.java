package dev.kitteh.factions.data;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Participator;
import dev.kitteh.factions.chat.ChatTarget;
import dev.kitteh.factions.command.defaults.CmdZone;
import dev.kitteh.factions.event.FPlayerLeaveEvent;
import dev.kitteh.factions.event.FactionAutoDisbandEvent;
import dev.kitteh.factions.event.LandClaimEvent;
import dev.kitteh.factions.event.LandUnclaimEvent;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.integration.Essentials;
import dev.kitteh.factions.integration.IntegrationManager;
import dev.kitteh.factions.landraidcontrol.DTRControl;
import dev.kitteh.factions.landraidcontrol.PowerControl;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.scoreboard.FScoreboard;
import dev.kitteh.factions.scoreboard.sidebar.FInfoSidebar;
import dev.kitteh.factions.tag.Tag;
import dev.kitteh.factions.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
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
@NullMarked
public abstract class MemoryFPlayer implements FPlayer {

    protected int factionId;
    protected Role role = Role.NORMAL;
    protected String title = "";
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
    protected boolean willAutoLeave = true;
    protected int mapHeight = 8; // default to old value
    protected boolean isFlying = false;
    protected boolean isAutoFlying = false;
    protected boolean flyTrailsState = false;
    protected @Nullable String flyTrailsEffect = null;

    protected boolean seeingChunk = false;

    protected FLocation lastStoodAt = new FLocation(); // Where did this player stand the last time we checked?
    protected transient boolean mapAutoUpdating;
    protected transient @Nullable Faction autoClaimFor;
    protected transient @Nullable String autoSetZone;
    protected transient @Nullable Faction autoUnclaimFor;
    protected transient boolean loginPvpDisabled;
    protected transient long lastFrostwalkerMessage;
    protected transient boolean shouldTakeFallDamage = true;
    protected transient @Nullable OfflinePlayer offlinePlayer;

    public void cleanupDeserialization() {
        this.shouldTakeFallDamage = true;
    }

    public void onLogInOut() {
        if (this.asPlayer() instanceof Player player) {
            this.kills = player.getStatistic(Statistic.PLAYER_KILLS);
            this.deaths = player.getStatistic(Statistic.DEATHS);
            this.autoLeaveExempt(player.hasPermission(Permission.AUTO_LEAVE_BYPASS.node));
        }
        this.lastLoginTime = System.currentTimeMillis();
        if (FactionsPlugin.instance().conf().factions().pvp().getNoPVPDamageToOthersForXSecondsAfterLogin() > 0) {
            this.loginPvpDisabled = true;
        }
    }

    @Override
    public Faction faction() {
        Faction faction = Factions.factions().get(this.factionId);
        if (faction == null) {
            AbstractFactionsPlugin.getInstance().getLogger().warning("Found null faction (id " + this.factionId + ") for player " + this.name());
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
        if (FactionsPlugin.instance().integrationManager().isEnabled(IntegrationManager.Integration.ESS) && Essentials.isVanished(player)) {
            return true;
        }
        if (player != null) {
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
                (this.chatTarget instanceof ChatTarget.Relation && !FactionsPlugin.instance().conf().factions().chat().internalChat().isRelationChatEnabled()) ||
                (this.chatTarget instanceof ChatTarget.Role && !FactionsPlugin.instance().conf().factions().chat().internalChat().isFactionMemberChatEnabled())) {
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
                this.offlinePlayer = AbstractFactionsPlugin.getInstance().getOfflinePlayer(this.name, this.id);
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
        this.resetFactionData();
        this.power = FactionsPlugin.instance().conf().factions().landRaidControl().power().getPlayerStarting();
        this.lastPowerUpdateTime = System.currentTimeMillis();
        this.lastLoginTime = System.currentTimeMillis();
        this.mapAutoUpdating = false;
        this.autoClaimFor = null;
        this.loginPvpDisabled = FactionsPlugin.instance().conf().factions().pvp().getNoPVPDamageToOthersForXSecondsAfterLogin() > 0;
        this.powerBoost = 0.0;
        this.kills = 0;
        this.deaths = 0;
        this.mapHeight = FactionsPlugin.instance().conf().map().getHeight();

        Faction newFaction = Factions.factions().get(FactionsPlugin.instance().conf().factions().other().getNewPlayerStartingFactionID());
        if (newFaction != null) {
            this.factionId = FactionsPlugin.instance().conf().factions().other().getNewPlayerStartingFactionID();
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
        this.title = "";
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
        if (this.lastLoginTime + (FactionsPlugin.instance().conf().factions().pvp().getNoPVPDamageToOthersForXSecondsAfterLogin() * 1000L) < System.currentTimeMillis()) {
            this.loginPvpDisabled = false;
            return false;
        }
        return true;
    }

    @Override
    public FLocation lastStoodAt() {
        return this.lastStoodAt;
    }

    @Override
    public void lastStoodAt(FLocation flocation) {
        this.lastStoodAt = flocation;
    }

    //----------------------------------------------//
    // Title, Name, Faction Tag and Chat
    //----------------------------------------------//

    // Base:

    @Override
    public String title() {
        return this.hasFaction() ? title : TL.NOFACTION_PREFIX.toString();
    }

    @Override
    public void title(String title) {
        this.title = title;
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
                if (fplayer.name() == null) {
                    continue;
                }
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
                                NameLookup lookup = FactionsPlugin.instance().gson().fromJson(new InputStreamReader(url.openStream()), NameLookup.class);
                                String newName = lookup.name;
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        //noinspection ConstantValue
                                        if (newName != null && fplayer.name().equals(uuidName)) {
                                            ((MemoryFPlayer) fplayer).setName(newName);
                                        }
                                    }
                                }.runTask(AbstractFactionsPlugin.getInstance());
                            } catch (Exception ignored) {
                            }
                        }
                    }.runTaskAsynchronously(AbstractFactionsPlugin.getInstance());
                }
            }
        }
        this.name = name;
    }

    public String tagOrEmpty() {
        return this.hasFaction() ? this.faction().tag() : "";
    }

    // Base concatenations:

    public String nameAndSomething(String something) {
        String ret = this.role.getPrefix();
        if (!something.isEmpty()) {
            ret += something + " ";
        }
        ret += this.name();
        return ret;
    }

    @Override
    public String nameWithTitle() {
        return this.nameAndSomething(this.title());
    }

    @Override
    public String nameWithTag() {
        return this.nameAndSomething(this.tagOrEmpty());
    }

    // Chat Tag:
    // These are injected into the format of global chat messages.

    @Override
    public String chatTag() {
        return this.hasFaction() ? String.format(FactionsPlugin.instance().conf().factions().chat().getTagFormat(), this.role().getPrefix() + this.tagOrEmpty()) : TL.NOFACTION_PREFIX.toString();
    }

    // Colored Chat Tag
    @Override
    public String chatTag(@Nullable Participator participator) {
        return this.hasFaction() ? this.relationTo(participator).chatColor() + chatTag() : TL.NOFACTION_PREFIX.toString();
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

    //----------------------------------------------//
    // Power
    //----------------------------------------------//
    @Override
    public double power() {
        this.updatePower();
        return this.power;
    }

    @Override
    public void power(double power) {
        this.power = Math.min(Math.max(this.powerMin(), power), this.powerMax());
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
        return FactionsPlugin.instance().conf().factions().landRaidControl().power().getPlayerMax() + this.powerBoost;
    }

    @Override
    public double powerMin() {
        return FactionsPlugin.instance().conf().factions().landRaidControl().power().getPlayerMin() + this.powerBoost;
    }

    @Override
    public int powerRounded() {
        return (int) Math.round(this.power());
    }

    @Override
    public int powerMaxRounded() {
        return (int) Math.round(this.powerMax());
    }

    @Override
    public int powerMinRounded() {
        return (int) Math.round(this.powerMin());
    }

    @Override
    public void updatePower() {
        if (!this.isOnline()) {
            losePowerFromBeingOffline();
            if (!FactionsPlugin.instance().conf().factions().landRaidControl().power().isRegenOffline()) {
                return;
            }
        } else if (hasFaction() && faction().isPowerFrozen()) {
            return; // Don't let power regen if faction power is frozen.
        } else if (FactionsPlugin.instance().conf().plugins().essentialsX().isPreventRegenWhileAfk() && Essentials.isAfk(this.asPlayer())) {
            return;
        }
        long now = System.currentTimeMillis();
        long millisPassed = now - this.lastPowerUpdateTime;
        this.lastPowerUpdateTime = now;

        Player thisPlayer = this.asPlayer();
        if (thisPlayer != null && thisPlayer.isDead()) {
            return;  // don't let dead players regain power until they respawn
        }

        int millisPerMinute = 60 * 1000;
        this.alterPower(millisPassed * FactionsPlugin.instance().conf().factions().landRaidControl().power().getPowerPerMinute() / millisPerMinute);
    }

    @Override
    public void losePowerFromBeingOffline() {
        long now = System.currentTimeMillis();
        if (FactionsPlugin.instance().conf().factions().landRaidControl().power().getOfflineLossPerDay() > 0.0 && this.power > FactionsPlugin.instance().conf().factions().landRaidControl().power().getOfflineLossLimit()) {
            long millisPassed = now - this.lastPowerUpdateTime;

            double loss = millisPassed * FactionsPlugin.instance().conf().factions().landRaidControl().power().getOfflineLossPerDay() / (24 * 60 * 60 * 1000);
            if (this.power - loss < FactionsPlugin.instance().conf().factions().landRaidControl().power().getOfflineLossLimit()) {
                loss = this.power;
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

    //----------------------------------------------//
    // Territory
    //----------------------------------------------//
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
        boolean showTitle = FactionsPlugin.instance().conf().factions().enterTitles().isEnabled();
        boolean showChat = true;
        Player player = asPlayer();

        if (showTitle && player != null) {
            int in = FactionsPlugin.instance().conf().factions().enterTitles().getFadeIn();
            int stay = FactionsPlugin.instance().conf().factions().enterTitles().getStay();
            int out = FactionsPlugin.instance().conf().factions().enterTitles().getFadeOut();

            String title = Tag.parsePlain(toShow, this, FactionsPlugin.instance().conf().factions().enterTitles().getTitle());
            String sub = AbstractFactionsPlugin.getInstance().txt().parse(Tag.parsePlain(toShow, this, FactionsPlugin.instance().conf().factions().enterTitles().getSubtitle()));

            player.sendTitle(title, sub, in, stay, out);

            showChat = FactionsPlugin.instance().conf().factions().enterTitles().isAlsoShowChat();
        }

        if (showInfoBoard(toShow)) {
            FScoreboard.get(this).setTemporarySidebar(new FInfoSidebar(toShow));
            showChat = FactionsPlugin.instance().conf().scoreboard().info().isAlsoSendChat();
        }
        if (showChat) {
            this.sendMessage(AbstractFactionsPlugin.getInstance().txt().parse(TL.FACTION_LEAVE.format(from.tagString(this), toShow.tagString(this))));
        }
    }

    /**
     * Check if the scoreboard should be shown. Simple method to be used by above method.
     *
     * @param toShow Faction to be shown.
     * @return true if should show, otherwise false.
     */
    public boolean showInfoBoard(Faction toShow) {
        return showScoreboard && !toShow.isWarZone() && !toShow.isWilderness() && !toShow.isSafeZone() && FactionsPlugin.instance().conf().scoreboard().info().isEnabled() && FScoreboard.get(this) != null;
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

        boolean perm = myFaction.permanent();

        if (!perm && this.role() == Role.ADMIN && myFaction.members().size() > 1) {
            msg(TL.LEAVE_PASSADMIN);
            return;
        }

        if (makePay && !FactionsPlugin.instance().landRaidControl().canLeaveFaction(this)) {
            return;
        }

        // if economy is enabled and they're not on the bypass list, make sure they can pay
        if (econMakePay && !Econ.hasAtLeast(this, FactionsPlugin.instance().conf().economy().getCostLeave(), TL.LEAVE_TOLEAVE.toString())) {
            return;
        }

        FPlayerLeaveEvent leaveEvent = new FPlayerLeaveEvent(this, myFaction, FPlayerLeaveEvent.Reason.LEAVE);
        Bukkit.getServer().getPluginManager().callEvent(leaveEvent);
        if (leaveEvent.isCancelled()) {
            return;
        }

        // then make 'em pay (if applicable)
        if (econMakePay && !Econ.modifyMoney(this, -FactionsPlugin.instance().conf().economy().getCostLeave(), TL.LEAVE_TOLEAVE.toString(), TL.LEAVE_FORLEAVE.toString())) {
            return;
        }

        // Am I the last one in the faction?
        if (myFaction.members().size() == 1) {
            // Transfer all money
            if (Econ.shouldBeUsed() && FactionsPlugin.instance().conf().economy().isBankEnabled()) {
                if (!perm || FactionsPlugin.instance().conf().economy().isBankPermanentFactionSendBalanceToLastLeaver()) {
                    //Give all the faction's money to the disbander
                    double amount = Econ.getBalance(myFaction);
                    Econ.transferMoney(this, myFaction, this, amount, false);

                    if (amount > 0.0) {
                        String amountString = Econ.moneyString(amount);
                        this.msg(TL.COMMAND_DISBAND_HOLDINGS, amountString);
                        //TODO: Format this correctly and translate
                        FactionsPlugin.instance().log(this.name() + " has been given bank holdings of " + amountString + " from disbanding " + myFaction.tag() + ".");
                    }
                }
            }
        }

        if (myFaction.isNormal()) {
            for (FPlayer fplayer : myFaction.membersOnline(true)) {
                fplayer.msg(TL.LEAVE_LEFT, this.describeTo(fplayer, true), myFaction.describeTo(fplayer));
            }

            if (FactionsPlugin.instance().conf().logging().isFactionLeave()) {
                FactionsPlugin.instance().log(TL.LEAVE_LEFT.format(this.name(), myFaction.tag()));
            }
        }

        this.resetFactionData(true);
        if (FactionsPlugin.instance().conf().commands().fly().isEnable()) {
            flying(false, false);
        }

        if (myFaction.isNormal() && !perm && myFaction.members().isEmpty()) {
            // Remove this faction
            for (FPlayer fplayer : FPlayers.fPlayers().online()) {
                fplayer.msg(TL.LEAVE_DISBANDED, myFaction.describeTo(fplayer, true));
            }

            AbstractFactionsPlugin.getInstance().getServer().getPluginManager().callEvent(new FactionAutoDisbandEvent(myFaction));
            Factions.factions().remove(myFaction);
            if (FactionsPlugin.instance().conf().logging().isFactionDisband()) {
                FactionsPlugin.instance().log(TL.LEAVE_DISBANDEDLOG.format(myFaction.tag(), "" + myFaction.id(), this.name()));
            }
        }
    }

    @Override
    public void attemptAutoSetZone(FLocation flocation) {
        if (this.hasFaction() && this.autoSetZone != null) {
            Faction faction = this.faction();
            Faction.Zone zone = faction.zones().get(this.autoSetZone);
            if (zone == null) {
                this.autoSetZone = null; // TODO should we warn about this?
                return;
            }
            Faction.Zone currentZone = faction.zones().get(flocation);
            if (currentZone == zone) {
                return;
            }
            if (CmdZone.claim(this, faction, flocation, zone, false)) {
                this.sendMessage(Mini.parse(FactionsPlugin.instance().tl().commands().zone().claim().getSuccess(), Placeholder.unparsed("oldzone", currentZone.name()), Placeholder.unparsed("newzone", zone.name())));
            }
        }
    }

    @Override
    public boolean canClaimForFaction(Faction forFaction) {
        Player player = this.asPlayer();
        if (player == null) {
            return false;
        }
        return this.adminBypass() || !forFaction.isWilderness() && (forFaction == this.faction() && this.faction().hasAccess(this, PermissibleActions.TERRITORY, null)) || (forFaction.isSafeZone() && Permission.MANAGE_SAFE_ZONE.has(player)) || (forFaction.isWarZone() && Permission.MANAGE_WAR_ZONE.has(player));
    }

    @Override
    public boolean canClaimForFactionAtLocation(Faction forFaction, FLocation flocation, boolean notifyFailure) {
        Player player = this.asPlayer();
        if (player == null) {
            return false;
        }
        AbstractFactionsPlugin plugin = AbstractFactionsPlugin.getInstance();
        String denyReason = null;
        Faction myFaction = faction();
        Faction currentFaction = Board.board().factionAt(flocation);
        int ownedLand = forFaction.claimCount();
        int factionBuffer = plugin.conf().factions().claims().getBufferZone();
        int worldBuffer = plugin.conf().worldBorder().getBuffer();

        if (plugin.conf().worldGuard().isCheckingEither() && plugin.getWorldguard() != null && plugin.getWorldguard().checkForRegionsInChunk(flocation.chunk())) {
            // Checks for WorldGuard regions in the chunk attempting to be claimed
            denyReason = plugin.txt().parse(TL.CLAIM_PROTECTED.toString());
        } else if (plugin.conf().factions().claims().getWorldsNoClaiming().contains(flocation.worldName())) {
            // Cannot claim in this world
            denyReason = plugin.txt().parse(TL.CLAIM_DISABLED.toString());
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
            denyReason = plugin.txt().parse(TL.CLAIM_CANTCLAIM.toString(), forFaction.describeTo(this));
        } else if (forFaction == currentFaction) {
            // Already owned by this faction, nitwit
            denyReason = plugin.txt().parse(TL.CLAIM_ALREADYOWN.toString(), forFaction.describeTo(this, true));
        } else if (forFaction.members().size() < plugin.conf().factions().claims().getRequireMinFactionMembers()) {
            // Need more members in order to claim land
            denyReason = plugin.txt().parse(TL.CLAIM_MEMBERS.toString(), plugin.conf().factions().claims().getRequireMinFactionMembers());
        } else if (currentFaction.isSafeZone()) {
            // Cannot claim safezone
            denyReason = plugin.txt().parse(TL.CLAIM_SAFEZONE.toString());
        } else if (currentFaction.isWarZone()) {
            // Cannot claim warzone
            denyReason = plugin.txt().parse(TL.CLAIM_WARZONE.toString());
        } else if (plugin.landRaidControl() instanceof PowerControl && ownedLand >= forFaction.power()) {
            // Already own at least as much land as power
            denyReason = plugin.txt().parse(TL.CLAIM_POWER.toString());
        } else if (plugin.landRaidControl() instanceof DTRControl && ownedLand >= plugin.landRaidControl().landLimit(forFaction)) {
            // Already own at least as much land as land limit (DTR)
            denyReason = plugin.txt().parse(TL.CLAIM_DTR_LAND.toString());
        } else if (plugin.conf().factions().claims().getLandsMax() != 0 && ownedLand >= plugin.conf().factions().claims().getLandsMax() && forFaction.isNormal()) {
            // Land limit reached
            denyReason = plugin.txt().parse(TL.CLAIM_LIMIT.toString());
        } else if (currentFaction.relationTo(forFaction) == Relation.ALLY) {
            // // Can't claim ally
            denyReason = plugin.txt().parse(TL.CLAIM_ALLY.toString());
        } else if (plugin.conf().factions().claims().isMustBeConnected() && !this.adminBypass() && myFaction.claimCount(flocation.world()) > 0 && Board.board().isDisconnectedLocation(flocation, myFaction) && (!plugin.conf().factions().claims().isCanBeUnconnectedIfOwnedByOtherFaction() || !currentFaction.isNormal())) {
            // Must be contiguous/connected
            if (plugin.conf().factions().claims().isCanBeUnconnectedIfOwnedByOtherFaction()) {
                denyReason = plugin.txt().parse(TL.CLAIM_CONTIGIOUS.toString());
            } else {
                denyReason = plugin.txt().parse(TL.CLAIM_FACTIONCONTIGUOUS.toString());
            }
        } else if (!(currentFaction.isNormal() && plugin.conf().factions().claims().isAllowOverClaimAndIgnoringBuffer() && currentFaction.hasLandInflation()) && factionBuffer > 0 && Board.board().hasFactionWithin(flocation, myFaction, factionBuffer)) {
            // Too close to buffer
            denyReason = plugin.txt().parse(TL.CLAIM_TOOCLOSETOOTHERFACTION.format(factionBuffer));
        } else if (flocation.isOutsideWorldBorder(worldBuffer)) {
            // Border buffer
            if (worldBuffer > 0) {
                denyReason = plugin.txt().parse(TL.CLAIM_OUTSIDEBORDERBUFFER.format(worldBuffer));
            } else {
                denyReason = plugin.txt().parse(TL.CLAIM_OUTSIDEWORLDBORDER.toString());
            }
        } else if (currentFaction.isNormal()) {
            if (myFaction.peaceful()) {
                // Cannot claim as peaceful
                denyReason = plugin.txt().parse(TL.CLAIM_PEACEFUL.toString(), currentFaction.tagString(this));
            } else if (currentFaction.peaceful()) {
                // Cannot claim from peaceful
                denyReason = plugin.txt().parse(TL.CLAIM_PEACEFULTARGET.toString(), currentFaction.tagString(this));
            } else if (!currentFaction.hasLandInflation()) {
                // Cannot claim other faction (perhaps based on power/land ratio)
                // TODO more messages WARN current faction most importantly
                denyReason = plugin.txt().parse(TL.CLAIM_THISISSPARTA.toString(), currentFaction.tagString(this));
            } else if (currentFaction.hasLandInflation() && !plugin.conf().factions().claims().isAllowOverClaim()) {
                // deny over claim when it normally would be allowed.
                denyReason = plugin.txt().parse(TL.CLAIM_OVERCLAIM_DISABLED.toString());
            } else if (!Board.board().isBorderLocation(flocation)) {
                denyReason = plugin.txt().parse(TL.CLAIM_BORDER.toString());
            }
        }
        // TODO: Add more else if statements.

        if (notifyFailure && denyReason != null) {
            msg(denyReason);
        }
        return denyReason == null;
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

            if (FactionsPlugin.instance().conf().economy().getClaimUnconnectedFee() != 0.0 && forFaction.claimCount(flocation.world()) > 0 && Board.board().isDisconnectedLocation(flocation, forFaction)) {
                cost += FactionsPlugin.instance().conf().economy().getClaimUnconnectedFee();
            }

            if (FactionsPlugin.instance().conf().economy().isBankEnabled() && FactionsPlugin.instance().conf().economy().isBankFactionPaysLandCosts() && this.hasFaction() && this.faction().hasAccess(this, PermissibleActions.ECONOMY, null)) {
                payee = this.faction();
            } else {
                payee = this;
            }

            if (!Econ.hasAtLeast(payee, cost, TL.CLAIM_TOCLAIM.toString())) {
                return false;
            }
        }

        LandClaimEvent claimEvent = new LandClaimEvent(flocation, forFaction, this);
        Bukkit.getServer().getPluginManager().callEvent(claimEvent);
        if (claimEvent.isCancelled()) {
            return false;
        }

        // then make 'em pay (if applicable)
        if (mustPay && !Econ.modifyMoney(payee, -cost, TL.CLAIM_TOCLAIM.toString(), TL.CLAIM_FORCLAIM.toString())) {
            return false;
        }

        // Was an over claim
        if (mustPay && currentFaction.isNormal() && currentFaction.hasLandInflation()) {
            // Give them money for over claiming.
            Econ.modifyMoney(payee, FactionsPlugin.instance().conf().economy().getOverclaimRewardMultiplier(), TL.CLAIM_TOOVERCLAIM.toString(), TL.CLAIM_FOROVERCLAIM.toString());
        }

        // announce success
        Set<FPlayer> informTheseFPlayers = new HashSet<>();
        informTheseFPlayers.add(this);
        informTheseFPlayers.addAll(forFaction.membersOnline(true));
        for (FPlayer fp : informTheseFPlayers) {
            fp.msg(TL.CLAIM_CLAIMED, this.describeTo(fp, true), forFaction.describeTo(fp), currentFaction.describeTo(fp));
        }

        Board.board().claim(flocation, forFaction);

        if (FactionsPlugin.instance().conf().logging().isLandClaims()) {
            FactionsPlugin.instance().log(TL.CLAIM_CLAIMEDLOG.toString(), this.name(), flocation.coordString(), forFaction.tag());
        }

        return true;
    }

    @Override
    public boolean attemptUnclaim(Faction forFaction, FLocation flocation, boolean notifyFailure) {
        Faction targetFaction = Board.board().factionAt(flocation);

        if (!targetFaction.equals(forFaction)) {
            this.msg(TL.COMMAND_UNCLAIM_WRONGFACTIONOTHER);
            return false;
        }

        Player player = this.asPlayer();
        if (player == null) {
            return false;
        }

        if (targetFaction.isSafeZone()) {
            if (Permission.MANAGE_SAFE_ZONE.has(player)) {
                Board.board().unclaim(flocation);
                this.msg(TL.COMMAND_UNCLAIM_SAFEZONE_SUCCESS);

                if (FactionsPlugin.instance().conf().logging().isLandUnclaims()) {
                    FactionsPlugin.instance().log(TL.COMMAND_UNCLAIM_LOG.format(this.name(), flocation.coordString(), targetFaction.tag()));
                }
                return true;
            } else {
                if (notifyFailure) {
                    this.msg(TL.COMMAND_UNCLAIM_SAFEZONE_NOPERM);
                }
                return false;
            }
        } else if (targetFaction.isWarZone()) {
            if (Permission.MANAGE_WAR_ZONE.has(player)) {
                Board.board().unclaim(flocation);
                this.msg(TL.COMMAND_UNCLAIM_WARZONE_SUCCESS);

                if (FactionsPlugin.instance().conf().logging().isLandUnclaims()) {
                    FactionsPlugin.instance().log(TL.COMMAND_UNCLAIM_LOG.format(this.name(), flocation.coordString(), targetFaction.tag()));
                }
                return true;
            } else {
                if (notifyFailure) {
                    this.msg(TL.COMMAND_UNCLAIM_WARZONE_NOPERM);
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

            targetFaction.msg(TL.COMMAND_UNCLAIM_UNCLAIMED, this.describeTo(targetFaction, true));
            this.msg(TL.COMMAND_UNCLAIM_UNCLAIMS);

            if (FactionsPlugin.instance().conf().logging().isLandUnclaims()) {
                FactionsPlugin.instance().log(TL.COMMAND_UNCLAIM_LOG.format(this.name(), flocation.coordString(), targetFaction.tag()));
            }

            return true;
        }

        if (!this.hasFaction()) {
            if (notifyFailure) {
                this.msg(TL.COMMAND_UNCLAIM_NOTAMEMBER);
            }
            return false;
        }

        if (!targetFaction.hasAccess(this, PermissibleActions.TERRITORY, flocation)) {
            if (notifyFailure) {
                this.msg(TL.CLAIM_CANTUNCLAIM, targetFaction.describeTo(this));
            }
            return false;
        }

        if (this.faction() != targetFaction) {
            if (notifyFailure) {
                this.msg(TL.COMMAND_UNCLAIM_WRONGFACTION);
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

            if (FactionsPlugin.instance().conf().economy().isBankEnabled() && FactionsPlugin.instance().conf().economy().isBankFactionPaysLandCosts()) {
                if (!Econ.modifyMoney(this.faction(), refund, TL.COMMAND_UNCLAIM_TOUNCLAIM.toString(), TL.COMMAND_UNCLAIM_FORUNCLAIM.toString())) {
                    return false;
                }
            } else {
                if (!Econ.modifyMoney(this, refund, TL.COMMAND_UNCLAIM_TOUNCLAIM.toString(), TL.COMMAND_UNCLAIM_FORUNCLAIM.toString())) {
                    return false;
                }
            }
        }

        Board.board().unclaim(flocation);
        this.faction().msg(TL.COMMAND_UNCLAIM_FACTIONUNCLAIMED, this.describeTo(this.faction(), true));

        if (FactionsPlugin.instance().conf().logging().isLandUnclaims()) {
            FactionsPlugin.instance().log(TL.COMMAND_UNCLAIM_LOG.format(this.name(), flocation.coordString(), targetFaction.tag()));
        }

        return true;
    }

    public boolean shouldBeSaved() {
        return this.hasFaction() ||
                (FactionsPlugin.instance().landRaidControl() instanceof PowerControl &&
                        (this.powerRounded() != FactionsPlugin.instance().conf().factions().landRaidControl().power().getPlayerStarting() ||
                                this.powerBoost() != 0));
    }

    @Override
    public void msg(@NonNull String str, @NonNull Object @NonNull ... args) {
        this.sendMessage(AbstractFactionsPlugin.getInstance().txt().parse(str, args));
    }

    @Override
    public @Nullable Player asPlayer() {
        return Bukkit.getPlayer(this.id);
    }

    @Override
    public boolean isOnline() {
        Player player = this.asPlayer();
        return player != null && WorldUtil.isEnabled(player.getWorld());
    }

    @Override
    public void flightCheck() {
        if (FactionsPlugin.instance().conf().commands().fly().isEnable() && !this.adminBypass()) {
            boolean canFly = this.canFlyAtLocation(this.lastStoodAt());
            if (this.flying() && !canFly) {
                this.flying(false, false);
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
    public void flying(boolean fly) {
        flying(fly, false);
    }

    @Override
    public void flying(boolean fly, boolean damage) {
        Player player = asPlayer();
        if (player != null) {
            player.setAllowFlight(fly);
            player.setFlying(fly);
        }

        if (!damage) {
            msg(TL.COMMAND_FLY_CHANGE, fly ? "enabled" : "disabled");
        } else {
            msg(TL.COMMAND_FLY_DAMAGE);
        }

        // If leaving fly mode, don't let them take fall damage for x seconds.
        if (!fly) {
            int cooldown = FactionsPlugin.instance().conf().commands().fly().getFallDamageCooldown();

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
                }.runTaskLater(AbstractFactionsPlugin.getInstance(), 20L * cooldown);
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
        msg(TL.COMMAND_FLY_AUTO, autoFly ? "enabled" : "disabled");
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
        FactionsPlugin.instance().seeChunkUtil().updatePlayerInfo(this.id, seeingChunk);
    }

    @Override
    public boolean flyTrail() {
        return flyTrailsState;
    }

    @Override
    public void flyTrail(boolean state) {
        flyTrailsState = state;
        msg(TL.COMMAND_FLYTRAILS_CHANGE, state ? "enabled" : "disabled");
    }

    @Override
    public @Nullable String flyTrailEffect() {
        return flyTrailsEffect;
    }

    @Override
    public void flyTrailEffect(String effect) {
        flyTrailsEffect = effect;
        msg(TL.COMMAND_FLYTRAILS_PARTICLE_CHANGE, effect);
    }

    @Override
    public void sendMessage(@NonNull Component component) {
        if (this.asPlayer() instanceof Player player) {
            ComponentDispatcher.send(player, component);
        }
    }

    @Override
    public void sendMessage(String msg) {
        if (msg.contains("{null}")) {
            return; // user wants this message to not send
        }
        Player player = this.asPlayer();
        if (player == null) {
            return;
        }
        if (msg.contains("/n/")) {
            for (String s : msg.split("/n/")) {
                player.sendMessage(s);
            }
            return;
        }

        player.sendMessage(msg);
    }

    @Override
    public void sendMessage(List<String> msgs) {
        for (String msg : msgs) {
            this.sendMessage(msg);
        }
    }

    @Override
    public int mapHeight() {
        if (this.mapHeight < 1) {
            this.mapHeight = FactionsPlugin.instance().conf().map().getHeight();
        }

        return this.mapHeight;
    }

    @Override
    public void mapHeight(int height) {
        this.mapHeight = Math.min(height, (FactionsPlugin.instance().conf().map().getHeight() * 2));
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
}
