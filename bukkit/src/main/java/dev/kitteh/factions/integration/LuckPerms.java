package dev.kitteh.factions.integration;

import dev.kitteh.factions.integration.permcontext.LuckpermsContextCalculator;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;

import java.util.logging.Level;

@ApiStatus.Internal
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
            AbstractFactionsPlugin.instance().getLogger().log(Level.SEVERE, "Failed to connect to LuckPerms!", e);
            return false;
        }
        AbstractFactionsPlugin.instance().log("Successfully hooked into LuckPerms for permission contexts!");
        return true;
    }

    public static void shutdown(AbstractFactionsPlugin plugin) {
        if (calculator != null && LuckPerms.plugin.isEnabled()) {
            try {
                net.luckperms.api.LuckPerms api = LuckPermsProvider.get();
                api.getContextManager().unregisterCalculator(calculator);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to unregister contexts with LuckPerms!", e);
            }
        }
    }
}
