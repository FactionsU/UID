package dev.kitteh.factions.util;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.particle.ParticleProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class FlightUtil {

    private static FlightUtil instance;

    private EnemiesTask enemiesTask;

    private FlightUtil() {
        double enemyCheck = FactionsPlugin.instance().conf().commands().fly().getRadiusCheck() * 20;
        if (enemyCheck > 0) {
            enemiesTask = new EnemiesTask();
            enemiesTask.runTaskTimer(AbstractFactionsPlugin.getInstance(), 0, (long) enemyCheck);
        }

        double spawnRate = FactionsPlugin.instance().conf().commands().fly().particles().getSpawnRate() * 20;
        if (spawnRate > 0) {
            new ParticleTrailsTask().runTaskTimer(AbstractFactionsPlugin.getInstance(), 0, (long) spawnRate);
        }
    }

    public static void start() {
        instance = new FlightUtil();
    }

    public static FlightUtil instance() {
        return instance;
    }

    public boolean enemiesNearby(FPlayer target, int radius) {
        if (this.enemiesTask == null) {
            return false;
        } else {
            return this.enemiesTask.enemiesNearby(target, radius);
        }
    }

    public static class EnemiesTask extends BukkitRunnable {

        @Override
        public void run() {
            Collection<FPlayer> players = FPlayers.fPlayers().online();
            for (Player player : Bukkit.getOnlinePlayers()) {
                FPlayer pilot = FPlayers.fPlayers().get(player);
                if (pilot.flying() && !pilot.adminBypass()) {
                    if (enemiesNearby(pilot, FactionsPlugin.instance().conf().commands().fly().getEnemyRadius(), players)) {
                        pilot.msg(TL.COMMAND_FLY_ENEMY_DISABLE);
                        pilot.flying(false);
                        if (pilot.autoFlying()) {
                            pilot.autoFlying(false);
                        }
                    }
                }
            }
        }

        public boolean enemiesNearby(FPlayer target, int radius) {
            return this.enemiesNearby(target, radius, FPlayers.fPlayers().online());
        }

        public boolean enemiesNearby(FPlayer target, int radius, Collection<FPlayer> players) {
            if (!WorldUtil.isEnabled(target.asPlayer().getWorld())) {
                return false;
            }
            int radiusSquared = radius * radius;
            Location loc = target.asPlayer().getLocation();
            Location cur = new Location(loc.getWorld(), 0, 0, 0);
            for (FPlayer player : players) {
                if (player == target || player.adminBypass()) {
                    continue;
                }

                player.asPlayer().getLocation(cur);
                if (cur.getWorld().getUID().equals(loc.getWorld().getUID()) &&
                        cur.distanceSquared(loc) <= radiusSquared &&
                        player.relationTo(target) == Relation.ENEMY &&
                        target.asPlayer().canSee(player.asPlayer())) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class ParticleTrailsTask extends BukkitRunnable {

        private final int amount;
        private final float speed;

        private ParticleTrailsTask() {
            this.amount = FactionsPlugin.instance().conf().commands().fly().particles().getAmount();
            this.speed = (float) FactionsPlugin.instance().conf().commands().fly().particles().getSpeed();
        }

        @Override
        public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                FPlayer pilot = FPlayers.fPlayers().get(player);
                if (pilot.flying()) {
                    if (pilot.flyTrailEffect() != null && Permission.FLY_TRAILS.has(player) && pilot.flyTrail()) {
                        Particle effect = ParticleProvider.effectFromString(pilot.flyTrailEffect());
                        FactionsPlugin.instance().particleProvider().spawn(effect, player.getLocation(), amount, speed, 0, 0, 0);
                    }
                }
            }
        }

    }

}
