package dev.kitteh.factions.util;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.Random;

// -----------------------------
// Smoke Directions 
// -----------------------------
// Direction ID    Direction
//            0    South - East
//            1    South
//            2    South - West
//            3    East
//            4    (Up or middle ?)
//            5    West
//            6    North - East
//            7    North
//            8    North - West
//-----------------------------

@ApiStatus.Internal
public class SmokeUtil {

    public static final Random random = new Random();

    public static void spawnSingle(Location location, int direction) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        location.getWorld().playEffect(location.clone(), Effect.SMOKE, direction);
    }

    public static void spawnSingleRandom(Location location) {
        spawnSingle(location, random.nextInt(9));
    }

    public static void spawnCloudRandom(Location location, float thickness) {
        int singles = (int) Math.floor(thickness * 9);
        for (int i = 0; i < singles; i++) {
            spawnSingleRandom(location.clone());
        }
    }

    public static void spawnCloudRandom(Collection<Location> locations, float thickness) {
        for (Location location : locations) {
            spawnCloudRandom(location, thickness);
        }
    }
}
