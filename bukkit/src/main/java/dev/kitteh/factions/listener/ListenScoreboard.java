package dev.kitteh.factions.listener;

import dev.kitteh.factions.event.FPlayerJoinEvent;
import dev.kitteh.factions.event.FPlayerLeaveEvent;
import dev.kitteh.factions.scoreboard.FTeamWrapper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ListenScoreboard implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    final public void onFactionJoin(FPlayerJoinEvent event) {
        FTeamWrapper.applyUpdatesLater(event.getFaction());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFactionLeave(FPlayerLeaveEvent event) {
        FTeamWrapper.applyUpdatesLater(event.getFaction());
    }
}
