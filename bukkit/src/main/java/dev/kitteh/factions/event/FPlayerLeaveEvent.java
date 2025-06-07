package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import org.bukkit.event.Cancellable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class FPlayerLeaveEvent extends FactionPlayerEvent implements Cancellable {
    private final Reason reason;
    boolean cancelled = false;

    public enum Reason {
        ADMIN_KICKED(true),
        KICKED(true),
        DISBAND(false),
        LEAVE(true),
        BANNED(true),
        ;

        final boolean cancellable;

        Reason(boolean cancellable) {
            this.cancellable = cancellable;
        }
    }

    public FPlayerLeaveEvent(FPlayer p, Faction f, Reason r) {
        super(f, p);
        reason = r;
    }

    public boolean isCancellable() {
        return this.reason.cancellable;
    }

    /**
     * Get the reason the player left the faction.
     *
     * @return reason player left the faction.
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
                throw new IllegalStateException("Cannot cancel leave reason '" + reason + "'");
            }
        } else {
            this.cancelled = false;
        }
    }
}
