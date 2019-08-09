package com.massivecraft.factions.data;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.P;

public class SaveTask implements Runnable {

    private static boolean running = false;

    private P p;

    public SaveTask(P p) {
        this.p = p;
    }

    public void run() {
        if (!p.getAutoSave() || running) {
            return;
        }
        running = true;
        Factions.getInstance().forceSave(false);
        FPlayers.getInstance().forceSave(false);
        Board.getInstance().forceSave(false);
        running = false;
    }
}
