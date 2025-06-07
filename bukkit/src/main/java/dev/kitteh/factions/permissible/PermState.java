package dev.kitteh.factions.permissible;

/**
 * A tri-state for permissible action status in a selector's permissions.
 */
public enum PermState {
    ALLOW,
    DENY,
    UNSET;

    public static PermState of(Boolean bool) {
        return bool == null ? UNSET : bool ? ALLOW : DENY;
    }
}
