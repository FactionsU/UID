package com.massivecraft.factions.integration;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.integration.dynmap.EngineDynmap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;

public class IntegrationManager implements Listener {
    public enum Integration {
        DYNMAP("dynmap", EngineDynmap.getInstance()::init),
        @SuppressWarnings("Convert2MethodRef")
        ESS("Essentials", (p) -> Essentials.setup(p)), // RESIST THE URGE TO REPLACE WITH LAMBDA REFERENCE
        @SuppressWarnings("Convert2MethodRef")
        DEPENIZEN("Depenizen", (p) -> Depenizen.init(p)), // RESIST THE URGE TO REPLACE WITH LAMBDA REFERENCE
        GRAVES("Graves", Graves::init),
        LUCKPERMS("LuckPerms", (plugin) -> {
            String[] version = plugin.getDescription().getVersion().split("\\.");
            boolean notSupported = true;
            try {
                int major = Integer.parseInt(version[0]);
                int minor = Integer.parseInt(version[1]);
                if ((major == 5 && minor > 0) || major > 5) {
                    notSupported = false;
                }
            } catch (NumberFormatException ignored) {
            }
            if (notSupported) {
                FactionsPlugin.getInstance().log("Found an outdated LuckPerms. With LuckPerms 5.1.0 and above, FactionsUUID supports permission contexts!");
            } else {
                if (LuckPerms.init(FactionsPlugin.getInstance())) {
                    FactionsPlugin.getInstance().luckpermsEnabled();
                }
            }
            return true;
        }),
        LWC("LWC", com.massivecraft.factions.integration.LWC::setup),
        @SuppressWarnings("Convert2MethodRef")
        MAGIC("Magic", (p) -> Magic.init(p)), // RESIST THE URGE TO REPLACE WITH LAMBDA REFERENCE
        PLACEHOLDERAPI("PlaceholderAPI", (p) -> FactionsPlugin.getInstance().setupPlaceholderAPI()),
        PLACEHOLDERAPI_OTHER("MVdWPlaceholderAPI", (p) -> FactionsPlugin.getInstance().setupOtherPlaceholderAPI()),
        @SuppressWarnings("Convert2MethodRef")
        SENTINEL("Sentinel", (p) -> Sentinel.init(p)), // RESIST THE URGE TO REPLACE WITH LAMBDA REFERENCE
        WORLDGUARD("WorldGuard", (plugin) -> {
            FactionsPlugin f = FactionsPlugin.getInstance();
            String version = plugin.getDescription().getVersion();
            if (version.startsWith("7")) {
                f.setWorldGuard(new Worldguard());
                f.getLogger().info("Found support for WorldGuard version " + version);
                return true;
            } else {
                f.log(Level.WARNING, "Found WorldGuard but couldn't support this version: " + version);
            }
            return false;
        });

        private static final Map<String, Function<Plugin, Boolean>> STARTUP_MAP = new HashMap<>();
        private static final Map<String, Integration> INT_MAP = new HashMap<>();

        static {
            for (Integration integration : values()) {
                STARTUP_MAP.put(integration.pluginName, integration.startup);
                INT_MAP.put(integration.pluginName, integration);
            }
        }

        static Function<Plugin, Boolean> getStartup(String pluginName) {
            return STARTUP_MAP.getOrDefault(pluginName, Integration::omNomNom);
        }

        private static boolean omNomNom(Plugin plugin) {
            return false;
        }

        private final String pluginName;
        private final Function<Plugin, Boolean> startup;

        Integration(String pluginName, Function<Plugin, Boolean> startup) {
            this.pluginName = pluginName;
            this.startup = startup;
        }
    }

    private final Set<Integration> integrations = new HashSet<>();

    public static void onLoad(FactionsPlugin plugin) {
        try {
            Field depGraph = SimplePluginManager.class.getDeclaredField("dependencyGraph");
            depGraph.setAccessible(true);
            Object graph = depGraph.get(plugin.getServer().getPluginManager());
            Method putEdge = graph.getClass().getDeclaredMethod("putEdge", Object.class, Object.class);
            putEdge.setAccessible(true);
            for (String depend : Integration.STARTUP_MAP.keySet()) {
                putEdge.invoke(graph, plugin.getDescription().getName(), depend);
            }
        } catch (Exception ignored) {
        }
    }

    public IntegrationManager(FactionsPlugin plugin) {
        for (Integration integration : Integration.values()) {
            Plugin plug = plugin.getServer().getPluginManager().getPlugin(integration.pluginName);
            if (plug != null && plug.isEnabled()) {
                try {
                    if (integration.startup.apply(plug)) {
                        this.integrations.add(integration);
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to start " + integration.pluginName + " integration", e);
                }
            }
        }
    }

    @EventHandler
    public void onPluginEnabled(PluginEnableEvent event) {
        if (Integration.getStartup(event.getPlugin().getName()).apply(event.getPlugin())) {
            this.integrations.add(Integration.INT_MAP.get(event.getPlugin().getName()));
        }
    }

    public boolean isEnabled(Integration integration) {
        return this.integrations.contains(integration);
    }
}
