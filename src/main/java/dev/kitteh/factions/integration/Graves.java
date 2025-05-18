package dev.kitteh.factions.integration;

import com.ranull.graves.event.GraveCreateEvent;
import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class Graves {
    private static com.ranull.graves.Graves plugin;

    public static boolean init(Plugin graves) {
        plugin = (com.ranull.graves.Graves) graves;
        AbstractFactionsPlugin plugin = AbstractFactionsPlugin.getInstance();
        plugin.getLogger().info("Found Graves plugin");
        MainConfig.Plugins.Graves g = plugin.conf().plugins().graves();
        if (g.isAllowAnyoneToOpenGraves()) {
            plugin.getLogger().info("Configured to allow anyone to open graves regardless of permissions.");
        }
        if (g.isPreventGravesInSafezone() || g.isPreventGravesInWarzone()) {
            String s;
            if (g.isPreventGravesInSafezone() && !g.isPreventGravesInWarzone()) {
                s = "safezone.";
            } else if (!g.isPreventGravesInSafezone()) {
                s = "warzone.";
            } else {
                s = "safezone and warzone.";
            }
            plugin.getLogger().info("Configured to prevent graves in " + s);
            plugin.getServer().getPluginManager().registerEvents(new GraveListener(), plugin);
        }
        return true;
    }

    public static boolean allowAnyway(Block block) {
        try {
            if (plugin != null && FactionsPlugin.getInstance().conf().plugins().graves().isAllowAnyoneToOpenGraves()) {
                return plugin.getBlockManager().getGraveFromBlock(block) != null;
            }
        } catch (Exception oops) {
            AbstractFactionsPlugin.getInstance().getLogger().log(Level.WARNING, "A Grave(s) error occurred!", oops);
        }
        return false;
    }

    private static class GraveListener implements Listener {
        @EventHandler
        public void graveCreate(GraveCreateEvent event) {
            boolean safe = FactionsPlugin.getInstance().conf().plugins().graves().isPreventGravesInSafezone();
            boolean war = FactionsPlugin.getInstance().conf().plugins().graves().isPreventGravesInWarzone();
            if (!safe && !war) {
                return;
            }

            if (!WorldUtil.isEnabled(event.getGrave().getLocationDeath().getWorld())) {
                return;
            }
            Faction faction = Board.board().factionAt(new FLocation(event.getGrave().getLocationDeath()));
            if ((safe && faction.isSafeZone()) || (war && faction.isWarZone())) {
                event.setCancelled(true);
                plugin.debugMessage("Grave not created for " + plugin.getEntityManager().getEntityName(event.getEntity()) + " because FactionsUUID (which is sending this debug message) blocked it (" + (faction.isSafeZone() ? "safezone" : "warzone") + " protected)", 2);
            }
        }
    }
}
