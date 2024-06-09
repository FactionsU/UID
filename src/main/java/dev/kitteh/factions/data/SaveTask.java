package dev.kitteh.factions.data;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;

public class SaveTask implements Runnable {

    private static boolean running = false;

    private final FactionsPlugin plugin;

    public SaveTask(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    public void run() {
        if (!plugin.getAutoSave() || running) {
            return;
        }
        running = true;
        Factions.getInstance().forceSave(false);
        FPlayers.getInstance().forceSave(false);
        Board.getInstance().forceSave(false);
        running = false;
    }
}
