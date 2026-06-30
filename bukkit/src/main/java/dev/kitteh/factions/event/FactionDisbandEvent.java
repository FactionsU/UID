package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Event called when a faction is disbanded.
 */
@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public class FactionDisbandEvent extends FactionEvent implements Cancellable {
    private boolean cancelled = false;
    private final @Nullable FPlayer sender;

    public FactionDisbandEvent(@Nullable FPlayer sender, Faction faction) {
        super(faction);
        this.sender = sender;
    }

    @Nullable
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
