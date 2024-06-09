package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Event called when a player loses dtr.
 */
public class DTRLossEvent extends FactionPlayerEvent implements Cancellable {
    private boolean cancelled = false;

    public DTRLossEvent(Faction f, FPlayer p) {
        super(f, p);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean c) {
        this.cancelled = c;
    }
}
