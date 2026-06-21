package dev.kitteh.factions.listener;

import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.upgrade.Upgrades;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ListenPaperBeacon implements Listener {
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBeaconEffect(BeaconEffectEvent event) {
        if (!WorldUtil.isEnabled(event.getBlock())) {
            return;
        }

        FLocation location = new FLocation(event.getBlock());
        Faction faction = location.faction();
        if (!faction.isNormal() || faction.upgradeLevel(Upgrades.BEACON_EFFECT_CONTROL) < 1) {
            return;
        }

        if (!faction.hasAccess(FPlayers.fPlayers().get(event.getPlayer()), PermissibleActions.BEACON, location)) {
            event.setCancelled(true);
        }
    }
}
