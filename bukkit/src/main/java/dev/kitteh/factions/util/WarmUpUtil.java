package dev.kitteh.factions.util;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@ApiStatus.Internal
@NullMarked
public class WarmUpUtil {
    @Deprecated(forRemoval = true, since = "4.5.0")
    public static void process(final FPlayer player, Warmup warmup, String message, final Runnable runnable, long delay) {
        Player plr = player.asPlayer();
        if (plr != null && Permission.WARMUP_EXEMPT.has(plr)) {
            delay = 0;
        }
        if (delay > 0) {
            player.msgLegacy(message);
            int id = new BukkitRunnable() {
                @Override
                public void run() {
                    player.cancelWarmup();
                    runnable.run();
                }
            }.runTaskLater(AbstractFactionsPlugin.instance(), delay * 20).getTaskId();
            player.addWarmup(warmup, id);
        } else {
            runnable.run();
        }
    }

    public static void process(final FPlayer player, Warmup warmup, Component message, final Runnable runnable, long delay) {
        Player plr = player.asPlayer();
        if (plr != null && Permission.WARMUP_EXEMPT.has(plr)) {
            delay = 0;
        }
        if (delay > 0) {
            player.sendMessage(message);
            int id = new BukkitRunnable() {
                @Override
                public void run() {
                    player.cancelWarmup();
                    runnable.run();
                }
            }.runTaskLater(AbstractFactionsPlugin.instance(), delay * 20).getTaskId();
            player.addWarmup(warmup, id);
        } else {
            runnable.run();
        }
    }

    public enum Warmup {
        HOME, WARP, FLIGHT, STUCK
    }
}
