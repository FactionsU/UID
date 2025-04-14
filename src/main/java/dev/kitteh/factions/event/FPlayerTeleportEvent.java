package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@NullMarked
public class FPlayerTeleportEvent extends FactionPlayerEvent implements Cancellable {
    public enum Reason {
        HOME, AHOME, WARP, STUCK
    }

    private final Reason reason;
    private boolean cancelled = false;
    private final @Nullable Location location;

    public FPlayerTeleportEvent(FPlayer p, @Nullable Location location, Reason r) {
        super(p.getFaction(), p);
        reason = Objects.requireNonNull(r);
        this.location = location;
    }

    /**
     * Get the reason the player is teleporting.
     *
     * @return reason player is teleporting.
     */
    public Reason getReason() {
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
