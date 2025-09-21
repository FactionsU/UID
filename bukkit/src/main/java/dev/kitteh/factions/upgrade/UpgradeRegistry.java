package dev.kitteh.factions.upgrade;

import dev.kitteh.factions.data.MemoryUniverse;
import dev.kitteh.factions.plugin.Instances;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Upgrade registry.
 */
@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public class UpgradeRegistry {
    private static boolean closed = false;
    private static final Map<String, Upgrade> upgradeRegistry = new ConcurrentHashMap<>();
    private static final Map<String, UpgradeVariable> variableRegistry = new ConcurrentHashMap<>();
    private static final List<Consumer<MemoryUniverse>> upgradeDefaultRunners = new CopyOnWriteArrayList<>();

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
        for (Consumer<MemoryUniverse> upgrade : upgradeDefaultRunners) {
            upgrade.accept(Instances.UNIVERSE);
        }
    }

    /**
     * Gets a registered upgrade.
     *
     * @param name upgrade name
     * @return upgrade or null if none registered
     */
    public static @Nullable Upgrade getUpgrade(String name) {
        return upgradeRegistry.get(name.toLowerCase());
    }

    /**
     * Gets a registered upgrade variable.
     *
     * @param name upgrade variable name
     * @return upgrade variable or null if none registered
     */
    public static @Nullable UpgradeVariable getVariable(String name) {
        return variableRegistry.get(name.toLowerCase());
    }

    /**
     * Gets all registered upgrades.
     *
     * @return collection of upgrades registered
     */
    public static Collection<? extends Upgrade> getUpgrades() {
        return new HashSet<>(upgradeRegistry.values());
    }

    /**
     * Registers an upgrade.
     *
     * @param upgrade upgrade to register
     * @param settings upgrade settings
     * @param defaultDisabled if the upgrade should be disabled by default
     * @throws IllegalStateException if called after load time
     * @throws IllegalArgumentException if upgrade name is already registered
     * @throws IllegalArgumentException if upgrade settings does not match upgrade
     * @throws IllegalArgumentException if upgrade variables present are not registered
     */
    public static void registerUpgrade(Upgrade upgrade, UpgradeSettings settings, boolean defaultDisabled) {
        if (closed) {
            throw new IllegalStateException("Cannot register upgrade. Must be completed during load.");
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
        upgradeDefaultRunners.add(u -> u.addDefaultsIfNotPresent(settings, defaultDisabled));
    }

    /**
     * Registers an upgrade variable.
     *
     * @param variable variable to register
     * @throws IllegalStateException if called after load time
     * @throws IllegalArgumentException if variable name already registered
     */
    public static void registerVariable(UpgradeVariable variable) {
        if (closed) {
            throw new IllegalStateException("Cannot register upgrade variable. Must be completed during load.");
        }
        if (variableRegistry.containsKey(variable.name().toLowerCase())) {
            throw new IllegalArgumentException("Upgrade variable with name '" + variable.name() + "' already registered");
        }
        variableRegistry.put(variable.name().toLowerCase(), variable);
    }
}
