package dev.kitteh.factions;

import dev.kitteh.factions.plugin.Instances;
import dev.kitteh.factions.upgrade.Upgrade;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Information not tied to any given faction.
 */
@ApiStatus.AvailableSince("4.0.0")
@ApiStatus.NonExtendable
@NullMarked
public interface Universe {
    /**
     * Gets the universe.
     *
     * @return the universe
     */
    static Universe universe() {
        return Instances.UNIVERSE;
    }

    /**
     * Gets grace time remaining.
     *
     * @return remaining grace time or zero if not active
     */
    Duration graceRemaining();

    /**
     * Gets if grace is currently enabled
     *
     * @return true if grace enabled
     */
    @ApiStatus.AvailableSince("4.1.1")
    default boolean grace() {
        return !this.graceRemaining().equals(Duration.ZERO);
    }

    /**
     * Sets grace time remaining.
     *
     * @param graceRemaining remaining grace time or zero to deactivate
     */
    void graceRemaining(Duration graceRemaining);

    /**
     * Gets the last time used for the shield scheduling check.
     *
     * @return last time used for shield scheduling check
     */
    @ApiStatus.AvailableSince("4.6.0")
    LocalTime shieldScheduleLastTimeChecked();

    /**
     * Gets the last date on which daily faction dues were collected, used to
     * avoid collecting more than once per calendar day.
     *
     * @return the last date dues were collected, or null if never collected
     */
    @ApiStatus.AvailableSince("4.7.0")
    @Nullable LocalDate lastDuesCollectionDate();

    /**
     * Sets the last date on which daily faction dues were collected.
     *
     * @param date the last date dues were collected
     */
    @ApiStatus.AvailableSince("4.7.0")
    void lastDuesCollectionDate(LocalDate date);

    /**
     * Gets if a given upgrade is enabled.
     *
     * @param upgrade upgrade to check
     * @return true if enabled
     */
    boolean isUpgradeEnabled(Upgrade upgrade);

    /**
     * Gets the upgrade settings for an upgrade.
     *
     * @param upgrade upgrade
     * @return settings for the upgrade
     */
    UpgradeSettings upgradeSettings(Upgrade upgrade);

    /**
     * Sets whether a given upgrade is enabled.
     *
     * @param upgrade upgrade to modify
     * @param enabled true to enable, false to disable
     * @throws IllegalArgumentException if the upgrade is not registered
     */
    @ApiStatus.AvailableSince("4.6.0")
    void upgradeEnabled(Upgrade upgrade, boolean enabled);

    /**
     * Sets the upgrade settings for an upgrade.
     *
     * @param settings new settings
     * @throws IllegalArgumentException if the upgrade is not registered or the settings are flawed
     */
    @ApiStatus.AvailableSince("4.6.0")
    void upgradeSettings(UpgradeSettings settings);
}
