package com.massivecraft.factions.event;

import com.massivecraft.factions.FPlayer;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;

public class FactionSetHomeEvent extends FactionPlayerEvent implements Cancellable {
    private final Location location;
    private boolean cancelled;

    public FactionSetHomeEvent(FPlayer fPlayer, Location location) {
        super(fPlayer.getFaction(), fPlayer);
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
