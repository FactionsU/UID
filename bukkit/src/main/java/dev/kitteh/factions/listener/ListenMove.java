package dev.kitteh.factions.listener;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.plugin.Instances;
import dev.kitteh.factions.util.ComponentDispatcher;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.WarmUpUtil;
import dev.kitteh.factions.util.WorldUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.UUID;

public class ListenMove implements Listener {
    private final FactionsPlugin plugin;
    // Holds the next time a player can have a map shown.
    private final HashMap<UUID, Long> mapLastShown = new HashMap<>();

    public ListenMove(FactionsPlugin plugin) {
        this.plugin = plugin;
    }


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
            if (me.warmup() instanceof WarmUpUtil.Warmup warmup && warmup != WarmUpUtil.Warmup.STUCK) {
                me.cancelWarmup();
                me.msgLegacy(TL.WARMUPS_NOTIFY_CANCELLED);
            }
        }

        // quick check to make sure player is moving between chunks; good performance boost
        if (fromLoc.getBlockX() >> 4 == toLoc.getBlockX() >> 4 && fromLoc.getBlockZ() >> 4 == toLoc.getBlockZ() >> 4 && fromLoc.getWorld() == toLoc.getWorld()) {
            return;
        }

        if (!WorldUtil.isEnabled(toLoc)) {
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

        if (me.autoClaim() instanceof Faction faction) {
            me.attemptClaim(faction, to, true);
        } else if (me.autoUnclaim() instanceof Faction faction) {
            me.attemptUnclaim(faction, to, true);
        }

        // Did we change "host"(faction)?
        Faction factionFrom = from.faction();
        Faction factionTo = to.faction();
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

        dance:
        if (me.mapAutoUpdating()) {
            if (!mapLastShown.containsKey(player.getUniqueId()) || (mapLastShown.get(player.getUniqueId()) < System.currentTimeMillis())) {
                if (!Permission.MAP_AUTO.has(player)) {
                    me.mapAutoUpdating(false);
                    break dance;
                }
                for (Component component : Instances.BOARD.getMap(me, to, player.getLocation().getYaw())) {
                    me.sendMessage(component);
                }
                mapLastShown.put(player.getUniqueId(), System.currentTimeMillis() + this.plugin.conf().commands().map().getCooldown());
            }
        }
        if (changedFaction) {
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
}
