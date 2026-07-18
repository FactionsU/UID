package dev.kitteh.factions.policy;

import dev.kitteh.factions.Faction;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * The action taken when a faction's bank cannot afford its daily
 * {@link Faction#rentDebt() rent}.
 */
@ApiStatus.AvailableSince("4.7.0")
@NullMarked
public enum RentFailurePolicy {
    /**
     * Unclaim chunks, starting with the one players have spent the least time in,
     * until the faction can afford the (recalculated) rent.
     */
    UNCLAIM_UNTIL_AFFORD,
    /**
     * Unclaim all the faction's land.
     */
    UNCLAIM_ALL,
    /**
     * Disband the faction.
     */
    DISBAND,
    /**
     * Carry the unpaid rent forward as debt, collected in addition next time.
     */
    DEBT;
}
