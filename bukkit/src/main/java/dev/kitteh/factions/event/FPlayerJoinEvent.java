package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import org.bukkit.event.Cancellable;
import org.jspecify.annotations.NullMarked;

/**
 * Event called when an FPlayer joins a Faction.
 */
@NullMarked
public class FPlayerJoinEvent extends FactionPlayerEvent implements Cancellable {
    public enum Reason {
        CREATE(false),
        COMMAND(true),
        COMMAND_FORCE(true),
        ;

        final boolean cancellable;

        Reason(boolean cancellable) {
            this.cancellable = cancellable;
        }

        public boolean isCancellable() {
            return cancellable;
        }
    }

    private final Reason reason;
    private boolean cancelled = false;

    public FPlayerJoinEvent(FPlayer fp, Faction f, Reason r) {
        super(f, fp);
        reason = r;
    }

    public boolean isCancellable() {
        return this.reason.cancellable;
    }

    /**
     * Get the reason the player joined the faction.
     *
     * @return reason player joined the faction.
     */
    public Reason getReason() {
        return reason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean c) {
        if (c) {
            if (this.isCancellable()) {
                cancelled = true;
            } else {
                throw new IllegalStateException("Cannot cancel join reason '" + reason + "'");
            }
        } else {
            this.cancelled = false;
        }
    }
}
