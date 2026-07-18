package dev.kitteh.factions.policy;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.config.Confs;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * The action a faction takes against a member who cannot afford their daily
 * {@link Faction#dues() dues} when collection runs.
 */
@ApiStatus.AvailableSince("4.7.0")
@NullMarked
public enum DuesFailurePolicy {
    /**
     * Record the failure and take no further immediate action.
     */
    RECORD,
    /**
     * Move the member down one role, or leave them at the lowest role.
     */
    DEMOTE,
    /**
     * Carry the unpaid amount forward to owe more the next time.
     */
    DEBT,
    /**
     * Remove the member from the faction.
     */
    DISMISS;

    public static final DuesFailurePolicy DEFAULT = RECORD;

    public static @Nullable DuesFailurePolicy fromString(String check) {
        return switch (check.toLowerCase(Locale.ROOT)) {
            case "record" -> RECORD;
            case "demote" -> DEMOTE;
            case "debt" -> DEBT;
            case "dismiss" -> DISMISS;
            default -> null;
        };
    }

    public String translation() {
        var tl = Confs.tl().commands().set().dues().policies();
        return switch (this) {
            case RECORD -> tl.getRecord();
            case DEMOTE -> tl.getDemote();
            case DEBT -> tl.getDebt();
            case DISMISS -> tl.getDismiss();
        };
    }
}
