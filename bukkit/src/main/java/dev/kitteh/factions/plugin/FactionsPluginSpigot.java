package dev.kitteh.factions.plugin;

import dev.kitteh.factions.listener.FactionsLegacyChatListener;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FactionsPluginSpigot extends AbstractFactionsPlugin {
    @Override
    protected String pluginType() {
        return "Spigot";
    }

    @Override
    public void onPluginLoad() {
        // Fix for legacy dependency system's grumpiness about not defining softdepends because it's a nightmare
        try {
            Field depGraph = SimplePluginManager.class.getDeclaredField("dependencyGraph");
            depGraph.setAccessible(true);
            Object graph = depGraph.get(this.getServer().getPluginManager());
            Method putEdge = graph.getClass().getDeclaredMethod("putEdge", Object.class, Object.class);
            putEdge.setAccessible(true);
            String pluginName = this.getDescription().getName();
            for (String depend : this.integrationManager().integrationNames()) {
                putEdge.invoke(graph, pluginName, depend);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void registerEvents() {
        this.getServer().getPluginManager().registerEvents(new FactionsLegacyChatListener(this), this);
    }
}
