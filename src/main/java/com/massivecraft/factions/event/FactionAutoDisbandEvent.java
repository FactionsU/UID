package com.massivecraft.factions.event;

import com.massivecraft.factions.Faction;

/**
 * Event called when a faction is disbanded automatically.
 */
public class FactionAutoDisbandEvent extends FactionEvent {
    public FactionAutoDisbandEvent(Faction faction) {
        super(faction);
    }
}
