package dev.kitteh.factions.listener;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.protection.Protection;
import dev.kitteh.factions.upgrade.Upgrades;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.material.Directional;

import java.util.Set;

public class ListenBlock implements Listener {
    private final FactionsPlugin plugin;

    public ListenBlock(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!WorldUtil.isEnabled(event.getBlock())) {
            return;
        }

        if (!event.canBuild()) {
            return;
        }

        // special case for flint&steel, which should only be prevented by DenyUsage list
        if (event.getBlockPlaced().getType() == Material.FIRE) {
            return;
        }

        Faction targetFaction = new FLocation(event.getBlock().getLocation()).faction();
        if (targetFaction.isNormal() && !targetFaction.isPeaceful() && this.plugin.conf().factions().specialCase().getIgnoreBuildMaterials().contains(event.getBlock().getType())) {
            return;
        }

        if (Protection.denyBuildOrDestroyBlock(event.getPlayer(), event.getBlock(), PermissibleActions.BUILD, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!WorldUtil.isEnabled(event.getBlock())) {
            return;
        }

        if (this.plugin.conf().factions().protection().getBreakExceptions().contains(event.getBlock().getType()) &&
                new FLocation(event.getBlock().getLocation()).faction().isNormal()) {
            return;
        }

        if (Protection.denyBuildOrDestroyBlock(event.getPlayer(), event.getBlock(), PermissibleActions.DESTROY, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        if (!WorldUtil.isEnabled(event.getBlock())) {
            return;
        }

        if (this.plugin.conf().factions().protection().getBreakExceptions().contains(event.getBlock().getType()) &&
                new FLocation(event.getBlock().getLocation()).faction().isNormal()) {
            return;
        }

        if (event.getInstaBreak() && Protection.denyBuildOrDestroyBlock(event.getPlayer(), event.getBlock(), PermissibleActions.DESTROY, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        if (!WorldUtil.isEnabled(event.getBlock())) {
            return;
        }

        int x = Math.floorMod(event.getBlock().getX(), 16);
        int z = Math.floorMod(event.getBlock().getZ(), 16);

        if (!(x == 0 || x == 15 || z == 0 || z == 15)) {
            return;
        }

        Material material = event.getItem().getType();
        FLocation start = new FLocation(event.getBlock());
        FLocation end = new FLocation(event.getBlock().getRelative(((Directional) event.getBlock().getState().getData()).getFacing())); // ((Dispenser) event.getBlock().getBlockData()).getFacing()
        if (start.equals(end)) {
            return;
        }

        Faction startFaction = start.faction();
        Faction endFaction = end.faction();
        if (startFaction == endFaction) {
            return;
        }
        if (this.plugin.landRaidControl().isRaidable(endFaction)) {
            return;
        }

        MainConfig.Factions.Protection protConf = this.plugin.conf().factions().protection();

        if (endFaction.hasMembersOnline()) {
            if (!protConf.getTerritoryDenyUsageMaterials().contains(material)) {
                return; // Item isn't one we're preventing for online factions.
            }
        } else {
            if (!protConf.getTerritoryDenyUsageMaterialsWhenOffline().contains(material)) {
                return; // Item isn't one we're preventing for offline factions.
            }
        }

        if (endFaction.isWilderness()) {
            if (!protConf.isWildernessDenyUsage() || protConf.getWorldsNoWildernessProtection().contains(event.getBlock().getWorld().getName())) {
                return; // This is not faction territory. Use whatever you like here.
            }
        } else if (endFaction.isSafeZone()) {
            if (!protConf.isSafeZoneDenyUsage()) {
                return;
            }
        } else if (endFaction.isWarZone()) {
            if (!protConf.isWarZoneDenyUsage()) {
                return;
            }
        } else if (endFaction.hasAccess(startFaction, PermissibleActions.ITEM, end)) {
            return;
        }
        event.setCancelled(true);
    }

    private static final Set<Material> waterBreakable = Set.of(Material.REDSTONE_WIRE, Material.REDSTONE_TORCH, Material.REDSTONE_WALL_TORCH, Material.COMPARATOR, Material.REPEATER);

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (!WorldUtil.isEnabled(event.getBlock())) {
            return;
        }

        boolean exp = this.plugin.conf().exploits().isLiquidFlow();
        boolean safe = this.plugin.conf().factions().protection().isSafeZonePreventLiquidFlowIn();
        boolean war = this.plugin.conf().factions().protection().isWarZonePreventLiquidFlowIn();

        if ((exp || safe || war) && event.getBlock().isLiquid() && event.getToBlock().isEmpty()) {
            Faction from = new FLocation(event.getBlock()).faction();
            Faction to = new FLocation(event.getToBlock()).faction();
            if (from == to) {
                // not concerned with inter-faction events
                return;
            }
            // from faction != to faction
            if (exp && to.isNormal()) {
                if (from.isNormal() && from.relationTo(to).isAlly()) {
                    return;
                }
                event.setCancelled(true);
            }
            if ((safe && to.isSafeZone() || (war && to.isWarZone()))) {
                event.setCancelled(true);
            }
        }

        if (event.getBlock().getType() == Material.WATER && waterBreakable.contains(event.getToBlock().getType()) && new FLocation(event.getToBlock()).faction().upgradeLevel(Upgrades.REDSTONE_PROTECT) > 0) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        if (!WorldUtil.isEnabled(event.getBlock())) {
            return;
        }

        if (event.getBlock().getType() == Material.ICE &&
                this.plugin.conf().factions().protection().isTerritoryDenyIceFormation() &&
                new FLocation(event.getBlock()).faction().isNormal()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFrostWalker(EntityBlockFormEvent event) {
        if (!WorldUtil.isEnabled(event.getBlock())) {
            return;
        }

        if (event.getEntity().getType() != EntityType.PLAYER) {
            return;
        }

        Player player = (Player) event.getEntity();

        // only notify every 10 seconds
        FPlayer fPlayer = FPlayers.fPlayers().get(player);
        boolean notify = fPlayer.lastFrostwalkerMessageTime() + 10000 < System.currentTimeMillis();
        if (notify) {
            fPlayer.updateLastFrostwalkerMessageTime();
        }

        // Check if they have build permissions here. If not, block this from happening.
        if (Protection.denyBuildOrDestroyBlock(player, event.getBlock(), PermissibleActions.FROSTWALK, notify)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPaintingBreak(HangingBreakEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity())) {
            return;
        }

        if (event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION || (event.getCause() == HangingBreakEvent.RemoveCause.ENTITY && event instanceof HangingBreakByEntityEvent && ((HangingBreakByEntityEvent) event).getRemover() instanceof Creeper)) {
            Location loc = event.getEntity().getLocation();
            Faction faction = new FLocation(loc).faction();
            if (faction.noExplosionsInTerritory()) {
                // faction is peaceful and has explosions set to disabled
                event.setCancelled(true);
                return;
            }

            boolean online = faction.hasMembersOnline();
            MainConfig.Factions.Protection protection = FactionsPlugin.instance().conf().factions().protection();

            if ((faction.isWilderness() && !protection.getWorldsNoWildernessProtection().contains(loc.getWorld().getName()) && (protection.isWildernessBlockCreepers() || protection.isWildernessBlockFireballs() || protection.isWildernessBlockTNT())) ||
                    (faction.isNormal() && (online ? (protection.isTerritoryBlockCreepers() || protection.isTerritoryBlockFireballs() || protection.isTerritoryBlockTNT()) : (protection.isTerritoryBlockCreepersWhenOffline() || protection.isTerritoryBlockFireballsWhenOffline() || protection.isTerritoryBlockTNTWhenOffline()))) ||
                    (faction.isWarZone() && (protection.isWarZoneBlockCreepers() || protection.isWarZoneBlockFireballs() || protection.isWarZoneBlockTNT())) ||
                    faction.isSafeZone()) {
                // explosion which needs prevention
                event.setCancelled(true);
                return;
            }
        }

        if (!(event instanceof HangingBreakByEntityEvent)) {
            return;
        }

        Entity breaker = ((HangingBreakByEntityEvent) event).getRemover();
        if (!(breaker instanceof Player)) {
            return;
        }

        if (Protection.denyBuildOrDestroyBlock((Player) breaker, event.getEntity().getLocation(), PermissibleActions.DESTROY, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPaintingPlace(HangingPlaceEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity())) {
            return;
        }

        if (Protection.denyBuildOrDestroyBlock(event.getPlayer(), event.getBlock().getRelative(event.getBlockFace()), PermissibleActions.BUILD, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity())) {
            return;
        }

        Entity entity = event.getEntity();

        Location loc = event.getBlock().getLocation();

        switch (entity) {
            case Enderman ignored -> {
                if (stopEndermanBlockManipulation(loc)) {
                    event.setCancelled(true);
                }
            }
            case Silverfish ignored -> {
                Faction faction = new FLocation(loc).faction();
                if (faction.isSafeZone() || faction.isWarZone() || faction.isPeaceful()) {
                    event.setCancelled(true);
                }
            }
            case Wither ignored -> {
                Faction faction = new FLocation(loc).faction();
                MainConfig.Factions.Protection protection = FactionsPlugin.instance().conf().factions().protection();
                // it's a bit crude just using fireball protection, but I'd rather not add in a whole new set of xxxBlockWitherExplosion or whatever
                if ((faction.isWilderness() && protection.isWildernessBlockFireballs() && !protection.getWorldsNoWildernessProtection().contains(loc.getWorld().getName())) ||
                        (faction.isNormal() && (faction.hasMembersOnline() ? protection.isTerritoryBlockFireballs() : protection.isTerritoryBlockFireballsWhenOffline())) ||
                        (faction.isWarZone() && protection.isWarZoneBlockFireballs()) ||
                        faction.isSafeZone()) {
                    event.setCancelled(true);
                }
            }
            default -> {
                // NOOP
            }
        }
    }

    private boolean stopEndermanBlockManipulation(Location loc) {
        if (loc == null) {
            return false;
        }
        // quick check to see if all Enderman deny options are enabled; if so, no need to check location
        MainConfig.Factions.Protection protection = FactionsPlugin.instance().conf().factions().protection();
        if (protection.isWildernessDenyEndermanBlocks() &&
                protection.isTerritoryDenyEndermanBlocks() &&
                protection.isTerritoryDenyEndermanBlocksWhenOffline() &&
                protection.isSafeZoneDenyEndermanBlocks() &&
                protection.isWarZoneDenyEndermanBlocks()) {
            return true;
        }

        Faction faction = new FLocation(loc).faction();

        if (faction.isWilderness()) {
            return protection.isWildernessDenyEndermanBlocks();
        } else if (faction.isNormal()) {
            return faction.hasMembersOnline() ? protection.isTerritoryDenyEndermanBlocks() : protection.isTerritoryDenyEndermanBlocksWhenOffline();
        } else if (faction.isSafeZone()) {
            return protection.isSafeZoneDenyEndermanBlocks();
        } else if (faction.isWarZone()) {
            return protection.isWarZoneDenyEndermanBlocks();
        }

        return false;
    }
}
