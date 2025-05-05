package dev.kitteh.factions.upgrade;

import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@NullMarked
public final class UpgradeSettings {
    private final Upgrade upgrade;
    private final Map<UpgradeVariable, LeveledValueProvider> variableSettings;
    private final int maxLevel;
    private final LeveledValueProvider costSettings;

    public UpgradeSettings(Upgrade upgrade, Map<UpgradeVariable, LeveledValueProvider> variableSettings, int maxLevel, LeveledValueProvider costSettings) {
        if (maxLevel > upgrade.maxLevel()) {
            throw new IllegalArgumentException("Max level must be less than or equal to " + upgrade.maxLevel());
        }
        for (UpgradeVariable var : upgrade.variables()) {
            if (!variableSettings.containsKey(var)) {
                throw new IllegalArgumentException("Variable '" + var.name() + "' does not exist in settings");
            }
            if (!variableSettings.get(var).supportsUpToLevel(maxLevel)) {
                throw new IllegalArgumentException("Variable '" + var.name() + "' does not support up to max level");
            }
        }
        if (!costSettings.supportsUpToLevel(maxLevel)) {
            throw new IllegalArgumentException("Cost settings must support up to max level");
        }
        this.upgrade = upgrade;
        this.variableSettings = new HashMap<>(variableSettings);
        this.maxLevel = maxLevel;
        this.costSettings = costSettings;
    }

    public Upgrade upgrade() {
        return upgrade;
    }

    public BigDecimal valueAt(UpgradeVariable variable, int level) {
        return Objects.requireNonNull(this.variableSettings.get(variable)).get(level);
    }

    public int maxLevel() {
        return maxLevel;
    }

    public BigDecimal costAt(int level) {
        if (level < 1 || level > maxLevel) {
            throw new IllegalArgumentException("Level must be between 1 and max level");
        }
        return this.costSettings.get(level);
    }
}
