package dev.kitteh.factions.event;

import dev.kitteh.factions.Faction;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Event called when a faction is disbanded automatically.
 */
@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public class FactionAutoDisbandEvent extends FactionEvent {
    public FactionAutoDisbandEvent(Faction faction) {
        super(faction);
    }
}
