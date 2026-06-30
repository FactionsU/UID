package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public class FactionNewAdminEvent extends FactionPlayerEvent {
    public FactionNewAdminEvent(FPlayer p, Faction f) {
        super(f, p);
    }
}
