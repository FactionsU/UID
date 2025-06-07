package dev.kitteh.factions.upgrade;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    public Upgrade upgrade() {
        return upgrade;
    }

    public BigDecimal valueAt(UpgradeVariable variable, int level) {
        return variable.get(Objects.requireNonNull(this.variableSettings.get(variable)).get(level));
    }

    public int maxLevel() {
        return maxLevel;
    }

    public int startingLevel() {
        return startingLevel;
    }

    public BigDecimal costAt(int level) {
        if (level < 1 || level > maxLevel) {
            throw new IllegalArgumentException("Level must be between 1 and max level");
        }
        return this.costSettings.get(level);
    }
}
