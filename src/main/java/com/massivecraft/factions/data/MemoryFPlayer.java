package com.massivecraft.factions.data;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.event.FactionAutoDisbandEvent;
import com.massivecraft.factions.event.LandClaimEvent;
import com.massivecraft.factions.event.LandUnclaimEvent;
import com.massivecraft.factions.iface.EconomyParticipator;
import com.massivecraft.factions.iface.RelationParticipator;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.integration.Essentials;
import com.massivecraft.factions.integration.IntegrationManager;
import com.massivecraft.factions.integration.LWC;
import com.massivecraft.factions.landraidcontrol.DTRControl;
import com.massivecraft.factions.landraidcontrol.PowerControl;
import com.massivecraft.factions.perms.PermissibleActions;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.scoreboards.FScoreboard;
import com.massivecraft.factions.scoreboards.sidebar.FInfoSidebar;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.tag.Tag;
import com.massivecraft.factions.util.RelationUtil;
import com.massivecraft.factions.util.TL;
import com.massivecraft.factions.util.WarmUpUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * Logged in players always have exactly one FPlayer instance. Logged out players may or may not have an FPlayer
 * instance. They will always have one if they are part of a faction. This is because only players with a faction are
 * saved to disk (in order to not waste disk space).
 * <p/>
 * The FPlayer is linked to a minecraft player using the player name.
 * <p/>
 * The same instance is always returned for the same player. This means you can use the == operator. No .equals method
 * necessary.
 */

public abstract class MemoryFPlayer implements FPlayer {

    protected int factionId;
    protected Role role;
    protected String title;
    protected double power;
    protected double powerBoost;
    protected long lastPowerUpdateTime;
    protected long lastLoginTime;
    protected ChatMode chatMode;
    protected boolean ignoreAllianceChat = false;
    protected String id;
    protected String name;
    protected boolean monitorJoins;
    protected boolean spyingChat = false;
    protected boolean showScoreboard = true;
    protected WarmUpUtil.Warmup warmup;
    protected int warmupTask;
    protected boolean isAdminBypassing = false;
    protected int kills, deaths;
    protected boolean willAutoLeave = true;
    protected int mapHeight = 8; // default to old value
    protected boolean isFlying = false;
    protected boolean isAutoFlying = false;
    protected boolean flyTrailsState = false;
    protected String flyTrailsEffect = null;

    protected boolean seeingChunk = false;

    protected transient FLocation lastStoodAt = new FLocation(); // Where did this player stand the last time we checked?
    protected transient boolean mapAutoUpdating;
    protected transient Faction autoClaimFor;
    protected transient Faction autoUnclaimFor;
    protected transient boolean loginPvpDisabled;
    protected transient long lastFrostwalkerMessage;
    protected transient boolean shouldTakeFallDamage = true;
    protected transient OfflinePlayer offlinePlayer;

    public void login() {
        this.kills = getPlayer().getStatistic(Statistic.PLAYER_KILLS);
        this.deaths = getPlayer().getStatistic(Statistic.DEATHS);
    }

    public void logout() {
        this.kills = getPlayer().getStatistic(Statistic.PLAYER_KILLS);
        this.deaths = getPlayer().getStatistic(Statistic.DEATHS);
    }

    public Faction getFaction() {
        Faction faction = Factions.getInstance().getFactionById(this.factionId);
        if (faction == null) {
            FactionsPlugin.getInstance().getLogger().warning("Found null faction (id " + this.factionId + ") for player " + this.getName());
            this.factionId = 0;
            faction = Factions.getInstance().getFactionById(this.factionId);
        }
        return faction;
    }

    public String getFactionId() {
        return String.valueOf(this.factionId);
    }

    public int getFactionIntId() {
        return this.factionId;
    }

    public boolean hasFaction() {
        return factionId != 0;
    }

    public void setFaction(Faction faction) {
        Faction oldFaction = this.getFaction();
        if (oldFaction != null) {
            oldFaction.removeFPlayer(this);
        }
        faction.addFPlayer(this);
        this.factionId = faction.getIntId();
    }

