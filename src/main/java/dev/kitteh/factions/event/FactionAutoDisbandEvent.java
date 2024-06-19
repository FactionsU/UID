package dev.kitteh.factions.event;

import dev.kitteh.factions.Faction;
import org.jspecify.annotations.NullMarked;

/**
 * Event called when a faction is disbanded automatically.
 */
@NullMarked
public class FactionAutoDisbandEvent extends FactionEvent {
    public FactionAutoDisbandEvent(Faction faction) {
        super(faction);
    }
}
