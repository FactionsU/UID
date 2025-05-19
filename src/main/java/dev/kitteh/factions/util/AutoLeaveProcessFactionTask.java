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
        this.iterator = Factions.factions().all().listIterator();
    }

    @Override
    public void go(MainConfig conf) {
        Faction faction = iterator.next();

        if (faction.isSafeZone() || faction.isWarZone()) {
            return;
        }

        if (faction.isWilderness()) {
            if (conf.factions().other().isAutoLeaveDeleteFPlayerData()) {
                for (FPlayer fplayer : faction.members()) {
                    if (!fplayer.isOnline() && now - fplayer.lastLogin() > toleranceMillis) {
                        // Check if they should be exempt from this.
                        if (fplayer.autoLeaveExempt()) {
                            FactionsPlugin.instance().debug(Level.INFO, fplayer.name() + " was going to be auto-removed but was set not to.");
                            continue;
                        }
                        if ((conf.logging().isFactionLeave() || conf.logging().isFactionKick()) && (fplayer.hasFaction() || conf.factions().other().isAutoLeaveDeleteFPlayerData())) {
                            FactionsPlugin.instance().log("Player " + fplayer.name() + " was auto-removed due to inactivity.");
                        }
                        fplayer.eraseData();
                    }
                }
            }
            return;
        }

        for (FPlayer fplayer : faction.members()) {
            if (fplayer.isOnline() || now - fplayer.lastLogin() < toleranceMillis) {
                return; // At least one still active player!
            }
            if (fplayer.autoLeaveExempt()) {
                FactionsPlugin.instance().debug(Level.INFO, fplayer.name() + " was going to be auto-removed but was set not to.");
                return; // Won't autoremove this faction due to this player
            }
        }

        // Every single one of them is inactive and removable!

        Role role = Role.getByValue(0);
        for (int i = 0; role != null; role = Role.getByValue(++i)) {
            for (FPlayer fplayer : faction.members(role)) {
                if ((conf.logging().isFactionLeave() || conf.logging().isFactionKick()) && (fplayer.hasFaction() || conf.factions().other().isAutoLeaveDeleteFPlayerData())) {
                    FactionsPlugin.instance().log("Player " + fplayer.name() + " was auto-removed due to inactivity.");
                }
                fplayer.leave(false);
                if (conf.factions().other().isAutoLeaveDeleteFPlayerData()) {
                    fplayer.eraseData();
                }
            }
        }
    }
}
