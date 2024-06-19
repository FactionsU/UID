package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import org.bukkit.event.Cancellable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class LandUnclaimAllEvent extends FactionPlayerEvent implements Cancellable {
    private boolean cancelled;

    public LandUnclaimAllEvent(Faction f, FPlayer p) {
        super(f, p);
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
