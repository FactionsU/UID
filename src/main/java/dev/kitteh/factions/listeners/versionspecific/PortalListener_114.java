package dev.kitteh.factions.listeners.versionspecific;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.perms.Relation;
import dev.kitteh.factions.util.TL;
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
public class PortalListener_114 implements Listener {
    public final FactionsPlugin plugin;

    public PortalListener_114(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        Entity entity = event.getEntity();

        if (!FactionsPlugin.getInstance().conf().factions().portals().isLimit()) {
            return; // Don't do anything if they don't want us to.
        }

        if (!(entity instanceof Player player) || !plugin.worldUtil().isEnabled(event.getEntity().getWorld())) {
            return;
        }

        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        // Only 8 blocks so a loop should be fine.
        for (BlockState block : event.getBlocks()) {
            FLocation loc = new FLocation(block.getLocation());
            Faction faction = Board.getInstance().getFactionAt(loc);

            if (faction.isWilderness()) {
                continue; // We don't care about wilderness.
            } else if (!faction.isNormal() && !player.isOp()) {
                // Don't let non ops make portals in safezone or warzone.
                event.setCancelled(true);
                return;
            }

            String mininumRelation = FactionsPlugin.getInstance().conf().factions().portals().getMinimumRelation();

            // Don't let people portal into nether bases if server owners don't want that.
            if (!fPlayer.getFaction().getRelationTo(faction).isAtLeast(Relation.fromString(mininumRelation))) {
                event.setCancelled(true);
                player.sendMessage(TL.PLAYER_PORTAL_NOTALLOWED.toString());
                return;
            }
        }
    }
}
