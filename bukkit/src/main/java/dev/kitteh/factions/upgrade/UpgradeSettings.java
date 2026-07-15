package dev.kitteh.factions.upgrade;

import dev.kitteh.factions.Faction;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
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
    private final List<UpgradePrerequisite> prerequisites;

    public UpgradeSettings(Upgrade upgrade, Map<UpgradeVariable, LeveledValueProvider> variableSettings, int maxLevel, int startingLevel, LeveledValueProvider costSettings) {
        this(upgrade, variableSettings, maxLevel, startingLevel, costSettings, List.of());
    }

    @ApiStatus.AvailableSince("4.6.0")
    public UpgradeSettings(Upgrade upgrade, Map<UpgradeVariable, LeveledValueProvider> variableSettings, int maxLevel, int startingLevel, LeveledValueProvider costSettings, List<UpgradePrerequisite> prerequisites) {
        this.upgrade = upgrade;
        this.variableSettings = new HashMap<>(variableSettings);
        this.maxLevel = maxLevel;
        this.startingLevel = startingLevel;
        this.costSettings = costSettings;
        this.prerequisites = List.copyOf(prerequisites);
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
                return "Variable '" + var.name() + "' must exist in settings";
            }
            if (!variableSettings.get(var).supportsUpToLevel(maxLevel)) {
                return "Variable '" + var.name() + "' must support up to max level";
            }
        }
        if (!costSettings.supportsUpToLevel(maxLevel)) {
            return "Cost settings must support up to max level";
        }
        for (UpgradePrerequisite prerequisite : prerequisites()) {
            if (prerequisite.upgrade().equalsIgnoreCase(upgrade.name())) {
                return "Upgrade cannot be its own prerequisite";
            }
            Upgrade required = UpgradeRegistry.getUpgrade(prerequisite.upgrade());
            if (required == null) {
                return "Prerequisite '" + prerequisite.upgrade() + "' is not a registered upgrade";
            }
            if (prerequisite.minLevel() > required.maxLevel()) {
                return "Prerequisite '" + prerequisite.upgrade() + "' min level exceeds its max level";
            }
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
     * Gets the value provider for a given variable.
     *
     * @param variable variable
     * @return value provider or null if the variable is not present
     */
    @ApiStatus.AvailableSince("4.6.0")
    public @Nullable LeveledValueProvider variableProvider(UpgradeVariable variable) {
        return this.variableSettings.get(variable);
    }

    /**
     * Gets the value provider for the cost of this upgrade.
     *
     * @return cost value provider
     */
    @ApiStatus.AvailableSince("4.6.0")
    public LeveledValueProvider costProvider() {
        return this.costSettings;
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

    /**
     * Gets the prerequisites for purchase.
     *
     * @return list of prerequisites, possibly empty
     */
    @ApiStatus.AvailableSince("4.6.0")
    public List<UpgradePrerequisite> prerequisites() {
        //noinspection ConstantValue
        return this.prerequisites == null ? List.of() : this.prerequisites;
    }

    /**
     * Tests whether a faction satisfies every prerequisite.
     *
     * @param faction faction to test
     * @return true if all prerequisites are met
     */
    @ApiStatus.AvailableSince("4.6.0")
    public boolean prerequisitesMet(Faction faction) {
        for (UpgradePrerequisite prerequisite : prerequisites()) {
            Upgrade required = UpgradeRegistry.getUpgrade(prerequisite.upgrade());
            if (required == null || faction.upgradeLevel(required) < prerequisite.minLevel()) {
                return false;
            }
        }
        return true;
    }
}