    public void setMonitorJoins(boolean monitor) {
        this.monitorJoins = monitor;
    }

    public boolean isMonitoringJoins() {
        return this.monitorJoins;
    }

    public Role getRole() {
        // Hack to fix null roles..
        if (role == null) {
            this.role = Role.NORMAL;
        }

        return this.role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public double getPowerBoost() {
        return this.powerBoost;
    }

    public void setPowerBoost(double powerBoost) {
        this.powerBoost = powerBoost;
    }

    public boolean willAutoLeave() {
        return this.willAutoLeave;
    }

    public void setAutoLeave(boolean willLeave) {
        this.willAutoLeave = willLeave;
        FactionsPlugin.getInstance().debug(name + " set autoLeave to " + willLeave);
    }

    public long getLastFrostwalkerMessage() {
        return this.lastFrostwalkerMessage;
    }

    public void setLastFrostwalkerMessage() {
        this.lastFrostwalkerMessage = System.currentTimeMillis();
    }

    public Faction getAutoClaimFor() {
        return autoClaimFor;
    }

    public void setAutoClaimFor(Faction faction) {
        this.autoClaimFor = faction;
        if (faction != null) {
            this.autoUnclaimFor = null;
        }
    }

    public Faction getAutoUnclaimFor() {
        return autoUnclaimFor;
    }

    public void setAutoUnclaimFor(Faction faction) {
        this.autoUnclaimFor = faction;
        if (faction != null) {
            this.autoClaimFor = null;
        }
    }

    @Deprecated
    public boolean isAutoSafeClaimEnabled() {
        return autoClaimFor != null && autoClaimFor.isSafeZone();
    }

    @Deprecated
    public void setIsAutoSafeClaimEnabled(boolean enabled) {
        this.setAutoClaimFor(enabled ? Factions.getInstance().getSafeZone() : null);
    }

    @Deprecated
    public boolean isAutoWarClaimEnabled() {
        return autoClaimFor != null && autoClaimFor.isWarZone();
    }

    @Deprecated
    public void setIsAutoWarClaimEnabled(boolean enabled) {
        this.setAutoClaimFor(enabled ? Factions.getInstance().getWarZone() : null);
    }

    public boolean isAdminBypassing() {
        return this.isAdminBypassing;
    }

    public boolean isVanished() {
        Player player = this.getPlayer();
        if (FactionsPlugin.getInstance().getIntegrationManager().isEnabled(IntegrationManager.Integration.ESS) && Essentials.isVanished(player)) {
            return true;
        }
        if (player != null) {
            for (MetadataValue metadataValue : player.getMetadata("vanished")) {
                if (metadataValue != null && metadataValue.asBoolean()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setIsAdminBypassing(boolean val) {
        this.isAdminBypassing = val;
    }

    public void setChatMode(ChatMode chatMode) {
        this.chatMode = chatMode;
    }

    public ChatMode getChatMode() {
        if (this.chatMode == null || this.factionId == 0 || !FactionsPlugin.getInstance().conf().factions().chat().isFactionOnlyChat()) {
            this.chatMode = ChatMode.PUBLIC;
        }
        return chatMode;
    }

    public void setIgnoreAllianceChat(boolean ignore) {
        this.ignoreAllianceChat = ignore;
    }

    public boolean isIgnoreAllianceChat() {
        return ignoreAllianceChat;
    }

    public void setSpyingChat(boolean chatSpying) {
        this.spyingChat = chatSpying;
    }

    public boolean isSpyingChat() {
        return spyingChat;
    }

    // FIELD: account
    public String getAccountId() {
        return this.getId();
    }

    public OfflinePlayer getOfflinePlayer() {
        if (this.offlinePlayer == null) {
            UUID uuid = UUID.fromString(getId());
            this.offlinePlayer = Bukkit.getPlayer(uuid);
            if (this.offlinePlayer == null) {
                this.offlinePlayer = FactionsPlugin.getInstance().getOfflinePlayer(this.name, uuid);
            }
        }
        return this.offlinePlayer;
    }

    public void setOfflinePlayer(Player player) {
        this.offlinePlayer = player;
    }

    public MemoryFPlayer() {
    }

    public MemoryFPlayer(String id) {
        this.id = id;
        this.resetFactionData(false);
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

        if (FactionsPlugin.getInstance().conf().factions().other().getNewPlayerStartingFactionID() > 0 && Factions.getInstance().isValidFactionId(FactionsPlugin.getInstance().conf().factions().other().getNewPlayerStartingFactionID())) {
            this.factionId = FactionsPlugin.getInstance().conf().factions().other().getNewPlayerStartingFactionID();
        }
    }

    @Deprecated
    public MemoryFPlayer(MemoryFPlayer other) {
        this.factionId = other.factionId;
        this.id = other.id;
        this.power = other.power;
        this.lastLoginTime = other.lastLoginTime;
        this.mapAutoUpdating = other.mapAutoUpdating;
        this.autoClaimFor = other.autoClaimFor;
        this.loginPvpDisabled = other.loginPvpDisabled;
        this.powerBoost = other.powerBoost;
        this.role = other.role;
        this.title = other.title;
        this.chatMode = other.chatMode;
        this.spyingChat = other.spyingChat;
        this.lastStoodAt = other.lastStoodAt;
        this.isAdminBypassing = other.isAdminBypassing;
        this.kills = other.kills;
        this.deaths = other.deaths;
        this.mapHeight = other.mapHeight;
    }

    public void resetFactionData(boolean doSpoutUpdate) {
        // clean up any territory ownership in old faction, if there is one
        if (Factions.getInstance().isValidFactionId(this.getFactionIntId())) {
            Faction currentFaction = this.getFaction();
            currentFaction.removeFPlayer(this);
            if (currentFaction.isNormal()) {
                currentFaction.clearClaimOwnership(this);
            }
        }

        this.factionId = 0; // The default neutral faction
        this.chatMode = ChatMode.PUBLIC;
        this.role = Role.NORMAL;
        this.title = "";
        this.autoClaimFor = null;
    }

    public void resetFactionData() {
        this.resetFactionData(true);
    }

    // -------------------------------------------- //
    // Getters And Setters
    // -------------------------------------------- //


    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
        if (FactionsPlugin.getInstance().conf().factions().pvp().getNoPVPDamageToOthersForXSecondsAfterLogin() > 0) {
            this.loginPvpDisabled = true;
        }
    }

    public boolean isMapAutoUpdating() {
        return mapAutoUpdating;
    }

    public void setMapAutoUpdating(boolean mapAutoUpdating) {
        this.mapAutoUpdating = mapAutoUpdating;
    }

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

    public FLocation getLastStoodAt() {
        return this.lastStoodAt;
    }

    public void setLastStoodAt(FLocation flocation) {
        this.lastStoodAt = flocation;
    }

    //----------------------------------------------//
    // Title, Name, Faction Tag and Chat
    //----------------------------------------------//

    // Base:

    public String getTitle() {
        return this.hasFaction() ? title : TL.NOFACTION_PREFIX.toString();
    }

    public void setTitle(CommandSender sender, String title) {
        // Check if the setter has it.
        if (sender.hasPermission(Permission.TITLE_COLOR.node)) {
            title = ChatColor.translateAlternateColorCodes('&', title);
        }

        this.title = title;
    }

    public String getName() {
        if (this.name == null) {
            // Older versions of FactionsUUID don't save the name,
            // so `name` will be null the first time it's retrieved
            // after updating
            OfflinePlayer offline = Bukkit.getOfflinePlayer(UUID.fromString(getId()));
            this.name = offline.getName() != null ? offline.getName() : getId();
        }
        return name;
    }

    private static class NameLookup {
        String name;
    }

    public void setName(String name) {
        if (!name.equalsIgnoreCase(this.name)) {
            for (FPlayer fplayer : FPlayers.getInstance().getAllFPlayers()) {
                if (fplayer.getName().equalsIgnoreCase(name)) {
                    String uuidName = fplayer.getId();
                    ((MemoryFPlayer) fplayer).name = uuidName;
                    UUID u;
                    try {
                        u = UUID.fromString(uuidName);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
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
                                        if (newName != null && fplayer.getName().equals(uuidName)) {
                                            ((MemoryFPlayer) fplayer).setName(newName);
                                        }
                                    }
                                }.runTask(FactionsPlugin.getInstance());
                            } catch (Exception ignored) {
                            }
                        }
                    }.runTaskAsynchronously(FactionsPlugin.getInstance());
                }
            }
        }
        this.name = name;
    }

