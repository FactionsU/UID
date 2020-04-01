package com.massivecraft.factions.event;

import com.massivecraft.factions.FPlayer;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;

public class FPlayerTeleportEvent extends FactionPlayerEvent implements Cancellable {

    private final PlayerTeleportReason reason;
    private boolean cancelled = false;
    private final Location location;

    public enum PlayerTeleportReason {
        HOME, AHOME, WARP, STUCK
    }

    public FPlayerTeleportEvent(FPlayer p, Location location, FPlayerTeleportEvent.PlayerTeleportReason r) {
        super(p.getFaction(), p);
        reason = r;
        this.location = location;
    }

    /**
     * Get the reason the player is teleporting.
     *
     * @return reason player is teleporting.
     */
    public FPlayerTeleportEvent.PlayerTeleportReason getReason() {
        return reason;
    }

    /**
     * Gets the destination, if known before starting to teleport.
     *
     * @return destination unless STUCK
     */
    public Location getDestination() {
        return location;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean c) {
        cancelled = c;
    }

}
