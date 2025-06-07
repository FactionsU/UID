package dev.kitteh.factions.integration;

import com.earth2me.essentials.AsyncTeleport;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.listener.EssentialsListener;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.ComponentDispatcher;
import net.ess3.api.IEssentials;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public class Essentials {

    private static IEssentials essentials;

    public static boolean setup(Plugin ess) {
        essentials = (IEssentials) ess;
        AbstractFactionsPlugin plugin = AbstractFactionsPlugin.instance();
        plugin.getLogger().info("Found and connected to Essentials");
        if (plugin.conf().factions().other().isDeleteEssentialsHomes()) {
            plugin.getLogger().info("Based on main.conf will delete Essentials player homes in their old faction when they leave");
            plugin.getServer().getPluginManager().registerEvents(new EssentialsListener(essentials), plugin);
        }
        if (plugin.conf().factions().homes().isTeleportCommandEssentialsIntegration()) {
            plugin.getLogger().info("Using Essentials for teleportation");
        }
        return true;
    }

    // return false if feature is disabled or Essentials isn't available
    public static boolean handleTeleport(Player player, Location loc) {
        if (!FactionsPlugin.instance().conf().factions().homes().isTeleportCommandEssentialsIntegration() || essentials == null) {
            return false;
        }

        AsyncTeleport teleport = essentials.getUser(player).getAsyncTeleport();
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        future.exceptionally(e -> {
            ComponentDispatcher.send(player, Component.text().color(NamedTextColor.RED).content(e.getMessage()));
            return false;
        });
        teleport.teleport(loc, null, PlayerTeleportEvent.TeleportCause.PLUGIN, future);

        return true;
    }

    public static boolean isVanished(Player player) {
        return essentials != null && player != null && essentials.getUser(player).isVanished();
    }

    public static boolean isIgnored(Player viewer, Player chatter) {
        return essentials != null && essentials.getUser(viewer).isIgnoredPlayer(essentials.getUser(chatter));
    }

    public static boolean isAfk(Player player) {
        return essentials != null && player != null && essentials.getUser(player).isAfk();
    }

    public static boolean isOverBalCap(double amount) {
        if (essentials == null) {
            return false;
        }

        return amount > essentials.getSettings().getMaxMoney().doubleValue();
    }

    public static Plugin getEssentials() {
        return essentials;
    }
}
