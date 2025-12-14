package dev.kitteh.factions.util;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.permissible.Selectable;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.ApiStatus;

import java.util.ListIterator;

@ApiStatus.Internal
public class AutoLeaveTask implements Runnable {
    private static AutoLeaveProcessor<?> task;
    private final double rate;
    private final boolean factions;

    public AutoLeaveTask() {
        this.rate = FactionsPlugin.instance().conf().factions().other().getAutoLeaveRoutineRunsEveryXMinutes();
        this.factions = FactionsPlugin.instance().conf().factions().other().isAutoLeaveOnlyEntireFactionInactive();
    }

    @Override
    public synchronized void run() {
        if (task != null && !task.isFinished()) {
            return;
        }

        task = this.factions ? new AutoLeaveProcessFactionTask() : new AutoLeaveProcessTask();
        task.runTaskTimer(AbstractFactionsPlugin.instance(), 1, 1);

        // maybe setting has been changed? if so, restart this task at new rate
        if (this.rate != FactionsPlugin.instance().conf().factions().other().getAutoLeaveRoutineRunsEveryXMinutes() ||
                this.factions != FactionsPlugin.instance().conf().factions().other().isAutoLeaveOnlyEntireFactionInactive()) {
            AbstractFactionsPlugin.instance().startAutoLeaveTask(true);
        }
    }

    public static abstract class AutoLeaveProcessor<T extends Selectable> extends BukkitRunnable {
        protected transient boolean readyToGo = true;
        protected transient boolean finished;
        protected transient ListIterator<T> iterator;
        protected final transient double toleranceMillis = FactionsPlugin.instance().conf().factions().other().getAutoLeaveAfterDaysOfInactivity() * 24 * 60 * 60 * 1000;
        protected long now;

        // we're done, shut down
        public void stop() {
            readyToGo = false;
            finished = true;

            this.cancel();
        }

        @Override
        public final void run() {
            MainConfig conf = FactionsPlugin.instance().conf();
            if (conf.factions().other().getAutoLeaveAfterDaysOfInactivity() <= 0.0 || conf.factions().other().getAutoLeaveRoutineMaxMillisecondsPerTick() <= 0) {
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
                this.now = System.currentTimeMillis();

                // if this iteration has been running for maximum time, stop to take a breather until next tick
                if (now > loopStartTime + conf.factions().other().getAutoLeaveRoutineMaxMillisecondsPerTick()) {
                    readyToGo = true;
                    return;
                }
                this.go(conf);
            }
            // looks like we've finished
            this.stop();
        }

        abstract void go(MainConfig conf);

        public boolean isFinished() {
            return finished;
        }
    }
}
