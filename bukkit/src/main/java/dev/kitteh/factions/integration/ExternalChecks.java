package dev.kitteh.factions.integration;

import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public final class ExternalChecks {
    @FunctionalInterface
    public interface BoolFunction {
        boolean apply(Player player);
    }

    @FunctionalInterface
    public interface BoolBiFunction {
        boolean apply(Player player1, Player player2);
    }

    private record SingleCheck(Plugin plugin, BoolFunction function) {
    }

    private record DoubleCheck(Plugin plugin, BoolBiFunction function) {
    }

    private static final List<SingleCheck> afk = new ArrayList<>();
    private static final List<DoubleCheck> ignored = new ArrayList<>();
    private static final List<SingleCheck> vanished = new ArrayList<>();

    /**
     * Registers a function for testing if a player is AFK.
     *
     * @param plugin plugin registering
     * @param function function testing the player
     */
    public static void registerAfk(Plugin plugin, BoolFunction function) {
        afk.add(new SingleCheck(Objects.requireNonNull(plugin), Objects.requireNonNull(function)));
    }

    /**
     * Registers a function for testing if a player is AFK.
     *
     * @param plugin plugin registering
     * @param function function testing if, respectively, the viewer is ignoring the chatter
     */
    public static void registerIgnored(Plugin plugin, BoolBiFunction function) {
        ignored.add(new DoubleCheck(Objects.requireNonNull(plugin), Objects.requireNonNull(function)));
    }

    /**
     * Registers a function for testing if a player is vanished.
     *
     * @param plugin plugin registering
     * @param function function testing the player
     */
    public static void registerVanished(Plugin plugin, BoolFunction function) {
        vanished.add(new SingleCheck(Objects.requireNonNull(plugin), Objects.requireNonNull(function)));
    }

    public static boolean isAfk(Player player) {
        for (SingleCheck check : afk) {
            try {
                if (check.function.apply(player)) {
                    return true;
                }
            } catch (Exception e) {
                AbstractFactionsPlugin.instance().getLogger().log(Level.WARNING, "Could not check with " + check.plugin.getName() + " if player is afk!", e);
            }
        }
        return false;
    }

    public static boolean isIgnored(Player viewer, Player chatter) {
        for (DoubleCheck check : ignored) {
            try {
                if (check.function.apply(viewer, chatter)) {
                    return true;
                }
            } catch (Exception e) {
                AbstractFactionsPlugin.instance().getLogger().log(Level.WARNING, "Could not check with " + check.plugin.getName() + " if player is ignored!", e);
            }
        }
        return false;
    }

    public static boolean isVanished(Player player) {
        for (SingleCheck check : vanished) {
            try {
                if (check.function.apply(player)) {
                    return true;
                }
            } catch (Exception e) {
                AbstractFactionsPlugin.instance().getLogger().log(Level.WARNING, "Could not check with " + check.plugin.getName() + " if player is vanished!", e);
            }
        }
        return false;
    }
}
