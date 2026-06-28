package dev.kitteh.factions.upgrade;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

/**
 * A prerequisite that a faction must satisfy before another upgrade can be purchased.
 * The referenced upgrade must be enabled and the faction must hold it at or above {@link #minLevel()}.
 *
 * @param upgrade name of the required upgrade, as registered with {@link UpgradeRegistry}
 * @param minLevel minimum level of the required upgrade, at least 1
 */
@ApiStatus.AvailableSince("4.6.0")
@NullMarked
public record UpgradePrerequisite(String upgrade, int minLevel) {
    public UpgradePrerequisite {
        Objects.requireNonNull(upgrade, "Prerequisite upgrade is null");
        if (minLevel < 1) {
            throw new IllegalArgumentException("Prerequisite minLevel must be at least 1");
        }
    }

    public static UpgradePrerequisite of(Upgrade upgrade, int minLevel) {
        return new UpgradePrerequisite(upgrade.name(), minLevel);
    }
}
