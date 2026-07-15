package dev.kitteh.factions.util;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

@ApiStatus.Internal
public class FlightUtil {

    private static FlightUtil instance;

    private EnemiesTask enemiesTask;

    private FlightUtil() {
        double enemyCheck = Confs.main().commands().fly().getRadiusCheck() * 20;
        if (enemyCheck > 0) {
            enemiesTask = new EnemiesTask();
            enemiesTask.runTaskTimer(AbstractFactionsPlugin.instance(), 0, (long) enemyCheck);
        }

        double spawnRate = Confs.main().commands().fly().particles().getSpawnRate() * 20;
        if (spawnRate > 0) {
            new ParticleTrailsTask().runTaskTimer(AbstractFactionsPlugin.instance(), 0, (long) spawnRate);
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
                    if (enemiesNearby(pilot, Confs.main().commands().fly().getEnemyRadius(), players)) {
                        pilot.sendRichMessage(Confs.tl().commands().fly().getEnemyDisable());
                        pilot.flying(false);
                        if (pilot.autoFlying()) {
                            pilot.autoFlying(false);
                            pilot.sendRichMessage(Confs.tl().commands().fly().getAuto(),
                                    Placeholder.unparsed("state", "disabled"));
                        }
                    }
                }
            }
        }

        public boolean enemiesNearby(FPlayer target, int radius) {
            return this.enemiesNearby(target, radius, FPlayers.fPlayers().online());
        }

        public boolean enemiesNearby(FPlayer target, int radius, Collection<FPlayer> players) {
            if (!WorldUtil.isEnabled(target.asPlayer())) {
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
            this.amount = Confs.main().commands().fly().particles().getAmount();
            this.speed = (float) Confs.main().commands().fly().particles().getSpeed();
        }

        @Override
        public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                FPlayer pilot = FPlayers.fPlayers().get(player);
                if (pilot.flying()) {
                    if (pilot.flyTrailEffect() != null && Permission.FLY_TRAILS.has(player) && pilot.flyTrail()) {
                        Particle effect = ParticleProvider.effectFromString(pilot.flyTrailEffect());
                        if (effect == null) {
                            return;
                        }
                        Collection<Player> viewers = player.getWorld().getPlayersSeeingChunk(player.getLocation().getChunk());
                        Class<?> dataType = effect.getDataType();
                        if (Confs.main().commands().fly().particles().isColorRelationally()) {
                            viewers.forEach(p -> p.spawnParticle(effect, player.getLocation(), amount, speed, 0, 0, 0,
                                    MiscUtil.colorToParticleColor(pilot.relationTo(FPlayers.fPlayers().get(p)).color(), dataType)));
                        } else {
                            Object color = MiscUtil.colorToParticleColor(Confs.main().commands().fly().particles().getColorARGB(), dataType);
                            viewers.forEach(p -> p.spawnParticle(effect, player.getLocation(), amount, speed, 0, 0, 0, color));
                        }
                    }
                }
            }
        }
    }
}
