package dev.kitteh.factions.listener;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

/*
  Blocking all portal creation not in wilderness because we can't properly check if the creator has permission
  to create at the target destination.
 */
public class PortalListener implements Listener {
    public final FactionsPlugin plugin;

    public PortalListener(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        Entity entity = event.getEntity();

        if (!this.plugin.conf().factions().portals().isLimit()) {
            return; // Don't do anything if they don't want us to.
        }

        if (!(entity instanceof Player player) || !WorldUtil.isEnabled(player.getWorld())) {
            return;
        }

        FPlayer fPlayer = FPlayers.fPlayers().get(player);

        // Only 8 blocks so a loop should be fine.
        for (BlockState block : event.getBlocks()) {
            FLocation loc = new FLocation(block.getLocation());
            Faction faction = Board.board().factionAt(loc);

            if (faction.isWilderness()) {
                continue; // We don't care about wilderness.
            } else if (!faction.isNormal() && !player.isOp()) {
                // Don't let non ops make portals in safezone or warzone.
                event.setCancelled(true);
                return;
            }

            String minimumRelation = this.plugin.conf().factions().portals().getMinimumRelation();

            // Don't let people portal into nether bases if server owners don't want that.
            if (!fPlayer.faction().relationTo(faction).isAtLeast(Relation.fromString(minimumRelation))) {
                event.setCancelled(true);
                player.sendMessage(TL.PLAYER_PORTAL_NOTALLOWED.toString());
                return;
            }
        }
    }
}
