package com.massivecraft.factions.integration;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.integration.permcontext.Context;
import com.massivecraft.factions.integration.permcontext.ContextManager;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class LuckPerms {
    private static class FactionsContextCalculator implements ContextCalculator<Player> {
        @Override
        public void calculate(Player player, ContextConsumer contextConsumer) {
            for (Context context : ContextManager.getContexts()) {
                for (String value : context.getValues(player)) {
                    contextConsumer.accept(context.getNamespacedName(), value);
                }
            }
        }

        @Override
        public ContextSet estimatePotentialContexts() {
            ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
            for (Context context : ContextManager.getContexts()) {
                for (String value : context.getPossibleValues()) {
                    builder.add(context.getNamespacedName(), value);
                }
            }
            return builder.build();
        }
    }

    private static FactionsContextCalculator calculator;

    public static void init(FactionsPlugin plugin) {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            return;
        }

        String[] version = plugin.getServer().getPluginManager().getPlugin("LuckPerms").getDescription().getVersion().split("\\.");
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
            plugin.log("Found an outdated LuckPerms. With LuckPerms 5.1.0 and above, FactionsUUID supports permission contexts!");
            return;
        }

        try {
            net.luckperms.api.LuckPerms api = LuckPermsProvider.get();
            api.getContextManager().registerCalculator(calculator = new FactionsContextCalculator());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to LuckPerms!", e);
            return;
        }
        plugin.log("Successfully hooked into LuckPerms for permission contexts!");
    }

    public static void shutdown(FactionsPlugin plugin) {
        if (calculator != null) {
            try {
                net.luckperms.api.LuckPerms api = LuckPermsProvider.get();
                api.getContextManager().unregisterCalculator(calculator);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to unregister contexts with LuckPerms!", e);
            }
        }
    }
}
