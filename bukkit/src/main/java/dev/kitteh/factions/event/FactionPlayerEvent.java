package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import org.jspecify.annotations.NullMarked;

/**
 * Represents an event involving a Faction and a FPlayer.
 */
@NullMarked
public class FactionPlayerEvent extends FactionEvent {
    private final FPlayer fPlayer;

    public FactionPlayerEvent(Faction faction, FPlayer fPlayer) {
        super(faction);
        this.fPlayer = fPlayer;
    }

    public FPlayer getFPlayer() {
        return this.fPlayer;
    }
}
