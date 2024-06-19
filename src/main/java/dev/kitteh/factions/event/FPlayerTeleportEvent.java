package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class FPlayerTeleportEvent extends FactionPlayerEvent implements Cancellable {
    private final PlayerTeleportReason reason;
    private boolean cancelled = false;
    private final @Nullable Location location;

    public enum PlayerTeleportReason {
        HOME, AHOME, WARP, STUCK
    }

    public FPlayerTeleportEvent(FPlayer p, @Nullable Location location, FPlayerTeleportEvent.PlayerTeleportReason r) {
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
    public @Nullable Location getDestination() {
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
