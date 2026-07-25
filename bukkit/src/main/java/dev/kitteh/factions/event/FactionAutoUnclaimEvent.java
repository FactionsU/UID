package dev.kitteh.factions.event;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.Faction;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/// Event called when land is unclaimed from a faction automatically, with no player responsible.
///
/// Cancelling this leaves the land claimed. The faction is still liable for whatever prompted the
/// unclaim, so it may be billed or unclaimed again later.
@ApiStatus.AvailableSince("4.7.0")
@NullMarked
public class FactionAutoUnclaimEvent extends FactionEvent implements Cancellable {
    private boolean cancelled;
    private final FLocation location;

    public FactionAutoUnclaimEvent(FLocation location, Faction faction) {
        super(faction);
        this.location = location;
    }

    /// Gets the land being unclaimed.
    ///
    /// @return the location being unclaimed
    public FLocation getLocation() {
        return this.location;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
