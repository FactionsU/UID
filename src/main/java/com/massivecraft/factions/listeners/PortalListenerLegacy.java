package com.massivecraft.factions.listeners;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Relation;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class PortalListenerLegacy implements Listener {
    @EventHandler
    public void onTravel(PlayerPortalEvent event) {
        TravelAgent agent = event.getPortalTravelAgent();

        // If they aren't able to find a portal, it'll try to create one.
        if (event.useTravelAgent() && agent.getCanCreatePortal() && agent.findPortal(event.getTo()) == null) {
            if (this.shouldCancel(event.getTo(), event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }

    public boolean shouldCancel(Location location, Player player) {
        if (!FactionsPlugin.getInstance().worldUtil().isEnabled(player.getWorld())) {
            return true;
        }

        if (!FactionsPlugin.getInstance().conf().factions().portals().isLimit()) {
            return false; // Don't do anything if they don't want us to.
        }
        FLocation loc = new FLocation(location);
        Faction faction = Board.getInstance().getFactionAt(loc);
        if (faction.isWilderness()) {
            return false; // We don't care about wilderness.
        } else if (!faction.isNormal() && !player.isOp()) {
            // Don't let non ops make portals in safezone or warzone.
            return true;
        }

        FPlayer fp = FPlayers.getInstance().getByPlayer(player);
        String mininumRelation = FactionsPlugin.getInstance().conf().factions().portals().getMinimumRelation(); // Defaults to Neutral if typed wrong.
        return !fp.getFaction().getRelationTo(faction).isAtLeast(Relation.fromString(mininumRelation));
    }
}
