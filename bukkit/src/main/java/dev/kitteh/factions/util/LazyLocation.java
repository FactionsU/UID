package dev.kitteh.factions.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jspecify.annotations.NullMarked;

/**
 * This class provides a lazy-load Location, so that World doesn't need to be initialized
 * yet when an object of this class is created, only when the Location is first accessed.
 */
@NullMarked
public record LazyLocation(
        String worldName,
        double x,
        double y,
        double z,
        float pitch,
        float yaw) {
    public LazyLocation(Location loc) {
        this(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getPitch(), loc.getYaw());
    }

    public Location asLocation() {
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }
}
