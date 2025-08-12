package dev.kitteh.factions.data;

import dev.kitteh.factions.*;
import dev.kitteh.factions.plugin.Instances;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class SaveTask implements Runnable {
    private static boolean running = false;

    private final FactionsPlugin plugin;

    public SaveTask(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.autoSave() || running) {
            return;
        }
        running = true;
        Instances.PLAYERS.forceSave(false);
        Instances.FACTIONS.forceSave(false);
        Instances.BOARD.forceSave(false);
        Instances.UNIVERSE.forceSave(false);
        running = false;
    }
}
