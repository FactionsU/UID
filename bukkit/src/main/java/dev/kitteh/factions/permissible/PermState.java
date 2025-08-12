package dev.kitteh.factions.permissible;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A tri-state for permissible action status in a selector's permissions.
 */
@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public enum PermState {
    ALLOW,
    DENY,
    UNSET;

    public static PermState of(@Nullable Boolean bool) {
        return bool == null ? UNSET : bool ? ALLOW : DENY;
    }
}
