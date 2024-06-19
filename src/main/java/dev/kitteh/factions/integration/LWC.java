package dev.kitteh.factions.integration;

import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LWC {
    private static com.griefcraft.lwc.LWC lwc;

    public static boolean setup(Plugin plugin) {
        if (!(plugin instanceof LWCPlugin)) {
            return false;
        }

        lwc = ((LWCPlugin) plugin).getLWC();
        FactionsPlugin.getInstance().log("Successfully hooked into LWC!" + (FactionsPlugin.getInstance().conf().lwc().isEnabled() ? "" : " Integration is currently disabled (\"lwc.integration\")."));
        return true;
    }

    public static boolean getEnabled() {
        return lwc != null && FactionsPlugin.getInstance().conf().lwc().isEnabled();
    }

    public static Plugin getLWC() {
        return lwc == null ? null : lwc.getPlugin();
    }

    public static void clearOtherLocks(FLocation flocation, Faction faction) {
        Protection protection;
        for (Block block : findBlocks(flocation)) {
            if ((protection = lwc.findProtection(block)) != null) {
                if (!faction.getFPlayers().contains(FPlayers.getInstance().getByPlayer(Bukkit.getServer().getOfflinePlayer(protection.getOwner())))) {
                    protection.remove();
                }
            }
        }
    }

    public static void clearAllLocks(FLocation flocation) {
        Protection protection;
        for (Block block : findBlocks(flocation)) {
            if ((protection = lwc.findProtection(block)) != null) {
                protection.remove();
            }
        }
    }

    private static List<Block> findBlocks(FLocation flocation) {
        World world = Bukkit.getWorld(flocation.getWorldName());
        if (world == null) {
            return Collections.emptyList();  // world not loaded or something? cancel out to prevent error
        }

        BlockState[] blocks = flocation.getChunk().getTileEntities();
        List<Block> lwcBlocks = new LinkedList<>();

        for (BlockState block : blocks) {
            if (lwc.isProtectable(block)) {
                lwcBlocks.add(block.getBlock());
            }
        }
        return lwcBlocks;
    }
}
