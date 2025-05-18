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
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
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
        if (this.getPlayer() instanceof Player player) {
            this.kills = player.getStatistic(Statistic.PLAYER_KILLS);
            this.deaths = player.getStatistic(Statistic.DEATHS);
            this.setAutoLeaveExempt(player.hasPermission(Permission.AUTO_LEAVE_BYPASS.node));
        }
    }

    @Override
    public Faction getFaction() {
        Faction faction = Factions.getInstance().getFactionById(this.factionId);
        if (faction == null) {
            AbstractFactionsPlugin.getInstance().getLogger().warning("Found null faction (id " + this.factionId + ") for player " + this.getName());
            this.resetFactionData(true);
            faction = Factions.getInstance().getWilderness();
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
    public void setFaction(Faction faction) {
        Faction oldFaction = this.getFaction();
        oldFaction.removeFPlayer(this);
        faction.addFPlayer(this);
        this.factionId = faction.getId();
    }

    @Override
    public void setMonitorJoins(boolean monitor) {
        this.monitorJoins = monitor;
    }

    @Override
    public boolean isMonitoringJoins() {
        return this.monitorJoins;
    }

    @Override
    public Role getRole() {
        return this.role;
    }

    @Override
    public void setRole(Role role) {
        this.role = Objects.requireNonNull(role);
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
    public boolean isAutoLeaveExempt() {
        return !this.willAutoLeave;
    }

    @Override
    public void setAutoLeaveExempt(boolean exempt) {
        this.willAutoLeave = !exempt;
    }

    @Override
    public long getLastFrostwalkerMessageTime() {
        return this.lastFrostwalkerMessage;
    }

    @Override
    public void setLastFrostwalkerMessageTime() {
        this.lastFrostwalkerMessage = System.currentTimeMillis();
    }

    @Override
    public @Nullable Faction getAutoClaimFor() {
        return autoClaimFor;
    }

    @Override
    public void setAutoClaimFor(@Nullable Faction faction) {
        this.autoClaimFor = faction;
        if (faction != null) {
            this.autoUnclaimFor = null;
        }
    }

    @Override
    public @Nullable String getAutoSetZone() {
        return this.autoSetZone;
    }

    @Override
    public void setAutoSetZone(@Nullable String zone) {
        this.autoSetZone = zone;
    }

    @Override
    public @Nullable Faction getAutoUnclaimFor() {
        return autoUnclaimFor;
    }

    @Override
    public void setAutoUnclaimFor(@Nullable Faction faction) {
        this.autoUnclaimFor = faction;
        if (faction != null) {
            this.autoClaimFor = null;
            this.autoSetZone = null;
        }
    }

    @Override
    public boolean isAdminBypassing() {
        return this.isAdminBypassing;
    }

    @Override
    public boolean isVanished() {
        Player player = this.getPlayer();
        if (FactionsPlugin.getInstance().getIntegrationManager().isEnabled(IntegrationManager.Integration.ESS) && Essentials.isVanished(player)) {
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
    public void setIsAdminBypassing(boolean val) {
        this.isAdminBypassing = val;
        if (this.getPlayer() instanceof Player player) {
            player.updateCommands();
        }
    }

    @Override
    public void setChatTarget(ChatTarget chatTarget) {
        this.chatTarget = Objects.requireNonNull(chatTarget);
    }

    @Override
    public ChatTarget getChatTarget() {
        //noinspection ConstantValue
        if (this.chatTarget == null || this.factionId == Factions.ID_WILDERNESS ||
                (this.chatTarget instanceof ChatTarget.Relation && !FactionsPlugin.getInstance().conf().factions().chat().internalChat().isRelationChatEnabled()) ||
                (this.chatTarget instanceof ChatTarget.Role && !FactionsPlugin.getInstance().conf().factions().chat().internalChat().isFactionMemberChatEnabled())) {
            this.chatTarget = ChatTarget.PUBLIC;
        }
        return this.chatTarget;
    }

    @Override
    public void setIgnoreAllianceChat(boolean ignore) {
        this.ignoreAllianceChat = ignore;
    }

    @Override
    public boolean isIgnoreAllianceChat() {
        return ignoreAllianceChat;
    }

    @Override
    public void setIgnoreTruceChat(boolean ignore) {
        this.ignoreTruceChat = ignore;
    }

    @Override
    public boolean isIgnoreTruceChat() {
        return ignoreTruceChat;
    }

    @Override
    public void setSpyingChat(boolean chatSpying) {
        this.spyingChat = chatSpying;
    }

    @Override
    public boolean isSpyingChat() {
        return spyingChat;
    }

    @Override
    public OfflinePlayer getOfflinePlayer() {
        if (this.offlinePlayer == null) {
            this.offlinePlayer = Bukkit.getPlayer(this.id);
            if (this.offlinePlayer == null) {
                this.offlinePlayer = AbstractFactionsPlugin.getInstance().getOfflinePlayer(this.name, this.id);
            }
        }
        return this.offlinePlayer;
    }

    @Override
    public void setOfflinePlayer(@Nullable Player player) {
        this.offlinePlayer = player;
    }

    public MemoryFPlayer(UUID id) {
        this.id = id;
        this.name = id.toString();
        this.resetFactionData();
        this.power = FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getPlayerStarting();
        this.lastPowerUpdateTime = System.currentTimeMillis();
        this.lastLoginTime = System.currentTimeMillis();
        this.mapAutoUpdating = false;
        this.autoClaimFor = null;
        this.loginPvpDisabled = FactionsPlugin.getInstance().conf().factions().pvp().getNoPVPDamageToOthersForXSecondsAfterLogin() > 0;
        this.powerBoost = 0.0;
        this.kills = 0;
        this.deaths = 0;
        this.mapHeight = FactionsPlugin.getInstance().conf().map().getHeight();

        Faction newFaction = Factions.getInstance().getFactionById(FactionsPlugin.getInstance().conf().factions().other().getNewPlayerStartingFactionID());
        if (newFaction != null) {
            this.factionId = FactionsPlugin.getInstance().conf().factions().other().getNewPlayerStartingFactionID();
        }
    }

    @Override
    public void resetFactionData() {
        this.resetFactionData(false);
    }

    @Override
    public void resetFactionData(boolean updateCommands) {
        // clean up any territory ownership in old faction, if there is one
        Faction currentFaction = Factions.getInstance().getFactionById(this.factionId);
        if (currentFaction != null) {
            currentFaction.removeFPlayer(this);
        }

        this.factionId = Factions.ID_WILDERNESS; // The default neutral faction
        this.role = Role.NORMAL;
        this.title = "";
        this.autoClaimFor = null;
        this.autoUnclaimFor = null;
        this.autoSetZone = null;

        if (updateCommands && this.getPlayer() instanceof Player player) {
            player.updateCommands();
        }
    }

    @Override
    public long getLastLoginTime() {
        return lastLoginTime;
    }

    @Override
    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
        if (FactionsPlugin.getInstance().conf().factions().pvp().getNoPVPDamageToOthersForXSecondsAfterLogin() > 0) {
            this.loginPvpDisabled = true;
        }
    }

    @Override
    public boolean isMapAutoUpdating() {
        return mapAutoUpdating;
    }

    @Override
    public void setMapAutoUpdating(boolean mapAutoUpdating) {
        this.mapAutoUpdating = mapAutoUpdating;
    }

    @Override
    public boolean hasLoginPvpDisabled() {
        if (!loginPvpDisabled) {
            return false;
        }
        if (this.lastLoginTime + (FactionsPlugin.getInstance().conf().factions().pvp().getNoPVPDamageToOthersForXSecondsAfterLogin() * 1000L) < System.currentTimeMillis()) {
            this.loginPvpDisabled = false;
            return false;
        }
        return true;
    }

    @Override
    public FLocation getLastStoodAt() {
        return this.lastStoodAt;
    }

    @Override
    public void setLastStoodAt(FLocation flocation) {
        this.lastStoodAt = flocation;
    }

    //----------------------------------------------//
    // Title, Name, Faction Tag and Chat
    //----------------------------------------------//

    // Base:

    @Override
    public String getTitle() {
        return this.hasFaction() ? title : TL.NOFACTION_PREFIX.toString();
    }

    @Override
    public void setTitle(CommandSender sender, String title) {
        // Check if the setter has it.
        if (sender.hasPermission(Permission.TITLE_COLOR.node)) {
            title = ChatColor.translateAlternateColorCodes('&', title);
        }

        this.title = title;
    }

    @Override
    public String getName() {
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
            for (FPlayer fplayer : FPlayers.getInstance().getAllFPlayers()) {
                if (fplayer.getName() == null) {
                    continue;
                }
                if (fplayer.getName().equalsIgnoreCase(name)) {
                    UUID u = fplayer.getUniqueId();
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
                                NameLookup lookup = FactionsPlugin.getInstance().getGson().fromJson(new InputStreamReader(url.openStream()), NameLookup.class);
                                String newName = lookup.name;
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        //noinspection ConstantValue
                                        if (newName != null && fplayer.getName().equals(uuidName)) {
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

    @Override
    public String getTag() {
        return this.hasFaction() ? this.getFaction().getTag() : "";
    }

    // Base concatenations:

    @Override
    public String getNameAndSomething(String something) {
        String ret = this.role.getPrefix();
        if (!something.isEmpty()) {
            ret += something + " ";
        }
        ret += this.getName();
        return ret;
    }

    @Override
    public String getNameAndTitle() {
        return this.getNameAndSomething(this.getTitle());
    }

    @Override
    public String getNameAndTag() {
        return this.getNameAndSomething(this.getTag());
    }

    // Colored concatenations:
    // These are used in information messages

    @Override
    public String getNameAndTitle(@Nullable Faction faction) {
        return this.getColorStringTo(faction) + this.getNameAndTitle();
    }

    @Override
    public String getNameAndTitle(@Nullable FPlayer fplayer) {
        return this.getColorStringTo(fplayer) + this.getNameAndTitle();
    }

    // Chat Tag:
    // These are injected into the format of global chat messages.

    @Override
    public String getChatTag() {
        return this.hasFaction() ? String.format(FactionsPlugin.getInstance().conf().factions().chat().getTagFormat(), this.getRole().getPrefix() + this.getTag()) : TL.NOFACTION_PREFIX.toString();
    }

    // Colored Chat Tag
    @Override
    public String getChatTag(@Nullable Faction faction) {
        return this.hasFaction() ? this.getRelationTo(faction).chatColor() + getChatTag() : TL.NOFACTION_PREFIX.toString();
    }

    @Override
    public String getChatTag(@Nullable FPlayer fplayer) {
        return this.hasFaction() ? this.getRelationTo(fplayer).chatColor() + getChatTag() : TL.NOFACTION_PREFIX.toString();
    }

    @Override
    public int getKills() {
        if (this.getPlayer() instanceof Player player) {
            this.kills = player.getStatistic(Statistic.PLAYER_KILLS);
        }
        return this.kills;
    }

    @Override
    public int getDeaths() {
        if (this.getPlayer() instanceof Player player) {
            this.deaths = player.getStatistic(Statistic.DEATHS);
        }
        return this.deaths;
    }

    //----------------------------------------------//
    // Power
    //----------------------------------------------//
    @Override
    public double getPower() {
        this.updatePower();
        return this.power;
    }

    @Override
    public void setPower(double power) {
        this.power = Math.min(Math.max(this.getPowerMin(), power), this.getPowerMax());
    }

    @Override
    public void alterPower(double delta) {
        int start = (int) Math.round(this.power);
        this.power += delta;
        if (this.power > this.getPowerMax()) {
            this.power = this.getPowerMax();
        } else if (this.power < this.getPowerMin()) {
            this.power = this.getPowerMin();
        }
        int end = (int) Math.round(this.power);
        if (this.hasFaction() && end != start && FactionsPlugin.getInstance().getLandRaidControl() instanceof PowerControl) {
            ((PowerControl) FactionsPlugin.getInstance().getLandRaidControl()).onPowerChange(this.getFaction(), start, end);
        }
    }

    @Override
    public double getPowerMax() {
        return FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getPlayerMax() + this.powerBoost;
    }

    @Override
    public double getPowerMin() {
        return FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getPlayerMin() + this.powerBoost;
    }

    @Override
    public int getPowerRounded() {
        return (int) Math.round(this.getPower());
    }

    @Override
    public int getPowerMaxRounded() {
        return (int) Math.round(this.getPowerMax());
    }

    @Override
    public int getPowerMinRounded() {
        return (int) Math.round(this.getPowerMin());
    }

    @Override
    public void updatePower() {
        if (this.isOffline()) {
            losePowerFromBeingOffline();
            if (!FactionsPlugin.getInstance().conf().factions().landRaidControl().power().isRegenOffline()) {
                return;
            }
        } else if (hasFaction() && getFaction().isPowerFrozen()) {
            return; // Don't let power regen if faction power is frozen.
        } else if (FactionsPlugin.getInstance().conf().plugins().essentialsX().isPreventRegenWhileAfk() && Essentials.isAfk(this.getPlayer())) {
            return;
        }
        long now = System.currentTimeMillis();
        long millisPassed = now - this.lastPowerUpdateTime;
        this.lastPowerUpdateTime = now;

        Player thisPlayer = this.getPlayer();
        if (thisPlayer != null && thisPlayer.isDead()) {
            return;  // don't let dead players regain power until they respawn
        }

        int millisPerMinute = 60 * 1000;
        this.alterPower(millisPassed * FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getPowerPerMinute() / millisPerMinute);
    }

    @Override
    public void losePowerFromBeingOffline() {
        long now = System.currentTimeMillis();
        if (FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getOfflineLossPerDay() > 0.0 && this.power > FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getOfflineLossLimit()) {
            long millisPassed = now - this.lastPowerUpdateTime;

            double loss = millisPassed * FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getOfflineLossPerDay() / (24 * 60 * 60 * 1000);
            if (this.power - loss < FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getOfflineLossLimit()) {
                loss = this.power;
            }
            this.alterPower(-loss);
        }
        this.lastPowerUpdateTime = now;
    }

    @Override
    public void onDeath() {
        if (hasFaction()) {
            getFaction().setLastDeath(System.currentTimeMillis());
        }
    }

    //----------------------------------------------//
    // Territory
    //----------------------------------------------//
    @Override
    public boolean isInOwnTerritory() {
        return getStandingInFaction() == this.getFaction();
    }

    @Override
    public boolean isInOthersTerritory() {
        Faction factionHere = getStandingInFaction();
        return factionHere.isNormal() && factionHere != this.getFaction();
    }

    @Override
    public boolean isInAllyTerritory() {
        return getStandingInFaction().getRelationTo(this).isAlly();
    }

    @Override
    public boolean isInNeutralTerritory() {
        return getStandingInFaction().getRelationTo(this).isNeutral();
    }

    @Override
    public boolean isInEnemyTerritory() {
        return getStandingInFaction().getRelationTo(this).isEnemy();
    }

    private Faction getStandingInFaction() {
        Player player = this.getPlayer();
        return Board.getInstance().getFactionAt(player == null ? this.lastStoodAt : new FLocation(player));
    }

    @Override
    public void sendFactionHereMessage(Faction from) {
        Faction toShow = Board.getInstance().getFactionAt(getLastStoodAt());
        boolean showTitle = FactionsPlugin.getInstance().conf().factions().enterTitles().isEnabled();
        boolean showChat = true;
        Player player = getPlayer();

        if (showTitle && player != null) {
            int in = FactionsPlugin.getInstance().conf().factions().enterTitles().getFadeIn();
            int stay = FactionsPlugin.getInstance().conf().factions().enterTitles().getStay();
            int out = FactionsPlugin.getInstance().conf().factions().enterTitles().getFadeOut();

            String title = Tag.parsePlain(toShow, this, FactionsPlugin.getInstance().conf().factions().enterTitles().getTitle());
            String sub = AbstractFactionsPlugin.getInstance().txt().parse(Tag.parsePlain(toShow, this, FactionsPlugin.getInstance().conf().factions().enterTitles().getSubtitle()));

            player.sendTitle(title, sub, in, stay, out);

            showChat = FactionsPlugin.getInstance().conf().factions().enterTitles().isAlsoShowChat();
        }

        if (showInfoBoard(toShow)) {
            FScoreboard.get(this).setTemporarySidebar(new FInfoSidebar(toShow));
            showChat = FactionsPlugin.getInstance().conf().scoreboard().info().isAlsoSendChat();
        }
        if (showChat) {
            this.sendMessage(AbstractFactionsPlugin.getInstance().txt().parse(TL.FACTION_LEAVE.format(from.getTag(this), toShow.getTag(this))));
        }
    }

    /**
     * Check if the scoreboard should be shown. Simple method to be used by above method.
     *
     * @param toShow Faction to be shown.
     * @return true if should show, otherwise false.
     */
    public boolean showInfoBoard(Faction toShow) {
        return showScoreboard && !toShow.isWarZone() && !toShow.isWilderness() && !toShow.isSafeZone() && FactionsPlugin.getInstance().conf().scoreboard().info().isEnabled() && FScoreboard.get(this) != null;
    }

    @Override
    public boolean showScoreboard() {
        return this.showScoreboard;
    }

    @Override
    public void setShowScoreboard(boolean show) {
        this.showScoreboard = show;
    }

    @Override
    public void leave(boolean makePay) {
        Faction myFaction = this.getFaction();
        boolean econMakePay = makePay && Econ.shouldBeUsed() && !this.isAdminBypassing();

        boolean perm = myFaction.isPermanent();

        if (!perm && this.getRole() == Role.ADMIN && myFaction.getFPlayers().size() > 1) {
            msg(TL.LEAVE_PASSADMIN);
            return;
        }

        if (makePay && !FactionsPlugin.getInstance().getLandRaidControl().canLeaveFaction(this)) {
            return;
        }

        // if economy is enabled and they're not on the bypass list, make sure they can pay
        if (econMakePay && !Econ.hasAtLeast(this, FactionsPlugin.getInstance().conf().economy().getCostLeave(), TL.LEAVE_TOLEAVE.toString())) {
            return;
        }

        FPlayerLeaveEvent leaveEvent = new FPlayerLeaveEvent(this, myFaction, FPlayerLeaveEvent.Reason.LEAVE);
        Bukkit.getServer().getPluginManager().callEvent(leaveEvent);
        if (leaveEvent.isCancelled()) {
            return;
        }

        // then make 'em pay (if applicable)
        if (econMakePay && !Econ.modifyMoney(this, -FactionsPlugin.getInstance().conf().economy().getCostLeave(), TL.LEAVE_TOLEAVE.toString(), TL.LEAVE_FORLEAVE.toString())) {
            return;
        }

        // Am I the last one in the faction?
        if (myFaction.getFPlayers().size() == 1) {
            // Transfer all money
            if (Econ.shouldBeUsed() && FactionsPlugin.getInstance().conf().economy().isBankEnabled()) {
                if (!perm || FactionsPlugin.getInstance().conf().economy().isBankPermanentFactionSendBalanceToLastLeaver()) {
                    //Give all the faction's money to the disbander
                    double amount = Econ.getBalance(myFaction);
                    Econ.transferMoney(this, myFaction, this, amount, false);

                    if (amount > 0.0) {
                        String amountString = Econ.moneyString(amount);
                        this.msg(TL.COMMAND_DISBAND_HOLDINGS, amountString);
                        //TODO: Format this correctly and translate
                        FactionsPlugin.getInstance().log(this.getName() + " has been given bank holdings of " + amountString + " from disbanding " + myFaction.getTag() + ".");
                    }
                }
            }
        }

        if (myFaction.isNormal()) {
            for (FPlayer fplayer : myFaction.getFPlayersWhereOnline(true)) {
                fplayer.msg(TL.LEAVE_LEFT, this.describeTo(fplayer, true), myFaction.describeTo(fplayer));
            }

            if (FactionsPlugin.getInstance().conf().logging().isFactionLeave()) {
                FactionsPlugin.getInstance().log(TL.LEAVE_LEFT.format(this.getName(), myFaction.getTag()));
            }
        }

        myFaction.removeAnnouncements(this);
        this.resetFactionData(true);
        if (FactionsPlugin.getInstance().conf().commands().fly().isEnable()) {
            setFlying(false, false);
        }

        if (myFaction.isNormal() && !perm && myFaction.getFPlayers().isEmpty()) {
            // Remove this faction
            for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
                fplayer.msg(TL.LEAVE_DISBANDED, myFaction.describeTo(fplayer, true));
            }

            AbstractFactionsPlugin.getInstance().getServer().getPluginManager().callEvent(new FactionAutoDisbandEvent(myFaction));
            Factions.getInstance().removeFaction(myFaction);
            if (FactionsPlugin.getInstance().conf().logging().isFactionDisband()) {
                FactionsPlugin.getInstance().log(TL.LEAVE_DISBANDEDLOG.format(myFaction.getTag(), "" + myFaction.getId(), this.getName()));
            }
        }
    }

    @Override
    public void attemptAutoSetZone(FLocation flocation) {
        if (this.hasFaction() && this.autoSetZone != null) {
            Faction faction = this.getFaction();
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
                this.sendMessage(Mini.parse(FactionsPlugin.getInstance().tl().commands().zone().claim().getSuccess(), Placeholder.unparsed("oldzone", currentZone.name()), Placeholder.unparsed("newzone", zone.name())));
            }
        }
    }

    @Override
    public boolean canClaimForFaction(Faction forFaction) {
        Player player = this.getPlayer();
        if (player == null) {
            return false;
        }
        return this.isAdminBypassing() || !forFaction.isWilderness() && (forFaction == this.getFaction() && this.getFaction().hasAccess(this, PermissibleActions.TERRITORY, null)) || (forFaction.isSafeZone() && Permission.MANAGE_SAFE_ZONE.has(player)) || (forFaction.isWarZone() && Permission.MANAGE_WAR_ZONE.has(player));
    }

    @Override
    public boolean canClaimForFactionAtLocation(Faction forFaction, FLocation flocation, boolean notifyFailure) {
        Player player = this.getPlayer();
        if (player == null) {
            return false;
        }
        AbstractFactionsPlugin plugin = AbstractFactionsPlugin.getInstance();
        String denyReason = null;
        Faction myFaction = getFaction();
        Faction currentFaction = Board.getInstance().getFactionAt(flocation);
        int ownedLand = forFaction.getLandRounded();
        int factionBuffer = plugin.conf().factions().claims().getBufferZone();
        int worldBuffer = plugin.conf().worldBorder().getBuffer();

        if (plugin.conf().worldGuard().isCheckingEither() && plugin.getWorldguard() != null && plugin.getWorldguard().checkForRegionsInChunk(flocation.getChunk())) {
            // Checks for WorldGuard regions in the chunk attempting to be claimed
            denyReason = plugin.txt().parse(TL.CLAIM_PROTECTED.toString());
        } else if (plugin.conf().factions().claims().getWorldsNoClaiming().contains(flocation.worldName())) {
            // Cannot claim in this world
            denyReason = plugin.txt().parse(TL.CLAIM_DISABLED.toString());
        } else if (this.isAdminBypassing()) {
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
        } else if (forFaction.getFPlayers().size() < plugin.conf().factions().claims().getRequireMinFactionMembers()) {
            // Need more members in order to claim land
            denyReason = plugin.txt().parse(TL.CLAIM_MEMBERS.toString(), plugin.conf().factions().claims().getRequireMinFactionMembers());
        } else if (currentFaction.isSafeZone()) {
            // Cannot claim safezone
            denyReason = plugin.txt().parse(TL.CLAIM_SAFEZONE.toString());
        } else if (currentFaction.isWarZone()) {
            // Cannot claim warzone
            denyReason = plugin.txt().parse(TL.CLAIM_WARZONE.toString());
        } else if (plugin.getLandRaidControl() instanceof PowerControl && ownedLand >= forFaction.getPower()) {
            // Already own at least as much land as power
            denyReason = plugin.txt().parse(TL.CLAIM_POWER.toString());
        } else if (plugin.getLandRaidControl() instanceof DTRControl && ownedLand >= plugin.getLandRaidControl().getLandLimit(forFaction)) {
            // Already own at least as much land as land limit (DTR)
            denyReason = plugin.txt().parse(TL.CLAIM_DTR_LAND.toString());
        } else if (plugin.conf().factions().claims().getLandsMax() != 0 && ownedLand >= plugin.conf().factions().claims().getLandsMax() && forFaction.isNormal()) {
            // Land limit reached
            denyReason = plugin.txt().parse(TL.CLAIM_LIMIT.toString());
        } else if (currentFaction.getRelationTo(forFaction) == Relation.ALLY) {
            // // Can't claim ally
            denyReason = plugin.txt().parse(TL.CLAIM_ALLY.toString());
        } else if (plugin.conf().factions().claims().isMustBeConnected() && !this.isAdminBypassing() && myFaction.getLandRoundedInWorld(flocation.worldName()) > 0 && Board.getInstance().isDisconnectedLocation(flocation, myFaction) && (!plugin.conf().factions().claims().isCanBeUnconnectedIfOwnedByOtherFaction() || !currentFaction.isNormal())) {
            // Must be contiguous/connected
            if (plugin.conf().factions().claims().isCanBeUnconnectedIfOwnedByOtherFaction()) {
                denyReason = plugin.txt().parse(TL.CLAIM_CONTIGIOUS.toString());
            } else {
                denyReason = plugin.txt().parse(TL.CLAIM_FACTIONCONTIGUOUS.toString());
            }
        } else if (!(currentFaction.isNormal() && plugin.conf().factions().claims().isAllowOverClaimAndIgnoringBuffer() && currentFaction.hasLandInflation()) && factionBuffer > 0 && Board.getInstance().hasFactionWithin(flocation, myFaction, factionBuffer)) {
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
            if (myFaction.isPeaceful()) {
                // Cannot claim as peaceful
                denyReason = plugin.txt().parse(TL.CLAIM_PEACEFUL.toString(), currentFaction.getTag(this));
            } else if (currentFaction.isPeaceful()) {
                // Cannot claim from peaceful
                denyReason = plugin.txt().parse(TL.CLAIM_PEACEFULTARGET.toString(), currentFaction.getTag(this));
            } else if (!currentFaction.hasLandInflation()) {
                // Cannot claim other faction (perhaps based on power/land ratio)
                // TODO more messages WARN current faction most importantly
                denyReason = plugin.txt().parse(TL.CLAIM_THISISSPARTA.toString(), currentFaction.getTag(this));
            } else if (currentFaction.hasLandInflation() && !plugin.conf().factions().claims().isAllowOverClaim()) {
                // deny over claim when it normally would be allowed.
                denyReason = plugin.txt().parse(TL.CLAIM_OVERCLAIM_DISABLED.toString());
            } else if (!Board.getInstance().isBorderLocation(flocation)) {
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

        Faction currentFaction = Board.getInstance().getFactionAt(flocation);

        int ownedLand = forFaction.getLandRounded();

        if (!this.canClaimForFactionAtLocation(forFaction, flocation, notifyFailure)) {
            return false;
        }

        // if economy is enabled and they're not on the bypass list, make sure they can pay
        boolean mustPay = Econ.shouldBeUsed() && !this.isAdminBypassing() && !forFaction.isSafeZone() && !forFaction.isWarZone();
        double cost = 0.0;
        Participator payee = null;
        if (mustPay) {
            cost = Econ.calculateClaimCost(ownedLand, currentFaction.isNormal());

            if (FactionsPlugin.getInstance().conf().economy().getClaimUnconnectedFee() != 0.0 && forFaction.getLandRoundedInWorld(flocation.worldName()) > 0 && Board.getInstance().isDisconnectedLocation(flocation, forFaction)) {
                cost += FactionsPlugin.getInstance().conf().economy().getClaimUnconnectedFee();
            }

            if (FactionsPlugin.getInstance().conf().economy().isBankEnabled() && FactionsPlugin.getInstance().conf().economy().isBankFactionPaysLandCosts() && this.hasFaction() && this.getFaction().hasAccess(this, PermissibleActions.ECONOMY, null)) {
                payee = this.getFaction();
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
            Econ.modifyMoney(payee, FactionsPlugin.getInstance().conf().economy().getOverclaimRewardMultiplier(), TL.CLAIM_TOOVERCLAIM.toString(), TL.CLAIM_FOROVERCLAIM.toString());
        }

        // announce success
        Set<FPlayer> informTheseFPlayers = new HashSet<>();
        informTheseFPlayers.add(this);
        informTheseFPlayers.addAll(forFaction.getFPlayersWhereOnline(true));
        for (FPlayer fp : informTheseFPlayers) {
            fp.msg(TL.CLAIM_CLAIMED, this.describeTo(fp, true), forFaction.describeTo(fp), currentFaction.describeTo(fp));
        }

        Board.getInstance().setFactionAt(forFaction, flocation);

        if (FactionsPlugin.getInstance().conf().logging().isLandClaims()) {
            FactionsPlugin.getInstance().log(TL.CLAIM_CLAIMEDLOG.toString(), this.getName(), flocation.getCoordString(), forFaction.getTag());
        }

        return true;
    }

    @Override
    public boolean attemptUnclaim(Faction forFaction, FLocation flocation, boolean notifyFailure) {
        Faction targetFaction = Board.getInstance().getFactionAt(flocation);

        if (!targetFaction.equals(forFaction)) {
            this.msg(TL.COMMAND_UNCLAIM_WRONGFACTIONOTHER);
            return false;
        }

        Player player = this.getPlayer();
        if (player == null) {
            return false;
        }

        if (targetFaction.isSafeZone()) {
            if (Permission.MANAGE_SAFE_ZONE.has(player)) {
                Board.getInstance().removeAt(flocation);
                this.msg(TL.COMMAND_UNCLAIM_SAFEZONE_SUCCESS);

                if (FactionsPlugin.getInstance().conf().logging().isLandUnclaims()) {
                    FactionsPlugin.getInstance().log(TL.COMMAND_UNCLAIM_LOG.format(this.getName(), flocation.getCoordString(), targetFaction.getTag()));
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
                Board.getInstance().removeAt(flocation);
                this.msg(TL.COMMAND_UNCLAIM_WARZONE_SUCCESS);

                if (FactionsPlugin.getInstance().conf().logging().isLandUnclaims()) {
                    FactionsPlugin.getInstance().log(TL.COMMAND_UNCLAIM_LOG.format(this.getName(), flocation.getCoordString(), targetFaction.getTag()));
                }
                return true;
            } else {
                if (notifyFailure) {
                    this.msg(TL.COMMAND_UNCLAIM_WARZONE_NOPERM);
                }
                return false;
            }
        }

        if (this.isAdminBypassing()) {
            LandUnclaimEvent unclaimEvent = new LandUnclaimEvent(flocation, targetFaction, this);
            Bukkit.getServer().getPluginManager().callEvent(unclaimEvent);
            if (unclaimEvent.isCancelled()) {
                return false;
            }

            Board.getInstance().removeAt(flocation);

            targetFaction.msg(TL.COMMAND_UNCLAIM_UNCLAIMED, this.describeTo(targetFaction, true));
            this.msg(TL.COMMAND_UNCLAIM_UNCLAIMS);

            if (FactionsPlugin.getInstance().conf().logging().isLandUnclaims()) {
                FactionsPlugin.getInstance().log(TL.COMMAND_UNCLAIM_LOG.format(this.getName(), flocation.getCoordString(), targetFaction.getTag()));
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

        if (this.getFaction() != targetFaction) {
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
            double refund = Econ.calculateClaimRefund(this.getFaction().getLandRounded());

            if (FactionsPlugin.getInstance().conf().economy().isBankEnabled() && FactionsPlugin.getInstance().conf().economy().isBankFactionPaysLandCosts()) {
                if (!Econ.modifyMoney(this.getFaction(), refund, TL.COMMAND_UNCLAIM_TOUNCLAIM.toString(), TL.COMMAND_UNCLAIM_FORUNCLAIM.toString())) {
                    return false;
                }
            } else {
                if (!Econ.modifyMoney(this, refund, TL.COMMAND_UNCLAIM_TOUNCLAIM.toString(), TL.COMMAND_UNCLAIM_FORUNCLAIM.toString())) {
                    return false;
                }
            }
        }

        Board.getInstance().removeAt(flocation);
        this.getFaction().msg(TL.COMMAND_UNCLAIM_FACTIONUNCLAIMED, this.describeTo(this.getFaction(), true));

        if (FactionsPlugin.getInstance().conf().logging().isLandUnclaims()) {
            FactionsPlugin.getInstance().log(TL.COMMAND_UNCLAIM_LOG.format(this.getName(), flocation.getCoordString(), targetFaction.getTag()));
        }

        return true;
    }

    public boolean shouldBeSaved() {
        return this.hasFaction() ||
                (FactionsPlugin.getInstance().getLandRaidControl() instanceof PowerControl &&
                        (this.getPowerRounded() != FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getPlayerStarting() ||
                                this.getPowerBoost() != 0));
    }

    @Override
    public void msg(@NonNull String str, @NonNull Object @NonNull ... args) {
        this.sendMessage(AbstractFactionsPlugin.getInstance().txt().parse(str, args));
    }

    @Override
    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(this.id);
    }

    @Override
    public boolean isOnline() {
        Player player = this.getPlayer();
        return player != null && WorldUtil.isEnabled(player.getWorld());
    }

    // make sure target player should be able to detect that this player is online
    @Override
    public boolean isOnlineAndVisibleTo(Player player) {
        Player target = this.getPlayer();
        return target != null && player.canSee(target) && WorldUtil.isEnabled(player.getWorld());
    }

    @Override
    public boolean isOffline() {
        return !isOnline();
    }

    @Override
    public void flightCheck() {
        if (FactionsPlugin.getInstance().conf().commands().fly().isEnable() && !this.isAdminBypassing()) {
            boolean canFly = this.canFlyAtLocation(this.getLastStoodAt());
            if (this.isFlying() && !canFly) {
                this.setFlying(false, false);
            } else if (this.isAutoFlying() && !this.isFlying() && canFly) {
                this.setFlying(true);
            }
        }
    }

    @Override
    public boolean isFlying() {
        return isFlying;
    }

    @Override
    public void setFlying(boolean fly) {
        setFlying(fly, false);
    }

    @Override
    public void setFlying(boolean fly, boolean damage) {
        Player player = getPlayer();
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
            int cooldown = FactionsPlugin.getInstance().conf().commands().fly().getFallDamageCooldown();

            // If the value is 0 or lower, make them take fall damage.
            // Otherwise, start a timer and have this cancel after a few seconds.
            // Short task so we're just doing it in method. Not clean but eh.
            if (cooldown > 0) {
                setTakeFallDamage(false);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        setTakeFallDamage(true);
                    }
                }.runTaskLater(AbstractFactionsPlugin.getInstance(), 20L * cooldown);
            }
        }

        isFlying = fly;
    }

    @Override
    public boolean isAutoFlying() {
        return isAutoFlying;
    }

    @Override
    public void setAutoFlying(boolean autoFly) {
        msg(TL.COMMAND_FLY_AUTO, autoFly ? "enabled" : "disabled");
        this.isAutoFlying = autoFly;
    }

    @Override
    public boolean canFlyAtLocation() {
        return canFlyAtLocation(lastStoodAt);
    }

    @Override
    public boolean canFlyAtLocation(FLocation location) {
        Player player = this.getPlayer();
        if (player == null) {
            return false;
        }
        Faction faction = Board.getInstance().getFactionAt(location);
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
    public boolean shouldTakeFallDamage() {
        return this.shouldTakeFallDamage;
    }

    @Override
    public void setTakeFallDamage(boolean fallDamage) {
        this.shouldTakeFallDamage = fallDamage;
    }

    @Override
    public boolean isSeeingChunk() {
        return seeingChunk;
    }

    @Override
    public void setSeeingChunk(boolean seeingChunk) {
        this.seeingChunk = seeingChunk;
        FactionsPlugin.getInstance().getSeeChunkUtil().updatePlayerInfo(this.id, seeingChunk);
    }

    @Override
    public boolean getFlyTrailsState() {
        return flyTrailsState;
    }

    @Override
    public void setFlyTrailsState(boolean state) {
        flyTrailsState = state;
        msg(TL.COMMAND_FLYTRAILS_CHANGE, state ? "enabled" : "disabled");
    }

    @Override
    public @Nullable String getFlyTrailsEffect() {
        return flyTrailsEffect;
    }

    @Override
    public void setFlyTrailsEffect(String effect) {
        flyTrailsEffect = effect;
        msg(TL.COMMAND_FLYTRAILS_PARTICLE_CHANGE, effect);
    }

    @Override
    public void sendMessage(@NonNull Component component) {
        if (this.getPlayer() instanceof Player player) {
            ComponentDispatcher.send(player, component);
        }
    }

    @Override
    public void sendMessage(String msg) {
        if (msg.contains("{null}")) {
            return; // user wants this message to not send
        }
        Player player = this.getPlayer();
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
    public int getMapHeight() {
        if (this.mapHeight < 1) {
            this.mapHeight = FactionsPlugin.getInstance().conf().map().getHeight();
        }

        return this.mapHeight;
    }

    @Override
    public void setMapHeight(int height) {
        this.mapHeight = Math.min(height, (FactionsPlugin.getInstance().conf().map().getHeight() * 2));
    }

    @Override
    public UUID getUniqueId() {
        return id;
    }

    @Override
    public void clearWarmup() {
        if (warmup != null) {
            Bukkit.getScheduler().cancelTask(warmupTask);
            this.stopWarmup();
        }
    }

    @Override
    public void stopWarmup() {
        warmup = null;
    }

    @Override
    public boolean isWarmingUp() {
        return warmup != null;
    }

    @Override
    public WarmUpUtil.@Nullable Warmup getWarmupType() {
        return warmup;
    }

    @Override
    public void addWarmup(WarmUpUtil.Warmup warmup, int taskId) {
        if (this.warmup != null) {
            this.clearWarmup();
        }
        this.warmup = warmup;
        this.warmupTask = taskId;
    }
}
