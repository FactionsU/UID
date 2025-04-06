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

    @Override
    public void run() {
        if (!plugin.getAutoSave() || running) {
            return;
        }
        running = true;
        ((MemoryFPlayers) FPlayers.getInstance()).forceSave(false);
        ((MemoryFactions) Factions.getInstance()).forceSave(false);
        ((MemoryBoard) Board.getInstance()).forceSave(false);
        running = false;
    }
}