    public String getTag() {
        return this.hasFaction() ? this.getFaction().getTag() : "";
    }

    // Base concatenations:

    public String getNameAndSomething(String something) {
        String ret = this.role.getPrefix();
        if (something != null && !something.isEmpty()) {
            ret += something + " ";
        }
        ret += this.getName();
        return ret;
    }

    public String getNameAndTitle() {
        return this.getNameAndSomething(this.getTitle());
    }

    public String getNameAndTag() {
        return this.getNameAndSomething(this.getTag());
    }

    // Colored concatenations:
    // These are used in information messages

    public String getNameAndTitle(Faction faction) {
        return this.getColorStringTo(faction) + this.getNameAndTitle();
    }

    public String getNameAndTitle(MemoryFPlayer fplayer) {
        return this.getColorStringTo(fplayer) + this.getNameAndTitle();
    }

    // Chat Tag:
    // These are injected into the format of global chat messages.

    public String getChatTag() {
        return this.hasFaction() ? String.format(FactionsPlugin.getInstance().conf().factions().chat().getTagFormat(), this.getRole().getPrefix() + this.getTag()) : TL.NOFACTION_PREFIX.toString();
    }

    // Colored Chat Tag
    public String getChatTag(Faction faction) {
        return this.hasFaction() ? this.getRelationTo(faction).getColor() + getChatTag() : TL.NOFACTION_PREFIX.toString();
    }

