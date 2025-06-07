package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import org.bukkit.event.Cancellable;
import org.jspecify.annotations.NullMarked;

/**
 * Event called when a faction is disbanded.
 */
@NullMarked
public class FactionDisbandEvent extends FactionEvent implements Cancellable {
    private boolean cancelled = false;
    private final FPlayer sender;

    public FactionDisbandEvent(FPlayer sender, Faction faction) {
        super(faction);
        this.sender = sender;
    }

    public FPlayer getFPlayer() {
        return this.sender;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
