package dev.kitteh.factions.util;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.logging.Level;

/*
 * reference diagram, task should move in this pattern out from chunk 0 in the center.
 *  8 [>][>][>][>][>] etc.
 * [^][6][>][>][>][>][>][6]
 * [^][^][4][>][>][>][4][v]
 * [^][^][^][2][>][2][v][v]
 * [^][^][^][^][0][v][v][v]
 * [^][^][^][1][1][v][v][v]
 * [^][^][3][<][<][3][v][v]
 * [^][5][<][<][<][<][5][v]
 * [7][<][<][<][<][<][<][7]
 */

public abstract class SpiralTask implements Runnable {

    // general task-related reference data
    private final World world;
    private boolean readyToGo = false;
    private int taskID = -1;
    private final int limit;

    // values for the spiral pattern routine
    private int x = 0;
    private int z = 0;
    private boolean isZLeg = false;
    private boolean isNeg = false;
    private int length = -1;
    private int current = 0;

    @SuppressWarnings("this-escape")
    public SpiralTask(FLocation fLocation, int radius) {
        // limit is determined based on spiral leg length for given radius; see insideRadius()
        this.limit = (radius - 1) * 2;

        this.world = Bukkit.getWorld(fLocation.worldName());
        if (this.world == null) {
            AbstractFactionsPlugin.instance().log(Level.WARNING, "[SpiralTask] A valid world must be specified!");
            this.stop();
            return;
        }

        this.x = fLocation.x();
        this.z = fLocation.z();

        this.readyToGo = true;

        // get this party started
        this.setTaskID(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(AbstractFactionsPlugin.instance(), this, 2, 2));
    }

    /*
     * This is where the necessary work is done; you'll need to override this method with whatever you want
     * done at each chunk in the spiral pattern.
     * Return false if the entire task needs to be aborted, otherwise return true to continue.
     */
    public abstract boolean work();

    /*
     * Returns an FLocation pointing at the current chunk X and Z values.
     */
    public final FLocation currentFLocation() {
        return new FLocation(world.getName(), x, z);
    }

    /*
     * Returns a Location pointing at the current chunk X and Z values.
     * note that the Location is at the corner of the chunk, not the center.
     */
    public final Location currentLocation() {
        return new Location(world, FLocation.chunkToBlock(x), 65.0, FLocation.chunkToBlock(z));
    }

    /*
     * Below are the guts of the class, which you normally wouldn't need to mess with.
     */

    private void setTaskID(int ID) {
        if (ID == -1) {
            this.stop();
        }
        taskID = ID;
    }

    @Override
    public final void run() {
        if (!this.valid() || !readyToGo) {
            return;
        }

        // this is set so it only does one iteration at a time, no matter how frequently the timer fires
        readyToGo = false;

        // make sure we're still inside the specified radius
        if (!this.insideRadius()) {
            return;
        }

        // track this to keep one iteration from dragging on too long and possibly choking the system
        long loopStartTime = now();

        // keep going until the task has been running for 20ms or more, then stop to take a breather
        while (now() < loopStartTime + 20) {
            // run the primary task on the current X/Z coordinates
            if (!this.work()) {
                this.finish();
                return;
            }

            // move on to next chunk in spiral
            if (!this.moveToNext()) {
                return;
            }
        }

        // ready for the next iteration to run
        readyToGo = true;
    }

    // step through chunks in spiral pattern from center; returns false if we're done, otherwise returns true
    public final boolean moveToNext() {
        if (!this.valid()) {
            return false;
        }

        // make sure we don't need to turn down the next leg of the spiral
        if (current < length) {
            current++;

            // if we're outside the radius, we're done
            if (!this.insideRadius()) {
                return false;
            }
        } else {    // one leg/side of the spiral down...
            current = 0;
            isZLeg ^= true;
            // every second leg (between X and Z legs, negative or positive), length increases
            if (isZLeg) {
                isNeg ^= true;
                length++;
            }
        }

        // move one chunk further in the appropriate direction
        if (isZLeg) {
            z += (isNeg) ? -1 : 1;
        } else {
            x += (isNeg) ? -1 : 1;
        }

        return true;
    }

    public final boolean insideRadius() {
        boolean inside = current < limit;
        if (!inside) {
            this.finish();
        }
        return inside;
    }

    // for successful completion
    public void finish() {
        this.stop();
    }

    // we're done, whether finished or cancelled
    public final void stop() {
        if (!this.valid()) {
            return;
        }

        readyToGo = false;
        Bukkit.getServer().getScheduler().cancelTask(taskID);
        taskID = -1;
    }

    // is this task still valid/workable?
    public final boolean valid() {
        return taskID != -1;
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}
