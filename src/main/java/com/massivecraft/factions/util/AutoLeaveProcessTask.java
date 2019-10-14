package com.massivecraft.factions.util;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Role;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Level;

public class AutoLeaveProcessTask extends BukkitRunnable {

    private transient boolean readyToGo;
    private transient boolean finished;
    private transient ListIterator<FPlayer> iterator;
    private transient double toleranceMillis;

    public AutoLeaveProcessTask() {
        ArrayList<FPlayer> fplayers = (ArrayList<FPlayer>) FPlayers.getInstance().getAllFPlayers();
        this.iterator = fplayers.listIterator();
        this.toleranceMillis = FactionsPlugin.getInstance().conf().factions().other().getAutoLeaveAfterDaysOfInactivity() * 24 * 60 * 60 * 1000;
        this.readyToGo = true;
        this.finished = false;
    }

    public void run() {
        if (FactionsPlugin.getInstance().conf().factions().other().getAutoLeaveAfterDaysOfInactivity() <= 0.0 || FactionsPlugin.getInstance().conf().factions().other().getAutoLeaveRoutineMaxMillisecondsPerTick() <= 0.0) {
            this.stop();
            return;
        }

        if (!readyToGo) {
            return;
        }
        // this is set so it only does one iteration at a time, no matter how frequently the timer fires
        readyToGo = false;
        // and this is tracked to keep one iteration from dragging on too long and possibly choking the system if there are a very large number of players to go through
        long loopStartTime = System.currentTimeMillis();

        while (iterator.hasNext()) {
            long now = System.currentTimeMillis();

            // if this iteration has been running for maximum time, stop to take a breather until next tick
            if (now > loopStartTime + FactionsPlugin.getInstance().conf().factions().other().getAutoLeaveRoutineMaxMillisecondsPerTick()) {
                readyToGo = true;
                return;
            }

            FPlayer fplayer = iterator.next();

            // Check if they should be exempt from this.
            if (!fplayer.willAutoLeave()) {
                FactionsPlugin.getInstance().debug(Level.INFO, fplayer.getName() + " was going to be auto-removed but was set not to.");
                continue;
            }

            if (fplayer.isOffline() && now - fplayer.getLastLoginTime() > toleranceMillis) {
                if (FactionsPlugin.getInstance().conf().logging().isFactionLeave() || FactionsPlugin.getInstance().conf().logging().isFactionKick()) {
                    FactionsPlugin.getInstance().log("Player " + fplayer.getName() + " was auto-removed due to inactivity.");
                }

                // if player is faction admin, sort out the faction since he's going away
                if (fplayer.getRole() == Role.ADMIN) {
                    Faction faction = fplayer.getFaction();
                    if (faction != null) {
                        fplayer.getFaction().promoteNewLeader();
                    }
                }

                fplayer.leave(false);
                iterator.remove();  // go ahead and remove this list's link to the FPlayer object
                if (FactionsPlugin.getInstance().conf().factions().other().isAutoLeaveDeleteFPlayerData()) {
                    fplayer.remove();
                }
            }
        }

        // looks like we've finished
        this.stop();
    }

    // we're done, shut down
    public void stop() {
        readyToGo = false;
        finished = true;

        this.cancel();
    }

    public boolean isFinished() {
        return finished;
    }
}
