package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class FactionRenameEvent extends FactionPlayerEvent implements Cancellable {

    private boolean cancelled = false;
    private final String tag;

    public FactionRenameEvent(FPlayer sender, String newTag) {
        super(sender.getFaction(), sender);
        tag = newTag;
    }

    /**
     * Get the new faction tag.
     *
     * @return new faction tag as String.
     */
    public String getFactionTag() {
        return tag;
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
