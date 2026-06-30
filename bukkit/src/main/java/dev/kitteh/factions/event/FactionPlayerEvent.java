package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Represents an event involving a Faction and a FPlayer.
 */
@ApiStatus.AvailableSince("4.0.0")
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