    @Override
    public String getChatTag(FPlayer fplayer) {
        return this.hasFaction() ? this.getRelationTo(fplayer).getColor() + getChatTag() : TL.NOFACTION_PREFIX.toString();
    }

    public int getKills() {
        return isOnline() ? getPlayer().getStatistic(Statistic.PLAYER_KILLS) : this.kills;
    }

    public int getDeaths() {
        return isOnline() ? getPlayer().getStatistic(Statistic.DEATHS) : this.deaths;

    }

    // -------------------------------
    // Relation and relation colors
    // -------------------------------

    @Override
    public String describeTo(RelationParticipator that, boolean ucfirst) {
        return RelationUtil.describeThatToMe(this, that, ucfirst);
    }

    @Override
    public String describeTo(RelationParticipator that) {
        return RelationUtil.describeThatToMe(this, that);
    }

    @Override
    public Relation getRelationTo(RelationParticipator rp) {
        return RelationUtil.getRelationTo(this, rp);
    }

    @Override
    public Relation getRelationTo(RelationParticipator rp, boolean ignorePeaceful) {
        return RelationUtil.getRelationTo(this, rp, ignorePeaceful);
    }

    public Relation getRelationToLocation() {
        return Board.getInstance().getFactionAt(new FLocation(this)).getRelationTo(this);
    }

    @Deprecated
    @Override
    public ChatColor getColorTo(RelationParticipator rp) {
        return RelationUtil.getColorOfThatToMe(this, rp);
    }

    @Override
    public String getColorStringTo(RelationParticipator rp) {
        return RelationUtil.getColorStringOfThatToMe(this, rp);
    }

    //----------------------------------------------//
    // Health
    //----------------------------------------------//
    public void heal(int amnt) {
        Player player = this.getPlayer();
        if (player == null) {
            return;
        }
        player.setHealth(player.getHealth() + amnt);
    }


    //----------------------------------------------//
    // Power
    //----------------------------------------------//
    public double getPower() {
        this.updatePower();
        return this.power;
    }

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

    public double getPowerMax() {
        return FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getPlayerMax() + this.powerBoost;
    }

