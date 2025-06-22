package dev.kitteh.factions.upgrade;

import dev.kitteh.factions.Universe;
import dev.kitteh.factions.data.MemoryUniverse;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public class UpgradeRegistry {
    private static boolean closed = false;
    private static final Map<String, Upgrade> upgradeRegistry = new ConcurrentHashMap<>();
    private static final Map<String, UpgradeVariable> variableRegistry = new ConcurrentHashMap<>();

    static {
        for (UpgradeVariable variable : Upgrades.VARIABLES) {
            variableRegistry.put(variable.name().toLowerCase(), variable);
        }

        for (Upgrade upgrade : Upgrades.UPGRADES) {
            upgradeRegistry.put(upgrade.name().toLowerCase(), upgrade);
        }
    }

    @SuppressWarnings("unused")
    private static void close() {
        closed = true;
    }

    public static @Nullable Upgrade getUpgrade(String name) {
        return upgradeRegistry.get(name.toLowerCase());
    }

    public static @Nullable UpgradeVariable getVariable(String name) {
        return variableRegistry.get(name.toLowerCase());
    }

    public static Collection<? extends Upgrade> getUpgrades() {
        return new HashSet<>(upgradeRegistry.values());
    }

    public static void registerUpgrade(Upgrade upgrade, UpgradeSettings settings, boolean defaultDisabled) {
        if (closed) {
            throw new IllegalStateException("Cannot register upgrade. Must be done during onLoad().");
        }
        if (upgradeRegistry.containsKey(upgrade.name().toLowerCase())) {
            throw new IllegalArgumentException("Upgrade with name '" + upgrade.name() + "' already registered");
        }
        if (upgrade != settings.upgrade()) {
            throw new IllegalArgumentException("Upgrade settings does not contain same Upgrade");
        }
        for (UpgradeVariable variable : upgrade.variables()) {
            UpgradeVariable var = getVariable(variable.name());
            if (var == null) {
                throw new IllegalArgumentException("Variable '" + variable.name() + "' not found");
            }
            if (var != variable) {
                throw new IllegalArgumentException("Variable with name '" + variable.name() + "' already registered but does not match this upgrade's variable");
            }
        }
        upgradeRegistry.put(upgrade.name().toLowerCase(), upgrade);
        ((MemoryUniverse) Universe.universe()).addDefaultsIfNotPresent(settings, defaultDisabled);
    }

    public static void registerVariable(UpgradeVariable variable) {
        if (closed) {
            throw new IllegalStateException("Cannot register upgrade variable. Must be done during onLoad().");
        }
        if (variableRegistry.containsKey(variable.name().toLowerCase())) {
            throw new IllegalArgumentException("Upgrade variable with name '" + variable.name() + "' already registered");
        }
        variableRegistry.put(variable.name().toLowerCase(), variable);
    }
}
