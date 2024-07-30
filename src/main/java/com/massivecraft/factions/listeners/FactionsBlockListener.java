package com.massivecraft.factions.listeners;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.file.MainConfig;
import com.massivecraft.factions.perms.PermissibleAction;
import com.massivecraft.factions.perms.PermissibleActions;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Directional;

import java.util.List;

public class FactionsBlockListener extends AbstractListener {

    public final FactionsPlugin plugin;

    public FactionsBlockListener(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.worldUtil().isEnabled(event.getBlock().getWorld())) {
            return;
        }

        if (!event.canBuild()) {
            return;
        }

        // special case for flint&steel, which should only be prevented by DenyUsage list
        if (event.getBlockPlaced().getType() == Material.FIRE) {
            return;
        }

        Faction targetFaction = Board.getInstance().getFactionAt(new FLocation(event.getBlock().getLocation()));
        if (targetFaction.isNormal() && !targetFaction.isPeaceful() && FactionsPlugin.getInstance().conf().factions().specialCase().getIgnoreBuildMaterials().contains(event.getBlock().getType())) {
            return;
        }

        if (!playerCanBuildDestroyBlock(event.getPlayer(), event.getBlock().getLocation(), PermissibleActions.BUILD, false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        if (!plugin.worldUtil().isEnabled(event.getBlock().getWorld())) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        Material material = item.getType();
        FLocation start = new FLocation(event.getBlock());
        FLocation end = new FLocation(event.getBlock().getRelative(((Directional) event.getBlock().getState().getData()).getFacing())); // ((Dispenser) event.getBlock().getBlockData()).getFacing()
        if (start.equals(end)) {
            return;
        }

        Faction startFaction = Board.getInstance().getFactionAt(start);
        Faction endFaction = Board.getInstance().getFactionAt(end);
        if (startFaction == endFaction) {
            return;
        }
        if (FactionsPlugin.getInstance().getLandRaidControl().isRaidable(endFaction)) {
            return;
        }

        MainConfig.Factions.Protection protConf = FactionsPlugin.getInstance().conf().factions().protection();

        if (endFaction.hasPlayersOnline()) {
            if (!protConf.getTerritoryDenyUsageMaterials().contains(material)) {
                return; // Item isn't one we're preventing for online factions.
            }
        } else {
            if (!protConf.getTerritoryDenyUsageMaterialsWhenOffline().contains(material)) {
                return; // Item isn't one we're preventing for offline factions.
            }
        }

        if (endFaction.isWilderness()) {
            if (!protConf.isWildernessDenyUsage() || protConf.getWorldsNoWildernessProtection().contains(event.getBlock().getLocation().getWorld().getName())) {
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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (!plugin.worldUtil().isEnabled(event.getBlock().getWorld())) {
            return;
        }

        boolean exp = FactionsPlugin.getInstance().conf().exploits().isLiquidFlow();
        boolean safe = FactionsPlugin.getInstance().conf().factions().protection().isSafeZonePreventLiquidFlowIn();
        boolean war = FactionsPlugin.getInstance().conf().factions().protection().isWarZonePreventLiquidFlowIn();

        if ((exp || safe || war) && event.getBlock().isLiquid() && event.getToBlock().isEmpty()) {
            Faction from = Board.getInstance().getFactionAt(new FLocation(event.getBlock()));
            Faction to = Board.getInstance().getFactionAt(new FLocation(event.getToBlock()));
            if (from == to) {
                // not concerned with inter-faction events
                return;
            }
            // from faction != to faction
            if (exp && to.isNormal()) {
                if (from.isNormal() && from.getRelationTo(to).isAlly()) {
                    return;
                }
                event.setCancelled(true);
            }
            if ((safe && to.isSafeZone() || (war && to.isWarZone()))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        if (!plugin.worldUtil().isEnabled(event.getBlock().getWorld())) {
            return;
        }

        if (event.getBlock().getType() == Material.ICE &&
                FactionsPlugin.getInstance().conf().factions().protection().isTerritoryDenyIceFormation() &&
                Board.getInstance().getFactionAt(new FLocation(event.getBlock())).isNormal()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.worldUtil().isEnabled(event.getBlock().getWorld())) {
            return;
        }

        if (FactionsPlugin.getInstance().conf().factions().protection().getBreakExceptions().contains(event.getBlock().getType()) &&
                Board.getInstance().getFactionAt(new FLocation(event.getBlock().getLocation())).isNormal()) {
            return;
        }

        if (!playerCanBuildDestroyBlock(event.getPlayer(), event.getBlock().getLocation(), PermissibleActions.DESTROY, false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        if (!plugin.worldUtil().isEnabled(event.getBlock().getWorld())) {
            return;
        }

        if (FactionsPlugin.getInstance().conf().factions().protection().getBreakExceptions().contains(event.getBlock().getType()) &&
                Board.getInstance().getFactionAt(new FLocation(event.getBlock().getLocation())).isNormal()) {
            return;
        }

        if (event.getInstaBreak() && !playerCanBuildDestroyBlock(event.getPlayer(), event.getBlock().getLocation(), PermissibleActions.DESTROY, false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        this.handleExplosion(event.getBlock().getLocation(), null, event, event.blockList());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (!plugin.worldUtil().isEnabled(event.getBlock().getWorld())) {
            return;
        }

        if (!FactionsPlugin.getInstance().conf().factions().protection().isPistonProtectionThroughDenyBuild()) {
            return;
        }

        // if the pushed blocks list is empty, no worries
        if (event.getBlocks().isEmpty()) {
            return;
        }

        Faction pistonFaction = Board.getInstance().getFactionAt(new FLocation(event.getBlock()));

        if (!canPistonMoveBlock(pistonFaction, event.getBlocks(), event.getDirection())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (!plugin.worldUtil().isEnabled(event.getBlock().getWorld())) {
            return;
        }

        // if not a sticky piston, retraction should be fine
        if (!event.isSticky() || !FactionsPlugin.getInstance().conf().factions().protection().isPistonProtectionThroughDenyBuild()) {
            return;
        }

        List<Block> blocks = event.getBlocks();

        // if the retracted blocks list is empty, no worries
        if (blocks.isEmpty()) {
            return;
        }

        Faction pistonFaction = Board.getInstance().getFactionAt(new FLocation(event.getBlock()));

        if (!canPistonMoveBlock(pistonFaction, blocks, null)) {
            event.setCancelled(true);
        }
    }

    private boolean canPistonMoveBlock(Faction pistonFaction, List<Block> blocks, BlockFace direction) {
        String world = blocks.getFirst().getWorld().getName();
        List<FLocation> locations = (direction == null ? blocks.stream() : blocks.stream().map(b -> b.getRelative(direction)))
                .map(Block::getLocation)
                .map(FLocation::new)
                .distinct()
                .toList();

        boolean disableOverall = FactionsPlugin.getInstance().conf().factions().other().isDisablePistonsInTerritory();
        for (FLocation location : locations) {
            Faction otherFaction = Board.getInstance().getFactionAt(location);
            if (pistonFaction == otherFaction) {
                continue;
            }
            // Check if the piston is moving in a faction's territory. This disables pistons entirely in faction territory.
            if (disableOverall && otherFaction.isNormal()) {
                return false;
            }
            if (otherFaction.isWilderness() && FactionsPlugin.getInstance().conf().factions().protection().isWildernessDenyBuild() && !FactionsPlugin.getInstance().conf().factions().protection().getWorldsNoWildernessProtection().contains(world)) {
                return false;
            } else if (otherFaction.isSafeZone() && FactionsPlugin.getInstance().conf().factions().protection().isSafeZoneDenyBuild()) {
                return false;
            } else if (otherFaction.isWarZone() && FactionsPlugin.getInstance().conf().factions().protection().isWarZoneDenyBuild()) {
                return false;
            }
            Relation rel = pistonFaction.getRelationTo(otherFaction);
            if (!otherFaction.hasAccess(rel, PermissibleActions.BUILD, location)) {
                return false;
            }
        }
        return true;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFrostWalker(EntityBlockFormEvent event) {
        if (!plugin.worldUtil().isEnabled(event.getBlock().getWorld())) {
            return;
        }

        if (event.getEntity() == null || event.getEntity().getType() != EntityType.PLAYER || event.getBlock() == null) {
            return;
        }

        Player player = (Player) event.getEntity();
        Location location = event.getBlock().getLocation();

        // only notify every 10 seconds
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        boolean justCheck = fPlayer.getLastFrostwalkerMessage() + 10000 > System.currentTimeMillis();
        if (!justCheck) {
            fPlayer.setLastFrostwalkerMessage();
        }

        // Check if they have build permissions here. If not, block this from happening.
        if (!playerCanBuildDestroyBlock(player, location, PermissibleActions.FROSTWALK, justCheck)) {
            event.setCancelled(true);
        }
    }

    public static boolean playerCanBuildDestroyBlock(Player player, Location location, PermissibleAction permissibleAction, boolean justCheck) {
        String name = player.getName();
        MainConfig conf = FactionsPlugin.getInstance().conf();
        if (conf.factions().protection().getPlayersWhoBypassAllProtection().contains(name)) {
            return true;
        }

        FPlayer me = FPlayers.getInstance().getById(player.getUniqueId().toString());
        if (me.isAdminBypassing()) {
            return true;
        }

        FLocation loc = new FLocation(location);
        Faction otherFaction = Board.getInstance().getFactionAt(loc);

        if (otherFaction.isWilderness()) {
            if (conf.worldGuard().isBuildPriority() && FactionsPlugin.getInstance().getWorldguard() != null && FactionsPlugin.getInstance().getWorldguard().playerCanBuild(player, location)) {
                return true;
            }

            if (!conf.factions().protection().isWildernessDenyBuild() || conf.factions().protection().getWorldsNoWildernessProtection().contains(location.getWorld().getName())) {
                return true; // This is not faction territory. Use whatever you like here.
            }

            if (!justCheck) {
                me.msg(TL.PERM_DENIED_WILDERNESS, permissibleAction.getShortDescription());
            }

            return false;
        } else if (otherFaction.isSafeZone()) {
            if (conf.worldGuard().isBuildPriority() && FactionsPlugin.getInstance().getWorldguard() != null && FactionsPlugin.getInstance().getWorldguard().playerCanBuild(player, location)) {
                return true;
            }

            if (!conf.factions().protection().isSafeZoneDenyBuild() || Permission.MANAGE_SAFE_ZONE.has(player)) {
                return true;
            }

            if (!justCheck) {
                me.msg(TL.PERM_DENIED_SAFEZONE, permissibleAction.getShortDescription());
            }

            return false;
        } else if (otherFaction.isWarZone()) {
            if (conf.worldGuard().isBuildPriority() && FactionsPlugin.getInstance().getWorldguard() != null && FactionsPlugin.getInstance().getWorldguard().playerCanBuild(player, location)) {
                return true;
            }

            if (!conf.factions().protection().isWarZoneDenyBuild() || Permission.MANAGE_WAR_ZONE.has(player)) {
                return true;
            }

            if (!justCheck) {
                me.msg(TL.PERM_DENIED_WARZONE, permissibleAction.getShortDescription());
            }

            return false;
        }
        if (FactionsPlugin.getInstance().getLandRaidControl().isRaidable(otherFaction)) {
            return true;
        }

        Faction myFaction = me.getFaction();
        boolean pain = !justCheck && otherFaction.hasAccess(me, PermissibleActions.PAINBUILD, loc);

        // If the faction hasn't: defined access or denied, fallback to config values
        if (!otherFaction.hasAccess(me, permissibleAction, loc)) {
            if (pain && permissibleAction != PermissibleActions.FROSTWALK) {
                player.damage(conf.factions().other().getActionDeniedPainAmount());
                me.msg(TL.PERM_DENIED_PAINTERRITORY, permissibleAction.getShortDescription(), otherFaction.getTag(myFaction));
                return true;
            } else if (!justCheck) {
                me.msg(TL.PERM_DENIED_TERRITORY, permissibleAction.getShortDescription(), otherFaction.getTag(myFaction));
            }
            return false;
        }

        // Also cancel and/or cause pain if player doesn't have ownership rights for this claim
        if (conf.factions().ownedArea().isEnabled() && (conf.factions().ownedArea().isDenyBuild() || conf.factions().ownedArea().isPainBuild()) && !otherFaction.playerHasOwnershipRights(me, loc)) {
            if (pain && conf.factions().ownedArea().isPainBuild()) {
                player.damage(conf.factions().other().getActionDeniedPainAmount());

                if (!conf.factions().ownedArea().isDenyBuild()) {
                    me.msg(TL.PERM_DENIED_PAINOWNED, permissibleAction.getShortDescription(), otherFaction.getOwnerListString(loc));
                }
            }
            if (conf.factions().ownedArea().isDenyBuild()) {
                if (!justCheck) {
                    me.msg(TL.PERM_DENIED_OWNED, permissibleAction.getShortDescription(), otherFaction.getOwnerListString(loc));
                }

                return false;
            }
        }

        return true;
    }
}