    public double getPowerMin() {
        return FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getPlayerMin() + this.powerBoost;
    }

    public int getPowerRounded() {
        return (int) Math.round(this.getPower());
    }

    public int getPowerMaxRounded() {
        return (int) Math.round(this.getPowerMax());
    }

    public int getPowerMinRounded() {
        return (int) Math.round(this.getPowerMin());
    }

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

    public void onDeath() {
        if (hasFaction()) {
            getFaction().setLastDeath(System.currentTimeMillis());
        }
    }

    //----------------------------------------------//
    // Territory
    //----------------------------------------------//
    public boolean isInOwnTerritory() {
        return Board.getInstance().getFactionAt(new FLocation(this)) == this.getFaction();
    }

    public boolean isInOthersTerritory() {
        Faction factionHere = Board.getInstance().getFactionAt(new FLocation(this));
        return factionHere != null && factionHere.isNormal() && factionHere != this.getFaction();
    }

    public boolean isInAllyTerritory() {
        return Board.getInstance().getFactionAt(new FLocation(this)).getRelationTo(this).isAlly();
    }

    public boolean isInNeutralTerritory() {
        return Board.getInstance().getFactionAt(new FLocation(this)).getRelationTo(this).isNeutral();
    }

    public boolean isInEnemyTerritory() {
        return Board.getInstance().getFactionAt(new FLocation(this)).getRelationTo(this).isEnemy();
    }

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
            String sub = FactionsPlugin.getInstance().txt().parse(Tag.parsePlain(toShow, this, FactionsPlugin.getInstance().conf().factions().enterTitles().getSubtitle()));

            // We send null instead of empty because Spigot won't touch the title if it's null, but clears if empty.
            // We're just trying to be as unintrusive as possible.
            player.sendTitle(title, sub, in, stay, out);

