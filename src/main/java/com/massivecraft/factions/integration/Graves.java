package com.massivecraft.factions.integration;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.file.MainConfig;
import com.ranull.graves.event.GraveCreateEvent;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class Graves {
    private static com.ranull.graves.Graves plugin;

    public static boolean init(Plugin graves) {
        plugin = (com.ranull.graves.Graves) graves;
        FactionsPlugin plugin = FactionsPlugin.getInstance();
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
            FactionsPlugin.getInstance().getServer().getPluginManager().registerEvents(new GraveListener(), FactionsPlugin.getInstance());
        }
        return true;
    }

    public static boolean allowAnyway(Block block) {
        try {
            if (plugin != null && FactionsPlugin.getInstance().conf().plugins().graves().isAllowAnyoneToOpenGraves()) {
                return plugin.getBlockManager().getGraveFromBlock(block) != null;
            }
        } catch (Exception oops) {
            FactionsPlugin.getInstance().getLogger().log(Level.WARNING, "A Grave(s) error occurred!", oops);
            oops.printStackTrace(); // OBNOXIOUS!
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

            if (!FactionsPlugin.getInstance().worldUtil().isEnabled(event.getGrave().getLocationDeath().getWorld())) {
                return;
            }
            Faction faction = Board.getInstance().getFactionAt(new FLocation(event.getGrave().getLocationDeath()));
            if ((safe && faction.isSafeZone()) || (war && faction.isWarZone())) {
                event.setCancelled(true);
                plugin.debugMessage("Grave not created for " + plugin.getEntityManager().getEntityName(event.getEntity()) + " because FactionsUUID (which is sending this debug message) blocked it (" + (faction.isSafeZone() ? "safezone" : "warzone") + " protected)", 2);
            }
        }
    }
}
