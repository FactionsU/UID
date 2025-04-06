package dev.kitteh.factions.util;

import dev.kitteh.factions.FLocation;
import org.jspecify.annotations.NullMarked;

/**
 * Simple two-ints-in-a-long Morton code.
 */
@NullMarked
public final class Morton {
    public static long get(FLocation location) {
        return Morton.get(location.x(), location.z());
    }

    /**
     * Gets a Morton code for the given coordinates.
     *
     * @param x x coordinate
     * @param z z coordinate
     * @return Morton code for the coordinates
     */
    public static long get(int x, int z) {
        return (Morton.spreadOut(z) << 1) + Morton.spreadOut(x);
    }

    /**
     * Gets the X value from a given Morton code.
     *
     * @param mortonCode Morton code
     * @return x coordinate
     */
    public static int getX(long mortonCode) {
        return Morton.comeTogether(mortonCode);
    }

    /**
     * Gets the Z value from a given Morton code.
     *
     * @param mortonCode Morton code
     * @return z coordinate
     */
    public static int getZ(long mortonCode) {
        return Morton.comeTogether(mortonCode >> 1);
    }

    private static long spreadOut(long l) {
        l &= 0x00000000FFFFFFFFL;
        l = (l | (l << 16)) & 0x0000FFFF0000FFFFL;
        l = (l | (l << 8)) & 0x00FF00FF00FF00FFL;
        l = (l | (l << 4)) & 0x0F0F0F0F0F0F0F0FL;
        l = (l | (l << 2)) & 0x3333333333333333L;
        l = (l | (l << 1)) & 0x5555555555555555L;
        return l;
    }

    private static int comeTogether(long l) {
        l = l & 0x5555555555555555L;
        l = (l | (l >> 1)) & 0x3333333333333333L;
        l = (l | (l >> 2)) & 0x0F0F0F0F0F0F0F0FL;
        l = (l | (l >> 4)) & 0x00FF00FF00FF00FFL;
        l = (l | (l >> 8)) & 0x0000FFFF0000FFFFL;
        l = (l | (l >> 16)) & 0x00000000FFFFFFFFL;
        return (int) l;
    }
}