            showChat = FactionsPlugin.getInstance().conf().factions().enterTitles().isAlsoShowChat();
        }

        if (showInfoBoard(toShow)) {
            FScoreboard.get(this).setTemporarySidebar(new FInfoSidebar(toShow));
            showChat = FactionsPlugin.getInstance().conf().scoreboard().info().isAlsoSendChat();
        }
        if (showChat) {
            this.sendMessage(FactionsPlugin.getInstance().txt().parse(TL.FACTION_LEAVE.format(from.getTag(this), toShow.getTag(this))));
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

    // -------------------------------
    // Actions
    // -------------------------------

    public void leave(boolean makePay) {
        Faction myFaction = this.getFaction();
        boolean econMakePay = makePay && Econ.shouldBeUsed() && !this.isAdminBypassing();

        if (myFaction == null) {
            resetFactionData();
            return;
        }

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

        FPlayerLeaveEvent leaveEvent = new FPlayerLeaveEvent(this, myFaction, FPlayerLeaveEvent.PlayerLeaveReason.LEAVE);
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
        this.resetFactionData();
        if (FactionsPlugin.getInstance().conf().commands().fly().isEnable()) {
            setFlying(false, false);
        }

        if (myFaction.isNormal() && !perm && myFaction.getFPlayers().isEmpty()) {
            // Remove this faction
            for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
                fplayer.msg(TL.LEAVE_DISBANDED, myFaction.describeTo(fplayer, true));
            }

            FactionsPlugin.getInstance().getServer().getPluginManager().callEvent(new FactionAutoDisbandEvent(myFaction));
            Factions.getInstance().removeFaction(myFaction);
            if (FactionsPlugin.getInstance().conf().logging().isFactionDisband()) {
                FactionsPlugin.getInstance().log(TL.LEAVE_DISBANDEDLOG.format(myFaction.getTag(), "" + myFaction.getIntId(), this.getName()));
            }
        }
    }

    public boolean canClaimForFaction(Faction forFaction) {
        return this.isAdminBypassing() || !forFaction.isWilderness() && (forFaction == this.getFaction() && this.getFaction().hasAccess(this, PermissibleActions.TERRITORY, null)) || (forFaction.isSafeZone() && Permission.MANAGE_SAFE_ZONE.has(getPlayer())) || (forFaction.isWarZone() && Permission.MANAGE_WAR_ZONE.has(getPlayer()));
    }

    // Not used
    public boolean canClaimForFactionAtLocation(Faction forFaction, Location location, boolean notifyFailure) {
        return canClaimForFactionAtLocation(forFaction, new FLocation(location), notifyFailure);
    }

    public boolean canClaimForFactionAtLocation(Faction forFaction, FLocation flocation, boolean notifyFailure) {
        FactionsPlugin plugin = FactionsPlugin.getInstance();
        String denyReason = null;
        Faction myFaction = getFaction();
        Faction currentFaction = Board.getInstance().getFactionAt(flocation);
        int ownedLand = forFaction.getLandRounded();
        int factionBuffer = plugin.conf().factions().claims().getBufferZone();
        int worldBuffer = plugin.conf().worldBorder().getBuffer();

        if (plugin.conf().worldGuard().isCheckingEither() && plugin.getWorldguard() != null && plugin.getWorldguard().checkForRegionsInChunk(flocation.getChunk())) {
            // Checks for WorldGuard regions in the chunk attempting to be claimed
            denyReason = plugin.txt().parse(TL.CLAIM_PROTECTED.toString());
        } else if (plugin.conf().factions().claims().getWorldsNoClaiming().contains(flocation.getWorldName())) {
            // Cannot claim in this world
            denyReason = plugin.txt().parse(TL.CLAIM_DISABLED.toString());
        } else if (this.isAdminBypassing()) {
            // Admin bypass
            return true;
        } else if (forFaction.isSafeZone() && Permission.MANAGE_SAFE_ZONE.has(getPlayer())) {
            // Safezone and can claim for such
            return true;
        } else if (forFaction.isWarZone() && Permission.MANAGE_WAR_ZONE.has(getPlayer())) {
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
        } else if (plugin.getLandRaidControl() instanceof PowerControl && ownedLand >= forFaction.getPowerRounded()) {
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
        } else if (plugin.conf().factions().claims().isMustBeConnected() && !this.isAdminBypassing() && myFaction.getLandRoundedInWorld(flocation.getWorldName()) > 0 && !Board.getInstance().isConnectedLocation(flocation, myFaction) && (!plugin.conf().factions().claims().isCanBeUnconnectedIfOwnedByOtherFaction() || !currentFaction.isNormal())) {
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

    public boolean attemptClaim(Faction forFaction, Location location, boolean notifyFailure) {
        return attemptClaim(forFaction, new FLocation(location), notifyFailure);
    }

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
        EconomyParticipator payee = null;
        if (mustPay) {
            cost = Econ.calculateClaimCost(ownedLand, currentFaction.isNormal());

            if (FactionsPlugin.getInstance().conf().economy().getClaimUnconnectedFee() != 0.0 && forFaction.getLandRoundedInWorld(flocation.getWorldName()) > 0 && !Board.getInstance().isConnectedLocation(flocation, forFaction)) {
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

        if (LWC.getEnabled() && forFaction.isNormal() && FactionsPlugin.getInstance().conf().lwc().isResetLocksOnCapture()) {
            LWC.clearOtherLocks(flocation, this.getFaction());
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

    public boolean attemptUnclaim(Faction forFaction, FLocation flocation, boolean notifyFailure) {
        Faction targetFaction = Board.getInstance().getFactionAt(flocation);

        if (!targetFaction.equals(forFaction)) {
            this.msg(TL.COMMAND_UNCLAIM_WRONGFACTIONOTHER);
            return false;
        }

        if (targetFaction.isSafeZone()) {
            if (Permission.MANAGE_SAFE_ZONE.has(this.getPlayer())) {
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
            if (Permission.MANAGE_WAR_ZONE.has(this.getPlayer())) {
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

    public void msg(String str, Object... args) {
        this.sendMessage(FactionsPlugin.getInstance().txt().parse(str, args));
    }

    public void msg(TL translation, Object... args) {
        this.msg(translation.toString(), args);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(UUID.fromString(this.getId()));
    }

    public boolean isOnline() {
        Player player = this.getPlayer();
        return player != null && FactionsPlugin.getInstance().worldUtil().isEnabled(player.getWorld());
    }

    // make sure target player should be able to detect that this player is online
    public boolean isOnlineAndVisibleTo(Player player) {
        Player target = this.getPlayer();
        return target != null && player.canSee(target) && FactionsPlugin.getInstance().worldUtil().isEnabled(player.getWorld());
    }

    public boolean isOffline() {
        return !isOnline();
    }

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

    public boolean isFlying() {
        return isFlying;
    }

    public void setFlying(boolean fly) {
        setFlying(fly, false);
    }

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
                }.runTaskLater(FactionsPlugin.getInstance(), 20L * cooldown);
            }
        }

        isFlying = fly;
    }

    public boolean isAutoFlying() {
        return isAutoFlying;
    }

    public void setAutoFlying(boolean autoFly) {
        msg(TL.COMMAND_FLY_AUTO, autoFly ? "enabled" : "disabled");
        this.isAutoFlying = autoFly;
    }

    public boolean canFlyAtLocation() {
        return canFlyAtLocation(lastStoodAt);
    }

    public boolean canFlyAtLocation(FLocation location) {
        Faction faction = Board.getInstance().getFactionAt(location);
        if (faction.isWilderness()) {
            return Permission.FLY_WILDERNESS.has(getPlayer());
        } else if (faction.isSafeZone()) {
            return Permission.FLY_SAFEZONE.has(getPlayer());
        } else if (faction.isWarZone()) {
            return Permission.FLY_WARZONE.has(getPlayer());
        }

        // admin bypass (ops) can fly.
        if (isAdminBypassing) {
            return true;
        }

        return faction.hasAccess(this, PermissibleActions.FLY, location);
    }

    public boolean shouldTakeFallDamage() {
        return this.shouldTakeFallDamage;
    }

    public void setTakeFallDamage(boolean fallDamage) {
        this.shouldTakeFallDamage = fallDamage;
    }

    public boolean isSeeingChunk() {
        return seeingChunk;
    }

    public void setSeeingChunk(boolean seeingChunk) {
        this.seeingChunk = seeingChunk;
        FactionsPlugin.getInstance().getSeeChunkUtil().updatePlayerInfo(UUID.fromString(getId()), seeingChunk);
    }

    public boolean getFlyTrailsState() {
        return flyTrailsState;
    }

    public void setFlyTrailsState(boolean state) {
        flyTrailsState = state;
        msg(TL.COMMAND_FLYTRAILS_CHANGE, state ? "enabled" : "disabled");
    }

    public String getFlyTrailsEffect() {
        return flyTrailsEffect;
    }

    public void setFlyTrailsEffect(String effect) {
        flyTrailsEffect = effect;
        msg(TL.COMMAND_FLYTRAILS_PARTICLE_CHANGE, effect);
    }

    // -------------------------------------------- //
    // Message Sending Helpers
    // -------------------------------------------- //

    public void sendMessage(String msg) {
        if (msg.contains("{null}")) {
            return; // user wants this message to not send
        }
        if (msg.contains("/n/")) {
            for (String s : msg.split("/n/")) {
                sendMessage(s);
            }
            return;
        }
        Player player = this.getPlayer();
        if (player == null) {
            return;
        }
        player.sendMessage(msg);
    }

    public void sendMessage(List<String> msgs) {
        for (String msg : msgs) {
            this.sendMessage(msg);
        }
    }

    public int getMapHeight() {
        if (this.mapHeight < 1) {
            this.mapHeight = FactionsPlugin.getInstance().conf().map().getHeight();
        }

        return this.mapHeight;
    }

    public void setMapHeight(int height) {
        this.mapHeight = Math.min(height, (FactionsPlugin.getInstance().conf().map().getHeight() * 2));
    }

    public String getNameAndTitle(FPlayer fplayer) {
        return this.getColorStringTo(fplayer) + this.getNameAndTitle();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
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
    public WarmUpUtil.Warmup getWarmupType() {
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
