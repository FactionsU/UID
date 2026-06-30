package dev.kitteh.factions.event;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Event called when an FPlayer unclaims land for a Faction.
 */
@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public class LandUnclaimEvent extends FactionPlayerEvent implements Cancellable {
    private boolean cancelled;
    private final FLocation location;

    public LandUnclaimEvent(FLocation loc, Faction f, FPlayer p) {
        super(f, p);
        cancelled = false;
        location = loc;
    }

    public FLocation getLocation() {
        return this.location;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean c) {
        cancelled = c;
    }
}
