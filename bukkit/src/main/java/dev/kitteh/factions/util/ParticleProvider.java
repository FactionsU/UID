package dev.kitteh.factions.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ParticleProvider {
    private static final Particle fallback = Particle.DUST;

    public static Particle effectFromString(String string) {
        for (Particle particle : Particle.values()) {
            if (particle.name().equalsIgnoreCase(string)) {
                return particle;
            }
        }
        return fallback;
    }

    private ParticleProvider() {
    }

    @Deprecated(forRemoval = true, since = "4.6.1")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    public static void spawn(Particle effect, Location location, int count) {
        location.getWorld().spawnParticle(effect, location, count);
    }

    @Deprecated(forRemoval = true, since = "4.6.1")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    public static void spawn(Player player, Particle effect, Location location, int count) {
        player.spawnParticle(effect, location, count);
    }

    @Deprecated(forRemoval = true, since = "4.6.1")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    public static void spawn(Particle particle, Location location, int count, double speed, double offsetX, double offsetY, double offsetZ) {
        location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
    }

    @Deprecated(forRemoval = true, since = "4.6.1")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    public static void spawn(Player player, Particle particle, Location location, int count, double speed, double offsetX, double offsetY, double offsetZ) {
        player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
    }

    @Deprecated(forRemoval = true, since = "4.6.1")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    public static void spawn(Particle particle, Location location, Color color) {
        if (particle.getDataType().equals(Particle.DustOptions.class)) {
            location.getWorld().spawnParticle(particle, location, 1, new Particle.DustOptions(color, 1));
        } else if (particle.getDataType() == Void.class) {
            location.getWorld().spawnParticle(particle, location, 1, null);
        }
    }

    @Deprecated(forRemoval = true, since = "4.6.1")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    public static void spawn(Player player, Particle particle, Location location, Color color) {
        if (particle.getDataType().equals(Particle.DustOptions.class)) {
            player.spawnParticle(particle, location, 1, new Particle.DustOptions(color, 1.5f));
        } else if (particle.getDataType().equals(Color.class)) {
            player.spawnParticle(particle, location, 1, color);
        } else if (particle.getDataType() == Void.class) {
            player.spawnParticle(particle, location, 1, null);
        }
    }
}
