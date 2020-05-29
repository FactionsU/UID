package com.massivecraft.factions.integration;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.integration.permcontext.LuckpermsContextCalculator;
import net.luckperms.api.LuckPermsProvider;

import java.util.logging.Level;

public class LuckPerms {
    private static LuckpermsContextCalculator calculator;

    public static boolean init(FactionsPlugin plugin) {
        calculator = new LuckpermsContextCalculator();
        try {
            net.luckperms.api.LuckPerms api = LuckPermsProvider.get();
            api.getContextManager().registerCalculator(calculator);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to LuckPerms!", e);
            return false;
        }
        plugin.log("Successfully hooked into LuckPerms for permission contexts!");
        return true;
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
