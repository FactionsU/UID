package dev.kitteh.factions;

import dev.kitteh.factions.plugin.Instances;
import dev.kitteh.factions.upgrade.Upgrade;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import org.jspecify.annotations.NullMarked;

import java.time.Duration;

@NullMarked
public interface Universe {
    static Universe getInstance() {
        return Instances.UNIVERSE;
    }

    /**
     * Gets grace time remaining.
     *
     * @return remaining grace time or zero if not active
     */
    Duration getGraceRemaining();

    /**
     * Sets grace time remaining.
     *
     * @param graceRemaining remaining grace time or zero to deactivate
     */
    void setGraceRemaining(Duration graceRemaining);

    boolean isUpgradeEnabled(Upgrade upgrade);

    UpgradeSettings getUpgradeSettings(Upgrade upgrade);
}
