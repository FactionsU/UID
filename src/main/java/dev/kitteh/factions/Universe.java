package dev.kitteh.factions;

import dev.kitteh.factions.plugin.Instances;
import dev.kitteh.factions.upgrade.Upgrade;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import org.jspecify.annotations.NullMarked;

import java.time.Duration;

/**
 * Information not tied to any given faction.
 */
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
     * Sets grace time remaining.
     *
     * @param graceRemaining remaining grace time or zero to deactivate
     */
    void graceRemaining(Duration graceRemaining);

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
}
