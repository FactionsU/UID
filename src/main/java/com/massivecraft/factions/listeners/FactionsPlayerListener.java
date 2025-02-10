package com.massivecraft.factions.listeners;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.file.MainConfig;
import com.massivecraft.factions.data.MemoryFPlayer;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.gui.GUI;
import com.massivecraft.factions.integration.Graves;
import com.massivecraft.factions.perms.PermissibleAction;
import com.massivecraft.factions.perms.PermissibleActions;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.scoreboards.FScoreboard;
import com.massivecraft.factions.scoreboards.FTeamWrapper;
import com.massivecraft.factions.scoreboards.sidebar.FDefaultSidebar;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import com.massivecraft.factions.util.TextUtil;
import com.massivecraft.factions.util.VisualizeUtil;
import net.kyori.adventure.audience.Audience;
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
import org.bukkit.event.player.PlayerKickEvent;
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

    private final FactionsPlugin plugin;

    public FactionsPlayerListener(FactionsPlugin plugin) {
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
        final FPlayer me = FPlayers.getInstance().getByPlayer(player);
        ((MemoryFPlayer) me).setName(player.getName());

        this.plugin.getLandRaidControl().onJoin(me);

        FLocation standing = new FLocation(player.getLocation());
        // Store player's current FLocation
        me.setLastStoodAt(standing);

        if (this.plugin.conf().factions().protection().territoryTeleport().isEnabled()) {
            long diff = System.currentTimeMillis() - me.getLastLoginTime();
            MainConfig.Factions.Protection.TerritoryTeleport terry = this.plugin.conf().factions().protection().territoryTeleport();
            if (diff > (1000L * terry.getTimeSinceLastSignedIn())) {
                Faction standingFaction = Board.getInstance().getFactionAt(standing);
                Relation relation = me.getRelationTo(standingFaction);
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
                                    target = me.getFaction().getHome();
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

        // Update the lastLoginTime for this fplayer
        me.setLastLoginTime(System.currentTimeMillis());

        me.login(); // set kills / deaths
        me.setOfflinePlayer(player);

        if (me.isSpyingChat() && !player.hasPermission(Permission.CHATSPY.node)) {
            me.setSpyingChat(false);
            FactionsPlugin.getInstance().log(Level.INFO, "Found %s spying chat without permission on login. Disabled their chat spying.", player.getName());
        }

        if (me.isAdminBypassing() && !player.hasPermission(Permission.BYPASS.node)) {
            me.setIsAdminBypassing(false);
            FactionsPlugin.getInstance().log(Level.INFO, "Found %s on admin Bypass without permission on login. Disabled it for them.", player.getName());
        }

        if (plugin.worldUtil().isEnabled(player.getWorld())) {
            this.initFactionWorld(me);
        }
    }

    private void initFactionWorld(FPlayer me) {
        // Check for Faction announcements. Let's delay this so they actually see it.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (me.isOnline()) {
                    me.getFaction().sendUnreadAnnouncements(me);
                }
            }
        }.runTaskLater(FactionsPlugin.getInstance(), 33L); // Don't ask me why.

        if (FactionsPlugin.getInstance().conf().scoreboard().constant().isEnabled()) {
            FScoreboard.init(me);
            FScoreboard.get(me).setDefaultSidebar(new FDefaultSidebar());
            FScoreboard.get(me).setSidebarVisibility(me.showScoreboard());
        }

        Faction myFaction = me.getFaction();
        if (!myFaction.isWilderness()) {
            for (FPlayer other : myFaction.getFPlayersWhereOnline(true)) {
                if (other != me && other.isMonitoringJoins()) {
                    other.msg(TL.FACTION_LOGIN, me.getName());
                }
            }
        }

        // If they have the permission, don't let them autoleave. Bad inverted setter :\
        me.setAutoLeave(!me.getPlayer().hasPermission(Permission.AUTO_LEAVE_BYPASS.node));
        me.setTakeFallDamage(true);
        if (me.isFlying()) {
            me.getPlayer().setAllowFlight(true);
            me.getPlayer().setFlying(true);
        }
        me.flightCheck();

        if (FactionsPlugin.getInstance().getSeeChunkUtil() != null) {
            FactionsPlugin.getInstance().getSeeChunkUtil().updatePlayerInfo(UUID.fromString(me.getId()), me.isSeeingChunk());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        FPlayer me = FPlayers.getInstance().getByPlayer(event.getPlayer());

        FactionsPlugin.getInstance().getLandRaidControl().onQuit(me);
        // and update their last login time to point to when the logged off, for auto-remove routine
        me.setLastLoginTime(System.currentTimeMillis());

        me.logout(); // cache kills / deaths

        // if player is waiting for fstuck teleport but leaves, remove
        if (FactionsPlugin.getInstance().getStuckMap().containsKey(me.getPlayer().getUniqueId())) {
            FPlayers.getInstance().getByPlayer(me.getPlayer()).msg(TL.COMMAND_STUCK_CANCELLED);
            FactionsPlugin.getInstance().getStuckMap().remove(me.getPlayer().getUniqueId());
            FactionsPlugin.getInstance().getTimers().remove(me.getPlayer().getUniqueId());
        }

        Faction myFaction = me.getFaction();
        if (!myFaction.isWilderness()) {
            myFaction.memberLoggedOff();
        }

        if (!myFaction.isWilderness()) {
            for (FPlayer player : myFaction.getFPlayersWhereOnline(true)) {
                if (player != me && player.isMonitoringJoins()) {
                    player.msg(TL.FACTION_LOGOUT, me.getName());
                }
            }
        }

        FScoreboard.remove(me, event.getPlayer());

        if (FactionsPlugin.getInstance().getSeeChunkUtil() != null) {
            FactionsPlugin.getInstance().getSeeChunkUtil().updatePlayerInfo(UUID.fromString(me.getId()), false);
        }
        me.setOfflinePlayer(null);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onGameMode(PlayerGameModeChangeEvent event) {
        if (!plugin.worldUtil().isEnabled(event.getPlayer())) {
            return;
        }
        if (event.getNewGameMode() == GameMode.SURVIVAL) {
            FPlayer me = FPlayers.getInstance().getByPlayer(event.getPlayer());
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (me.isFlying() && me.getPlayer() != null) {
                        me.getPlayer().setAllowFlight(true);
                        me.getPlayer().setFlying(true);
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
        if (!plugin.worldUtil().isEnabled(player)) {
            return;
        }
        FPlayer me = FPlayers.getInstance().getByPlayer(player);

        // clear visualization
        if (fromLoc.getBlockX() != toLoc.getBlockX() || fromLoc.getBlockY() != toLoc.getBlockY() || fromLoc.getBlockZ() != toLoc.getBlockZ() || fromLoc.getWorld() != toLoc.getWorld()) {
            VisualizeUtil.clear(player);
            if (me.isWarmingUp()) {
                me.clearWarmup();
                me.msg(TL.WARMUPS_CANCELLED);
            }
        }

        // quick check to make sure player is moving between chunks; good performance boost
        if (fromLoc.getBlockX() >> 4 == toLoc.getBlockX() >> 4 && fromLoc.getBlockZ() >> 4 == toLoc.getBlockZ() >> 4 && fromLoc.getWorld() == toLoc.getWorld()) {
            return;
        }

        if (!plugin.worldUtil().isEnabled(toLoc.getWorld())) {
            return;
        }

        // Did we change coord?
        FLocation from = new FLocation(fromLoc);
        FLocation to = new FLocation(toLoc);

        if (from.equals(to)) {
            return;
        }

        // Yes we did change coord (:

        me.setLastStoodAt(to);

        boolean canFlyPreClaim = me.canFlyAtLocation();

        if (me.getAutoClaimFor() != null) {
            me.attemptClaim(me.getAutoClaimFor(), to, true);
        } else if (me.getAutoUnclaimFor() != null) {
            me.attemptUnclaim(me.getAutoUnclaimFor(), to, true);
        }

        // Did we change "host"(faction)?
        Faction factionFrom = Board.getInstance().getFactionAt(from);
        Faction factionTo = Board.getInstance().getFactionAt(to);
        boolean changedFaction = (factionFrom != factionTo);

        free:
        if (plugin.conf().commands().fly().isEnable() && !me.isAdminBypassing()) {
            boolean canFly = me.canFlyAtLocation(to);
            if (!changedFaction) {
                if (canFly && !canFlyPreClaim && me.isFlying() && plugin.conf().commands().fly().isDisableFlightDuringAutoclaim()) {
                    me.setFlying(false);
                }
                break free;
            }
            if (me.isFlying() && !canFly) {
                me.setFlying(false);
            } else if (me.isAutoFlying() && !me.isFlying() && canFly) {
                me.setFlying(true);
            }
        }

        if (me.isMapAutoUpdating()) {
            if (!showTimes.containsKey(player.getUniqueId()) || (showTimes.get(player.getUniqueId()) < System.currentTimeMillis())) {
                Audience audience = FactionsPlugin.getInstance().getAdventure().player(player);
                for (Component component : Board.getInstance().getMap(me, to, player.getLocation().getYaw())) {
                    audience.sendMessage(component);
                }
                showTimes.put(player.getUniqueId(), System.currentTimeMillis() + FactionsPlugin.getInstance().conf().commands().map().getCooldown());
            }
        } else {
            Faction myFaction = me.getFaction();
            String ownersTo = myFaction.getOwnerListString(to);

            if (changedFaction) {
                me.sendFactionHereMessage(factionFrom);
                if (FactionsPlugin.getInstance().conf().factions().ownedArea().isEnabled() && FactionsPlugin.getInstance().conf().factions().ownedArea().isMessageOnBorder() && myFaction == factionTo && !ownersTo.isEmpty()) {
                    me.sendMessage(TL.GENERIC_OWNERS.format(ownersTo));
                }
            } else if (FactionsPlugin.getInstance().conf().factions().ownedArea().isEnabled() && FactionsPlugin.getInstance().conf().factions().ownedArea().isMessageInsideTerritory() && myFaction == factionTo && !myFaction.isWilderness()) {
                String ownersFrom = myFaction.getOwnerListString(from);
                if (FactionsPlugin.getInstance().conf().factions().ownedArea().isMessageByChunk() || !ownersFrom.equals(ownersTo)) {
                    if (!ownersTo.isEmpty()) {
                        me.sendMessage(TL.GENERIC_OWNERS.format(ownersTo));
                    } else if (!TL.GENERIC_PUBLICLAND.toString().isEmpty()) {
                        me.sendMessage(TL.GENERIC_PUBLICLAND.toString());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (!plugin.worldUtil().isEnabled(event.getPlayer().getWorld())) {
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
                type == EntityType.LEASH_HITCH ||
                type == EntityType.MINECART_CHEST ||
                type == EntityType.MINECART_FURNACE ||
                type == EntityType.MINECART_HOPPER ||
                type == EntityType.CHEST_BOAT
        ) {
            check = true;
        }

        if (check && !FactionsPlugin.getInstance().conf().factions().protection().getEntityInteractExceptions().contains(event.getRightClicked().getType().name()) &&
                !this.playerCanInteractHere(event.getPlayer(), event.getRightClicked().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.worldUtil().isEnabled(event.getPlayer().getWorld())) {
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
            if (FactionsPlugin.getInstance().conf().exploits().isInteractionSpam()) {
                String name = player.getName();
                InteractAttemptSpam attempt = interactSpammers.get(name);
                if (attempt == null) {
                    attempt = new InteractAttemptSpam();
                    interactSpammers.put(name, attempt);
                }
                int count = attempt.increment();
                if (count >= 10) {
                    FPlayer me = FPlayers.getInstance().getByPlayer(player);
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
                if (!FactionsPlugin.getInstance().conf().factions().specialCase().getIgnoreBuildMaterials().contains(item.getType()) &&
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
        if (!plugin.worldUtil().isEnabled(event.getBlock().getWorld())) {
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
        MainConfig.Factions facConf = FactionsPlugin.getInstance().conf().factions();
        if (facConf.protection().getPlayersWhoBypassAllProtection().contains(name)) {
            return true;
        }

        FPlayer me = FPlayers.getInstance().getByPlayer(player);
        if (me.isAdminBypassing()) {
            return true;
        }

        FLocation loc = new FLocation(location);
        Faction otherFaction = Board.getInstance().getFactionAt(loc);

        if (FactionsPlugin.getInstance().getLandRaidControl().isRaidable(otherFaction)) {
            return true;
        }

        if (checkDenyList) {
            if (otherFaction.hasPlayersOnline()) {
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
                me.msg(TL.PLAYER_USE_TERRITORY, TextUtil.getMaterialName(material), otherFaction.getTag(me.getFaction()));
            }
            return false;
        }

        // Also cancel if player doesn't have ownership rights for this claim
        if (facConf.ownedArea().isEnabled() && facConf.ownedArea().isDenyUsage() && !otherFaction.playerHasOwnershipRights(me, loc)) {
            if (!justCheck) {
                me.msg(TL.PLAYER_USE_OWNED, TextUtil.getMaterialName(material), otherFaction.getOwnerListString(loc));
            }

            return false;
        }

        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!plugin.worldUtil().isEnabled(event.getPlayer().getWorld())) {
            return;
        }

        FPlayer me = FPlayers.getInstance().getByPlayer(event.getPlayer());

        FactionsPlugin.getInstance().getLandRaidControl().onRespawn(me);

        Location home = me.getFaction().getHome();
        MainConfig.Factions facConf = FactionsPlugin.getInstance().conf().factions();
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
        }.runTask(FactionsPlugin.getInstance());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        FPlayer me = FPlayers.getInstance().getByPlayer(event.getPlayer());
        boolean isEnabled = plugin.worldUtil().isEnabled(event.getPlayer().getWorld());
        if (!isEnabled) {
            FScoreboard.remove(me, event.getPlayer());
            if (me.isFlying()) {
                me.setFlying(false);
            }
            return;
        }
        FLocation to = new FLocation(event.getPlayer().getLocation());
        me.setLastStoodAt(to);
        me.flightCheck();
        if (!event.getFrom().equals(event.getPlayer().getWorld()) && !plugin.worldUtil().isEnabled(event.getFrom())) {
            FactionsPlugin.getInstance().getLandRaidControl().update(me);
            this.initFactionWorld(me);
        }
    }

    // For some reason onPlayerInteract() sometimes misses bucket events depending on distance (something like 2-3 blocks away isn't detected),
    // but these separate bucket events below always fire without fail
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!plugin.worldUtil().isEnabled(event.getPlayer().getWorld())) {
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
        if (!plugin.worldUtil().isEnabled(event.getPlayer().getWorld())) {
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

        FPlayer me = FPlayers.getInstance().getByPlayer(player);
        if (me.isAdminBypassing()) {
            return;
        }

        FLocation location = new FLocation(event.getLectern().getLocation());
        Faction otherFaction = Board.getInstance().getFactionAt(location);
        if (this.plugin.getLandRaidControl().isRaidable(otherFaction)) {
            return;
        }

        PermissibleAction action = PermissibleActions.CONTAINER;
        if (!otherFaction.hasAccess(me, action, location)) {
            me.msg(TL.GENERIC_NOPERMISSION, action.getShortDescription());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerArmorStandManipulateEvent event) {
        if (!plugin.worldUtil().isEnabled(event.getPlayer().getWorld())) {
            return;
        }

        if (!canPlayerUseBlock(event.getPlayer(), Material.ARMOR_STAND, event.getRightClicked().getLocation(), false)) {
            event.setCancelled(true);
        }
    }

    public static boolean preventCommand(String fullCmd, Player player) {
        MainConfig.Factions.Protection protection = FactionsPlugin.getInstance().conf().factions().protection();
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

        FPlayer me = FPlayers.getInstance().getByPlayer(player);

        String shortCmd;  // command without the slash at the beginning
        if (fullCmd.startsWith("/")) {
            shortCmd = fullCmd.substring(1);
        } else {
            shortCmd = fullCmd;
            fullCmd = "/" + fullCmd;
        }

        if (me.hasFaction() &&
                !me.isAdminBypassing() &&
                !protection.getPermanentFactionMemberDenyCommands().isEmpty() &&
                me.getFaction().isPermanent() &&
                isCommandInSet(fullCmd, shortCmd, protection.getPermanentFactionMemberDenyCommands())) {
            me.msg(TL.PLAYER_COMMAND_PERMANENT, fullCmd);
            return true;
        }

        Faction at = Board.getInstance().getFactionAt(new FLocation(player.getLocation()));
        if (at.isWilderness() && !protection.getWildernessDenyCommands().isEmpty() && !me.isAdminBypassing() && isCommandInSet(fullCmd, shortCmd, protection.getWildernessDenyCommands())) {
            me.msg(TL.PLAYER_COMMAND_WILDERNESS, fullCmd);
            return true;
        }

        Relation rel = at.getRelationTo(me);
        if (at.isNormal() && rel.isAlly() && !protection.getTerritoryAllyDenyCommands().isEmpty() && !me.isAdminBypassing() && isCommandInSet(fullCmd, shortCmd, protection.getTerritoryAllyDenyCommands())) {
            me.msg(TL.PLAYER_COMMAND_ALLY, fullCmd);
            return true;
        }

        if (at.isNormal() && rel.isTruce() && !protection.getTerritoryTruceDenyCommands().isEmpty() && !me.isAdminBypassing() && isCommandInSet(fullCmd, shortCmd, protection.getTerritoryTruceDenyCommands())) {
            me.msg(TL.PLAYER_COMMAND_TRUCE, fullCmd);
            return true;
        }

        if (at.isNormal() && rel.isNeutral() && !protection.getTerritoryNeutralDenyCommands().isEmpty() && !me.isAdminBypassing() && isCommandInSet(fullCmd, shortCmd, protection.getTerritoryNeutralDenyCommands())) {
            me.msg(TL.PLAYER_COMMAND_NEUTRAL, fullCmd);
            return true;
        }

        if (at.isNormal() && rel.isEnemy() && !protection.getTerritoryEnemyDenyCommands().isEmpty() && !me.isAdminBypassing() && isCommandInSet(fullCmd, shortCmd, protection.getTerritoryEnemyDenyCommands())) {
            me.msg(TL.PLAYER_COMMAND_ENEMY, fullCmd);
            return true;
        }

        if (at.isWarZone() && !protection.getWarzoneDenyCommands().isEmpty() && !me.isAdminBypassing() && isCommandInSet(fullCmd, shortCmd, protection.getWarzoneDenyCommands())) {
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
        if (!plugin.worldUtil().isEnabled(event.getWhoClicked().getWorld())) {
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
        if (!plugin.worldUtil().isEnabled(event.getWhoClicked().getWorld())) {
            return;
        }

        if (event.getInventory().getHolder() instanceof GUI) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        FPlayer badGuy = FPlayers.getInstance().getByPlayer(event.getPlayer());
        if (badGuy == null) {
            return;
        }

        // if player was banned (not just kicked), get rid of their stored info
        if (FactionsPlugin.getInstance().conf().factions().other().isRemovePlayerDataWhenBanned() && event.getReason().equals("Banned by admin.")) {
            if (badGuy.getRole() == Role.ADMIN) {
                badGuy.getFaction().promoteNewLeader();
            }

            badGuy.leave(false);
            badGuy.remove();
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
        if (!plugin.worldUtil().isEnabled(event.getPlayer().getWorld())) {
            return;
        }

        String cmd = event.getMessage().split(" ")[0];

        if (FactionsPlugin.getInstance().conf().factions().chat().isTriggerPublicChat(cmd.startsWith("/") ? cmd.substring(1) : cmd)) {
            FPlayer p = FPlayers.getInstance().getByPlayer(event.getPlayer());
            p.setChatMode(ChatMode.PUBLIC);
            p.msg(TL.COMMAND_CHAT_MODE_PUBLIC);
        }

        if (FactionsPlayerListener.preventCommand(event.getMessage(), event.getPlayer())) {
            if (plugin.logPlayerCommands()) {
                plugin.getLogger().info("[PLAYER_COMMAND] " + event.getPlayer().getName() + ": " + event.getMessage());
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreLogin(PlayerLoginEvent event) {
        FPlayers.getInstance().getByPlayer(event.getPlayer());
    }
}
