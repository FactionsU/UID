package dev.kitteh.factions.listener;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.List;

public class ListenPiston implements Listener {
    private final FactionsPlugin plugin;

    public ListenPiston(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (!WorldUtil.isEnabled(event.getBlock())) {
            return;
        }

        if (!this.plugin.conf().factions().protection().isPistonProtectionThroughDenyBuild()) {
            return;
        }

        // if the pushed blocks list is empty, no worries
        if (event.getBlocks().isEmpty()) { // This list is generated live, so we bail out of the earlier escapes first
            return;
        }

        if (denyPistonMoveBlocks(event.getBlock(), event.getBlocks(), event.getDirection())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        // if not a sticky piston, retraction should be fine
        if (!event.isSticky()) {
            return;
        }

        if (!WorldUtil.isEnabled(event.getBlock())) {
            return;
        }

        if (!this.plugin.conf().factions().protection().isPistonProtectionThroughDenyBuild()) {
            return;
        }

        List<Block> blocks = event.getBlocks();

        // if the retracted blocks list is empty, no worries
        if (blocks.isEmpty()) {
            return;
        }

        if (denyPistonMoveBlocks(event.getBlock(), blocks, null)) {
            event.setCancelled(true);
        }
    }

    private boolean denyPistonMoveBlocks(Block block, List<Block> blocks, BlockFace direction) {
        Faction pistonFaction = new FLocation(block).faction();

        String world = blocks.getFirst().getWorld().getName();
        List<FLocation> locations = (direction == null ? blocks.stream() : blocks.stream().map(b -> b.getRelative(direction)))
                .map(Block::getLocation)
                .map(FLocation::new)
                .distinct()
                .toList();

        boolean disableOverall = this.plugin.conf().factions().other().isDisablePistonsInTerritory();
        boolean denyWilderness = this.plugin.conf().factions().protection().isWildernessDenyBuild() && !this.plugin.conf().factions().protection().getWorldsNoWildernessProtection().contains(world);
        boolean denySafezone = this.plugin.conf().factions().protection().isSafeZoneDenyBuild();
        boolean denyWarzone = this.plugin.conf().factions().protection().isWarZoneDenyBuild();

        for (FLocation location : locations) {
            Faction otherFaction = location.faction();
            if (pistonFaction == otherFaction) {
                continue;
            }
            // Check if the piston is moving in a faction's territory. This disables pistons entirely in faction territory.
            if (disableOverall && otherFaction.isNormal()) {
                return true;
            }
            if (otherFaction.isWilderness() && denyWilderness) {
                return true;
            } else if (otherFaction.isSafeZone() && denySafezone) {
                return true;
            } else if (otherFaction.isWarZone() && denyWarzone) {
                return true;
            }
            Relation rel = pistonFaction.relationTo(otherFaction);
            if (!otherFaction.hasAccess(rel, PermissibleActions.BUILD, location)) {
                return true;
            }
        }
        return false;
    }
}
