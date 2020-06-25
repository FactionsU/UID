package com.massivecraft.factions.integration.permcontext;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.massivecraft.factions.FactionsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Plugin-agnostic registration of contexts. Other plugins can hook into this.
 */
public class ContextManager implements Listener {
    private static Multimap<String, Context> registeredContexts;

    /**
     * Should be called by FactionsUUID only.
     *
     * @param plugin the plugin
     */
    public static void init(FactionsPlugin plugin) {
        registeredContexts = Multimaps.synchronizedMultimap(ArrayListMultimap.create());
        registeredContexts.putAll(plugin.getName(), Arrays.asList(Contexts.values()));
        plugin.getServer().getPluginManager().registerEvents(new ContextManager(), plugin);
    }

    /**
     * Should be called by FactionsUUID only.
     */
    public static void shutdown() {
        registeredContexts = null;
    }

    /**
     * Gets currently registered contexts.
     *
     * @return immutable set of registered contexts
     */
    public static Set<Context> getContexts() {
        if (registeredContexts == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new HashSet<>(registeredContexts.values()));
    }

    /**
     * Registers a context. Should be called after FactionsUUID is loaded or
     * reloaded. Easiest strategy is to run in onEnable and on {@code PluginEnableEvent}.
     *
     * @param context context
     */
    public static void registerContext(Context context) {
        if (registeredContexts == null) {
            throw new IllegalStateException("Cannot register contexts before FactionsUUID finishes loading!");
        }
        Objects.requireNonNull(context);
        if (context.getNamespacedName().indexOf(':') == -1) {
            throw new IllegalArgumentException("Invalid namespaced name " + context.getNamespacedName());
        }
        if (context.getNamespace().equalsIgnoreCase(Contexts.FACTIONSUUID_NAMESPACE) ||
                context.getNamespacedName().split(":")[0].equalsIgnoreCase(Contexts.FACTIONSUUID_NAMESPACE)) {
            throw new IllegalArgumentException("Cannot register contexts using namespace 'factionsuuid'");
        }

        registeredContexts.put(JavaPlugin.getProvidingPlugin(context.getClass()).getName(), context);
    }

    private ContextManager() {
        // Honk!
    }

    @EventHandler
    public void onDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(FactionsPlugin.getInstance())) {
            return;
        }
        if (registeredContexts != null) {
            registeredContexts.removeAll(event.getPlugin().getName());
        }
    }
}
