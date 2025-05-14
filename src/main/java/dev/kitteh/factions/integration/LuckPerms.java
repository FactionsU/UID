package dev.kitteh.factions.integration;

import dev.kitteh.factions.integration.permcontext.LuckpermsContextCalculator;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class LuckPerms {
    private static LuckpermsContextCalculator calculator;
    private static Plugin plugin;

    public static boolean init(Plugin plugin) {
        LuckPerms.plugin = plugin;
        calculator = new LuckpermsContextCalculator();
        try {
            net.luckperms.api.LuckPerms api = LuckPermsProvider.get();
            api.getContextManager().registerCalculator(calculator);
        } catch (Exception e) {
            AbstractFactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to connect to LuckPerms!", e);
            return false;
        }
        AbstractFactionsPlugin.getInstance().log("Successfully hooked into LuckPerms for permission contexts!");
        return true;
    }

    public static void shutdown(AbstractFactionsPlugin plugin) {
        if (calculator != null && plugin.isEnabled()) {
            try {
                net.luckperms.api.LuckPerms api = LuckPermsProvider.get();
                api.getContextManager().unregisterCalculator(calculator);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to unregister contexts with LuckPerms!", e);
            }
        }
    }
}
