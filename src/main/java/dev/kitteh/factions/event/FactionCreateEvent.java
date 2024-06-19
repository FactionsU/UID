package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NullMarked;

/**
 * Event called when a Faction is created.
 */
@NullMarked
public class FactionCreateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final FPlayer sender;
    private final Faction faction;

    public FactionCreateEvent(FPlayer sender, Faction faction) {
        this.sender = sender;
        this.faction = faction;
    }

    public FPlayer getFPlayer() {
        return this.sender;
    }

    public Faction getFaction() {
        return this.faction;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
