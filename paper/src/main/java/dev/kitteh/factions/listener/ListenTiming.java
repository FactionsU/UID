package dev.kitteh.factions.listener;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.plugin.Instances;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import dev.kitteh.factions.upgrade.Upgrades;
import dev.kitteh.factions.util.MiscUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public class ListenTiming implements Listener {
    private LocalTime lastCheckedBucket;

    public void start() {
        this.shieldCheck();
    }

    @EventHandler
    public void onTick(ServerTickEndEvent event) {
        this.shieldCheck();
    }

    private void shieldCheck() {
        if (!Universe.universe().isUpgradeEnabled(Upgrades.SHIELD)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalTime current = MiscUtil.floorToHalfHour(now.toLocalTime());

        if (current.equals(this.lastCheckedBucket)) {
            return;
        }
        this.lastCheckedBucket = current;

        LocalTime cursor = Universe.universe().shieldScheduleLastTimeChecked();
        if (cursor == null) {
            return;
        }

        while (!cursor.equals(current)) {
            Instances.UNIVERSE.shieldScheduleBumpNextTime();
            cursor = Universe.universe().shieldScheduleLastTimeChecked();
            this.activateScheduled(cursor, now);
        }
    }

    private void activateScheduled(LocalTime bucket, LocalDateTime now) {
        UpgradeSettings settings = Universe.universe().upgradeSettings(Upgrades.SHIELD);
        if (settings == null) {
            return;
        }

        LocalDateTime scheduledDT = now.withHour(bucket.getHour()).withMinute(bucket.getMinute()).withSecond(0).withNano(0);
        if (scheduledDT.isAfter(now)) {
            scheduledDT = scheduledDT.minusDays(1);
        }
        Instant start = scheduledDT.atZone(ZoneId.systemDefault()).toInstant();
        Instant nowInstant = now.atZone(ZoneId.systemDefault()).toInstant();

        for (Faction faction : Factions.factions().all()) {
            LocalTime scheduled = faction.shieldDailyScheduledTime();
            if (scheduled == null || !scheduled.equals(bucket)) {
                continue;
            }
            if (faction.shieldActive() || faction.shieldCooldownActive()) {
                continue;
            }
            int lvl = faction.upgradeLevel(Upgrades.SHIELD);
            if (lvl == 0) {
                continue;
            }

            Duration duration = Duration.ofSeconds(settings.valueAt(Upgrades.Variables.DURATION, lvl).longValue());
            Duration cooldown = Duration.ofSeconds(settings.valueAt(Upgrades.Variables.COOLDOWN, lvl).longValue());

            if (!start.plus(duration).isAfter(nowInstant)) {
                continue;
            }

            faction.shield(start, duration, cooldown);

            faction.sendRichMessage(
                    FactionsPlugin.instance().tl().commands().shield().getActivatedScheduled(),
                    Placeholder.unparsed("duration", MiscUtil.durationString(faction.shieldRemaining()))
            );
        }
    }
}
