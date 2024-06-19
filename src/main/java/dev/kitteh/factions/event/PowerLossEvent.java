package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import org.bukkit.event.Cancellable;
import org.jspecify.annotations.NullMarked;

/**
 * Event called when a player loses power.
 */
@NullMarked
public class PowerLossEvent extends FactionPlayerEvent implements Cancellable {
    private boolean cancelled = false;
    private String message;

    public PowerLossEvent(Faction f, FPlayer p) {
        super(f, p);
    }

    /**
     * Get the power loss message.
     *
     * @return power loss message as String.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the power loss message.
     *
     * @param message message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean c) {
        this.cancelled = c;
    }
}
