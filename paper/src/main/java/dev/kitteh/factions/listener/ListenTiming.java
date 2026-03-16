package dev.kitteh.factions.listener;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.plugin.Instances;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import dev.kitteh.factions.upgrade.Upgrades;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ListenTiming implements Listener {
    public void start() {
        this.shieldStart();
    }

    @EventHandler
    public void onTick(ServerTickEndEvent event) {
        this.shieldTick();
    }

    private void shieldStart() {
        if (!Universe.universe().isUpgradeEnabled(Upgrades.SHIELD)) {
            return;
        }
        LocalTime now = LocalTime.now();
        LocalDateTime nowDT = LocalDateTime.now();

        LocalTime previous30 = LocalTime.now().withSecond(0).withNano(0);
        previous30 = previous30.withMinute(previous30.getMinute() >= 30 ? 30 : 0);

        LocalTime next = Universe.universe().shieldScheduleLastTimeChecked();

        List<Faction> eligible = new ArrayList<>();

        boolean today = true;
        LocalDateTime then = nowDT.withSecond(0).withNano(0).withHour(next.getHour()).withMinute(next.getMinute());
        if (now.isBefore(next)) {
            then = then.minusDays(1);
            today = false;
        }

        for (Faction faction : Factions.factions().all()) {
            int lvl;
            LocalTime scheduledTime;
            if (faction.shieldActive() || faction.shieldCooldownActive() ||
                    (scheduledTime = faction.shieldDailyScheduledTime()) == null ||
                    (lvl = faction.upgradeLevel(Upgrades.SHIELD)) == 0) {
                continue;
            }

            LocalDateTime scheduledDT;
            if (scheduledTime.isBefore(previous30)) {
                scheduledDT = nowDT.withHour(scheduledTime.getHour()).withMinute(scheduledTime.getMinute());
            } else if (today) {
                continue;
            } else {
                scheduledDT = nowDT.minusDays(1).withHour(scheduledTime.getHour()).withMinute(scheduledTime.getMinute());
            }
            if (then.isAfter(scheduledDT)) {
                continue;
            }

            UpgradeSettings settings = Universe.universe().upgradeSettings(Upgrades.SHIELD);
            long duration = settings.valueAt(Upgrades.Variables.DURATION, lvl).longValue();
            long cooldown = settings.valueAt(Upgrades.Variables.COOLDOWN, lvl).longValue();

            scheduledDT.atZone(ZoneId.systemDefault()).toInstant();
        }

        while (now.isBefore(next) || next.isBefore(previous30)) {
            Instances.UNIVERSE.shieldScheduleBumpNextTime();
            next = Universe.universe().shieldScheduleLastTimeChecked();
        }
    }

    private void shieldTick() {
        if (!Universe.universe().isUpgradeEnabled(Upgrades.SHIELD)) {
            return;
        }
    }
}
