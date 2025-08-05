package dev.kitteh.factions.listener;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;

public class ListenEnderPearl implements Listener {
    public final FactionsPlugin plugin;

    public ListenEnderPearl(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void enderPearlTeleport(PlayerTeleportEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer())) {
            return;
        }

        if (!this.plugin.conf().exploits().isEnderPearlClipping()) {
            return;
        }
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }

        // this exploit works when the target location is within 0.31 blocks or so of a door or glass block or similar...
        Location target = Objects.requireNonNull(event.getTo()); // This does not spark joy
        Location from = event.getFrom();

        // blocks who occupy less than 1 block width or length wise need to be handled differently
        Material mat = target.getBlock().getType();
        if (((mat.name().endsWith("GLASS_PANE") || mat == Material.IRON_BARS) && clippingThrough(target, from, 0.65)) || ((mat.name().contains("FENCE")) && clippingThrough(target, from, 0.45))) {
            event.setTo(from);
            return;
        }

        // simple fix otherwise: ender pearl target locations are standardized to be in the center (X/Z) of the target block, not at the edges
        target.setX(target.getBlockX() + 0.5);
        target.setZ(target.getBlockZ() + 0.5);
        event.setTo(target);
    }

    private boolean clippingThrough(Location target, Location from, double thickness) {
        return ((from.getX() > target.getX() && (from.getX() - target.getX() < thickness)) || (target.getX() > from.getX() && (target.getX() - from.getX() < thickness)) || (from.getZ() > target.getZ() && (from.getZ() - target.getZ() < thickness)) || (target.getZ() > from.getZ() && (target.getZ() - from.getZ() < thickness)));
    }
}
