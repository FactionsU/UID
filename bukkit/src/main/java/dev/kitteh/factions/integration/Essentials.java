package dev.kitteh.factions.integration;

import com.earth2me.essentials.AsyncTeleport;
import com.earth2me.essentials.User;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.event.FPlayerLeaveEvent;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.ComponentDispatcher;
import net.ess3.api.IEssentials;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@ApiStatus.Internal
public class Essentials {
    private static IEssentials essentials;

    public static boolean setup(Plugin ess) {
        essentials = (IEssentials) ess;
        AbstractFactionsPlugin plugin = AbstractFactionsPlugin.instance();
        plugin.getLogger().info("Found and connected to Essentials");
        ExternalChecks.registerAfk(ess, player -> essentials.getUser(player).isAfk());
        ExternalChecks.registerIgnored(ess, (viewer, chatter) -> essentials.getUser(viewer).isIgnoredPlayer(essentials.getUser(chatter)));
        ExternalChecks.registerMuted(ess, player -> essentials.getUser(player).isMuted());
        ExternalChecks.registerVanished(ess, player -> essentials.getUser(player).isVanished());
        if (FactionsPlugin.instance().conf().factions().homes().isTeleportCommandEssentialsIntegration()) {
            ExternalChecks.registerTeleport(ess, Essentials::handleTeleport);
        }
        if (plugin.conf().factions().other().isDeleteEssentialsHomes()) {
            plugin.getLogger().info("Based on main.conf will delete Essentials player homes in their old faction when they leave");
            plugin.getServer().getPluginManager().registerEvents(new EssentialsListener(essentials), plugin);
        }
        if (plugin.conf().factions().homes().isTeleportCommandEssentialsIntegration()) {
            plugin.getLogger().info("Using Essentials for teleportation");
        }
        return true;
    }

    private static boolean handleTeleport(Player player, Location loc) {
        AsyncTeleport teleport = essentials.getUser(player).getAsyncTeleport();
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        future.exceptionally(e -> {
            ComponentDispatcher.send(player, Component.text().color(NamedTextColor.RED).content(e.getMessage()));
            return false;
        });
        teleport.teleport(loc, null, PlayerTeleportEvent.TeleportCause.PLUGIN, future);

        return true;
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

    public static class EssentialsListener implements Listener {

        private final com.earth2me.essentials.IEssentials ess;

        public EssentialsListener(com.earth2me.essentials.IEssentials essentials) {
            this.ess = essentials;
        }

        @EventHandler
        public void onLeave(FPlayerLeaveEvent event) throws Exception {
            // Get the USER from their UUID.
            Faction faction = event.getFaction();
            User user = ess.getUser(event.getFPlayer().uniqueId());
            if (user == null) {
                AbstractFactionsPlugin.instance().log(Level.WARNING, "Attempted to remove Essentials homes for " + event.getFPlayer().name() + " " +
                        "but no Essentials data at all was found for this user. " +
                        "This may be a bug in Essentials, or may be that the player only played prior to adding Essentials to the server");
                return;
            }

            List<String> homes = user.getHomes();
            if (homes == null || homes.isEmpty()) {
                return;
            }

            // Not a great way to do this on essential's side.
            for (String homeName : user.getHomes()) {

                Location loc = user.getHome(homeName);
                if (loc == null) { // Newer EssX just returns null on invalid world
                    AbstractFactionsPlugin.instance().getLogger().warning("Tried to check on home \"" + homeName + "\" for user \"" + event.getFPlayer().name() + "\" but Essentials could not load that home (invalid world?). Skipping it.");
                    continue;
                }

                Faction factionAt = new FLocation(loc).faction();
                // We're only going to remove homes in territory that belongs to THEIR faction.
                if (factionAt == faction && factionAt.isNormal()) {
                    user.delHome(homeName);
                    AbstractFactionsPlugin.instance().log(Level.INFO, "FactionLeaveEvent: Removing EssX home %s, player %s, in territory of %s",
                            homeName, event.getFPlayer().name(), faction.tag());
                }
            }
        }
    }
}
