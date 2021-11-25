package com.massivecraft.factions.util;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.file.MainConfig;
import com.massivecraft.factions.perms.Role;

import java.util.ArrayList;
import java.util.logging.Level;

public class AutoLeaveProcessTask extends AutoLeaveTask.AutoLeaveProcessor<FPlayer> {
    public AutoLeaveProcessTask() {
        this.iterator = ((ArrayList<FPlayer>) FPlayers.getInstance().getAllFPlayers()).listIterator();
    }

    public void go(MainConfig conf) {
        FPlayer fplayer = iterator.next();

        if (fplayer.isOffline() && now - fplayer.getLastLoginTime() > toleranceMillis) {
            if (!fplayer.willAutoLeave()) {
                FactionsPlugin.getInstance().debug(Level.INFO, fplayer.getName() + " was going to be auto-removed but was set not to.");
                return;
            }
            if ((conf.logging().isFactionLeave() || conf.logging().isFactionKick()) && (fplayer.hasFaction() || conf.factions().other().isAutoLeaveDeleteFPlayerData())) {
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
            if (conf.factions().other().isAutoLeaveDeleteFPlayerData()) {
                fplayer.remove();
            }
        }
    }
}
