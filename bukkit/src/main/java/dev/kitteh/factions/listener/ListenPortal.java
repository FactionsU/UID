package dev.kitteh.factions.listener;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

public class ListenPortal implements Listener {
    public final FactionsPlugin plugin;

    public ListenPortal(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        if (!WorldUtil.isEnabled(event.getWorld())) {
            return;
        }
        Entity entity = event.getEntity();

        if (!this.plugin.conf().factions().portals().isLimit()) {
            return;
        }

        if (!(entity instanceof Player player) || !WorldUtil.isEnabled(player)) {
            return;
        }

        FPlayer fPlayer = FPlayers.fPlayers().get(player);
        Relation minimumRelation = this.plugin.conf().factions().portals().getMinimumRelation();

        boolean match = event.getBlocks().stream().map(bs -> new FLocation(bs.getLocation()).faction())
                .distinct()
                .anyMatch(faction -> {
                    if (faction.isWilderness()) {
                        return false;
                    }
                    if (faction.isSafeZone() && Permission.MANAGE_SAFE_ZONE.has(player)) {
                        return false;
                    }
                    if (faction.isWarZone() && Permission.MANAGE_WAR_ZONE.has(player)) {
                        return false;
                    }
                    if (faction.isNormal() && fPlayer.faction().relationTo(faction).isAtLeast(minimumRelation)) {
                        return false;
                    }
                    return true;
                });
        if (match) {
            event.setCancelled(true);
            player.sendMessage(TL.PLAYER_PORTAL_NOTALLOWED.toString());
        }
    }
}
