package dev.kitteh.factions.event;

import dev.kitteh.factions.Faction;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/// Event called when all of a faction's land is unclaimed automatically, with no player responsible.
///
/// Cancelling this leaves the land claimed. The faction is still liable for whatever prompted the
/// unclaim, so it may be billed or unclaimed again later.
@ApiStatus.AvailableSince("4.7.0")
@NullMarked
public class FactionAutoUnclaimAllEvent extends FactionEvent implements Cancellable {
    private boolean cancelled;

    public FactionAutoUnclaimAllEvent(Faction faction) {
        super(faction);
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
