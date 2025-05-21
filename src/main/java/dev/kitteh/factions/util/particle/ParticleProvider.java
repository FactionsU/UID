package dev.kitteh.factions.util.particle;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class ParticleProvider {
    private ParticleProvider() {
    }

    private static final Particle fallback = Particle.DUST;

    public static void spawn(Particle effect, Location location, int count) {
        location.getWorld().spawnParticle(effect, location, count);
    }

    public static void spawn(Player player, Particle effect, Location location, int count) {
        player.spawnParticle(effect, location, count);
    }

    public static void spawn(Particle particle, Location location, int count, double speed, double offsetX, double offsetY, double offsetZ) {
        location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
    }

    public static void spawn(Player player, Particle particle, Location location, int count, double speed, double offsetX, double offsetY, double offsetZ) {
        player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
    }

    public static void spawn(Particle particle, Location location, Color color) {
        if (particle.getDataType().equals(Particle.DustOptions.class)) {
            location.getWorld().spawnParticle(particle, location, 1, new Particle.DustOptions(color, 1));
        } else if (particle.getDataType() == Void.class) {
            location.getWorld().spawnParticle(particle, location, 1, null);
        }
    }

    public static void spawn(Player player, Particle particle, Location location, Color color) {
        if (particle.getDataType().equals(Particle.DustOptions.class)) {
            player.spawnParticle(particle, location, 1, new Particle.DustOptions(color, 1.5f));
        } else if (particle.getDataType() == Void.class) {
            player.spawnParticle(particle, location, 1, null);
        }
    }

    public static Particle effectFromString(String string) {
        for (Particle particle : Particle.values()) {
            if (particle.name().equalsIgnoreCase(string)) {
                return particle;
            }
        }
        return fallback;
    }
}
