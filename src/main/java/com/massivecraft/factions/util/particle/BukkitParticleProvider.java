package com.massivecraft.factions.util.particle;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class BukkitParticleProvider {
    private final Particle fallback = Particle.REDSTONE;

    public void spawn(Particle effect, Location location, int count) {
        location.getWorld().spawnParticle(effect, location, count);
    }

    public void playerSpawn(Player player, Particle effect, Location location, int count) {
        player.spawnParticle(effect, location, count);
    }

    public void spawn(Particle particle, Location location, int count, double speed, double offsetX, double offsetY, double offsetZ) {
        location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
    }

    public void playerSpawn(Player player, Particle particle, Location location, int count, double speed, double offsetX, double offsetY, double offsetZ) {
        player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
    }

    public void spawn(Particle particle, Location location, ParticleColor color) {
        if (particle.getDataType().equals(Particle.DustOptions.class)) {
            location.getWorld().spawnParticle(particle, location, 1, new Particle.DustOptions(color.getColor(), 1));
        } else if (particle.getDataType()==Void.class){
            location.getWorld().spawnParticle(particle, location, 1, null);
        }
    }

    public void playerSpawn(Player player, Particle particle, Location location, ParticleColor color) {
        if (particle.getDataType().equals(Particle.DustOptions.class)) {
            player.spawnParticle(particle, location, 1, new Particle.DustOptions(color.getColor(), 1.5f));
        } else if (particle.getDataType()==Void.class){
            player.spawnParticle(particle, location, 1, null);
        }
    }

    public Particle effectFromString(String string) {
        for (Particle particle : Particle.values()) {
            if (particle.name().equalsIgnoreCase(string)) {
                return particle;
            }
        }
        return fallback;
    }

    public String effectName(Particle particle) {
        return particle.name();
    }
}
