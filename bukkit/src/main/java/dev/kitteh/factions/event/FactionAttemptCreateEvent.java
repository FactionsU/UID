package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NullMarked;

/**
 * Event called when a Faction is soon to be created.
 */
@NullMarked
public class FactionAttemptCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final String factionTag;
    private final FPlayer sender;
    private boolean cancelled;

    public FactionAttemptCreateEvent(FPlayer sender, String tag) {
        this.factionTag = tag;
        this.sender = sender;
    }

    public FPlayer getFPlayer() {
        return this.sender;
    }

    public String getFactionTag() {
        return this.factionTag;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
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
