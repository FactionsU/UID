package dev.kitteh.factions.integration;

import dev.kitteh.factions.integration.dynmap.EngineDynmap;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;

public class IntegrationManager implements Listener {

    public interface Integration {
        String pluginName();

        Function<Plugin, Boolean> startup();
    }

    @SuppressWarnings("Convert2MethodRef")
    public enum Integrations implements Integration {
        DYNMAP("dynmap", p -> EngineDynmap.getInstance().init(p)),
        ESS("Essentials", p -> Essentials.setup(p)), // RESIST THE URGE TO REPLACE WITH LAMBDA REFERENCE
        DEPENIZEN("Depenizen", p -> Depenizen.init(p)), // RESIST THE URGE TO REPLACE WITH LAMBDA REFERENCE
        DUELS("Duels", p -> Duels.init(p)),
        GRAVES("Graves", p -> {
            try {
                Class.forName("com.ranull.graves.Graves");
                return Graves.init(p);
            } catch (Exception ignored) {
            }
            return false;
        }),
        GRAVESX("GravesX", p -> Graves.init(p)),
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
                AbstractFactionsPlugin.instance().log("Found an outdated LuckPerms. With LuckPerms 5.1.0 and above, FactionsUUID supports permission contexts!");
            } else {
                if (LuckPerms.init(plugin)) {
                    AbstractFactionsPlugin.instance().luckpermsEnabled();
                }
            }
            return true;
        }),
        MAGIC("Magic", p -> Magic.init(p)), // RESIST THE URGE TO REPLACE WITH LAMBDA REFERENCE
        PLACEHOLDERAPI("PlaceholderAPI", p -> {
            PapiExpansion papi = new PapiExpansion();
            if (papi.register()) {
                AbstractFactionsPlugin.instance().getLogger().info("Successfully registered placeholders with PlaceholderAPI.");
                return true;
            }
            return false;
        }),
        SENTINEL("Sentinel", p -> Sentinel.init(p)), // RESIST THE URGE TO REPLACE WITH LAMBDA REFERENCE
        WORLDGUARD("WorldGuard", plugin -> {
            AbstractFactionsPlugin f = AbstractFactionsPlugin.instance();
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

        private final String pluginName;
        private final Function<Plugin, Boolean> startup;

        Integrations(String pluginName, Function<Plugin, Boolean> startup) {
            this.pluginName = pluginName;
            this.startup = startup;
        }

        @Override
        public String pluginName() {
            return this.pluginName;
        }

        @Override
        public Function<Plugin, Boolean> startup() {
            return this.startup;
        }
    }

    private final AbstractFactionsPlugin plugin;

    private final Map<String, Integration> integrations = new HashMap<>();

    private final Set<Integration> integrationsEnabled = new HashSet<>();

    public IntegrationManager(AbstractFactionsPlugin plugin) {
        this.plugin = plugin;
        for (Integrations integration : Integrations.values()) {
            this.add(integration);
        }
    }

    public void add(Integration integration) {
        this.integrations.put(integration.pluginName(), integration);
    }

    @EventHandler
    public void onPluginEnabled(ServerLoadEvent event) {
        for (Integration integration : this.integrations.values()) {
            Plugin plug = this.plugin.getServer().getPluginManager().getPlugin(integration.pluginName());

            if (plug != null && plug.isEnabled()) {
                try {
                    if (integration.startup().apply(plug)) {
                        this.integrationsEnabled.add(integration);
                    }
                } catch (Exception e) {
                    this.plugin.getLogger().log(Level.WARNING, "Failed to start " + integration.pluginName() + " integration", e);
                }
            }
        }
    }

    public boolean isEnabled(Integrations integration) {
        return this.integrationsEnabled.contains(integration);
    }

    public Set<String> integrationNames() {
        return new HashSet<>(this.integrations.keySet());
    }
}
