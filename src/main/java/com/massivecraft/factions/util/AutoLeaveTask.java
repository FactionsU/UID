package com.massivecraft.factions.util;

import com.massivecraft.factions.P;

public class AutoLeaveTask implements Runnable {

    private static AutoLeaveProcessTask task;
    double rate;

    public AutoLeaveTask() {
        this.rate = P.getInstance().conf().factions().getAutoLeaveRoutineRunsEveryXMinutes();
    }

    public synchronized void run() {
        if (task != null && !task.isFinished()) {
            return;
        }

        task = new AutoLeaveProcessTask();
        task.runTaskTimer(P.p, 1, 1);

        // maybe setting has been changed? if so, restart this task at new rate
        if (this.rate != P.getInstance().conf().factions().getAutoLeaveRoutineRunsEveryXMinutes()) {
            P.getInstance().startAutoLeaveTask(true);
        }
    }
}
