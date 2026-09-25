package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public class FactionRenameEvent extends FactionPlayerEvent implements Cancellable {
    private boolean cancelled = false;
    private final String tag;

    public FactionRenameEvent(FPlayer sender, Faction faction, String newTag) {
        super(faction, sender);
        this.tag = newTag;
    }

    /// @see #FactionRenameEvent(FPlayer, Faction, String)
    @Deprecated(forRemoval = true, since = "4.7.2")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    public FactionRenameEvent(FPlayer sender, String newTag) {
        this(sender, sender.faction(), newTag);
    }

    /// Get the new faction tag.
    ///
    /// @return new faction tag as String.
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
