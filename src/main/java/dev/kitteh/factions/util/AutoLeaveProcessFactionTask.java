package dev.kitteh.factions.util;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.permissible.Role;

import java.util.logging.Level;

public class AutoLeaveProcessFactionTask extends AutoLeaveTask.AutoLeaveProcessor<Faction> {
    public AutoLeaveProcessFactionTask() {
        this.iterator = Factions.getInstance().getAllFactions().listIterator();
    }

    @Override
    public void go(MainConfig conf) {
        Faction faction = iterator.next();

        if (faction.isPlayerFreeType()) {
            return;
        }

        if (faction.isWilderness()) {
            if (conf.factions().other().isAutoLeaveDeleteFPlayerData()) {
                for (FPlayer fplayer : faction.getFPlayers()) {
                    if (fplayer.isOffline() && now - fplayer.getLastLoginTime() > toleranceMillis) {
                        // Check if they should be exempt from this.
                        if (fplayer.isAutoLeaveExempt()) {
                            FactionsPlugin.getInstance().debug(Level.INFO, fplayer.getName() + " was going to be auto-removed but was set not to.");
                            continue;
                        }
                        if ((conf.logging().isFactionLeave() || conf.logging().isFactionKick()) && (fplayer.hasFaction() || conf.factions().other().isAutoLeaveDeleteFPlayerData())) {
                            FactionsPlugin.getInstance().log("Player " + fplayer.getName() + " was auto-removed due to inactivity.");
                        }
                        fplayer.remove();
                    }
                }
            }
            return;
        }

        for (FPlayer fplayer : faction.getFPlayers()) {
            if (fplayer.isOnline() || now - fplayer.getLastLoginTime() < toleranceMillis) {
                return; // At least one still active player!
            }
            if (fplayer.isAutoLeaveExempt()) {
                FactionsPlugin.getInstance().debug(Level.INFO, fplayer.getName() + " was going to be auto-removed but was set not to.");
                return; // Won't autoremove this faction due to this player
            }
        }

        // Every single one of them is inactive and removable!

        Role role = Role.getByValue(0);
        for (int i = 0; role != null; role = Role.getByValue(++i)) {
            for (FPlayer fplayer : faction.getFPlayersWhereRole(role)) {
                if ((conf.logging().isFactionLeave() || conf.logging().isFactionKick()) && (fplayer.hasFaction() || conf.factions().other().isAutoLeaveDeleteFPlayerData())) {
                    FactionsPlugin.getInstance().log("Player " + fplayer.getName() + " was auto-removed due to inactivity.");
                }
                fplayer.leave(false);
                if (conf.factions().other().isAutoLeaveDeleteFPlayerData()) {
                    fplayer.remove();
                }
            }
        }
    }
}
