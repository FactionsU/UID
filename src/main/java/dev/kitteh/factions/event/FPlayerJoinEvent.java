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
        CREATE, LEADER, COMMAND
    }

    public FPlayerJoinEvent(FPlayer fp, Faction f, PlayerJoinReason r) {
        super(f, fp);
        reason = r;
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
        cancelled = c;
    }
}
