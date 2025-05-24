package dev.kitteh.factions.listener;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.chat.ChatTarget;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.data.MemoryBoard;
import dev.kitteh.factions.data.MemoryFPlayer;
import dev.kitteh.factions.event.FPlayerJoinEvent;
import dev.kitteh.factions.event.FPlayerLeaveEvent;
import dev.kitteh.factions.gui.GUI;
import dev.kitteh.factions.integration.Graves;
import dev.kitteh.factions.permissible.PermissibleAction;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.scoreboard.FScoreboard;
import dev.kitteh.factions.scoreboard.FTeamWrapper;
import dev.kitteh.factions.scoreboard.sidebar.FDefaultSidebar;
import dev.kitteh.factions.util.ComponentDispatcher;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.TextUtil;
import dev.kitteh.factions.util.WorldUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.NumberConversions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class FactionsPlayerListener extends AbstractListener {

    private final AbstractFactionsPlugin plugin;

    public FactionsPlayerListener(AbstractFactionsPlugin plugin) {
        this.plugin = plugin;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            initPlayer(player);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        initPlayer(event.getPlayer());
        this.plugin.updateNotification(event.getPlayer());
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
                Faction standingFaction = Board.board().factionAt(standing);
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
                            me.msg(TL.PLAYER_TELEPORTEDONJOIN, relation.nicename);
                        }
                    });
                }
            }
        }

        ((MemoryFPlayer) me).onLogInOut();
        ((MemoryFPlayer) me).setOfflinePlayer(player);

        if (me.spyingChat() && !player.hasPermission(Permission.CHATSPY.node)) {
            me.spyingChat(false);
            this.plugin.log(Level.INFO, "Found %s spying chat without permission on login. Disabled their chat spying.", player.getName());
        }

        if (me.adminBypass() && !player.hasPermission(Permission.BYPASS.node)) {
            me.adminBypass(false);
            this.plugin.log(Level.INFO, "Found %s on admin Bypass without permission on login. Disabled it for them.", player.getName());
        }

        if (WorldUtil.isEnabled(player.getWorld())) {
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
            FScoreboard.get(me).setDefaultSidebar(new FDefaultSidebar());
            FScoreboard.get(me).setSidebarVisibility(me.showScoreboard());
        }

        Faction myFaction = me.faction();
        if (!myFaction.isWilderness()) {
            for (FPlayer other : myFaction.membersOnline(true)) {
                if (other != me && other.monitorJoins()) {
                    other.msg(TL.FACTION_LOGIN, me.name());
                }
            }
        }

        me.takeFallDamage(true);
        if (me.flying()) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }
        me.flightCheck();

        if (this.plugin.seeChunkUtil() != null) {
            this.plugin.seeChunkUtil().updatePlayerInfo(me.uniqueId(), me.seeChunk());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        FPlayer me = FPlayers.fPlayers().get(player);

        this.plugin.landRaidControl().onQuit(me);

        ((MemoryFPlayer) me).onLogInOut();

        // if player is waiting for fstuck teleport but leaves, remove
        if (this.plugin.stuckMap().containsKey(player.getUniqueId())) {
            FPlayers.fPlayers().get(player).msg(TL.COMMAND_STUCK_CANCELLED);
            this.plugin.stuckMap().remove(player.getUniqueId());
            this.plugin.timers().remove(player.getUniqueId());
        }

        Faction myFaction = me.faction();
        if (!myFaction.isWilderness()) {
            myFaction.trackMemberLoggedOff();
        }

        if (!myFaction.isWilderness()) {
            for (FPlayer fPlayer : myFaction.membersOnline(true)) {
                if (fPlayer != me && fPlayer.monitorJoins()) {
                    fPlayer.msg(TL.FACTION_LOGOUT, me.name());
                }
            }
        }

        FScoreboard.remove(me, event.getPlayer());

        if (this.plugin.seeChunkUtil() != null) {
            this.plugin.seeChunkUtil().updatePlayerInfo(me.uniqueId(), false);
        }
        ((MemoryFPlayer) me).setOfflinePlayer(null);
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
                    if (me.flying() && me.asPlayer() != null) {
                        me.asPlayer().setAllowFlight(true);
                        me.asPlayer().setFlying(true);
                    }
                    me.flightCheck();
                }
            }.runTask(this.plugin);
        }
    }

    // Holds the next time a player can have a map shown.
    private final HashMap<UUID, Long> showTimes = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        this.handleMovement(event.getPlayer(), event.getFrom(), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        this.handleMovement(event.getPlayer(), event.getFrom(), event.getTo());
    }

    private void handleMovement(Player player, Location fromLoc, Location toLoc) {
        if (!WorldUtil.isEnabled(player)) {
            return;
        }
        FPlayer me = FPlayers.fPlayers().get(player);

        // clear visualization
        if (fromLoc.getBlockX() != toLoc.getBlockX() || fromLoc.getBlockY() != toLoc.getBlockY() || fromLoc.getBlockZ() != toLoc.getBlockZ() || fromLoc.getWorld() != toLoc.getWorld()) {
            if (me.warmingUp()) {
                me.cancelWarmup();
                me.msg(TL.WARMUPS_CANCELLED);
            }
        }

        // quick check to make sure player is moving between chunks; good performance boost
        if (fromLoc.getBlockX() >> 4 == toLoc.getBlockX() >> 4 && fromLoc.getBlockZ() >> 4 == toLoc.getBlockZ() >> 4 && fromLoc.getWorld() == toLoc.getWorld()) {
            return;
        }

        if (!WorldUtil.isEnabled(toLoc.getWorld())) {
            return;
        }

        // Did we change coord?
        FLocation from = new FLocation(fromLoc);
        FLocation to = new FLocation(toLoc);

        if (from.equals(to)) {
            return;
        }

        // Yes we did change coord (:

        me.lastStoodAt(to);

        boolean canFlyPreClaim = me.canFlyAtLocation();

        if (me.autoClaim() != null) {
            me.attemptClaim(me.autoClaim(), to, true);
        } else if (me.autoUnclaim() != null) {
            me.attemptUnclaim(me.autoUnclaim(), to, true);
        }

        // Did we change "host"(faction)?
        Faction factionFrom = Board.board().factionAt(from);
        Faction factionTo = Board.board().factionAt(to);
        boolean changedFaction = (factionFrom != factionTo);

        if (factionTo == me.faction()) {
            me.attemptAutoSetZone(to);
        }

        free:
        if (plugin.conf().commands().fly().isEnable() && !me.adminBypass()) {
            boolean canFly = me.canFlyAtLocation(to);
            if (!changedFaction) {
                if (canFly && !canFlyPreClaim && me.flying() && plugin.conf().commands().fly().isDisableFlightDuringAutoclaim()) {
                    me.flying(false);
                }
                break free;
            }
            if (me.flying() && !canFly) {
                me.flying(false);
            } else if (me.autoFlying() && !me.flying() && canFly) {
                me.flying(true);
            }
        }

        if (me.mapAutoUpdating()) {
            if (!showTimes.containsKey(player.getUniqueId()) || (showTimes.get(player.getUniqueId()) < System.currentTimeMillis())) {
                for (Component component : ((MemoryBoard) Board.board()).getMap(me, to, player.getLocation().getYaw())) {
                    me.sendMessage(component);
                }
                showTimes.put(player.getUniqueId(), System.currentTimeMillis() + this.plugin.conf().commands().map().getCooldown());
            }
        } else if (changedFaction) {
            me.sendFactionHereMessage(factionFrom);
        }

        if (factionTo.isNormal()) {
            Faction.Zone oldZone = factionFrom.zones().get(from);
            Faction.Zone newZone = factionTo.zones().get(to);
            if (!oldZone.equals(newZone) && (changedFaction || !oldZone.greeting().equals(newZone.greeting()))) {
                ComponentDispatcher.sendActionBar(player, newZone.greeting());
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer().getWorld())) {
            return;
        }

        boolean check = false;
        EntityType type = event.getRightClicked().getType();
        if (type == EntityType.ITEM_FRAME || type == EntityType.GLOW_ITEM_FRAME) {
            if (!canPlayerUseBlock(event.getPlayer(), Material.ITEM_FRAME, event.getRightClicked().getLocation(), false)) {
                event.setCancelled(true);
            }
        } else if (type == EntityType.HORSE ||
                type == EntityType.SKELETON_HORSE ||
                type == EntityType.ZOMBIE_HORSE ||
                type == EntityType.DONKEY ||
                type == EntityType.MULE ||
                type == EntityType.LLAMA ||
                type == EntityType.TRADER_LLAMA ||
                type == EntityType.PIG ||
                type == EntityType.LEASH_KNOT ||
                type.name().contains("_MINECART") ||
                type.name().contains("_CHEST_BOAT")
        ) {
            check = true;
        }

        if (check && !this.plugin.conf().factions().protection().getEntityInteractExceptions().contains(event.getRightClicked().getType().name()) &&
                !this.playerCanInteractHere(event.getPlayer(), event.getRightClicked().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer().getWorld())) {
            return;
        }

        // only need to check right-clicks and physical as of MC 1.4+; good performance boost
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.PHYSICAL) {
            return;
        }

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        if (block == null) {
            return;  // clicked in air, apparently
        }

        if (Graves.allowAnyway(block)) {
            return;
        }

        if (event.getAction() == Action.PHYSICAL && block.getType() == Material.FARMLAND) {
            if (!FactionsBlockListener.playerCanBuildDestroyBlock(player, block.getLocation(), PermissibleActions.DESTROY, false)) {
                event.setCancelled(true);
            }
        }

        if (!canPlayerUseBlock(player, block.getType(), block.getLocation(), false)) {
            event.setCancelled(true);
            if (block.getType().name().endsWith("_PLATE")) {
                return;
            }
            if (this.plugin.conf().exploits().isInteractionSpam()) {
                String name = player.getName();
                InteractAttemptSpam attempt = interactSpammers.get(name);
                if (attempt == null) {
                    attempt = new InteractAttemptSpam();
                    interactSpammers.put(name, attempt);
                }
                int count = attempt.increment();
                if (count >= 10) {
                    FPlayer me = FPlayers.fPlayers().get(player);
                    me.msg(TL.PLAYER_OUCH);
                    player.damage(NumberConversions.floor((double) count / 10));
                }
            }
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;  // only interested on right-clicks for below
        }

        ItemStack item;
        if ((item = event.getItem()) != null) {
            Material material = item.getType();
            String materialName = item.getType().name();
            if (material == Material.ARMOR_STAND || material == Material.END_CRYSTAL || materialName.contains("MINECART")) {
                if (!this.plugin.conf().factions().specialCase().getIgnoreBuildMaterials().contains(item.getType()) &&
                        !FactionsBlockListener.playerCanBuildDestroyBlock(event.getPlayer(), event.getClickedBlock().getRelative(event.getBlockFace()).getLocation(), PermissibleActions.BUILD, false)) {
                    event.setCancelled(true);
                }
            }

        }

        if (!playerCanUseItemHere(player, block.getLocation(), event.getMaterial(), false)) {
            event.setCancelled(true);
        }
    }


    // for handling people who repeatedly spam attempts to open a door (or similar) in another faction's territory
    private final Map<String, InteractAttemptSpam> interactSpammers = new HashMap<>();

    private static class InteractAttemptSpam {
        private int attempts = 0;
        private long lastAttempt = System.currentTimeMillis();

        // returns the current attempt count
        public int increment() {
            long Now = System.currentTimeMillis();
            if (Now > lastAttempt + 2000) {
                attempts = 1;
            } else {
                attempts++;
            }
            lastAttempt = Now;
            return attempts;
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!WorldUtil.isEnabled(event.getBlock().getWorld())) {
            return;
        }

        if (!this.playerCanUseItemHere(event.getPlayer(), event.getBlock().getLocation(), event.getBlock().getType(), false, false)) {
            event.setCancelled(true);
        }
    }

    public boolean playerCanUseItemHere(Player player, Location location, Material material, boolean justCheck) {
        return this.playerCanUseItemHere(player, location, material, true, justCheck);
    }

    public boolean playerCanUseItemHere(Player player, Location location, Material material, boolean checkDenyList, boolean justCheck) {
        String name = player.getName();
        MainConfig.Factions facConf = this.plugin.conf().factions();
        if (facConf.protection().getPlayersWhoBypassAllProtection().contains(name)) {
            return true;
        }

        FPlayer me = FPlayers.fPlayers().get(player);
        if (me.adminBypass()) {
            return true;
        }

        FLocation loc = new FLocation(location);
        Faction otherFaction = Board.board().factionAt(loc);

        if (this.plugin.landRaidControl().isRaidable(otherFaction)) {
            return true;
        }

        if (checkDenyList) {
            if (otherFaction.hasMembersOnline()) {
                if (!facConf.protection().getTerritoryDenyUsageMaterials().contains(material)) {
                    return true; // Item isn't one we're preventing for online factions.
                }
            } else {
                if (!facConf.protection().getTerritoryDenyUsageMaterialsWhenOffline().contains(material)) {
                    return true; // Item isn't one we're preventing for offline factions.
                }
            }
        }

        if (otherFaction.isWilderness()) {
            if (!facConf.protection().isWildernessDenyUsage() || facConf.protection().getWorldsNoWildernessProtection().contains(location.getWorld().getName())) {
                return true; // This is not faction territory. Use whatever you like here.
            }

            if (!justCheck) {
                me.msg(TL.PLAYER_USE_WILDERNESS, TextUtil.getMaterialName(material));
            }

            return false;
        } else if (otherFaction.isSafeZone()) {
            if (!facConf.protection().isSafeZoneDenyUsage() || Permission.MANAGE_SAFE_ZONE.has(player)) {
                return true;
            }

            if (!justCheck) {
                me.msg(TL.PLAYER_USE_SAFEZONE, TextUtil.getMaterialName(material));
            }

            return false;
        } else if (otherFaction.isWarZone()) {
            if (!facConf.protection().isWarZoneDenyUsage() || Permission.MANAGE_WAR_ZONE.has(player)) {
                return true;
            }

            if (!justCheck) {
                me.msg(TL.PLAYER_USE_WARZONE, TextUtil.getMaterialName(material));
            }

            return false;
        }

        if (!otherFaction.hasAccess(me, PermissibleActions.ITEM, loc)) {
            if (!justCheck) {
                me.msg(TL.PLAYER_USE_TERRITORY, TextUtil.getMaterialName(material), otherFaction.tagString(me.faction()));
            }
            return false;
        }

        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer().getWorld())) {
            return;
        }

        FPlayer me = FPlayers.fPlayers().get(event.getPlayer());

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
        boolean isEnabled = WorldUtil.isEnabled(event.getPlayer().getWorld());
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

    // For some reason onPlayerInteract() sometimes misses bucket events depending on distance (something like 2-3 blocks away isn't detected),
    // but these separate bucket events below always fire without fail
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer().getWorld())) {
            return;
        }

        Block block = event.getBlockClicked();
        Player player = event.getPlayer();

        if (!playerCanUseItemHere(player, block.getRelative(event.getBlockFace()).getLocation(), event.getBucket(), false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer().getWorld())) {
            return;
        }

        Block block = event.getBlockClicked();
        Player player = event.getPlayer();

        if (!playerCanUseItemHere(player, block.getLocation(), event.getBucket(), false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void doYouHaveALibraryCard(PlayerTakeLecternBookEvent event) {
        Player player = event.getPlayer();
        if (this.plugin.conf().factions().protection().getPlayersWhoBypassAllProtection().contains(player.getName())) {
            return;
        }

        FPlayer me = FPlayers.fPlayers().get(player);
        if (me.adminBypass()) {
            return;
        }

        FLocation location = new FLocation(event.getLectern().getLocation());
        Faction otherFaction = Board.board().factionAt(location);
        if (this.plugin.landRaidControl().isRaidable(otherFaction)) {
            return;
        }

        PermissibleAction action = PermissibleActions.CONTAINER;
        if (!otherFaction.hasAccess(me, action, location)) {
            me.msg(TL.GENERIC_NOPERMISSION, action.shortDescription());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerArmorStandManipulateEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer().getWorld())) {
            return;
        }

        if (!canPlayerUseBlock(event.getPlayer(), Material.ARMOR_STAND, event.getRightClicked().getLocation(), false)) {
            event.setCancelled(true);
        }
    }

    public static boolean preventCommand(String fullCmd, Player player) {
        MainConfig.Factions.Protection protection = FactionsPlugin.instance().conf().factions().protection();
        if ((protection.getTerritoryNeutralDenyCommands().isEmpty() &&
                protection.getTerritoryEnemyDenyCommands().isEmpty() &&
                protection.getPermanentFactionMemberDenyCommands().isEmpty() &&
                protection.getWildernessDenyCommands().isEmpty() &&
                protection.getTerritoryAllyDenyCommands().isEmpty() &&
                protection.getTerritoryTruceDenyCommands().isEmpty() &&
                protection.getWarzoneDenyCommands().isEmpty())) {
            return false;
        }

        fullCmd = fullCmd.toLowerCase();

        FPlayer me = FPlayers.fPlayers().get(player);

        String shortCmd;  // command without the slash at the beginning
        if (fullCmd.startsWith("/")) {
            shortCmd = fullCmd.substring(1);
        } else {
            shortCmd = fullCmd;
            fullCmd = "/" + fullCmd;
        }

        if (me.hasFaction() &&
                !me.adminBypass() &&
                !protection.getPermanentFactionMemberDenyCommands().isEmpty() &&
                me.faction().permanent() &&
                isCommandInSet(fullCmd, shortCmd, protection.getPermanentFactionMemberDenyCommands())) {
            me.msg(TL.PLAYER_COMMAND_PERMANENT, fullCmd);
            return true;
        }

        Faction at = Board.board().factionAt(new FLocation(player.getLocation()));
        if (at.isWilderness() && !protection.getWildernessDenyCommands().isEmpty() && !me.adminBypass() && isCommandInSet(fullCmd, shortCmd, protection.getWildernessDenyCommands())) {
            me.msg(TL.PLAYER_COMMAND_WILDERNESS, fullCmd);
            return true;
        }

        Relation rel = at.relationTo(me);
        if (at.isNormal() && rel.isAlly() && !protection.getTerritoryAllyDenyCommands().isEmpty() && !me.adminBypass() && isCommandInSet(fullCmd, shortCmd, protection.getTerritoryAllyDenyCommands())) {
            me.msg(TL.PLAYER_COMMAND_ALLY, fullCmd);
            return true;
        }

        if (at.isNormal() && rel.isTruce() && !protection.getTerritoryTruceDenyCommands().isEmpty() && !me.adminBypass() && isCommandInSet(fullCmd, shortCmd, protection.getTerritoryTruceDenyCommands())) {
            me.msg(TL.PLAYER_COMMAND_TRUCE, fullCmd);
            return true;
        }

        if (at.isNormal() && rel.isNeutral() && !protection.getTerritoryNeutralDenyCommands().isEmpty() && !me.adminBypass() && isCommandInSet(fullCmd, shortCmd, protection.getTerritoryNeutralDenyCommands())) {
            me.msg(TL.PLAYER_COMMAND_NEUTRAL, fullCmd);
            return true;
        }

        if (at.isNormal() && rel.isEnemy() && !protection.getTerritoryEnemyDenyCommands().isEmpty() && !me.adminBypass() && isCommandInSet(fullCmd, shortCmd, protection.getTerritoryEnemyDenyCommands())) {
            me.msg(TL.PLAYER_COMMAND_ENEMY, fullCmd);
            return true;
        }

        if (at.isWarZone() && !protection.getWarzoneDenyCommands().isEmpty() && !me.adminBypass() && isCommandInSet(fullCmd, shortCmd, protection.getWarzoneDenyCommands())) {
            me.msg(TL.PLAYER_COMMAND_WARZONE, fullCmd);
            return true;
        }

        return false;
    }

    private static boolean isCommandInSet(String fullCmd, String shortCmd, Set<String> set) {
        for (String string : set) {
            if (string == null) {
                continue;
            }
            string = string.toLowerCase();
            if (fullCmd.startsWith(string) || shortCmd.startsWith(string)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractGUI(InventoryClickEvent event) {
        if (!WorldUtil.isEnabled(event.getWhoClicked().getWorld())) {
            return;
        }

        Inventory clickedInventory = getClickedInventory(event);
        if (clickedInventory == null) {
            return;
        }
        if (clickedInventory.getHolder() instanceof GUI<?> ui) {
            event.setCancelled(true);
            ui.click(event.getRawSlot(), event.getClick());
        }
    }

    private Inventory getClickedInventory(InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        InventoryView view = event.getView();
        if (rawSlot < 0 || rawSlot >= view.countSlots()) { // < 0 check also covers situation of InventoryView.OUTSIDE (-999)
            return null;
        }
        if (rawSlot < view.getTopInventory().getSize()) {
            return view.getTopInventory();
        } else {
            return view.getBottomInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMoveGUI(InventoryDragEvent event) {
        if (!WorldUtil.isEnabled(event.getWhoClicked().getWorld())) {
            return;
        }

        if (event.getInventory().getHolder() instanceof GUI) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    final public void onFactionJoin(FPlayerJoinEvent event) {
        FTeamWrapper.applyUpdatesLater(event.getFaction());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFactionLeave(FPlayerLeaveEvent event) {
        FTeamWrapper.applyUpdatesLater(event.getFaction());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer().getWorld())) {
            return;
        }

        String cmd = event.getMessage().split(" ")[0];

        if (this.plugin.conf().factions().chat().isTriggerPublicChat(cmd.startsWith("/") ? cmd.substring(1) : cmd)) {
            FPlayer p = FPlayers.fPlayers().get(event.getPlayer());
            p.chatTarget(ChatTarget.PUBLIC);
            p.msg(TL.COMMAND_CHAT_MODE_PUBLIC);
        }

        if (FactionsPlayerListener.preventCommand(event.getMessage(), event.getPlayer())) {
            if (plugin.conf().logging().isPlayerCommands()) {
                plugin.getLogger().info("[PLAYER_COMMAND] " + event.getPlayer().getName() + ": " + event.getMessage());
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreLogin(PlayerLoginEvent event) {
        FPlayers.fPlayers().get(event.getPlayer());
    }
}
