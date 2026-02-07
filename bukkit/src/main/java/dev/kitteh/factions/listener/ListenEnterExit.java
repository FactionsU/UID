package dev.kitteh.factions.listener;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.data.MemoryFPlayer;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.scoreboard.FScoreboard;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public class ListenEnterExit implements Listener {
    private final AbstractFactionsPlugin plugin;

    public ListenEnterExit(AbstractFactionsPlugin plugin) {
        this.plugin = plugin;

        // Reloading is *bad*!
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            initPlayer(player);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        initPlayer(event.getPlayer());
        this.plugin.updateNotification(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        FPlayer me = FPlayers.fPlayers().get(player);

        this.plugin.landRaidControl().onQuit(me);

        ((MemoryFPlayer) me).onLogInOut();

        Faction myFaction = me.faction();
        if (!myFaction.isWilderness()) {
            myFaction.trackMemberLoggedOff();
        }

        if (!myFaction.isWilderness()) {
            for (FPlayer fPlayer : myFaction.membersOnline(true)) {
                if (fPlayer != me && fPlayer.monitorJoins()) {
                    fPlayer.msgLegacy(TL.FACTION_LOGOUT, me.name());
                }
            }
        }

        FScoreboard.remove(me, event.getPlayer());

        this.plugin.seeChunkUtil().updatePlayerInfo(me.uniqueId(), false);
        ((MemoryFPlayer) me).setOfflinePlayer(null);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        FPlayer me = FPlayers.fPlayers().get(event.getPlayer());

        if (!WorldUtil.isEnabled(event.getPlayer())) {
            return;
        }

        this.plugin.landRaidControl().onRespawn(me);

        Location home = me.faction().home();
        MainConfig.Factions facConf = this.plugin.conf().factions();
        if (facConf.homes().isEnabled() &&
                facConf.homes().isTeleportToOnDeath() &&
                home != null &&
                (facConf.landRaidControl().power().isRespawnHomeFromNoPowerLossWorlds() || !facConf.landRaidControl().power().getWorldsNoPowerLoss().contains(event.getPlayer().getWorld().getName()))) {
            event.setRespawnLocation(home);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                me.flightCheck();
            }
        }.runTask(this.plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        FPlayer me = FPlayers.fPlayers().get(event.getPlayer());
        boolean isEnabled = WorldUtil.isEnabled(event.getPlayer());
        event.getPlayer().updateCommands();
        if (!isEnabled) {
            FScoreboard.remove(me, event.getPlayer());
            if (me.flying()) {
                me.flying(false);
            }
            return;
        }
        FLocation to = new FLocation(event.getPlayer().getLocation());
        me.lastStoodAt(to);
        me.flightCheck();
        if (!event.getFrom().equals(event.getPlayer().getWorld()) && !WorldUtil.isEnabled(event.getFrom())) {
            this.plugin.landRaidControl().update(me);
            this.initFactionWorld(event.getPlayer(), me);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onGameMode(PlayerGameModeChangeEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer())) {
            return;
        }
        if (event.getNewGameMode() == GameMode.SURVIVAL) {
            FPlayer me = FPlayers.fPlayers().get(event.getPlayer());
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (me.flying() && me.asPlayer() instanceof Player player) {
                        player.setAllowFlight(true);
                        player.setFlying(true);
                    }
                    me.flightCheck();
                }
            }.runTask(this.plugin);
        }
    }

    private void initPlayer(Player player) {
        // Make sure that all online players do have a fplayer.
        final FPlayer me = FPlayers.fPlayers().get(player);
        ((MemoryFPlayer) me).setName(player.getName());

        this.plugin.landRaidControl().onJoin(me);

        FLocation standing = new FLocation(player.getLocation());
        // Store player's current FLocation
        me.lastStoodAt(standing);

        if (this.plugin.conf().factions().protection().territoryTeleport().isEnabled()) {
            long diff = System.currentTimeMillis() - me.lastLogin();
            MainConfig.Factions.Protection.TerritoryTeleport terry = this.plugin.conf().factions().protection().territoryTeleport();
            if (diff > (1000L * terry.getTimeSinceLastSignedIn())) {
                Faction standingFaction = standing.faction();
                Relation relation = me.relationTo(standingFaction);
                if (terry.isRelationToTeleportOut(relation, standingFaction)) {
                    Location target = null;
                    for (String destination : terry.getDestination().split(",")) {
                        switch (destination.trim().toLowerCase()) {
                            case "spawn":
                                World world = this.plugin.getServer().getWorld(terry.getDestinationSpawnWorld());
                                if (world != null) {
                                    target = world.getSpawnLocation();
                                }
                                break;
                            case "home":
                                if (me.hasFaction()) {
                                    target = me.faction().home();
                                }
                                break;
                            case "bed":
                                target = player.getRespawnLocation();
                        }
                        if (target != null) {
                            break;
                        }
                    }
                    if (target == null) {
                        target = this.plugin.getServer().getWorlds().getFirst().getSpawnLocation();
                    }
                    this.plugin.teleport(player, target).thenAccept(success -> {
                        if (success) {
                            me.msgLegacy(TL.PLAYER_TELEPORTEDONJOIN, relation.nicename);
                        }
                    });
                }
            }
        }

        ((MemoryFPlayer) me).onLogInOut();
        ((MemoryFPlayer) me).setOfflinePlayer(player);

        if (me.spyingChat() && !player.hasPermission(Permission.CHATSPY.node)) {
            me.spyingChat(false);
            this.plugin.log(Level.INFO, "Found " + player.getName() + " spying chat without permission on login. Disabled their chat spying.");
        }

        if (me.adminBypass() && !player.hasPermission(Permission.BYPASS.node)) {
            me.adminBypass(false);
            this.plugin.log(Level.INFO, "Found " + player.getName() + " on admin Bypass without permission on login. Disabled it for them.");
        }

        if (WorldUtil.isEnabled(player)) {
            this.initFactionWorld(player, me);
        }
        player.updateCommands();
    }

    private void initFactionWorld(Player player, FPlayer me) {
        // Check for Faction announcements. Let's delay this so they actually see it.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (me.isOnline()) {
                    me.faction().sendUnreadAnnouncements(me);
                }
            }
        }.runTaskLater(this.plugin, 33L); // Don't ask me why.

        if (this.plugin.conf().scoreboard().constant().isEnabled()) {
            FScoreboard.init(player, me);
            FScoreboard.get(me).setSidebarVisibility(me.showScoreboard());
        }

        Faction myFaction = me.faction();
        if (!myFaction.isWilderness()) {
            for (FPlayer other : myFaction.membersOnline(true)) {
                if (other != me && other.monitorJoins()) {
                    other.msgLegacy(TL.FACTION_LOGIN, me.name());
                }
            }
        }

        me.takeFallDamage(true);
        if (me.flying()) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }
        me.flightCheck();

        this.plugin.seeChunkUtil().updatePlayerInfo(me.uniqueId(), me.seeChunk());
    }

}
