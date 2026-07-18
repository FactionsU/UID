package dev.kitteh.factions.listener;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.plugin.Instances;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ListenChunks implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void load(ChunkLoadEvent event) {
        Instances.BOARD.cachedInhabitedTime(new FLocation(event.getChunk()), event.getChunk().getInhabitedTime());
    }
}
