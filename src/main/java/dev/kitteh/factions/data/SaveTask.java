package dev.kitteh.factions.data;

import dev.kitteh.factions.*;

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
        ((MemoryFPlayers) FPlayers.fPlayers()).forceSave(false);
        ((MemoryFactions) Factions.factions()).forceSave(false);
        ((MemoryBoard) Board.board()).forceSave(false);
        ((MemoryUniverse) Universe.universe()).forceSave(false);
        running = false;
    }
}
