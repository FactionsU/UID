package dev.kitteh.factions.event;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jspecify.annotations.NullMarked;

/**
 * Event called when an FPlayer claims land for a Faction.
 */
@NullMarked
public class LandClaimEvent extends FactionPlayerEvent implements Cancellable {
    private boolean cancelled;
    private final FLocation location;

    public LandClaimEvent(FLocation loc, Faction f, FPlayer p) {
        super(f, p);
        cancelled = false;
        location = loc;
    }

    /**
     * Get the FLocation involved in this event.
     *
     * @return the FLocation (also a chunk) involved in this event.
     */
    public FLocation getLocation() {
        return this.location;
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
