package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Event called when a Faction is created via command.
 */
@NullMarked
public class FactionCreateEvent extends FactionEvent {
    public enum Reason {
        COMMAND,
        PLUGIN
    }

    private @Nullable
    final FPlayer sender;
    private Reason reason;

    public FactionCreateEvent(@Nullable FPlayer sender, Faction faction, Reason reason) {
        super(faction);
        this.sender = sender;
        this.reason = reason;
    }

    public @Nullable FPlayer getFPlayer() {
        return this.sender;
    }

    public Reason getReason() {
        return this.reason;
    }
}
