package dev.kitteh.factions.tagresolver;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import net.kyori.adventure.text.minimessage.Context;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@ApiStatus.AvailableSince("4.0.0")
@ApiStatus.NonExtendable
@NullMarked
public abstract class ObservedResolver extends HelperResolver {
    private final @Nullable FPlayer forcedObserver;

    protected ObservedResolver(String name, @Nullable FPlayer forcedObserver) {
        super(name);
        this.forcedObserver = forcedObserver;
    }

    @Deprecated(forRemoval = true, since = "4.5.0")
    protected ObservedResolver(String name, @Nullable Player forcedObserver) {
        super(name);
        this.forcedObserver = forcedObserver == null ? null : FPlayers.fPlayers().get(forcedObserver);
    }

    protected ObservedResolver(String name) {
        super(name);
        this.forcedObserver = null;
    }

    @Override
    protected @Nullable FPlayer observer(Context context) {
        if (this.forcedObserver != null) {
            return this.forcedObserver;
        }
        return super.observer(context);
    }
}