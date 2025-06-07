package dev.kitteh.factions.integration;

import dev.kitteh.factions.event.DTRLossEvent;
import dev.kitteh.factions.event.PowerLossEvent;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class Duels implements Listener {
    public static boolean init(Plugin plugin) {
        if (plugin instanceof com.meteordevelopments.duels.api.Duels duels) {
            AbstractFactionsPlugin fuuid = AbstractFactionsPlugin.instance();
            fuuid.getServer().getPluginManager().registerEvents(new Duels(duels), fuuid);
            boolean noPowerLoss = duels.getConfig().getBoolean("supported-plugins.FactionsUUID.no-power-loss-in-duel", false);
            fuuid.getLogger().info("Found Duels plugin. Currently configured to " + (noPowerLoss ? "not " : "") + "lose power/dtr in a duel.");
            fuuid.getLogger().info(" Change this in the Duels config.");
            return true;
        }
        return false;
    }

    private final com.meteordevelopments.duels.api.Duels duels;

    private Duels(com.meteordevelopments.duels.api.Duels duels) {
        this.duels = duels;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPowerLoss(PowerLossEvent event) {
        if (duels.getConfig().getBoolean("supported-plugins.FactionsUUID.no-power-loss-in-duel", false) &&
                event.getFPlayer().asPlayer() instanceof Player player &&
                this.duels.getArenaManager().isInMatch(player)) {
            event.setCancelled(true);
            event.setMessage(null);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDtrLoss(DTRLossEvent event) {
        if (duels.getConfig().getBoolean("supported-plugins.FactionsUUID.no-power-loss-in-duel", false) &&
                event.getFPlayer().asPlayer() instanceof Player player &&
                this.duels.getArenaManager().isInMatch(player)) {
            event.setCancelled(true);
        }
    }
}
