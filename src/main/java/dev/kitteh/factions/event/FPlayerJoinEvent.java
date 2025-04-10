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
    private final PlayerJoinReason reason;
    private boolean cancelled = false;

    public enum PlayerJoinReason {
        CREATE(false),
        COMMAND(true),
        ;

        final boolean cancellable;

        PlayerJoinReason(boolean cancellable) {
            this.cancellable = cancellable;
        }

        public boolean isCancellable() {
            return cancellable;
        }
    }

    public FPlayerJoinEvent(FPlayer fp, Faction f, PlayerJoinReason r) {
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
    public PlayerJoinReason getReason() {
        return reason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean c) {
        if (this.isCancellable()) {
            cancelled = c;
        } else {
            throw new IllegalStateException("Cannot cancel join reason '" + reason + "'");
        }
    }
}
