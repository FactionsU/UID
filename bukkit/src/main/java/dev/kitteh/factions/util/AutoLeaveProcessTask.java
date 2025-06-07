package dev.kitteh.factions.util;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;

import java.util.ArrayList;
import java.util.logging.Level;

public class AutoLeaveProcessTask extends AutoLeaveTask.AutoLeaveProcessor<FPlayer> {
    public AutoLeaveProcessTask() {
        this.iterator = ((ArrayList<FPlayer>) FPlayers.fPlayers().all()).listIterator();
    }

    @Override
    public void go(MainConfig conf) {
        FPlayer fplayer = iterator.next();

        if (!fplayer.isOnline() && now - fplayer.lastLogin() > toleranceMillis) {
            if (fplayer.autoLeaveExempt()) {
                AbstractFactionsPlugin.instance().debug(Level.INFO, fplayer.name() + " was going to be auto-removed but was set not to.");
                return;
            }
            if ((conf.logging().isFactionLeave() || conf.logging().isFactionKick()) && (fplayer.hasFaction() || conf.factions().other().isAutoLeaveDeleteFPlayerData())) {
                AbstractFactionsPlugin.instance().log("Player " + fplayer.name() + " was auto-removed due to inactivity.");
            }

            // if player is faction admin, sort out the faction since he's going away
            if (fplayer.role() == Role.ADMIN) {
                Faction faction = fplayer.faction();
                if (faction != null) {
                    fplayer.faction().promoteNewLeader();
                }
            }

            fplayer.leave(false);
            iterator.remove();  // go ahead and remove this list's link to the FPlayer object
            if (conf.factions().other().isAutoLeaveDeleteFPlayerData()) {
                fplayer.eraseData();
            }
        }
    }
}
