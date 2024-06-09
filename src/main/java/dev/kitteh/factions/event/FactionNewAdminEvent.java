package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;

public class FactionNewAdminEvent extends FactionPlayerEvent {

    public FactionNewAdminEvent(FPlayer p, Faction f) {
        super(f, p);
    }

}
