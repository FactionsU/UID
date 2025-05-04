package dev.kitteh.factions.upgrade;

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
        for (UpgradeVariable variable : Upgrades.Variables.VARIABLES) {
            registerVariable(variable);
        }

        for (Upgrade upgrade : Upgrades.UPGRADES) {
            registerUpgrade(upgrade);
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

    public static void registerUpgrade(Upgrade action) {
        if (closed) {
            throw new IllegalStateException("Cannot register upgrade. Must be done during onLoad().");
        }
        if (upgradeRegistry.containsKey(action.name().toLowerCase())) {
            throw new IllegalArgumentException("Upgrade with name " + action.name() + " already registered");
        }
        upgradeRegistry.put(action.name().toLowerCase(), action);
    }

    public static void registerVariable(UpgradeVariable action) {
        if (closed) {
            throw new IllegalStateException("Cannot register upgrade variable. Must be done during onLoad().");
        }
        if (variableRegistry.containsKey(action.name().toLowerCase())) {
            throw new IllegalArgumentException("Upgrade variable with name " + action.name() + " already registered");
        }
        variableRegistry.put(action.name().toLowerCase(), action);
    }
}
