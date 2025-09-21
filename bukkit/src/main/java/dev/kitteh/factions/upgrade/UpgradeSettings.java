package dev.kitteh.factions.upgrade;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Settings for an upgrade.
 */
@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public final class UpgradeSettings {
    private final Upgrade upgrade;
    private final Map<UpgradeVariable, LeveledValueProvider> variableSettings;
    private final int maxLevel;
    private final int startingLevel;
    private final LeveledValueProvider costSettings;

    public UpgradeSettings(Upgrade upgrade, Map<UpgradeVariable, LeveledValueProvider> variableSettings, int maxLevel, int startingLevel, LeveledValueProvider costSettings) {
        this.upgrade = upgrade;
        this.variableSettings = new HashMap<>(variableSettings);
        this.maxLevel = maxLevel;
        this.startingLevel = startingLevel;
        this.costSettings = costSettings;
        if (this.findFlaw() instanceof String issue) {
            throw new IllegalArgumentException(issue);
        }
    }

    /**
     * Tests this setting for issues like levels not aligning or missing variables.
     * Called during construction, but this method allows for testing deserialized objects.
     *
     * @return first flaw found or null if no flaws found
     */
    public @Nullable String findFlaw() {
        if (maxLevel > upgrade.maxLevel()) {
            return "Max level must be less than or equal to " + upgrade.maxLevel();
        }
        if (startingLevel > maxLevel) {
            return "Starting level must be less than or equal to " + maxLevel;
        }
        for (UpgradeVariable var : upgrade.variables()) {
            if (!variableSettings.containsKey(var)) {
                return "Variable '" + var.name() + "' does not exist in settings";
            }
            if (!variableSettings.get(var).supportsUpToLevel(maxLevel)) {
                return "Variable '" + var.name() + "' does not support up to max level";
            }
        }
        if (!costSettings.supportsUpToLevel(maxLevel)) {
            return "Cost settings must support up to max level";
        }
        return null;
    }

    /**
     * Gets the associated upgrade.
     *
     * @return upgrade
     */
    public Upgrade upgrade() {
        return upgrade;
    }

    /**
     * Gets the value of a variable at a given level.
     *
     * @param variable variable
     * @param level level
     * @return value of variable at level
     */
    public BigDecimal valueAt(UpgradeVariable variable, int level) {
        return variable.get(Objects.requireNonNull(this.variableSettings.get(variable)).get(level));
    }

    /**
     * Gets the max level chosen in settings. Cannot exceed {@link Upgrade#maxLevel()}.
     *
     * @return max level
     */
    public int maxLevel() {
        return maxLevel;
    }

    /**
     * Gets the starting level for factions to begin at.
     *
     * @return starting level
     */
    public int startingLevel() {
        return startingLevel;
    }

    /**
     * Gets the cost of the upgrade at a given level.
     *
     * @param level level
     * @return cost at level
     */
    public BigDecimal costAt(int level) {
        if (level < 1 || level > maxLevel) {
            throw new IllegalArgumentException("Level must be between 1 and max level");
        }
        return this.costSettings.get(level);
    }
}
