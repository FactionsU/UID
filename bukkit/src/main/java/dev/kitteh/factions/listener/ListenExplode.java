package dev.kitteh.factions.listener;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.protection.Protection;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.Chunk;
import org.bukkit.ExplosionResult;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.WindCharge;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NullMarked
public class ListenExplode implements Listener {
    private final FactionsPlugin plugin;
    
    public ListenExplode(FactionsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        this.handleExplosion(event.getBlock().getLocation(), null, event, event.getExplosionResult(), event.blockList());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        this.handleExplosion(event.getLocation(), event.getEntity(), event, event.getExplosionResult(), event.blockList());
    }

    protected void handleExplosion(Location loc, @Nullable Entity boomer, Cancellable event, ExplosionResult result, List<Block> blockList) {
        if (!WorldUtil.isEnabled(loc)) {
            return;
        }

        if (Protection.denyExplode(boomer, new FLocation(loc))) {
            event.setCancelled(true);
            return;
        }

        // Block wind charges from triggering blocks (like doors, levers, buttons)
        if (result == ExplosionResult.TRIGGER_BLOCK && boomer instanceof WindCharge charge &&
                this.plugin.conf().factions().protection().isTerritoryBlockWindChargeInteractionMatchingPerms() &&
                charge.getShooter() instanceof Player shooter) {
            blockList.removeIf(block -> Protection.denyUseBlock(shooter, block.getType(), block.getLocation(), false));
        }

        // For cross-border damage
        List<Chunk> chunks = blockList.stream().map(Block::getChunk).distinct().collect(Collectors.toList());
        if (chunks.removeIf(chunk -> Protection.denyExplode(boomer, new FLocation(chunk)))) {
            blockList.removeIf(block -> !chunks.contains(block.getChunk()));
        }

        // Anti-Waterlog feature
        if ((boomer instanceof TNTPrimed || boomer instanceof ExplosiveMinecart) && this.plugin.conf().exploits().isTntWaterlog() && !Universe.universe().grace()) {
            // TNT in water/lava doesn't normally destroy any surrounding blocks, which is usually desired behavior, but...
            // this change below provides workaround for waterwalling providing perfect protection,
            // and makes cheap (non-obsidian) TNT cannons require minor maintenance between shots
            Block center = loc.getBlock();
            if (center.isLiquid()) {
                // a single surrounding block in all 6 directions is broken if the material is weak enough
                List<Block> targets = new ArrayList<>();
                targets.add(center.getRelative(0, 0, 1));
                targets.add(center.getRelative(0, 0, -1));
                targets.add(center.getRelative(0, 1, 0));
                targets.add(center.getRelative(0, -1, 0));
                targets.add(center.getRelative(1, 0, 0));
                targets.add(center.getRelative(-1, 0, 0));
                for (Block target : targets) {
                    Material type = target.getType();
                    if (type.isBlock() && type.getBlastResistance() >= 100F) {
                        continue;
                    }
                    if (!Protection.denyExplode(boomer, new FLocation(target.getLocation()))) {
                        target.breakNaturally();
                    }
                }
            }
        }
    }
}
